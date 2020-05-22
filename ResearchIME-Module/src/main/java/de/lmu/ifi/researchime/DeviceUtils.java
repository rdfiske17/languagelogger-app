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

package de.lmu.ifi.researchime;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

public class DeviceUtils {

    public static boolean isWifiConnected(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return networkInfo.isConnected();
    }

    public static int getCurrentOrientation(@NonNull Context context){
        Resources resources = context.getResources();
        if (resources != null) {
            Configuration config = resources.getConfiguration();
            if (config != null) {
                return config.orientation;
            }
        }
        return -1;
    }

    public static String getCurrentOrientationReadable(@NonNull Context context){
        int orientation = getCurrentOrientation(context);
        return orientation == Configuration.ORIENTATION_PORTRAIT ? "portrait"
                : orientation == Configuration.ORIENTATION_LANDSCAPE ? "landscape" : "unknown";
    }

    @NonNull
    public static ScreenDimensions getPixelScreenDimensions(@NonNull Context context) {

        int widthPx, heightPx;
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        widthPx = metrics.widthPixels;
        heightPx = metrics.heightPixels;
        if (Build.VERSION.SDK_INT >= 14 && Build.VERSION.SDK_INT < 17){
            try {
                widthPx = (Integer) Display.class.getMethod("getRawWidth").invoke(display);
                heightPx = (Integer) Display.class.getMethod("getRawHeight").invoke(display);
            } catch (Exception e) {}
        }
        if (Build.VERSION.SDK_INT >= 17){
            try {
                Point size = new Point();
                Display.class.getMethod("getRealSize", Point.class).invoke(display, size);
                widthPx = size.x;
                heightPx = size.y;
            } catch (Exception e) {}
        }

        if (getCurrentOrientation(context) == Configuration.ORIENTATION_LANDSCAPE){
            // switch width and height
            int temp = widthPx;
            //noinspection SuspiciousNameCombination
            widthPx = heightPx;
            heightPx = temp;
        }
        ScreenDimensions dimens = new ScreenDimensions();
        dimens.heigthPx = heightPx;
        dimens.widthPx = widthPx;
        dimens.heigthMM = heightPx / metrics.ydpi;

        return dimens;
    }

    public static class ScreenDimensions {
        public int widthPx = -1;
        public int heigthPx = -1;
        public float widthMM = -1;
        public float heigthMM = -1;
    }
}
