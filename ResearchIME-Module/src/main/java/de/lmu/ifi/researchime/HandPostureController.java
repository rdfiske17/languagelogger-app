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

package de.lmu.ifi.researchime;

import android.content.Context;
import android.preference.PreferenceManager;
import android.view.View;

import de.lmu.ifi.researchime.data.KeyboardInteractorRegistry;
import de.lmu.ifi.researchime.data.keyboard.model.KeyboardContainer;
import de.lmu.ifi.researchime.module.R;


public class HandPostureController {

    private static final String HAND_POSTURE_DISABLED = "disabled";
    private static final String SUFFIX_OUTDATED = "(outdated)";
    private static final int[] POSTURE_IDS = {
            R.id.research_hand_posture_left_index, R.id.research_hand_posture_two_index, R.id.research_hand_posture_right_index,
            R.id.research_hand_posture_left_thumb, R.id.research_hand_posture_two_thumb, R.id.research_hand_posture_right_thumb
    };

    private View handPostures;
    private String latestHandPosture = "unknown";
    private boolean latestHandPostureIsOutdated = false;
    private static long latestHandPostureConfirmation;
    private int promptTimeout;

    public HandPostureController(final View inputView){
        Context context = inputView.getContext().getApplicationContext();
        int defaultPromptTimeout = context.getResources().getInteger(R.integer.research_config_hand_posture_prompt_timeout_millis_default);
        promptTimeout = PreferenceManager.getDefaultSharedPreferences(context).getInt(
                context.getString(R.string.research_config_hand_posture_prompt_timeout_millis),
                defaultPromptTimeout);
        handPostures = inputView.getRootView().findViewById(R.id.research_hand_postures);
        handPostures.setVisibility(View.GONE);
        for (final int postureId : POSTURE_IDS){
            handPostures.findViewById(postureId).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int i = v.getId();
                    if (i == R.id.research_hand_posture_left_index) {
                        latestHandPosture = "left_index";
                    } else if (i == R.id.research_hand_posture_two_index) {
                        latestHandPosture = "two_index";
                    } else if (i == R.id.research_hand_posture_right_index) {
                        latestHandPosture = "right_index";
                    } else if (i == R.id.research_hand_posture_left_thumb) {
                        latestHandPosture = "left_thumb";
                    } else if (i == R.id.research_hand_posture_two_thumb) {
                        latestHandPosture = "two_thumb";
                    } else if (i == R.id.research_hand_posture_right_thumb) {
                        latestHandPosture = "right_thumb";
                    } else {
                        latestHandPosture = "unknown";
                    }
                    latestHandPostureIsOutdated = false;
                    latestHandPostureConfirmation = System.currentTimeMillis();
                    handPostures.setVisibility(View.GONE);
                }
            });
        }
    }

    public void prompt() {
        KeyboardContainer container = KeyboardInteractorRegistry.getKeyboardInteractor(handPostures.getContext()).getModel();
        if(! container.showHandPosture()){
            latestHandPostureIsOutdated = false;
            latestHandPosture = HAND_POSTURE_DISABLED;
            return;
        }

        //ask the user to confirm the handposture every x milliseconds at max.
        if (System.currentTimeMillis() > latestHandPostureConfirmation + promptTimeout) {
            handPostures.setVisibility(View.VISIBLE);
        }
        //else mark the current value as outdated (could still be true but we don't know)
        else{
            latestHandPostureIsOutdated = true;
        }
    }

    public String getLatestHandPosture(){
        return latestHandPosture + (latestHandPostureIsOutdated ? SUFFIX_OUTDATED : "");
    }
}

