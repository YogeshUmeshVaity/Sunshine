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
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import com.example.android.sunshine.app.data.WeatherContract;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    // Since the database always returns the columns in the order we specify in projection,
    // we can rely on the indices in cursor matching the order from our projection.
    // That way avoiding the inefficient getColumnIndex() calls.
    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    static final int COL_WEATHER_ID = 0;
    static final int COL_WEATHER_DATE = 1;
    static final int COL_WEATHER_DESC = 2;
    static final int COL_WEATHER_MAX_TEMP = 3;
    static final int COL_WEATHER_MIN_TEMP = 4;
    static final int COL_LOCATION_SETTING = 5;
    static final int COL_WEATHER_CONDITION_ID = 6;
    static final int COL_COORD_LAT = 7;
    static final int COL_COORD_LONG = 8;
    private static final String CURRENT_LIST_POSTION = "selectedItemPosition";
    private static final String LOG_TAG = "ForecastFragment";
    private static final int FORECAST_LOADER_ID = 0;
    private static final String[] FORECAST_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.LocationEntry.COLUMN_COORD_LAT,
            WeatherContract.LocationEntry.COLUMN_COORD_LONG
    };
    private ListView forecastListView;
    private int selectedItemPosition = ListView.INVALID_POSITION;
    private ForecastAdapter mForecastAdapter;
    private ForecastFragment.Callback mCallBack;
    private boolean specialTodayLayout;

    public ForecastFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Must set this to true here to have the options menu of fragment appear.
        setHasOptionsMenu(true);

        // The CursorAdapter will take data from our cursor using Loader and populate the ListView.
        mForecastAdapter = new ForecastAdapter(getActivity(), null, 0);
        mForecastAdapter.setSpecialTodayLayout(specialTodayLayout);

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        forecastListView = (ListView) rootView.findViewById(R.id.listview_forecast);
        // Associate the ArrayAdapter with the ListView
        forecastListView.setAdapter(mForecastAdapter);

        forecastListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView adapterView, View view, int position, long id) {
                // CursorAdapter returns a cursor at the correct position for getItem(), or null
                // if it cannot seek to that position.
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null) {
                    String locationSetting = Utility.getPreferredLocation(getActivity());
                    Uri uri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
                            locationSetting, cursor.getLong(COL_WEATHER_DATE));
                    ((Callback)getActivity()).onItemSelected(uri);
                    // Save the selected position
                }
                selectedItemPosition = position;
            }
        });

        if (savedInstanceState != null
                && savedInstanceState.containsKey(CURRENT_LIST_POSTION)) {
            selectedItemPosition = savedInstanceState.getInt(CURRENT_LIST_POSTION);
        }

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        // Prepare the loader. Either re-connect with an existing one, or start a new one.
        getLoaderManager().initLoader(FORECAST_LOADER_ID, null, this);

        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (selectedItemPosition != ListView.INVALID_POSITION) {
            outState.putInt(CURRENT_LIST_POSTION, selectedItemPosition);
        }
        super.onSaveInstanceState(outState);
    }

    /**
     * Initialize the contents of the Activity's standard options menu.  You
     * should place your menu items in to <var>menu</var>.  For this method
     * to be called, you must have first called {@link #setHasOptionsMenu}.
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
        if (id == R.id.action_show_location) {
            showLocationMap();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateWeather() {
        // location is the postal code
        String location = Utility.getPreferredLocation(getActivity());
        FetchWeatherTask weatherTask = new FetchWeatherTask(getContext());
        weatherTask.execute(location);
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

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // This is called when a new Loader needs to be created.
        // This fragment only uses one loader, so we don't care about checking the id.

        final String locationSetting = Utility.getPreferredLocation(getActivity());
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
            locationSetting, System.currentTimeMillis());
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";

        return new CursorLoader(
            getActivity(), weatherForLocationUri, FORECAST_COLUMNS, null, null, sortOrder);
    }

    // This method is called when the Loader completes querying process and the data is ready.
    // We should not close the old cursor.
    // The framework will take care of closing the old cursor once we return.
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor newData) {
        mForecastAdapter.swapCursor(newData);
        // If we don't need to restart the loader, and there's a desired position to restore to,
        // do so now.
        if (selectedItemPosition != ListView.INVALID_POSITION) {
            forecastListView.smoothScrollToPosition(selectedItemPosition);
        }
    }

    // This is called when the last Cursor provided to onLoadFinished() above is about to be closed.
    // We need to make sure we are no longer using it.
    // Here we are removing the reference of previous Cursor from our adapter.
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mForecastAdapter.swapCursor(null);
    }

    public void onLocationChanged() {
        updateWeather();
        getLoaderManager().restartLoader(FORECAST_LOADER_ID, null, this);
    }

    public void setSpecialTodayLayout(boolean specialTodayLayout) {
        this.specialTodayLayout = specialTodayLayout;
        if (mForecastAdapter != null) {
            mForecastAdapter.setSpecialTodayLayout(specialTodayLayout);
        }
    }

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(Uri dateUri);
    }
}
