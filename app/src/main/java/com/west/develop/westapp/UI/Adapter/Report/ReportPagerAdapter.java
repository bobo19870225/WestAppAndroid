package com.west.develop.westapp.UI.Adapter.Report;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.List;

/**
 * Created by Develop12 on 2017/5/25.
 */
public class ReportPagerAdapter  extends FragmentPagerAdapter {


    private List<Fragment> fragmentList;

    public ReportPagerAdapter(FragmentManager fm, List<Fragment> fragmentList) {
        super(fm);
        this.fragmentList = fragmentList;
    }



    @Override
    public Fragment getItem(int position) {
        if (fragmentList.size() != 0) {
            return fragmentList.get(position);
        }
      return null;
    }

    @Override
    public int getCount() {
        return fragmentList.size();
    }
}
