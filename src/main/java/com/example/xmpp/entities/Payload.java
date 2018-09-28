package com.example.xmpp.entities;

public class Payload {
    private String title;
    private String summary;

    public Payload() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String toString() {
        return getTitle() + ": " + getSummary();
    }
}
