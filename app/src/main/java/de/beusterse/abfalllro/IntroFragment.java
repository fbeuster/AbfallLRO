package de.beusterse.abfalllro;

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

/**
 * Selects the matching intro page layout file
 *
 * Created by Felix Beuster
 */
public class IntroFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    private static String PAGE = "page";

    private int page;

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

    private void fillDropdown(Spinner dropdown, int defaultValueId, int entriesId) {
        String defaultValue = getString( defaultValueId );
        String[] values     = getResources().getStringArray( entriesId );

        ArrayAdapter<String> valuesAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, values);

        dropdown.setAdapter( valuesAdapter );
        dropdown.setSelection( valuesAdapter.getPosition(defaultValue) );
    }

    private void initializeLocationPage() {
        Spinner locationDropdown        = (Spinner) getActivity().findViewById(R.id.locationDropdown);
        Spinner locationStreetDropdown  = (Spinner) getActivity().findViewById(R.id.locationStreetDropdown);

        fillDropdown(
                locationDropdown,
                R.string.pref_location_default,
                R.array.pref_locations);
        locationDropdown.setOnItemSelectedListener(this);

        fillDropdown(
                locationStreetDropdown,
                R.string.pref_location_street_default,
                R.array.pref_location_streets);
        locationStreetDropdown.setEnabled(false);
    }

    private void initializeSchedulePage() {
        CheckBox black = (CheckBox) getActivity().findViewById(R.id.monthlyBlackCheckBox);
        CheckBox green = (CheckBox) getActivity().findViewById(R.id.monthlyGreenCheckBox);

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

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = pref.edit();

        editor.putString(getString(R.string.pref_key_pickup_town), city.getSelectedItem().toString());
        editor.putString(getString(R.string.pref_key_pickup_street), street.getSelectedItem().toString());

        editor.apply();
    }

    private void saveSchedulePage() {
        CheckBox black = (CheckBox) getActivity().findViewById(R.id.monthlyBlackCheckBox);
        CheckBox green = (CheckBox) getActivity().findViewById(R.id.monthlyGreenCheckBox);

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = pref.edit();

        editor.putBoolean(getString(R.string.pref_key_pickup_schedule_black), black.isChecked());
        editor.putBoolean(getString(R.string.pref_key_pickup_schedule_green), green.isChecked());

        editor.apply();
    }
}
