package de.beusterse.abfalllro.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;

import de.beusterse.abfalllro.R;
import de.beusterse.abfalllro.activities.SettingsActivity;

/**
 * Selects the matching intro page layout file
 *
 * Created by Felix Beuster
 */
public class IntroFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    private static String PAGE = "page";

    private int page;

    private SharedPreferences pref;

    public static IntroFragment newInstance(int position) {
        Bundle bundle = new Bundle();
        bundle.putInt(PAGE, position);

        IntroFragment introFragment = new IntroFragment();
        introFragment.setArguments(bundle);

        return introFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!getArguments().containsKey(PAGE)) {
            throw new RuntimeException("missing " + PAGE + " argument");
        }
        page = getArguments().getInt(PAGE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                Bundle savedInstanceState) {
        int layout;
        switch (page) {
            case 0:
                layout = R.layout.intro_page_setup_location;
                break;
            case 1:
                layout = R.layout.intro_page_setup_schedule;
                break;
            case 2:
                layout = R.layout.intro_page_guide_main;
                break;
            case 3:
                layout = R.layout.intro_page_guide_preview;
                break;
            default:
                layout = R.layout.intro_page_guide_settings;
        }

        View view = getActivity().getLayoutInflater().inflate(layout, container, false);
        view.setTag(page);

        return view;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        String value = parent.getItemAtPosition(pos).toString();

        Spinner locationStreetDropdown  = (Spinner) getActivity().findViewById(R.id.locationStreetDropdown);

        locationStreetDropdown.setEnabled( value.equals(SettingsActivity.CITY_WITH_STREETS) );

        saveLocationPage();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        pref = PreferenceManager.getDefaultSharedPreferences(getActivity());

        switch (page) {
            case 0:
                initializeLocationPage();
                break;
            case 1:
                initializeSchedulePage();
                break;
            default:
                break;
        }
    }

    private void fillDropdown(Spinner dropdown, String entry, int entriesId) {
        String[] values     = getResources().getStringArray( entriesId );

        ArrayAdapter<String> valuesAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, values);

        dropdown.setAdapter( valuesAdapter );
        dropdown.setSelection( valuesAdapter.getPosition(entry) );
    }

    private void initializeLocationPage() {
        Spinner locationDropdown        = (Spinner) getActivity().findViewById(R.id.locationDropdown);
        Spinner locationStreetDropdown  = (Spinner) getActivity().findViewById(R.id.locationStreetDropdown);

        fillDropdown(
                locationDropdown,
                pref.getString(
                        getString(R.string.pref_key_pickup_town),
                        getString(R.string.pref_location_default)),
                R.array.pref_locations);
        locationDropdown.setOnItemSelectedListener(this);

        fillDropdown(
                locationStreetDropdown,
                pref.getString(
                        getString(R.string.pref_key_pickup_street),
                        getString(R.string.pref_location_street_default)),
                R.array.pref_location_streets);
        locationStreetDropdown.setEnabled(false);
    }

    private void initializeSchedulePage() {
        // TODO check boxes should be replaced with spinner
        // TODO all four cans need to be configured
        CheckBox black = (CheckBox) getActivity().findViewById(R.id.monthlyBlackCheckBox);
        CheckBox green = (CheckBox) getActivity().findViewById(R.id.monthlyGreenCheckBox);

        black.setChecked( pref.getString(getString(R.string.pref_key_pickup_schedule_black), getString(R.string.pref_can_schedule_biweekly)).equals(getString(R.string.pref_can_schedule_monthly)) );
        green.setChecked( pref.getString(getString(R.string.pref_key_pickup_schedule_green), getString(R.string.pref_can_schedule_biweekly)).equals(getString(R.string.pref_can_schedule_monthly)) );

        black.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                saveSchedulePage();
            }
        });

        green.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                saveSchedulePage();
            }
        });
    }

    private void saveLocationPage() {
        Spinner city    = (Spinner) getActivity().findViewById(R.id.locationDropdown);
        Spinner street  = (Spinner) getActivity().findViewById(R.id.locationStreetDropdown);

        SharedPreferences.Editor editor = pref.edit();

        editor.putString(getString(R.string.pref_key_pickup_town), city.getSelectedItem().toString());
        editor.putString(getString(R.string.pref_key_pickup_street), street.getSelectedItem().toString());

        editor.apply();
    }

    private void saveSchedulePage() {
        // TODO update for multiple schedules
        CheckBox black = (CheckBox) getActivity().findViewById(R.id.monthlyBlackCheckBox);
        CheckBox green = (CheckBox) getActivity().findViewById(R.id.monthlyGreenCheckBox);

        SharedPreferences.Editor editor = pref.edit();

        int black_schedule = black.isChecked() ? R.string.pref_can_schedule_monthly : R.string.pref_can_schedule_biweekly;
        int green_schedule = green.isChecked() ? R.string.pref_can_schedule_monthly : R.string.pref_can_schedule_biweekly;

        editor.putString(getString(R.string.pref_key_pickup_schedule_black), getString(black_schedule));
        editor.putString(getString(R.string.pref_key_pickup_schedule_green), getString(green_schedule));

        editor.apply();
    }
}
