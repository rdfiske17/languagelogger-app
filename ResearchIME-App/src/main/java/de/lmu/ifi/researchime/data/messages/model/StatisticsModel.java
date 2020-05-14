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

import android.text.TextUtils;

import org.apache.commons.lang3.StringUtils;

import de.lmu.ifi.researchime.data.utils.DateConverter;

public class StatisticsModel  {

    private long userEventsCount;
    private long totalEventsCount;
    private long studyEndDate;
    private String currentLayoutName;
    private String studyName;

    private transient String studyEndText;

    public long getUserEventsCount(){
        return userEventsCount;
    }

    public long getTotalEventsCount(){
        return totalEventsCount;
    }

    public String getStudyEnd(){
        if(studyEndText == null){
            studyEndText = DateConverter.getDayString(studyEndDate);
        }
        return studyEndText;
    }

    public String getStudyName(){
        return studyName;
    }

    public boolean hasStudyName(){
        return !TextUtils.isEmpty(studyName);
    }

    public String getCurrentLayoutName(){
        return currentLayoutName;
    }

    public boolean hasCurrentLayoutName(){
        return !TextUtils.isEmpty(currentLayoutName);
    }

    public boolean hasStudyEndDate(){
        return studyEndDate > 0;
    }

    public boolean hasMetaInfo(){
        return userEventsCount > 0 || totalEventsCount > 0 || studyEndDate > 0 ||
                StringUtils.isNoneBlank(studyName) ||StringUtils.isNoneBlank(currentLayoutName);
    }
}
