package com.west.develop.westapp.UI.Fragment.report;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.west.develop.westapp.Dialog.DialogAdapter.BackupAdapter;
import com.west.develop.westapp.Dialog.MenuDialog;
import com.west.develop.westapp.R;
import com.west.develop.westapp.UI.base.BaseFragment;

/**
 * Created by Develop0 on 2018/3/15.
 */

public class BackupFragment extends BaseFragment{

    private String mTitle = "";

    private static BackupFragment instance;
    public static BackupFragment newInstance() {
        Bundle args = new Bundle();
        BackupFragment fragment = new BackupFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static BackupFragment getInstance(){
        return  instance;
    }

    private TextView mTotalCount_TV;
    //private RefreshLayout mRefreshLayout;
    private ListView mListView;
    private BackupAdapter mAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.fragment_remote_report, container, false);
        initView(inflate);
        initListener();
        initData();
        instance = this;
        return inflate;
    }

    private void initView(View contentView){
        mTotalCount_TV = (TextView)contentView.findViewById(R.id.report_count);
        mListView = (ListView)contentView.findViewById(R.id.RemoteReport_LV);
    }

    private void initData(){
        mTitle = getString(R.string.remote_report);

        //mReports.add(new Report());

        mAdapter = new BackupAdapter(getContext(),BackupAdapter.MODE_ALL_FRAGMENT);
        mListView.setAdapter(mAdapter);

        mTotalCount_TV.setText(mAdapter.getCount() + "");

        mAdapter.setItemLongClickListener(new BackupAdapter.OnItemLongClickListener() {
            @Override
            public void onLongClick(final BackupAdapter.Item item) {
                final MenuDialog dialog = new MenuDialog.Builder(getContext())
                        .addButton(getString(R.string.report_delete), new MenuDialog.OnClickListener() {
                            @Override
                            public void onClick(MenuDialog dialogInterface) {
                                mAdapter.deleteItem(item);
                                dialogInterface.hide();
                            }
                        }).build();
                dialog.show(mListView,(int)getResources().getDimension(R.dimen.statusBarSize));
            }
        });
    }

    private void initListener(){
    }

    public String getTitleText(){
        return mTitle;
    }


}
