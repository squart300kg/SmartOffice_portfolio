package com.example.smartoffice.media;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.util.Log;

import com.example.smartoffice.R;
import com.google.android.material.tabs.TabLayout;

public class Activity_Media extends AppCompatActivity
{

    private TabLayout tab;
    private adapterPager adapterPager;
    private ViewPager pager;

    String TAG = "Activity_Media";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity__media);

        Log.e(TAG, "onCreate: " );

        tab = findViewById(R.id.tab);
        tab.addTab(tab.newTab().setText("사진"));
        tab.addTab(tab.newTab().setText("녹화영상"));

        pager = findViewById(R.id.pager);
        adapterPager = new adapterPager(getSupportFragmentManager(), tab.getTabCount());
        pager.setAdapter(adapterPager);

        pager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tab));
        tab.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener()
        {
            @Override
            public void onTabSelected(TabLayout.Tab tab)
            {
                // TODO : tab의 상태가 선택 상태로 변경.
                pager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab)
            {
                // TODO : tab의 상태가 선택되지 않음으로 변경.
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab)
            {
                // TODO : 이미 선택된 상태의 tab이 사용자에 의해 다시 선택됨.
            }
        });
    }

    public class adapterPager extends FragmentStatePagerAdapter
    {

        private int mPageCount;

        public adapterPager(FragmentManager fm, int pageCount)
        {
            super(fm);
            this.mPageCount = pageCount;
            Log.e(TAG, "adapterPager: " );
        }

        @NonNull
        @Override
        public Fragment getItem(int position)
        {
            switch (position)
            {
                case 0:
                    Fragment_Photo fragment_photo = new Fragment_Photo();
                    Log.e(TAG, "getItem: Fragment_Photo" );
                    return fragment_photo;

                case 1:
                    Fragment_Video fragment_video = new Fragment_Video();
                    Log.e(TAG, "getItem: Fragment_Photo" );
                    return fragment_video;

                default:
                    return null;
            }
        }

        @Override
        public int getCount()
        {
            return mPageCount;
        }
    }
}
