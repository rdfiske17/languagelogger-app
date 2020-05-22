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

import android.content.Context;
import android.support.annotation.NonNull;

import android.view.View;
import android.view.inputmethod.EditorInfo;

import de.lmu.ifi.researchime.config.ConfigurationManager;
import de.lmu.ifi.researchime.contentextraction.logging.LogHelper;
import de.lmu.ifi.researchime.contentextraction.model.event.EventInputMode;
import de.lmu.ifi.researchime.data.KeyboardInteractorRegistry;
import de.lmu.ifi.researchime.data.keyboard.model.KeyboardContainer;
import de.lmu.ifi.researchime.model.KeyboardState;
import de.lmu.ifi.researchime.model.User;
import de.lmu.ifi.researchime.contentextraction.model.event.AutoCorrectEvent;
import de.lmu.ifi.researchime.contentextraction.model.event.ContentChangeEvent;
import de.lmu.ifi.researchime.contentextraction.model.event.Event;
import de.lmu.ifi.researchime.contentextraction.model.event.PrivateModeEvent;
import de.lmu.ifi.researchime.contentextraction.model.event.SuggestionPickedEvent;
import de.lmu.ifi.researchime.contentextraction.model.event.TouchEvent;
import de.lmu.ifi.researchime.registration.UserRegistrationHandler;

public class EventHandler implements IEventHandler, PrivateModeController.PrivateModeListener {

    private static final String TAG = "EventHandler";

    //TODO when the user changes the language, keyboardstate isn't immediately updated unitl setinputview happens

    private boolean isInitialized = false;

    private EventBuffer buffer;

    private PersistentStorageAndTransmissionController storageAndTransmissionController;
    private ContentAbstractionCaller contentAbstractionCaller;
    private SensorController sensorController;
    private HandPostureController handPostureController;
    private PrivateModeController privateModeController;

    private User user;
    private KeyboardStateController keyboardStateController;
    private KeyboardState keyboardState;

    private EditorInfo editorInfo;
    private EventInputMode lastEventInputMode;

    private KeyboardContainer keyboardContainer;

    /** --- TRACKING --- **/
    private boolean isTrackingEnabled(){
        return keyboardContainer == null || keyboardContainer.isTrackingEnabled();
    }

    private boolean isShowHandPosture(){
        return keyboardContainer == null ? false : keyboardContainer.showHandPosture();
    }

    private boolean isAnonymizeInputEvents(){
        return keyboardContainer == null ? false : keyboardContainer.isAnonymizeInputEvents();
    }

    /** ----------------- EVENTS ----------------- **/

    @Override
    public void onSetInputView(@NonNull View view, @NonNull Context context, @NonNull String locale) {
        LogHelper.i(TAG,"onSetInputView");
        initialize(context);
        handPostureController = new HandPostureController(view);
    }

    private void handleKeyboardState(Context context, View view, String locale){
        KeyboardState keyboardState = KeyboardStateController.getCurrentState(context, view, locale);
        //if nothing has changed we don't need to do anything
        if (this.keyboardState == null || !this.keyboardState.equals(keyboardState)){
            this.keyboardState = keyboardState;
            if(keyboardStateController == null){
                keyboardStateController = new KeyboardStateController();
            }
            keyboardStateController.onStateChanged(keyboardState, context);
        }
    }

    @Override
    public void onStartInput(@NonNull EditorInfo editorInfo, @NonNull Context context) {
        LogHelper.i(TAG,"onStartInput");
        initialize(context);
        this.editorInfo = editorInfo;
        ConfigurationManager.updateConfigurationsFromServer(context);
        if (privateModeController != null) {
            privateModeController.setVisible(isTrackingEnabled());
            privateModeController.onStartInput(editorInfo);
        }
    }

    @Override
    public void onStartInputView(@NonNull View view, @NonNull Context context, @NonNull String locale) {
        LogHelper.i(TAG,"onStartInputView");
        initialize(context);
        handleKeyboardState(context, view, locale);
        if (isInitialized && isTrackingEnabled()){
            sensorController.startRecording();
            if (isShowHandPosture()) {
                handPostureController.prompt();
            }
        }
    }

    @Override
    public void onFinishInputView() {
        LogHelper.i(TAG,"onFinishInputView()");
        if (isInitialized){
            sensorController.stopRecording();
        }
    }

    private void initialize(Context context){
        if (!isInitialized){
            LogHelper.i(TAG, "Initializing handler");
            if(keyboardContainer == null) keyboardContainer = KeyboardInteractorRegistry.getKeyboardInteractor(context).getModel();
            if (storageAndTransmissionController == null) storageAndTransmissionController = new PersistentStorageAndTransmissionController(context);
            if (contentAbstractionCaller == null) contentAbstractionCaller = new ContentAbstractionCaller(context);
            if (privateModeController == null) privateModeController = new PrivateModeController(this);
            if (buffer == null) buffer = new EventBuffer();
            if (sensorController == null) sensorController = new SensorController(context);
            if (user == null) user = UserRegistrationHandler.getUserOrLaunchRegistration(context);
            if (storageAndTransmissionController != null && privateModeController != null && buffer != null && user != null &&
                    keyboardState != null && sensorController != null && handPostureController != null && contentAbstractionCaller != null){
                LogHelper.i(TAG, "Initialization successful");
                isInitialized = true;
            }
            else {
                LogHelper.w(TAG, String.format("Couldn't initialize because %s was null.",
                        storageAndTransmissionController == null? "storageAndTransmissionController" : privateModeController == null? "privateModeController" : buffer == null? "buffer" :
                                user == null? "user" : sensorController == null? "sensorManager" : keyboardState == null? "keyboardState" :
                                        contentAbstractionCaller == null? "contentAbstractionCaller" : "?"));
            }

            Thread.setDefaultUncaughtExceptionHandler(new AppExceptionHandler());
        }
    }

    @Override
    public void onFinishInput(@NonNull Context context) {
        LogHelper.i(TAG,"onFinishInput()");
        if (isInitialized) {
            storageAndTransmissionController.storeEvents(context, buffer);
            contentAbstractionCaller.extractWordEventsAndCallContentAbstractionModule(context, user.getUuid(), buffer.getAll());

            // to avoid concurrency problems
            buffer = new EventBuffer();

            sensorController.stopRecording();
        }
    }

    @Override
    public void onTouchDownEvent(long timestamp, int x, int y, float pressure, float size, String code, EventInputMode inputMode) {
        LogHelper.i(TAG,"onTouchDownEvent()");
        this.lastEventInputMode = inputMode;
        processNewEvent(new TouchEvent(Event.Type.TOUCH_DOWN, code, x, y, pressure, size, inputMode));
    }

    @Override
    public void onTouchMoveEvent(long timestamp, int x, int y, float pressure, float size, String code, EventInputMode inputMode) {
        LogHelper.i(TAG,"onTouchMoveEvent()");
        this.lastEventInputMode = inputMode;
        processNewEvent(new TouchEvent(Event.Type.TOUCH_MOVE, code, x, y, pressure, size, inputMode));
    }

    @Override
    public void onTouchUpEvent(long timestamp, int x, int y, float pressure, float size, String code, EventInputMode inputMode) {
        LogHelper.i(TAG,"onTouchUpEvent()");
        this.lastEventInputMode = inputMode;
        processNewEvent(new TouchEvent(Event.Type.TOUCH_UP, code, x, y, pressure, size, inputMode));
    }

    @Override
    public void onAutoCompleteSuggestionPicked(String suggestion) {
        LogHelper.i(TAG,"onAutoCompleteSuggestionPicked()");
        processNewEvent(new SuggestionPickedEvent(suggestion));
    }

    @Override
    public void onInputContentChanged(long timestamp, String content) {
        LogHelper.i(TAG,"onInputContentChanged()");
        processNewEvent(new ContentChangeEvent(content, lastEventInputMode));
    }

    @Override
    public void onAutoCorrectionApplied(CharSequence oldText, CharSequence newText, int offset) {
        LogHelper.i(TAG,"onAutoCorrectionApplied()");
        processNewEvent(new AutoCorrectEvent(oldText.toString(), newText.toString(), offset));
    }

    @Override
    public void onPrivateModeStatusChange(boolean enabled, PrivateModeEvent.Cause cause) {
        processNewEvent(new PrivateModeEvent(enabled, cause));
    }

    public void initPrivateModeManager(PrivateModeButton privateModeButton) {
        if (privateModeController == null) privateModeController = new PrivateModeController(this);
        privateModeController.init(privateModeButton);
        privateModeController.setVisible(isTrackingEnabled());
    }

    private void processNewEvent(Event event){
        LogHelper.i(TAG,"processNewEvent(): "+event.getType());
        //not initialized yet, ignore event
        if (!isInitialized){
            return;
        }

        if(! isTrackingEnabled()){
            return;
        }

        //private mode is on, ignore event
        if (event.getType() != Event.Type.PRIVATE_MODE && privateModeController.isEnabled()){
            return;
        }

        event.setSensors(sensorController.getLatestSensorValues());
        event.setHandPosture(handPostureController.getLatestHandPosture());
        event.setUserUuid(user.getUuid());
        event.setKeyboardStateUuid(keyboardState.getUuid());
        event.setFieldPackageName(editorInfo.packageName);
        event.setFieldId(editorInfo.fieldId);
        event.setFieldHintText(editorInfo.hintText);

        buffer.add(event);
    }
}
