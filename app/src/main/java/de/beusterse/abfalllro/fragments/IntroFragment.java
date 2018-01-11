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
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.beusterse.abfalllro.R;
import de.beusterse.abfalllro.activities.SettingsActivity;
import de.beusterse.abfalllro.capsules.Can;
import de.beusterse.abfalllro.utils.ArrayUtils;
import de.beusterse.abfalllro.utils.SpinnerUtils;

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

        Spinner locationStreetDropdown  = getActivity().findViewById(R.id.locationStreetDropdown);

        locationStreetDropdown.setEnabled( value.equals(SettingsActivity.CITY_WITH_STREETS) );

        saveLocationPage();
        initializeSchedulePage();
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

    private boolean hasLocationCanSchedule(String location, int can, String schedule) {
        if (schedule.equals(getString(R.string.pref_can_schedule_twice_a_week))) {
            if (can == Can.BLACK && ArrayUtils.indexOf(getResources().getStringArray(R.array.pref_location_twice_per_week_black), location) == -1) {
                return false;
            }

            if (can == Can.GREEN && ArrayUtils.indexOf(getResources().getStringArray(R.array.pref_location_twice_per_week_green), location) == -1) {
                return false;
            }
        }

        if (schedule.equals(getString(R.string.pref_can_schedule_weekly))) {
            if (can == Can.BLACK && ArrayUtils.indexOf(getResources().getStringArray(R.array.pref_location_weekly_black), location) == -1) {
                return false;
            }

            if (can == Can.GREEN && ArrayUtils.indexOf(getResources().getStringArray(R.array.pref_location_weekly_green), location) == -1) {
                return false;
            }
        }

        return true;
    }

    private void initializeLocationPage() {
        Spinner locationDropdown        = getActivity().findViewById(R.id.locationDropdown);
        Spinner locationStreetDropdown  = getActivity().findViewById(R.id.locationStreetDropdown);

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
        locationStreetDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                saveLocationPage();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                saveLocationPage();
            }
        });
    }

    private void initializeSchedulePage() {
        initializeSpinner((Spinner) getActivity().findViewById(R.id.scheduleBlackSpinner), Can.BLACK);
        initializeSpinner((Spinner) getActivity().findViewById(R.id.scheduleBlueSpinner), Can.BLUE);
        initializeSpinner((Spinner) getActivity().findViewById(R.id.scheduleGreenSpinner), Can.GREEN);
        initializeSpinner((Spinner) getActivity().findViewById(R.id.scheduleYellowSpinner), Can.YELLOW);
    }

    private void initializeSpinner(Spinner spinner, int can) {
        String biweekly     = getString(R.string.pref_can_schedule_biweekly);
        String location     = pref.getString(getString(R.string.pref_key_pickup_town), getString(R.string.pref_location_default));
        String twicePerWeek = getString(R.string.pref_can_schedule_twice_a_week);
        String weekly       = getString(R.string.pref_can_schedule_weekly);

        // load available schedules in list
        String[] schedules      = getResources().getStringArray(R.array.pref_general_schedule_list);
        final List<String> list = new ArrayList<>(Arrays.asList(schedules));

        // update biweekly, weekly and twice per week schedules
        if (can == Can.BLUE || can == Can.YELLOW) {
            list.remove(list.indexOf(biweekly));
            list.remove(list.indexOf(weekly));
            list.remove(list.indexOf(twicePerWeek));

        } else {
            if (!hasLocationCanSchedule(location, can, weekly)) {
                list.remove(list.indexOf(weekly));
            }

            if (!hasLocationCanSchedule(location, can, twicePerWeek)) {
                list.remove(list.indexOf(twicePerWeek));
            }
        }

        // set new adapter
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, list);
        spinner.setAdapter(adapter);

        // update values
        adapter.notifyDataSetChanged();

        // set default selection
        spinner.setSelection( SpinnerUtils.indexOf(spinner, getString(R.string.pref_can_schedule_monthly)));

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                saveSchedulePage();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                saveSchedulePage();
            }
        });
    }

    private void saveLocationPage() {
        Spinner city    = getActivity().findViewById(R.id.locationDropdown);
        Spinner street  = getActivity().findViewById(R.id.locationStreetDropdown);

        SharedPreferences.Editor editor = pref.edit();

        editor.putString(getString(R.string.pref_key_pickup_town), city.getSelectedItem().toString());
        editor.putString(getString(R.string.pref_key_pickup_street), street.getSelectedItem().toString());

        editor.apply();
    }

    private void saveSchedulePage() {
        Spinner blackSpinner = getActivity().findViewById(R.id.scheduleBlackSpinner);
        Spinner blueSpinner = getActivity().findViewById(R.id.scheduleBlueSpinner);
        Spinner greenSpinner = getActivity().findViewById(R.id.scheduleGreenSpinner);
        Spinner yellowSpinner = getActivity().findViewById(R.id.scheduleYellowSpinner);

        SharedPreferences.Editor editor = pref.edit();

        editor.putString(getString(R.string.pref_key_pickup_schedule_black), blackSpinner.getSelectedItem().toString());
        editor.putString(getString(R.string.pref_key_pickup_schedule_blue), blueSpinner.getSelectedItem().toString());
        editor.putString(getString(R.string.pref_key_pickup_schedule_green), greenSpinner.getSelectedItem().toString());
        editor.putString(getString(R.string.pref_key_pickup_schedule_yellow), yellowSpinner.getSelectedItem().toString());

        editor.apply();
    }
}
