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

package de.lmu.ifi.researchime.data.base;

import android.databinding.BaseObservable;
import android.databinding.ObservableInt;

import java.util.HashSet;
import java.util.Set;

public class StateHolder extends BaseObservable {

    private Set<StateChangedListener> listeners;

    private ObservableInt state;

    public StateHolder() {
        this.state = new ObservableInt(State.IDLE);
    }

    @Override
    public synchronized void notifyChange() {
        super.notifyChange();
        notifyListeners();
    }

    public void setFailure(Exception e) {
        setState(State.FAILURE);
    }

    @State
    public int getState() {
        //noinspection WrongConstant
        return state.get();
    }

    public void setState(@State int state) {
        this.state.set(state);
        notifyChange();
    }

    private void notifyListeners() {
        if (listeners != null) {
            for (StateChangedListener listener : listeners) {
                listener.onStateChanged(getState());
            }
        }
    }

    public void addListener(StateChangedListener listener) {
        if (listeners == null) {
            listeners = new HashSet<>();
        }
        listeners.add(listener);
    }

    public void removeListener(StateChangedListener listener) {
        if (listeners != null) {
            listeners.remove(listener);
        }
    }
}
