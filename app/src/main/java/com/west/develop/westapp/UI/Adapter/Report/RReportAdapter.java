package com.west.develop.westapp.UI.Adapter.Report;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.west.develop.westapp.Bean.Report;
import com.west.develop.westapp.R;

import java.util.ArrayList;

/**
 * Created by Develop0 on 2018/3/16.
 */

public class RReportAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<Report> mReportList = new ArrayList<>();

    public RReportAdapter(Context context, ArrayList<Report> list){
        mContext = context;
        mReportList.addAll(list);
    }

    @Override
    public int getCount() {
        return mReportList.size();
    }

    @Override
    public Object getItem(int position) {
        return mReportList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    public void clearReport(){
        mReportList.clear();
    }


    public void add(Report report){
        if(report != null ){
            mReportList.add(mReportList.size(),report);
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if(convertView == null){
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_report,null);
            holder = new ViewHolder();
            holder.checkBox = (CheckBox) convertView.findViewById(R.id.checkboxReport);
            holder.mBoxLayout = (LinearLayout) convertView.findViewById(R.id.item_checkLayout);
            holder.fileName = (TextView) convertView.findViewById(R.id.file_name);
            holder.postedFlag = (TextView) convertView.findViewById(R.id.posted);
            convertView.setTag(holder);
        }
        else{
            holder = (ViewHolder)convertView.getTag();
        }

        Report report = mReportList.get(position);
        holder.fileName.setText(report.getReportName());
        holder.checkBox.setVisibility(View.GONE);
        holder.postedFlag.setVisibility(View.GONE);
        return convertView;
    }


    class ViewHolder{
        LinearLayout mBoxLayout;
        CheckBox checkBox;
        TextView fileName;
        TextView postedFlag;
    }
}
