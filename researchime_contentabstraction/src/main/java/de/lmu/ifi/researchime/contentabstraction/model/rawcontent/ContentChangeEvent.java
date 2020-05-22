package de.lmu.ifi.researchime.contentabstraction.model.rawcontent;

//import androidx.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.raizlabs.android.dbflow.StringUtils;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.ForeignKeyReference;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.lmu.ifi.researchime.contentabstraction.database.DBFlowDatabase;
import de.lmu.ifi.researchime.contentabstraction.model.ContentUnitEventType;
import de.lmu.ifi.researchime.contentabstraction.model.InputContent;
import de.lmu.ifi.researchime.contentabstraction.model.MessageStatistics;

/**
 * a high level event combines multiple input events. E.g. the input events "t","h","e" might be
 * combined to a highlevel event "the" (word add)
 *
 */
@Table(database = DBFlowDatabase.class)
public class ContentChangeEvent extends BaseModel {

    @PrimaryKey(autoincrement = true)
    long id;
    @ForeignKey
    private ContentUnit contentUnitBefore;
    @ForeignKey
    private ContentUnit contentUnitAfter;
    @Column
    private ContentUnitEventType type;
    @ForeignKey
    private MessageStatistics messageStatistics;
    @Column
    private Date date;

    // meta: lists of ids, saving for which configuration this event was already processed
    @Column
    public String processedByWhitelistCounter = "";

    @Column
    public String processedByPatternmatcher = "";

    @Column
    public String processedByCategorizer = "";

    @ForeignKey(tableClass = InputContent.class, references = @ForeignKeyReference(columnName = "parentInputContentId", foreignKeyColumnName = "id"))
    private Long parentInputContentId;

    public List<Long> getProcessedByLogicalCategoryListIds(){
        if (StringUtils.isNullOrEmpty(processedByCategorizer)){
            return new ArrayList<>();
        }
        String[] stringIds = processedByCategorizer.split(",");
        List<Long> logicalCategoryListIds = new ArrayList<>();
        for(String stringId : stringIds){
            logicalCategoryListIds.add(Long.valueOf(stringId));
        }
        return logicalCategoryListIds;
    }

    public void setProcessedByLogicalCategoryListIds(List<Long> logicalCategoryListIds){
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i<logicalCategoryListIds.size(); i++){
            stringBuilder.append(logicalCategoryListIds.get(i));
            if (i < logicalCategoryListIds.size()-1){
                stringBuilder.append(",");
            }
        }
        this.processedByCategorizer = stringBuilder.toString();
        this.update();
    }

    public List<Long> getProcessedByWhitelistCounterIds(){
        if (StringUtils.isNullOrEmpty(processedByWhitelistCounter)){
            return new ArrayList<>();
        }
        String[] stringIds = processedByWhitelistCounter.split(",");
        List<Long> whitelistCounterIds = new ArrayList<>();
        for(String stringId : stringIds){
            whitelistCounterIds.add(Long.valueOf(stringId));
        }
        return whitelistCounterIds;
    }

    public void setProcessedByWhitelistCounterIds(List<Long> whitelistCounterIds){
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i<whitelistCounterIds.size(); i++){
            stringBuilder.append(whitelistCounterIds.get(i));
            if (i < whitelistCounterIds.size()-1){
                stringBuilder.append(",");
            }
        }
        this.processedByWhitelistCounter = stringBuilder.toString();
        this.update();
    }

    public List<Long> getProcessedByPatternMatcherIds(){
        if (StringUtils.isNullOrEmpty(processedByPatternmatcher)){
            return new ArrayList<>();
        }
        String[] stringIds = processedByPatternmatcher.split(",");
        List<Long> patternMatcherIds = new ArrayList<>();
        for(String stringId : stringIds){
            patternMatcherIds.add(Long.valueOf(stringId));
        }
        return patternMatcherIds;
    }

    public void setProcessedByPatternMatcherIds(List<Long> patternMatcherIds){
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i<patternMatcherIds.size(); i++){
            stringBuilder.append(patternMatcherIds.get(i));
            if (i < patternMatcherIds.size()-1){
                stringBuilder.append(",");
            }
        }
        this.processedByPatternmatcher = stringBuilder.toString();
        this.update();
    }



    /**
     * for DBFlow only!
     */
    public ContentChangeEvent(){

    }

    public ContentChangeEvent(InputContent inputContent, ContentUnitEventType type, ContentUnit contentUnitBefore, ContentUnit contentUnitAfter, Long timestamp) {

        // TODO similar code as in AbstractedWordAction and AbstractedRawContent
        if (type == ContentUnitEventType.ADDED && (contentUnitBefore != null || contentUnitAfter == null)) {
            throw new IllegalArgumentException("cannot create a ContentChangeEvent event of type ADDED, specifying a categoryBefore value or no categoryAfter value");
        }
        if (type == ContentUnitEventType.CHANGED && (contentUnitBefore == null || contentUnitAfter == null)) {
            throw new IllegalArgumentException("cannot create a ContentChangeEvent event of type CHANGED, that does not specify both categoryBefore and categoryAfter");
        }
        if (type == ContentUnitEventType.REMOVED && (contentUnitAfter != null || contentUnitBefore == null)) {
            throw new IllegalArgumentException("cannot create a ContentChangeEvent event of type REMOVED, specifying a categoryAfter value or no categoryBefore value");
        }
        if (type == ContentUnitEventType.CONTAINED && (contentUnitBefore != null || contentUnitAfter == null)) {
            throw new IllegalArgumentException("cannot create a ContentChangeEvent event of type CONTAINED, specifying a categoryBefore value or no categoryAfter value");
        }
        if (type == ContentUnitEventType.SPLITTED && (contentUnitBefore == null || contentUnitAfter == null)) {
            throw new IllegalArgumentException("cannot create a ContentChangeEvent event of type SPLITTED, specifying a categoryBefore value or no categoryAfter value");
        }
        if (type == ContentUnitEventType.JOINED && (contentUnitBefore == null || contentUnitAfter == null)) {
            throw new IllegalArgumentException("cannot create a ContentChangeEvent event of type JOINED, specifying a categoryBefore value or no categoryAfter value");
        }

        this.contentUnitBefore = contentUnitBefore;
        this.contentUnitAfter = contentUnitAfter;
        this.messageStatistics = inputContent.getMessageStatistics();
        this.type = type;
        this.parentInputContentId = inputContent.getId();
        this.date = new Date(timestamp);
    }

    public boolean isWordAddedEvent(){
        return type == ContentUnitEventType.ADDED;
    }

    public ContentUnit getAddedWord(){
        if (!isWordAddedEvent()){
            throw new IllegalArgumentException("you cannot call getAddedWord on a Event of type "+type);
        }
        return getContentUnitAfter();
    }

    public boolean isWordChangedEvent(){
        return type == ContentUnitEventType.CHANGED;
    }

    public boolean isWordRemovedEvent(){
        return type == ContentUnitEventType.REMOVED;
    }

    public boolean isWordContainedEvent() {
        return type == ContentUnitEventType.CONTAINED;
    }

    public boolean isWordSplittedEvent() {
        return type == ContentUnitEventType.SPLITTED;
    }

    public boolean isWordJoinedEvent() {
        return type == ContentUnitEventType.JOINED;
    }

    public ContentUnit getRemovedWord(){
        if (!isWordRemovedEvent()){
            throw new IllegalArgumentException("you cannot call getRemovedWord on a Event of type "+type);
        }
        return getContentUnitBefore();
    }

    // --- standard getter and setter ---

    public ContentUnit getContentUnitBefore() {
        return contentUnitBefore;
    }

    public void setContentUnitBefore(ContentUnit contentUnitBefore) {
        this.contentUnitBefore = contentUnitBefore;
    }

    public ContentUnit getContentUnitAfter() {
        return contentUnitAfter;
    }

    public void setContentUnitAfter(ContentUnit contentUnitAfter) {
        this.contentUnitAfter = contentUnitAfter;
    }

    public ContentUnitEventType getType() {
        return type;
    }

    public void setType(ContentUnitEventType type) {
        this.type = type;
    }

    public void setProcessedByCategorizer(Long logicalListId) {
        List<Long> processedByIds = getProcessedByLogicalCategoryListIds();
        processedByIds.add(logicalListId);
        setProcessedByLogicalCategoryListIds(processedByIds);
    }

    public void setProcessedByWhitelistCounter(Long logicalWordlistId) {
        List<Long> processedByIds = getProcessedByWhitelistCounterIds();
        processedByIds.add(logicalWordlistId);
        setProcessedByWhitelistCounterIds(processedByIds);
    }

    public void setProcessedByPatternmatcher(Long  patternMatcherId) {
        List<Long> processedByIds = getProcessedByPatternMatcherIds();
        processedByIds.add(patternMatcherId);
        setProcessedByPatternMatcherIds(processedByIds);
    }

    public Long getParentInputContentId() {
        return parentInputContentId;
    }

    public void setParentInputContentId(Long parentInputContentId) {
        this.parentInputContentId = parentInputContentId;
    }

    public MessageStatistics getMessageStatistics() {
        return messageStatistics;
    }

    public void setMessageStatistics(MessageStatistics messageStatistics) {
        this.messageStatistics = messageStatistics;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

   // @NonNull
    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("ContentChangeEvent "+this.hashCode());
        stringBuilder.append("\ncontentUnitBefore="+(contentUnitBefore != null ? contentUnitBefore.toString() : "null"));
        stringBuilder.append("\ncontentUnitAfter="+(contentUnitAfter != null ? contentUnitAfter.toString() : "null"));
        stringBuilder.append("\ntpye="+(type != null ? type.toString() : "null"));
        stringBuilder.append("\n=messageStatistics"+(messageStatistics != null ? messageStatistics.toString() : "null"));
        return stringBuilder.toString();
    }
}
