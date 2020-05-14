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

import android.text.InputType;
import android.view.View;
import android.view.inputmethod.EditorInfo;

import de.lmu.ifi.researchime.contentextraction.model.event.PrivateModeEvent;

public class PrivateModeController {

    private boolean privateModeEnabled;
    private boolean privateModeDisablingDelayed;
    private boolean privateModeAutomaticallyEnabled;

    private PrivateModeButton privateModeButton;
    private int currentInputType;
    private PrivateModeListener listener;

    public PrivateModeController(PrivateModeListener listener){
        this.listener = listener;
    }

    private void togglePrivateMode(PrivateModeEvent.Cause cause) {
        setPrivateModeEnabled(!privateModeEnabled, cause);
    }

    private void setPrivateModeEnabled(boolean enabled, PrivateModeEvent.Cause cause) {
        if (!enabled && cause.equals(PrivateModeEvent.Cause.USER) && PrivateModeController.shouldEnforcePrivateMode(currentInputType)){
            /** the user is not allowed to turn off the private mode if the current input is password etc.  */
            return;
        }
        if (enabled == this.privateModeEnabled){
            /** no change so we do not generate an event */
            return;
        }

        this.privateModeEnabled = enabled;

        listener.onPrivateModeStatusChange(enabled, cause);

        if (privateModeButton != null) {
            privateModeButton.setActive(enabled);
        }

        if (enabled && cause.equals(PrivateModeEvent.Cause.USER)){
            //to avoid that private information is leaked through the content change event, delay the disabling until a new input is started
            privateModeDisablingDelayed = true;
        }

        if (cause != PrivateModeEvent.Cause.USER){
            privateModeAutomaticallyEnabled = true;
        }
    }

    static PrivateModeEvent.Cause editorInfoToPrivateModeCause(int inputType){
        PrivateModeEvent.Cause possiblePrivateModeEventCause = null;

        if ((inputType & InputType.TYPE_MASK_CLASS) == InputType.TYPE_CLASS_PHONE) {
            possiblePrivateModeEventCause = PrivateModeEvent.Cause.PHONE_NUMBER;
        } else if ((inputType & InputType.TYPE_MASK_CLASS) == InputType.TYPE_CLASS_NUMBER
                && (inputType & InputType.TYPE_MASK_VARIATION) == InputType.TYPE_NUMBER_VARIATION_PASSWORD) {
            possiblePrivateModeEventCause = PrivateModeEvent.Cause.NUMBER_PASSWORD;
        } else if ((inputType & InputType.TYPE_MASK_CLASS) == InputType.TYPE_CLASS_TEXT) {
            switch (inputType & InputType.TYPE_MASK_VARIATION) {
                case InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS:
                    possiblePrivateModeEventCause = PrivateModeEvent.Cause.EMAIL_ADDRESS;
                    break;
                case InputType.TYPE_TEXT_VARIATION_PASSWORD:
                    possiblePrivateModeEventCause = PrivateModeEvent.Cause.PASSWORD;
                    break;
                case InputType.TYPE_TEXT_VARIATION_PERSON_NAME:
                    possiblePrivateModeEventCause = PrivateModeEvent.Cause.PERSON_NAME;
                    break;
                case InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS:
                    possiblePrivateModeEventCause = PrivateModeEvent.Cause.POSTAL_ADDRESS;
                    break;
                case InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD:
                    possiblePrivateModeEventCause = PrivateModeEvent.Cause.VISIBLE_PASSWORD;
                    break;
                case InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS:
                    possiblePrivateModeEventCause = PrivateModeEvent.Cause.WEB_EMAIL_ADDRESS;
                    break;
                case InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD:
                    possiblePrivateModeEventCause = PrivateModeEvent.Cause.WEB_PASSWORD;
                    break;
            }
        }
        return possiblePrivateModeEventCause;
    }

    static boolean shouldEnforcePrivateMode(int inputType){
        return editorInfoToPrivateModeCause(inputType) != null;
    }

    public void onStartInput(EditorInfo editorInfo) {
        this.currentInputType = editorInfo.inputType;

        //reset the state if it was enabled automatically (e.g. password)
        if (privateModeAutomaticallyEnabled){
            setPrivateModeEnabled(false, PrivateModeEvent.Cause.AUTOMATIC_RESET);
            privateModeAutomaticallyEnabled = false;
        }

        //enable private mode automatically (e.g. password)
        if (!privateModeEnabled) {
            PrivateModeEvent.Cause possiblePrivateModeEventCause = PrivateModeController.editorInfoToPrivateModeCause(currentInputType);
            if (possiblePrivateModeEventCause != null) {
                setPrivateModeEnabled(true, possiblePrivateModeEventCause);
            }
        }

        //reset delay
        privateModeDisablingDelayed = false;
    }

    public boolean isEnabled(){
        return privateModeEnabled || privateModeDisablingDelayed;
    }

    public void init(PrivateModeButton privateModeButton) {
        if (privateModeButton != null) {
            //init the button
            privateModeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    togglePrivateMode(PrivateModeEvent.Cause.USER);
                }
            });
            //set the button to the current state
            privateModeButton.setActive(privateModeEnabled);

            this.privateModeButton = privateModeButton;
        }
    }

    public void setVisible(boolean isVisible) {
        if(privateModeButton != null){
            privateModeButton.setVisible(isVisible);
        }
    }

    public interface PrivateModeListener {
        void onPrivateModeStatusChange(boolean enabled, PrivateModeEvent.Cause cause);
    }
}
