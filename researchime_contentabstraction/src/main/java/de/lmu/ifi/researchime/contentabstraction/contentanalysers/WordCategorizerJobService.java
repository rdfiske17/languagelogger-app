package de.lmu.ifi.researchime.contentabstraction.contentanalysers;

import android.app.job.JobParameters;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.IBinder;

import com.raizlabs.android.dbflow.sql.language.SQLite;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.lmu.ifi.researchime.contentabstraction.AppExceptionHandler;
import de.lmu.ifi.researchime.contentabstraction.OnAbstractedActionEventsReadyBroadcastReceiver;
import de.lmu.ifi.researchime.base.logging.BaseInputLogger;
import de.lmu.ifi.researchime.base.logging.LogHelper;
import de.lmu.ifi.researchime.contentabstraction.logging.InputLogger;
import de.lmu.ifi.researchime.contentabstraction.model.ContentUnitEventType;
import de.lmu.ifi.researchime.contentabstraction.model.abstractedcontent.AbstractedAction;
import de.lmu.ifi.researchime.contentabstraction.model.abstractedcontent.AbstractedWordAction;
import de.lmu.ifi.researchime.contentabstraction.model.config.LogicalCategoryList;
import de.lmu.ifi.researchime.contentabstraction.model.config.LogicalCategoryList_Table;
import de.lmu.ifi.researchime.contentabstraction.model.rawcontent.ContentChangeEvent;
import de.lmu.ifi.researchime.contentabstraction.model.rawcontent.ContentChangeEvent_Table;
import de.lmu.ifi.researchime.contentabstraction.treetagger.TreeTaggerService;

/**
 * creates @see AbstractedAction events from ContentChangeEvents , by classifying the ContentChangeEvent's content with the configured LogicalCategoryList
 * This JobService expects the first parameter to be the LogicalCategoryList's id . Just one LogicalCategoryList per service execution is possible
 * - service fetches LogicalCategoryList from the module's database
 * - it fetches all unprocessed ContentChangeEvents from the module's database
 * - it does the categorization work, resulting in AbstractedAction events being stored in the module's database
 * - a broadcast intent to OnAbstractedActionEventsReadyBroadcastReceiver is sent, to notify the caller that the service has finished
 */
public class WordCategorizerJobService extends AbstractContentEventAnalyser {

    public static final String TAG = "WordCategorizerJobServ.";
    public static final String PARAM_KEY_LOGICALLISTID = "logicallistId";

    private WordCategorizationAsyncTask asyncTask;
    private TreeTaggerService treeTaggerService;

    private ServiceConnection treeTaggerServiceConnection;

    public WordCategorizerJobService(){}

    private static class WordCategorizationAsyncTask extends AsyncTask<Long, Void, Long> {

        private WeakReference<WordCategorizerJobService> activityReference;
        private final JobParameters params;
        private LogicalCategoryList logicalCategoryList;

        // only retain a weak reference to the activity
        WordCategorizationAsyncTask(WordCategorizerJobService context, final JobParameters params) {
            activityReference = new WeakReference<>(context);
            this.params = params;
        }


        @Override
        protected Long doInBackground(Long... logicalCategoryListIdList) {
            LogHelper.i(TAG,"doInBackground()");
            WordCategorizerJobService service = activityReference.get();

            Long logicalCategoryListId = logicalCategoryListIdList[0];
            logicalCategoryList = SQLite
                    .select()
                    .from(LogicalCategoryList.class)
                    .where(LogicalCategoryList_Table.logicallistId.is(logicalCategoryListId))
                    .querySingle();

            LogHelper.i(TAG,"started AsyncTask for word categorization by list: "+logicalCategoryList.getLogicallistId()+"/"+logicalCategoryList.getLogicallistName());

            // check if list is downloaded
            if (!logicalCategoryList.isDownloaded()){
                LogHelper.w(TAG, "could not categorize by list "+logicalCategoryList.getLogicallistId()+"/"+logicalCategoryList.getLogicallistName()+" because it isn't downloaded yet");
                cancel(true);
                return null;
            }

            try {
                LogHelper.i(TAG,"AsyncTask doInBackground()");
                Long startTime = System.currentTimeMillis();

                final List<ContentChangeEvent> events = SQLite
                        .select()
                        .from(ContentChangeEvent.class)
                        .where(ContentChangeEvent_Table.processedByCategorizer.notLike("%,"+logicalCategoryListId+",%"))
                        .and(ContentChangeEvent_Table.processedByCategorizer.isNot(logicalCategoryListId.toString()))
                        .and(ContentChangeEvent_Table.processedByCategorizer.notLike("%,"+logicalCategoryListId))
                        .and(ContentChangeEvent_Table.processedByCategorizer.notLike(logicalCategoryListId+",%"))
                        .queryList();
                LogHelper.i(TAG,"could load "+events.size()+" events for categorization");


                Map<String,String> word2Category = importWordCategoryList(service.getApplicationContext(), logicalCategoryList.getLocalFilename());
                List<AbstractedAction> abstractedActions = createWordCategoryActions(word2Category, events, logicalCategoryListId);
                for(AbstractedAction abstractedAction : abstractedActions){
                    abstractedAction.setLogicalCategoryList(logicalCategoryList);
                    abstractedAction.save();
                }

                InputLogger.log(service.getApplicationContext(), abstractedActions, logicalCategoryList.getLogicallistId()+"/"+logicalCategoryList.getLogicallistName());

                // notify the the "main component" that there are new events => it will call the callback provided by the parent app
                service.sendBroadcast(new Intent(OnAbstractedActionEventsReadyBroadcastReceiver.RECEIVER_ACTION));
                LogHelper.i(TAG,"events-ready broadcast sent");

                return startTime;
            } catch (Exception e) {
                LogHelper.e(TAG,"AsyncTask in WordCategorizerJobService encountered an exception",e);
                cancel(true);
                return null;
            }
        }

        public String maybeLemmatize(String word) {
            if (logicalCategoryList.isPreappyLemmaExtraction()) {
                if (activityReference.get().treeTaggerService == null){
                    activityReference.get().setupTreeTaggerService();
                }
                try {
                    return activityReference.get().treeTaggerService.lemmatizeWord(word);
                } catch (Exception e) {
                    LogHelper.e(TAG, "could not lemmatize word: "+word,e);
                    return word;
                }
            }
            else {
                return word;
            }
        }

        /**
         * word events like ADDED: "sun", ADDED: "relaxing" are converted to "ADDED positive emotion word", "ADDED verb"
         * @param wordEvents
         */
        public List<AbstractedAction> createWordCategoryActions(Map<String,String> word2Category, List<? extends ContentChangeEvent> wordEvents, Long logicalCategoryListId){

            List<AbstractedAction> abstractedActions = new ArrayList<>();

            for(ContentChangeEvent contentChangeEvent : wordEvents){

                if (contentChangeEvent.isWordAddedEvent()){
                    String word = contentChangeEvent.getAddedWord().getAsString();
                    String category = word2Category.get(maybeLemmatize(word));
                    abstractedActions.add(new AbstractedWordAction(ContentUnitEventType.ADDED,null,category == null ?"unknown":category, contentChangeEvent));
                }
                else if (contentChangeEvent.isWordChangedEvent()){
                    String wordBefore = contentChangeEvent.getContentUnitBefore().getAsString();
                    String wordAfter = contentChangeEvent.getContentUnitAfter().getAsString();
                    String categoryBefore = word2Category.get(maybeLemmatize(wordBefore));
                    String categoryAfter = word2Category.get(maybeLemmatize(wordAfter));
                    abstractedActions.add(new AbstractedWordAction(ContentUnitEventType.CHANGED,categoryBefore == null ?"unknown":categoryBefore, categoryAfter == null ?"unknown":categoryAfter, contentChangeEvent));
                }
                else if (contentChangeEvent.isWordRemovedEvent()){
                    String word = contentChangeEvent.getRemovedWord().getAsString();
                    String category = word2Category.get(maybeLemmatize(word));
                    abstractedActions.add(new AbstractedWordAction(ContentUnitEventType.REMOVED,category == null ?"unknown":category, null,contentChangeEvent));
                }

                contentChangeEvent.setProcessedByCategorizer(logicalCategoryListId);
                contentChangeEvent.setMessageStatistics(contentChangeEvent.getMessageStatistics());
                contentChangeEvent.save();
            }

            return abstractedActions;
        }

        public Map<String,String> importWordCategoryList(Context context, String wordlistFilename) throws IOException {
            Long startTime = System.currentTimeMillis();
            Map<String,String> word2Category = new HashMap<>();

            BufferedReader br = new BufferedReader(new InputStreamReader(context.openFileInput(wordlistFilename)));

            String line;
            while ((line = br.readLine()) != null) {
                String[] lineAsArray;
                if((lineAsArray = line.split("\t")).length == 2){
                    String[] words = lineAsArray[0].split(",");
                    for (String word : words){
                        word2Category.put(word, lineAsArray[1]);
                    }
                } else {
                    LogHelper.w(TAG, "did not import line "+line);
                }
            }
            br.close();

            LogHelper.i(TAG,"wordlist "+wordlistFilename+" import took "+(System.currentTimeMillis()-startTime)+"ms");
            return word2Category;
        }

        @Override
        protected void onPostExecute(Long startTime) {
            WordCategorizerJobService service = activityReference.get();
            service.jobFinished(params, false); // signals the system that the job has finished, and the wakelock can be removed
        }

        @Override
        protected void onCancelled() {
            LogHelper.w(TAG,"AsyncTask onCancelled()");
            WordCategorizerJobService service = activityReference.get();
            service.jobFinished(params, true); // if something failed, retry (exponential backoff)
        }
    }

    @Override
    public boolean onStartJob(final JobParameters params) {

        Thread.setDefaultUncaughtExceptionHandler(new AppExceptionHandler());

        // load the list object
        Long logicallistId = params.getExtras().getLong(PARAM_KEY_LOGICALLISTID);
        LogHelper.i(TAG,"started JobService for word categorization by list "+logicallistId);


        // the work is done on an AsyncTask, to reduce load on Main Thread
        asyncTask = new WordCategorizationAsyncTask(this, params);
        asyncTask.execute(logicallistId);

        return true; // keeping the wakelock alive as long as the async task's thread runs
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        LogHelper.w(TAG,"onStopJob()");
        if (asyncTask != null && !asyncTask.isCancelled()) {
            asyncTask.cancel(false);
        }
        return false;
    }

    @Override
    public void onDestroy() {
        try {
            unbindService(treeTaggerServiceConnection);
        } catch (Exception e) {}
        LogHelper.i(TAG, "service destroyed.");
        super.onDestroy();
    }

    private void setupTreeTaggerService(){
            Intent serviceIntent = new Intent(getApplicationContext(), TreeTaggerService.class);

            treeTaggerServiceConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    treeTaggerService = ((TreeTaggerService.LocalBinder) service).getService();
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    LogHelper.w(TAG, "disconnected for TreeTaggerService");
                }
            };
            bindService(serviceIntent, treeTaggerServiceConnection, Context.BIND_AUTO_CREATE);

            try {
                int sleepCount = 0;
                final int SLEEP_DURATION_MILLIS = 100;
                final int MAX_SLEEP_COUNT = 100;
                while (treeTaggerService == null) {
                   Thread.sleep(SLEEP_DURATION_MILLIS);
                   sleepCount++;
                   if (sleepCount > MAX_SLEEP_COUNT){
                       throw new InterruptedException("slept to wait for treeTaggerService to many times: "+sleepCount+" ("+SLEEP_DURATION_MILLIS*sleepCount+"ms) ");
                   }
                }
                LogHelper.i(TAG, "treeTagger service connected.");
            } catch (InterruptedException e) {
                LogHelper.e(TAG, "boundService connection could not be established within the timeout",e);
            }
    }



}
