package com.hsw.adsorption;

import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

/**
 * @author heshuai
 * created on: 2020-03-16 11:30
 * description: 可拖动可吸附左右边界的View
 */
public class AdsorptionView extends RelativeLayout {

    private View dragView;

    /**
     * 最后一次按下去的X、Y位置
     */
    private int lastX, lastY;

    /**
     * 可设置距离边界的距离，正负值均可，单位为dp
     */
    private int hideSize = 10;

    public AdsorptionView(Context context) {
        this(context, null);
    }

    public AdsorptionView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AdsorptionView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate(context, R.layout.layout_item_view, this);
        dragView = findViewById(R.id.iv_image);
        hideSize = dip2px(context, hideSize);
        initView();
    }

    private void initView() {
        dragView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        lastX = (int) event.getRawX();
                        lastY = (int) event.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        int mTop = dragView.getTop();
                        int mLeft = dragView.getLeft();
                        int mBottom = dragView.getBottom();
                        int mRight = dragView.getRight();

                        //X轴拖动的距离
                        int slideX = (int) (event.getRawX() - lastX);
                        //Y轴拖动的距离
                        int slideY = (int) (event.getRawY() - lastY);

                        if (mTop < 0) {
                            mTop = 0;
                            mBottom = dragView.getHeight();
                        }

                        int rootHeight = getHeight();
                        int rootWidth = getWidth();

                        //为上下左右做边界限制
                        if (mBottom > rootHeight) {
                            mBottom = rootHeight;
                            mTop = rootHeight - dragView.getHeight();
                        }

                        if (mLeft < -hideSize) {
                            mLeft = -hideSize;
                            mRight = dragView.getWidth() - hideSize;
                        }

                        if (mRight > rootWidth + hideSize) {
                            mRight = rootWidth + hideSize;
                            mLeft = rootWidth - dragView.getWidth() + hideSize;
                        }

                        dragView.layout(mLeft + slideX, mTop + slideY, mRight + slideX, mBottom + slideY);
                        lastX = (int) event.getRawX();
                        lastY = (int) event.getRawY();
                        break;
                    case MotionEvent.ACTION_UP:
                        int leftLimit = (getWidth() - dragView.getWidth()) / 2;
                        //在最中间的时候动画所需最大执行时间
                        int maxDuration = 500;
                        int duration;
                        if (dragView.getLeft() < leftLimit) {
                            //根据距离边界的距离，弹性计算动画执行时间，防止距离边界很近的时候执行时间仍是过长
                            duration = maxDuration * (dragView.getLeft() + hideSize) / (leftLimit + hideSize);
                            animSlide(dragView, dragView.getLeft(), -hideSize, duration);
                        } else {
                            duration = maxDuration * (getWidth() + hideSize - dragView.getRight()) / (leftLimit + hideSize);
                            animSlide(dragView, dragView.getLeft(), getWidth() - dragView.getWidth() + hideSize, duration);
                        }
                        break;
                        default:break;
                }
                return true;
            }
        });
    }

    private void animSlide(final View view, final int leftFrom, int leftTo, int duration) {
        ValueAnimator valueAnimator = ValueAnimator.ofInt(leftFrom, leftTo);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int viewLeft = (int) valueAnimator.getAnimatedValue();
                view.layout(viewLeft, view.getTop(), viewLeft + view.getWidth(), view.getBottom());
            }
        });
        //为防止溢出边界时，duration时间为负值，做下0判断
        valueAnimator.setDuration(duration < 0 ? 0 : duration);
        valueAnimator.start();
    }

    public int dip2px(Context paramContext, float paramFloat) {
        return (int) (0.5F + paramFloat
                * paramContext.getResources().getDisplayMetrics().density);
    }
}
