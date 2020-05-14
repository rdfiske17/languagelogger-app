package de.lmu.ifi.researchime.contentabstraction.model.rawcontent;

import com.google.gson.annotations.Expose;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

import de.lmu.ifi.researchime.contentabstraction.database.DBFlowDatabase;

@Table(database = DBFlowDatabase.class)
public class ContentUnit extends BaseModel {

    @PrimaryKey(autoincrement = true)
    long id;

    @Expose
    @Column
    private String content;

    /**
     * for DBFlow only!
     */
    public ContentUnit() {
    }

    public ContentUnit(String content) {
        this.content = content;
        save();
    }

    public String getAsString(){
        return content;
    }

    @Override
    public String toString() {
        return content;
    }

    // ----- for DBFlow -----
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
