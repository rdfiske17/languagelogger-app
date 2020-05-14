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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.lmu.ifi.researchime.contentabstraction.OnWordFrequenciesChangedBroadcastReceiver;
import de.lmu.ifi.researchime.base.logging.BaseInputLogger;
import de.lmu.ifi.researchime.base.logging.LogHelper;
import de.lmu.ifi.researchime.contentabstraction.logging.InputLogger;
import de.lmu.ifi.researchime.contentabstraction.model.ContentUnitEventType;
import de.lmu.ifi.researchime.contentabstraction.model.abstractedcontent.WordFrequency;
import de.lmu.ifi.researchime.contentabstraction.model.abstractedcontent.WordFrequency_Table;
import de.lmu.ifi.researchime.contentabstraction.model.config.LogicalWordList;
import de.lmu.ifi.researchime.contentabstraction.model.config.LogicalWordList_Table;
import de.lmu.ifi.researchime.contentabstraction.model.rawcontent.ContentChangeEvent;
import de.lmu.ifi.researchime.contentabstraction.model.rawcontent.ContentChangeEvent_Table;
import de.lmu.ifi.researchime.contentabstraction.treetagger.TreeTaggerService;

/**
 * creates/updates @see WordFrequency objects from ContentChangeEvents , by counting the ContentChangeEvent's content's occurrences with the configured LogicalWordList
 * This JobService expects the first parameter to be the LogicalWordList's id . Just one LogicalWordList per service execution is possible
 * - service fetches LogicalWordList from the module's database
 * - it fetches all unprocessed ContentChangeEvents from the module's database
 * - it does the counting work, and creates or updates WordFrequency objects stored in the module's database
 * - a broadcast intent to OnWordFrequenciesChangedBroadcastReceiver is sent, to notify the caller that the service has finished
 */
public class WordFrequencyCounterJobService extends AbstractContentEventAnalyser {

    public static final String TAG = "WordFreq.CounterJob.S.";
    public static final Integer JOB_ID = TAG.hashCode();
    public static final String PARAM_KEY_LOGICALLISTID = "logicallistId";

    private WordFrequencyCountAsyncTask asyncTask;
    private TreeTaggerService treeTaggerService;
    private ServiceConnection treeTaggerServiceConnection;

    private static class WordFrequencyCountAsyncTask extends AsyncTask<LogicalWordList, Void, Long> {

        private WeakReference<WordFrequencyCounterJobService> activityReference;
        private final JobParameters params;

        // only retain a weak reference to the activity
        WordFrequencyCountAsyncTask(WordFrequencyCounterJobService context, final JobParameters params) {
            activityReference = new WeakReference<>(context);
            this.params = params;
        }

        @Override
        protected Long doInBackground(LogicalWordList... logicalWordLists) {
            LogHelper.i(TAG,"doInBackground()");
            WordFrequencyCounterJobService service = activityReference.get();

            LogicalWordList logicalWordList = logicalWordLists[0];

            LogHelper.i(TAG,"started AsyncTask for word count by whitelist: "+logicalWordList.getLogicallistId()+"/"+logicalWordList.getLogicallistName());

            // check if list is downloaded
            if (!logicalWordList.isDownloaded()){
                LogHelper.w(TAG, "could not categorize by list "+logicalWordList.getLogicallistId()+"/"+logicalWordList.getLogicallistName()+" because it isn't downloaded yet");
                cancel(true);
                return null;
            }

            try {
                LogHelper.i(TAG,"AsyncTask doInBackground()");
                Long startTime = System.currentTimeMillis();

                List<ContentChangeEvent> events = SQLite
                        .select()
                        .from(ContentChangeEvent.class)
                        .where(ContentChangeEvent_Table.processedByWhitelistCounter.notLike("%,"+logicalWordList.getLogicallistId()+",%"))
                        .and(ContentChangeEvent_Table.processedByWhitelistCounter.isNot(logicalWordList.getLogicallistId().toString()))
                        .and(ContentChangeEvent_Table.processedByWhitelistCounter.notLike("%,"+logicalWordList.getLogicallistId()))
                        .and(ContentChangeEvent_Table.processedByWhitelistCounter.notLike(logicalWordList.getLogicallistId()+",%"))
                        .queryList();
                LogHelper.i(TAG,"could load "+events.size()+" events for categorization");

                Map<String,Integer> wordsFound = new HashMap<>(); // word -> occurrence count

                Set<String> wordlist = service.importWordWhitelist(service.getApplicationContext(), logicalWordList.getLocalFilename());
                for(ContentChangeEvent hle : events){
                    if (hle.getType() == ContentUnitEventType.ADDED){
                        String word = hle.getAddedWord().getAsString();

                        // lemma extraction
                        word = maybeLemmatize(logicalWordList, word);

                        if (wordlist.contains(word)) {
                            if (!wordsFound.containsKey(word)) {
                                wordsFound.put(word,1);
                            }
                            else {
                                wordsFound.put(word, wordsFound.get(word)+1);
                            }
                        }
                    }

                    hle.setProcessedByWhitelistCounter(logicalWordList.getLogicallistId());
                    hle.save();
                }

                // update word frequencies in db
                for (Map.Entry<String,Integer> wordFound : wordsFound.entrySet()) {
                    final WordFrequency wordFrequency = SQLite
                            .select()
                            .from(WordFrequency.class)
                            .where(WordFrequency_Table.word.is(wordFound.getKey()))
                            .querySingle();
                    if (wordFrequency != null) {
                        // increase count if word already exists
                        wordFrequency.increaseCountBy(wordFound.getValue());
                        wordFrequency.update();
                    }
                    else {
                        // create new entry if word did not occur yet
                        WordFrequency wordFrequencyNew = new WordFrequency();
                        wordFrequencyNew.setLogicalWordList(logicalWordList);
                        wordFrequencyNew.setWord(wordFound.getKey());
                        wordFrequencyNew.setCount(wordFound.getValue());
                        wordFrequencyNew.save();
                    }
                }

                InputLogger.logWhitelistWords(service.getApplicationContext(), wordsFound, logicalWordList);

                // notify the the "main component" that there are new events => it will call the callback provided by the parent app
                service.sendBroadcast(new Intent(OnWordFrequenciesChangedBroadcastReceiver.RECEIVER_ACTION));
                LogHelper.i(TAG,"wordfrequencies-changed broadcast sent");

                return startTime;
            } catch (Exception e){
                LogHelper.e(TAG,"AsyncTask in WordFrequencyCounterJobService encountered an exception",e);
                cancel(true);
                return null;
            }
        }

        public String maybeLemmatize(LogicalWordList logicalWordList, String word) {
            if (logicalWordList.isPreappyLemmaExtraction()) {
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

        @Override
        protected void onPostExecute(Long startTime) {
            WordFrequencyCounterJobService service = activityReference.get();
            service.jobFinished(params, false); // signals the system that the job has finished, and the wakelock can be removed
        }

        @Override
        protected void onCancelled() {
            WordFrequencyCounterJobService service = activityReference.get();
            LogHelper.w(TAG,"AsyncTask onCancelled()");
            service.jobFinished(params, true); // if something failed, retry (exponential backoff)
        }
    }

    @Override
    public boolean onStartJob(final JobParameters jobParameters) {
        Long logicallistId = jobParameters.getExtras().getLong(PARAM_KEY_LOGICALLISTID);
        LogHelper.i(TAG,"started JobService for word categorization by list "+logicallistId);


        LogicalWordList logicalWordList = SQLite
                .select()
                .from(LogicalWordList.class)
                .where(LogicalWordList_Table.logicallistId.is(logicallistId))
                .querySingle();

        // the work is done on an AsyncTask, to reduce load on Main Thread
        asyncTask = new WordFrequencyCountAsyncTask(this, jobParameters);
        asyncTask.execute(logicalWordList);

        return true; // keeping the wakelock alive as long as the async task's thread runs
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        LogHelper.w(TAG,"onStopJob()");
        if (asyncTask != null && !asyncTask.isCancelled()) {
            asyncTask.cancel(false);
        }
        return false;
    }

    public Set<String> importWordWhitelist(Context context, String wordlistFilename){
        Long startTime = System.currentTimeMillis();
        Set<String> wordset = new HashSet<>();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(context.openFileInput(wordlistFilename)));
            String line;

            while ((line = br.readLine()) != null) {

                if (!line.startsWith("#")){
                    String[] lineAsArray = line.split(" ");

                    processLineArray(lineAsArray, wordset);



                } else {
                    LogHelper.w(TAG, "did not import line "+line);
                }
            }
            br.close();
        }
        catch (IOException e) {
            //You'll need to add proper error handling here
            e.printStackTrace();
        }
        LogHelper.i(TAG,"wordset "+wordlistFilename+" import took "+(System.currentTimeMillis()-startTime)+"ms. It contains "+wordset.size()+" items");
        return wordset;
    }

    /**
     * takes a array of lines from the derewo word list, and creates a set of words
     * @param lineArray the derewo word list as array of lines
     * @param wordset an (empty) set where the words will be saved to
     */
    public void processLineArray(String[] lineArray, Set<String> wordset){
        String wordColumn = lineArray[0];
        boolean isWordWithBraketOptions = wordColumn.contains("(") && wordColumn.contains(")");
        if (!isWordWithBraketOptions){
            String[] splitByComma = wordColumn.split(",");
            // for example  der,die,das  or a usual word
            for(String word : splitByComma){
                wordset.add(word);
            }
        }
        else {
            // it's a word like deine(r) or deine(r,s)
            // divide into pre- and in-braket word
            Pattern braketsPattern = Pattern.compile("^(.+)\\((.+)\\)$");
            Matcher braketsMatcher = braketsPattern.matcher(wordColumn);
            braketsMatcher.matches();
            MatchResult bracketsMatchResult = braketsMatcher.toMatchResult();

            String wordBeforeBracket = bracketsMatchResult.group(1);
            String optionsInBracket = bracketsMatchResult.group(2);

            String[] optionsSplitByBrackets = optionsInBracket.split(",");

            // 1. save the version of the word without any option
            wordset.add(wordBeforeBracket);

            // 2. save one version of the word before + one option at a time
            for(String wordPartOption : optionsSplitByBrackets){
                wordset.add(wordBeforeBracket+wordPartOption);
            }
        }
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
            while (treeTaggerService == null) {
                Thread.sleep(100);
                sleepCount++;
                if (sleepCount > 100){
                    throw new InterruptedException("slept to wait for treeTaggerService to many times: "+sleepCount);
                }
            }
            LogHelper.i(TAG, "treeTagger service connected.");
        } catch (InterruptedException e) {
            LogHelper.e(TAG, "boundService connection could not be established within the timeout",e);
        }
    }

    @Override
    public void onDestroy() {
        try {
            unbindService(treeTaggerServiceConnection);
        } catch (Exception e) {}
        LogHelper.i(TAG, "service destroyed.");
        super.onDestroy();
    }

}
