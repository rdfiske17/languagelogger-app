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
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import de.lmu.ifi.researchime.contentextraction.logging.LogHelper;
import de.lmu.ifi.researchime.contentextraction.model.EventJson;
import de.lmu.ifi.researchime.contentextraction.model.event.Event;
import de.lmu.ifi.researchime.data.KeyboardInteractorRegistry;
import de.lmu.ifi.researchime.data.keyboard.model.KeyboardContainer;


public class EventBuffer {

    private static final String TAG = "EventBuffer";
    private List<Event> buffer = new ArrayList<>();

    public void add(Event event){
        buffer.add(event);
    }

    public List<EventJson> getAllAsJsonEvents(Context context){
        List<EventJson> jsonEvents = new ArrayList<>(buffer.size());
        LogHelper.i(TAG, String.format("Buffer size is %d.", buffer.size()));
        LogHelper.i(TAG, "Start converting " + buffer.size() + " events to json.");

        KeyboardContainer keyboardContainer = KeyboardInteractorRegistry.getKeyboardInteractor(context).getModel();


        for (Event event : buffer){
            jsonEvents.add(event.toEventJson(keyboardContainer.isAnonymizeInputEvents()));
        }
        LogHelper.i(TAG, "Finished converting " + buffer.size() + " events to json.");
        return jsonEvents;
    }

    public List<Event> getAll(){
        return buffer;
    }

    public boolean isEmpty(){
        return buffer.isEmpty();
    }
}
