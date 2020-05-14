package de.lmu.ifi.researchime.contentextraction;

import android.content.Context;

import com.raizlabs.android.dbflow.config.FlowConfig;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.config.RimeContentextractionGeneratedDatabaseHolder;

import de.lmu.ifi.researchime.contentextraction.model.RimeContentExtractionDbFlowDb;

public class DbInitializer {

    public static void initRimeContentExtractionDb(Context context){
        FlowConfig flowConfig2 = new FlowConfig.Builder(context)
                .addDatabaseHolder(RimeContentextractionGeneratedDatabaseHolder.class)
                .openDatabasesOnInit(true)
                .build();
        FlowManager.init(flowConfig2);
     //   FlowManager.getDatabase(RimeContentExtractionDbFlowDb.class).getWritableDatabase();
    }

}
