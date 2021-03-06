package com.maoye.mlh_slotmachine.widget.banner.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.Scroller;

import com.maoye.mlh_slotmachine.widget.banner.ViewBanner;


public class BannerViewPager extends ViewPager {
    private boolean scrollable = true;

    private static final double MOVE_LIMITATION = 6;// 触发移动的像素距离
    private float mLastMotionX; // 手指触碰屏幕的最后一次x坐标
    private int currentItem;
    private float preX;

    public BannerViewPager(Context context) {
        super(context);

    }

    public BannerViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);

    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (this.scrollable) {
            if (getCurrentItem() == 0 && getChildCount() == 0) {
                return false;
            }
            return super.onTouchEvent(event);
        } else {
            return false;
        }
    }


/*
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // TODO Auto-generated method stub
        if (getCurrentItem() == 0 && getChildCount() == 0) {
            return false;
        }
        final int action = event.getAction();
        final float x = event.getX();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mLastMotionX = x;
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:

                if (Math.abs(x - mLastMotionX) >MOVE_LIMITATION) {
                    if (x - mLastMotionX < 0) {
                        scrollRight();
                    } else {
                        scrollLeft();
                    }
                    return true;
                }
                break;
            default:
                break;
        }
        return super.onTouchEvent(event);
    }*/
    public void scrollRight() {
        if (getChildCount() > 1) {
            currentItem = currentItem % (getChildCount() + 1) + 1;
            if (currentItem == 1) {
                setCurrentItem(currentItem, false);
            } else {
               setCurrentItem(currentItem);

            }
        }
    }

    public void scrollLeft() {
        if (getChildCount() > 1) {
            currentItem = getCurrentItem();
            if (currentItem > 1) {
                setCurrentItem(currentItem-1);
            } else if (currentItem == 1) {
                setCurrentItem(getChildCount(), false);
            } else {
                setCurrentItem(currentItem);
            }
        }
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (this.scrollable) {
            if (getCurrentItem() == 0 && getChildCount() == 0) {
                return false;
            }
            boolean res = super.onInterceptTouchEvent(ev);
            if (ev.getAction() == MotionEvent.ACTION_DOWN) {
                preX = ev.getX();
            } else {
                if (Math.abs(ev.getX() - preX) >MOVE_LIMITATION) {
                    return true;
                } else {
                    preX = ev.getX();
                }
            }
            return res;

        } else {
            return false;
        }
    }

    public void setScrollable(boolean scrollable) {
        this.scrollable = scrollable;
    }
}
