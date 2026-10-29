package com.example.sams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PracticeSession implements Serializable {
    private String id;
    private String eventId;
    private String eventName;
    private String practiceDate;
    private String startTime;
    private String endTime;
    private String venue;
    private String schedulingType; // e.g. "Morning Practice Slot", "Intensive Camp", "Pre-Tournament Practice"
    private String coachName;
    private List<String> presentStudentIds;
    private long timestamp;

    public PracticeSession() {
        this.presentStudentIds = new ArrayList<>();
        this.timestamp = System.currentTimeMillis();
    }

    public PracticeSession(String id, String eventId, String eventName, String practiceDate,
                           String startTime, String endTime, String venue, String schedulingType, String coachName) {
        this.id = id;
        this.eventId = eventId;
        this.eventName = eventName;
        this.practiceDate = practiceDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.venue = venue;
        this.schedulingType = schedulingType;
        this.coachName = coachName;
        this.presentStudentIds = new ArrayList<>();
        this.timestamp = System.currentTimeMillis();
    }

    public String getId() { return id != null ? id : ""; }
    public void setId(String id) { this.id = id; }

    public String getEventId() { return eventId != null ? eventId : ""; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getEventName() { return eventName != null ? eventName : ""; }
    public void setEventName(String eventName) { this.eventName = eventName; }

    public String getPracticeDate() { return practiceDate != null ? practiceDate : ""; }
    public void setPracticeDate(String practiceDate) { this.practiceDate = practiceDate; }

    public String getStartTime() { return startTime != null ? startTime : ""; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime != null ? endTime : ""; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public String getTimeSlot() {
        if (startTime != null && endTime != null && !startTime.isEmpty() && !endTime.isEmpty()) {
            return startTime + " – " + endTime;
        } else if (startTime != null && !startTime.isEmpty()) {
            return startTime;
        }
        return "Regular Slot";
    }

    public String getVenue() { return venue != null ? venue : ""; }
    public void setVenue(String venue) { this.venue = venue; }

    public String getSchedulingType() { return schedulingType != null ? schedulingType : "Morning Slot"; }
    public void setSchedulingType(String schedulingType) { this.schedulingType = schedulingType; }

    public String getCoachName() { return coachName != null ? coachName : "Sports Coach"; }
    public void setCoachName(String coachName) { this.coachName = coachName; }

    public List<String> getPresentStudentIds() {
        if (presentStudentIds == null) presentStudentIds = new ArrayList<>();
        return presentStudentIds;
    }

    public void setPresentStudentIds(List<String> presentStudentIds) {
        this.presentStudentIds = presentStudentIds;
    }

    public boolean isStudentPresent(String studentId) {
        if (studentId == null || presentStudentIds == null) return false;
        for (String id : presentStudentIds) {
            if (studentId.equalsIgnoreCase(id)) return true;
        }
        return false;
    }

    public void markStudent(String studentId, boolean present) {
        if (studentId == null) return;
        if (presentStudentIds == null) presentStudentIds = new ArrayList<>();
        if (present && !isStudentPresent(studentId)) {
            presentStudentIds.add(studentId);
        } else if (!present) {
            presentStudentIds.remove(studentId);
        }
    }

    public int getPresentCount() {
        return presentStudentIds != null ? presentStudentIds.size() : 0;
    }

    public JSONObject toJson() {
        try {
            JSONObject obj = new JSONObject();
            obj.put("id", id);
            obj.put("eventId", eventId);
            obj.put("eventName", eventName);
            obj.put("practiceDate", practiceDate);
            obj.put("startTime", startTime);
            obj.put("endTime", endTime);
            obj.put("venue", venue);
            obj.put("schedulingType", schedulingType);
            obj.put("coachName", coachName);
            obj.put("timestamp", timestamp);

            JSONArray arr = new JSONArray();
            if (presentStudentIds != null) {
                for (String sId : presentStudentIds) {
                    arr.put(sId);
                }
            }
            obj.put("presentStudentIds", arr);
            return obj;
        } catch (JSONException e) {
            e.printStackTrace();
            return new JSONObject();
        }
    }

    public static PracticeSession fromJson(JSONObject obj) {
        if (obj == null) return null;
        PracticeSession ps = new PracticeSession(
                obj.optString("id", ""),
                obj.optString("eventId", ""),
                obj.optString("eventName", ""),
                obj.optString("practiceDate", ""),
                obj.optString("startTime", ""),
                obj.optString("endTime", ""),
                obj.optString("venue", ""),
                obj.optString("schedulingType", "Morning Slot"),
                obj.optString("coachName", "Sports Coach")
        );
        JSONArray arr = obj.optJSONArray("presentStudentIds");
        if (arr != null) {
            List<String> list = new ArrayList<>();
            for (int i = 0; i < arr.length(); i++) {
                list.add(arr.optString(i));
            }
            ps.setPresentStudentIds(list);
        }
        return ps;
    }
}
