package de.lmu.ifi.researchime.contentabstraction.model.config;

import com.raizlabs.android.dbflow.annotation.ColumnIgnore;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.ForeignKeyReference;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;

import de.lmu.ifi.researchime.contentabstraction.contentanalysers.patternmatching.RegexPatternMatcher;
import de.lmu.ifi.researchime.contentabstraction.contentanalysers.patternmatching.PatternMatcher;
import de.lmu.ifi.researchime.contentabstraction.database.DBFlowDatabase;

@Table(database = DBFlowDatabase.class, allFields = true)
public class PatternMatcherConfig {

    @PrimaryKey
    private Long regexMatcherId;

    @ForeignKey(tableClass = RIMEContentAbstractionConfig.class, references = @ForeignKeyReference(columnName = "parentRimeContentAbstractionConfigId", foreignKeyColumnName = "id"))
    private int parentRimeContentAbstractionConfigId;

    private String regexMatcherName;

    private boolean logRawContent;

    private String regex;

    @ColumnIgnore
    private PatternMatcher patternMatcherInstance = null;


    public PatternMatcherConfig(int parentRimeContentAbstractionConfigId) {
        this.parentRimeContentAbstractionConfigId = parentRimeContentAbstractionConfigId;
    }

    /**
     * for DBFlow only!
     */
    public PatternMatcherConfig() {
    }

    public PatternMatcher createPatternMatcherInstance(){
        if (patternMatcherInstance == null) {
            patternMatcherInstance = new RegexPatternMatcher(regex);
        }
        return patternMatcherInstance;
    }

    public int getParentRimeContentAbstractionConfigId() {
        return parentRimeContentAbstractionConfigId;
    }

    public void setParentRimeContentAbstractionConfigId(int parentRimeContentAbstractionConfigId) {
        this.parentRimeContentAbstractionConfigId = parentRimeContentAbstractionConfigId;
    }

    public boolean isLogRawContent() {
        return logRawContent;
    }

    public void setLogRawContent(boolean logRawContent) {
        this.logRawContent = logRawContent;
    }

    public Long getRegexMatcherId() {
        return regexMatcherId;
    }

    public void setRegexMatcherId(Long regexMatcherId) {
        this.regexMatcherId = regexMatcherId;
    }

    public String getRegex() {
        return regex;
    }

    public void setRegex(String regex) {
        this.regex = regex;
    }

    public String getRegexMatcherName() {
        return regexMatcherName;
    }

    public void setRegexMatcherName(String regexMatcherName) {
        this.regexMatcherName = regexMatcherName;
    }
}
