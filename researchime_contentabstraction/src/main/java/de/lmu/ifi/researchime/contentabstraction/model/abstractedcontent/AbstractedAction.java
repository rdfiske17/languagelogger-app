package de.lmu.ifi.researchime.contentabstraction.model.abstractedcontent;

import com.google.gson.annotations.Expose;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.structure.BaseModel;

import java.util.Date;
import java.util.List;

import de.lmu.ifi.researchime.contentabstraction.model.ContentUnitEventType;
import de.lmu.ifi.researchime.contentabstraction.model.MessageStatistics;
import de.lmu.ifi.researchime.contentabstraction.model.config.LogicalCategoryList;
import de.lmu.ifi.researchime.contentabstraction.model.config.PatternMatcherConfig;
import de.lmu.ifi.researchime.contentabstraction.model.rawcontent.ContentChangeEvent;

/**
 * the item which is finally logged. "The user added a positive emotion word"
 *
 * subclasses of this are DBFlow tables
 *
 * must correspond to AbstractedActionEvent class in backend (flattened)
 */
public abstract class AbstractedAction extends BaseModel {

    @PrimaryKey(autoincrement = true)
    public long id;

    private static final String TAG = "AbstractedAction";

    @Expose
    @Column
    protected ContentUnitEventType contentUnitEventType;

    @Column
    protected Date date;

    // optionale Referenz auf Wort-Event
    @Column
    @ForeignKey
    protected ContentChangeEvent contentChangeEvent;

    @Column
    @ForeignKey
    protected LogicalCategoryList logicalCategoryList;

    @Column
    @ForeignKey
    protected PatternMatcherConfig patternMatcherConfig;

    @Column
    @ForeignKey
    protected MessageStatistics messageStatistics;


    protected AbstractedAction(ContentUnitEventType type){
        this(type,null);
    }

    /**
     *
     * @param type
     * @param contentChangeEvent  optional reference to the original event, before the categorization
     */
    protected AbstractedAction(ContentUnitEventType type, ContentChangeEvent contentChangeEvent){
        this.contentUnitEventType = type;
        this.contentChangeEvent = contentChangeEvent;
        if (contentChangeEvent != null) {
            this.date = contentChangeEvent.getDate();
            this.messageStatistics = contentChangeEvent.getMessageStatistics();
        }
    }



    // --- standard getter and setter ---



    public ContentUnitEventType getContentUnitEventType() {
        return contentUnitEventType;
    }

    public ContentChangeEvent getContentChangeEvent() {
        return contentChangeEvent;
    }

    public void setContentChangeEvent(ContentChangeEvent contentChangeEvent) {
        this.contentChangeEvent = contentChangeEvent;
    }

    public void setContentUnitEventType(ContentUnitEventType contentUnitEventType) {
        this.contentUnitEventType = contentUnitEventType;
    }

    public LogicalCategoryList getLogicalCategoryList() {
        return logicalCategoryList;
    }

    public void setLogicalCategoryList(LogicalCategoryList logicalCategoryList) {
        this.logicalCategoryList = logicalCategoryList;
    }

    public PatternMatcherConfig getPatternMatcherConfig() {
        return patternMatcherConfig;
    }

    public void setPatternMatcherConfig(PatternMatcherConfig patternMatcherConfig) {
        this.patternMatcherConfig = patternMatcherConfig;
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
}
