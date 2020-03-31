package com.west.develop.westapp.UI.Adapter.Diagnosis;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.west.develop.westapp.R;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Develop11 on 2017/9/7.
 */

public class DiagnosisAdapter extends BaseAdapter {

    Context mContext;
    ArrayList<File> mFiles;
    boolean isDebug = false;

    public DiagnosisAdapter(Context context,ArrayList<File> files,boolean debug){
        mContext = context;
        mFiles = files;
        isDebug = debug;

        if(mFiles != null){
            for(int i = 0;i < mFiles.size();i++){
                if(mFiles.get(i).getName().toLowerCase().endsWith(".bin") || mFiles.get(i).isDirectory()){
                    continue;
                }
                mFiles.remove(i);
                i--;
            }
        }
    }

    @Override
    public int getCount() {
        return mFiles.size();
    }

    @Override
    public Object getItem(int position) {
        return mFiles.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if(convertView == null){
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_diagnosis,null);
            holder = new ViewHolder();

            holder.pathName_TV = (TextView)convertView.findViewById(R.id.itemPathName);
            convertView.setTag(holder);
        }
        else{
            holder = (ViewHolder)convertView.getTag();
        }

        String pathName = mFiles.get(position).getName();


        holder.pathName_TV.setText(pathName==null?"":pathName);

        return convertView;
    }

    public class ViewHolder{
//        public ImageView pathType_IMG;
        TextView pathName_TV;
    }
}
