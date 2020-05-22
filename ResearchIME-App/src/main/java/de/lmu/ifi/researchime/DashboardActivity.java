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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.android.inputmethod.latin.settings.SettingsActivity;
import com.android.inputmethod.latin.setup.SetupWizardActivity;
import com.android.inputmethod.latin.utils.UncachedInputMethodManagerUtils;

import de.lmu.ifi.researchime.config.ConfigurationManager;
import de.lmu.ifi.researchime.data.MessagesInteractorRegistry;
import de.lmu.ifi.researchime.data.base.State;
import de.lmu.ifi.researchime.data.base.StateChangedListener;
import de.lmu.ifi.researchime.data.base.StateInteractor;
import de.lmu.ifi.researchime.data.messages.MessagePoster;
import de.lmu.ifi.researchime.data.messages.model.MessageModel;
import de.lmu.ifi.researchime.data.messages.model.MessagesContainer;
import de.lmu.ifi.researchime.databinding.DashboardRootBinding;
import de.lmu.ifi.researchime.dialog.DialogHelper;
import de.lmu.ifi.researchime.recycler.adapter.BindingRecyclerViewAdapter;
import de.lmu.ifi.researchime.recycler.adapter.ScaleTranslateAnimationAdapter;
import de.lmu.ifi.researchime.recycler.binding.CompositeItemBinder;
import de.lmu.ifi.researchime.recycler.binding.ItemBinderInterface;
import de.lmu.ifi.researchime.recycler.binding.SectionItemDataBinder;
import de.lmu.ifi.researchime.registration.RegistrationActivity;
import de.lmu.ifi.researchime.registration.UserRegistrationHandler;
import de.lmu.ifi.researchime.views.widgets.ExpandablePanel;

public class DashboardActivity extends AppCompatActivity {

    private MessagePoster messagePoster;
    private StateInteractor<MessagesContainer> interactor;
    private DashboardRootBinding binding;
    private ScaleTranslateAnimationAdapter animationAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(! UserRegistrationHandler.isRegistered()){
            navigateToSetup(this, this);
            return;
        }

        interactor = MessagesInteractorRegistry.getMessagesInteractor(this);
        messagePoster = new MessagePoster();
        messagePoster.setListener(messagePostCallback);

        binding = DataBindingUtil.setContentView(this, R.layout.dashboard_root);
        binding.setState(interactor.getState());
        binding.setModel(interactor.getModel());
        reloadData(false);

        binding.errorView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reloadData(true);
            }
        });
        binding.chatExpandablePanel.collapse();
        binding.chatInput.setVisibility(View.INVISIBLE);
        setMessagingInputEnabled(false);
        binding.send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });
        binding.chatExpandablePanel.setOnExpandListener(expandListener);

        loadToolbar();
        loadSwipeRefresh();
        loadRecycler();
    }

    public static void navigateToSetup(Context context, Activity callingActivity){
        InputMethodManager input = (InputMethodManager) context.getSystemService(INPUT_METHOD_SERVICE);
        if(UncachedInputMethodManagerUtils.isThisImeCurrent(context, input)){
            RegistrationActivity.launch(context);
        }else{
            Intent intent = new Intent(context, SetupWizardActivity.class);
            context.startActivity(intent);
        }
        if (callingActivity != null) {
            callingActivity.finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(interactor != null){
            interactor.getState().addListener(dataStateCallback);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(interactor != null){
            interactor.getState().removeListener(dataStateCallback);
        }
        if(messagePoster != null){
            messagePoster.setListener(null);
        }
    }

    private void loadToolbar(){
        setSupportActionBar(binding.toolbar);
    }

    private void loadRecycler(){
        BindingRecyclerViewAdapter<MessageModel> adapter = new BindingRecyclerViewAdapter<>(getBinder(), interactor.getModel().getMessages());
        animationAdapter = new ScaleTranslateAnimationAdapter(adapter);
        binding.recycler.setAdapter(animationAdapter);
    }

    private void loadSwipeRefresh() {
        binding.swipeRefresh.setColorSchemeResources(R.color.colorAccent);
        binding.swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                reloadData(true);
            }
        });
    }

    private void reloadData(boolean forceConfigUpdate){
        interactor.reload();
        if(forceConfigUpdate || DeviceUtils.isWifiConnected(this)){
            ConfigurationManager.forceUpdateConfigurationsFromServer(this);
        }
    }

    private StateChangedListener dataStateCallback = new StateChangedListener() {
        @Override
        public void onStateChanged(@State int newState) {
            if(newState == State.IDLE){
                onDataFetched();
            }
        }
    };

    private void onDataFetched(){
        binding.swipeRefresh.setRefreshing(false);
        if(animationAdapter != null){
            animationAdapter.clearAnimatedPositions();
        }
    }

    private static ItemBinderInterface<MessageModel> getBinder() {
        return new CompositeItemBinder<>(
                new SectionItemDataBinder<MessageModel>(MessageModel.class, BR.message, R.layout.message_cell)
        );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.dashboard, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.send_message:
                toggleMessaging();
                break;
            case R.id.settings:
                showSettings();
                break;
            case R.id.info:
                showInfo();
                break;
        }
        return true;
    }

    private void showSettings(){
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    private void showInfo(){
        DialogHelper.showAboutDialog(this);
    }

    private ExpandablePanel.OnExpandListener expandListener = new ExpandablePanel.OnExpandListener() {
        @Override
        public void onExpand() {
        }

        @Override
        public void onCollapse() {
        }

        @Override
        public void onSlide(float offset) {
            if(offset == 1){
                binding.chatInput.setText("");
            }
        }
    };

    private void toggleMessaging(){
        binding.chatInput.setVisibility(View.VISIBLE);
        binding.chatExpandablePanel.toggle();
        setMessagingInputEnabled(binding.chatExpandablePanel.isExpanded());
    }

    private void setMessagingInputEnabled(boolean isEnabled){
        binding.chatInput.setEnabled(isEnabled);
        hideKeyboard();
    }

    private void sendMessage(){
        String text = binding.chatInput.getText().toString();
        if(TextUtils.isEmpty(text)){
            showInfo(getString(R.string.empty_message));
            return;
        }

        setMessagingInputEnabled(false);
        setSendUIState(true);
        messagePoster.post(MessageModel.createUserMessage(text));
    }

    private MessagePoster.MessagePostCallback messagePostCallback = new MessagePoster.MessagePostCallback() {
        @Override
        public void onSuccess(MessageModel message) {
            interactor.getModel().addMessageAtTop(message);
            binding.recycler.smoothScrollToPosition(0);
            sendSuccess();
        }

        @Override
        public void onError() {
            sendError();
        }
    };

    private void sendSuccess(){
        setMessagingInputEnabled(false);
        setSendUIState(false);
        binding.chatExpandablePanel.post(new Runnable() {
            @Override
            public void run() {
                binding.chatExpandablePanel.collapse();
            }
        });
        showInfo(getString(R.string.send_success));
    }

    private void sendError(){
        setMessagingInputEnabled(true);
        setSendUIState(false);
        showInfo(getString(R.string.send_error));
    }

    private void setSendUIState(boolean isEnabled){
        binding.send.setVisibility(isEnabled ? View.INVISIBLE : View.VISIBLE);
        binding.sendImage.setVisibility(isEnabled ? View.GONE : View.VISIBLE);
        binding.sendProgress.setVisibility(isEnabled ? View.VISIBLE : View.GONE);
    }

    private void hideKeyboard(){
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void showInfo(String text){
        Snackbar snackbar = Snackbar.make(binding.coordinator, text, Snackbar.LENGTH_LONG);
        snackbar.show();
    }
}
