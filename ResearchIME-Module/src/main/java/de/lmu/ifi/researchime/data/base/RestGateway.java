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


import android.content.Context;

import com.google.gson.Gson;

import de.lmu.ifi.researchime.base.RestClient;
import de.lmu.ifi.researchime.base.ServerApi;

public abstract class RestGateway<T> {

    private Gson jsonParser;
    private ServerApi restClient;
    private Class<T> classType;

    public RestGateway(Context context, Class<T> classType){
        this.restClient = RestClient.get(context.getApplicationContext());
        this.jsonParser = new Gson();
        this.classType = classType;
    }

    protected final Gson getJsonParser() {
        return jsonParser;
    }

    protected final ServerApi getClient(){
        return restClient;
    }

    protected final Class<T> getType(){
        return classType;
    }

    public abstract T fetch() throws Exception;

    public abstract T create();
}

