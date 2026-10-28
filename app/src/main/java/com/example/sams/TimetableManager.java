package com.example.sams;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class TimetableManager {
    private static final String PREF_NAME = "eventra_academic_timetable_pref";
    private static final String KEY_SLOTS = "academic_timetable_slots_v1";

    public static final String STREAM_SCIENCE = "Science Stream";
    public static final String STREAM_COMMERCE = "Commerce Stream";
    public static final String STREAM_ARTS = "Arts Stream";

    public static final String[] DAYS_OF_WEEK = new String[]{
            "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"
    };

    // Requested Daily Academic Timings
    public static final String TIME_SLOT_1_START = "08:00 AM";
    public static final String TIME_SLOT_1_END = "09:00 AM";

    public static final String TIME_SLOT_2_START = "09:00 AM";
    public static final String TIME_SLOT_2_END = "10:00 AM";

    public static final String TIME_SLOT_3_START = "10:00 AM";
    public static final String TIME_SLOT_3_END = "11:00 AM";

    public static final String TIME_BREAK_START = "11:00 AM";
    public static final String TIME_BREAK_END = "11:30 AM";

    public static final String TIME_SLOT_4_START = "11:30 AM";
    public static final String TIME_SLOT_4_END = "12:30 PM";

    public static final String TIME_SLOT_5_START = "12:30 PM";
    public static final String TIME_SLOT_5_END = "01:30 PM";

    private static TimetableManager instance;
    private final SharedPreferences prefs;

    private TimetableManager(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        seedDefaultTimetablesIfEmpty();
    }

    public static synchronized TimetableManager getInstance(Context context) {
        if (instance == null) {
            instance = new TimetableManager(context);
        }
        return instance;
    }

    private synchronized void seedDefaultTimetablesIfEmpty() {
        String existing = prefs.getString(KEY_SLOTS, null);
        if (existing == null || existing.trim().isEmpty() || existing.equals("[]")) {
            List<TimetableSlot> list = buildDefaultScheduleForAllStreams();
            saveAllSlots(list);
        }
    }

    public synchronized List<TimetableSlot> getAllSlots() {
        List<TimetableSlot> list = new ArrayList<>();
        String json = prefs.getString(KEY_SLOTS, "[]");
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                TimetableSlot slot = TimetableSlot.fromJsonObject(obj);
                if (slot != null) {
                    list.add(slot);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public synchronized void saveAllSlots(List<TimetableSlot> slots) {
        try {
            JSONArray arr = new JSONArray();
            for (TimetableSlot s : slots) {
                if (s != null) {
                    arr.put(s.toJsonObject());
                }
            }
            prefs.edit().putString(KEY_SLOTS, arr.toString()).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized TimetableSlot getSlotById(String slotId) {
        if (slotId == null) return null;
        for (TimetableSlot s : getAllSlots()) {
            if (slotId.equalsIgnoreCase(s.getId())) {
                return s;
            }
        }
        return null;
    }

    public synchronized List<TimetableSlot> getTimetableForStreamAndDay(String stream, String dayOfWeek) {
        String normalizedStream = normalizeStreamName(stream);
        String normalizedDay = normalizeDayOfWeek(dayOfWeek);

        List<TimetableSlot> result = new ArrayList<>();
        for (TimetableSlot s : getAllSlots()) {
            if (normalizeStreamName(s.getStream()).equalsIgnoreCase(normalizedStream)
                    && normalizeDayOfWeek(s.getDayOfWeek()).equalsIgnoreCase(normalizedDay)) {
                result.add(s);
            }
        }
        // Sort by slot number (1 to 5)
        result.sort((a, b) -> Integer.compare(a.getSlotNumber(), b.getSlotNumber()));
        return result;
    }

    public synchronized List<TimetableSlot> getTimetableForUser(User user, String dayOfWeek) {
        if (user == null) {
            return getTimetableForStreamAndDay(STREAM_SCIENCE, dayOfWeek);
        }
        String stream = normalizeStreamName(user.getDepartment());
        if (stream.isEmpty()) {
            stream = normalizeStreamName(user.getDegree());
        }
        if (stream.isEmpty()) {
            stream = STREAM_SCIENCE;
        }
        return getTimetableForStreamAndDay(stream, dayOfWeek);
    }

    /**
     * Interchanges / Swaps two lecture slots for a stream.
     * Swaps subjects, lecture titles, teachers, and rooms between slot1 and slot2.
     * Records interchanged status and mutual coordination communication note.
     */
    public synchronized boolean interchangeLectures(String slotId1, String slotId2,
                                                    String interchangeNote, String performingTeacherName) {
        if (slotId1 == null || slotId2 == null || slotId1.equalsIgnoreCase(slotId2)) {
            return false;
        }

        List<TimetableSlot> allSlots = getAllSlots();
        TimetableSlot slot1 = null;
        TimetableSlot slot2 = null;

        for (TimetableSlot s : allSlots) {
            if (slotId1.equalsIgnoreCase(s.getId())) slot1 = s;
            if (slotId2.equalsIgnoreCase(s.getId())) slot2 = s;
        }

        if (slot1 == null || slot2 == null) {
            return false;
        }

        // Capture data from slot 1
        String tempSubject = slot1.getSubject();
        String tempTitle = slot1.getLectureTitle();
        String tempTeacherName = slot1.getTeacherName();
        String tempTeacherEmail = slot1.getTeacherEmail();
        String tempRoom = slot1.getRoom();

        // Assign slot 2 data into slot 1
        slot1.setSubject(slot2.getSubject());
        slot1.setLectureTitle(slot2.getSubject() + " · Lecture " + slot1.getSlotNumber() + " (Swapped)");
        slot1.setTeacherName(slot2.getTeacherName());
        slot1.setTeacherEmail(slot2.getTeacherEmail());
        slot1.setRoom(slot2.getRoom());
        slot1.setInterchanged(true);
        slot1.setInterchangedWith(tempTeacherName);
        slot1.setInterchangeNote(interchangeNote != null && !interchangeNote.trim().isEmpty() ? 
                interchangeNote : "Interchanged with " + tempTeacherName + " via faculty mutual communication.");
        slot1.setLastUpdated(System.currentTimeMillis());

        // Assign saved slot 1 data into slot 2
        slot2.setSubject(tempSubject);
        slot2.setLectureTitle(tempSubject + " · Lecture " + slot2.getSlotNumber() + " (Swapped)");
        slot2.setTeacherName(tempTeacherName);
        slot2.setTeacherEmail(tempTeacherEmail);
        slot2.setRoom(tempRoom);
        slot2.setInterchanged(true);
        slot2.setInterchangedWith(slot1.getTeacherName());
        slot2.setInterchangeNote(interchangeNote != null && !interchangeNote.trim().isEmpty() ? 
                interchangeNote : "Interchanged with " + slot1.getTeacherName() + " via faculty mutual communication.");
        slot2.setLastUpdated(System.currentTimeMillis());

        saveAllSlots(allSlots);
        return true;
    }

    public synchronized void resetToDefaultSchedule() {
        List<TimetableSlot> list = buildDefaultScheduleForAllStreams();
        saveAllSlots(list);
    }

    public static String normalizeStreamName(String raw) {
        if (raw == null) return STREAM_SCIENCE;
        String lower = raw.toLowerCase();
        if (lower.contains("sci") || lower.contains("b.sc")) return STREAM_SCIENCE;
        if (lower.contains("com") || lower.contains("b.com")) return STREAM_COMMERCE;
        if (lower.contains("art") || lower.contains("b.a")) return STREAM_ARTS;
        return STREAM_SCIENCE;
    }

    public static String normalizeDayOfWeek(String raw) {
        if (raw == null || raw.trim().isEmpty()) return "Monday";
        String lower = raw.toLowerCase();
        if (lower.contains("mon")) return "Monday";
        if (lower.contains("tue")) return "Tuesday";
        if (lower.contains("wed")) return "Wednesday";
        if (lower.contains("thu")) return "Thursday";
        if (lower.contains("fri")) return "Friday";
        if (lower.contains("sat")) return "Saturday";
        if (lower.contains("sun")) return "Sunday";
        return "Monday";
    }

    private List<TimetableSlot> buildDefaultScheduleForAllStreams() {
        List<TimetableSlot> list = new ArrayList<>();

        // 1. Science Stream Schedule
        buildScienceStreamSchedule(list);

        // 2. Commerce Stream Schedule
        buildCommerceStreamSchedule(list);

        // 3. Arts Stream Schedule
        buildArtsStreamSchedule(list);

        return list;
    }

    private void buildScienceStreamSchedule(List<TimetableSlot> list) {
        String stream = STREAM_SCIENCE;
        String tPramod = "Dr. Pramod Naik";
        String ePramod = "pramod.naik@ycasc.com";
        String tSneha = "Dr. Sneha Gaonkar";
        String eSneha = "sneha.gaonkar@ycasc.com";
        String tVikram = "Prof. Vikram Dessai";
        String eVikram = "vikram.dessai@ycasc.com";

        // Monday
        list.add(new TimetableSlot("BSC_MON_1", stream, "Monday", 1, TIME_SLOT_1_START, TIME_SLOT_1_END, "Chemistry", "Chemistry · Lecture 1", tSneha, eSneha, "Room 204"));
        list.add(new TimetableSlot("BSC_MON_2", stream, "Monday", 2, TIME_SLOT_2_START, TIME_SLOT_2_END, "Physics", "Physics · Lecture 2", tPramod, ePramod, "Physics Lab"));
        list.add(new TimetableSlot("BSC_MON_3", stream, "Monday", 3, TIME_SLOT_3_START, TIME_SLOT_3_END, "Mathematics", "Mathematics · Lecture 3", tVikram, eVikram, "Room 204"));
        list.add(new TimetableSlot("BSC_MON_4", stream, "Monday", 4, TIME_SLOT_4_START, TIME_SLOT_4_END, "Computer Science", "Computer Science · Lecture 4", tVikram, eVikram, "Comp Sci Lab"));
        list.add(new TimetableSlot("BSC_MON_5", stream, "Monday", 5, TIME_SLOT_5_START, TIME_SLOT_5_END, "Chemistry Practical", "Chemistry Practical · Lecture 5", tSneha, eSneha, "Chemistry Lab"));

        // Tuesday
        list.add(new TimetableSlot("BSC_TUE_1", stream, "Tuesday", 1, TIME_SLOT_1_START, TIME_SLOT_1_END, "Physics", "Physics · Lecture 1", tPramod, ePramod, "Room 204"));
        list.add(new TimetableSlot("BSC_TUE_2", stream, "Tuesday", 2, TIME_SLOT_2_START, TIME_SLOT_2_END, "Mathematics", "Mathematics · Lecture 2", tVikram, eVikram, "Room 204"));
        list.add(new TimetableSlot("BSC_TUE_3", stream, "Tuesday", 3, TIME_SLOT_3_START, TIME_SLOT_3_END, "Chemistry", "Chemistry · Lecture 3", tSneha, eSneha, "Chemistry Lab"));
        list.add(new TimetableSlot("BSC_TUE_4", stream, "Tuesday", 4, TIME_SLOT_4_START, TIME_SLOT_4_END, "Physics Practical", "Physics Practical · Lecture 4", tPramod, ePramod, "Physics Lab"));
        list.add(new TimetableSlot("BSC_TUE_5", stream, "Tuesday", 5, TIME_SLOT_5_START, TIME_SLOT_5_END, "Computer Science", "Computer Science · Lecture 5", tVikram, eVikram, "Comp Sci Lab"));

        // Wednesday
        list.add(new TimetableSlot("BSC_WED_1", stream, "Wednesday", 1, TIME_SLOT_1_START, TIME_SLOT_1_END, "Chemistry", "Chemistry · Lecture 1", tSneha, eSneha, "Room 204"));
        list.add(new TimetableSlot("BSC_WED_2", stream, "Wednesday", 2, TIME_SLOT_2_START, TIME_SLOT_2_END, "Physics", "Physics · Lecture 2", tPramod, ePramod, "Physics Lab"));
        list.add(new TimetableSlot("BSC_WED_3", stream, "Wednesday", 3, TIME_SLOT_3_START, TIME_SLOT_3_END, "Mathematics", "Mathematics · Lecture 3", tVikram, eVikram, "Room 204"));
        list.add(new TimetableSlot("BSC_WED_4", stream, "Wednesday", 4, TIME_SLOT_4_START, TIME_SLOT_4_END, "Computer Science", "Computer Science · Lecture 4", tVikram, eVikram, "Comp Sci Lab"));
        list.add(new TimetableSlot("BSC_WED_5", stream, "Wednesday", 5, TIME_SLOT_5_START, TIME_SLOT_5_END, "Electronics", "Electronics & Tech · Lecture 5", tPramod, ePramod, "Room 204"));

        // Thursday
        list.add(new TimetableSlot("BSC_THU_1", stream, "Thursday", 1, TIME_SLOT_1_START, TIME_SLOT_1_END, "Mathematics", "Mathematics · Lecture 1", tVikram, eVikram, "Room 204"));
        list.add(new TimetableSlot("BSC_THU_2", stream, "Thursday", 2, TIME_SLOT_2_START, TIME_SLOT_2_END, "Chemistry", "Chemistry · Lecture 2", tSneha, eSneha, "Chemistry Lab"));
        list.add(new TimetableSlot("BSC_THU_3", stream, "Thursday", 3, TIME_SLOT_3_START, TIME_SLOT_3_END, "Physics", "Physics · Lecture 3", tPramod, ePramod, "Room 204"));
        list.add(new TimetableSlot("BSC_THU_4", stream, "Thursday", 4, TIME_SLOT_4_START, TIME_SLOT_4_END, "Data Structures Lab", "Data Structures Lab · Lecture 4", tVikram, eVikram, "Comp Sci Lab"));
        list.add(new TimetableSlot("BSC_THU_5", stream, "Thursday", 5, TIME_SLOT_5_START, TIME_SLOT_5_END, "Quantum Physics", "Quantum Physics · Lecture 5", tPramod, ePramod, "Physics Lab"));

        // Friday
        list.add(new TimetableSlot("BSC_FRI_1", stream, "Friday", 1, TIME_SLOT_1_START, TIME_SLOT_1_END, "Physics", "Physics · Lecture 1", tPramod, ePramod, "Room 204"));
        list.add(new TimetableSlot("BSC_FRI_2", stream, "Friday", 2, TIME_SLOT_2_START, TIME_SLOT_2_END, "Mathematics", "Mathematics · Lecture 2", tVikram, eVikram, "Room 204"));
        list.add(new TimetableSlot("BSC_FRI_3", stream, "Friday", 3, TIME_SLOT_3_START, TIME_SLOT_3_END, "Chemistry", "Chemistry · Lecture 3", tSneha, eSneha, "Chemistry Lab"));
        list.add(new TimetableSlot("BSC_FRI_4", stream, "Friday", 4, TIME_SLOT_4_START, TIME_SLOT_4_END, "Computer Science", "Computer Science · Lecture 4", tVikram, eVikram, "Comp Sci Lab"));
        list.add(new TimetableSlot("BSC_FRI_5", stream, "Friday", 5, TIME_SLOT_5_START, TIME_SLOT_5_END, "Organic Chemistry Tutorial", "Organic Chemistry · Lecture 5", tSneha, eSneha, "Room 204"));

        // Saturday
        list.add(new TimetableSlot("BSC_SAT_1", stream, "Saturday", 1, TIME_SLOT_1_START, TIME_SLOT_1_END, "Maths Workshop", "Mathematics Workshop · Lecture 1", tVikram, eVikram, "Room 204"));
        list.add(new TimetableSlot("BSC_SAT_2", stream, "Saturday", 2, TIME_SLOT_2_START, TIME_SLOT_2_END, "Physics Seminar", "Physics Seminar · Lecture 2", tPramod, ePramod, "Main Hall A"));
        list.add(new TimetableSlot("BSC_SAT_3", stream, "Saturday", 3, TIME_SLOT_3_START, TIME_SLOT_3_END, "Chemistry Research", "Chemistry Research · Lecture 3", tSneha, eSneha, "Main Hall A"));
        list.add(new TimetableSlot("BSC_SAT_4", stream, "Saturday", 4, TIME_SLOT_4_START, TIME_SLOT_4_END, "CS Project Lab", "CS Project Lab · Lecture 4", tVikram, eVikram, "Comp Sci Lab"));
        list.add(new TimetableSlot("BSC_SAT_5", stream, "Saturday", 5, TIME_SLOT_5_START, TIME_SLOT_5_END, "Science Forum", "Science Forum · Lecture 5", tPramod, ePramod, "Main Hall A"));
    }

    private void buildCommerceStreamSchedule(List<TimetableSlot> list) {
        String stream = STREAM_COMMERCE;
        String tSujata = "Prof. Sujata Kamat";
        String eSujata = "hod.commerce@ycasc.com";
        String tAarti = "Prof. Aarti Faldesai";
        String eAarti = "aarti.faldesai@ycasc.com";
        String tNitin = "Prof. Nitin Shenvi";
        String eNitin = "nitin.shenvi@ycasc.com";

        // Monday
        list.add(new TimetableSlot("COM_MON_1", stream, "Monday", 1, TIME_SLOT_1_START, TIME_SLOT_1_END, "Financial Accounting", "Financial Accounting · Lecture 1", tSujata, eSujata, "Room 101"));
        list.add(new TimetableSlot("COM_MON_2", stream, "Monday", 2, TIME_SLOT_2_START, TIME_SLOT_2_END, "Business Economics", "Business Economics · Lecture 2", tAarti, eAarti, "Room 101"));
        list.add(new TimetableSlot("COM_MON_3", stream, "Monday", 3, TIME_SLOT_3_START, TIME_SLOT_3_END, "Business Law", "Business Law · Lecture 3", tNitin, eNitin, "Room 101"));
        list.add(new TimetableSlot("COM_MON_4", stream, "Monday", 4, TIME_SLOT_4_START, TIME_SLOT_4_END, "Cost Accounting", "Cost Accounting · Lecture 4", tSujata, eSujata, "Room 102"));
        list.add(new TimetableSlot("COM_MON_5", stream, "Monday", 5, TIME_SLOT_5_START, TIME_SLOT_5_END, "Banking & Finance", "Banking & Finance · Lecture 5", tAarti, eAarti, "Room 101"));

        // Tuesday
        list.add(new TimetableSlot("COM_TUE_1", stream, "Tuesday", 1, TIME_SLOT_1_START, TIME_SLOT_1_END, "Business Economics", "Business Economics · Lecture 1", tAarti, eAarti, "Room 101"));
        list.add(new TimetableSlot("COM_TUE_2", stream, "Tuesday", 2, TIME_SLOT_2_START, TIME_SLOT_2_END, "Financial Accounting", "Financial Accounting · Lecture 2", tSujata, eSujata, "Room 101"));
        list.add(new TimetableSlot("COM_TUE_3", stream, "Tuesday", 3, TIME_SLOT_3_START, TIME_SLOT_3_END, "Corporate Law", "Corporate Law · Lecture 3", tNitin, eNitin, "Room 102"));
        list.add(new TimetableSlot("COM_TUE_4", stream, "Tuesday", 4, TIME_SLOT_4_START, TIME_SLOT_4_END, "Auditing Practice", "Auditing Practice · Lecture 4", tSujata, eSujata, "Room 101"));
        list.add(new TimetableSlot("COM_TUE_5", stream, "Tuesday", 5, TIME_SLOT_5_START, TIME_SLOT_5_END, "Business Law", "Business Law · Lecture 5", tNitin, eNitin, "Room 101"));

        // Wednesday
        list.add(new TimetableSlot("COM_WED_1", stream, "Wednesday", 1, TIME_SLOT_1_START, TIME_SLOT_1_END, "Financial Accounting", "Financial Accounting · Lecture 1", tSujata, eSujata, "Room 101"));
        list.add(new TimetableSlot("COM_WED_2", stream, "Wednesday", 2, TIME_SLOT_2_START, TIME_SLOT_2_END, "Banking Institutions", "Banking Institutions · Lecture 2", tAarti, eAarti, "Room 101"));
        list.add(new TimetableSlot("COM_WED_3", stream, "Wednesday", 3, TIME_SLOT_3_START, TIME_SLOT_3_END, "Business Law", "Business Law · Lecture 3", tNitin, eNitin, "Room 101"));
        list.add(new TimetableSlot("COM_WED_4", stream, "Wednesday", 4, TIME_SLOT_4_START, TIME_SLOT_4_END, "Direct Taxation", "Direct Taxation · Lecture 4", tNitin, eNitin, "Room 102"));
        list.add(new TimetableSlot("COM_WED_5", stream, "Wednesday", 5, TIME_SLOT_5_START, TIME_SLOT_5_END, "Cost Accounting", "Cost Accounting · Lecture 5", tSujata, eSujata, "Room 101"));

        // Thursday
        list.add(new TimetableSlot("COM_THU_1", stream, "Thursday", 1, TIME_SLOT_1_START, TIME_SLOT_1_END, "Business Economics", "Business Economics · Lecture 1", tAarti, eAarti, "Room 101"));
        list.add(new TimetableSlot("COM_THU_2", stream, "Thursday", 2, TIME_SLOT_2_START, TIME_SLOT_2_END, "Financial Accounting", "Financial Accounting · Lecture 2", tSujata, eSujata, "Room 101"));
        list.add(new TimetableSlot("COM_THU_3", stream, "Thursday", 3, TIME_SLOT_3_START, TIME_SLOT_3_END, "Business Mathematics", "Business Mathematics · Lecture 3", tNitin, eNitin, "Room 102"));
        list.add(new TimetableSlot("COM_THU_4", stream, "Thursday", 4, TIME_SLOT_4_START, TIME_SLOT_4_END, "Auditing & Assurance", "Auditing & Assurance · Lecture 4", tSujata, eSujata, "Room 101"));
        list.add(new TimetableSlot("COM_THU_5", stream, "Thursday", 5, TIME_SLOT_5_START, TIME_SLOT_5_END, "Commercial Banking", "Commercial Banking · Lecture 5", tAarti, eAarti, "Room 101"));

        // Friday
        list.add(new TimetableSlot("COM_FRI_1", stream, "Friday", 1, TIME_SLOT_1_START, TIME_SLOT_1_END, "Financial Accounting", "Financial Accounting · Lecture 1", tSujata, eSujata, "Room 101"));
        list.add(new TimetableSlot("COM_FRI_2", stream, "Friday", 2, TIME_SLOT_2_START, TIME_SLOT_2_END, "Business Law", "Business Law · Lecture 2", tNitin, eNitin, "Room 101"));
        list.add(new TimetableSlot("COM_FRI_3", stream, "Friday", 3, TIME_SLOT_3_START, TIME_SLOT_3_END, "Macroeconomics", "Macroeconomics · Lecture 3", tAarti, eAarti, "Room 101"));
        list.add(new TimetableSlot("COM_FRI_4", stream, "Friday", 4, TIME_SLOT_4_START, TIME_SLOT_4_END, "Corporate Accounts", "Corporate Accounts · Lecture 4", tSujata, eSujata, "Room 102"));
        list.add(new TimetableSlot("COM_FRI_5", stream, "Friday", 5, TIME_SLOT_5_START, TIME_SLOT_5_END, "Marketing Management", "Marketing Management · Lecture 5", tNitin, eNitin, "Room 101"));

        // Saturday
        list.add(new TimetableSlot("COM_SAT_1", stream, "Saturday", 1, TIME_SLOT_1_START, TIME_SLOT_1_END, "Tally & Accounting Lab", "Tally Lab · Lecture 1", tSujata, eSujata, "Accounts Lab"));
        list.add(new TimetableSlot("COM_SAT_2", stream, "Saturday", 2, TIME_SLOT_2_START, TIME_SLOT_2_END, "Economics Case Studies", "Economics Cases · Lecture 2", tAarti, eAarti, "Room 101"));
        list.add(new TimetableSlot("COM_SAT_3", stream, "Saturday", 3, TIME_SLOT_3_START, TIME_SLOT_3_END, "Corporate Law Seminar", "Corporate Law · Lecture 3", tNitin, eNitin, "Room 102"));
        list.add(new TimetableSlot("COM_SAT_4", stream, "Saturday", 4, TIME_SLOT_4_START, TIME_SLOT_4_END, "Commerce Forum", "Commerce Forum · Lecture 4", tSujata, eSujata, "Room 101"));
        list.add(new TimetableSlot("COM_SAT_5", stream, "Saturday", 5, TIME_SLOT_5_START, TIME_SLOT_5_END, "Stock Market Workshop", "Stock Workshop · Lecture 5", tAarti, eAarti, "Room 101"));
    }

    private void buildArtsStreamSchedule(List<TimetableSlot> list) {
        String stream = STREAM_ARTS;
        String tRobert = "Dr. Robert Vance";
        String eRobert = "hod.arts@ycasc.com";
        String tSanjay = "Prof. Sanjay Bandodkar";
        String eSanjay = "sanjay.bandodkar@ycasc.com";
        String tMeera = "Prof. Meera Chodankar";
        String eMeera = "meera.chodankar@ycasc.com";

        // Monday
        list.add(new TimetableSlot("ART_MON_1", stream, "Monday", 1, TIME_SLOT_1_START, TIME_SLOT_1_END, "English Literature", "English Literature · Lecture 1", tRobert, eRobert, "Room 301"));
        list.add(new TimetableSlot("ART_MON_2", stream, "Monday", 2, TIME_SLOT_2_START, TIME_SLOT_2_END, "History", "History (Civilization) · Lecture 2", tSanjay, eSanjay, "Room 301"));
        list.add(new TimetableSlot("ART_MON_3", stream, "Monday", 3, TIME_SLOT_3_START, TIME_SLOT_3_END, "Political Science", "Political Science · Lecture 3", tMeera, eMeera, "Room 301"));
        list.add(new TimetableSlot("ART_MON_4", stream, "Monday", 4, TIME_SLOT_4_START, TIME_SLOT_4_END, "Psychology", "Psychology · Lecture 4", tRobert, eRobert, "Room 302"));
        list.add(new TimetableSlot("ART_MON_5", stream, "Monday", 5, TIME_SLOT_5_START, TIME_SLOT_5_END, "Sociology", "Sociology · Lecture 5", tMeera, eMeera, "Room 301"));

        // Tuesday
        list.add(new TimetableSlot("ART_TUE_1", stream, "Tuesday", 1, TIME_SLOT_1_START, TIME_SLOT_1_END, "History", "History · Lecture 1", tSanjay, eSanjay, "Room 301"));
        list.add(new TimetableSlot("ART_TUE_2", stream, "Tuesday", 2, TIME_SLOT_2_START, TIME_SLOT_2_END, "Political Science", "Political Science · Lecture 2", tMeera, eMeera, "Room 301"));
        list.add(new TimetableSlot("ART_TUE_3", stream, "Tuesday", 3, TIME_SLOT_3_START, TIME_SLOT_3_END, "English Literature", "English Literature · Lecture 3", tRobert, eRobert, "Room 301"));
        list.add(new TimetableSlot("ART_TUE_4", stream, "Tuesday", 4, TIME_SLOT_4_START, TIME_SLOT_4_END, "Heritage Conservation", "Heritage Conservation · Lecture 4", tSanjay, eSanjay, "Room 302"));
        list.add(new TimetableSlot("ART_TUE_5", stream, "Tuesday", 5, TIME_SLOT_5_START, TIME_SLOT_5_END, "Psychology Lab", "Psychology Lab · Lecture 5", tRobert, eRobert, "Room 301"));

        // Wednesday
        list.add(new TimetableSlot("ART_WED_1", stream, "Wednesday", 1, TIME_SLOT_1_START, TIME_SLOT_1_END, "English Literature", "English Literature · Lecture 1", tRobert, eRobert, "Room 301"));
        list.add(new TimetableSlot("ART_WED_2", stream, "Wednesday", 2, TIME_SLOT_2_START, TIME_SLOT_2_END, "History", "History · Lecture 2", tSanjay, eSanjay, "Room 301"));
        list.add(new TimetableSlot("ART_WED_3", stream, "Wednesday", 3, TIME_SLOT_3_START, TIME_SLOT_3_END, "Political Science", "Political Science · Lecture 3", tMeera, eMeera, "Room 301"));
        list.add(new TimetableSlot("ART_WED_4", stream, "Wednesday", 4, TIME_SLOT_4_START, TIME_SLOT_4_END, "Indian Society", "Indian Society · Lecture 4", tMeera, eMeera, "Room 302"));
        list.add(new TimetableSlot("ART_WED_5", stream, "Wednesday", 5, TIME_SLOT_5_START, TIME_SLOT_5_END, "Literary Criticism", "Literary Criticism · Lecture 5", tRobert, eRobert, "Room 301"));

        // Thursday
        list.add(new TimetableSlot("ART_THU_1", stream, "Thursday", 1, TIME_SLOT_1_START, TIME_SLOT_1_END, "Political Science", "Political Science · Lecture 1", tMeera, eMeera, "Room 301"));
        list.add(new TimetableSlot("ART_THU_2", stream, "Thursday", 2, TIME_SLOT_2_START, TIME_SLOT_2_END, "History", "History · Lecture 2", tSanjay, eSanjay, "Room 301"));
        list.add(new TimetableSlot("ART_THU_3", stream, "Thursday", 3, TIME_SLOT_3_START, TIME_SLOT_3_END, "English Literature", "English Literature · Lecture 3", tRobert, eRobert, "Room 301"));
        list.add(new TimetableSlot("ART_THU_4", stream, "Thursday", 4, TIME_SLOT_4_START, TIME_SLOT_4_END, "Archaeology & Tourism", "Archaeology & Tourism · Lecture 4", tSanjay, eSanjay, "Room 302"));
        list.add(new TimetableSlot("ART_THU_5", stream, "Thursday", 5, TIME_SLOT_5_START, TIME_SLOT_5_END, "Child Psychology", "Child Psychology · Lecture 5", tRobert, eRobert, "Room 301"));

        // Friday
        list.add(new TimetableSlot("ART_FRI_1", stream, "Friday", 1, TIME_SLOT_1_START, TIME_SLOT_1_END, "History", "History · Lecture 1", tSanjay, eSanjay, "Room 301"));
        list.add(new TimetableSlot("ART_FRI_2", stream, "Friday", 2, TIME_SLOT_2_START, TIME_SLOT_2_END, "Political Science", "Political Science · Lecture 2", tMeera, eMeera, "Room 301"));
        list.add(new TimetableSlot("ART_FRI_3", stream, "Friday", 3, TIME_SLOT_3_START, TIME_SLOT_3_END, "English Literature", "English Literature · Lecture 3", tRobert, eRobert, "Room 301"));
        list.add(new TimetableSlot("ART_FRI_4", stream, "Friday", 4, TIME_SLOT_4_START, TIME_SLOT_4_END, "Constitutional Law", "Constitutional Law · Lecture 4", tMeera, eMeera, "Room 302"));
        list.add(new TimetableSlot("ART_FRI_5", stream, "Friday", 5, TIME_SLOT_5_START, TIME_SLOT_5_END, "Creative Writing", "Creative Writing · Lecture 5", tRobert, eRobert, "Room 301"));

        // Saturday
        list.add(new TimetableSlot("ART_SAT_1", stream, "Saturday", 1, TIME_SLOT_1_START, TIME_SLOT_1_END, "Heritage Seminar", "Heritage Seminar · Lecture 1", tSanjay, eSanjay, "Room 301"));
        list.add(new TimetableSlot("ART_SAT_2", stream, "Saturday", 2, TIME_SLOT_2_START, TIME_SLOT_2_END, "Model UN Debate", "Model UN Debate · Lecture 2", tMeera, eMeera, "Room 302"));
        list.add(new TimetableSlot("ART_SAT_3", stream, "Saturday", 3, TIME_SLOT_3_START, TIME_SLOT_3_END, "Poetry Colloquium", "Poetry Colloquium · Lecture 3", tRobert, eRobert, "Room 301"));
        list.add(new TimetableSlot("ART_SAT_4", stream, "Saturday", 4, TIME_SLOT_4_START, TIME_SLOT_4_END, "Social Survey Workshop", "Social Survey Workshop · Lecture 4", tMeera, eMeera, "Room 302"));
        list.add(new TimetableSlot("ART_SAT_5", stream, "Saturday", 5, TIME_SLOT_5_START, TIME_SLOT_5_END, "Drama & Performing Arts", "Performing Arts · Lecture 5", tRobert, eRobert, "Room 301"));
    }
}
