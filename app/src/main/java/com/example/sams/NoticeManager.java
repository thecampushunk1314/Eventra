package com.example.sams;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class NoticeManager {
    private static final String PREF_NAME = "eventra_notices_prefs";
    private static final String KEY_NOTICES = "saved_institutional_notices";
    private static final String KEY_LEGACY_CLEARED = "legacy_demo_notices_purged_v3";

    private static NoticeManager instance;
    private final SharedPreferences prefs;

    private NoticeManager(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        clearLegacyDemoNotices();
    }

    public static synchronized NoticeManager getInstance(Context context) {
        if (instance == null) {
            instance = new NoticeManager(context);
        }
        return instance;
    }

    /**
     * Purges pre-seeded demo notices so the notice board starts empty until teachers/admin add them.
     */
    private void clearLegacyDemoNotices() {
        if (!prefs.getBoolean(KEY_LEGACY_CLEARED, false)) {
            List<Notice> current = getAllNotices();
            List<Notice> userCreated = new ArrayList<>();
            for (Notice n : current) {
                if (n == null) continue;
                String id = n.getId() != null ? n.getId() : "";
                String title = n.getTitle() != null ? n.getTitle() : "";
                // Filter out obsolete mock/demo notice IDs and default templates
                if (id.equals("NOT-2026-001") || id.equals("NOT-2026-002") || id.equals("NOT-2026-003") || id.equals("NOT-DEFAULT")
                        || title.contains("Youth Festival") || title.contains("Research Conclave")
                        || title.contains("Assessment & Examination Schedule") || title.contains("Welcome to Eventra")) {
                    continue;
                }
                userCreated.add(n);
            }
            saveNotices(userCreated);
            prefs.edit().putBoolean(KEY_LEGACY_CLEARED, true).apply();
        }
    }

    public synchronized List<Notice> getAllNotices() {
        List<Notice> list = new ArrayList<>();
        String json = prefs.getString(KEY_NOTICES, "[]");
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                list.add(Notice.fromJsonObject(obj));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public synchronized Notice getLatestNotice() {
        List<Notice> all = getAllNotices();
        if (all.isEmpty()) {
            return null;
        }
        return all.get(0);
    }

    public synchronized void addNotice(Notice notice) {
        if (notice == null) return;
        List<Notice> list = getAllNotices();
        list.add(0, notice); // Insert latest at top
        saveNotices(list);
    }

    public synchronized boolean updateNotice(Notice notice) {
        if (notice == null || notice.getId() == null) return false;
        List<Notice> list = getAllNotices();
        boolean found = false;
        for (int i = 0; i < list.size(); i++) {
            if (notice.getId().equals(list.get(i).getId())) {
                list.set(i, notice);
                found = true;
                break;
            }
        }
        if (found) {
            saveNotices(list);
        }
        return found;
    }

    public synchronized boolean deleteNotice(String noticeId) {
        if (noticeId == null || noticeId.trim().isEmpty()) return false;
        List<Notice> list = getAllNotices();
        boolean removed = false;
        for (int i = 0; i < list.size(); i++) {
            if (noticeId.equals(list.get(i).getId())) {
                list.remove(i);
                removed = true;
                break;
            }
        }
        if (removed) {
            saveNotices(list);
        }
        return removed;
    }

    public synchronized Notice getNoticeById(String noticeId) {
        if (noticeId == null || noticeId.trim().isEmpty()) return null;
        for (Notice n : getAllNotices()) {
            if (noticeId.equals(n.getId())) {
                return n;
            }
        }
        return null;
    }

    public synchronized void clearAllNotices() {
        prefs.edit().remove(KEY_NOTICES).apply();
    }

    private synchronized void saveNotices(List<Notice> list) {
        try {
            JSONArray arr = new JSONArray();
            for (Notice n : list) {
                arr.put(n.toJsonObject());
            }
            prefs.edit().putString(KEY_NOTICES, arr.toString()).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
