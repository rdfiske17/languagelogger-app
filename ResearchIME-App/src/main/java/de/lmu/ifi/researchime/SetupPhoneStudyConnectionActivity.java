package de.lmu.ifi.researchime;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;

import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;

import com.android.inputmethod.latin.setup.SetupWizardActivity;
import com.android.inputmethod.latin.utils.UncachedInputMethodManagerUtils;

import de.lmu.ifi.researchime.registration.RegistrationActivity;
import de.lmu.ifi.researchime.registration.UserRegistrationHandler;

/**
 * Activity that is called by PhoneStudy, in its setup.
 * This activity checks whether ResearchIME is set up and registered. If not, setup / registration is started
 * If everything is fine, the PhoneStudy setup activity will be called
 */
public class SetupPhoneStudyConnectionActivity extends AppCompatActivity {

    private static final String TAG = "SetupPS.Conn.Activity";

    @Override
    protected void onResume() {
        Log.d(TAG,"onResume()");

        if(! UserRegistrationHandler.isRegistered()) {
            InputMethodManager input = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (UncachedInputMethodManagerUtils.isThisImeCurrent(this, input)) {
                // go to registration
                Log.i(TAG,"user is not registered, but keyboad is set up. Will go to registration");
                RegistrationActivity.launch(this);
            } else {
                // go to full setup
                Log.i(TAG,"keyboard is not set up nor is user registered. Will go to setup");
                Intent intent = new Intent(this, SetupWizardActivity.class);
                this.startActivity(intent);
            }
        }
        else {
            Log.i(TAG,"keyboard is set up and user is registered");
            goBackToPhoneStudySetup(this);
        }
        super.onResume();
    }

    public static void goBackToPhoneStudySetup(Activity activity){
        Log.i(TAG,"going back to PhoneStudy SetupActivity...");
        Intent launchIntent = activity.getPackageManager().getLaunchIntentForPackage("app.me.phonestudy");
        launchIntent.setComponent(new ComponentName(
                "app.me.phonestudy",
                "app.me.phonestudy.app.activities.SetupActivity"));
        launchIntent.putExtra("researchime_username", UserRegistrationHandler.getUserId());
        if (launchIntent != null) {
            activity.startActivity(launchIntent);//null pointer check in case package name was not found
        }
        else {
            Log.e(TAG,"PhoneStudy package not found");
        }
    }
}
