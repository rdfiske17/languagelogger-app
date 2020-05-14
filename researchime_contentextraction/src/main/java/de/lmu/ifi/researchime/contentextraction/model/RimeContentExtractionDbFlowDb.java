package de.lmu.ifi.researchime.contentextraction.model;

import com.raizlabs.android.dbflow.annotation.Database;

/*
TODO to use the database, the DBFlo
 */

@Database(name = RimeContentExtractionDbFlowDb.NAME, version = RimeContentExtractionDbFlowDb.VERSION)
public class RimeContentExtractionDbFlowDb {

    public static final String NAME = "ResearchIMEContentExtractionDBFlowDb";
    public static final int VERSION = 2; // increment this every time you change the schema of classes annotated with @Table

}
