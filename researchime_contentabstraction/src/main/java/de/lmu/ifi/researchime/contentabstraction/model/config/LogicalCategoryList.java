package de.lmu.ifi.researchime.contentabstraction.model.config;

import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.ForeignKeyReference;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

import de.lmu.ifi.researchime.contentabstraction.database.DBFlowDatabase;

/**
 * this class to be similar to the server project's class package models.wordlists.LogicalCategoryList (JSON mappable)
 */
@Table(database = DBFlowDatabase.class, allFields = true)
public class LogicalCategoryList extends BaseModel implements LogicalList {

    @PrimaryKey
    private Long logicallistId;

    private String logicallistName;

    private boolean preappyLemmaExtraction;

    private boolean downloaded = false;

    @ForeignKey(tableClass = RIMEContentAbstractionConfig.class, references = @ForeignKeyReference(columnName = "parentRimeContentAbstractionConfigId", foreignKeyColumnName = "id"))
    private int parentRimeContentAbstractionConfigId;

    public LogicalCategoryList(int parentRimeContentAbstractionConfigId) {
        this.parentRimeContentAbstractionConfigId = parentRimeContentAbstractionConfigId;
    }

    /**
     * for DBFlow only!
     */
    public LogicalCategoryList() {
    }

    public String getLocalFilename(){
        return "word2categorylist-"+getLogicallistId()+".rime";
    }

    public Long getLogicallistId() {
        return logicallistId;
    }

    public void setLogicallistId(Long logicallistId) {
        this.logicallistId = logicallistId;
    }

    public String getLogicallistName() {
        return logicallistName;
    }

    public void setLogicallistName(String logicallistName) {
        this.logicallistName = logicallistName;
    }

    public boolean isPreappyLemmaExtraction() {
        return preappyLemmaExtraction;
    }

    public void setPreappyLemmaExtraction(boolean preappyLemmaExtraction) {
        this.preappyLemmaExtraction = preappyLemmaExtraction;
    }

    public int getParentRimeContentAbstractionConfigId() {
        return parentRimeContentAbstractionConfigId;
    }

    public void setParentRimeContentAbstractionConfigId(int parentRimeContentAbstractionConfigId) {
        this.parentRimeContentAbstractionConfigId = parentRimeContentAbstractionConfigId;
    }

    public boolean isDownloaded() {
        return downloaded;
    }

    public void setDownloaded(boolean downloaded) {
        this.downloaded = downloaded;
    }
}
