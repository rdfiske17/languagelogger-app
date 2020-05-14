package de.lmu.ifi.researchime.contentabstraction.database;

import com.raizlabs.android.dbflow.annotation.Database;

/*
TODO to use the database, the DBFlo
 */

@Database(name = DBFlowDatabase.NAME, version = DBFlowDatabase.VERSION)
public class DBFlowDatabase {

    public static final String NAME = "ResearchIMEContentAbstractionDBFlowDb";
    public static final int VERSION = 2; // increment this every time you change the schema of classes annotated with @Table

}
