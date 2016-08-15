/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.sunshine.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.format.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Utility {

    // Format used for storing dates in the database.  ALso used for converting those strings
    // back into date objects for comparison/processing.
    public static final String DATE_FORMAT = "yyyyMMdd";
    private static final String TAG = "Utility.java";

    public static String getPreferredLocation(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_location_key),
            context.getString(R.string.pref_location_default));
    }

    public static boolean isMetric(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        // Get setting for temperatures units from prefs and then compare to metric value.
        return prefs.getString(
            context.getString(R.string.pref_temperature_units_key),
            context.getString(R.string.pref_temperature_units_metric_value))
            .equals(context.getString(R.string.pref_temperature_units_metric_value));
    }

    static String formatTemperature(Context context, double temperature, boolean isMetric) {
        double temp;
        if (!isMetric) {
            temp = 9 * temperature / 5 + 32;
        } else {
            temp = temperature;
        }
        // %.0f is the format string for a float, with 0 decimal places.
        return context.getString(R.string.format_temperature, temp);
    }

    static String formatDate(long dateInMilliseconds) {
        Date date = new Date(dateInMilliseconds);
        return DateFormat.getDateInstance().format(date);
    }

    /**
     * Helper method to convert the database representation of the date into something to display
     * to users.  As classy and polished a user experience as "20140102" is, we can do better.
     * @param context Context to use for resource localization
     * @param dateInMillis The date in milliseconds
     * @return a user-friendly representation of the date.
     */
    public static String getFriendlyDayString(Context context, long dateInMillis, boolean withDate) {
        // The day string for forecast uses the following logic:
        // For today: "Today, June 8"
        // For tomorrow:  "Tomorrow"
        // For the next 5 days: "Wednesday" (just the day name)
        // For all days after that: "Mon Jun 8"

        Time time = new Time();
        time.setToNow();
        long currentTime = System.currentTimeMillis();
        int julianDay = Time.getJulianDay(dateInMillis, time.gmtoff);
        int currentJulianDay = Time.getJulianDay(currentTime, time.gmtoff);

        // withDate is true if this function is called for details fragment.
        // In that case we don't need to proceed further. Just return Day. Because, the date is
        // show separately in details fragment.
        if (withDate) {
            return getDayName(context, dateInMillis);
        }

        // If the date we're building the String for is today's date, the format
        // is "Today, June 24"
        if (julianDay == currentJulianDay) {
            String today = context.getString(R.string.today);
            int formatId = R.string.format_full_friendly_date;
            // TODO: 20/4/16 format() seems redundant, consider removing before publish.
            return String.format(context.getString(
                formatId,
                today,
                getFormattedMonthDay(context, dateInMillis)));
        } else if (julianDay < currentJulianDay + 7) {
            // If the input date is less than a week in the future, just return the day name.
            return getDayName(context, dateInMillis);
        } else {
            // Otherwise, use the form "Mon Jun 3"
            SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
            return shortenedDateFormat.format(dateInMillis);
        }
    }

    /**
     * Given a day, returns just the name to use for that day.
     * E.g "today", "tomorrow", "wednesday".
     * @param context Context to use for resource localization
     * @param dateInMillis The date in milliseconds
     */
    public static String getDayName(Context context, long dateInMillis) {
        // If the date is today, return the localized version of "Today" instead of the actual
        // day name.

        Time t = new Time();
        t.setToNow();
        int julianDay = Time.getJulianDay(dateInMillis, t.gmtoff);
        int currentJulianDay = Time.getJulianDay(System.currentTimeMillis(), t.gmtoff);
        if (julianDay == currentJulianDay) {
            return context.getString(R.string.today);
        } else if (julianDay == currentJulianDay + 1) {
            return context.getString(R.string.tomorrow);
        } else {
            Time time = new Time();
            time.setToNow();
            // Otherwise, the format is just the day of the week (e.g "Wednesday".
            SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE");
            return dayFormat.format(dateInMillis);
        }
    }

    /**
     * Converts db date format to the format "Month day", e.g "June 24".
     * @param context Context to use for resource localization
     * @param dateInMillis The db formatted date string, expected to be of the form specified
     * in Utility.DATE_FORMAT
     * @return The day in the form of a string formatted "December 6"
     */
    public static String getFormattedMonthDay(Context context, long dateInMillis) {
        Time time = new Time();
        time.setToNow();
        SimpleDateFormat dbDateFormat = new SimpleDateFormat(Utility.DATE_FORMAT);
        SimpleDateFormat monthDayFormat = new SimpleDateFormat("MMMM dd");
        String monthDayString = monthDayFormat.format(dateInMillis);
        return monthDayString;
    }

    public static WindDirection getFormattedWind(Context context, float windSpeed, float degrees) {
        WindDirection windDirection = new WindDirection();
        int windFormat;
        if (Utility.isMetric(context)) {
            windFormat = R.string.format_wind_kmh;
        } else {
            windFormat = R.string.format_wind_mph;
            windSpeed = .621371192237334f * windSpeed;
        }

        // From wind direction in degrees, determine compass direction as a string (e.g NW)
        // You know what's fun, writing really long if/else statements with tons of possible
        // conditions.  Seriously, try it!
        String direction = "Unknown";
        String contentDescription = "Unknown";
        if (degrees >= 337.5 || degrees < 22.5) {
            contentDescription = context.getString(R.string.from_north);
            direction = context.getString(R.string.north);
        } else if (degrees >= 22.5 && degrees < 67.5) {
            contentDescription = context.getString(R.string.from_north_east);
            direction = context.getString(R.string.north_east);
        } else if (degrees >= 67.5 && degrees < 112.5) {
            contentDescription = context.getString(R.string.from_east);
            direction = context.getString(R.string.east);
        } else if (degrees >= 112.5 && degrees < 157.5) {
            contentDescription = context.getString(R.string.from_south_east);
            direction = context.getString(R.string.south_east);
        } else if (degrees >= 157.5 && degrees < 202.5) {
            contentDescription = context.getString(R.string.from_south);
            direction = context.getString(R.string.south);
        } else if (degrees >= 202.5 && degrees < 247.5) {
            contentDescription = context.getString(R.string.from_south_west);
            direction = context.getString(R.string.south_west);
        } else if (degrees >= 247.5 && degrees < 292.5) {
            contentDescription = context.getString(R.string.from_west);
            direction = context.getString(R.string.west);
        } else if (degrees >= 292.5 || degrees < 22.5) {
            contentDescription = context.getString(R.string.from_north_west);
            direction = context.getString(R.string.north_west);
        }
        windDirection.setDisplayText(
            String.format(context.getString(windFormat), windSpeed, direction));
        windDirection.setContentDescription(
            String.format(context.getString(windFormat), windSpeed, contentDescription));
        return windDirection;
    }

    /**
     * Helper method to provide the icon resource id according to the weather condition id returned
     * by the OpenWeatherMap call.
     * @param weatherId from OpenWeatherMap API response
     * @return resource id for the corresponding icon. -1 if no relation is found.
     */
    public static int getIconResourceForWeatherCondition(int weatherId) {
        // Based on weather code data found at:
        // http://bugs.openweathermap.org/projects/api/wiki/Weather_Condition_Codes
        if (weatherId >= 200 && weatherId <= 232) {
            return R.drawable.ic_storm;
        } else if (weatherId >= 300 && weatherId <= 321) {
            return R.drawable.ic_light_rain;
        } else if (weatherId >= 500 && weatherId <= 504) {
            return R.drawable.ic_rain;
        } else if (weatherId == 511) {
            return R.drawable.ic_snow;
        } else if (weatherId >= 520 && weatherId <= 531) {
            return R.drawable.ic_rain;
        } else if (weatherId >= 600 && weatherId <= 622) {
            return R.drawable.ic_snow;
        } else if (weatherId >= 701 && weatherId <= 761) {
            return R.drawable.ic_fog;
        } else if (weatherId == 761 || weatherId == 781) {
            return R.drawable.ic_storm;
        } else if (weatherId == 800) {
            return R.drawable.ic_clear;
        } else if (weatherId == 801) {
            return R.drawable.ic_light_clouds;
        } else if (weatherId >= 802 && weatherId <= 804) {
            return R.drawable.ic_cloudy;
        }
        return -1;
    }

    /**
     * Helper method to provide the art resource id according to the weather condition id returned
     * by the OpenWeatherMap call.
     * @param weatherId from OpenWeatherMap API response
     * @return resource id for the corresponding image. -1 if no relation is found.
     */
    public static int getArtResourceForWeatherCondition(int weatherId) {
        // Based on weather code data found at:
        // http://bugs.openweathermap.org/projects/api/wiki/Weather_Condition_Codes
        if (weatherId >= 200 && weatherId <= 232) {
            return R.drawable.art_storm;
        } else if (weatherId >= 300 && weatherId <= 321) {
            return R.drawable.art_light_rain;
        } else if (weatherId >= 500 && weatherId <= 504) {
            return R.drawable.art_rain;
        } else if (weatherId == 511) {
            return R.drawable.art_snow;
        } else if (weatherId >= 520 && weatherId <= 531) {
            return R.drawable.art_rain;
        } else if (weatherId >= 600 && weatherId <= 622) {
            return R.drawable.art_rain;
        } else if (weatherId >= 701 && weatherId <= 761) {
            return R.drawable.art_fog;
        } else if (weatherId == 761 || weatherId == 781) {
            return R.drawable.art_storm;
        } else if (weatherId == 800) {
            return R.drawable.art_clear;
        } else if (weatherId == 801) {
            return R.drawable.art_light_clouds;
        } else if (weatherId >= 802 && weatherId <= 804) {
            return R.drawable.art_clouds;
        }
        return -1;
    }

    public static float roundFigureDegrees(float degrees) {
        if (degrees >= 337.5 || degrees < 22.5) {
            return 0;
        } else if (degrees >= 22.5 && degrees < 67.5) {
           return 45;
        } else if (degrees >= 67.5 && degrees < 112.5) {
            return 90;
        } else if (degrees >= 112.5 && degrees < 157.5) {
            return 135;
        } else if (degrees >= 157.5 && degrees < 202.5) {
           return 180;
        } else if (degrees >= 202.5 && degrees < 247.5) {
            return 225;
        } else if (degrees >= 247.5 && degrees < 292.5) {
           return 270;
        } else if (degrees >= 292.5 || degrees < 22.5) {
            return 315;
        }
        return 0;
    }

    /**
     * Holds the wind direction information and respective content description.
     */
    public static class WindDirection {
        private String displayText;
        private String contentDescription;

        public String getDisplayText() {
            return displayText;
        }

        public void setDisplayText(String displayText) {
            this.displayText = displayText;
        }

        public String getContentDescription() {
            return contentDescription;
        }

        public void setContentDescription(String contentDescription) {
            this.contentDescription = contentDescription;
        }
    }
}
