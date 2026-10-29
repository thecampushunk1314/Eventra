package com.example.sams;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.Serializable;

public class Event implements Serializable {
    private String id;
    private String name;
    private String startDate;
    private String endDate;
    private String type; // Sports, Cultural, Technical, Academic
    private String level; // College, Inter-Collegiate, State, National
    private String organiser;
    private String coordinatorName;
    private String venue;
    private String description;
    private long timestamp;

    public Event() {
        this.timestamp = System.currentTimeMillis();
        this.type = "Sports";
        this.level = "Inter-Collegiate";
    }

    public Event(String id, String name, String startDate, String endDate, String type,
                 String level, String organiser, String coordinatorName, String venue, String description) {
        this.id = id;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.type = type;
        this.level = level;
        this.organiser = organiser;
        this.coordinatorName = coordinatorName;
        this.venue = venue;
        this.description = description;
        this.timestamp = System.currentTimeMillis();
    }

    public String getId() { return id != null ? id : ""; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name != null ? name : ""; }
    public void setName(String name) { this.name = name; }

    public String getStartDate() { return startDate != null ? startDate : ""; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate != null ? endDate : ""; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    public String getDatesDisplay() {
        if (startDate != null && endDate != null && !startDate.isEmpty() && !endDate.isEmpty()) {
            return startDate + " – " + endDate;
        } else if (startDate != null && !startDate.isEmpty()) {
            return startDate;
        }
        return "TBD";
    }

    public String getType() { return type != null ? type : "Sports"; }
    public void setType(String type) { this.type = type; }

    public String getLevel() { return level != null ? level : "Inter-Collegiate"; }
    public void setLevel(String level) { this.level = level; }

    public String getOrganiser() { return organiser != null ? organiser : ""; }
    public void setOrganiser(String organiser) { this.organiser = organiser; }

    public String getCoordinatorName() { return coordinatorName != null ? coordinatorName : ""; }
    public void setCoordinatorName(String coordinatorName) { this.coordinatorName = coordinatorName; }

    public String getVenue() { return venue != null ? venue : ""; }
    public void setVenue(String venue) { this.venue = venue; }

    public String getDescription() { return description != null ? description : ""; }
    public void setDescription(String description) { this.description = description; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public JSONObject toJson() {
        try {
            JSONObject obj = new JSONObject();
            obj.put("id", id);
            obj.put("name", name);
            obj.put("startDate", startDate);
            obj.put("endDate", endDate);
            obj.put("type", type);
            obj.put("level", level);
            obj.put("organiser", organiser);
            obj.put("coordinatorName", coordinatorName);
            obj.put("venue", venue);
            obj.put("description", description);
            obj.put("timestamp", timestamp);
            return obj;
        } catch (JSONException e) {
            e.printStackTrace();
            return new JSONObject();
        }
    }

    public static Event fromJson(JSONObject obj) {
        if (obj == null) return null;
        Event ev = new Event(
                obj.optString("id", ""),
                obj.optString("name", ""),
                obj.optString("startDate", ""),
                obj.optString("endDate", ""),
                obj.optString("type", "Sports"),
                obj.optString("level", "Inter-Collegiate"),
                obj.optString("organiser", ""),
                obj.optString("coordinatorName", ""),
                obj.optString("venue", ""),
                obj.optString("description", "")
        );
        ev.setTimestamp(obj.optLong("timestamp", System.currentTimeMillis()));
        return ev;
    }
}
