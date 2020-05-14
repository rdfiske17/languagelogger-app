package de.lmu.ifi.researchime.contentabstraction;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.raizlabs.android.dbflow.sql.language.SQLite;

import java.util.List;

import de.lmu.ifi.researchime.base.logging.LogHelper;
import de.lmu.ifi.researchime.contentabstraction.model.abstractedcontent.WordFrequency;

/**
 * Is called by @see de.lmu.ifi.researchime.contentabstraction.contentanalysers.WordFrequencyCounterJobService when it has finished processing and
 * the word frequencies thereby were updated
 */
public class OnWordFrequenciesChangedBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "OnWordFreq.Changed.B.R.";
    public static final String RECEIVER_ACTION = "ON_WORD_FREQ_CHAGED";
    private final RIMECallback rimeCallback;

    public OnWordFrequenciesChangedBroadcastReceiver(RIMECallback rimeCallback) {
        this.rimeCallback = rimeCallback;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        LogHelper.i(TAG,"onReceive()");
        List<WordFrequency> wordFrequencies = SQLite.select().from(WordFrequency.class).queryList();

        boolean success = rimeCallback.onWordFrequenciesChanged(wordFrequencies);

        if (success) {

        }
        else {
            LogHelper.w(TAG, "RIMECallback word frequencies execution was not successful.");
        }
    }
}
