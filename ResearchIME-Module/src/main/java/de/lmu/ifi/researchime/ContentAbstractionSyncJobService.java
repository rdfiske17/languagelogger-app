package de.lmu.ifi.researchime;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.raizlabs.android.dbflow.sql.language.SQLite;

import java.util.List;

import de.lmu.ifi.researchime.base.RestClient;
import de.lmu.ifi.researchime.contentabstraction.AppExceptionHandler;
import de.lmu.ifi.researchime.contentextraction.logging.LogHelper;
import de.lmu.ifi.researchime.contentextraction.model.AbstractActionEventJson;
import de.lmu.ifi.researchime.contentextraction.model.AbstractActionEventJson_Table;
import de.lmu.ifi.researchime.contentextraction.model.MessageStatisticsJson;
import de.lmu.ifi.researchime.contentextraction.model.MessageStatisticsJson_Table;
import de.lmu.ifi.researchime.contentextraction.model.WordFrequencyJson;
import retrofit.ResponseCallback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ContentAbstractionSyncJobService extends JobService {

    private static final String TAG = "ContentAbstr.SyncJobS.";

    @Override
    public boolean onStartJob(JobParameters params) {
        Thread.setDefaultUncaughtExceptionHandler(new AppExceptionHandler());
        LogHelper.i(TAG,"JobService started");

        uploadContentCategorizationEvents();
        syncWordFrequencyCounts();
        uploadMessageStatistics();
        return false;
    }

    private void uploadContentCategorizationEvents() {
        final List<AbstractActionEventJson> events = SQLite.select().from(AbstractActionEventJson.class).where(AbstractActionEventJson_Table.uploaded.is(false)).queryList();

        final JsonArray array = new JsonArray();
        for(AbstractActionEventJson abstractedAction : events){
            array.add(abstractedAction.getJson());
        }

        JsonObject object = new JsonObject();
        object.add("events", array);

        RestClient.get(getApplicationContext()).postContentAbstractionEvents(object, new ResponseCallback() {
            @Override
            public void success(Response response) {
                LogHelper.i(TAG, "Content Abstraction Events successfully sent to server.");
                // set events synced
                for(AbstractActionEventJson abstractedAction : events){
                    abstractedAction.setUploaded(true);
                    abstractedAction.save();
                }
                // delete AbstractActionEventJson objects to free storage
                int deletionCount = 0;
                for (AbstractActionEventJson abstractedAction : events) {
                    if (abstractedAction.isUploaded()) {
                        boolean deletedSuccessfully = abstractedAction.delete();
                        deletionCount += deletedSuccessfully ? 1 : 0;
                    }
                }
                LogHelper.i(TAG,"deleted "+deletionCount+" AbstractActionEventJson objects from DB");
            }

            @Override
            public void failure(RetrofitError error) {
                LogHelper.w(TAG, "Content Abstraction Events to server failure: " + RestClient.getErrorDescription(error));
            }
        });
    }

    private void syncWordFrequencyCounts(){
        final List<WordFrequencyJson> events = SQLite.select().from(WordFrequencyJson.class).queryList();

        final JsonArray array = new JsonArray();
        for(WordFrequencyJson wordFrequencyJson : events){
            array.add(wordFrequencyJson.getJson());
        }

        JsonObject object = new JsonObject();
        object.add("wordfrequencies", array);

        RestClient.get(getApplicationContext()).postWordFrequencies(object, new ResponseCallback() {
            @Override
            public void success(Response response) {
                LogHelper.i(TAG, "Word Frequencies successfully sent to server.");
            }

            @Override
            public void failure(RetrofitError error) {
                LogHelper.w(TAG, "Word Frequencies to server failure: " + RestClient.getErrorDescription(error));
            }
        });
    }

    private void uploadMessageStatistics(){
        LogHelper.i(TAG,"uploadMessageStatistics()");
        final List<MessageStatisticsJson> messageStatistics = SQLite.select().from(MessageStatisticsJson.class).where(MessageStatisticsJson_Table.uploaded.is(false)).queryList();
        LogHelper.i(TAG,messageStatistics.size()+" Message objects found for upload");

        final JsonArray array = new JsonArray();
        for(MessageStatisticsJson messageStatistic : messageStatistics){
            array.add(messageStatistic.getJson());
        }

        JsonObject object = new JsonObject();
        object.add("messageStatistics", array);

        RestClient.get(getApplicationContext()).postMessageStatistics(object, new ResponseCallback() {
            @Override
            public void success(Response response) {
                LogHelper.i(TAG, "MessageStatistics successfully sent to server.");
                // set objects as synced
                for (MessageStatisticsJson messageStatistic : messageStatistics){
                    messageStatistic.setUploaded(true);
                    messageStatistic.save();
                }
                // delete AbstractActionEventJson objects to free storage
                int deletionCount = 0;
                for (MessageStatisticsJson messageStatisticsJson : messageStatistics) {
                    if (messageStatisticsJson.isUploaded()) {
                        boolean deletedSuccessfully = messageStatisticsJson.delete();
                        deletionCount += deletedSuccessfully ? 1 : 0;
                    }
                }
                LogHelper.i(TAG,"deleted "+deletionCount+" MessageStatisticsJson objects from DB");
            }

            @Override
            public void failure(RetrofitError error) {
                LogHelper.w(TAG, "MessageStatistics to server failure: " + RestClient.getErrorDescription(error));
            }
        });
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }

    public static void launchService(Context context){
        JobScheduler jobScheduler =
                (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.schedule(new JobInfo.Builder(ContentAbstractionSyncJobService.TAG.hashCode(),
                new ComponentName(context, ContentAbstractionSyncJobService.class)).setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
                .build());
        LogHelper.i(TAG,"scheduled ContentAbstractionSyncJobService");
    }
}
