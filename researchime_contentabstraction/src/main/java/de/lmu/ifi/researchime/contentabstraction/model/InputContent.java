package de.lmu.ifi.researchime.contentabstraction.model;

import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.OneToMany;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.structure.BaseModel;

import java.util.List;

import de.lmu.ifi.researchime.contentabstraction.database.DBFlowDatabase;
import de.lmu.ifi.researchime.contentabstraction.model.rawcontent.ContentChangeEvent;
import de.lmu.ifi.researchime.contentabstraction.model.rawcontent.ContentChangeEvent_Table;

/**
 * consists of a set of ContentChangeEvents, that occurred during one keyboard usage "session"
 */
@Table(database = DBFlowDatabase.class)
public class InputContent extends BaseModel {

    @PrimaryKey(autoincrement = true)
    private long id;

    public List<ContentChangeEvent> contentChangeEvents;

    @ForeignKey
    private MessageStatistics messageStatistics;

    @OneToMany(methods = {OneToMany.Method.ALL}, variableName = "contentChangeEvents")
    public List<ContentChangeEvent> getContentChangeEvents() {
        if (contentChangeEvents == null || contentChangeEvents.isEmpty()){
            contentChangeEvents = SQLite.select()
                    .from(ContentChangeEvent.class)
                    .where(ContentChangeEvent_Table.parentInputContentId.eq(id))
                    .queryList();
        }
        return contentChangeEvents;
    }

    public void setContentChangeEvents(List<ContentChangeEvent> contentChangeEvents) {
        this.contentChangeEvents = contentChangeEvents;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public MessageStatistics getMessageStatistics() {
        return messageStatistics;
    }

    public void setMessageStatistics(MessageStatistics messageStatistics) {
        this.messageStatistics = messageStatistics;
    }
}
