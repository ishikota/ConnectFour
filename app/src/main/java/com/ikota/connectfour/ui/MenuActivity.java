package com.ikota.connectfour.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;

import com.ikota.connectfour.R;


/**
 * Created by kota on 2015/01/08.
 * This Activity is the start point of this app.
 * This is the host Activity which holds 4 menu_list_single
 * fragments(User vs CPU, CPU vs CPU, ...)
 */
public class MenuActivity extends FragmentActivity {
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_main);

        mViewPager = (ViewPager)findViewById(R.id.pager);
        ScreenSlidePagerAdapter adapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu_list_single; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my, menu);
        getActionBar().setTitle(getResources().getString(R.string.activity_menu));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.training:
                Intent intent = new Intent(MenuActivity.this, TrainingActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_from_right, R.anim.fadeout_depth);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * ViewPagerAdapter to display menu list fragments.
     */
    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        private static final int NUM_PAGES = 1;

        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
//                case 0:
//                    return new Menu1Fragment();
                case 0:
                    return new MenuFragment();
            }
            return null;
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }

}
