package de.beusterse.abfalllro;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;

/**
 * Handles the initial setup UI
 *
 * Created by Felix Beuster
 */
public class SetupActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);

        if (pref.getString(getString(R.string.pref_key_pickup_town), "").equals("") ) {
            Button submit = (Button) findViewById(R.id.locationButton);
            submit.setOnClickListener(this);

            Spinner locationDropdown        = (Spinner)findViewById(R.id.locationDropdown);
            Spinner locationStreetDropdown  = (Spinner)findViewById(R.id.locationStreetDropdown);

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

        } else {
            startActivity( new Intent(this, TrashCheckActivity.class) );
            finish();
        }
    }

    private void fillDropdown(Spinner dropdown, int defaultValueId, int entriesId) {
        String defaultValue = getString( defaultValueId );
        String[] values     = getResources().getStringArray( entriesId );

        ArrayAdapter<String> valuesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, values);

        dropdown.setAdapter( valuesAdapter );
        dropdown.setSelection( valuesAdapter.getPosition(defaultValue) );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity( new Intent(this, SettingsActivity.class) );
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.locationButton) {
            Spinner city    = (Spinner) findViewById(R.id.locationDropdown);
            Spinner street  = (Spinner) findViewById(R.id.locationStreetDropdown);

            CheckBox black = (CheckBox) findViewById(R.id.monthlyBlackCheckBox);
            CheckBox green = (CheckBox) findViewById(R.id.monthlyGreenCheckBox);

            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = pref.edit();

            editor.putString(getString(R.string.pref_key_pickup_town), city.getSelectedItem().toString());
            editor.putString(getString(R.string.pref_key_pickup_street), street.getSelectedItem().toString());

            editor.putBoolean(getString(R.string.pref_key_pickup_schedule_black), black.isChecked());
            editor.putBoolean(getString(R.string.pref_key_pickup_schedule_green), green.isChecked());

            editor.commit();

            startActivity( new Intent(this, TrashCheckActivity.class) );
            finish();
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        String value = parent.getItemAtPosition(pos).toString();

        Spinner locationStreetDropdown  = (Spinner)findViewById(R.id.locationStreetDropdown);

        locationStreetDropdown.setEnabled( value.equals(SettingsActivity.CITY_WITH_STREETS) );
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }
}
