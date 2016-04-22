package de.beusterse.abfalllro;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Button;

/**
 * Intro pages activity, communicates with the view pager.
 *
 * Created by Felix Beuster
 */
public class IntroActivity extends AppCompatActivity {

    private static final int NUM_PAGES = 5;
    private static final int LAST_SETUP_PAGE = 1;
    private ViewPager viewPager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.SetupTheme_NoActionBar);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setTitle(String.format( getString(R.string.intro_title),
                                getString(R.string.app_name),
                                getString(R.string.intro_title_setup)));

        PagerAdapter pagerAdapter = new IntroPagerAdapter(getSupportFragmentManager());

        viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(pagerAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener(){
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }

            @Override
            public void onPageSelected(int position) {
                updateOverlay(position);
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (viewPager.getCurrentItem() == 0) {
            super.onBackPressed();
        } else {
            viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
        }
    }

    private void updateOverlay(int position) {
        String step;

        if (position > LAST_SETUP_PAGE) {
            step = getString(R.string.intro_title_guide);
        } else {
            step = getString(R.string.intro_title_setup);
        }
        setTitle(String.format( getString(R.string.intro_title),
                                getString(R.string.app_name), step));

        Button nextButton = (Button) findViewById(R.id.intro_button_next);
        if (position == NUM_PAGES - 1) {
            nextButton.setText( getString(R.string.intro_button_done) );
        } else {
            nextButton.setText( getString(R.string.intro_button_next) );
        }
    }


    private class IntroPagerAdapter extends FragmentStatePagerAdapter {
        public IntroPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return IntroFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }
}
