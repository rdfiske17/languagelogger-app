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

package de.lmu.ifi.researchime.data.messages;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import de.lmu.ifi.researchime.base.RestClient;
import de.lmu.ifi.researchime.data.messages.model.MessageModel;
import retrofit.ResponseCallback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MessagePoster {

    private static final String TAG = "MessagePoster";

    private Gson gson;
    private MessagePostCallback listener;

    public MessagePoster(){
        this.gson = new Gson();
    }

    public void setListener(MessagePostCallback listener){
        this.listener = listener;
    }

    public void post(final MessageModel message){
        JsonObject json = gson.toJsonTree(message).getAsJsonObject();
        RestClient.get(null).postMessage(json, new ResponseCallback() {
            @Override
            public void success(Response response) {
                if(listener != null){
                    listener.onSuccess(message);
                }
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e(TAG, "error posting message to server " + error);
                if(listener != null){
                    listener.onError();
                }
            }
        });
    }

    public interface MessagePostCallback{
        void onSuccess(MessageModel message);
        void onError();
    }
}
