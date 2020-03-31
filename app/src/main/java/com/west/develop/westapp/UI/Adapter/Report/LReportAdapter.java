package com.west.develop.westapp.UI.Adapter.Report;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.west.develop.westapp.CallBack.SelectItemListener;
import com.west.develop.westapp.Tools.Utils.ReportUntil;
import com.west.develop.westapp.UI.Activity.DetailsReportActivity;
import com.west.develop.westapp.R;
import com.west.develop.westapp.Bean.Report;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Develop12 on 2017/5/15.
 *
 */
public class LReportAdapter extends BaseAdapter {
    private List<Report> reportList;
    private Context context;
    private SelectItemListener mSelectItemListener;
    private ItemLongClickListener mLongClickListener;
    private Set<String> mSet;

    public Set<String> getmSet() {
        return mSet;
    }

    public LReportAdapter(List<Report> reportList, Context context) {
        this.reportList = reportList;
        this.context = context;

    }

    @Override
    public int getCount() {
        if (reportList != null) {
            return reportList.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        if (reportList != null) {
            return reportList.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final Holder holder;
        if (convertView == null) {
            holder = new Holder();
            convertView = LayoutInflater.from(context).inflate(R.layout.item_report, null);
            holder.checkBox = (CheckBox) convertView.findViewById(R.id.checkboxReport);
            holder.mBoxLayout = (LinearLayout) convertView.findViewById(R.id.item_checkLayout);
            holder.fileName = (TextView) convertView.findViewById(R.id.file_name);
            holder.postedFlag = (TextView) convertView.findViewById(R.id.posted);
            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }

        //如果每一个item都选中了，那就把全选按钮选中
        holder.mBoxLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.checkBox.performClick();
            }
        });
        //如果每一个item都选中了，那就把全选按钮选中
        holder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {

                    if(!reportList.get(position).isPost()) {
                        if (reportList.get(position).isChecked()) {
                            reportList.get(position).setChecked(false);
                        } else {
                            reportList.get(position).setChecked(true);
                        }
                    }
                    else{
                        reportList.get(position).setChecked(false);
                    }

                    if (mSet == null) {
                        mSet = new HashSet<String>();
                    }
                    //记录是否被选中的状态
                    if (reportList.get(position).isChecked()) {
                        mSet.add(reportList.get(position).getFile());
                    } else {
                        mSet.remove(reportList.get(position).getFile());
                    }
                    for (int i = 0; i < reportList.size(); i++) {
                        if (!reportList.get(i).isChecked()) {
                            if (mSelectItemListener != null) {
                                mSelectItemListener.select(false);
                            }
                            return;
                        }
                    }
                    if (mSelectItemListener != null) {
                        mSelectItemListener.select(true);
                    }

                } catch (Exception e) {
                    Log.e("report", "onClick: " + e.toString());
                    e.printStackTrace();
                }
            }
        });
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // holder.checkBox.performClick();
                Intent intent = new Intent(context, DetailsReportActivity.class);
                intent.putExtra("name", reportList.get(position).getFile());
                context.startActivity(intent);
            }
        });

        convertView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(mLongClickListener != null){
                    mLongClickListener.onClick(reportList.get(position));
                }
                return true;
            }
        });

        if(reportList.get(position).isPost()){
            holder.checkBox.setButtonDrawable(R.mipmap.check_off);
        }
        else{
            holder.checkBox.setButtonDrawable(R.drawable.checkbox_style);
        }
        holder.checkBox.setChecked(reportList.get(position).isChecked());
        holder.fileName.setText(reportList.get(position).getReportName());

        holder.postedFlag.setVisibility(View.GONE);

        return convertView;
    }


    public void setAllChecked(boolean isChecked) {
        try {
            if (reportList != null) {
                for (int i = 0; i < reportList.size(); i++) {
                    if(reportList.get(i).isPost()){
                        reportList.get(i).setChecked(false);
                    }
                    else {
                        reportList.get(i).setChecked(isChecked);
                    }
                    if (mSet == null) {
                        mSet = new HashSet<String>();
                    }
                    //记录是否被选中的状态
                    if (reportList.get(i).isChecked()) {
                        mSet.add(reportList.get(i).getFile());
                    } else {
                        mSet.remove(reportList.get(i).getFile());
                    }
                }
            }
            this.notifyDataSetChanged();
        } catch (Exception e) {
            Log.e("report", "setAllChecked: " + e.toString());
            e.printStackTrace();
        }
    }

    public void deleteChecked(TextView count) {
        try {
            if (reportList != null) {
                int size = reportList.size();
                for (int i = 0; i < size; i++) {
                    if (reportList.get(i).isChecked()) {
                        //删除文件夹中的文件
                        ReportUntil.deleteReport(context, reportList.get(i).getFile());

                        reportList.remove(i);
                        i--;
                    }
                    size = reportList.size();
                    count.setText(size + "");
                }
            }
            this.notifyDataSetChanged();
        } catch (Exception e) {
            Log.e("report", "deleteChecked: " + e.toString());
            e.printStackTrace();
        }
    }

    public void setSelectItemListener(SelectItemListener listener) {
        this.mSelectItemListener = listener;
    }


    public void setItemLongClickListener(ItemLongClickListener listener){
        mLongClickListener = listener;
    }

    public interface ItemLongClickListener{
        void onClick(Report report);
    }


    class Holder {
        LinearLayout mBoxLayout;
        CheckBox checkBox;
        TextView fileName;
        TextView postedFlag;
    }
}
