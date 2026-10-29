package com.example.sams;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SpotPassManager {
    private static final String PREF_NAME = "eventra_spot_passes_pref";
    private static final String KEY_PASSES = "saved_spot_passes";

    private static final String KEY_CLEARED_LEGACY = "cleared_legacy_demo_passes_v3";

    private static SpotPassManager instance;
    private final SharedPreferences prefs;

    private SpotPassManager(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        clearLegacyDemoPasses();
    }

    public static synchronized SpotPassManager getInstance(Context context) {
        if (instance == null) {
            instance = new SpotPassManager(context);
        }
        return instance;
    }

    private static final String KEY_PURGED_ALL_DEMO = "purged_all_demo_passes_v5";

    private void clearLegacyDemoPasses() {
        if (!prefs.getBoolean(KEY_PURGED_ALL_DEMO, false)) {
            prefs.edit()
                    .putString(KEY_PASSES, "[]")
                    .putBoolean(KEY_PURGED_ALL_DEMO, true)
                    .apply();
        }
    }

    public synchronized List<SpotPass> getAllSpotPasses() {
        List<SpotPass> list = new ArrayList<>();
        String json = prefs.getString(KEY_PASSES, "[]");
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                SpotPass p = SpotPass.fromJson(obj);
                if (p != null) {
                    list.add(p);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public synchronized void saveAll(List<SpotPass> list) {
        try {
            JSONArray arr = new JSONArray();
            for (SpotPass p : list) {
                arr.put(p.toJson());
            }
            prefs.edit().putString(KEY_PASSES, arr.toString()).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void addSpotPass(SpotPass pass) {
        if (pass == null) return;
        List<SpotPass> list = getAllSpotPasses();
        list.add(0, pass); // newest at top
        saveAll(list);
    }

    public synchronized List<SpotPass> getSpotPassesForStudent(String studentId) {
        List<SpotPass> all = getAllSpotPasses();
        if (studentId == null || studentId.trim().isEmpty()) {
            return all;
        }
        List<SpotPass> filtered = new ArrayList<>();
        for (SpotPass p : all) {
            if (studentId.equalsIgnoreCase(p.getStudentId())) {
                filtered.add(p);
            }
        }
        return filtered;
    }

    public synchronized boolean updateSpotPass(SpotPass pass) {
        if (pass == null || pass.getId() == null) return false;
        List<SpotPass> list = getAllSpotPasses();
        boolean found = false;
        for (int i = 0; i < list.size(); i++) {
            if (pass.getId().equalsIgnoreCase(list.get(i).getId())) {
                list.set(i, pass);
                found = true;
                break;
            }
        }
        if (found) {
            saveAll(list);
        }
        return found;
    }

    public synchronized SpotPass getLatestSpotPassForStudent(String studentId) {
        List<SpotPass> list = getSpotPassesForStudent(studentId);
        return list.isEmpty() ? null : list.get(0);
    }

    public synchronized List<SpotPass> getApprovedPassesForStudent(String studentId) {
        List<SpotPass> all = getSpotPassesForStudent(studentId);
        List<SpotPass> approved = new ArrayList<>();
        for (SpotPass p : all) {
            if (p.isApproved()) {
                approved.add(p);
            }
        }
        return approved;
    }

    public synchronized boolean deleteSpotPass(String passId) {
        if (passId == null || passId.trim().isEmpty()) return false;
        List<SpotPass> list = getAllSpotPasses();
        boolean removed = false;
        for (int i = 0; i < list.size(); i++) {
            if (passId.equalsIgnoreCase(list.get(i).getId())) {
                list.remove(i);
                removed = true;
                break;
            }
        }
        if (removed) {
            saveAll(list);
        }
        return removed;
    }
}
