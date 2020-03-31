package com.west.develop.westapp.UI.Fragment.setting.Help;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import androidx.annotation.Nullable;

import com.west.develop.westapp.R;
import com.west.develop.westapp.UI.Adapter.MyExpandableAdapter;
import com.west.develop.westapp.UI.base.BaseFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Develop0 on 2017/12/29.
 */

public class CommonFragment extends BaseFragment {

    private ExpandableListView expandableListView;
    private List<String> group_list;//父节点的名称
    private List<String[]> item_list;
    private MyExpandableAdapter Adapter;


    public static CommonFragment newInstance() {
        CommonFragment fragment = new CommonFragment();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.fragment_help_common,null);

        initView(contentView);
        initListener();
        initData();

        return contentView;
    }

    private void initView(View contentView){
        expandableListView = (ExpandableListView) contentView.findViewById(R.id.expandlist);
    }

    private void initListener(){
        //当父目录为空时，不可点击
        expandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                if (item_list.get(groupPosition).length <= 0){
                    return  true;
                }
                return false;
            }
        });

        //只打开一组，其余关闭
        expandableListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int groupPosition) {
                int count = expandableListView.getExpandableListAdapter().getGroupCount();
                for (int i = 0; i < count; i++) {
                    if (i != groupPosition){
                        expandableListView.collapseGroup(i);
                    }

                }
            }
        });
    }

    private void initData(){
        group_list = new ArrayList<>();

        String[] problems = getResources().getStringArray(R.array.problem_Arrays);
        item_list = new ArrayList<>();
        for(int i = 0;i < problems.length;i++){
            group_list.add(problems[i]);
           // item_list.add();
        }

        String[] solve0 = getResources().getStringArray(R.array.problem_Solve_0);
        String[] solve1 = getResources().getStringArray(R.array.problem_Solve_1);
        String[] solve2 = getResources().getStringArray(R.array.problem_Solve_2);
        String[] solve3 = getResources().getStringArray(R.array.problem_Solve_3);
        String[] solve4 = getResources().getStringArray(R.array.problem_Solve_4);
        String[] solve5 = getResources().getStringArray(R.array.problem_Solve_5);
        String[] solve6 = getResources().getStringArray(R.array.problem_Solve_6);

        item_list.add(solve0);
        item_list.add(solve1);
        item_list.add(solve2);
        item_list.add(solve3);
        item_list.add(solve4);
        item_list.add(solve5);
        item_list.add(solve6);

        Adapter = new MyExpandableAdapter(group_list,item_list,getContext());
        expandableListView.setAdapter(Adapter);
        expandableListView.setGroupIndicator(null); //隐藏默认指示器
    }
}
