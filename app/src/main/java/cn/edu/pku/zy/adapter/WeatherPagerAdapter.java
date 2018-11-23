package cn.edu.pku.zy.adapter;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

public class WeatherPagerAdapter extends FragmentPagerAdapter {
    private List<Fragment> fragments;
    public WeatherPagerAdapter(FragmentManager fm,List<Fragment> fragments){
        super(fm);
        this.fragments=fragments;
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }
    public int getCount(){
        return fragments.size();
    }

}
