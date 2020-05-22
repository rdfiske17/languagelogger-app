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

package de.lmu.ifi.researchime.registration;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import de.lmu.ifi.researchime.base.RestClient;
import de.lmu.ifi.researchime.config.ConfigurationManager;
import de.lmu.ifi.researchime.contentabstraction.setup.SetupCompletionActivity;
import de.lmu.ifi.researchime.dialog.DialogHelper;
import de.lmu.ifi.researchime.model.User;
import de.lmu.ifi.researchime.module.R;
import de.lmu.ifi.researchime.module.databinding.RegistrationRootBinding;
import retrofit.ResponseCallback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class RegistrationActivity extends AppCompatActivity {

    private String userUuid;
    private ProgressDialog progressDialog;
    private RegistrationRootBinding binding;

    public static void launch(Context context){
        Intent intent = new Intent(context, RegistrationActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.registration_root);
        initView();
        ConfigurationManager.forceUpdateConfigurationsFromServer(this);
    }

    private void initView(){
        if (userUuid == null) {
            userUuid = java.util.UUID.randomUUID().toString();
        }
        binding.userId.setText(userUuid);
        binding.ageNumberPicker.setMaxValue(getResources().getInteger(R.integer.research_max_age));
        binding.ageNumberPicker.setMinValue(getResources().getInteger(R.integer.research_min_age));
        binding.ageNumberPicker.setWrapSelectorWheel(false);
        binding.buttonDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDoneButtonClicked();
            }
        });
        binding.toolbar.inflateMenu(R.menu.registration_menu);
        binding.toolbar.setOnMenuItemClickListener(menuItemClickListener);
    }

    private Toolbar.OnMenuItemClickListener menuItemClickListener = new Toolbar.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            int itemId = item.getItemId();
            if (itemId == R.id.settings) {
                showSettings();
            } else if (itemId == R.id.info) {
                showInfo();
            }
            return true;
        }
    };

    private void showSettings(){
        try {
            Intent intent = new Intent(this,  Class.forName("com.android.inputmethod.latin.settings.SettingsActivity"));
            startActivity(intent);
        } catch (ClassNotFoundException e) {
            Toast.makeText(this, "Settings not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void showInfo(){
        DialogHelper.showAboutDialog(this);
    }

    private void onDoneButtonClicked(){
        String gender;
        int id = binding.genderGroup.getCheckedRadioButtonId();
        if (id == R.id.female) {
            gender = "female";
        } else if (id == R.id.male) {
            gender = "male";
        } else {
            Toast.makeText(this, getString(R.string.research_no_gender_error), Toast.LENGTH_LONG).show();
            return;
        }

        int age = binding.ageNumberPicker.getValue();
        User user = UserRegistrationHandler.createUser(RegistrationActivity.this, userUuid, gender, age);
        postToServer(user);
    }

    private void postToServer(final User user) {
        showProgressDialog();
        RestClient.get(this).postUser(user.getJson(), new ResponseCallback() {
            @Override
            public void success(Response response) {
                onUserPostSuccess(user);
            }
            @Override
            public void failure(RetrofitError error) {
                onUserPostError(error);
            }
        });
    }

    private void onUserPostSuccess(User user){
        user.save();
        //save user id in preferences to show it on the preference screen
        PreferenceManager.getDefaultSharedPreferences(this).edit().putString(getString(R.string.research_preference_key_user_id), user.getUuid()).apply();
        ConfigurationManager.forceUpdateConfigurationsFromServer(this);

        goToSetupCompletionActivity();
    }

    private void goToSetupCompletionActivity(){
        Intent intent = new Intent(this, SetupCompletionActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }



    private void onUserPostError(RetrofitError error){
        if(isFinishing()){
            return;
        }
        dismissProgressDialog();
        new AlertDialog.Builder(this)
                .setMessage("Registration error:\n" + RestClient.getErrorDescription(error))
                .setPositiveButton("OK", null)
                .create().show();
    }

    private void showProgressDialog(){
        dismissProgressDialog();
        progressDialog = new ProgressDialog(this);
        progressDialog.show();
    }

    private void dismissProgressDialog(){
        if(progressDialog != null && progressDialog.isShowing()){
            progressDialog.dismiss();
        }
    }
}
