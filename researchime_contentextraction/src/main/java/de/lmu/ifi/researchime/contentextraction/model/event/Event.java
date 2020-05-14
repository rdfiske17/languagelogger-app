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

package de.lmu.ifi.researchime.contentextraction.model.event;

import java.util.HashMap;
import java.util.Map;

import de.lmu.ifi.researchime.contentextraction.model.EventJson;


public abstract class Event {

    public enum Type {
        TOUCH_DOWN, TOUCH_MOVE, TOUCH_UP, AUTO_CORRECT, SUGGESTION_PICKED, PRIVATE_MODE, CONTENT_CHANGE
    }

    private Type type;
    private long timestamp;
    private String userUuid;
    private String keyboardStateUuid;
    private String handPosture;
    private String fieldHintText;
    private String fieldPackageName;
    private Integer fieldId;
    private boolean anonymized;
    private Map<Integer, float[]> sensors;

    public Event(){}

    public Event(Type type){
        this.type = type;
        this.timestamp = System.currentTimeMillis();
    }

    public Type getType() {
        return type;
    }

    public void setSensors(HashMap<Integer, float[]> sensors){
        this.sensors = sensors;
    }

    public void setHandPosture(String handPosture) {
        this.handPosture = handPosture;
    }

    public void setUserUuid(String userUuid) {
        this.userUuid = userUuid;
    }

    public void setKeyboardStateUuid(String keyboardStateUuid) {
        this.keyboardStateUuid = keyboardStateUuid;
    }

    @Override
    public String toString() {
        return String.format("%13d %17s %22s user:%4s.. state:%4s..", timestamp, type, handPosture, userUuid, keyboardStateUuid);
    }

    public String toBriefString() {
        return type.toString();
    }

    public EventJson toEventJson(boolean anonymizeInputEvents){
        return new EventJson(this, anonymizeInputEvents);
    }

    public void setFieldPackageName(String fieldPackageName) {
        this.fieldPackageName = fieldPackageName;
    }

    public void setFieldId(int fieldId) {
        this.fieldId = fieldId;
    }

    public void setFieldHintText(CharSequence fieldHintText) {
        if (fieldHintText != null)
            this.fieldHintText = fieldHintText.toString();
    }

    public String getFieldHintText() {
        return fieldHintText;
    }

    public void anonymize(){
        anonymized = true;
    }

    public String getFieldPackageName() {
        return fieldPackageName;
    }

    public long getTimestamp() {
        return timestamp;
    }

}
