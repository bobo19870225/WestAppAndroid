package com.west.develop.westapp.UI.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.west.develop.westapp.CallBack.SelectItemListener;
import com.west.develop.westapp.R;
import com.west.develop.westapp.Bean.Upgrade.UpdateDB;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Develop12 on 2017/5/10.
 */
public class UpgradeAdapter extends BaseAdapter {
    private Context context;
    private List<UpdateDB> list = new ArrayList<>();
    private SelectItemListener listener;
    private Set<String> mSet = new HashSet<>();

    private Map<Character,Integer> mLetters = new HashMap<>();

   // private ArrayAdapter adapter;

    public Set<String> getmSet() {
        return mSet;
    }

    public void setmSet(Set<String> mSet) {
        this.mSet = mSet;
    }

    public UpgradeAdapter(Context context, SelectItemListener selectItemListener) {
        this.context = context;
        addAll(list);
        this.listener = selectItemListener;
    }

    public void add(UpdateDB object) {
        try {
            if (list.size() <= 0) {
                list.add(object);
                return;
            }
            int index = -1;

            int start = 0;
            int end = list.size() - 1;

            char ch = object.getFirstLetter();
            char chStart, chEnd, chMid;

            /**
             * 二分法查找字母位置
             */
            while (true) {
                chStart = list.get(start).getFirstLetter();
                chEnd = list.get(end).getFirstLetter();

                if (ch == chStart) {
                    index = start + 1;
                    break;
                } else if (ch < chStart) {
                    index = start;
                    break;
                }
                if (ch >= chEnd) {
                    index = end + 1;
                    break;
                }

                int mid = (end + start) / 2;
                chMid = list.get(mid).getFirstLetter();

                if (ch == chMid) {
                    index = mid + 1;
                    break;
                } else if (ch < chMid) {
                    end = mid;
                } else {
                    start = mid;
                }

                if (start == end || start == end - 1) {
                    index = start + 1;
                    break;
                }

            }

            if (index == -1) {
                index = list.size();
            }

            list.add(index, object);
            if (!mLetters.keySet().contains(ch)) {
                mLetters.put(ch, index);
            }

            for (char cha : mLetters.keySet()) {
                if (cha <= ch) {
                    continue;
                } else {
                    int position = mLetters.get(cha);
                    mLetters.put(cha, position + 1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void addAll(List<? extends UpdateDB> collection) {
        if (collection != null) {
            for (int i = 0; i < collection.size(); i++) {
                add(collection.get(i));
            }
        }
    }

    public void addAll(UpdateDB... items) {
        addAll(Arrays.asList(items));
    }


    public void addDelete(UpdateDB item){
        item.setDelete(true);
        add(item);
    }

    public void addDeleteAll(List<? extends UpdateDB> collection){
        if (collection != null) {
            for (int i = 0; i < collection.size(); i++) {
                addDelete(collection.get(i));
            }
        }
    }

    public void clear() {
        list.clear();
        mLetters.clear();

        //mSet.clear();
        //notifyDataSetChanged();
    }



    public void remove(UpdateDB object) {
        list.remove(object);

        for(char cha : mLetters.keySet()){
            if(cha <=object.getFirstLetter()){
                continue;
            }
            else{
                int position = mLetters.get(cha);
                mLetters.put(cha,position + 1);
            }
        }

        mSet.remove(object.getProgramName());
        //notifyDataSetChanged();
    }


    public List<UpdateDB> getList(){
        return list;
    }

    public int getPosition(char letter){
        if(mLetters.containsKey(letter)){
            return mLetters.get(letter);
        }
        else{
            for(char ch = (char)(letter + 1);ch <= 'Z';ch++){
                if(letter >= 'A' && letter <= 'Z'){
                    if(mLetters.containsKey(ch)){
                        return  mLetters.get(ch);
                    }
                }
                else
                    return -1;
            }
            return list.size();
        }
    }

    @Override
    public int getCount() {
        if (list != null) {
            return list.size();
        }
        return 0;
    }

    /**
     * 可升级数量
     * @return
     */
    public int getAuthenCount(){
        if (list != null) {
            int count = 0;
            for(int i = 0;i < list.size();i++){
                if(list.get(i).isAuthen()){
                    count++;
                }
            }
            return count;
        }
        return 0;
    }

    public int getSelectCount(){
        return mSet.size();
    }

    @Override
    public UpdateDB getItem(int position) {
        if (list != null && list.size() > position) {
            return list.get(position);
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
            convertView = LayoutInflater.from(context).inflate(R.layout.vlist,null);
            holder.checkBox = (CheckBox) convertView.findViewById(R.id.checkbox2);
            holder.carName = (TextView) convertView.findViewById(R.id.textname);
            holder.carVersion = (TextView) convertView.findViewById(R.id.textperversion);
           // holder.spinner = (Spinner) convertView.findViewById(R.id.spinner);
            convertView.setTag(holder);
        }
        else {
            holder = (Holder) convertView.getTag();
        }
        try {
            holder.carName.setText(list.get(position).getProgramName());
            if(list.get(position).isDelete()){
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    holder.carName.setTextColor(context.getResources().getColor(R.color.orange, null));
                }
                else{
                    holder.carName.setTextColor(context.getResources().getColor(R.color.orange));
                }
                holder.checkBox.setButtonDrawable(R.drawable.checkbox_style);
            }
            else if(list.get(position).isAuthen()){
                holder.carName.setTextColor(Color.BLACK);
                holder.checkBox.setButtonDrawable(R.drawable.checkbox_style);

            }
            else{
                holder.carName.setTextColor(Color.RED);
                holder.checkBox.setButtonDrawable(R.mipmap.check_off);
            }
            //String curV = list.get(position).getUpdateCurrentV();
            holder.carVersion.setText(String.valueOf(list.get(position).getFirstLetter()));
            holder.checkBox.setChecked(list.get(position).isChecked());
        }catch (Exception e){
            Log.e("refresh", "getView: "+ e.toString());
            e.printStackTrace();
        }
        if(mSet.contains(list.get(position).getProgramName())){
            holder.checkBox.setChecked(true);
            //list.get(position).setChecked(true);
        }

        holder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {

                    /**
                     * 未授权 不可选
                     */
                    if(!list.get(position).isAuthen() && !list.get(position).isDelete()){
                        holder.checkBox.setChecked(false);
                        return;
                    }
                    if (list.get(position).isChecked()) {
                        list.get(position).setChecked(false);
                    } else {
                        list.get(position).setChecked(true);
                    }

                    //记录是否被选中的状态
                    if (list.get(position).isChecked()) {
                        mSet.add(list.get(position).getProgramName());

                    } else {
                        mSet.remove(list.get(position).getProgramName());
                    }
                    //对checkbox进行监听，与全选的绑定
                    for (int i = 0; i < list.size(); i++) {
                        if (!list.get(i).isChecked()) {
                            listener.select(false);
                            return;
                        }
                    }
                    listener.select(true);
                }catch (Exception e){
                    Log.e("refresh", "onClick: "+e.toString() );
                    e.printStackTrace();
                }
            }
        });

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.checkBox.performClick();
                Log.e("TAG", "onClick: "+position);
            }
        });
        return convertView;
    }


    //全选
    public void setAllChecked(boolean flag) {
        try {
            if (list != null) {
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i).isAuthen() || list.get(i).isDelete()) {
                        list.get(i).setChecked(flag);

                        if (flag) {
                            mSet.add(list.get(i).getProgramName());
                        } else {
                            mSet.remove(list.get(i).getProgramName());
                        }
                    } else {
                        list.get(i).setChecked(false);
                        if (mSet != null) {
                            mSet.remove(list.get(i).getProgramName());
                        }
                    }
                }
            }
            this.notifyDataSetChanged();
        } catch (Exception e) {
            Log.e("refresh", "setAllChecked: " + e.toString());
            e.printStackTrace();
        }
    }

     public class Holder{
         public   CheckBox checkBox;
         public   TextView carName;
         public   TextView carVersion;
        // public   Spinner spinner;
    }


}
