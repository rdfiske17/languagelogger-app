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

package de.lmu.ifi.researchime.data;

import android.content.Context;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.lmu.ifi.researchime.data.base.PreferencesStateInteractor;
import de.lmu.ifi.researchime.data.base.StateInteractor;
import de.lmu.ifi.researchime.data.messages.MessagesGateway;
import de.lmu.ifi.researchime.data.messages.MessagesStorage;
import de.lmu.ifi.researchime.data.messages.model.MessagesContainer;

public class MessagesInteractorRegistry {

    private static ExecutorService executorService = Executors.newCachedThreadPool();
    private static StateInteractor<MessagesContainer> interactor;

    public synchronized static StateInteractor<MessagesContainer> getMessagesInteractor(Context context) {
        if (interactor == null) {
            MessagesStorage storage = new MessagesStorage(context);
            MessagesGateway gateway = new MessagesGateway(context);
            interactor = new PreferencesStateInteractor<>(executorService, gateway, storage);
        }
        return interactor;
    }
}
