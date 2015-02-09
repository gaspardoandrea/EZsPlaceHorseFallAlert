package org.ezsplace.horsefallalert;

public class AlertLogItem {
    private final int id;
    private final String entryDatetime;
    private final String text;

    public AlertLogItem(int id, String entryDatetime, String text) {
        this.id = id;
        this.entryDatetime = entryDatetime;
        this.text = text;
    }

    public int getId() {
        return id;
    }

    public String getEntryDatetime() {
        return entryDatetime;
    }

    public String getText() {
        return text;
    }
}
