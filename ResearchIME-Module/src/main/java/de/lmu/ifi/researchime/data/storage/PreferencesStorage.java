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

package de.lmu.ifi.researchime.data.storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.google.gson.Gson;

import java.io.Serializable;

public abstract class PreferencesStorage<T extends Serializable> {

    private static final String PREFS_KEY = "data";

    private Gson gson; //init lazily

    private T dataWrapper;
    private final Class<T> classType;
    private final Context context;

    public PreferencesStorage(Class<T> classType, Context context) {
        this.classType = classType;
        this.context = context.getApplicationContext();
        loadFromJson(getPrefsString());
    }

    private void loadFromJson(String json) {
        try{
            dataWrapper = getGson().fromJson(json, classType);
        }catch (Exception e){
            //CustomLogger.log(e);
            //prevent crashes caused by internal Gson errors in deserialization process
        }

        if(dataWrapper == null){
            dataWrapper = initEmptyDataWrapper();
        }
    }

    public void reset(){
        dataWrapper = initEmptyDataWrapper();
        save();
    }

    protected abstract T initEmptyDataWrapper();

    public void set(T dataWrapper){
        if(dataWrapper != null){
            this.dataWrapper = dataWrapper;
            save();
        }
    }

    public final T get(){
        if(dataWrapper == null){
            initEmptyDataWrapper();
        }
        return dataWrapper;
    }

    private String getPrefsString() {
        return getPrefs().getString(PREFS_KEY, "");
    }

    public void save() {
        String jsonString = getGson().toJson(get());
        SharedPreferences.Editor editor = getPrefs().edit();
        if (TextUtils.isEmpty(jsonString)) {
            editor.remove(PREFS_KEY);
        } else {
            editor.putString(PREFS_KEY, jsonString);
        }
        editor.commit();
    }

    protected abstract String getPrefsName();

    protected SharedPreferences getPrefs() {
        return context.getApplicationContext().getSharedPreferences(getPrefsName(), Context.MODE_PRIVATE);
    }

    public boolean hasPrefs(){
        return getPrefs().contains(PREFS_KEY);
    }

    private Gson getGson(){
        if(gson == null){
            gson = new Gson();
        }
        return gson;
    }
}
