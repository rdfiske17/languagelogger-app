/*
 * Copyright (C) 2016 - 2018 ResearchIME Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.lmu.ifi.researchime.data.utils;

import android.content.Context;
import android.support.annotation.Nullable;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import de.lmu.ifi.researchime.module.R;

public class DateConverter {

    /*
    public static String getFormattedDurationStringFromMillis(int millis) {
        // >=1h -> show hour, minutes and seconds
        if (millis >= 3600000) {
            long hours = TimeUnit.MILLISECONDS.toHours(millis);
            long hoursToMinutes = TimeUnit.HOURS.toMinutes(hours);
            long minutes = TimeUnit.MILLISECONDS.toMinutes(millis) - hoursToMinutes;
            long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(hoursToMinutes + minutes);
            return String.format(Locale.GERMANY, "%01d:%02d:%02d", hours, minutes, seconds);
        }
        //<1h -> show minutes and seconds
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(minutes);
        return String.format(Locale.GERMANY, "%01d:%02d", minutes, seconds);
    }
    */

    public static String getShortDate(long millis, Context context) {
        return getShortDateStringForDate(new Date(millis), context);
    }

    private static String getShortDateStringForDate(Date date, Context context) {
        if (date == null) {
            return "";
        }
        Calendar time = Calendar.getInstance();
        time.setTime(date);
        if (isToday(time)) {
            return context.getString(R.string.date_time_formatter, context.getString(R.string.today), getTimeString(date));
        }
        if (isYesterday(time)) {
            return context.getString(R.string.date_time_formatter, context.getString(R.string.yesterday), getTimeString(date));
        }
        return getDayString(date);
    }

    private static boolean isToday(Calendar time) {
        Calendar now = Calendar.getInstance(); // today
        return (now.get(Calendar.YEAR) == time.get(Calendar.YEAR)
                && now.get(Calendar.DAY_OF_YEAR) == time.get(Calendar.DAY_OF_YEAR));
    }

    private static boolean isYesterday(Calendar time) {
        Calendar now = Calendar.getInstance(); // today
        now.add(Calendar.DAY_OF_YEAR, -1); // yesterday
        return (now.get(Calendar.YEAR) == time.get(Calendar.YEAR)
                && now.get(Calendar.DAY_OF_YEAR) == time.get(Calendar.DAY_OF_YEAR));
    }

    private static String getTimeString(@Nullable Date date) {
        if (date == null) {
            return "";
        }
        DateFormat df = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return df.format(date);
    }

    public static String getDayString(long millis){
        return getDayString(new Date(millis));
    }

    private static String getDayString(@Nullable Date date) {
        if (date == null) {
            return "";
        }
        String formattedDate = new SimpleDateFormat("EEE", Locale.getDefault()).format(date);
        return formattedDate + " " + DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault()).format(date);
    }
}
