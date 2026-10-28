package com.example.sams;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SpotPassAction implements Serializable {
    private String id;
    private String passId;
    private String actedByRole;
    private String actedByName;
    private String action;
    private String remarks;
    private long timestamp;
    private long pdNotifiedOn;
    private long teacherNotifiedOn;

    public SpotPassAction() {
        this.timestamp = System.currentTimeMillis();
    }

    public SpotPassAction(String actedByRole, String actedByName, String action, String remarks, String pdNotified, String teacherNotified) {
        this.id = "ACT-" + System.currentTimeMillis();
        this.actedByRole = actedByRole;
        this.actedByName = actedByName;
        this.action = action;
        this.remarks = remarks;
        this.timestamp = System.currentTimeMillis();
        this.pdNotifiedOn = System.currentTimeMillis();
        this.teacherNotifiedOn = System.currentTimeMillis();
    }

    public SpotPassAction(String id, String passId, String actedByRole, String actedByName,
                          String action, String remarks, long timestamp, long pdNotifiedOn, long teacherNotifiedOn) {
        this.id = id;
        this.passId = passId;
        this.actedByRole = actedByRole;
        this.actedByName = actedByName;
        this.action = action;
        this.remarks = remarks;
        this.timestamp = timestamp;
        this.pdNotifiedOn = pdNotifiedOn;
        this.teacherNotifiedOn = teacherNotifiedOn;
    }

    public String getId() { return id != null ? id : ""; }
    public void setId(String id) { this.id = id; }

    public String getPassId() { return passId != null ? passId : ""; }
    public void setPassId(String passId) { this.passId = passId; }

    public String getActedByRole() { return actedByRole != null ? actedByRole : "Staff"; }
    public void setActedByRole(String actedByRole) { this.actedByRole = actedByRole; }

    public String getActedByName() { return actedByName != null ? actedByName : ""; }
    public void setActedByName(String actedByName) { this.actedByName = actedByName; }

    public String getAction() { return action != null ? action : "Reviewed"; }
    public void setAction(String action) { this.action = action; }

    public String getRemarks() { return remarks != null ? remarks : ""; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public long getPdNotifiedOn() { return pdNotifiedOn; }
    public void setPdNotifiedOn(long pdNotifiedOn) { this.pdNotifiedOn = pdNotifiedOn; }

    public long getTeacherNotifiedOn() { return teacherNotifiedOn; }
    public void setTeacherNotifiedOn(long teacherNotifiedOn) { this.teacherNotifiedOn = teacherNotifiedOn; }

    public String getFormattedTimestamp() {
        return new SimpleDateFormat("dd MMM, hh:mm a", Locale.US).format(new Date(timestamp));
    }

    public JSONObject toJson() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("id", id);
        obj.put("passId", passId);
        obj.put("actedByRole", actedByRole);
        obj.put("actedByName", actedByName);
        obj.put("action", action);
        obj.put("remarks", remarks);
        obj.put("timestamp", timestamp);
        obj.put("pdNotifiedOn", pdNotifiedOn);
        obj.put("teacherNotifiedOn", teacherNotifiedOn);
        return obj;
    }

    public static SpotPassAction fromJson(JSONObject obj) {
        if (obj == null) return null;
        return new SpotPassAction(
                obj.optString("id", ""),
                obj.optString("passId", ""),
                obj.optString("actedByRole", "Staff"),
                obj.optString("actedByName", ""),
                obj.optString("action", "Reviewed"),
                obj.optString("remarks", ""),
                obj.optLong("timestamp", System.currentTimeMillis()),
                obj.optLong("pdNotifiedOn", 0),
                obj.optLong("teacherNotifiedOn", 0)
        );
    }
}
