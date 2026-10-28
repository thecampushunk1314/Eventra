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
        return obj;
    }

    public static Notice fromJsonObject(JSONObject obj) {
        return new Notice(
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
    }
}
