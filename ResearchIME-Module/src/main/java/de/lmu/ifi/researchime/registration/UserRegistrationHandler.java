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

package de.lmu.ifi.researchime.registration;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteException;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.orhanobut.logger.Logger;

import java.util.Iterator;

import de.lmu.ifi.researchime.Config;
import de.lmu.ifi.researchime.DeviceUtils;
import de.lmu.ifi.researchime.base.RestClient;
import de.lmu.ifi.researchime.model.User;
import retrofit.ResponseCallback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class UserRegistrationHandler {

    private static final String PUSH_TOKEN_KEY = "pushToken";

    public static @Nullable User getUserOrLaunchRegistration(Context context){
        User user = getUser();
        if(user == null){
            RegistrationActivity.launch(context);
        }
        return user;
    }

    @NonNull
    public static User createUser(@NonNull Context context, @NonNull String uuid, @NonNull String gender, int age){
        DeviceUtils.ScreenDimensions dimens = DeviceUtils.getPixelScreenDimensions(context);
        User user = new User(uuid, gender, age, System.currentTimeMillis(), Build.MANUFACTURER, Build.MODEL, Config.getOSVersion(), dimens.widthPx, dimens.heigthPx);
        user.setPushToken(getPrefsPushToken(context));
        return user;
    }

    public static void updatePushToken(Context context, String token){
        setPrefsPushToken(context, token);
        User user = getUser();
        if(user != null){
            user.setPushToken(token);
            user.save();
            sendUserToBackend(context, user);
        }
    }

    private static void sendUserToBackend(Context context, User user){
        RestClient.get(context).postUser(user.getJson(), new ResponseCallback() {
            @Override
            public void success(Response response) {
                Logger.i("User data sent to server");
            }
            @Override
            public void failure(RetrofitError error) {
                Logger.e("Error sending user data to server", error);
            }
        });
    }

    public static User getUser(){
        try {
            Iterator<User> iterator = User.findAll(User.class);
            //should contain only one user object
            return iterator.hasNext() ? iterator.next() : null;
        } catch(SQLiteException e){
            return null;
        }
    }

    public static String getUserId(){
        User user = getUser();
        return user == null ? null: user.getUuid();
    }

    public static boolean isRegistered(){
        return getUser() != null;
    }

    private static String getPrefsPushToken(Context context){
        return getPrefs(context).getString(PUSH_TOKEN_KEY, null);
    }

    private static void setPrefsPushToken(Context context, String token){
        getPrefs(context).edit().putString(PUSH_TOKEN_KEY, token).apply();
    }

    private static SharedPreferences getPrefs(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
    }
}
