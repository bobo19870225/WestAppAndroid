package com.west.develop.westapp.UI.Fragment.report;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.west.develop.westapp.Bean.Report;
import com.west.develop.westapp.CallBack.SelectItemListener;
import com.west.develop.westapp.Dialog.MenuDialog;
import com.west.develop.westapp.R;
import com.west.develop.westapp.Tools.Utils.ReportUntil;
import com.west.develop.westapp.UI.Activity.MainActivity;
import com.west.develop.westapp.UI.Adapter.Report.LReportAdapter;
import com.west.develop.westapp.UI.base.BaseFragment;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by Develop12 on 2017/8/15.
 */
public class ReportFragment extends BaseFragment implements View.OnClickListener {

    private String mTitle = "";

    private ListView listView;
    private LReportAdapter adapter;
    private List<Report> listReport = new ArrayList<>();
    private TextView count;
    private CheckBox checkBox;

    private int selectcount;
    private boolean allItemflag = false;

    private static final int MSG_REPORT_REFRESH = 0;

    private static ReportFragment instance;
    public static ReportFragment newInstance() {

        Bundle args = new Bundle();
        ReportFragment fragment = new ReportFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static ReportFragment getInstance(){
        return instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    Handler handle = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case MSG_REPORT_REFRESH:
                    count.setText(listReport.size() +" ");

                    mTitle = getResources().getString(R.string.local_report);
                    if (selectcount > 0){
                        mTitle = getResources().getString(R.string.upgrade_select)+" "+selectcount+" "+getResources().getString(R.string.report_num);
                    }

                    refreshTitle();
                    adapter.notifyDataSetChanged();
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 选项选择事件
     */
    private SelectItemListener mSelectItemListener = new SelectItemListener() {
        @Override
        public void select(boolean b) {
            mTitle = getResources().getString(R.string.local_report);
            if (!b && !checkBox.isChecked()){
                int count = 0;
                for (int j = 0; j < listReport.size() ; j++) {
                    if (listReport.get(j).isChecked()){
                        count++;
                        mTitle = getResources().getString(R.string.upgrade_select)+" "+count+" "+getResources().getString(R.string.report_num);
                    }
                }
                refreshTitle();
                return;
            }else if (!b && checkBox.isChecked()){
                allItemflag = true;
                checkBox.setChecked(false);
                int count = 0;
                for (int j = 0; j < listReport.size() ; j++) {
                    if (listReport.get(j).isChecked()){
                        count++;
                        mTitle = getResources().getString(R.string.upgrade_select)+" "+count+" "+getResources().getString(R.string.report_num);
                    }

                }
            }else if (b){
                allItemflag = true;
                checkBox.setChecked(true);
                mTitle = getResources().getString(R.string.upgrade_select)+" "+listReport.size()+" "+getResources().getString(R.string.report_num);
            }
            refreshTitle();
        }
    };

    private LReportAdapter.ItemLongClickListener mItemLongClickListner = new LReportAdapter.ItemLongClickListener() {
        @Override
        public void onClick(final Report report) {
            if(report.isPost()){
                Toast.makeText(getContext(),getString(R.string.unable_Delete),Toast.LENGTH_SHORT).show();
                return;
            }
            final MenuDialog dialog = new MenuDialog.Builder(getContext())
                    .addButton(getString(R.string.report_delete), new MenuDialog.OnClickListener() {
                        @Override
                        public void onClick(MenuDialog dialogInterface) {
                            ReportUntil.deleteReport(getContext(),report.getFile());
                            refresh();
                            dialogInterface.hide();
                        }
                    }).build();
            dialog.show(listView,(int)getResources().getDimension(R.dimen.statusBarSize));
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.fragment_local_report, container, false);
        initView(inflate);
        initData();
        listener();
        instance = this;
        return inflate;
    }

    private void initView(View inflate) {
        count = (TextView) inflate.findViewById(R.id.report_diagnose_count);
        listView = (ListView) inflate.findViewById(R.id.listviewreport);
        checkBox = (CheckBox) inflate.findViewById(R.id.checkboxReport_bottom);
    }



    /**
     * 初始化数据
     */
    private void initData() {
        //初始化标题
        mTitle = getResources().getString(R.string.local_report);
        refreshTitle();

        /**
         * 获取本地记录
         */
        File[] files = ReportUntil.getReports(getContext());
        if(files != null) {
            for (int i = 0; i < files.length; i++) {
                String str = files[i].getName();
                Report report = new Report();
                if (str.indexOf(".txt") > 0) {
                    report.setFile(str.substring(0, str.lastIndexOf(".txt")));
                    listReport.add(report);
                }
            }
            Collections.reverse(listReport);
            handle.sendEmptyMessage(MSG_REPORT_REFRESH);
        }
        adapter = new LReportAdapter(listReport,getActivity());
        adapter.setSelectItemListener(mSelectItemListener);
        adapter.setItemLongClickListener(mItemLongClickListner);

        listView.setAdapter(adapter);
    }

    /**
     * 初始化监听
     */
    private void listener() {
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (allItemflag){
                    allItemflag = false;
                    return;
                }
                boolean flag = checkBox.isChecked();
                adapter.setAllChecked(flag);
                mTitle = getResources().getString(R.string.local_report);

                if (flag) {
                    selectcount = 0;
                    Set<String> set = adapter.getmSet();
                    if (set != null) {
                        Iterator it = set.iterator();
                        while (it.hasNext()) {
                            String name = (String) it.next();

                            for (int i = 0; i < listReport.size(); i++) {
                                if (listReport.get(i).getFile().equals(name)) {
                                    listReport.get(i).setChecked(true);
                                    selectcount++;
                                    break;
                                }
                            }
                        }
                    }
                    handle.sendEmptyMessage(MSG_REPORT_REFRESH);
/*
                    if (listReport.size() > 0){
                        mTitle = getResources().getString(R.string.upgrade_select)+ " "+listReport.size()+" "+getResources().getString(R.string.report_num);
                    }*/
                }
                refreshTitle();

                adapter.notifyDataSetChanged();//刷新适配器

            }
        });
    }



    /**
     * 更新数据
     */
    public void refresh(){
        if (listReport == null){
            listReport = new ArrayList<>();
        }
        listReport.clear();
        File[] files = ReportUntil.getReports(getContext());
        if (files != null && files.length > 0) {
            for (int i = 0; i < files.length; i++) {
                String str = files[i].getName();
                Report report = new Report();
                if (str.indexOf(".txt") > 0) {
                    report.setFile(str.substring(0, str.lastIndexOf(".txt")));
                    listReport.add(report);
                }
            }
        }
        Collections.reverse(listReport);

        selectcount = 0;
        Set<String> set = adapter.getmSet();
        if (set != null) {
            Iterator it = set.iterator();
            while (it.hasNext()) {
                String name = (String) it.next();

                for (int i = 0; i < listReport.size(); i++) {
                    if (listReport.get(i).getFile().equals(name)) {
                        listReport.get(i).setChecked(true);
                        selectcount++;
                        break;
                    }
                }
            }
        }
        handle.sendEmptyMessage(MSG_REPORT_REFRESH);
    }



    /**
     * 刷新标题
     */
    public void refreshTitle(){
        DataFragment report = DataFragment.getInstance();
        if(report != null){
            report.refreshTitle();
        }
    }


    /**
     * 获取标题文字
     * @return
     */
    public String getTitleText(){
        return  mTitle;
    }

    /**
     * 删除已选中的记录
     */
    public void deleteCheck(){
        adapter.deleteChecked(count);
        checkBox.setChecked(false);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.menu_iv:
                if (getContext() instanceof MainActivity) {
                    if (getContext() instanceof MainActivity) {
                        ((MainActivity) getActivity()).onFragSwitch(MainActivity.HOME_ID);

                    }
                    break;
                }

        }
    }




    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
