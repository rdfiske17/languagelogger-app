package de.lmu.ifi.researchime.contentabstraction.model.abstractedcontent;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

import de.lmu.ifi.researchime.contentabstraction.database.DBFlowDatabase;
import de.lmu.ifi.researchime.contentabstraction.model.config.LogicalWordList;

/**
 * this class has to match the equivalent in the backend project!
 */
@Table(database = DBFlowDatabase.class)
public class WordFrequency extends BaseModel {

    @PrimaryKey
    @Column
    private String word;

    @Column
    private Integer count;

    @Column
    @ForeignKey
    protected LogicalWordList logicalWordList;

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public void increaseCountBy(Integer increaseBy){
        this.count += increaseBy;
    }

    public LogicalWordList getLogicalWordList() {
        return logicalWordList;
    }

    public void setLogicalWordList(LogicalWordList logicalWordList) {
        this.logicalWordList = logicalWordList;
    }
}
