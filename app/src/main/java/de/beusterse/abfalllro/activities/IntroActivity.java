package de.beusterse.abfalllro.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import de.beusterse.abfalllro.BuildConfig;
import de.beusterse.abfalllro.controller.MigrationController;
import de.beusterse.abfalllro.fragments.IntroFragment;
import de.beusterse.abfalllro.R;

/**
 * Intro pages activity, communicates with the view pager.
 *
 * Created by Felix Beuster
 */
public class IntroActivity extends AppCompatActivity {

    public static final int NUM_PAGES = 5;
    private static final int LAST_SETUP_PAGE = 1;

    private boolean done;
    private ViewPager viewPager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.SetupTheme_NoActionBar);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);

        if (pref.getBoolean(getString(R.string.pref_key_intern_setup_done), false)) {
            MigrationController migrationController = new MigrationController(this);
            migrationController.migrate();

            startActivity(new Intent(this, TrashCheckActivity.class));
            finish();

        } else {

            updateOverlay(0);

            PagerAdapter pagerAdapter = new IntroPagerAdapter(getSupportFragmentManager());

            viewPager = (ViewPager) findViewById(R.id.pager);
            viewPager.setAdapter(pagerAdapter);
            viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
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

            final Activity that = this;
            Button nextButton = (Button) findViewById(R.id.intro_button_next);
            Button skipButton = (Button) findViewById(R.id.intro_button_skip);

            nextButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (done) {
                        saveSetup(that);
                        startActivity(new Intent(that, TrashCheckActivity.class));
                        that.finish();
                    } else {
                        viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
                    }
                }
            });

            skipButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    saveSetup(that);
                    startActivity(new Intent(that, TrashCheckActivity.class));
                    that.finish();
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        if (viewPager.getCurrentItem() == 0) {
            super.onBackPressed();
        } else {
            viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
        }
    }

    private int getIndicatorColor(int id) {
        if (Build.VERSION.SDK_INT >= 23) {
            return getColor(id);
        } else {
            return getResources().getColor(id);
        }
    }

    private void saveSetup(Activity activity) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(activity);
        SharedPreferences.Editor editor = pref.edit();

        editor.putInt(getString(R.string.pref_key_intern_migrated_version), BuildConfig.VERSION_CODE);
        editor.putBoolean(getString(R.string.pref_key_intern_setup_done), true);

        editor.apply();
    }

    private void setIndicator(View view, Drawable drawable) {
        if (Build.VERSION.SDK_INT >= 16) {
            view.setBackground(drawable);
        } else {
            view.setBackgroundDrawable(drawable);
        }
    }

    private void updateOverlay(int position) {
        boolean canSkip;
        String step;

        if (position > LAST_SETUP_PAGE) {
            canSkip = true;
            step    = getString(R.string.intro_title_guide);
        } else {
            canSkip = false;
            step    = getString(R.string.intro_title_setup);
        }

        Button skipButton = (Button) findViewById(R.id.intro_button_skip);
        skipButton.setEnabled(canSkip);

        setTitle(String.format( getString(R.string.intro_title),
                                getString(R.string.app_name), step));

        Button nextButton = (Button) findViewById(R.id.intro_button_next);
        if (position == NUM_PAGES - 1) {
            done = true;
            nextButton.setText( getString(R.string.intro_button_done) );
        } else {
            done = false;
            nextButton.setText( getString(R.string.intro_button_next) );
        }

        View view               = findViewById(R.id.intro_indicator);
        LayerDrawable indicator = (LayerDrawable) ResourcesCompat.getDrawable(getResources(), R.drawable.intro_indicator, null);
        Drawable layer          = indicator.getDrawable(position);
        layer.setColorFilter( getIndicatorColor(R.color.setupBackground), PorterDuff.Mode.SRC );
        indicator.setDrawableByLayerId(position, layer);

        setIndicator(view, indicator);
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
