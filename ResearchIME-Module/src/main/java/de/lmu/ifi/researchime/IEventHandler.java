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

import de.lmu.ifi.researchime.contentextraction.model.event.EventInputMode;

public interface IEventHandler {

    void onStartInput(@NonNull EditorInfo editorInfo, @NonNull Context context);

    void onStartInputView(@NonNull View view, @NonNull Context context, @NonNull String locale);

    void onFinishInputView();

    void onSetInputView(@NonNull View view, @NonNull Context context, @NonNull String locale);

    void onFinishInput(@NonNull Context context);

    void onTouchDownEvent(long timestamp, int x, int y, float pressure, float size, String code, EventInputMode inputMode);

    void onTouchMoveEvent(long timestamp, int x, int y, float pressure, float size, String code, EventInputMode inputMode);

    void onTouchUpEvent(long timestamp, int x, int y, float pressure, float size, String code, EventInputMode inputMode);

    void onAutoCompleteSuggestionPicked(String suggestion);

    /**
     * Creates an Event which contains a 'content' string.
     * Be sure that you don't leak text which was entered during private mode (e.g. by delaying the deactivation of the private mode)
     *
     * @param timestamp Timestamp
     * @param content Content of the input field
     */
    void onInputContentChanged(long timestamp, String content);

    void onAutoCorrectionApplied(CharSequence oldText, CharSequence newText, int offset);

    //TODO remove from this interface
    void initPrivateModeManager(PrivateModeButton privateModeButton);
}
