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
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.InputConnection;
import android.widget.ImageButton;

import de.lmu.ifi.researchime.contentextraction.logging.LogHelper;
import de.lmu.ifi.researchime.contentextraction.model.event.EventInputMode;
import de.lmu.ifi.researchime.data.KeyboardInteractorRegistry;

public class LatinIMEEventController {

    private static final String TAG = "LatinIMEEventController";
    private IEventHandler eventHandler = new EventHandler();
    private InputConnection inputConnection;
    private String textFieldContent = "";

    private static LatinIMEEventController instance;

    private LatinIMEEventController(){}

    public static LatinIMEEventController get(){
        if (instance == null){
            instance = new LatinIMEEventController();
        }
        return instance;
    }

    public void onStartInput(@Nullable EditorInfo editorInfo, @Nullable InputConnection inputConnection, @Nullable Context context) {
        if (inputConnection != null) {
            this.inputConnection = inputConnection;
        }
        //requirement for 'real' input start? otherwise also for finish input...
        if (editorInfo != null && context != null && editorInfo.initialSelStart >= 0 && editorInfo.initialSelEnd >= 0) {
            LogHelper.i(TAG, "Start input. Type = " + editorInfo.inputType + " Cursor position = " + editorInfo.initialSelStart + "," + editorInfo.initialSelEnd);

            eventHandler.onStartInput(editorInfo, context);

            checkForContentChange();
        }
    }

    public void onFinishInput(@Nullable Context context){
        if (context != null){
            LogHelper.i(TAG, "Finish input.");
            eventHandler.onFinishInput(context);
        }
    }

    public void onStartInputView(View view, Context context, String locale){
        eventHandler.onStartInputView(view, context, locale);
    }

    public void onFinishInputView(){
        eventHandler.onFinishInputView();
    }

    public void onTouchDownEvent(long timestamp, int x, int y, float pressure, float size, String code, boolean isMoreKeysKeyboard, EventInputMode inputMode){
        if (isMoreKeysKeyboard){
            //don't log MoreKeysKeyboard events
            //eventHandler.onTouchDownEvent(timestamp, code);
        }
        else{
            eventHandler.onTouchDownEvent(timestamp, x, y, pressure, size, code, inputMode);
        }
    }

    public void onTouchMoveEvent(long timestamp, int x, int y, float pressure, float size, String code, boolean isMoreKeysKeyboard, EventInputMode inputMode) {
        if (isMoreKeysKeyboard){
            //don't log MoreKeysKeyboard events
        }
        else{
            eventHandler.onTouchMoveEvent(timestamp, x, y, pressure, size, code, inputMode);
        }
    }

    public void onTouchUpEvent(long timestamp, int x, int y, float pressure, float size, String code, boolean isMoreKeysKeyboard, EventInputMode inputMode) {
        if (isMoreKeysKeyboard){
            //don't log MoreKeysKeyboard events
            //eventHandler.onTouchUpEvent(timestamp, code);
        }
        else{
            eventHandler.onTouchUpEvent(timestamp, x, y, pressure, size, code, inputMode);
        }
    }

    public void onAutoCompleteSuggestionPicked(String suggestion){
        eventHandler.onAutoCompleteSuggestionPicked(suggestion);
    }

    public void onAutoCorrectionApplied(CharSequence oldText, CharSequence newText, int offset) {
        eventHandler.onAutoCorrectionApplied(oldText, newText, offset);
    }

    public void checkForContentChange(){
        String content = getTextFromInputConnection(inputConnection);
        if (content == null) return;

        if (!content.equals(this.textFieldContent)) {
            this.textFieldContent = content;
            eventHandler.onInputContentChanged(SystemClock.uptimeMillis(), content);
        }
    }

    public boolean shouldShowSuggestionStrip(Context context) {
        //currently always true because we want to see/control private mode
        //always show suggestion strip if tracking is enabled
        return KeyboardInteractorRegistry.getKeyboardInteractor(context).getModel().isTrackingEnabled();
    }

    /**
     * waits until the view is inflated (to ensure that dimensions are available)
     * and then forwards it
     *  @param view
     * @param applicationContext
     * @param locale
     */
    public void onSetInputView(@NonNull final View view, @Nullable final Context applicationContext, @NonNull final String locale) {
        ViewTreeObserver viewTreeObserver = view.getViewTreeObserver();
        if (applicationContext != null && viewTreeObserver.isAlive()) {
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                        view.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    } else {
                        view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                    eventHandler.onSetInputView(view, applicationContext, locale);
                }
            });
        }
    }

    public void setPrivateModeButton(@Nullable ImageButton privateModeButton, Drawable activeDrawable, Drawable inactiveDrawable) {
        if (privateModeButton != null) {
            eventHandler.initPrivateModeManager(new PrivateModeButton(privateModeButton, activeDrawable, inactiveDrawable));
        }
    }

    private static String getTextFromInputConnection(InputConnection inputConnection){
        if (inputConnection == null) {
            return null;
        }
        ExtractedText extractedText = inputConnection.getExtractedText(new ExtractedTextRequest(), 0);
        if (extractedText == null || extractedText.text == null) {
            return null;
        }
        return extractedText.text.toString();
    }
}