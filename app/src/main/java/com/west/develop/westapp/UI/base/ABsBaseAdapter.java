package com.west.develop.westapp.UI.base;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

/**
 * Created by Develop11 on 2017/5/2.
 */
public abstract class ABsBaseAdapter<T> extends BaseAdapter {
    private List<T> data;
    private Context context;

    public ABsBaseAdapter( Context context,List<T> data) {
        this.data = data;
    }

    @Override
    public int getCount() {
        return data==null?0:data.size();
    }

    @Override
    public Object getItem(int position) {
        return data==null?null:data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public abstract View getView(int position, View convertView, ViewGroup parent);
}
