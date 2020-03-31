package com.west.develop.westapp.UI.Adapter;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.west.develop.westapp.R;

import java.util.List;

/**
 * Created by Develop12 on 2017/6/13.
 */
public class MyExpandableAdapter extends BaseExpandableListAdapter {
    private List<String> groupArray;
    private List<String[]> childArray;
    private Context context;

    public MyExpandableAdapter(List<String> groupArray, List<String[]> childArray, Context context) {
        this.groupArray = groupArray;
        this.childArray = childArray;
        this.context = context;
    }

    @Override
    public int getGroupCount() {

            return groupArray.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        if (childArray.get(groupPosition).length != 0) {
            return childArray.get(groupPosition).length;
        }
        return 0;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return groupArray.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return childArray.get(groupPosition)[childPosition];
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        GroupHolder groupHolder;
        if (convertView == null) {
            groupHolder = new GroupHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.expandlist_group,null);
            groupHolder.relativeLayout = (RelativeLayout) convertView.findViewById(R.id.group_rl);
            groupHolder.textView = (TextView) convertView.findViewById(R.id.group_tv);
            groupHolder.imageView = (ImageView) convertView.findViewById(R.id.group_image);
            convertView.setTag(groupHolder);
        } else {
            groupHolder = (GroupHolder) convertView.getTag();
        }
        //判断是否已经打开列表
        if (isExpanded){
            groupHolder.imageView.setImageResource(R.mipmap.expander_open_holo_light);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                groupHolder.relativeLayout.setBackgroundColor(context.getColor(R.color.expand_bg));
            }else {
                groupHolder.relativeLayout.setBackgroundColor(context.getResources().getColor(R.color.expand_bg));
            }
        } else {
            groupHolder.imageView.setImageResource(R.mipmap.expander_close_holo_light);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                groupHolder.relativeLayout.setBackgroundColor(context.getColor(R.color.colorWhite));
            }else {
                groupHolder.relativeLayout.setBackgroundColor(context.getResources().getColor(R.color.colorWhite));
            }
        }
        groupHolder.textView.setText(groupArray.get(groupPosition));
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        ChildHolder childHolder;
        if (convertView == null){
            childHolder = new ChildHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.expandlist_item,null);
            childHolder.textView = (TextView) convertView.findViewById(R.id.item_tv);
            convertView.setTag(childHolder);
        }
        else {
            childHolder = (ChildHolder) convertView.getTag();
        }
        childHolder.textView.setText(childArray.get(groupPosition)[childPosition]);
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    class GroupHolder{
        RelativeLayout relativeLayout;
        TextView textView;
        ImageView imageView;
    }
    class ChildHolder{
        TextView textView;
    }
}
