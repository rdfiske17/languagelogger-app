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

import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import de.lmu.ifi.researchime.contentextraction.logging.LogHelper;
import de.lmu.ifi.researchime.module.R;

public class SensorController implements SensorEventListener {

    private static final String TAG = "SensorController";
    private static final String SENSOR_TYPES_CONFIG_STRING_SEPARATOR = ",";

    private android.hardware.SensorManager sensorManager;
    private List<Sensor> sensors = new ArrayList<>();
    private int sensorSamplingMillis;
    private int sensorValuesOutdatedMillis;

    private SparseArray<SensorEvent> latestSensorEvents = new SparseArray<>();
    private SparseArray<Long> latestSensorUpdates = new SparseArray<>();

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SensorController(Context context){

        //comma-separated int list of the sensors which should be logged
        String sensorTypesString = PreferenceManager.getDefaultSharedPreferences(context).getString(
                context.getString(R.string.research_config_sensors),
                ""
        );

        sensorSamplingMillis = PreferenceManager.getDefaultSharedPreferences(context).getInt(
                context.getString(R.string.research_config_sensor_sampling_millis),
                context.getResources().getInteger(R.integer.research_config_sensor_sampling_millis_default)
        );

        sensorValuesOutdatedMillis = PreferenceManager.getDefaultSharedPreferences(context).getInt(
                context.getString(R.string.research_config_sensor_values_outdated_millis),
                context.getResources().getInteger(R.integer.research_config_sensor_values_outdated_millis_default)
        );

        List<Integer> sensorTypes = new ArrayList<>();

        for (String sensorTypeString : sensorTypesString.split(SENSOR_TYPES_CONFIG_STRING_SEPARATOR)){
            try {
                sensorTypes.add(Integer.parseInt(sensorTypeString.trim()));
            }
            catch (NumberFormatException e){
                LogHelper.e(TAG, sensorTypeString.trim() + " is not a valid sensor type, only int allowed here");
            }
        }

        sensorManager = (android.hardware.SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

        for (int sensorType : sensorTypes){
            Sensor sensor = sensorManager.getDefaultSensor(sensorType);
            if (sensor == null){
                LogHelper.w(TAG, "This device has no sensor of type " + sensorType);
            }
            else {
                sensors.add(sensor);
            }
        }
    }

    public void startRecording(){
        //asynchronous because it needed more than 500ms which adds to the startup of the keyboard view
        new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... params) {
                LogHelper.i(TAG, "Start recording");
                for (Sensor sensor : sensors){
                    sensorManager.registerListener(SensorController.this, sensor, 1000 * sensorSamplingMillis);
                }
                return null;
            }
        }.execute();
    }

    public void stopRecording(){
        LogHelper.i(TAG, "Stop recording");
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor != null) {
            latestSensorEvents.put(event.sensor.getType(), event);
            latestSensorUpdates.put(event.sensor.getType(), System.currentTimeMillis());
        }
    }

    @Override
    public void onAccuracyChanged(android.hardware.Sensor sensor, int accuracy) {

    }

    public HashMap<Integer, float[]> getLatestSensorValues() {
        HashMap<Integer, float[]> result = new HashMap<>();
        for (Sensor sensor : sensors){
            int sensorType = sensor.getType();
            Long latestSensorUpdate = latestSensorUpdates.get(sensorType);
            SensorEvent latestSensorEvent = latestSensorEvents.get(sensorType);
            if (latestSensorUpdate != null && latestSensorEvent != null) {
                if (System.currentTimeMillis() - latestSensorUpdate < sensorValuesOutdatedMillis) {
                    result.put(sensorType, latestSensorEvent.values.clone()); //have to clone so that future sensor value changes don't override the historical ones
                    LogHelper.d(TAG, "Sensor " + sensorType + ": " + Arrays.toString(latestSensorEvent.values));
                }
                else{
                    LogHelper.d(TAG, "Values of sensor " + sensorType + " are older than " + sensorValuesOutdatedMillis + "ms.");
                }
            }
        }
        return result;
    }
}