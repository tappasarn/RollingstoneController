package xyz.rollingstone;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import xyz.rollingstone.tabs.AutoTab;
import xyz.rollingstone.tabs.ManualTab;
import xyz.rollingstone.tabs.SettingTab;
import xyz.rollingstone.tabs.ScriptSelectTab;

public class ViewPagerAdapter extends FragmentStatePagerAdapter {

    private AutoTab autoTab;
    private ManualTab manualTab;
    private SettingTab settingTab;
    private ScriptSelectTab scriptSelectTab;

    // This will Store the Titles of the Tabs which are Going to be passed when ViewPagerAdapter is created
    private CharSequence Titles[];
    // Store the number of tabs, this will also be passed when the ViewPagerAdapter is created
    private int numTabs;

    private Context context;


    // Build a Constructor and assign the passed Values to appropriate values in the class
    public ViewPagerAdapter(FragmentManager fm, CharSequence mTitles[], int numTabs, Context context) {
        super(fm);
        this.Titles = mTitles;
        this.numTabs = numTabs;
        this.context = context;
    }

    // This method return the fragment for the every position in the View Pager
    @Override
    public Fragment getItem(int position) {
        switch(position) {
            case 0: return manualTab = new ManualTab();
            case 1: return autoTab = new AutoTab();
            case 2: return scriptSelectTab = new ScriptSelectTab();
            case 3: return settingTab = new SettingTab();
            default: return manualTab = new ManualTab();
        }
    }

    public ManualTab getManualTab() {
        return manualTab;
    }

    public SettingTab getSettingTab() {
        return settingTab;
    }

    public ScriptSelectTab getScriptSelectTab() {
        return scriptSelectTab;
    }

    public AutoTab getAutoTab() {
        return autoTab;
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