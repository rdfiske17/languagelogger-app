package de.lmu.ifi.researchime.contentabstraction.setup;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;

import android.widget.ProgressBar;
import android.widget.TextView;

import java.lang.ref.WeakReference;

import de.ifi.lmu.researchime.contentabstraction.R;
import de.lmu.ifi.researchime.base.logging.LogHelper;
import de.lmu.ifi.researchime.contentabstraction.RIMEInputContentProcessingController;
import de.lmu.ifi.researchime.contentabstraction.model.config.LogicalCategoryList;
import de.lmu.ifi.researchime.contentabstraction.model.config.LogicalWordList;
import de.lmu.ifi.researchime.contentabstraction.model.config.RIMEContentAbstractionConfig;


public class SetupCompletionActivity extends AppCompatActivity {

    private static final String TAG = "SetupCompletionActivity";

    private ProgressBar progressBar;
    private TextView progressText;
    private String serverBaseUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.setup_completion);

        progressBar = findViewById(R.id.progressBar);
        progressText = findViewById(R.id.progressText);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    },
                    123);
        }

        if(getIntent().hasExtra("serverBaseUrl")){
            this.serverBaseUrl = getIntent().getStringExtra("serverBaseUrl");
        }
        else {
            this.serverBaseUrl = getApplicationContext().getString(R.string.research_default_server_address);
        }

        downloadConfigAndWordlists(getApplicationContext());
    }

    private static class ConfigDownloaderAsyncTask extends AsyncTask<Object, Integer, RIMEContentAbstractionConfig> {

        private WeakReference<SetupCompletionActivity> activityReference;

        // only retain a weak reference to the activity
        ConfigDownloaderAsyncTask(SetupCompletionActivity context) {
            activityReference = new WeakReference<>(context);
        }

        private int progressMaxSteps = 1;// there is always at least 1, the config download
        boolean successful = true;

        @Override
        protected RIMEContentAbstractionConfig doInBackground(Object... objects) {

            LogHelper.i(TAG,"asyncTask started");
            SetupCompletionActivity activity = activityReference.get();
            try {
                activity.progressBar.setMax(1);
                activity.progressBar.setProgress(0);
                ConfigDownloader configDownloader = new ConfigDownloader((Context) objects[0], activity.serverBaseUrl);

                // first download config /api/contentabstraction/listconfig
                RIMEContentAbstractionConfig config = configDownloader.downloadConfig();
                // store it
                config.save();

                LogHelper.i(TAG, "configDownloader saved " + config.getLogicalCategoryLists().size() + " list configs");

                // count lists to download and set progressBar max
                int progressMaxSteps =
                        1 // downloading the config
                                + config.getLogicalCategoryLists().size()
                                + config.getLogicalWordLists().size();
                activity.progressBar.setMax(progressMaxSteps);
                activity.progressBar.setProgress(1);
                int progress = 1;
                activity.renderProgressText(1, progressMaxSteps);

                // then download the list files (both categories and whitelisting) /api/contentabstraction/logicalcategorylist/:logicallistId
                for (LogicalCategoryList logicalCategoryList : config.getLogicalCategoryLists()) {
                    configDownloader.downloadListFile(logicalCategoryList);
                    activity.progressBar.incrementProgressBy(1);
                    progress++;
                    activity.renderProgressText(progress, progressMaxSteps);
                }

                for (LogicalWordList logicalWordList : config.getLogicalWordLists()) {
                    configDownloader.downloadListFile(logicalWordList);
                    activity.progressBar.incrementProgressBy(1);
                    progress++;
                    activity.renderProgressText(progress, progressMaxSteps);
                }

                LogHelper.i(TAG,"completed");

                return config;
            } catch(Exception e){
                LogHelper.e(TAG, "downloading wordlist config and listfiles failed",e);
                successful = false;
            }
            return null;
        }

        protected void onProgressUpdate(Integer... progress) {
            SetupCompletionActivity activity = activityReference.get();
            activity.progressBar.setProgress(progress[0]);
            activity.renderProgressText(progress[0], progressMaxSteps);
        }

        protected void onPostExecute(RIMEContentAbstractionConfig result) {
            SetupCompletionActivity activity = activityReference.get();
            if (successful) {
                activity.showRegistrationSuccess();
            }
            else {
                activity.onDownloadError();
            }
        }
    }

    private void downloadConfigAndWordlists(Context context){
        new ConfigDownloaderAsyncTask(this).execute(context);
    }

    private void showRegistrationSuccess(){
        if(isFinishing()){
            return;
        }
        if (getCallingPackage() == null) {
            new AlertDialog.Builder(this)
                    .setMessage(getString(R.string.research_registration_success))
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    }).setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    finish();
                }
            }).show();
        }
        else {
            // if the Setup was launched from a external app (via startActivityForResult), return to the calling activity
            LogHelper.i(TAG,"will return to external calling activity...");
            Intent returnIntent = new Intent();
            returnIntent.putExtra("researchime_username", "-");//TODO UserRegistrationHandler.getUserId());
            setResult(RESULT_OK, returnIntent);
            finish();
        }

    }

    private void onDownloadError(){
        new AlertDialog.Builder(this)
                .setMessage(getString(R.string.research_setup_failed))
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();

                        // delete user from database, so the setup process will start from the beginning on the next app launch
                       // TODO ! - not possible anymore with new modules
                       // User.deleteAll(User.class);
                    }
                })
                .show();
    }

    private void renderProgressText(final int stepsDone, final int stepsTotal){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressText.setText(getString(R.string.setup_completion_progress_text, stepsDone, stepsTotal));
            }
        });
    }

}
