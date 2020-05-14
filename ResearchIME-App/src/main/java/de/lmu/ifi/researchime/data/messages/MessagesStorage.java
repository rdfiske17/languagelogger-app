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

import android.content.Context;

import de.lmu.ifi.researchime.data.messages.model.MessagesContainer;
import de.lmu.ifi.researchime.data.storage.PreferencesStorage;

public class MessagesStorage extends PreferencesStorage<MessagesContainer>{

    private static final String PREFS_NAME = "researchimeMessages";

    public MessagesStorage(Context context) {
        super(MessagesContainer.class, context);
    }

    @Override
    protected MessagesContainer initEmptyDataWrapper() {
        return new MessagesContainer();
    }

    @Override
    protected String getPrefsName() {
        return PREFS_NAME;
    }
}
