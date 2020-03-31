package com.west.develop.westapp.UI.Fragment.report;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.west.develop.westapp.CallBack.FragmentBackHandler;
import com.west.develop.westapp.R;
import com.west.develop.westapp.UI.Activity.MainActivity;
import com.west.develop.westapp.UI.Adapter.Report.ReportPagerAdapter;
import com.west.develop.westapp.UI.base.BaseFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Develop0 on 2018/3/15.
 */

public class DataFragment extends BaseFragment implements View.OnClickListener,FragmentBackHandler {
    private ImageView menu_iv;
    private TextView menu_title;
    private Button delete_ll;

    private View tab_Local;
    private View tab_Remote;
    private View localFlag;
    private View remoteFlag;

    private ReportPagerAdapter adapter;
    private ViewPager mviewPager;
    private List<Fragment> fragments = new ArrayList<Fragment>();

    private ReportFragment localFragment;
    private BackupFragment remoteFragment;
    //private BackupFragment

    private static DataFragment instance;
    public static DataFragment newInstance() {

        Bundle args = new Bundle();
        DataFragment fragment = new DataFragment();
        fragment.setArguments(args);

        return fragment;
    }

    public static DataFragment getInstance(){
        return instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.fragment_report, container, false);
        initView(inflate);
        initListener();
        initData();
        instance = this;
        return inflate;
    }

    /**
     * 初始化控件
     * @param contentView
     */
    private void initView(View contentView){
        menu_iv = (ImageView) contentView.findViewById(R.id.menu_iv);
        menu_title = (TextView) contentView.findViewById(R.id.menu_title);
        delete_ll = (Button) contentView.findViewById(R.id.delete_ll);
        mviewPager = (ViewPager)contentView.findViewById(R.id.report_viewpager);

        delete_ll = (Button) contentView.findViewById(R.id.delete_ll);
        delete_ll.setVisibility(View.VISIBLE);

        tab_Local = contentView.findViewById(R.id.tab_Local);
        tab_Remote = contentView.findViewById(R.id.tab_Remote);
        localFlag = contentView.findViewById(R.id.local_Flag);
        remoteFlag = contentView.findViewById(R.id.remote_Flag);
    }


    /**
     * 初始化监听
     */
    private void initListener(){
        //mviewPager.setCurrentItem(0);
        mviewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if(position == 0){
                    delete_ll.setVisibility(View.VISIBLE);
                    localFlag.setVisibility(View.VISIBLE);
                    remoteFlag.setVisibility(View.GONE);
                }
                else{
                    delete_ll.setVisibility(View.GONE);
                    localFlag.setVisibility(View.GONE);
                    remoteFlag.setVisibility(View.VISIBLE);
                }
                refreshTitle();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        menu_iv.setOnClickListener(this);
        delete_ll.setOnClickListener(this);

        tab_Local.setOnClickListener(this);
        tab_Remote.setOnClickListener(this);
    }

    /**
     * 初始化数据
     */
    private void initData(){
        localFragment = ReportFragment.newInstance();
        fragments.add(localFragment);

         remoteFragment = BackupFragment.newInstance();
        fragments.add(remoteFragment);

        adapter = new ReportPagerAdapter(getFragmentManager(),fragments);
        mviewPager.setAdapter(adapter);
    }

    /**
     * 刷新
     */
    public void refresh(){

    }

    /**
     * 更新标题
     */
    public void refreshTitle(){
        if (mviewPager.getCurrentItem() == 0){
            menu_title.setText(localFragment.getTitleText());
        }
        if(mviewPager.getCurrentItem() == 1){
            menu_title.setText(remoteFragment.getTitleText());
        }
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
            case R.id.delete_ll:
                if(mviewPager.getCurrentItem() == 0) {
                    localFragment.deleteCheck();
                    menu_title.setText(localFragment.getTitleText());
                }
                else if(mviewPager.getCurrentItem() == 1){
                }

                break;
            case R.id.tab_Local:
                mviewPager.setCurrentItem(0);
                break;
            case R.id.tab_Remote:
                mviewPager.setCurrentItem(1);
                break;
        }
    }

    @Override
    public boolean onBackPressed() {
        if(getContext() instanceof MainActivity){
            ((MainActivity)getActivity()).onFragSwitch(MainActivity.HOME_ID);
        }
        return true;
    }
}
