package de.lmu.ifi.researchime.contentabstraction.contentanalysers.patternmatching;

import android.app.job.JobParameters;
import android.content.Intent;

import com.raizlabs.android.dbflow.sql.language.SQLite;

import java.util.ArrayList;
import java.util.List;

import de.lmu.ifi.researchime.contentabstraction.OnAbstractedActionEventsReadyBroadcastReceiver;
import de.lmu.ifi.researchime.contentabstraction.contentanalysers.AbstractContentEventAnalyser;
import de.lmu.ifi.researchime.base.logging.LogHelper;
import de.lmu.ifi.researchime.contentabstraction.logging.InputLogger;
import de.lmu.ifi.researchime.contentabstraction.model.abstractedcontent.AbstractedAction;
import de.lmu.ifi.researchime.contentabstraction.model.abstractedcontent.AbstractedActionRawContent;
import de.lmu.ifi.researchime.contentabstraction.model.abstractedcontent.AbstractedWordAction;
import de.lmu.ifi.researchime.contentabstraction.model.config.PatternMatcherConfig;
import de.lmu.ifi.researchime.contentabstraction.model.config.PatternMatcherConfig_Table;
import de.lmu.ifi.researchime.contentabstraction.model.rawcontent.ContentChangeEvent;
import de.lmu.ifi.researchime.contentabstraction.model.rawcontent.ContentChangeEvent_Table;


public class PatternMatcherJobService extends AbstractContentEventAnalyser {

    public static final String TAG = "PatternMatcherJob.S.";
    public static final Integer JOB_ID = TAG.hashCode();
    public static final String PARAM_KEY_PATTERNID = "patternId";
    public static final String EMOJI_OBFUSCATION_TAG = "<EMOJIS>";

    @Override
    public boolean onStartJob(JobParameters params) {
        Long patternId = params.getExtras().getLong(PARAM_KEY_PATTERNID);
        LogHelper.i(TAG,"started JobService for pattern matching by pattern "+patternId);

        // load each pattern config
        List<PatternMatcherConfig> patternMatcherConfigs = SQLite
                .select()
                .from(PatternMatcherConfig.class)
                .where(PatternMatcherConfig_Table.regexMatcherId.is(patternId))
                .queryList();
//        PatternMatcherConfig testPatternMatcherConfig = new PatternMatcherConfig();
//        testPatternMatcherConfig.setLogRawContent(true);
//        testPatternMatcherConfig.setRegexMatcherName("Test Emoji Matcher");
//        testPatternMatcherConfig.setRegexMatcherId(1L);
//        testPatternMatcherConfig.setRegex("[\uD83C-\uDBFF\uDC00-\uDFFF]+");
//        List<PatternMatcherConfig> patternMatcherConfigs = new ArrayList<>();
//        patternMatcherConfigs.add(testPatternMatcherConfig);

        // load events
        final List<ContentChangeEvent> events = SQLite
                .select()
                .from(ContentChangeEvent.class)
                .where(ContentChangeEvent_Table.processedByPatternmatcher.notLike("%,"+patternId+",%"))
                .and(ContentChangeEvent_Table.processedByPatternmatcher.isNot(patternId.toString()))
                .and(ContentChangeEvent_Table.processedByPatternmatcher.notLike("%,"+patternId))
                .and(ContentChangeEvent_Table.processedByPatternmatcher.notLike(patternId+",%"))
                .queryList();
        LogHelper.i(TAG,"could load "+events.size()+" events for pattern matching");

        // for each event, run each patternmatcher
        for(ContentChangeEvent contentChangeEvent : events) {
            for (PatternMatcherConfig patternMatcherConfig : patternMatcherConfigs) {
                if (patternMatcherConfig.getRegexMatcherId().equals(patternId)) {
                    runPatternMatcher(contentChangeEvent, patternMatcherConfig);
                    contentChangeEvent.setProcessedByPatternmatcher(patternId);
                    contentChangeEvent.update();
                    break;
                }
            }
        }

        // notify the the "main component" that there are new events => it will call the callback provided by the parent app
        // TODO does not work in AndroidTest
        sendBroadcast(new Intent(OnAbstractedActionEventsReadyBroadcastReceiver.RECEIVER_ACTION));
        LogHelper.i(TAG,"events-ready broadcast sent");

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }

    public List<AbstractedAction> runPatternMatcher(ContentChangeEvent contentChangeEvent, PatternMatcherConfig patternMatcherConfig){

        PatternMatcher patternMatcher = patternMatcherConfig.createPatternMatcherInstance();
        boolean logRaw = patternMatcherConfig.isLogRawContent();

        List<AbstractedAction> abstractedActions = new ArrayList<>();
        try {
            switch (contentChangeEvent.getType()) {
                case ADDED:
                    List<String> matches = patternMatcher.match(contentChangeEvent.getContentUnitAfter());
                    for (String match : matches) {
                        if (logRaw) {
                            abstractedActions.add(new AbstractedActionRawContent(
                                    contentChangeEvent.getType(),
                                    null,
                                    match,
                                    contentChangeEvent
                            ));
                        } else {
                            abstractedActions.add(new AbstractedWordAction(
                                    contentChangeEvent.getType(),
                                    null,
                                    EMOJI_OBFUSCATION_TAG,
                                    contentChangeEvent
                            ));
                        }
                    }
                    break;
                case CHANGED:
                    List<String> matchesBefore = patternMatcher.match(contentChangeEvent.getContentUnitBefore());
                    List<String> matchesAfter = patternMatcher.match(contentChangeEvent.getContentUnitAfter());

                    LogHelper.i(TAG,"runPatternMatcher - case CHANGED - contentChangeEvent: "+contentChangeEvent.toString());

                    if (matchesBefore.size() == matchesAfter.size()) {
                        // if the match-sequence count of before and after is the same, than we assume it was a change
                        for (int i = 0; i < matchesBefore.size(); i++) {
                            if (logRaw) {
                                abstractedActions.add(new AbstractedActionRawContent(
                                        contentChangeEvent.getType(),
                                        matchesBefore.get(i),
                                        matchesAfter.get(i),
                                        contentChangeEvent
                                ));
                            } else {
                                abstractedActions.add(new AbstractedWordAction(
                                        contentChangeEvent.getType(),
                                        EMOJI_OBFUSCATION_TAG,
                                        EMOJI_OBFUSCATION_TAG,
                                        contentChangeEvent
                                ));
                            }
                        }
                    } else {
                        // otherwise, single removed and added events are assumed
                        for (String matchBefore : matchesBefore) {
                            if (logRaw) {
                                abstractedActions.add(new AbstractedActionRawContent(
                                        contentChangeEvent.getType(),
                                        matchBefore,
                                        matchesAfter.get(0),
                                        contentChangeEvent
                                ));
                            } else {
                                abstractedActions.add(new AbstractedWordAction(
                                        contentChangeEvent.getType(),
                                        EMOJI_OBFUSCATION_TAG,
                                        "",
                                        contentChangeEvent
                                ));
                            }
                        }
                        for (String matchAfter : matchesAfter) {
                            if (logRaw) {
                                abstractedActions.add(new AbstractedActionRawContent(
                                        contentChangeEvent.getType(),
                                        "",
                                        matchAfter,
                                        contentChangeEvent
                                ));
                            } else {
                                abstractedActions.add(new AbstractedWordAction(
                                        contentChangeEvent.getType(),
                                        "",
                                        EMOJI_OBFUSCATION_TAG,
                                        contentChangeEvent
                                ));
                            }
                        }
                    }
                    break;
                case REMOVED:
                    List<String> matches2 = patternMatcher.match(contentChangeEvent.getContentUnitBefore());
                    for (String match : matches2) {
                        if (logRaw) {
                            abstractedActions.add(new AbstractedActionRawContent(
                                    contentChangeEvent.getType(),
                                    match,
                                    null,
                                    contentChangeEvent
                            ));
                        } else {
                            abstractedActions.add(new AbstractedWordAction(
                                    contentChangeEvent.getType(),
                                    EMOJI_OBFUSCATION_TAG,
                                    null,
                                    contentChangeEvent
                            ));
                        }
                    }
                    break;
                default:
                    LogHelper.w(TAG, "pattern matching for ContentChangeEvent of type " + contentChangeEvent.getType() + " is not implemented");
            }

        } catch (Exception e){
            LogHelper.e(TAG,"fetching data from pattern matcher failed",e);
        }

        // save actions
        for (AbstractedAction abstractedAction : abstractedActions){
            abstractedAction.setPatternMatcherConfig(patternMatcherConfig);
            abstractedAction.save();

            InputLogger.logPatternMatch(getApplicationContext(),"-", patternMatcherConfig.getRegexMatcherName(), abstractedAction);
        }

        InputLogger.log(getApplicationContext(), abstractedActions, patternMatcherConfig.getRegexMatcherId()+"/"+patternMatcherConfig.getRegexMatcherName());

        return abstractedActions; // for testing
    }
}
