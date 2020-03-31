package com.west.develop.westapp.CustomView.QuikeSideBarView;

/**
 * Created by Sai on 16/3/25.
 */
public interface OnQuickSideBarTouchListener {
    void onLetterChanged(char letter, int position, float y);
    void onLetterTouching(boolean touching);
}
