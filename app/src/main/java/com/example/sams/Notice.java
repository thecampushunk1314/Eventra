package com.example.sams;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class Notice implements Serializable {
    private String id;
    private String title;
    private String category;
    private String author;
    private String targetAudience;
    private String content;
    private String date;
    private long timestamp;
    private String eventTime;
    private boolean isCcNotice;
    private String meetingOutcomes;
    private boolean acknowledgedByPrincipal;
    private long acknowledgedTimestamp;
    private String acknowledgedBy;

    public Notice(String id, String title, String category, String author, String targetAudience, String content, String date, long timestamp) {
        this(id, title, category, author, targetAudience, content, date, timestamp, "");
    }

    public Notice(String id, String title, String category, String author, String targetAudience, String content, String date, long timestamp, String eventTime) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.author = author;
        this.targetAudience = targetAudience;
        this.content = content;
        this.date = date;
        this.timestamp = timestamp;
        this.eventTime = eventTime != null ? eventTime : "";
        this.isCcNotice = false;
        this.meetingOutcomes = "";
        this.acknowledgedByPrincipal = false;
        this.acknowledgedTimestamp = 0;
        this.acknowledgedBy = "";
    }

    public String getId() { return id != null ? id : ""; }
    public String getTitle() { return title != null ? title : ""; }
    public String getCategory() { return category != null ? category : "General Notice"; }
    public String getAuthor() { return author != null ? author : "Principal's Office"; }
    public String getTargetAudience() { return targetAudience != null ? targetAudience : "All Students & Faculty"; }
    public String getContent() { return content != null ? content : ""; }
    public String getDate() { return date != null ? date : ""; }
    public long getTimestamp() { return timestamp; }
    public String getEventTime() { return eventTime != null ? eventTime : ""; }
    public void setEventTime(String eventTime) { this.eventTime = eventTime; }
    public void setTitle(String title) { this.title = title; }
    public void setCategory(String category) { this.category = category; }
    public void setContent(String content) { this.content = content; }
    public void setDate(String date) { this.date = date; }
    public void setTargetAudience(String targetAudience) { this.targetAudience = targetAudience; }
    public void setAuthor(String author) { this.author = author; }

    public boolean isCcNotice() { return isCcNotice; }
    public void setCcNotice(boolean ccNotice) { isCcNotice = ccNotice; }

    public String getMeetingOutcomes() { return meetingOutcomes != null ? meetingOutcomes : ""; }
    public void setMeetingOutcomes(String meetingOutcomes) { this.meetingOutcomes = meetingOutcomes; }

    public boolean isAcknowledgedByPrincipal() { return acknowledgedByPrincipal; }
    public void setAcknowledgedByPrincipal(boolean acknowledgedByPrincipal) { this.acknowledgedByPrincipal = acknowledgedByPrincipal; }

    public long getAcknowledgedTimestamp() { return acknowledgedTimestamp; }
    public void setAcknowledgedTimestamp(long acknowledgedTimestamp) { this.acknowledgedTimestamp = acknowledgedTimestamp; }

    public String getAcknowledgedBy() { return acknowledgedBy != null ? acknowledgedBy : ""; }
    public void setAcknowledgedBy(String acknowledgedBy) { this.acknowledgedBy = acknowledgedBy; }

    public JSONObject toJsonObject() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("id", id);
        obj.put("title", title);
        obj.put("category", category);
        obj.put("author", author);
        obj.put("targetAudience", targetAudience);
        obj.put("content", content);
        obj.put("date", date);
        obj.put("timestamp", timestamp);
        obj.put("eventTime", eventTime);
        obj.put("isCcNotice", isCcNotice);
        obj.put("meetingOutcomes", meetingOutcomes);
        obj.put("acknowledgedByPrincipal", acknowledgedByPrincipal);
        obj.put("acknowledgedTimestamp", acknowledgedTimestamp);
        obj.put("acknowledgedBy", acknowledgedBy);
        return obj;
    }

    public static Notice fromJsonObject(JSONObject obj) {
        Notice n = new Notice(
                obj.optString("id", "NOT-" + System.currentTimeMillis()),
                obj.optString("title", "Institutional Notice"),
                obj.optString("category", "General"),
                obj.optString("author", "Principal Office"),
                obj.optString("targetAudience", "All"),
                obj.optString("content", ""),
                obj.optString("date", ""),
                obj.optLong("timestamp", System.currentTimeMillis()),
                obj.optString("eventTime", "")
        );
        n.setCcNotice(obj.optBoolean("isCcNotice", false));
        n.setMeetingOutcomes(obj.optString("meetingOutcomes", ""));
        n.setAcknowledgedByPrincipal(obj.optBoolean("acknowledgedByPrincipal", false));
        n.setAcknowledgedTimestamp(obj.optLong("acknowledgedTimestamp", 0));
        n.setAcknowledgedBy(obj.optString("acknowledgedBy", ""));
        return n;
    }
}
