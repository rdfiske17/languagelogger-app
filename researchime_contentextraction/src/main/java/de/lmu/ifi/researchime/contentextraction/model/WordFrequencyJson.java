package de.lmu.ifi.researchime.contentextraction.model;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.annotations.Expose;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

import de.lmu.ifi.researchime.contentabstraction.model.abstractedcontent.WordFrequency;

@Table(database = RimeContentExtractionDbFlowDb.class)
public class WordFrequencyJson extends BaseModel {

    private static final Gson gson = new Gson();

    @Expose
    @PrimaryKey
    private Long clientEventId;
    @Expose
    @Column
    private String userUuid;
    @Expose
    @Column
    private String word;
    @Expose
    @Column
    private Long logicalWordListId;
    @Expose
    @Column
    private Integer count;

    public static WordFrequencyJson fromWordFrequency(WordFrequency wordFrequency, String userUuid){
        WordFrequencyJson wordFrequencyJson = new WordFrequencyJson();
        wordFrequencyJson.word = wordFrequency.getWord();
        wordFrequencyJson.userUuid = userUuid;
        wordFrequencyJson.logicalWordListId = wordFrequency.getLogicalWordList().getLogicallistId();
        wordFrequencyJson.count = wordFrequency.getCount();
        return wordFrequencyJson;
    }

    public JsonElement getJson(){
        return gson.toJsonTree(this);
    }

    public Long getClientEventId() {
        return clientEventId;
    }

    public void setClientEventId(Long clientEventId) {
        this.clientEventId = clientEventId;
    }

    public String getUserUuid() {
        return userUuid;
    }

    public void setUserUuid(String userUuid) {
        this.userUuid = userUuid;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public Long getLogicalWordListId() {
        return logicalWordListId;
    }

    public void setLogicalWordListId(Long logicalWordListId) {
        this.logicalWordListId = logicalWordListId;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }


}
