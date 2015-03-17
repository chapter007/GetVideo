package com.example.zhangjie.getvideo;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.view.MenuItem;

import java.io.IOException;

/**
 * Created by zhangjie on 2015/2/22.
 */
public class Setting extends ToolBar{

    private SettingFragment mSettingFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        enableHomeButton();
        if (savedInstanceState==null){
            mSettingFragment=new SettingFragment();
            replaceFragment(R.id.setting,mSettingFragment);
        }

    }

    @Override
    protected int getLayoutResource() {
        return R.layout.setting;
    }

    public static class SettingFragment extends PreferenceFragment{

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.setting);
        }
    }

    public void replaceFragment(int viewId,Fragment fragment){
        FragmentManager fragmentManager=getFragmentManager();
        fragmentManager.beginTransaction().replace(viewId,fragment).commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
