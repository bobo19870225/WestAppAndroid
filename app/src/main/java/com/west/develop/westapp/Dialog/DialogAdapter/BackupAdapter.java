package com.west.develop.westapp.Dialog.DialogAdapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.west.develop.westapp.Dialog.FileDialog;
import com.west.develop.westapp.R;
import com.west.develop.westapp.Tools.Utils.FileUtil;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Develop0 on 2018/5/19.
 */

public class BackupAdapter extends BaseAdapter {
    public static final int MODE_RUN_DIALOG = 1;
    public static final int MODE_ALL_FRAGMENT = 2;
    private Context mContext;
    private ArrayList<Item> items = new ArrayList<>();
    private Item selectedItem = null;

    private ArrayList<Item> mSelected = new ArrayList<>();

    private int mMode = MODE_RUN_DIALOG;

    private OnItemLongClickListener mLongClickListener;

    public BackupAdapter(Context context){
        mContext = context;
        loadList();
    }

    public BackupAdapter(Context context,byte flag){
       this(context);
        if(flag == (byte)0x01){
            if(items != null && items.size() > 0){
                selectedItem = items.get(0);
            }
        }
    }

    public BackupAdapter(Context context,int mode){
        this(context);
        setMode(mode);
    }



    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Object getItem(int position) {
        if(position >= 0 && position < items.size()){
            return items.get(position);
        }
        else{
            return null;
        }
    }

    public Item getSelectedItem(){
        return selectedItem;
    }

    private void addItem(Item item){
        if(items == null){
            items = new ArrayList<>();
        }

        int index = items.size();
        for(int i = items.size();i > 0;i--){
            if(item.getTimeMills() < items.get(i - 1).getTimeMills()){
                break;
            }
            index--;
        }

        if(item != null) {
            items.add(index, item);
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if(convertView != null){
            holder = (ViewHolder)convertView.getTag();
        }
        else{
            convertView = LayoutInflater.from(mContext).inflate(R.layout.adapter_backup,null);
            holder = new ViewHolder();
            holder.checkLayout = (LinearLayout)convertView.findViewById(R.id.item_checkLayout);
            //holder.checkBox = (CheckBox)convertView.findViewById(R.id.item_CheckBox);
            holder.name = (TextView)convertView.findViewById(R.id.item_Name);
            holder.size = (TextView)convertView.findViewById(R.id.item_Size);
            holder.time = (TextView)convertView.findViewById(R.id.item_Time);
            convertView.setTag(holder);
        }

        final Item item = items.get(position);
        if(mMode == MODE_RUN_DIALOG){
            holder.checkLayout.setVisibility(View.GONE);

            if(item == selectedItem){
                convertView.setBackgroundColor(Color.GRAY);
            }
            else{
                convertView.setBackgroundColor(Color.WHITE);
            }
        }
        else if(mMode == MODE_ALL_FRAGMENT){
            holder.checkLayout.setVisibility(View.VISIBLE);

         /*   if(mSelected.contains(item)){
                holder.checkBox.setChecked(true);
            }
            else{
                holder.checkBox.setChecked(false);
            }*/
        }

        holder.name.setText(item.getName() == null?"":item.getName());

        String fileSize = FileUtil.getFileSizeStr(item.getLength());
        holder.size.setText(fileSize);

        if(item.getTimeMills() > 0) {
            Date date = new Date(item.getTimeMills());
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            holder.time.setText(format.format(date));
        }

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mMode == MODE_RUN_DIALOG) {
                    selectedItem = item;

                }
                else if(mMode == MODE_ALL_FRAGMENT){
                    if(mSelected.contains(item)){
                        mSelected.remove(item);
                    }
                    else{
                        mSelected.add(item);
                    }
                }
                notifyDataSetChanged();
            }
        });

        if(mMode == MODE_ALL_FRAGMENT) {
            convertView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (mLongClickListener != null) {
                        mLongClickListener.onLongClick(item);
                    }
                    return true;
                }
            });
        }
        else{
            convertView.setOnLongClickListener(null);
        }

        return convertView;
    }

    public void deleteItem(Item item){
        if(mSelected.contains(item)){
            mSelected.remove(item);
        }
        if(items.contains(item)){
            items.remove(item);
            //item.file.delete();
        }
        notifyDataSetChanged();
    }

    public void deleteSeleted(){
        for(int i = 0;i < mSelected.size();i++){
            Item item = mSelected.get(i);
            if(items.contains(item)){
                items.remove(item);
                //item.file.delete();
            }
        }
        notifyDataSetChanged();
    }


    /**
     * 加载列表
     */
    private void loadList(){
        File rootDir = new File(FileUtil.getBackUPRoot(mContext));

        if(!rootDir.exists()){
            return;
        }

        File[] fileList = rootDir.listFiles();

        if(fileList != null){
            for(int i = 0;i < fileList.length;i++){
                File file = fileList[i];
                if(file.getName().toLowerCase().endsWith(FileDialog.BACKUP_FILE_TYPE)){
                    Item item = new Item(file);
                    addItem(item);
                    //items.add(0,item);
                }
            }
        }
    }


    public void setItemLongClickListener(OnItemLongClickListener listener){
        mLongClickListener = listener;
    }

    public void setMode(int mode){
        if(mode == MODE_ALL_FRAGMENT || mode == MODE_RUN_DIALOG){
            mMode = mode;
        }
    }


    private class ViewHolder{
        LinearLayout checkLayout;
       // CheckBox checkBox;
        TextView name;
        TextView size;
        TextView time;
    }

    public class Item{
        private File file;
        private String name;
        private long length;
        private long timeMills;

        public File getFile() {
            return file;
        }

        public Item(File file){
            this.file = file;
        }

        public String getName() {
            if(file == null){
                return null;
            }
            String fileName = file.getName();
            fileName = fileName.substring(0,fileName.length() - FileDialog.BACKUP_FILE_TYPE.length());
            if(fileName.indexOf("_") > 0){
                fileName = fileName.substring(fileName.indexOf("_") + 1);
            }
            return fileName;
        }


        public long getLength() {
            if(file == null){
                return 0;
            }
            return file.length();
        }

        public long getTimeMills() {
            if(file == null){
                return 0;
            }
            String fileName = file.getName();
            fileName = fileName.substring(0,fileName.length() - FileDialog.BACKUP_FILE_TYPE.length());
            if(fileName.indexOf("_") > 0){
                String millsStr = fileName.substring(0,fileName.indexOf("_"));
                long mills = Long.parseLong(millsStr);
                return mills;
            }
            return 0;
        }

    }


    public interface OnItemLongClickListener{
        void onLongClick(Item item);
    }

}
