package de.lmu.ifi.researchime.contentabstraction;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.IntentFilter;
import android.os.PersistableBundle;

import com.raizlabs.android.dbflow.config.FlowConfig;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.config.RimeContentabstractionGeneratedDatabaseHolder;

import java.util.List;

import de.lmu.ifi.researchime.base.BuildConfig;
import de.lmu.ifi.researchime.contentabstraction.contentanalysers.WordCategorizerJobService;
import de.lmu.ifi.researchime.contentabstraction.contentanalysers.WordFrequencyCounterJobService;
import de.lmu.ifi.researchime.contentabstraction.contentanalysers.patternmatching.PatternMatcherJobService;
import de.lmu.ifi.researchime.base.logging.LogHelper;
import de.lmu.ifi.researchime.contentabstraction.model.config.LogicalCategoryList;
import de.lmu.ifi.researchime.contentabstraction.model.config.LogicalWordList;
import de.lmu.ifi.researchime.contentabstraction.model.config.PatternMatcherConfig;
import de.lmu.ifi.researchime.contentabstraction.model.config.RIMEContentAbstractionConfig;
import de.lmu.ifi.researchime.contentabstraction.model.rawcontent.ContentChangeEvent;

/**
 * The main class of the ResearchIME Content Abstraction module to call!
 * It is given a configuration through the constructor, and afterwards by calling the processContentChangeEvents method
 * the given ContentChangeEvents are analyzed. Results are returned to the caller via a callback object
 */
public class RIMEInputContentProcessingController {

    private static final String TAG = "InputContentProc.Cont.";
    private final RIMEContentAbstractionConfig rimeContentAbstractionConfig;
    private OnAbstractedActionEventsReadyBroadcastReceiver abstractedActionEventsReadyBroadcastReceiver;
    private OnWordFrequenciesChangedBroadcastReceiver wordFrequenciesChangedBroadcastReceiver;

    /**
     * Initializes the ResearchIME Content Abstraction module with the given configuration
     * @param context
     * @param rimeContentAbstractionConfig
     */
    public RIMEInputContentProcessingController(Context context, RIMEContentAbstractionConfig rimeContentAbstractionConfig) {
        Thread.setDefaultUncaughtExceptionHandler(new AppExceptionHandler());
        this.rimeContentAbstractionConfig = rimeContentAbstractionConfig;
        initDatabase(context);
        this.rimeContentAbstractionConfig.save(); // save into database, just in case the caller did not. The services called in the following will try to fetch it from there
    }

    public static void initDatabase(Context context){
        // https://www.jianshu.com/p/0c017a715410
        // https://github.com/agrosner/DBFlow/issues/266
        FlowConfig flowConfig = new FlowConfig.Builder(context)
                .addDatabaseHolder(RimeContentabstractionGeneratedDatabaseHolder.class)
                .openDatabasesOnInit(true)
                .build();
        FlowManager.init(flowConfig);

        LogHelper.i(TAG,"initialized DBFlowDatabase (RimeContentabstractionGeneratedDatabaseHolder)");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @param context
     * @param contentChangeEvents the events to be analyzed
     * @param callback that is notified when new AbstractedAction events were created or the word frequencies counts changed
     */
    public void processContentChangeEvents(Context context, List<ContentChangeEvent> contentChangeEvents, RIMECallback callback){
        LogHelper.d(TAG,"processContentChangeEvents()");
        if (!contentChangeEvents.isEmpty()) {

            // register observer that will be notified when the below services finish
            abstractedActionEventsReadyBroadcastReceiver = new OnAbstractedActionEventsReadyBroadcastReceiver(callback);
            context.getApplicationContext().registerReceiver(abstractedActionEventsReadyBroadcastReceiver, new IntentFilter(OnAbstractedActionEventsReadyBroadcastReceiver.RECEIVER_ACTION));

            wordFrequenciesChangedBroadcastReceiver = new OnWordFrequenciesChangedBroadcastReceiver(callback);
            context.getApplicationContext().registerReceiver(wordFrequenciesChangedBroadcastReceiver, new IntentFilter(OnWordFrequenciesChangedBroadcastReceiver.RECEIVER_ACTION));

            // copy the given events into this module's database
            for(ContentChangeEvent contentChangeEvent : contentChangeEvents){
                contentChangeEvent.save();
            }

            LogHelper.i(TAG,"should have saved "+contentChangeEvents.size()+" high level events to db");


            // --------- finally trigger next processing steps -----------

            // ----- word categorization service ------
            LogHelper.d(TAG,rimeContentAbstractionConfig.getLogicalCategoryLists().size()+" logical category lists ready for categorization");
            if (rimeContentAbstractionConfig.getLogicalCategoryLists().size() == 0) {
                LogHelper.w(TAG, "0 logical category lists seems unrealistic - is there a problem?");
            }
            for (LogicalCategoryList logicalCategoryList : rimeContentAbstractionConfig.getLogicalCategoryLists()){
                PersistableBundle bundle = new PersistableBundle();
                bundle.putLong(WordCategorizerJobService.PARAM_KEY_LOGICALLISTID, logicalCategoryList.getLogicallistId());


                JobInfo.Builder builder = new JobInfo.Builder(
                        (WordCategorizerJobService.TAG + logicalCategoryList.getLogicallistId()).hashCode(),
                        new ComponentName(context, WordCategorizerJobService.class)
                ).setExtras(bundle);
                if (BuildConfig.DEBUG) {
                    builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY); // to simplify testing
                }
                else {
                    builder.setRequiresDeviceIdle(true);
                }
                JobScheduler jobSchedulerCategorization =
                        (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
                jobSchedulerCategorization.schedule(builder.build());
                LogHelper.i(TAG, "scheduled WordCategorizerJobService");
            }

            // ------- whitelist word frequency counter --------
            LogHelper.d(TAG,rimeContentAbstractionConfig.getLogicalWordLists().size()+" logical word lists ready for frequency counting");
            if (rimeContentAbstractionConfig.getLogicalWordLists().size() == 0) {
                LogHelper.w(TAG, "0 logical word lists seems unrealistic - is there a problem?");
            }
            for (LogicalWordList logicalWordList : rimeContentAbstractionConfig.getLogicalWordLists()){
                PersistableBundle bundle = new PersistableBundle();
                bundle.putLong(WordFrequencyCounterJobService.PARAM_KEY_LOGICALLISTID, logicalWordList.getLogicallistId());

                JobInfo.Builder builder = new JobInfo.Builder(
                        (WordFrequencyCounterJobService.TAG + logicalWordList.getLogicallistId()).hashCode(),
                        new ComponentName(context, WordFrequencyCounterJobService.class)
                ).setExtras(bundle);
                if (BuildConfig.DEBUG) {
                    builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY); // to simplify testing
                }
                else {
                    builder.setRequiresDeviceIdle(true);
                }
                JobScheduler jobSchedulerCategorization =
                        (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
                jobSchedulerCategorization.schedule(builder.build());
                LogHelper.i(TAG, "scheduled WordFrequencyCounterJobService");
            }

            // ------ pattern matcher to detect emojis, phonenumbers, ...
            for(PatternMatcherConfig patternMatcherConfig : rimeContentAbstractionConfig.getPatternMatcherConfigs()) {
                PersistableBundle bundle = new PersistableBundle();
                bundle.putLong(PatternMatcherJobService.PARAM_KEY_PATTERNID, patternMatcherConfig.getRegexMatcherId());
                JobInfo.Builder builder =  new JobInfo.Builder(
                        PatternMatcherJobService.TAG.hashCode(),
                        new ComponentName(context, PatternMatcherJobService.class)
                ).setExtras(bundle);
                if (BuildConfig.DEBUG) {
                    builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
                }
                else {
                    builder.setRequiresDeviceIdle(true);
                }
                JobScheduler jobSchedulerCategorization =
                        (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
                jobSchedulerCategorization.schedule(builder.build());
                LogHelper.i(TAG, "scheduled PatternMatcherJobService");
            }
        }

    }

    /**
     *
     * @param config to know which configs exists, and further know whether an event has been processed for all configs
     */
    public void cleanupOldHLCCEs(Context context, RIMEContentAbstractionConfig config){
        PersistableBundle bundle = new PersistableBundle();

        long[] logicalCategoryListIds = new long[config.getLogicalCategoryLists().size()];
        for (int i = 0; i<config.getLogicalCategoryLists().size(); i++){
            logicalCategoryListIds[i] = config.getLogicalCategoryLists().get(i).getLogicallistId();
        }

        long[] logicalWordListIds = new long[config.getLogicalWordLists().size()];
        for(int i = 0; i<config.getLogicalWordLists().size(); i++){
            logicalWordListIds[i] = config.getLogicalWordLists().get(i).getLogicallistId();
        }

        long[] patternMatcherIds = new long[config.getPatternMatcherConfigs().size()];
        for(int i = 0; i<config.getPatternMatcherConfigs().size(); i++){
            patternMatcherIds[i] = config.getPatternMatcherConfigs().get(i).getRegexMatcherId();
        }

        bundle.putLongArray("logicalCategoryListIds", logicalCategoryListIds);
        bundle.putLongArray("logicalWordListIds", logicalWordListIds);
        bundle.putLongArray("patternMatcherIds", patternMatcherIds);

        JobScheduler jobSchedulerCategorization =
                (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobSchedulerCategorization.schedule(
                new JobInfo.Builder(
                        CleanupContentChangeEventsJobService.TAG.hashCode(),
                        new ComponentName(context, CleanupContentChangeEventsJobService.class)
                ).setExtras(bundle)
                        .setRequiresDeviceIdle(true)
                        .build());
        LogHelper.i(TAG, "scheduled CleanupContentChangeEventsJobService");
    }
}
