package com.west.develop.westapp.CustomView.QuikeSideBarView;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.west.develop.westapp.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 快速选择侧边栏
 * Created by Sai on 16/3/25.
 */
public class QuickSideBarView extends View {

    private OnQuickSideBarTouchListener listener;
    private List<Character> mLetters;
    private int mChoose = -1;
    private Paint mPaint = new Paint();
    private float mTextSize;
    private float mTextSizeChoose;
    private int mTextColor;
    private int mTextColorChoose;
    private int mWidth;
    private int mHeight;
    private float mItemHeight;
   // private float mItemStartY = 0;

    public QuickSideBarView(Context context) {
        this(context, null);
    }

    public QuickSideBarView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public QuickSideBarView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        List<String> letters = Arrays.asList(mContext.getResources().getStringArray(R.array.quickSideBarLetters));
        mLetters = new ArrayList<>();

        for(int i = 0;i < letters.size();i++){
            if(!mLetters.contains(letters.get(i).charAt(0))){
                mLetters.add(letters.get(i).charAt(0));
            }
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mTextColor = mContext.getResources().getColor(android.R.color.black, null);
            mTextColorChoose = mContext.getResources().getColor(android.R.color.black, null);
        }
        else{
            mTextColor = mContext.getResources().getColor(android.R.color.black);
            mTextColorChoose = mContext.getResources().getColor(android.R.color.black);
        }

        mTextSize = mContext.getResources().getDimensionPixelSize(R.dimen.textSize_quicksidebar);
        mTextSizeChoose = mContext.getResources().getDimensionPixelSize(R.dimen.textSize_quicksidebar_choose);
        //mItemHeight = context.getResources().getDimension(R.dimen.height_quicksidebaritem);
        //横屏
        if (newConfig.orientation == getResources().getConfiguration().ORIENTATION_LANDSCAPE){
            mItemHeight = getResources().getDimension(R.dimen.height_quicksidebaritem_vertical);

        }else if (newConfig.orientation == getResources().getConfiguration().ORIENTATION_PORTRAIT){//竖屏
            mItemHeight = getResources().getDimension(R.dimen.height_quicksidebaritem);
        }
        if (mattrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(mattrs, R.styleable.QuickSideBarView);

            mTextColor = a.getColor(R.styleable.QuickSideBarView_sidebarTextColor, mTextColor);
            mTextColorChoose = a.getColor(R.styleable.QuickSideBarView_sidebarTextColorChoose, mTextColorChoose);
            mTextSize = a.getDimension(R.styleable.QuickSideBarView_sidebarTextSize, mTextSize);
            mTextSizeChoose = a.getDimension(R.styleable.QuickSideBarView_sidebarTextSizeChoose, mTextSizeChoose);
            mItemHeight = a.getDimension(R.styleable.QuickSideBarView_sidebarItemHeight, mItemHeight);
            a.recycle();
        }

        super.onConfigurationChanged(newConfig);

    }

    private AttributeSet mattrs;
    private Context mContext;
    private void init(Context context, AttributeSet attrs) {
        mattrs = attrs;
        mContext = context;
        onConfigurationChanged(getResources().getConfiguration());
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (int i = 0; i < mLetters.size(); i++) {
            mPaint.setColor(mTextColor);

            mPaint.setAntiAlias(true);
            mPaint.setTextSize(mTextSize);
            if (i == mChoose) {
                mPaint.setColor(mTextColorChoose);
                mPaint.setFakeBoldText(true);
                mPaint.setTypeface(Typeface.DEFAULT_BOLD);
                mPaint.setTextSize(mTextSizeChoose);
            }

            //计算位置
            Rect rect = new Rect();
            mPaint.getTextBounds(String.valueOf(mLetters.get(i)), 0, String.valueOf(mLetters.get(i)).length(), rect);
            float xPos = (int) ((mWidth - rect.width()) * 0.5);
            float yPos = mItemHeight * i + /*(int)((mItemHeight - rect.height()) * 1) +*/ mItemHeight;

            canvas.drawText(String.valueOf(mLetters.get(i)), xPos, yPos, mPaint);
            mPaint.reset();
        }

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mHeight = getMeasuredHeight();
        mWidth = getMeasuredWidth();
        //mItemStartY = (mHeight - mLetters.size()*mItemHeight)/2;

        /*
        设置宽度
         */
        int specMode = MeasureSpec.getMode(heightMeasureSpec);
        int specSize = MeasureSpec.getSize(heightMeasureSpec);
        //父控件允许子控件的最大宽度,包括子控件设置的具体值或者fill_parent
        if(specMode == MeasureSpec.EXACTLY) { //fill_parent，也相当于设置了exactly
            mHeight = specSize;
        } else {
            //子控件需要的宽度
            int desiredSize = (int) (getPaddingTop() + getPaddingBottom() + mLetters.size() * mItemHeight);
            if(specMode == MeasureSpec.AT_MOST) { //wrap_content
                //两者取最小
                desiredSize = Math.min(specSize, desiredSize);
            }
            mHeight = desiredSize;
        }

        setMeasuredDimension(widthMeasureSpec, mHeight);

    }



    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        final int action = event.getAction();
        final float y = event.getY();
        final int oldChoose = mChoose;
        final int newChoose = (int) ((y - mItemHeight) / mItemHeight);
        switch (action) {
            case MotionEvent.ACTION_UP:
                mChoose = -1;
                if (listener != null) {
                    listener.onLetterTouching(false);
                }
                invalidate();
                break;
            default:
                if (oldChoose != newChoose) {
                    if (newChoose >= 0 && newChoose < mLetters.size()) {
                        mChoose = newChoose;
                        if (listener != null) {
                            //计算位置
                            Rect rect = new Rect();
                            mPaint.getTextBounds(String.valueOf(mLetters.get(mChoose)), 0, String.valueOf(mLetters.get(mChoose)).length(), rect);
                            float yPos = mItemHeight * mChoose + /*(int) ((mItemHeight - rect.height()) * 0.5) +*/ mItemHeight;
                            listener.onLetterChanged(mLetters.get(newChoose), mChoose, yPos);
                        }
                    }
                    invalidate();
                }
                //如果是cancel也要调用onLetterUpListener 通知
                if (event.getAction() == MotionEvent.ACTION_CANCEL) {
                    if (listener != null) {
                        listener.onLetterTouching(false);
                    }
                } else if (event.getAction() == MotionEvent.ACTION_DOWN) {//按下调用 onLetterDownListener
                    if (listener != null) {
                        listener.onLetterTouching(true);
                    }
                }

                break;
        }
        return false;
    }

    public OnQuickSideBarTouchListener getListener() {
        return listener;
    }

    public void setOnQuickSideBarTouchListener(OnQuickSideBarTouchListener listener) {
        this.listener = listener;
    }

    public void setChooseLetter(char letter){
        for(int i = 0;i < mLetters.size();i++){
            if(letter == mLetters.get(i)){
                mChoose = i;
                invalidate();
                break;
            }
        }
    }

    public int getLetterY(char letter){
        for(int i = 0 ;i < mLetters.size();i++){
            if(mLetters.get(i) == letter){
                float yPos = mItemHeight * i + /*(int) ((mItemHeight - rect.height()) * 0.5) +*/ mItemHeight;
                return (int)yPos;
            }
        }

        return -1;
    }

    public int getItemHeight(){
        return (int)mItemHeight;
    }

    public List<Character> getLetters() {
        return mLetters;
    }

    /**
     * 设置字母表
     * @param letters
     */
    public void setLetters(List<Character> letters) {
        this.mLetters = letters;
        invalidate();
    }
}

