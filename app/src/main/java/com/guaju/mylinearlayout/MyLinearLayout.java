package com.guaju.mylinearlayout;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;
import android.widget.LinearLayout;
import android.widget.OverScroller;
import android.widget.Scroller;

import static android.content.ContentValues.TAG;

/**
 * Created by guaju on 2017/10/18.
 * OverScroller  管理滑动和是否滑动超出边界这样的滑动类
 * 1：能够监听是否处于滑动状态 computeScrollOffset
 * 2：判断是否滑动结束    isFinished
 * 3：控制控件开始滑动 startScroll
 * 4：控制控件的Fling操作fling
 * 5: 取消滑动abortAnimation
 *
 * ViewConfiguration android自带的具备view的一些特性的类
 * 1：getScaledDoubleTapSlop触摸和滑动的临界值
 * 2：getScaledMaximumFlingVelocity()最大的滑翔速度
 * 3：getScaledMinimumFlingVelocity()最小的滑翔速度
 *
 * VelocityTracker 速度跟踪器，我们这里面配合overscroller使用
 * fling的时候使用了
 * 1:创建方法 VelocityTracker.obtain()
 * 2:释放方法   mVelocityTracker.recycle();mVelocityTracker = null;
 * 3:给速度跟踪器添加一个motion event：mVelocityTracker.addMovement(event);
 * 4:拿到当前的速度    mVelocityTracker.getYVelocity();拿到y方向的速度
 * ，这个值有正负表示向上或者向下滑动
 *
 * invalidate    postInvalidate
 * 对界面进行重绘 因为有时你做了界面更新，view并没有更新
 *
 *
 *
 *
 *
 */

public class MyLinearLayout extends LinearLayout {
    private int mMaximumVelocity, mMinimumVelocity;
    VelocityTracker mVelocityTracker;

    Scroller   scroller;

    OverScroller overScroller;    //超出越出的意思


    private float y;
    private float lastY;
    private float dy;
    //设定手指滑动或者点击的临界值
    private int touchslop;
    private OverScroller mScroller;
    public MyLinearLayout(Context context) {
        super(context);
        setOrientation(LinearLayout.VERTICAL);
        touchslop= ViewConfiguration.get(getContext()).getScaledDoubleTapSlop();
    }
    public MyLinearLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setOrientation(LinearLayout.VERTICAL);
        mScroller=new OverScroller(getContext());
        mMaximumVelocity = ViewConfiguration.get(context)
                .getScaledMaximumFlingVelocity();
        mMinimumVelocity = ViewConfiguration.get(context)
                .getScaledMinimumFlingVelocity();

    }

//    @Override
//    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec),2*MeasureSpec.getSize(heightMeasureSpec));
//    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }

    private void initVelocityTrackerIfNotExists(MotionEvent event) {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //初始化速度跟踪器，并传递给他事件
        initVelocityTrackerIfNotExists(event);


        //监听手指的动作
        int action = event.getAction();
        //获取当前的y坐标
        y = event.getY();
        switch (action){
            case MotionEvent.ACTION_DOWN:
                y = event.getY();
                //定义一个变量来存储滑动之前的y值
                lastY = y;
                //当break的时候，先跳出switch 然后return false  事件终止
                //当return false的时候直接事件终止
                //只有当return true的时候才会进入move事件，move事件执行完之后，事件终止
                return true;

            case MotionEvent.ACTION_MOVE:
//                Log.e(TAG, "onTouchEvent: move事件" );
                //拿到手指滑动的距离
                dy = y - lastY;
                //如果他的范围小于touchslop,不处理，大于才处理
                if (Math.abs(dy)>touchslop){
                    //通过scrollby去让他跟着手走

                    scrollBy(0, -(int) dy);
//                    ((View)getParent()).scrollBy(0, -(int) dy);
//                    Log.e(TAG, "onTouchEvent: " );
                }
                lastY=y;
                break;
            case MotionEvent.ACTION_CANCEL:
                if (!mScroller.isFinished()){
                    mScroller.abortAnimation();
                    Log.e(TAG, "onTouchEvent: 取消了" );
                }

                //释放速度跟踪器
                recycleVelocityTracker();
            case MotionEvent.ACTION_UP:
                //计算了下当前的速度
                mVelocityTracker.computeCurrentVelocity(1000,mMaximumVelocity);
                int yVelocity = (int) mVelocityTracker.getYVelocity();
                if (Math.abs(yVelocity)>mMinimumVelocity){

                    mScroller.fling(0,getScrollY(),0,-yVelocity,0,0,0,yVelocity>0?-1000:-20);
                    //重新调用ondraw,做界面的重绘
                    invalidate();
                }


                break;
        }
        boolean b = super.onTouchEvent(event);
//        Log.e(TAG, "onTouchEvent: "+b );
        return b;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;
    }

    //自动滑动效果
    @Override
    public void scrollTo(int x, int y) {

        if (y > 200) {
            y = 200;
        }
        int scrollY = getScrollY();
        if (y !=scrollY ) {
            super.scrollTo(x, y);

        }
        //设置是否是隐藏
//        isTopHidden = getScrollY() == mTopViewHeight;

    }
////让你的滚动有平移动画的效果,如果不写的话
    @Override
    public void computeScroll() {
        // 如果还没有滚动完就让他接着滚
        if (mScroller.computeScrollOffset()) {
            scrollTo(0, mScroller.getCurrY());
            invalidate();
        }
    }

    private void recycleVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

}
