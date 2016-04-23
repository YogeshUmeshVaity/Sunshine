package com.example.android.sunshine.app;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.sunshine.app.data.WeatherContract;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    // These constants correspond to the projection defined above, and must change if the
    // projection changes
    static final int COL_WEATHER_ID = 0;
    static final int COL_WEATHER_DATE = 1;
    static final int COL_WEATHER_DESC = 2;
    static final int COL_WEATHER_MAX_TEMP = 3;
    static final int COL_WEATHER_MIN_TEMP = 4;
    static final int COL_LOCATION_SETTING = 5;
    static final int COL_WEATHER_CONDITION_ID = 6;
    static final int COL_WEATHER_HUMIDITY = 7;
    static final int COL_WEATHER_PRESSURE = 8;
    static final int COL_WEATHER_WIND_SPEED = 9;


    private static final String[] DETAILS_COLUMNS = {
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
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
            WeatherContract.WeatherEntry.COLUMN_PRESSURE,
            WeatherContract.WeatherEntry.COLUMN_WIND_SPEED
    };
    private static final int DETAILS_LOADER = 1;

    private ShareActionProvider shareActionProvider;
    private String forecastDetails;

    public DetailFragment() {
        // Without this, system won't call the onCreateOptionsMenu.
        setHasOptionsMenu(true);
    }

    /**
     * Initialize the contents of the Activity's standard options menu.  You
     * should place your menu items in to <var>menu</var>.  For this method
     * to be called, you must have first called {@link #setHasOptionsMenu}.  See
     * {@link Activity#onCreateOptionsMenu(Menu) Activity.onCreateOptionsMenu}
     * for more information.
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        // Inflates the menus defined in detailfragment menu xml.
        // without this, Share button in this case won't show up.
        inflater.inflate(R.menu.detailfragment, menu);

        // Find the menu item with ShareActionProvider
        MenuItem shareMenuItem = menu.findItem(R.id.action_share);

        // Assign ShareActionProvider object to member variable when the options menu is created.
        shareActionProvider =
                (ShareActionProvider) MenuItemCompat.getActionProvider(shareMenuItem);

        // Update the shareActionProvider with the intent.
        if (forecastDetails != null) {
            shareActionProvider.setShareIntent(createShareIntent());
        }
    }

    /**
     * Creates intent of weather details to be shared.
     *
     * @return returns an Intent object with weather data.
     */
    private Intent createShareIntent() {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        // If you leave out the flag, when returning to your app (from home screen, from
        // recents etc.), you would see the Activity of the share target
        // (messaging/mailing/IM app) instead of yours.
        shareIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(
                Intent.EXTRA_TEXT, forecastDetails + getString(R.string.hashSunshineApp));
        return shareIntent;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_detail, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAILS_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri weatherForDateUri;
        Intent detailIntent = getActivity().getIntent();
        if (detailIntent == null) return null;
        weatherForDateUri = detailIntent.getData();
        return new CursorLoader(getActivity(), weatherForDateUri, DETAILS_COLUMNS,
                null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Move the cursor to first row as well as test if it's null.
        if (!cursor.moveToFirst()) return;

        String date = Utility.formatDate(cursor.getLong(COL_WEATHER_DATE));
        String weatherDescription = cursor.getString(COL_WEATHER_DESC);
        boolean isMetric = Utility.isMetric(getActivity());
        String high = Utility.formatTemperature(
                getActivity(), cursor.getDouble(COL_WEATHER_MAX_TEMP), isMetric);
        String low = Utility.formatTemperature(
                getActivity(), cursor.getDouble(COL_WEATHER_MIN_TEMP), isMetric);

        forecastDetails = String.format("%s - %s - %s/%s", date, weatherDescription, high, low);
        TextView detailTextView = (TextView) getView().findViewById(R.id.detail_text_view);
        detailTextView.setText(forecastDetails);

        if (shareActionProvider != null) {
            shareActionProvider.setShareIntent(createShareIntent());
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }
}
