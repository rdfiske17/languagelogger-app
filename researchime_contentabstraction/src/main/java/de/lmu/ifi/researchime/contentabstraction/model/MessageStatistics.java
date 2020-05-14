package de.lmu.ifi.researchime.contentabstraction.model;

import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

import de.lmu.ifi.researchime.contentabstraction.database.DBFlowDatabase;

@Table(database = DBFlowDatabase.class)
public class MessageStatistics extends BaseModel {

    @PrimaryKey(autoincrement = true)
    private long id;

    private Integer characterCountAdded;

    private Integer characterCountAltered;

    private Integer characterCountSubmitted;

    private String inputTargetApp;

    private Long timestampTypeStart;

    private Long timestampTypeEnd;

    private String fieldHintText;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Integer getCharacterCountAdded() {
        return characterCountAdded;
    }

    public void setCharacterCountAdded(Integer characterCountAdded) {
        this.characterCountAdded = characterCountAdded;
    }

    public Integer getCharacterCountAltered() {
        return characterCountAltered;
    }

    public void setCharacterCountAltered(Integer characterCountAltered) {
        this.characterCountAltered = characterCountAltered;
    }

    public Integer getCharacterCountSubmitted() {
        return characterCountSubmitted;
    }

    public void setCharacterCountSubmitted(Integer characterCountSubmitted) {
        this.characterCountSubmitted = characterCountSubmitted;
    }

    public String getInputTargetApp() {
        return inputTargetApp;
    }

    public void setInputTargetApp(String inputTargetApp) {
        this.inputTargetApp = inputTargetApp;
    }

    public Long getTimestampTypeStart() {
        return timestampTypeStart;
    }

    public void setTimestampTypeStart(Long timestampTypeStart) {
        this.timestampTypeStart = timestampTypeStart;
    }

    public Long getTimestampTypeEnd() {
        return timestampTypeEnd;
    }

    public void setTimestampTypeEnd(Long timestampTypeEnd) {
        this.timestampTypeEnd = timestampTypeEnd;
    }

    public String getFieldHintText() {
        return fieldHintText;
    }

    public void setFieldHintText(String fieldHintText) {
        this.fieldHintText = fieldHintText;
    }
}
