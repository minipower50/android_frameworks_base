/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Slog;
import android.widget.LinearLayout;
import android.view.GestureDetector;
import android.view.MotionEvent;

public class ExpandedView extends LinearLayout {
    private static final int SWIPE_MIN_DISTANCE = 200;
    private static final int SWIPE_THRESHOLD_VELOCITY = 800;

    PhoneStatusBar mService;
    int mPrevHeight = -1;

    private final GestureDetector mGestureDetector;
    private Runnable mSwipeUpCallback = null;
    private final Handler mHandler = new Handler();

    public ExpandedView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mGestureDetector = new GestureDetector(context,
                new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onFling(MotionEvent e1, MotionEvent e2, float vX, float vY) {
                        if (e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE && Math.abs(vY)
                                > SWIPE_THRESHOLD_VELOCITY) {
                            if (mSwipeUpCallback != null) {
                                mHandler.post(mSwipeUpCallback);
                                return true;
                            }
                        }
                        return false;
                    }
        });
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    /** We want to shrink down to 0, and ignore the background. */
    @Override
    public int getSuggestedMinimumHeight() {
        return 0;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (mSwipeUpCallback != null) {
            boolean handled = mGestureDetector.onTouchEvent(event);
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_OUTSIDE:
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    return handled;
                case MotionEvent.ACTION_MOVE:
            }
        }
        return super.onInterceptTouchEvent(event);
    }

    public void setOnSwipeUpCallback(Runnable callback) {
        mSwipeUpCallback = callback;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
         super.onLayout(changed, left, top, right, bottom);
         int height = bottom - top;
         if (height != mPrevHeight) {
             if (PhoneStatusBar.DEBUG) {
                 Slog.d(PhoneStatusBar.TAG, "ExpandedView height changed old=" + mPrevHeight
                      + " new=" + height);
             }
             mPrevHeight = height;
             mService.updateExpandedViewPos(PhoneStatusBar.EXPANDED_LEAVE_ALONE);
         }
     }
}
