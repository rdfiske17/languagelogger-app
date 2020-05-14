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


import com.google.gson.Gson;

import java.io.IOException;

public abstract class JsonGateway<T> {

    private static Gson jsonParser;

    protected <T> T fetch(Class<T> tClass, String json) throws IOException {
        createClient();
        return jsonParser.fromJson(json, tClass);
    }

    private synchronized void createClient() {
        if (jsonParser == null) {
            jsonParser = new Gson();
        }
    }

    public abstract T fetch() throws IOException;

    public abstract T create();
}

