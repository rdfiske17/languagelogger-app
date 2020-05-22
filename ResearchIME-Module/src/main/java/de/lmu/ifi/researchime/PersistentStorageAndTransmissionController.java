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
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.sql.language.SQLite;

import java.util.List;

import de.lmu.ifi.researchime.contentextraction.logging.LogHelper;
import de.lmu.ifi.researchime.contentextraction.model.EventJson;
import de.lmu.ifi.researchime.data.KeyboardInteractorRegistry;
import de.lmu.ifi.researchime.data.keyboard.model.KeyboardContainer;
import de.lmu.ifi.researchime.filter.AnonymizationFilter;
import de.lmu.ifi.researchime.filter.ResearchImeFilter;
import de.lmu.ifi.researchime.module.R;

public class PersistentStorageAndTransmissionController {

    private static final String TAG = "PersistentStorageATC";
    private final int databaseTransmissionThreshold;

    private AnonymizationFilter filter;
    private EventTransmitter transmitter;
    private KeyboardContainer keyboardContainer;

    public PersistentStorageAndTransmissionController(@NonNull Context context) {
        int defaultDatabaseTransmissionThreshold = context.getResources().getInteger(R.integer.research_config_event_transmission_threshold_default);
        if(keyboardContainer == null) keyboardContainer = KeyboardInteractorRegistry.getKeyboardInteractor(context).getModel();
        databaseTransmissionThreshold = PreferenceManager.getDefaultSharedPreferences(context).getInt(
                context.getString(R.string.research_config_event_transmission_threshold),
                defaultDatabaseTransmissionThreshold);

        try {
            if (context.getResources().getBoolean(R.bool.filter_ngram)){
                int nGramFilterN = context.getResources().getInteger(R.integer.filter_ngram_n);
                double nGramFilterP = Double.parseDouble(context.getResources().getString(R.string.filter_ngram_p));
                filter = new ResearchImeFilter(nGramFilterN, nGramFilterP);
            }
        }
        catch(NumberFormatException e){
            e.printStackTrace();
        }

        transmitter = new EventTransmitter();
    }

    public void storeEvents(Context context, EventBuffer buffer){
        new StoreAndTransmitTask().execute(context, buffer); // stores buffer to db events, transmits events, deletes db
        LogHelper.i(TAG,"disabled transmission");
    }

    private class StoreAndTransmitTask extends AsyncTask<Object, Void, Void> {

        @Override
        protected Void doInBackground(Object... params) {
            Context context = (Context)params[0];
            EventBuffer buffer = (EventBuffer)params[1];
            EventBuffer anonymizedBuffer = new EventBuffer();
            // anonymization of ResearchIME v1 disabled, because it otherwise hinders the content abstraction
//            if (filter != null) {
//                anonymizedBuffer = filter.filter(buffer);
//            }
//            else{
            anonymizedBuffer = buffer;
//            }
            storeBufferToDB(context, anonymizedBuffer);
            transmitDBToServer(context);

            //delete from db to prevent repeated transmission to server
            SQLite.delete().from(EventJson.class).executeUpdateDelete();

            return null;
        }

        private void storeBufferToDB(Context context, EventBuffer buffer) {
            LogHelper.i(TAG,"Store buffer to DB ...");

            List<EventJson> jsonEvents = buffer.getAllAsJsonEvents(context);
            for (EventJson eventJson : jsonEvents){
                eventJson.save();
            }
        }

        private void transmitDBToServer(Context context) {
            long count = SQLite.select().from(EventJson.class).count();
            if (count < databaseTransmissionThreshold) {
                LogHelper.i(TAG, String.format("Not transmitting events to server. %d events is under threshold of %d events.", count, databaseTransmissionThreshold));
                return;
            }

            if (!DeviceUtils. isWifiConnected(context)){
                LogHelper.i(TAG, "Not transmitting events to server. WIFI is not connected.");
                return;
            }

            final List<EventJson> events = SQLite.select().from(EventJson.class).queryList();

            transmitter.transmit(context, events, new EventTransmitter.TransmissionCallback() {
                @Override
                public void onTransmissionSuccess() {
                    //everything fine!
                }

                @Override
                public void onTransmissionFailure() {
                    //store back to database and try next time
                    for(EventJson eventJson : events){
                        eventJson.save();
                    }
                }
            });
        }
    }
}
