package com.example.android.sunshine.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment {

    private ArrayAdapter<String> weekForecastAdapter;

    public ForecastFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Must set this to true here to have the options menu of fragment appear.
        setHasOptionsMenu(true);

        // ArrayAdapter takes data from source: ArrayList
        weekForecastAdapter = new ArrayAdapter<String>(
                getActivity(),
                R.layout.list_item_forecast,
                R.id.list_item_forecast_textview,
                new ArrayList<String>());

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        ListView weekForecastList = (ListView) rootView.findViewById(R.id.listview_forecast);
        // Associate the ArrayAdapter with the ListView
        weekForecastList.setAdapter(weekForecastAdapter);

        weekForecastList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            /**
             * Callback method to be invoked when an item in this AdapterView has
             * been clicked.
             * <p>
             * Implementers can call getItemAtPosition(position) if they need
             * to access the data associated with the selected item.
             *
             * @param parent   The AdapterView where the click happened.
             * @param view     The view within the AdapterView that was clicked (this
             *                 will be a view provided by the adapter)
             * @param position The position of the view in the adapter.
             * @param id       The row id of the item that was clicked.
             */
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent detailIntent = new Intent(getActivity(), DetailActivity.class);
                detailIntent.putExtra(Intent.EXTRA_TEXT, weekForecastAdapter.getItem(position));
                startActivity(detailIntent);
            }
        });

        return rootView;
    }

    /**
     * Called when the Fragment is visible to the user.  This is generally
     * tied to {@link Activity#onStart() Activity.onStart} of the containing
     * Activity's lifecycle.
     */
    @Override
    public void onStart() {
        super.onStart();
        updateWeather();
    }

    private void updateWeather() {
        // Get settings for location(postal code)
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(getContext());
        // location is the postal code
        String location = sharedPreferences.getString(
                getString(R.string.pref_location_key),
                getString(R.string.pref_location_default));
        new FetchWeatherTask(getContext(), weekForecastAdapter).execute(location);
    }

    /**
     * Initialize the contents of the Activity's standard options menu.  You
     * should place your menu items in to <var>menu</var>.  For this method
     * to be called, you must have first called {@link #setHasOptionsMenu}.
     *
     * @param menu The options menu in which you place your items.
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    /**
     * This hook is called whenever an item in your options menu is selected.
     * The default implementation simply returns false to have the normal
     * processing happen (calling the item's Runnable or sending a message to
     * its Handler as appropriate).  You can use this method for any items
     * for which you would like to do processing without those other
     * facilities.
     * <p>
     * <p>Derived classes should call through to the base class for it to
     * perform the default menu handling.
     *
     * @param item The menu item that was selected.
     * @return boolean Return false to allow normal menu processing to
     * proceed, true to consume it here.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            // Get settings for location(postal code)
            updateWeather();
            return true;
        }
        if(id == R.id.action_show_location) {
            showLocationMap();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Sends an intent to open the location on google maps
     * TODO:include the code to check if the google maps is installed, otherwise the the app crashes.
     */
    private void showLocationMap() {
        // Get preference value for location(postal code).
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getContext());
        String postalCode = settings.getString(getString(R.string.pref_location_key), "");

        Uri geoLocation = Uri.parse("geo:0,0?").buildUpon()
                .appendQueryParameter("q", postalCode)
                .build();

        Intent sendLocationToMap = new Intent(Intent.ACTION_VIEW);
        sendLocationToMap.setData(geoLocation);
        if (sendLocationToMap.resolveActivity(getContext().getPackageManager()) != null) {
            startActivity(sendLocationToMap);
        } else {
            // Notify user that Google maps is not installed
            Toast.makeText(getContext(), getString(R.string.google_maps_not_installed), Toast.LENGTH_SHORT).show();
        }
    }
}
