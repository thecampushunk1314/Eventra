package com.example.sams;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class EventManager {
    private static final String PREF_NAME = "eventra_events_pref";
    private static final String KEY_EVENTS = "saved_events";
    private static final String KEY_SESSIONS = "saved_practice_sessions";
    private static final String KEY_SEEDED = "seeded_events_v1";

    private static EventManager instance;
    private final SharedPreferences prefs;
    private final Context appContext;

    private EventManager(Context context) {
        this.appContext = context.getApplicationContext();
        this.prefs = appContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        seedDefaultEventsIfEmpty();
    }

    public static synchronized EventManager getInstance(Context context) {
        if (instance == null) {
            instance = new EventManager(context);
        }
        return instance;
    }

    private void seedDefaultEventsIfEmpty() {
        if (!prefs.getBoolean(KEY_SEEDED, false)) {
            List<Event> events = new ArrayList<>();
            events.add(new Event(
                    "EV-2026-001",
                    "Inter-Collegiate Football Championship",
                    "20 Oct 2026",
                    "30 Oct 2026",
                    "Sports",
                    "Inter-Collegiate",
                    "Goa University Sports Council",
                    "Coach Rajesh Parab",
                    "University Athletics Complex",
                    "Official inter-collegiate tournament for selection to West Zone Inter-University team."
            ));
            events.add(new Event(
                    "EV-2026-002",
                    "State Youth Cultural & Drama Fest",
                    "05 Nov 2026",
                    "08 Nov 2026",
                    "Cultural",
                    "State Level",
                    "Directorate of Art & Culture",
                    "Dr. Meenakshi Sundaram",
                    "Kala Academy Main Stage",
                    "State cultural competition across theatre, classical music, and folk dance."
            ));
            events.add(new Event(
                    "EV-2026-003",
                    "Annual College Athletics & Track Meet",
                    "15 Nov 2026",
                    "17 Nov 2026",
                    "Sports",
                    "College",
                    "YCASC Physical Education Dept",
                    "Maria Rodrigues (Sports Director)",
                    "Yashoda College Main Grounds",
                    "College track and field competitions across 100m, 400m, relay, and long jump."
            ));
            saveEvents(events);

            List<PracticeSession> sessions = new ArrayList<>();
            PracticeSession s1 = new PracticeSession(
                    "PRAC-101",
                    "EV-2026-001",
                    "Inter-Collegiate Football Championship",
                    "28 Oct 2026",
                    "07:00 AM",
                    "09:30 AM",
                    "Yashoda College Main Ground",
                    "Morning Pre-Match Conditioning",
                    "Coach Rajesh Parab"
            );
            // Pre-seed some active participants
            s1.markStudent("SCI-26-001", true);
            s1.markStudent("COM-26-001", true);
            sessions.add(s1);

            PracticeSession s2 = new PracticeSession(
                    "PRAC-102",
                    "EV-2026-002",
                    "State Youth Cultural & Drama Fest",
                    "29 Oct 2026",
                    "03:00 PM",
                    "05:30 PM",
                    "College Auditorium Stage",
                    "Stage Drama & Rehearsal Run",
                    "Dr. Meenakshi Sundaram"
            );
            s2.markStudent("ART-26-001", true);
            sessions.add(s2);

            saveSessions(sessions);
            prefs.edit().putBoolean(KEY_SEEDED, true).apply();
        }
    }

    public synchronized List<Event> getAllEvents() {
        List<Event> list = new ArrayList<>();
        String json = prefs.getString(KEY_EVENTS, "[]");
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                Event ev = Event.fromJson(obj);
                if (ev != null) list.add(ev);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public synchronized void saveEvents(List<Event> list) {
        try {
            JSONArray arr = new JSONArray();
            for (Event e : list) {
                arr.put(e.toJson());
            }
            prefs.edit().putString(KEY_EVENTS, arr.toString()).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void addEvent(Event event) {
        if (event == null) return;
        List<Event> list = getAllEvents();
        list.add(0, event);
        saveEvents(list);
    }

    public synchronized Event getEventById(String eventId) {
        if (eventId == null) return null;
        for (Event e : getAllEvents()) {
            if (eventId.equalsIgnoreCase(e.getId())) {
                return e;
            }
        }
        return null;
    }

    public synchronized List<PracticeSession> getAllPracticeSessions() {
        List<PracticeSession> list = new ArrayList<>();
        String json = prefs.getString(KEY_SESSIONS, "[]");
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                PracticeSession ps = PracticeSession.fromJson(obj);
                if (ps != null) list.add(ps);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public synchronized void saveSessions(List<PracticeSession> list) {
        try {
            JSONArray arr = new JSONArray();
            for (PracticeSession ps : list) {
                arr.put(ps.toJson());
            }
            prefs.edit().putString(KEY_SESSIONS, arr.toString()).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void addPracticeSession(PracticeSession session) {
        if (session == null) return;
        List<PracticeSession> list = getAllPracticeSessions();
        list.add(0, session);
        saveSessions(list);
    }

    public synchronized PracticeSession getPracticeSessionById(String id) {
        if (id == null) return null;
        for (PracticeSession ps : getAllPracticeSessions()) {
            if (id.equalsIgnoreCase(ps.getId())) return ps;
        }
        return null;
    }

    public synchronized boolean updatePracticeSession(PracticeSession session) {
        if (session == null || session.getId() == null) return false;
        List<PracticeSession> list = getAllPracticeSessions();
        for (int i = 0; i < list.size(); i++) {
            if (session.getId().equalsIgnoreCase(list.get(i).getId())) {
                list.set(i, session);
                saveSessions(list);
                return true;
            }
        }
        return false;
    }

    /**
     * Records ground attendance verified on the field by coach / coordinator.
     * Automatically updates student duty leave counts and credit in UserManager.
     */
    public synchronized void recordGroundAttendance(String sessionId, List<String> presentStudentIds) {
        PracticeSession session = getPracticeSessionById(sessionId);
        if (session == null) return;

        session.setPresentStudentIds(presentStudentIds);
        updatePracticeSession(session);

        // Credit co-curricular attendance to students
        UserManager userManager = UserManager.getInstance(appContext);
        for (String sId : presentStudentIds) {
            User student = userManager.getUserById(sId);
            if (student != null) {
                int currDuty = student.getDutyLeaveApprovedCount();
                student.setDutyLeaveApprovedCount(currDuty + 1);
                student.calculateAttendance();
                userManager.updateUser(student);
            }
        }
    }
}
