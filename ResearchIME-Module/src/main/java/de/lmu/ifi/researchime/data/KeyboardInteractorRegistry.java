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
import de.lmu.ifi.researchime.data.keyboard.KeyboardGateway;
import de.lmu.ifi.researchime.data.keyboard.KeyboardStorage;
import de.lmu.ifi.researchime.data.keyboard.model.KeyboardContainer;

public class KeyboardInteractorRegistry {

    private static ExecutorService executorService = Executors.newCachedThreadPool();
    private static StateInteractor<KeyboardContainer> interactor;

    public synchronized static StateInteractor<KeyboardContainer> getKeyboardInteractor(Context context) {
        if (interactor == null) {
            KeyboardStorage storage = new KeyboardStorage(context);
            KeyboardGateway gateway = new KeyboardGateway(context, storage);
            interactor = new PreferencesStateInteractor<>(executorService, gateway, storage);
        }
        return interactor;
    }
}
