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
 * This class represents the user and his device with all values that should not change over the course of the study.
 */
public class User extends SugarRecord{

    private static GsonBuilder gsonBuilder = new GsonBuilder();
    private static Gson gson = gsonBuilder.excludeFieldsWithoutExposeAnnotation().create();

    /** We have to use expose because otherwise, the fields inherited from sugar record are included. */
    @Expose private String uuid;
    @Expose private String gender;
    @Expose private int age;
    @Expose private long registrationTimestamp;
    @Expose private String deviceManufacturer;
    @Expose private String deviceModel;
    @Expose private int deviceScreenWidth;
    @Expose private int deviceScreenHeight;

    @Expose private String osVersion;
    @Expose private String pushToken;

    //TODO screen dimensions in mm -> inaccurate values

    //empty constructor for sugar record
    public User(){
    }

    public User(String uuid, String gender, int age, long registrationTimestamp, String deviceManufacturer, String deviceModel, String osVersion, int displayWidth, int displayHeight) {
        this.uuid = uuid;
        this.gender = gender;
        this.age = age;
        this.registrationTimestamp = registrationTimestamp;
        this.deviceManufacturer = deviceManufacturer;
        this.deviceModel = deviceModel;
        this.osVersion = osVersion;
        this.deviceScreenWidth = displayWidth;
        this.deviceScreenHeight = displayHeight;
    }

    public void setPushToken(String pushToken){
        this.pushToken = pushToken;
    }

    @Override
    public String toString() {
        return "User{" +
                "uuid='" + uuid + '\'' +
                ", gender='" + gender + '\'' +
                ", age=" + age +
                ", registrationTimestamp=" + registrationTimestamp +
                ", deviceManufacturer='" + deviceManufacturer + '\'' +
                ", deviceModel='" + deviceModel + '\'' +
                ", deviceScreenWidth=" + deviceScreenWidth +
                ", deviceScreenHeight=" + deviceScreenHeight +
                '}';
    }

    public JsonObject getJson(){
        return gson.toJsonTree(this).getAsJsonObject();
    }

    public String getUuid() {
        return uuid;
    }
}
