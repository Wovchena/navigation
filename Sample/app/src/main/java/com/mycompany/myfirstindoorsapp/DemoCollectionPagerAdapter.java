package com.mycompany.myfirstindoorsapp;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import java.util.List;

public class DemoCollectionPagerAdapter extends FragmentPagerAdapter {

    private PagedActivity pagedActivity;
    List<Fragment> mFragments;

    public DemoCollectionPagerAdapter(FragmentManager fm, PagedActivity pa, List<Fragment> fragmentList) {
        super(fm);
        pagedActivity=pa;
        mFragments=fragmentList;
    }

    @Override
    public Fragment getItem(int i) {
        return mFragments.get(i);
    }





    @Override
    public int getCount() {
        return  mFragments.size();
    }
}