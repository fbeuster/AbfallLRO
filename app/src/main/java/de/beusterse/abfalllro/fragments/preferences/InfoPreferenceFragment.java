package de.beusterse.abfalllro.fragments.preferences;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.Preference;
import de.beusterse.abfalllro.R;
import de.beusterse.abfalllro.utils.AppUtils;

public class InfoPreferenceFragment extends ReturnPreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

        setPreferencesFromResource(R.xml.pref_info, rootKey);
        setHasOptionsMenu(true);
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_key_info_theme)));

        Intent websiteIntent = new Intent(Intent.ACTION_VIEW);
        websiteIntent.setData(Uri.parse(getString(R.string.pref_url_website_long)));

        Preference websitePreference = findPreference(getString(R.string.pref_key_info_website));
        websitePreference.setIntent(websiteIntent);


        Preference pref = findPreference(getString(R.string.pref_key_info_rate));
        pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                final String package_name = preference.getContext().getPackageName();

                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + package_name)));

                } catch (android.content.ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + package_name)));
                }

                return true;
            }
        });

        Preference privacyButton = findPreference(getString(R.string.pref_key_info_privacy));
        privacyButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                WebView webView = new WebView(getActivity());
                webView.loadDataWithBaseURL(null, getString(R.string.pref_privacy_policy_text), "text/html", "UTF-8", null);

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                builder.setView(            webView);
                builder.setTitle(           getString(R.string.pref_privacy_policy_title) );
                builder.setNeutralButton(   getString(R.string.dialog_button_positive),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });

                final AlertDialog dialog = builder.create();

                webView.setWebViewClient(new WebViewClient(){
                    public void onPageFinished(WebView view, String url) {
                        dialog.show();
                    }
                });

                return true;
            }
        });

        Preference nightModePreference = findPreference(getString(R.string.pref_key_info_theme));
        nightModePreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                try {
                    int nightMode = Integer.parseInt(newValue.toString());
                    AppUtils.setDayNightMode(nightMode);
                } catch (Exception e) {
                }

                return true;
            }
        });
    }
}
