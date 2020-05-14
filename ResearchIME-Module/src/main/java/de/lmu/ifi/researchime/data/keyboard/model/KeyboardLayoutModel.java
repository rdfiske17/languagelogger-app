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

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Locale;

public class KeyboardLayoutModel {

    private String locale;
    private String rows; //the layout is specified as unparsed json string on the server side

    private transient List<KeyboardRowModel> rowsList;

    public List<KeyboardRowModel> getRows(){
        if(rowsList == null && rows != null){
            Type listType = new TypeToken<List<KeyboardRowModel>>(){}.getType();
            rowsList = new Gson().fromJson(rows, listType);
        }
        return rowsList;
    }

    public boolean hasRows(){
        return getRows() != null && !getRows().isEmpty();
    }

    public boolean hasLocale(Locale locale){
        return TextUtils.equals(this.locale, locale.getLanguage());
    }
}
