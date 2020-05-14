package de.lmu.ifi.researchime.contentabstraction.model.abstractedcontent;

import com.google.gson.annotations.Expose;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.Table;

import de.lmu.ifi.researchime.contentabstraction.database.DBFlowDatabase;
import de.lmu.ifi.researchime.contentabstraction.model.ContentUnitEventType;
import de.lmu.ifi.researchime.contentabstraction.model.rawcontent.ContentChangeEvent;

@Table(database = DBFlowDatabase.class)
public class AbstractedWordAction extends AbstractedAction {

    // categoized fields are only available in AbstractedWordAction
    @Expose
    @Column
    private String categoryBefore;
    @Expose
    @Column
    private String categoryAfter;

    /**
     * for DBFlow only!
     */
    public AbstractedWordAction() {
        super(null);
    }

    public AbstractedWordAction(ContentUnitEventType type, String categoryBefore, String categoryAfter) {
        this(type,categoryBefore,categoryAfter,null);
    }

    public AbstractedWordAction(ContentUnitEventType type, String categoryBefore, String categoryAfter, ContentChangeEvent contentChangeEvent) {
        super(type, contentChangeEvent);

        if (type == ContentUnitEventType.ADDED && (categoryBefore != null || categoryAfter == null)) {
            throw new IllegalArgumentException("cannot create a AbstractedWordAction event of type ADDED, specifying a categoryBefore value or no categoryAfter value");
        }
        if (type == ContentUnitEventType.CHANGED && (categoryBefore == null || categoryAfter == null)) {
            throw new IllegalArgumentException("cannot create a AbstractedWordAction event of type CHANGED, that does not specify both categoryBefore and categoryAfter");
        }
        if (type == ContentUnitEventType.REMOVED && (categoryAfter != null || categoryBefore == null)) {
            throw new IllegalArgumentException("cannot create a AbstractedWordAction event of type REMOVED, specifying a categoryAfter value or no categoryBefore value");
        }

        this.categoryBefore = categoryBefore;
        this.categoryAfter = categoryAfter;
    }

    public String getAddedWordCategory(){
        if (contentUnitEventType != ContentUnitEventType.ADDED) {
            throw new IllegalArgumentException("you cannot call getAddedWordCategory() on an action contentUnitEventType of type "+ contentUnitEventType);
        }
        return categoryAfter;
    }

    public String getRemovedWordCategory(){
        if (contentUnitEventType != ContentUnitEventType.REMOVED) {
            throw new IllegalArgumentException("you cannot call getRemovedWordCategory() on an action contentUnitEventType of type "+ contentUnitEventType);
        }
        return categoryBefore;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(contentUnitEventType).append("[");
        if (contentUnitEventType == ContentUnitEventType.ADDED){
            if (contentChangeEvent != null) {
                stringBuilder.append("\"").append(contentChangeEvent.getAddedWord().getAsString()).append("\"").append("/");
            }
            stringBuilder.append(getAddedWordCategory());
        }
        else if (contentUnitEventType == ContentUnitEventType.CHANGED){
            if (contentChangeEvent != null) {
                stringBuilder.append("\"").append(contentChangeEvent.getContentUnitBefore().getAsString()).append("\"").append("/");
            }
            stringBuilder.append(categoryBefore);
            stringBuilder.append("->");
            if (contentChangeEvent != null) {
                stringBuilder.append("\"").append(contentChangeEvent.getContentUnitAfter().getAsString()).append("\"").append("/");
            }
            stringBuilder.append(categoryAfter);
        }
        else if (contentUnitEventType == ContentUnitEventType.REMOVED){
            if (contentChangeEvent != null) {
                stringBuilder.append("\"").append(contentChangeEvent.getRemovedWord().getAsString()).append("\"").append("/");
            }
            stringBuilder.append(getRemovedWordCategory());
        }
        stringBuilder.append("] ; ");
        return stringBuilder.toString();
    }

    public String getCategoryBefore() {
        return categoryBefore;
    }

    public void setCategoryBefore(String categoryBefore) {
        this.categoryBefore = categoryBefore;
    }

    public String getCategoryAfter() {
        return categoryAfter;
    }

    public void setCategoryAfter(String categoryAfter) {
        this.categoryAfter = categoryAfter;
    }
}
