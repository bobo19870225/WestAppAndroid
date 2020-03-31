package com.west.develop.westapp.CustomView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;

/**
 * Created by Develop11 on 2017/11/23.
 */

public class ScreenView extends View {
    public static final int _TOTAL_LINES = 160;
    public static final int _TOTAL_COLUMNS = 160;

    public static final int _PAGE_HEIGHT = 8;

    public static final int _TYPE_TEXT_12x12 = 1;
    public static final int _TYPE_TEXT_6x8 = 2;

    /**
     * 字体
     */
    private Typeface mTypeFace = Typeface.MONOSPACE;

    /**
     * 保存显示
     */
    private Bitmap mBitmap;
    private Paint mPaint;

    /**
     * 内容尺寸
     */
    private int mWidth;
    private int mHeight;


    /**
     * 列宽和行高
     */
    private float mLineHeight;
    private float mColumnWidth;

    /**
     * 正显 字体背景
     */
    private int bgColor = Color.parseColor("#FFFFFF");

    /**
     * 待显示内容
     */
    private ArrayList<TextItem> mTexts = new ArrayList<>();

    /**
     * 进度条显示与进度
     */
    private boolean mProgressShow = false;
    private int mProgress = 0;

    public ScreenView(Context context){
        super(context);
        init();
    }

    public ScreenView(Context context, AttributeSet attrs){
        super(context,attrs);
        init();
    }

    public ScreenView(Context context, AttributeSet attrs, int defStyleAttr){
        super(context,attrs,defStyleAttr);
        init();
    }

    private  void init(){
        mPaint = new Paint();
        mPaint.setStrokeCap(Paint.Cap.SQUARE);
        mPaint.setColor(Color.BLUE);
        mPaint.setAntiAlias(true);
        mPaint.setTypeface(mTypeFace);
    }


    /**
     * 清除屏幕
     */
    public void Clr_Scr(){
        mBitmap = null;
        mTexts.clear();
        mProgressShow = false;
        mProgress = 0;
        invalidate();
    }

    /**
     * 显示 12 * 12 字库里面的汉字或字符
     * @param PAG   行的页（0,2,4,6,8,10,12,14,16,18）
     * @param COL   列（0 - 159）
     * @param NOT_DISP  =0:正显  ;    !=0: 反显
     * @param STR_LEN
     * @param STRING
     */
    public void GENERAL_CN_EN_STR(int PAG,int COL,int NOT_DISP,final int STR_LEN,final String STRING){
        int lenght = STR_LEN;
        String disp_STR = STRING;
        if (lenght != 0){
            if (lenght > disp_STR.length()){
                lenght = disp_STR.length();
            }
            disp_STR = disp_STR.substring(0,lenght);
        }
        Log.e("GENERAL_disp_STR", disp_STR);
        mTexts.add(new TextItem(PAG,COL,NOT_DISP != 0,disp_STR,_TYPE_TEXT_12x12));

        invalidate();
    }

    /**
     * 显示 M128 0x10000后的 FLASH 常量型 12 * 12 汉字和 SRAM 文字
     * @param PAG   行的页（0,2,4,6,8,10,12,14,16,18）
     * @param COL   列（0 - 159）
     * @param NOT_DISP  =0:正显  ;    !=0: 反显
     * @param STR_LEN
     * @param STRING
     */
    public void SPECIFY_CN_EN_STR( int PAG, int COL,final int NOT_DISP, final int STR_LEN, final String STRING){
        int lenght = STR_LEN;
        String disp_STR = STRING;
        if (lenght != 0){
            if (lenght > disp_STR.length()){
                    lenght = disp_STR.length();
            }
            disp_STR = disp_STR.substring(0,lenght);
        }
        Log.e("SPECIFY_disp_STR", disp_STR);
        mTexts.add(new TextItem(PAG,COL,NOT_DISP != 0,disp_STR,_TYPE_TEXT_12x12));

        invalidate();
    }

    /**
     * 显示 6 * 8 字符串
     * @param PAG   行的页（0 - 19）
     * @param COL   列（0 - 159）
     * @param NOT_DISP  =0:正显  ;    !=0: 反显
     * @param STR_LEN
     * @param STRING
     */
    public void ASCII_6x8( int PAG, int COL,final int NOT_DISP,final int STR_LEN,final String STRING){
        int lenght = STR_LEN;
        String disp_STR = STRING;
        if (lenght != 0){
            if (lenght > disp_STR.length()){
                lenght = disp_STR.length();
            }
            disp_STR = disp_STR.substring(0,lenght);
        }
        mTexts.add(new TextItem(PAG,COL,NOT_DISP != 0,disp_STR,_TYPE_TEXT_6x8));
        invalidate();
    }

    /**
     * 显示进度条
     */
    public void ProgressFrame(){
        mProgressShow = true;
        invalidate();
    }


    /**
     * 更新进度
     * @param progres
     */
    public void onProgress(int progres){
        mProgressShow = true;
        if(progres >= 0 && progres <= 100) {
            mProgress = progres;
        }
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        /*
        * 设置宽度
        */
        mWidth = MeasureSpec.getSize(widthMeasureSpec) - getPaddingLeft() - getPaddingRight();
        mHeight = MeasureSpec.getSize(heightMeasureSpec) - getPaddingBottom() - getPaddingTop();


        /**
         * 获取列的宽度和行的高度
         */
        mColumnWidth = (float) Math.ceil((float)mWidth / _TOTAL_COLUMNS);
        mLineHeight =(float) Math.ceil ((float)mHeight / _TOTAL_LINES);

    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        /**
         * 显示之前的屏幕
         */
        if (mBitmap != null) {
            canvas.drawBitmap(mBitmap, 0, 0, mPaint);
        }

        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();

        canvas.translate(paddingLeft,paddingTop);

        /**
         * 显示进度条和进度
         */
        if(mProgressShow){
            mPaint.setColor(Color.DKGRAY);                    //设置画笔颜色
            float[] pts={30 * mColumnWidth,4 * _PAGE_HEIGHT * mLineHeight,130 * mColumnWidth,4 * _PAGE_HEIGHT * mLineHeight,
                    130 * mColumnWidth,4 * _PAGE_HEIGHT * mLineHeight,130 * mColumnWidth,5 * _PAGE_HEIGHT * mLineHeight,
                    130 * mColumnWidth,5 * _PAGE_HEIGHT * mLineHeight,30 * mColumnWidth,5 * _PAGE_HEIGHT * mLineHeight,
                    30 * mColumnWidth,5 * _PAGE_HEIGHT * mLineHeight,30 * mColumnWidth,4 * _PAGE_HEIGHT * mLineHeight};

            mPaint.setStrokeWidth((float) 2.0);              //线宽
            canvas.drawLines(pts, mPaint);

            Rect progressRect = new Rect((int)(30 * mColumnWidth),(int)(4 * _PAGE_HEIGHT * mLineHeight),(int)((30 + mProgress) * mColumnWidth),(int)(5 * _PAGE_HEIGHT * mLineHeight));
            canvas.drawRect(progressRect,mPaint);
        }

        /**
         * 显示文字
         */
        for (int i = 0; i < mTexts.size(); i++) {
            TextItem item = mTexts.get(i);

            if (item.isText_12x12()) {
                /**
                 * 显示12x12的文字
                 */
                mPaint.setTextSize(mColumnWidth * 13);
                mPaint.setTextScaleX((float)1.0);

                float firstCol = 0;
                if(item.STRING.length() > 0){
                    firstCol = mPaint.measureText(String.valueOf(item.STRING.charAt(0))) / mColumnWidth;
                }

                int PAG = item.PAG;
                int COL = item.COL;
                while (firstCol + COL > _TOTAL_COLUMNS) {
                    if (COL <= _TOTAL_COLUMNS) {
                        COL = 0;
                        COL += 2;
                    } else {
                        COL = COL - _TOTAL_COLUMNS;
                        PAG += 2;
                    }
                }
                String splitStr = splitText(item.STRING, COL);

                /**
                 * 分行
                 */
                String[] strArray = splitStr.split("\n");
                for (int m = 0; m < strArray.length; m++) {
                    Rect rect;
                    if (m == 0) {
                        rect = new Rect((int) (COL * mColumnWidth), (int) (Math.rint(PAG * _PAGE_HEIGHT * mLineHeight)), mWidth, mHeight);
                    } else {
                        rect = new Rect(0, (int) (Math.ceil(PAG * _PAGE_HEIGHT * mLineHeight)), mWidth, mHeight);
                    }

                    if (!item.NOTDisp) {
                        /**
                         * 正显
                         */
                        mPaint.setColor(bgColor);
                        canvas.drawRect(rect.left, rect.top, rect.left + mPaint.measureText(strArray[m]), rect.top + (int) (2 * _PAGE_HEIGHT * mLineHeight), mPaint);
                        mPaint.setColor(Color.BLACK);
                        canvas.drawText(strArray[m], rect.left, rect.top + mLineHeight * 12, mPaint);
                    } else {
                        /**
                         * 反显
                         */
                        mPaint.setColor(Color.DKGRAY);
                        canvas.drawRect(rect.left, rect.top, rect.left + mPaint.measureText(strArray[m]), rect.top + (int) (2 * _PAGE_HEIGHT * mLineHeight), mPaint);
                        mPaint.setColor(Color.WHITE);
                        canvas.drawText(strArray[m], rect.left, rect.top + mLineHeight * 12, mPaint);
                    }
                    PAG += 2;
                }


            } else if (item.isText_6x8()) {
                /**
                 * 显示6x8的文字
                 */
                mPaint.setTextSize((float)(Math.ceil(mColumnWidth * 6)));
                mPaint.setTextScaleX((float)1.65);

                float letterWidth = mPaint.measureText(String.valueOf(item.STRING.charAt(0)));
                float firstCol =  letterWidth / (int)mColumnWidth;

                int PAG = item.PAG;
                int COL = item.COL;
                while (firstCol + COL > _TOTAL_COLUMNS) {
                    if (COL <= _TOTAL_COLUMNS) {
                        COL = 0;
                        COL += 1;
                    } else {
                        COL = COL - _TOTAL_COLUMNS;
                        PAG += 1;
                    }
                }
                String splitStr = splitText(item.STRING, COL);

                /**
                 * 分行
                 */
                String[] strArray = splitStr.split("\n");
                for (int m = 0; m < strArray.length; m++) {
                    Rect rect;
                    if (m == 0) {
                        rect = new Rect((int) (COL * mColumnWidth), (int) (Math.rint(PAG * _PAGE_HEIGHT * mLineHeight)), mWidth, mHeight);
                    } else {
                        rect = new Rect(0, (int) (Math.ceil(PAG * _PAGE_HEIGHT * mLineHeight)), mWidth, mHeight);
                    }
                    if (!item.NOTDisp) {
                        /**
                         * 正显
                         */
                        mPaint.setColor(bgColor);
                        canvas.drawRect(rect.left, rect.top, rect.left + mPaint.measureText(strArray[m]), rect.top + (int) (_PAGE_HEIGHT * mLineHeight), mPaint);
                        mPaint.setColor(Color.BLACK);
                        canvas.drawText(strArray[m], rect.left, rect.top + mLineHeight * 6, mPaint);
                    } else {
                        /**
                         * 反显
                         */
                        mPaint.setColor(Color.DKGRAY);
                        canvas.drawRect(rect.left, rect.top, rect.left + mPaint.measureText(strArray[m]), rect.top + (int) (_PAGE_HEIGHT * mLineHeight), mPaint);
                        mPaint.setColor(Color.WHITE);
                        canvas.drawText(strArray[m], rect.left, rect.top + mLineHeight * 6, mPaint);
                    }
                    PAG += 2;
                }
            }
        }
        if (!isDrawingCacheEnabled()) {
            convertViewToBitmap();
        }
    }

    /**
     * 将空间的画面保存在 Bitmap
     * 在下次显示时，直接显示该Bitmap
     */
    public void convertViewToBitmap(){
        setDrawingCacheEnabled(true);
        buildDrawingCache(true);
        Bitmap bitmap = getDrawingCache(true);
        if (null == bitmap || bitmap.isRecycled()){
            return ;
        }
        int bmpSrcWidth = bitmap.getWidth();
        int bmpSrcHeight = bitmap.getHeight();
        mBitmap = Bitmap.createBitmap(bmpSrcWidth, bmpSrcHeight, Bitmap.Config.ARGB_8888);
        if (null != mBitmap) {
            Canvas canvas = new Canvas(mBitmap);
            final Rect rect = new Rect(0, 0, bmpSrcWidth, bmpSrcHeight);
            canvas.drawBitmap(bitmap, rect, rect, null);
        }
        mTexts.clear();
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
            bitmap = null;
        }
        setDrawingCacheEnabled(false);
    }

    protected String splitText(String sStr, int COL){
        int strLen = sStr.length();
        int lineWidth = (int)(COL * mColumnWidth);
        StringBuilder sbNewText = new StringBuilder();

        for (int index = 0; index < strLen; index++) {
            char ch = sStr.charAt(index);

            if (ch == '\r' || ch == '\n') {
                lineWidth = 0;
                sbNewText.append(ch);
                continue;
            }

            float scaleX = mPaint.getTextScaleX();
            float letterSpace = mPaint.getLetterSpacing();

            float textSize = mPaint.getTextSize();
            float size = mPaint.measureText(String.valueOf(ch));

            lineWidth += size;

            //防止出现多个接连的 换行符
            if (ch == '\n' || ch == '\r') {
                if (sbNewText.charAt(sbNewText.length() - 1) != '\n') {

                    sbNewText.append(sStr.charAt(index));
                }
                lineWidth = 0;
            } else if (lineWidth <= mWidth) {
                sbNewText.append(sStr.charAt(index));
            } else {
                lineWidth = 0;

                if (index < sStr.length() - 1) {
                    char ch_Next = sStr.charAt(index + 1);
                    if (ch_Next != '\n' && ch_Next != '\r') {
                        sbNewText.append("\n");
                        index--;
                    } else {
                        sbNewText.append(ch);
                    }
                } else {

                    sbNewText.append("\n");
                    index--;
                }
            }
        }

        return new String(sbNewText);
    }

    class TextItem{
        int PAG;
        int COL;
        boolean NOTDisp = false;
        String STRING;
        int TYPE;

        public TextItem(int pag, int col, boolean isNotDisp, String string, int type){
            this.PAG = pag;
            this.COL = col;
            this.NOTDisp = isNotDisp;
            this.STRING = string;
            this.TYPE = type;
        }

        public boolean isText_12x12(){
            return TYPE == _TYPE_TEXT_12x12;
        }

        public boolean isText_6x8(){
            return TYPE == _TYPE_TEXT_6x8;
        }
    }

}
