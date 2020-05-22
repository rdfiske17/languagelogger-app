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

package de.lmu.ifi.researchime.messaging;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import de.lmu.ifi.researchime.DashboardActivity;
import de.lmu.ifi.researchime.R;
import de.lmu.ifi.researchime.config.ConfigurationManager;
import de.lmu.ifi.researchime.data.MessagesInteractorRegistry;
import de.lmu.ifi.researchime.data.messages.model.MessageModel;

public class MessagingService extends FirebaseMessagingService {

    //this is the first entry point of a notification in Firebase
    //we use our own notification handling and ignore the Firebase Console notifications
    //-> no super call
    @Override
    public void zzm(Intent intent) {
        String payload = intent.getStringExtra("gcm.notification.body");
        if(payload != null){
            JsonObject json = new JsonParser().parse(payload).getAsJsonObject();
            String type = json.get("type").getAsString();
            if(type != null){
                onPayloadReceived(type, json);
            }
        }
    }

    private void onPayloadReceived(String type, JsonObject data) {
        switch(type){
            case "message":
                onMessageReceived(data.getAsJsonObject("data"));
                break;
            case "updateConfig":
                updateConfig();
                break;
        }
    }

    private void onMessageReceived(JsonObject data) {
        MessageModel message = new Gson().fromJson(data, MessageModel.class);
        message.setNewMessage();
        showMessageNotification(message);
        addMessageToObservable(message);
    }

    private void updateConfig(){
        ConfigurationManager.forceUpdateConfigurationsFromServer(this);
        ConfigurationManager.resetLastCheckTime();
    }

    private void addMessageToObservable(final MessageModel message){
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                MessagesInteractorRegistry.getMessagesInteractor(getApplicationContext())
                        .getModel().addMessageAtTop(message);
            }
        });
    }

    private void showMessageNotification(MessageModel message){
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(message.getMessage()))
                        .setSmallIcon(R.drawable.ic_keyboard)
                        .setAutoCancel(true)
                        .setContentTitle(message.getTitle())
                        .setColor(ContextCompat.getColor(this, R.color.colorPrimary))
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setContentText(message.getMessage());
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, DashboardActivity.class), 0);
        mBuilder.setContentIntent(contentIntent);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify((int) (Math.random() * 1000), mBuilder.build());
    }
}
