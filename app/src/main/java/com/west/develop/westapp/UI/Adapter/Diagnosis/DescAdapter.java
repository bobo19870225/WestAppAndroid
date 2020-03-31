package com.west.develop.westapp.UI.Adapter.Diagnosis;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.west.develop.westapp.R;

import java.util.ArrayList;

/**
 * Created by Develop12 on 2018/1/9.
 */

public class DescAdapter  extends BaseAdapter {
    Context mContext;
    String[] mList;

    public DescAdapter(Context mContext, String[] mList) {
        this.mContext = mContext;
        this.mList = mList;
    }

    @Override
    public int getCount() {
        return mList.length;
    }

    @Override
    public Object getItem(int position) {
        return mList[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if(convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_desc, null);
            holder = new ViewHolder();

            holder.pathName_TV = (TextView) convertView.findViewById(R.id.desc_item);
            convertView.setTag(holder);
        }
        else{
            holder = (ViewHolder)convertView.getTag();
        }

        String pathName = mList[position];
        holder.pathName_TV.setText(pathName);
        return convertView;
    }

    public class ViewHolder{
        TextView pathName_TV;
    }
}
