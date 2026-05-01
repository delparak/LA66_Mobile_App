package com.sz.cp2102.adapter;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.List;

public class IndexFragmentPageAdapter extends FragmentPagerAdapter {

    private FragmentManager fragmetnmanager;  // Create the FragmentManager
    private List<Fragment> listfragment; // Create a List<Fragment>

    public IndexFragmentPageAdapter(FragmentManager fm, List<Fragment> list) {
        super(fm);
        this.fragmetnmanager = fm;
        this.listfragment = list;
    }

    @Override
    public Fragment getItem(int i) {
        return listfragment.get(i);
    }

    @Override
    public int getCount() {
        return listfragment.size();
    }
}
