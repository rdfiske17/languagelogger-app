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
import android.support.annotation.NonNull;
import android.view.View;

import java.util.UUID;

import de.lmu.ifi.researchime.data.KeyboardInteractorRegistry;
import de.lmu.ifi.researchime.model.KeyboardState;

public class KeyboardStateController {

    private final KeyboardStateDatabase database;

    public static KeyboardState getCurrentState(@NonNull Context context, @NonNull View view, String locale) {
        String uuid = UUID.randomUUID().toString();
        String orientation = DeviceUtils.getCurrentOrientationReadable(context);
        String layoutId = KeyboardInteractorRegistry.getKeyboardInteractor(context).getModel().getActiveLayoutId();
        return new KeyboardState(uuid, System.currentTimeMillis(), orientation, locale, view.getHeight(), view.getWidth(), layoutId);
    }

    public KeyboardStateController(){
        this.database = new KeyboardStateDatabase();
    }

    public void onStateChanged(KeyboardState state, Context context){
        new KeyboardStateTransmissionTask(context, database).execute(state);
    }
}
