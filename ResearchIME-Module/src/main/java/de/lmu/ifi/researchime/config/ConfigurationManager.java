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

package de.lmu.ifi.researchime.config;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import de.lmu.ifi.researchime.base.RestClient;
import de.lmu.ifi.researchime.contentextraction.logging.LogHelper;
import de.lmu.ifi.researchime.data.KeyboardInteractorRegistry;
import de.lmu.ifi.researchime.module.R;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ConfigurationManager {

    private static final String TAG = "ConfigurationManager";

    private static long lastCheck;

    public static void updateConfigurationsFromServer(Context context) {
        long nextCheckTime = lastCheck + context.getResources().getInteger(R.integer.research_max_config_check_interval_seconds) * 1000;
        if (System.currentTimeMillis() > nextCheckTime) {
            forceUpdateConfigurationsFromServer(context);
        }
    }

    public static void forceUpdateConfigurationsFromServer(Context context){
        context = context.getApplicationContext();
        loadRemoteConfiguration(context);
        loadKeyboardLayout(context);
    }

    public static void resetLastCheckTime(){
        lastCheck = 0;
    }

    private static void loadKeyboardLayout(Context context){
        KeyboardInteractorRegistry.getKeyboardInteractor(context).reload();
    }

    private static void loadRemoteConfiguration(final Context context){
        RestClient.get(context).getConfiguration(new Callback<JsonObject>() {
            @Override
            public void success(JsonObject configurations, Response response) {
                lastCheck = System.currentTimeMillis();
                if (configurations != null){
                    onSuccess(context, configurations);
                }
            }

            @Override
            public void failure(RetrofitError error) {
                LogHelper.e(TAG,RestClient.getErrorDescription(error));
            }
        });
    }

    private static void onSuccess(Context context, JsonObject configurations){
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        for (String configKey : context.getResources().getStringArray(R.array.research_remote_configurations)){
            JsonElement configValueElement = configurations.get(configKey);
            if (configValueElement != null && configValueElement.isJsonPrimitive()){
                JsonPrimitive configValue = configValueElement.getAsJsonPrimitive();
                LogHelper.i(TAG, configKey + " = " + configValue);
                if (configValue.isBoolean()){
                    editor.putBoolean(configKey, configValue.getAsBoolean());
                } else if (configValue.isNumber()){
                    editor.putInt(configKey, configValue.getAsInt());
                } else if (configValue.isString()){
                    editor.putString(configKey, configValue.getAsString());
                }
            }
        }
        editor.apply();
    }
}
