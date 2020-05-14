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

import java.text.DecimalFormat;


public class TouchEvent extends Event {

    private String code;

    private Integer x;
    private Integer y;
    private Float pressure;
    private Float size;
    private String inputMode;

    public TouchEvent(Type type, String code, int x, int y, float pressure, float size, EventInputMode inputMode){
        this(type, code, inputMode);
        this.x = x;
        this.y = y;
        this.pressure = pressure;
        this.size = size;
    }

    public TouchEvent(Type type, String code, EventInputMode inputMode){
        super(type);
        this.code = code;
        this.inputMode = inputMode.getLogText();
    }

    @Override
    public String toString() {
        return String.format("%s %4d %4d %6s %10s %10f %10s",
                super.toString(),
                x != null ? x : -1,
                y != null ? y : -1,
                pressure != null ? new DecimalFormat("0.0000").format(pressure) : "",
                code,
                size,
                inputMode);
    }

    @Override
    public String toBriefString() {
        return String.format("%s '%s' x:%d y:%d mode:%s", super.toBriefString(), code, x, y, inputMode);
    }

    @Override
    public void anonymize() {
        super.anonymize();
        code = null;
        x = null;
        y = null;
    }

    public String getCode(){
        return code;
    }
}
