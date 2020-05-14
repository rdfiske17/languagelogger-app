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

public class PrivateModeEvent extends Event{

    public enum Cause {
        /** for enabling and disabling */
        USER,
        /** only for enabling */
        PHONE_NUMBER, EMAIL_ADDRESS, PASSWORD, POSTAL_ADDRESS, PERSON_NAME,
        NUMBER_PASSWORD, VISIBLE_PASSWORD, WEB_EMAIL_ADDRESS, WEB_PASSWORD,
        /** only for disabling */
        AUTOMATIC_RESET,
    }

    private final boolean privateMode;
    private final Cause cause;

    public PrivateModeEvent(boolean privateMode, Cause cause){
        super(Event.Type.PRIVATE_MODE);
        this.privateMode = privateMode;
        this.cause = cause;
    }

    @Override
    public String toString() {
        return String.format("%s Enabled: %b, Cause: %s", super.toString(), privateMode, cause.toString());
    }

    @Override
    public String toBriefString() {
        return String.format("%s %b", super.toBriefString(), privateMode);
    }
}
