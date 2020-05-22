package de.lmu.ifi.researchime;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.sql.language.SQLite;

import java.util.List;

import de.lmu.ifi.researchime.contentabstraction.RIMEInputContentProcessingController;
import de.lmu.ifi.researchime.contentabstraction.RIMECallback;
import de.lmu.ifi.researchime.contentabstraction.model.InputContent;
import de.lmu.ifi.researchime.contentabstraction.model.MessageStatistics;
import de.lmu.ifi.researchime.contentabstraction.model.abstractedcontent.AbstractedAction;
import de.lmu.ifi.researchime.contentabstraction.model.abstractedcontent.WordFrequency;
import de.lmu.ifi.researchime.contentabstraction.model.config.RIMEContentAbstractionConfig;
import de.lmu.ifi.researchime.contentextraction.FullMessageWordExtractor;
import de.lmu.ifi.researchime.contentextraction.KeyboardMessageStatisticsGenerator;
import de.lmu.ifi.researchime.contentextraction.MessageDiffWordEventExtractor;
import de.lmu.ifi.researchime.contentextraction.logging.LogHelper;
import de.lmu.ifi.researchime.contentextraction.model.AbstractActionEventJson;
import de.lmu.ifi.researchime.contentextraction.model.MessageStatisticsJson;
import de.lmu.ifi.researchime.contentextraction.model.WordFrequencyJson;
import de.lmu.ifi.researchime.contentabstraction.model.rawcontent.ContentChangeEvent;
import de.lmu.ifi.researchime.contentextraction.logging.InputLogger;
import de.lmu.ifi.researchime.contentextraction.model.event.Event;

public class ContentAbstractionCaller {

    private static final String TAG = "InputContentProc.Cont.";

    private final MessageDiffWordEventExtractor messageDiffWordEventExtractor;
    private final FullMessageWordExtractor fullMessageWordExtractor;
    private final KeyboardMessageStatisticsGenerator keyboardMessageStatisticsGenerator;

    public ContentAbstractionCaller(@NonNull Context context) {
        messageDiffWordEventExtractor = new MessageDiffWordEventExtractor();
        fullMessageWordExtractor = new FullMessageWordExtractor();
        keyboardMessageStatisticsGenerator = new KeyboardMessageStatisticsGenerator();
    }

    public void extractWordEventsAndCallContentAbstractionModule(final Context context, final String userUuid, List<Event> eventBufferEvents){
        LogHelper.d(TAG,"extractWordEventsAndCallContentAbstractionModule()");
        if (!eventBufferEvents.isEmpty()) {
            // ----- create a "message-object" (class InputContent) -----
            // it is used to know which events belong together, and holds message statistics
            InputContent inputContent = new InputContent();
            inputContent.save();

            // -------- generate message statistics --------
            MessageStatistics messageStatistics = keyboardMessageStatisticsGenerator.createMessageStatistics(eventBufferEvents);
            inputContent.setMessageStatistics(messageStatistics);
            try {
                // save into db, which is a different one than the content extraction module (database exists per module)
                MessageStatisticsJson.fromMessageStatistics(messageStatistics, userUuid).save();
            } catch (Exception e){
                LogHelper.e(TAG,"saving MessageStatistics from content_abstraction module failed",e);
            }

            // ------- trigger PhoneStudy snapshot -------
            LogHelper.i(TAG,"triggering PhoneStudy snapshot");
            Intent intent = new Intent("my-fence-receiver-action");//AwarenessApiReceiver.FENCE_RECEIVER_ACTION
            intent.putExtra("trigger","RIME [messageStatisticsId:"+messageStatistics.getId()+"]");
            context.sendBroadcast(intent);



            // ----- create ContentChange events for message diff ------
            // each word the user adds, edits or removes yields one event.
            // Words that existed in the input field before and were not changed are not regarded
            List<ContentChangeEvent> highLevelEvents = messageDiffWordEventExtractor.extractHigherLevelContentChangeEvents(inputContent, eventBufferEvents, null);
            inputContent.setContentChangeEvents(highLevelEvents);
            InputLogger.log(context, eventBufferEvents, highLevelEvents);
            for(ContentChangeEvent contentChangeEvent : highLevelEvents){
                contentChangeEvent.save();
            }

            // ----- create ContentChange events for full message ------
            // a CONTAINED event is created for each word of the message
            // no matter whether it was in the textfield before, or added during that typing session
            List<ContentChangeEvent> highLevelEventsFullMsg = fullMessageWordExtractor.extractHigherLevelContentChangeEvents(inputContent, eventBufferEvents, null);
            inputContent.getContentChangeEvents().addAll(highLevelEventsFullMsg);
            InputLogger.log(context, eventBufferEvents, highLevelEventsFullMsg);
            for(ContentChangeEvent contentChangeEvent : highLevelEventsFullMsg){
                contentChangeEvent.save();
            }

            LogHelper.i(TAG,"should have saved "+highLevelEvents.size()+" msg-diff- and "+highLevelEventsFullMsg.size()+" full-msg-high-level events to db");

            // call content abstraction module
            RIMEContentAbstractionConfig rimeContentAbstractionConfig = SQLite.select().from(RIMEContentAbstractionConfig.class).querySingle();
            if (rimeContentAbstractionConfig != null) {
                RIMEInputContentProcessingController rimeInputContentProcessingController = new RIMEInputContentProcessingController(context, rimeContentAbstractionConfig);
                rimeInputContentProcessingController.processContentChangeEvents(context, highLevelEvents, new RIMECallback() {
                    @Override
                    public boolean onAbstractedActionEventsReady(List<AbstractedAction> abstractedActionEvents) {
                        LogHelper.d(TAG,"onAbstractedActionEventsReady()");
                        try {
                            // save into db, which is a different one than the content extraction module (database exists per module)
                            for (AbstractedAction abstractedAction : abstractedActionEvents) {
                                AbstractActionEventJson.fromAbstractActionEvent(abstractedAction, userUuid).save();
                            }
                            // TODO transaction to revert the already done events in case of failure
                        } catch (Exception e){
                            LogHelper.e(TAG,"saving AbstractAction events from content_abstraction module failed",e);
                            return false;
                        }

                        ContentAbstractionSyncJobService.launchService(context);
                        return true;
                    }

                    @Override
                    public boolean onWordFrequenciesChanged(List<WordFrequency> wordFrequencies) {
                        LogHelper.d(TAG,"onWordFrequenciesChanged()");
                        try {
                            // save into db, which is a different one than the content extraction module (database exists per module)
                            for (WordFrequency wordFrequency : wordFrequencies) {
                                WordFrequencyJson.fromWordFrequency(wordFrequency, userUuid).save();
                            }
                        } catch (Exception e){
                            LogHelper.e(TAG,"saving WordFrequency counts from content_abstraction module failed",e);
                            return false;
                        }

                        ContentAbstractionSyncJobService.launchService(context);
                        return true;
                    }
                });

                // clean up old HighLevelContentChangeEvents (not those of this processing; due to asynchronous execution)
                rimeInputContentProcessingController.cleanupOldHLCCEs(context, rimeContentAbstractionConfig);
            }
            else {
                LogHelper.w(TAG,"Will not trigger event extraction, because no RIMEContentAbstractionConfig was found");
            }

            // TODO somehting like this should not be possible from this module:
            //new de.lmu.ifi.researchime.contentabstraction.contentanalysers.WordFrequencyCounterJobService();
        }
        else {
            LogHelper.i(TAG,"stopped processing; buffer is empty");
        }

    }

}
