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

package de.lmu.ifi.researchime.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import com.orm.SugarRecord;

/**
 *
 * This class represents the state of the device and contains values that can change from time to time but normally do not change for every event.
 *
 */
public class KeyboardState extends SugarRecord{

    private static GsonBuilder gsonBuilder = new GsonBuilder();
    private static Gson gson = gsonBuilder.excludeFieldsWithoutExposeAnnotation().create();

    /** We have to use expose because otherwise, the fields inherited from sugar record are included. */
    @Expose private String uuid;
    @Expose private long timestamp;
    @Expose private String orientation;
    @Expose private String locale;
    @Expose private int height;
    @Expose private int width;

    @Expose private String layoutId;

    //empty constructor for sugar record
    public KeyboardState(){
    }

    public KeyboardState(String uuid, long timestamp, String orientation, String locale, int height, int width, String layoutId) {
        this.uuid = uuid;
        this.timestamp = timestamp;
        this.orientation = orientation;
        this.locale = locale;
        this.height = height;
        this.width = width;
        this.layoutId = layoutId;
    }

    public JsonObject getJson(){
        return gson.toJsonTree(this).getAsJsonObject();
    }

    public String getUuid() {
        return uuid;
    }
}
