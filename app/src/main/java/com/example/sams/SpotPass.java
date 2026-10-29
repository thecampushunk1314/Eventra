package com.example.sams;

import org.json.JSONObject;
import java.io.Serializable;

public class SpotPass implements Serializable {
    private String id;
    private String studentId;
    private String studentName;
    private String studentClass;
    private String activityTitle;
    private String date;
    private String timeSlot;
    private String subject;
    private String subjectTeacher;
    private String classTeacher;
    private String reason;
    private String status;
    private int progress;
    private String classTeacherStatus;
    private String subjectTeacherStatus;
    private long timestamp;
    private String eventType; // Sports, Cultural, Volunteer Work, Sick Leave, Emergency, Competition
    private java.util.List<SpotPassAction> actionHistory;

    public SpotPass() {
        this.status = "Pending Class Teacher";
        this.progress = 50;
        this.classTeacherStatus = "Pending Review";
        this.subjectTeacherStatus = "Notified of Slot";
        this.timestamp = System.currentTimeMillis();
        this.eventType = "Sports";
        this.actionHistory = new java.util.ArrayList<>();
    }

    public SpotPass(String id, String studentId, String studentName, String studentClass,
                    String activityTitle, String date, String timeSlot, String subject,
                    String subjectTeacher, String classTeacher, String reason, String status,
                    int progress, String classTeacherStatus, String subjectTeacherStatus, long timestamp) {
        this(id, studentId, studentName, studentClass, activityTitle, date, timeSlot, subject,
             subjectTeacher, classTeacher, reason, status, progress, classTeacherStatus, subjectTeacherStatus, timestamp, "Sports");
    }

    public SpotPass(String id, String studentId, String studentName, String studentClass,
                    String activityTitle, String date, String timeSlot, String subject,
                    String subjectTeacher, String classTeacher, String reason, String status,
                    int progress, String classTeacherStatus, String subjectTeacherStatus, long timestamp, String eventType) {
        this.id = id;
        this.studentId = studentId;
        this.studentName = studentName;
        this.studentClass = studentClass;
        this.activityTitle = activityTitle;
        this.date = date;
        this.timeSlot = timeSlot;
        this.subject = subject;
        this.subjectTeacher = subjectTeacher;
        this.classTeacher = classTeacher;
        this.reason = reason;
        this.status = status;
        this.progress = progress;
        this.classTeacherStatus = classTeacherStatus;
        this.subjectTeacherStatus = subjectTeacherStatus;
        this.timestamp = timestamp;
        this.eventType = eventType != null && !eventType.isEmpty() ? eventType : "Sports";
    }

    public String getEventType() { return eventType != null ? eventType : "Sports"; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getId() { return id != null ? id : ""; }
    public void setId(String id) { this.id = id; }

    public String getStudentId() { return studentId != null ? studentId : ""; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getStudentName() { return studentName != null ? studentName : ""; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getStudentClass() { return studentClass != null ? studentClass : ""; }
    public void setStudentClass(String studentClass) { this.studentClass = studentClass; }

    public String getStudentRoll() { return studentId != null ? studentId : ""; }

    public String getActivityTitle() { return activityTitle != null ? activityTitle : "Spot Pass Duty"; }
    public void setActivityTitle(String activityTitle) { this.activityTitle = activityTitle; }

    public String getDate() { return date != null ? date : "Today"; }
    public void setDate(String date) { this.date = date; }

    public String getTimeSlot() { return timeSlot != null ? timeSlot : "Lecture Slot 1"; }
    public void setTimeSlot(String timeSlot) { this.timeSlot = timeSlot; }

    public String getSubject() { return subject != null ? subject : "General Academic"; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getSubjectTeacher() { return subjectTeacher != null ? subjectTeacher : "Subject Teacher"; }
    public void setSubjectTeacher(String subjectTeacher) { this.subjectTeacher = subjectTeacher; }

    public String getClassTeacher() { return classTeacher != null ? classTeacher : "Class Teacher"; }
    public void setClassTeacher(String classTeacher) { this.classTeacher = classTeacher; }

    public String getReason() { return reason != null ? reason : ""; }
    public void setReason(String reason) { this.reason = reason; }

    public String getStatus() { return status != null ? status : "Pending"; }
    public void setStatus(String status) { this.status = status; }

    public int getProgress() { return progress; }
    public void setProgress(int progress) { this.progress = progress; }

    public String getClassTeacherStatus() { return classTeacherStatus != null ? classTeacherStatus : "Pending"; }
    public void setClassTeacherStatus(String classTeacherStatus) { this.classTeacherStatus = classTeacherStatus; }

    public String getSubjectTeacherStatus() { return subjectTeacherStatus != null ? subjectTeacherStatus : "Notified"; }
    public void setSubjectTeacherStatus(String subjectTeacherStatus) { this.subjectTeacherStatus = subjectTeacherStatus; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public boolean isApproved() {
        if (status == null) return false;
        String s = status.toLowerCase();
        return s.contains("approved") || s.contains("granted") || s.contains("accepted");
    }

    public java.util.List<SpotPassAction> getActionHistory() {
        if (actionHistory == null) actionHistory = new java.util.ArrayList<>();
        return actionHistory;
    }

    public void setActionHistory(java.util.List<SpotPassAction> actionHistory) {
        this.actionHistory = actionHistory;
    }

    public void addAction(SpotPassAction action) {
        if (actionHistory == null) actionHistory = new java.util.ArrayList<>();
        actionHistory.add(action);
    }

    public JSONObject toJson() {
        try {
            JSONObject obj = new JSONObject();
            obj.put("id", id);
            obj.put("studentId", studentId);
            obj.put("studentName", studentName);
            obj.put("studentClass", studentClass);
            obj.put("activityTitle", activityTitle);
            obj.put("date", date);
            obj.put("timeSlot", timeSlot);
            obj.put("subject", subject);
            obj.put("subjectTeacher", subjectTeacher);
            obj.put("classTeacher", classTeacher);
            obj.put("reason", reason);
            obj.put("status", status);
            obj.put("progress", progress);
            obj.put("classTeacherStatus", classTeacherStatus);
            obj.put("subjectTeacherStatus", subjectTeacherStatus);
            obj.put("timestamp", timestamp);
            obj.put("eventType", eventType);

            if (actionHistory != null && !actionHistory.isEmpty()) {
                org.json.JSONArray arr = new org.json.JSONArray();
                for (SpotPassAction act : actionHistory) {
                    arr.put(act.toJson());
                }
                obj.put("actionHistory", arr);
            }

            return obj;
        } catch (Exception e) {
            e.printStackTrace();
            return new JSONObject();
        }
    }

    public static SpotPass fromJson(JSONObject obj) {
        if (obj == null) return null;
        SpotPass pass = new SpotPass(
                obj.optString("id", ""),
                obj.optString("studentId", ""),
                obj.optString("studentName", ""),
                obj.optString("studentClass", ""),
                obj.optString("activityTitle", "Spot Pass"),
                obj.optString("date", "Today"),
                obj.optString("timeSlot", "Lecture slot 1"),
                obj.optString("subject", "Science"),
                obj.optString("subjectTeacher", "Subject Teacher"),
                obj.optString("classTeacher", "Class Teacher"),
                obj.optString("reason", ""),
                obj.optString("status", "Pending"),
                obj.optInt("progress", 50),
                obj.optString("classTeacherStatus", "Pending Review"),
                obj.optString("subjectTeacherStatus", "Notified of Slot"),
                obj.optLong("timestamp", System.currentTimeMillis()),
                obj.optString("eventType", "Sports")
        );

        if (obj.has("actionHistory")) {
            org.json.JSONArray arr = obj.optJSONArray("actionHistory");
            if (arr != null) {
                java.util.List<SpotPassAction> history = new java.util.ArrayList<>();
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject aObj = arr.optJSONObject(i);
                    SpotPassAction act = SpotPassAction.fromJson(aObj);
                    if (act != null) history.add(act);
                }
                pass.setActionHistory(history);
            }
        }

        return pass;
    }
}
