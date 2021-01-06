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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.List;

import de.lmu.ifi.researchime.base.RestClient;
import de.lmu.ifi.researchime.contentextraction.logging.LogHelper;
import de.lmu.ifi.researchime.contentextraction.model.EventJson;
import retrofit.ResponseCallback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class EventTransmitter {

    private static final String TAG = "EventTransmitter";

    public void transmit(final Context context, List<EventJson> events, final TransmissionCallback callback) {
        LogHelper.i(TAG, String.format("Transmitting %d events to the server.", events.size()));

        final JsonArray array = new JsonArray();
        for (EventJson event : events){
            array.add(event.getJsonAsJsonElement());
        }

        JsonObject object = new JsonObject();
        object.add("events", array);

        RestClient.get(context).postEvents(object, new ResponseCallback() {
            @Override
            public void success(Response response) {
                LogHelper.i(TAG, "Events successfully sent to server.");
                callback.onTransmissionSuccess();
            }

            @Override
            public void failure(RetrofitError error) {
                LogHelper.i(TAG, "Events to server failure: " + RestClient.getErrorDescription(error));
                callback.onTransmissionFailure();
            }
        });
    }

    public interface TransmissionCallback {
        void onTransmissionSuccess();
        void onTransmissionFailure();
    }
}
