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


package de.lmu.ifi.researchime.contentextraction.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

import java.lang.reflect.Type;

import de.lmu.ifi.researchime.contentextraction.model.event.AutoCorrectEvent;
import de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent;
import de.lmu.ifi.researchime.contentextraction.model.event.Event;
import de.lmu.ifi.researchime.contentextraction.model.event.PrivateModeEvent;
import de.lmu.ifi.researchime.contentextraction.model.event.SuggestionPickedEvent;
import de.lmu.ifi.researchime.contentextraction.model.event.TouchEvent;

@Table(database = RimeContentExtractionDbFlowDb.class)
public class EventJson extends BaseModel {

    private static Gson gson =  new GsonBuilder().registerTypeAdapter(Event.class, new JsonDeserializer<Event>() {
        @Override
        public Event deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            String type = jsonObject.get("type").getAsString();
            Class typeClass = null;
            switch(type) {
                case "TOUCH_DOWN":
                    typeClass = TouchEvent.class;
                    break;
                case "TOUCH_MOVE":
                    typeClass = TouchEvent.class;
                    break;
                case "TOUCH_UP":
                    typeClass = TouchEvent.class;
                    break;
                case "AUTO_CORRECT":
                    typeClass = AutoCorrectEvent.class;
                    break;
                case "SUGGESTION_PICKED":
                    typeClass = SuggestionPickedEvent.class;
                    break;
                case "PRIVATE_MODE":
                    typeClass = PrivateModeEvent.class;
                    break;
                case "CONTENT_CHANGE":
                    typeClass = ContentChangeEvent.class;
                    break;
            }

            return context.deserialize(json, typeClass);
        }
    }).create();
    private static JsonParser parser = new JsonParser();

    @PrimaryKey(autoincrement = true)
    private Integer id;

    @Column
    private String json = "";

    public EventJson(Event event, boolean anonymizeInputEvents){
        JsonElement jsonElement = gson.toJsonTree(event);
        if (jsonElement.getAsJsonObject().has("content")) {
            jsonElement.getAsJsonObject().remove("content");
        }
        if (anonymizeInputEvents){
            if (jsonElement.getAsJsonObject().has("code")) {
                jsonElement.getAsJsonObject().remove("code");
            }
            if (jsonElement.getAsJsonObject().has("x")) {
                jsonElement.getAsJsonObject().remove("x");
            }
            if (jsonElement.getAsJsonObject().has("y")) {
                jsonElement.getAsJsonObject().remove("y");
            }
        }
        json = jsonElement.toString();
    }

    //empty constructor for sugar record
    public EventJson(){}

    @Override
    public String toString() {
        return json;
    }

    public JsonElement getJsonAsJsonElement(){
        return parser.parse(json).getAsJsonObject();
    }

    public Event getEvent(){
        if (json == null || "".equals(json)) {
            return null;
        }
        return gson.fromJson(getJsonAsJsonElement(), Event.class);
    }

    public void setJson(String json) {
        this.json = json;
    }

    public String getJson() {
        return json;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}
