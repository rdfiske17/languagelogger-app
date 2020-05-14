package de.lmu.ifi.researchime.contentextraction.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.annotations.Expose;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

import java.util.Date;

import de.lmu.ifi.researchime.contentabstraction.model.abstractedcontent.AbstractedAction;

@Table(database = RimeContentExtractionDbFlowDb.class)
public class AbstractActionEventJson extends BaseModel {

    public static final Gson GSON = new GsonBuilder()
            .excludeFieldsWithoutExposeAnnotation()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
            .create();

    @Expose
    @PrimaryKey
    private Long clientEventId;
    @Expose
    @Column
    private String userUuid;
    @Expose
    @Column
    private String eventJson;
    @Expose
    @Column
    private Long logicalCategoryListId;
    @Expose
    @Column
    private Long regexMatcherId;
    @Expose
    @Column
    private Long messageStatisticsId;
    @Expose
    @Column
    private Date date;
    @Column
    private boolean uploaded;

    public static AbstractActionEventJson fromAbstractActionEvent(AbstractedAction abstractedAction, String userUuid){
        AbstractActionEventJson abstractActionEventJson = new AbstractActionEventJson();
        abstractActionEventJson.clientEventId = abstractedAction.id;
        abstractActionEventJson.userUuid = userUuid;
        abstractActionEventJson.eventJson = GSON.toJson(abstractedAction);
        if (abstractedAction.getLogicalCategoryList() != null) {
            abstractActionEventJson.logicalCategoryListId = abstractedAction.getLogicalCategoryList().getLogicallistId();
        }
        if (abstractedAction.getPatternMatcherConfig() != null) {
            abstractActionEventJson.regexMatcherId = abstractedAction.getPatternMatcherConfig().getRegexMatcherId();
        }
        if (abstractedAction.getMessageStatistics() != null) {
            abstractActionEventJson.messageStatisticsId = abstractedAction.getMessageStatistics().getId();
        }
        abstractActionEventJson.uploaded = false;
        abstractActionEventJson.date = abstractedAction.getDate();
        return abstractActionEventJson;
    }

    public JsonElement getJson(){
        return GSON.toJsonTree(this);
    }

    public String getUserUuid() {
        return userUuid;
    }

    public void setUserUuid(String userUuid) {
        this.userUuid = userUuid;
    }

    public String getEventJson() {
        return eventJson;
    }

    public void setEventJson(String eventJson) {
        this.eventJson = eventJson;
    }

    public Long getLogicalCategoryListId() {
        return logicalCategoryListId;
    }

    public void setLogicalCategoryListId(Long logicalCategoryListId) {
        this.logicalCategoryListId = logicalCategoryListId;
    }

    public boolean isUploaded() {
        return uploaded;
    }

    public void setUploaded(boolean uploaded) {
        this.uploaded = uploaded;
    }

    public Long getClientEventId() {
        return clientEventId;
    }

    public void setClientEventId(Long clientEventId) {
        this.clientEventId = clientEventId;
    }

    public Long getRegexMatcherId() {
        return regexMatcherId;
    }

    public void setRegexMatcherId(Long regexMatcherId) {
        this.regexMatcherId = regexMatcherId;
    }

    public Long getMessageStatisticsId() {
        return messageStatisticsId;
    }

    public void setMessageStatisticsId(Long messageStatisticsId) {
        this.messageStatisticsId = messageStatisticsId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }


}
