package de.lmu.ifi.researchime.contentabstraction.model.abstractedcontent;


import com.google.gson.annotations.Expose;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.Table;

import de.lmu.ifi.researchime.contentabstraction.database.DBFlowDatabase;
import de.lmu.ifi.researchime.contentabstraction.model.ContentUnitEventType;
import de.lmu.ifi.researchime.contentabstraction.model.rawcontent.ContentChangeEvent;

@Table(database = DBFlowDatabase.class)
public class AbstractedActionRawContent extends AbstractedAction {

    // fields for raw-content only in EomjiAction and PunctuationAction
    @Expose
    @Column
    private String rawContentBefore;
    @Expose
    @Column
    private String rawContentAfter;

    /**
     * for dbflow only!
     */
    public AbstractedActionRawContent() {
        super(null);
    }

    public AbstractedActionRawContent(ContentUnitEventType type, String rawContentBefore, String rawContentAfter, ContentChangeEvent contentChangeEvent) {
        super(type, contentChangeEvent);

        // TODO similar code as in AbstractedWordAction - needs multi-inheritance ;)
        if (type == ContentUnitEventType.ADDED && (rawContentBefore != null || rawContentAfter == null)) {
            throw new IllegalArgumentException("cannot create a AbstractedActionRawContent event of type ADDED, specifying a categoryBefore value or no categoryAfter value");
        }
        if (type == ContentUnitEventType.CHANGED && (rawContentBefore == null || rawContentAfter == null)) {
            throw new IllegalArgumentException("cannot create a AbstractedActionRawContent event of type CHANGED, that does not specify both categoryBefore and categoryAfter");
        }
        if (type == ContentUnitEventType.REMOVED && (rawContentAfter != null || rawContentBefore == null)) {
            throw new IllegalArgumentException("cannot create a AbstractedActionRawContent event of type REMOVED, specifying a categoryAfter value or no categoryBefore value");
        }

        this.rawContentBefore = rawContentBefore;
        this.rawContentAfter = rawContentAfter;
        this.messageStatistics = contentChangeEvent.getMessageStatistics();
    }

    public String getRawContentBefore() {
        return rawContentBefore;
    }

    public void setRawContentBefore(String rawContentBefore) {
        this.rawContentBefore = rawContentBefore;
    }

    public String getRawContentAfter() {
        return rawContentAfter;
    }

    public void setRawContentAfter(String rawContentAfter) {
        this.rawContentAfter = rawContentAfter;
    }
}
