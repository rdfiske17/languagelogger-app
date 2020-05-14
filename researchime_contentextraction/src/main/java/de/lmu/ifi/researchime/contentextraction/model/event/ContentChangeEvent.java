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

package de.lmu.ifi.researchime.contentextraction.model.event;

public class ContentChangeEvent extends Event {

    private String content;
    private Integer contentLength;
    private String inputMode;

    public ContentChangeEvent(String content, EventInputMode inputMode){
        super(Type.CONTENT_CHANGE);
        this.content = content;

        if (content != null){
            contentLength = content.length();
        }

        if(inputMode != null){
            this.inputMode = inputMode.getLogText();
        }
    }

    @Override
    public String toString() {
        return String.format("%s '%s' %s", super.toString(), content, inputMode);
    }

    @Override
    public String toBriefString() {
        return String.format("%s length:%d mode:%s", super.toBriefString(), contentLength, inputMode);
    }

    @Override
    public void anonymize() {
        super.anonymize();
        this.content = null;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getContentLength() {
        return contentLength;
    }

    public void setContentLength(Integer contentLength) {
        this.contentLength = contentLength;
    }

    public String getInputMode() {
        return inputMode;
    }

    public void setInputMode(String inputMode) {
        this.inputMode = inputMode;
    }
}
