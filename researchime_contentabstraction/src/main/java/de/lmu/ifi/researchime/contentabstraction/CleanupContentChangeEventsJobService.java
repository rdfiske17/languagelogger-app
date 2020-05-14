package de.lmu.ifi.researchime.contentabstraction;

import android.app.job.JobParameters;
import android.app.job.JobService;

import com.raizlabs.android.dbflow.list.FlowCursorIterator;
import com.raizlabs.android.dbflow.sql.language.SQLite;

import java.util.ArrayList;
import java.util.List;

import de.lmu.ifi.researchime.base.logging.LogHelper;
import de.lmu.ifi.researchime.contentabstraction.model.rawcontent.ContentChangeEvent;

/**
 * Expects bundle with the following long arrays:
 *    * logicalCategoryListIds
 *    * logicalWordListIds
 *    * patternMatcherIds
 * For a ContentChangeEvent to be deleted, it has to be marked as processed by all those configs
 */
public class CleanupContentChangeEventsJobService extends JobService {

    public static final String TAG = "CleanupContentChangeEventsJobService";

    @Override
    public boolean onStartJob(JobParameters params) {
        LogHelper.i(TAG,"onStartJob()");

        long[] logicalCategoryListIds = params.getExtras().getLongArray("logicalCategoryListIds");
        List<Long> logicalCategoryListIdsList = new ArrayList<>();
        for(long id: logicalCategoryListIds){
            logicalCategoryListIdsList.add(id);
        }

        long[] logicalWordListIds = params.getExtras().getLongArray("logicalWordListIds");
        List<Long> logicalWordListIdsList = new ArrayList<>();
        for(long id: logicalWordListIds){
            logicalWordListIdsList.add(id);
        }

        long[] patternMatcherIds = params.getExtras().getLongArray("patternMatcherIds");
        List<Long> patternMatcherIdsList = new ArrayList<>();
        for(long id : patternMatcherIds){
            patternMatcherIdsList.add(id);
        }

        FlowCursorIterator<ContentChangeEvent> cursor = SQLite.select()
                .from(ContentChangeEvent.class)
                .queryResults().iterator();

        int deletionCounter = 0;
        while(cursor.hasNext()){
            ContentChangeEvent contentChangeEvent = cursor.next();
            // check if this event has been processed by all lists and matchers
            if(!contentChangeEvent.getProcessedByLogicalCategoryListIds().containsAll(logicalCategoryListIdsList)){
                continue;
            }
            if(!contentChangeEvent.getProcessedByWhitelistCounterIds().containsAll(logicalWordListIdsList)){
                continue;
            }
            if(!contentChangeEvent.getProcessedByPatternMatcherIds().containsAll(patternMatcherIdsList)){
                continue;
            }

            contentChangeEvent.delete();
            deletionCounter++;
        }
        LogHelper.i(TAG,"deleted "+deletionCounter+" ContentChangeEvents");

        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }
}
