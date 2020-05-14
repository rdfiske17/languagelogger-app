package de.lmu.ifi.researchime.contentabstraction.model.config;

import com.raizlabs.android.dbflow.annotation.OneToMany;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.structure.BaseModel;

import java.util.List;

import de.lmu.ifi.researchime.contentabstraction.database.DBFlowDatabase;

/**
 * this class has to be similar to the app module project's models.config.RIMEContentAbstractionConfig
 */
@Table(database = DBFlowDatabase.class)
public class RIMEContentAbstractionConfig extends BaseModel {

    @PrimaryKey(autoincrement = true)
    private int id;

    public List<LogicalCategoryList> logicalCategoryLists;

    public List<LogicalWordList> logicalWordLists;

    public List<PatternMatcherConfig> patternMatcherConfigs;

    @OneToMany(methods = {OneToMany.Method.ALL}, variableName = "logicalCategoryLists")
    public List<LogicalCategoryList> getLogicalCategoryLists() {
        if (logicalCategoryLists == null || logicalCategoryLists.isEmpty()){
            logicalCategoryLists = SQLite.select()
                    .from(LogicalCategoryList.class)
                   // .where(LogicalCategoryList_Table.parentRimeContentAbstractionConfigId.eq(id)) // TODO fix this !
                    .queryList();
        }
        return logicalCategoryLists;
    }

    @OneToMany(methods = {OneToMany.Method.ALL}, variableName = "logicalWordLists")
    public List<LogicalWordList> getLogicalWordLists() {
        if (logicalWordLists == null || logicalWordLists.isEmpty()){
            logicalWordLists = SQLite.select()
                    .from(LogicalWordList.class)
                    //.where(LogicalWordList_Table.parentRimeContentAbstractionConfigId.eq(id)) // TODO fix this !
                    .queryList();
        }
        return logicalWordLists;
    }

    @OneToMany(methods = {OneToMany.Method.ALL}, variableName = "patternMatcherConfigs")
    public List<PatternMatcherConfig> getPatternMatcherConfigs() {
        if (patternMatcherConfigs == null || patternMatcherConfigs.isEmpty()){
            patternMatcherConfigs = SQLite.select()
                    .from(PatternMatcherConfig.class)
                 //   .where(PatternMatcherConfig_Table.parentRimeContentAbstractionConfigId.eq(id)) // TODO fix this !
                    .queryList();
        }
        return patternMatcherConfigs;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
        if (logicalCategoryLists != null) {
            for (LogicalCategoryList logicalCategoryList : logicalCategoryLists) {
                logicalCategoryList.setParentRimeContentAbstractionConfigId(id);
            }
        }
        if (logicalWordLists != null) {
            for (LogicalWordList logicalWordList : logicalWordLists) {
                logicalWordList.setParentRimeContentAbstractionConfigId(id);
            }
        }
        if (patternMatcherConfigs != null) {
            for (PatternMatcherConfig patternMatcherConfig : patternMatcherConfigs) {
                patternMatcherConfig.setParentRimeContentAbstractionConfigId(id);
            }
        }
    }

    public void setLogicalCategoryLists(List<LogicalCategoryList> logicalCategoryLists) {
        this.logicalCategoryLists = logicalCategoryLists;
    }

    public void setLogicalWordLists(List<LogicalWordList> logicalWordLists) {
        this.logicalWordLists = logicalWordLists;
    }

    public void setPatternMatcherConfigs(List<PatternMatcherConfig> patternMatcherConfigs) {
        this.patternMatcherConfigs = patternMatcherConfigs;
    }
}
