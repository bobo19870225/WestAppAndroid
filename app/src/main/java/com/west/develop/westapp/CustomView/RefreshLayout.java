package com.west.develop.westapp.CustomView;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.AbsListView;
import android.widget.ListView;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.west.develop.westapp.R;

/**
 * Created by Develop12 on 2017/10/28.
 */

public class RefreshLayout extends SwipeRefreshLayout implements AbsListView.OnScrollListener {

    // listview
    private ListView mListView;
    // 上拉接口监听器, 到了最底部的上拉加载操作
    private OnLoadListener mOnLoadListener;
    // ListView的加载中footer
    private View mListViewFooter;
    // 是否在加载中 ( 上拉加载更多 )
    private boolean isLoading = false;
    // 按下时的y坐标
    private int mYDown;
    // 抬起时的y坐标
    private int mLastY;
    // 滑动到最下面时的上拉操作
    private int mTouchSlop;

    private ChildScrollListener childScrollListener;


    public RefreshLayout(Context context) {
        super(context);
    }

    public RefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mListViewFooter = LayoutInflater.from(context).inflate(R.layout.listview_footer, null, false);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        Log.e("RefreshLayout", "RefreshLayout: " + mTouchSlop);

    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        // 初始化ListView对象
        if (mListView == null) {
            getListView();
        }

    }

    private void getListView() {
        int childs = getChildCount();
        if (childs > 0) {
            for(int i = 0;i < childs;i++){
                View childView = getChildAt(i);
                if (childView instanceof ListView) {
                    mListView = (ListView) childView;
                    mListView.setOnScrollListener(this);
                    break;
                }
            }

        }

    }

    public boolean isLoading() {
        return isLoading;
    }

    // 设置加载状态,添加或者移除加载更多圆形进度条
    public void setLoading(boolean loading) {
        if(isLoading == loading){
            return;
        }
        /*if(isLoading){
            setLoading(false);
        }*/
        isLoading = loading;
        if (isLoading) {
            mListView.addFooterView(mListViewFooter);
        } else {
            mListView.removeFooterView(mListViewFooter);
            // 重置滑动的坐标
            mYDown = 0;
            mLastY = 0;
        }
    }

    private boolean canLoadMore() {
        // 1. 是上拉状态
        boolean condition1 = (mYDown - mLastY) >= mTouchSlop;

        // 2. 当前页面可见的item是最后一个条目
        boolean condition2 = false;
        if (mListView != null && mListView.getAdapter() != null) {
            condition2 = mListView.getLastVisiblePosition() == (mListView.getAdapter().getCount() - 1);
        }
        // 3. 正在加载状态
        boolean condition3 = !isLoading;
        return condition1 && condition2 && condition3;
    }

    public void setOnLoadListener(OnLoadListener mOnLoadListener) {
        this.mOnLoadListener = mOnLoadListener;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (canLoadMore()) {
            if (mOnLoadListener != null) {
                setLoading(true);
                mOnLoadListener.onLoad();
            }
        }
        if(childScrollListener != null){
            childScrollListener.onScrollStateChanged(view,scrollState);
        }

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if(childScrollListener != null){
            childScrollListener.onScroll(view,firstVisibleItem,visibleItemCount,totalItemCount);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        final int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                //按下
                mYDown = (int) ev.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                //移动
                mLastY = (int) ev.getRawY();

                break;
            case MotionEvent.ACTION_UP:
                //抬起
                if (canLoadMore()) {
                    if (mOnLoadListener != null) {
                        setLoading(true);
                        mOnLoadListener.onLoad();
                    }
                }

                break;
            default:
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    public void setChildScrollListener(ChildScrollListener scrollListener){
        childScrollListener = scrollListener;
    }


    // 加载更多的接口
    public interface OnLoadListener {
        public void onLoad();
    }

    public interface ChildScrollListener{
        void onScrollStateChanged(AbsListView view, int scrollState);
        void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount);
    }

}
