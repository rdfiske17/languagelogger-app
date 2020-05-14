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

import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;

public abstract class ValueTask<T> extends Task<T> {

    @Override
    public final T call() {
        Handler handler = new Handler(Looper.getMainLooper());

        try {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    onExecuteMainThread();
                }
            });

            final T data = onExecuteBackground();

            if (!Thread.currentThread().isInterrupted()) {
                if (data == null) {
                    throw new Exception("data missing");
                } else {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            onComplete(data);
                        }
                    });
                }
                return data;
            }
        } catch (final Exception e) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    onError(e);
                }
            });
        }

        return null;
    }

    public abstract T onExecuteBackground() throws Exception;

    public abstract void onExecuteMainThread();

    public abstract void onComplete(T data);
}
