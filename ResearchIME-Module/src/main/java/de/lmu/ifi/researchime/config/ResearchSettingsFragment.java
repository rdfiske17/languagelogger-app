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

package de.lmu.ifi.researchime.config;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import de.lmu.ifi.researchime.module.R;

public final class ResearchSettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.research_prefs_screen);

        Preference userIdPreference = findPreference(getString(R.string.research_preference_key_user_id));
        String userID = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(
                getString(R.string.research_preference_key_user_id),
                getString(R.string.research_uuid_placeholder));
        if (userIdPreference != null && userID != null) {
            userIdPreference.setSummary(userID);
        }

        Preference emailPreference = findPreference(getString(R.string.research_preference_key_email));
        if (emailPreference != null){
            emailPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    generateSendEmailIntent();
                    return true;
                }
            });
        }
    }

    private void generateSendEmailIntent() {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", getString(R.string.research_feedback_mail), null));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.research_feedback_mail_subject));
        startActivity(Intent.createChooser(emailIntent, getString(R.string.research_mail_prompt)));
    }
}
