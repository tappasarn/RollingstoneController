package xyz.rollingstone;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import xyz.rollingstone.tabs.ManualTab;
import xyz.rollingstone.tabs.SettingTab;
import xyz.rollingstone.tabs.Auto;
import xyz.rollingstone.tabs.ScriptSelectTab;

public class ViewPagerAdapter extends FragmentStatePagerAdapter {

    // This will Store the Titles of the Tabs which are Going to be passed when ViewPagerAdapter is created
    private CharSequence Titles[];
    // Store the number of tabs, this will also be passed when the ViewPagerAdapter is created
    private int numTabs;


    // Build a Constructor and assign the passed Values to appropriate values in the class
    public ViewPagerAdapter(FragmentManager fm, CharSequence mTitles[], int numTabs) {
        super(fm);
        this.Titles = mTitles;
        this.numTabs = numTabs;
    }

    // This method return the fragment for the every position in the View Pager
    @Override
    public Fragment getItem(int position) {
        switch(position) {
            case 0: return new ManualTab();
            case 1: return new Auto();
            case 2: return new ScriptSelectTab();
            case 3: return new SettingTab();
            default: return new ManualTab();
        }
    }

    // This method return the titles for the Tabs in the Tab Strip
    @Override
    public CharSequence getPageTitle(int position) {
        return Titles[position];
    }

    // This method return the Number of tabs for the tabs Strip
    @Override
    public int getCount() {
        return numTabs;
    }

}