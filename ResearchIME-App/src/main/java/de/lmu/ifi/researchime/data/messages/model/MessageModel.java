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

package de.lmu.ifi.researchime.data.messages.model;

import android.content.Context;
import android.text.TextUtils;

import de.lmu.ifi.researchime.data.utils.DateConverter;
import de.lmu.ifi.researchime.registration.UserRegistrationHandler;

public class MessageModel  {

    private String title;
    private String message;
    private long timestamp;
    private String userId;

    private boolean isUserMessage;
    private boolean isNewMessage;

    public static MessageModel createUserMessage(String message){
        MessageModel model = new MessageModel();
        model.message = message;
        model.timestamp = System.currentTimeMillis();
        model.isUserMessage = true;
        model.userId = UserRegistrationHandler.getUserId();
        return model;
    }

    public String getTitle(){
        return title;
    }

    public String getMessage(){
        return message;
    }

    public String getDate(Context context){
        return DateConverter.getShortDate(timestamp, context);
    }

    public boolean hasTitle(){
        return ! TextUtils.isEmpty(getTitle());
    }

    public boolean isUserMessage(){
        return isUserMessage;
    }

    public void setNewMessage(){
        isNewMessage = true;
    }

    public boolean isNewMessage(){
        return isNewMessage;
    }
}
