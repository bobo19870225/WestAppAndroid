package com.west.develop.westapp.UI.Adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.west.develop.westapp.Config.Config;
import com.west.develop.westapp.Tools.Utils.FileUtil;
import com.west.develop.westapp.R;
import com.west.develop.westapp.UI.base.ABsBaseAdapter;
import com.west.develop.westapp.Bean.NCarBean;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/5/10 0010.
 */
public class MainDiagnosisAdapter extends ABsBaseAdapter<NCarBean> {
    private List<NCarBean> mImages = new ArrayList<>();
    private Context context;
    private float textSize = 15;

    public MainDiagnosisAdapter(Context context, List<NCarBean> data) {
        super(context, data);
        this.mImages = data;
        this.context = context;
    }

    public float getTextSize() {
        return textSize;
    }

    public void setTextSize(float textSize) {
        this.textSize = textSize;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder;
        if (convertView == null) {
            holder = new Holder();
            convertView = LayoutInflater.from(context).inflate(R.layout.item_diagnosis_main, null);
            holder.imageView = (ImageView) convertView.findViewById(R.id.item_diagnosisResion_IMG);
            holder.brandName = (TextView) convertView.findViewById(R.id.item_diagnosisResion_NAME);
            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }
        if (mImages.get(position).getLogoPath() == null || "".equals(mImages.get(position).getLogoPath())) {
            holder.imageView.setImageDrawable(context.getDrawable(R.mipmap.default_icon));
        } else {
            String logoPath = FileUtil.getProgramIcon(context)+ mImages.get(position).getLogoPath();
            File filePath = new File(logoPath);
            if (filePath.exists()){
                Bitmap bitmap = BitmapFactory.decodeFile(logoPath);

                if(bitmap != null) {
                    holder.imageView.setImageBitmap(bitmap);
                }
            }
            else{
                holder.imageView.setImageDrawable(context.getDrawable(R.mipmap.default_icon));
            }
        }
        if (Config.getInstance(context).getLanguage() == Config.LANGUAGE_EN){
            holder.brandName.setText(mImages.get(position).getCarEnglishName());
        }else if (Config.getInstance(context).getLanguage() == Config.LANGUAGE_CH){
            holder.brandName.setText(mImages.get(position).getCarChineseName());
        }
        holder.brandName.setTextSize(getTextSize());

        return convertView;
    }
    class Holder {
        ImageView imageView;
        TextView brandName;
    }
}