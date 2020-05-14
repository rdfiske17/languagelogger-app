package de.lmu.ifi.researchime.contentabstraction;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.raizlabs.android.dbflow.sql.language.SQLite;

import java.util.ArrayList;
import java.util.List;

import de.lmu.ifi.researchime.base.logging.LogHelper;
import de.lmu.ifi.researchime.contentabstraction.model.abstractedcontent.AbstractedAction;
import de.lmu.ifi.researchime.contentabstraction.model.abstractedcontent.AbstractedActionRawContent;
import de.lmu.ifi.researchime.contentabstraction.model.abstractedcontent.AbstractedWordAction;

/**
 * Is called by @see de.lmu.ifi.researchime.contentabstraction.contentanalysers.WordCategorizerJobService when it has finished processing and created
 * some new @see AbstractedAction events
 */
public class OnAbstractedActionEventsReadyBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "OnAbst.Act.Ev.ReadyB.R.";
    public static final String RECEIVER_ACTION = "ON_ABSTRACTED_CONTENT_EVENTS_READY";
    private final RIMECallback rimeCallback;

    /**
     *
     * @param rimeCallback a callback given by the calling module
     */
    public OnAbstractedActionEventsReadyBroadcastReceiver(RIMECallback rimeCallback) {
        this.rimeCallback = rimeCallback;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        LogHelper.i(TAG,"onReceive()");
        List<AbstractedWordAction> abstractedWordActionEvents = SQLite.select().from(AbstractedWordAction.class).queryList();
        List<AbstractedActionRawContent> abstractedRawContentActionEvents = SQLite.select().from(AbstractedActionRawContent.class).queryList();

        List<AbstractedAction> abstractedActionEvents = new ArrayList<>();
        abstractedActionEvents.addAll(abstractedWordActionEvents);
        abstractedActionEvents.addAll(abstractedRawContentActionEvents);
        boolean success = rimeCallback.onAbstractedActionEventsReady(abstractedActionEvents);

        if (success) {
            for (AbstractedAction abstractedAction : abstractedActionEvents){
                abstractedAction.delete();
            }
        }
        else {
            LogHelper.w(TAG, "RIMECallback execution was not successful. Events will be kept for next call.");
        }
    }
}
