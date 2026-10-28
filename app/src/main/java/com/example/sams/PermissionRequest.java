package com.example.sams;

import org.json.JSONObject;
import java.io.Serializable;

public class PermissionRequest implements Serializable {
    private String id;
    private String studentId;
    private String studentName;
    private String studentClass;
    private String eventTitle;
    private String eventDates;
    private String scopeOrDept;
    private String reason;
    private String status;
    private int progress;
    private String supervisor;
    private String remarks;
    private long timestamp;

    public PermissionRequest() {
        this.status = "Pending PD";
        this.progress = 40;
        this.timestamp = System.currentTimeMillis();
    }

    public PermissionRequest(String id, String studentId, String studentName, String studentClass,
                             String eventTitle, String eventDates, String scopeOrDept, String reason,
                             String status, int progress, String supervisor, String remarks, long timestamp) {
        this.id = id;
        this.studentId = studentId;
        this.studentName = studentName;
        this.studentClass = studentClass;
        this.eventTitle = eventTitle;
        this.eventDates = eventDates;
        this.scopeOrDept = scopeOrDept;
        this.reason = reason;
        this.status = status;
        this.progress = progress;
        this.supervisor = supervisor;
        this.remarks = remarks;
        this.timestamp = timestamp;
    }

    public String getId() { return id != null ? id : ""; }
    public void setId(String id) { this.id = id; }

    public String getStudentId() { return studentId != null ? studentId : ""; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getStudentName() { return studentName != null ? studentName : ""; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getStudentClass() { return studentClass != null ? studentClass : ""; }
    public void setStudentClass(String studentClass) { this.studentClass = studentClass; }

    public String getEventTitle() { return eventTitle != null ? eventTitle : "Permission Request"; }
    public void setEventTitle(String eventTitle) { this.eventTitle = eventTitle; }

    public String getEventDates() { return eventDates != null ? eventDates : "Upcoming"; }
    public void setEventDates(String eventDates) { this.eventDates = eventDates; }

    public String getScopeOrDept() { return scopeOrDept != null ? scopeOrDept : "General"; }
    public void setScopeOrDept(String scopeOrDept) { this.scopeOrDept = scopeOrDept; }

    public String getReason() { return reason != null ? reason : ""; }
    public void setReason(String reason) { this.reason = reason; }

    public String getStatus() { return status != null ? status : "Pending"; }
    public void setStatus(String status) { this.status = status; }

    public int getProgress() { return progress; }
    public void setProgress(int progress) { this.progress = progress; }

    public String getSupervisor() { return supervisor != null ? supervisor : "Programme Director"; }
    public void setSupervisor(String supervisor) { this.supervisor = supervisor; }

    public String getRemarks() { return remarks != null ? remarks : "Remarks 01"; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public boolean isApproved() {
        if (status == null) return false;
        String s = status.toLowerCase();
        return s.contains("approved") || s.contains("accepted") || s.contains("granted");
    }

    public boolean isPending() {
        return !isApproved() && (status == null || !status.toLowerCase().contains("reject"));
    }

    public String getTitle() { return getEventTitle(); }
    public String getApplicantName() { return getStudentName(); }
    public String getApplicantRoll() { return getStudentId(); }
    public String getFormattedDateRange() { return getEventDates(); }

    public JSONObject toJson() {
        try {
            JSONObject obj = new JSONObject();
            obj.put("id", id);
            obj.put("studentId", studentId);
            obj.put("studentName", studentName);
            obj.put("studentClass", studentClass);
            obj.put("eventTitle", eventTitle);
            obj.put("eventDates", eventDates);
            obj.put("scopeOrDept", scopeOrDept);
            obj.put("reason", reason);
            obj.put("status", status);
            obj.put("progress", progress);
            obj.put("supervisor", supervisor);
            obj.put("remarks", remarks);
            obj.put("timestamp", timestamp);
            return obj;
        } catch (Exception e) {
            e.printStackTrace();
            return new JSONObject();
        }
    }

    public static PermissionRequest fromJson(JSONObject obj) {
        if (obj == null) return null;
        return new PermissionRequest(
                obj.optString("id", ""),
                obj.optString("studentId", ""),
                obj.optString("studentName", ""),
                obj.optString("studentClass", ""),
                obj.optString("eventTitle", "Event Permission"),
                obj.optString("eventDates", "Dates"),
                obj.optString("scopeOrDept", "General"),
                obj.optString("reason", ""),
                obj.optString("status", "Pending PD"),
                obj.optInt("progress", 40),
                obj.optString("supervisor", "Dr. Robert Vance"),
                obj.optString("remarks", "Remarks 01"),
                obj.optLong("timestamp", System.currentTimeMillis())
        );
    }
}
