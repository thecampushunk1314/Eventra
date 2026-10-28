package com.example.sams;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class TimetableSlot implements Serializable {
    private String id; // e.g. "BSC_MON_1"
    private String stream; // "Science Stream", "Commerce Stream", "Arts Stream"
    private String dayOfWeek; // "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"
    private int slotNumber; // 1 to 5
    private String startTime; // "08:00 AM"
    private String endTime; // "09:00 AM"
    private String timeDisplay; // "08:00 AM - 09:00 AM"
    private String subject; // "Chemistry"
    private String lectureTitle; // "Chemistry · Lecture 1"
    private String teacherName; // "Dr. Sneha Gaonkar"
    private String teacherEmail; // "sneha.gaonkar@ycasc.com"
    private String room; // "Room 204" / "Lab 1"
    private boolean isInterchanged;
    private String interchangedWith; // e.g. "Dr. Pramod Naik"
    private String interchangeNote; // e.g. "Mutual swap for Lab Practical setup"
    private long lastUpdated;

    public TimetableSlot() {}

    public TimetableSlot(String id, String stream, String dayOfWeek, int slotNumber,
                         String startTime, String endTime, String subject,
                         String lectureTitle, String teacherName, String teacherEmail, String room) {
        this.id = id;
        this.stream = stream;
        this.dayOfWeek = dayOfWeek;
        this.slotNumber = slotNumber;
        this.startTime = startTime;
        this.endTime = endTime;
        this.timeDisplay = startTime + " - " + endTime;
        this.subject = subject;
        this.lectureTitle = lectureTitle;
        this.teacherName = teacherName;
        this.teacherEmail = teacherEmail;
        this.room = room;
        this.isInterchanged = false;
        this.interchangedWith = "";
        this.interchangeNote = "";
        this.lastUpdated = System.currentTimeMillis();
    }

    public JSONObject toJsonObject() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("id", id);
            obj.put("stream", stream);
            obj.put("dayOfWeek", dayOfWeek);
            obj.put("slotNumber", slotNumber);
            obj.put("startTime", startTime);
            obj.put("endTime", endTime);
            obj.put("timeDisplay", timeDisplay);
            obj.put("subject", subject);
            obj.put("lectureTitle", lectureTitle);
            obj.put("teacherName", teacherName);
            obj.put("teacherEmail", teacherEmail);
            obj.put("room", room);
            obj.put("isInterchanged", isInterchanged);
            obj.put("interchangedWith", interchangedWith != null ? interchangedWith : "");
            obj.put("interchangeNote", interchangeNote != null ? interchangeNote : "");
            obj.put("lastUpdated", lastUpdated);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return obj;
    }

    public static TimetableSlot fromJsonObject(JSONObject obj) {
        if (obj == null) return null;
        TimetableSlot slot = new TimetableSlot();
        slot.id = obj.optString("id", "");
        slot.stream = obj.optString("stream", "");
        slot.dayOfWeek = obj.optString("dayOfWeek", "");
        slot.slotNumber = obj.optInt("slotNumber", 1);
        slot.startTime = obj.optString("startTime", "");
        slot.endTime = obj.optString("endTime", "");
        slot.timeDisplay = obj.optString("timeDisplay", slot.startTime + " - " + slot.endTime);
        slot.subject = obj.optString("subject", "");
        slot.lectureTitle = obj.optString("lectureTitle", "");
        slot.teacherName = obj.optString("teacherName", "");
        slot.teacherEmail = obj.optString("teacherEmail", "");
        slot.room = obj.optString("room", "");
        slot.isInterchanged = obj.optBoolean("isInterchanged", false);
        slot.interchangedWith = obj.optString("interchangedWith", "");
        slot.interchangeNote = obj.optString("interchangeNote", "");
        slot.lastUpdated = obj.optLong("lastUpdated", System.currentTimeMillis());
        return slot;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getStream() { return stream; }
    public void setStream(String stream) { this.stream = stream; }

    public String getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public int getSlotNumber() { return slotNumber; }
    public void setSlotNumber(int slotNumber) { this.slotNumber = slotNumber; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public String getTimeDisplay() { return timeDisplay; }
    public void setTimeDisplay(String timeDisplay) { this.timeDisplay = timeDisplay; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getLectureTitle() { return lectureTitle; }
    public void setLectureTitle(String lectureTitle) { this.lectureTitle = lectureTitle; }

    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }

    public String getTeacherEmail() { return teacherEmail; }
    public void setTeacherEmail(String teacherEmail) { this.teacherEmail = teacherEmail; }

    public String getRoom() { return room; }
    public void setRoom(String room) { this.room = room; }

    public boolean isInterchanged() { return isInterchanged; }
    public void setInterchanged(boolean interchanged) { isInterchanged = interchanged; }

    public String getInterchangedWith() { return interchangedWith; }
    public void setInterchangedWith(String interchangedWith) { this.interchangedWith = interchangedWith; }

    public String getInterchangeNote() { return interchangeNote; }
    public void setInterchangeNote(String interchangeNote) { this.interchangeNote = interchangeNote; }

    public long getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(long lastUpdated) { this.lastUpdated = lastUpdated; }
}
