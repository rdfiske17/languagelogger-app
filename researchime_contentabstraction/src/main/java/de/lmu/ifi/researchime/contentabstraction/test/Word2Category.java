package de.lmu.ifi.researchime.contentabstraction.test;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

import de.lmu.ifi.researchime.contentabstraction.database.DBFlowDatabase;
@Table(database = DBFlowDatabase.class)
public class Word2Category extends BaseModel {

        @Column
        @PrimaryKey
       private String word;

        @Column
       private String category;

    public Word2Category() {
    }

    public Word2Category(String word, String category) {
        this.word = word;
        this.category = category;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
