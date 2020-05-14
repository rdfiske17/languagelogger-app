package de.lmu.ifi.researchime.contentabstraction.model;

public enum ContentUnitEventType {
    // message-diff event-types: these 3 together represent all changes the user did in the text input field
    ADDED, CHANGED, REMOVED,
    // this represents splitting of an input text by adding a space between words
    SPLITTED, JOINED,
    // full-message event-type: all events of this type together build the full message that was finally contained in the text input field
    CONTAINED;
}
