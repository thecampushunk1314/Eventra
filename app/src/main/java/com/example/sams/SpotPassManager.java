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

    private static SpotPassManager instance;
    private final SharedPreferences prefs;

    private SpotPassManager(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        seedDefaultPassesIfEmpty();
    }

    public static synchronized SpotPassManager getInstance(Context context) {
        if (instance == null) {
            instance = new SpotPassManager(context);
        }
        return instance;
    }

    private void seedDefaultPassesIfEmpty() {
        if (!prefs.contains(KEY_PASSES)) {
            List<SpotPass> list = new ArrayList<>();
            // Baseline demo pass from Home design (Football practice approved by Class & Subject teacher)
            list.add(new SpotPass(
                    "SP-2026-001",
                    "SCI_25_001",
                    "Aditya Kamat",
                    "11th Standard (Science)",
                    "Football practice",
                    "Wed, 21 Oct",
                    "Lecture slot 1–2",
                    "Chemistry & Physics",
                    "Sneha Gaonkar (Subject Teacher)",
                    "Pramod Naik (Class Teacher / HOD)",
                    "Annual inter-college athletic practice trials. Duty leave approved for morning periods.",
                    "Approved",
                    100,
                    "Approved",
                    "Notified & Verified",
                    System.currentTimeMillis() - 86400000L
            ));
            saveAll(list);
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
            if (studentId.equalsIgnoreCase(p.getStudentId()) || p.getStudentId().isEmpty()) {
                filtered.add(p);
            }
        }
        return filtered.isEmpty() ? all : filtered;
    }

    public synchronized SpotPass getLatestSpotPassForStudent(String studentId) {
        List<SpotPass> list = getSpotPassesForStudent(studentId);
        return list.isEmpty() ? null : list.get(0);
    }
}
