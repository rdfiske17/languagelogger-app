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

import android.util.Log;

import java.util.List;

import de.lmu.ifi.researchime.contentextraction.logging.LogHelper;
import de.lmu.ifi.researchime.model.KeyboardState;

public class KeyboardStateDatabase {

    private static final String TAG = "KeyboardStateDatabase";

    public List<KeyboardState> getAll(){
        return KeyboardState.listAll(KeyboardState.class);
    }

    public void store(KeyboardState... events) {
        KeyboardState.saveInTx(events);
        LogHelper.i(TAG, String.format("Stored %d keyboard state events in DB.", events.length));
    }

    public void deleteAll(){
        KeyboardState.deleteAll(KeyboardState.class);
        LogHelper.i(TAG, "Deleted all keyboard state events from DB");
    }
}
