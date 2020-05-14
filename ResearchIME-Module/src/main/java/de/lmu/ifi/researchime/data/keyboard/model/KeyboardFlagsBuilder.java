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

public class KeyboardFlagsBuilder {

    public static int getKeyLabelFlags(String[] flags){
        int keyLabel = 0;
        if(flags != null){
            for(String flag : flags){
                keyLabel |= getKeyLabelFlags(flag);
            }
        }
        return keyLabel;
    }

    private static int getKeyLabelFlags(String flag){
        if(flag == null){
            return 0;
        }
        switch(flag){
            case "alignHintLabelToBottom": return 0x02;
            case "alignIconToBottom": return 0x04;
            case "alignLabelOffCenter": return 0x08;
            case "fontNormal": return 0x10;
            case "fontMonoSpace": return 0x20;
            case "fontDefault": return 0x30;
            case "followKeyLargeLetterRatio": return 0x40;
            case "followKeyLetterRatio": return 0x80;
            case "followKeyLabelRatio": return 0xC0;
            case "followKeyHintLabelRatio": return 0x140;
            case "hasPopupHint": return 0x200;
            case "hasShiftedLetterHint": return 0x400;
            case "hasHintLabel": return 0x800;
            case "autoXScale": return 0xc000;
            case "autoScale": return 0x800;
            case "preserveCase": return 0x10000;
            case "shiftedLetterActivated": return 0x20000;
            case "fromCustomActionLabel": return 0x40000;
            case "followFunctionalTextColor": return 0x80000;
            case "keepBackgroundAspectRatio": return 0x100000;
            case "disableKeyHintLabel": return 0x40000000;
            case "disableAdditionalMoreKeys": return 0x80000000;
            default: return 0;
        }
    }

    public static int getKeyActionFlags(String[] flags){
        int keyAction = 0;
        if(flags != null){
            for(String flag : flags){
                keyAction |= getKeyActionFlags(flag);
            }
        }
        return keyAction;
    }

    private static int getKeyActionFlags(String flag){
        if(flag == null){
            return 0;
        }
        switch(flag){
            case "isRepeatable": return 0x01;
            case "noKeyPreview": return 0x02;
            case "altCodeWhileTyping": return 0x04;
            case "enableLongPress": return 0x08;
            default: return 0;
        }
    }

    public static int getImeAction(String actionName){
        if(actionName == null){
            return 0;
        }
        switch(actionName){
            case "actionUnspecified": return 0;
            case "actionNone": return 1;
            case "actionGo": return 2;
            case "actionSearch": return 3;
            case "actionSend": return 4;
            case "actionNext": return 5;
            case "actionDone": return 6;
            case "actionPrevious": return 7;
            case "actionCustomLabel": return 0x100;
            default: return 0;
        }
    }
}
