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

import android.databinding.BaseObservable;
import android.databinding.ObservableArrayList;
import android.databinding.ObservableList;

import java.io.Serializable;
import java.util.List;

import de.lmu.ifi.researchime.data.base.BaseModel;

public class MessagesContainer extends BaseObservable implements BaseModel<MessagesContainer>, Serializable {

    private List<MessageModel> messages;
    private StatisticsModel statistics;

    private transient ObservableList<MessageModel> observableMessagesList;

    @Override
    public boolean isEmpty() {
        return statistics == null;
    }

    @Override
    public void set(MessagesContainer container) {
        observableMessagesList.clear();
        observableMessagesList.addAll(container.messages);
        statistics = container.statistics;
        notifyChange();
    }

    public ObservableList<MessageModel> getMessages(){
        if(observableMessagesList == null){
            observableMessagesList = new ObservableArrayList<>();
            if(messages != null){
                observableMessagesList.addAll(messages);
            }
        }
        return observableMessagesList;
    }

    public void addMessageAtTop(MessageModel model){
        getMessages().add(0, model);
    }

    public StatisticsModel getStatistics(){
        return statistics;
    }

    public boolean hasStatistics(){
        return statistics != null;
    }

    public boolean hasMessages(){
        return messages != null;
    }
}
