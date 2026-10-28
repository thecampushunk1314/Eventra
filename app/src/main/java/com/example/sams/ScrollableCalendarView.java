package com.example.sams;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.ViewParent;
import android.widget.CalendarView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * A CalendarView subclass that allows smooth vertical scrolling when placed
 * inside a ScrollView or NestedScrollView.
 *
 * In standard Android CalendarView, vertical drag gestures are swallowed by internal
 * subviews, preventing the parent ScrollView/NestedScrollView from scrolling.
 * This class detects vertical scroll gestures and delegates them to the parent,
 * while preserving horizontal paging for month navigation and tap-to-select dates.
 */
public class ScrollableCalendarView extends CalendarView {

    private float startX;
    private float startY;
    private int touchSlop;

    public ScrollableCalendarView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public ScrollableCalendarView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ScrollableCalendarView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        ViewParent parent = getParent();
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                startX = ev.getX();
                startY = ev.getY();
                if (parent != null) {
                    parent.requestDisallowInterceptTouchEvent(false);
                }
                break;

            case MotionEvent.ACTION_MOVE:
                float dx = Math.abs(ev.getX() - startX);
                float dy = Math.abs(ev.getY() - startY);

                if (dy > touchSlop && dy > dx) {
                    // Vertical drag: release interception to parent NestedScrollView
                    if (parent != null) {
                        parent.requestDisallowInterceptTouchEvent(false);
                    }
                } else if (dx > touchSlop && dx > dy) {
                    // Horizontal drag: keep on calendar for month flipping
                    if (parent != null) {
                        parent.requestDisallowInterceptTouchEvent(true);
                    }
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (parent != null) {
                    parent.requestDisallowInterceptTouchEvent(false);
                }
                break;
        }
        return super.dispatchTouchEvent(ev);
    }
}
