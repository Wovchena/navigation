package com.mycompany.myfirstindoorsapp;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.widget.Toast;

import com.customlbs.library.IndoorsException;
import com.customlbs.library.IndoorsFactory;
import com.customlbs.library.callbacks.IndoorsServiceCallback;
import com.customlbs.shared.Coordinate;
import com.customlbs.surface.library.IndoorsSurface;
import com.customlbs.surface.library.IndoorsSurfaceFactory;
import com.customlbs.surface.library.IndoorsSurfaceFragment;
import com.customlbs.surface.library.ViewMode;

import java.util.ArrayList;
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
        /*if (i==i) {
            final Fragment fragment = new DemoObjectFragment();
            Bundle args = new Bundle();
            // Our object is just an integer :-P
            args.putInt(DemoObjectFragment.ARG_OBJECT, i + 1);
            fragment.setArguments(args);
            return fragment;
        } else {
return null;
        }*/
        return mFragments.get(i);

    }





    @Override
    public int getCount() {
        return  mFragments.size();
    }

  /*  @Override
    public CharSequence getPageTitle(int position) {
        return "OBJECT " + (position + 1);
    }*/
}