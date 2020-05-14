package de.lmu.ifi.researchime.contentabstraction.model.config;

public interface LogicalList {

    String getLocalFilename();

    Long getLogicallistId();

    void setDownloaded(boolean downloaded);

    boolean update();
}
