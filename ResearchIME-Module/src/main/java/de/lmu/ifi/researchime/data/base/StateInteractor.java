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

import android.support.annotation.NonNull;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class StateInteractor<T extends BaseModel<T>> extends Interactor{

    private final RestGateway<T> gateway;
    private final StateHolder stateHolder;
    private final T model;

    private Future future;

    public StateInteractor(@NonNull ExecutorService service, RestGateway<T> gateway){
        super(service);
        this.gateway = gateway;
        this.stateHolder = new StateHolder();
        this.model = gateway.create();
    }

    public StateHolder getState(){
        return stateHolder;
    }

    public T getModel() {
        if(model.isEmpty()){
            reload();
        }
        return model;
    }

    public void reload() {
        if (future == null || future.isDone()) {
            future = execute(new FetchTask());
        }
    }

    protected void onFetched(T data){
        model.set(data);
        stateHolder.setState(model.isEmpty() ? State.FAILURE : State.IDLE);
    }

    protected void onFetchError(Exception e){
        e.printStackTrace();
        stateHolder.setFailure(e);
    }

    /* Tasks */
    private class FetchTask extends ValueTask<T> {

        @Override
        public T onExecuteBackground() throws Exception {
            return gateway.fetch();
        }

        @Override
        public void onExecuteMainThread() {
            getState().setState(State.WORKING);
        }

        @Override
        public void onComplete(T data) {
            onFetched(data);
        }

        @Override
        public void onError(Exception e) {
            onFetchError(e);
        }
    }
}
