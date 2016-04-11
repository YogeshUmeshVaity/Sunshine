package com.example.android.sunshine.app;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.example.android.sunshine.app.data.WeatherContract;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment {

    private static final String LOG_TAG = "ForecastFragment";
    private ForecastAdapter mForecastAdapter;

    public ForecastFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Must set this to true here to have the options menu of fragment appear.
        setHasOptionsMenu(true);

        String locationSetting = Utility.getPreferredLocation(getActivity());

        // Sort order ascending by date
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                locationSetting, System.currentTimeMillis());

        Cursor cursor = getActivity()
                .getContentResolver()
                .query(weatherForLocationUri, null, null, null, sortOrder);

        // The CursorAdapter will take data from our cursor and populate the ListView
        // However, we cannot use FLAG_AUTO_REQUERY since it is deprecated, so we will end
        // up with an empty list the first time we run.
        mForecastAdapter = new ForecastAdapter(getActivity(), cursor, 0);

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        ListView forecastListView = (ListView) rootView.findViewById(R.id.listview_forecast);
        // Associate the ArrayAdapter with the ListView
        forecastListView.setAdapter(mForecastAdapter);

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
        // location is the postal code
        String location = Utility.getPreferredLocation(getActivity());
        FetchWeatherTask weatherTask = new FetchWeatherTask(getContext());
        weatherTask.execute(location);
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
        String postalCode = Utility.getPreferredLocation(getActivity());
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
