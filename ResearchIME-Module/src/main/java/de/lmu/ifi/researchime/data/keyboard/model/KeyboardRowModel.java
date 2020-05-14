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

package de.lmu.ifi.researchime.data.keyboard.model;

import java.util.List;

public class KeyboardRowModel {

    private List<KeyboardKeyModel> keys;
    private float keyWidth;
    private float keyHeight;

    public List<KeyboardKeyModel> getKeys(){
        return keys;
    }

    public float getKeyWidth() {
        return keyWidth;
    }

    public float getKeyHeight() {
        return keyHeight;
    }

    public boolean hasKeys(){
        return getKeys() != null && !getKeys().isEmpty();
    }
}
