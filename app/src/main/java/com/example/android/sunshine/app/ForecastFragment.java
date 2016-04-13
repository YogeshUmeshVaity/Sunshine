package com.example.android.sunshine.app;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
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
public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = "ForecastFragment";
    private static final int FORECAST_LOADER_ID = 0;
    private ForecastAdapter mForecastAdapter;

    public ForecastFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Must set this to true here to have the options menu of fragment appear.
        setHasOptionsMenu(true);

        // The CursorAdapter will take data from our cursor using Loader and populate the ListView.
        mForecastAdapter = new ForecastAdapter(getActivity(), null, 0);

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        ListView forecastListView = (ListView) rootView.findViewById(R.id.listview_forecast);
        // Associate the ArrayAdapter with the ListView
        forecastListView.setAdapter(mForecastAdapter);

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        // Prepare the loader. Either re-connect with an existing one, or start a new one.
        getLoaderManager().initLoader(FORECAST_LOADER_ID, null, this);

        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        final String locationSetting = Utility.getPreferredLocation(getActivity());
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                locationSetting, System.currentTimeMillis());
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";

        return new CursorLoader(getActivity(), weatherForLocationUri, null, null, null, sortOrder);
    }

    // This method is called when the Loader completes querying process and the data is ready.
    // We should not close the old cursor.
    // The framework will take care of closing the old cursor once we return.
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor newData) {
        mForecastAdapter.swapCursor(newData);
    }

    // This is called when the last Cursor provided to onLoadFinished() above is about to be closed.
    // We need to make sure we are no longer using it.
    // Here we are removing the reference of previous Cursor from our adapter.
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mForecastAdapter.swapCursor(null);
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
