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
import android.os.AsyncTask;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.List;

import de.lmu.ifi.researchime.base.RestClient;
import de.lmu.ifi.researchime.contentextraction.logging.LogHelper;
import de.lmu.ifi.researchime.model.KeyboardState;
import retrofit.ResponseCallback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class KeyboardStateTransmissionTask extends AsyncTask<KeyboardState, Void, Void> {

    private static final String TAG = "Keyb.StateTransm.Task";
    private final Context context;
    private final KeyboardStateDatabase database;

    public KeyboardStateTransmissionTask(Context context, KeyboardStateDatabase database){
        this.context = context;
        this.database = database;
    }

    @Override
    protected Void doInBackground(KeyboardState... states) {
        for(KeyboardState state : states){
            database.store(state);
        }
        transmitDBToServer();
        return null;
    }

    private void transmitDBToServer() {
        if (! DeviceUtils.isWifiConnected(context)){
            LogHelper.i(TAG, "Not transmitting keyboars state events to server. WIFI is not connected.");
            return;
        }

        final List<KeyboardState> stateEvents = database.getAll();
        //delete from db to prevent repeated transmission to server
        database.deleteAll();
        sendStateEvents(stateEvents);
    }

    private JsonObject getStateJson(List<KeyboardState> stateEvents){
        JsonObject json = new JsonObject();
        JsonArray events = new JsonArray();
        for(KeyboardState state : stateEvents){
            events.add(state.getJson());
        }
        json.add("events", events);
        return json;
    }

    private void sendStateEvents(final List<KeyboardState> stateEvents){
        RestClient.get(context).postKeyboardState(getStateJson(stateEvents), new ResponseCallback() {
            @Override
            public void success(Response response) {
                LogHelper.i(TAG, "Keyboard state successfully sent to server.");
            }

            @Override
            public void failure(RetrofitError error) {
                LogHelper.e(TAG, "Keyboard state to server failure:\n" + RestClient.getErrorDescription(error));
                database.store(stateEvents.toArray(new KeyboardState[stateEvents.size()]));
            }
        });
    }
}