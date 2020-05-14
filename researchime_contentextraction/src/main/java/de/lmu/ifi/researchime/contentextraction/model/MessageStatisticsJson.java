package de.lmu.ifi.researchime.contentextraction.model;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.annotations.Expose;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

import de.lmu.ifi.researchime.contentabstraction.model.MessageStatistics;

@Table(database = RimeContentExtractionDbFlowDb.class)
public class MessageStatisticsJson extends BaseModel {

    private static final Gson gson = new Gson();

    @Expose
    @PrimaryKey
    private Long clientEventId;
    @Expose
    @Column
    private String userUuid;
    @Expose
    @Column
    private Integer characterCountAdded;
    @Expose
    @Column
    private Integer characterCountAltered;
    @Expose
    @Column
    private Integer characterCountSubmitted;
    @Expose
    @Column
    private String inputTargetApp;
    @Expose
    @Column
    private Long timestampTypeStart;
    @Expose
    @Column
    private Long timestampTypeEnd;
    @Expose
    @Column
    private String fieldHintText;
    @Column
    private boolean uploaded = false;


    public static MessageStatisticsJson fromMessageStatistics(MessageStatistics messageStatistics, String userUuid){
        MessageStatisticsJson messageStatisticsJson = new MessageStatisticsJson();
        messageStatisticsJson.clientEventId = messageStatistics.getId();
        messageStatisticsJson.userUuid = userUuid;
        messageStatisticsJson.characterCountAdded = messageStatistics.getCharacterCountAdded();
        messageStatisticsJson.characterCountAltered = messageStatistics.getCharacterCountAltered();
        messageStatisticsJson.characterCountSubmitted = messageStatistics.getCharacterCountSubmitted();
        messageStatisticsJson.inputTargetApp = messageStatistics.getInputTargetApp();
        messageStatisticsJson.timestampTypeStart = messageStatistics.getTimestampTypeStart();
        messageStatisticsJson.timestampTypeEnd = messageStatistics.getTimestampTypeEnd();
        messageStatisticsJson.fieldHintText = messageStatistics.getFieldHintText();
        messageStatisticsJson.uploaded = false;
        return messageStatisticsJson;
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

    public boolean isUploaded() {
        return uploaded;
    }

    public void setUploaded(boolean uploaded) {
        this.uploaded = uploaded;
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
