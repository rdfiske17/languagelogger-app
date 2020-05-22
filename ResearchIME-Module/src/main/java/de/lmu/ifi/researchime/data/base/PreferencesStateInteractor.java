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

package de.lmu.ifi.researchime.data.base;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.concurrent.ExecutorService;

import de.lmu.ifi.researchime.data.storage.PreferencesStorage;

public class PreferencesStateInteractor<T extends BaseModel<T> & Serializable> extends StateInteractor<T>{

    private PreferencesStorage<T> storage;

    public PreferencesStateInteractor(@NonNull ExecutorService service, RestGateway<T> gateway, PreferencesStorage<T> storage){
        super(service, gateway);
        this.storage = storage;
    }

    @Override
    protected void onFetched(T data) {
        super.onFetched(data);
        storage.set(data);
    }

    @Override
    protected void onFetchError(Exception e) {
        if(storage.hasPrefs()){
            super.onFetched(storage.get());
        }else{
            super.onFetchError(e);
        }
    }
}