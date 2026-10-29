package com.example.sams;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PermissionRequestManager {
    private static final String PREF_NAME = "eventra_permission_requests_pref";
    private static final String KEY_REQUESTS = "saved_permission_requests";

    private static final String KEY_CLEARED_LEGACY = "cleared_legacy_demo_permissions_v3";

    private static PermissionRequestManager instance;
    private final SharedPreferences prefs;

    private PermissionRequestManager(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        clearLegacyDemoPermissions();
    }

    public static synchronized PermissionRequestManager getInstance(Context context) {
        if (instance == null) {
            instance = new PermissionRequestManager(context);
        }
        return instance;
    }

    private static final String KEY_PURGED_ALL_DEMO = "purged_all_demo_permissions_v5";

    private void clearLegacyDemoPermissions() {
        if (!prefs.getBoolean(KEY_PURGED_ALL_DEMO, false)) {
            prefs.edit()
                    .putString(KEY_REQUESTS, "[]")
                    .putBoolean(KEY_PURGED_ALL_DEMO, true)
                    .apply();
        }
    }

    public synchronized List<PermissionRequest> getAllPermissions() {
        List<PermissionRequest> list = new ArrayList<>();
        String json = prefs.getString(KEY_REQUESTS, "[]");
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                PermissionRequest p = PermissionRequest.fromJson(obj);
                if (p != null) {
                    list.add(p);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public synchronized void saveAll(List<PermissionRequest> list) {
        try {
            JSONArray arr = new JSONArray();
            for (PermissionRequest p : list) {
                arr.put(p.toJson());
            }
            prefs.edit().putString(KEY_REQUESTS, arr.toString()).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void addPermission(PermissionRequest req) {
        if (req == null) return;
        List<PermissionRequest> list = getAllPermissions();
        list.add(0, req);
        saveAll(list);
    }

    public synchronized List<PermissionRequest> getPermissionsForStudent(String studentId) {
        List<PermissionRequest> all = getAllPermissions();
        if (studentId == null || studentId.trim().isEmpty()) {
            return all;
        }
        List<PermissionRequest> filtered = new ArrayList<>();
        for (PermissionRequest p : all) {
            if (studentId.equalsIgnoreCase(p.getStudentId())) {
                filtered.add(p);
            }
        }
        return filtered;
    }

    public synchronized boolean updatePermission(PermissionRequest req) {
        if (req == null || req.getId() == null) return false;
        List<PermissionRequest> list = getAllPermissions();
        boolean found = false;
        for (int i = 0; i < list.size(); i++) {
            if (req.getId().equalsIgnoreCase(list.get(i).getId())) {
                list.set(i, req);
                found = true;
                break;
            }
        }
        if (found) {
            saveAll(list);
        }
        return found;
    }

    public synchronized List<PermissionRequest> getAllRequests() {
        return getAllPermissions();
    }

    public synchronized List<PermissionRequest> getRequestsForStudent(String studentId) {
        return getPermissionsForStudent(studentId);
    }

    public synchronized List<PermissionRequest> getApprovedRequestsForStudent(String studentId) {
        List<PermissionRequest> all = getPermissionsForStudent(studentId);
        List<PermissionRequest> approved = new ArrayList<>();
        for (PermissionRequest r : all) {
            if (r.isApproved()) {
                approved.add(r);
            }
        }
        return approved;
    }

    public synchronized boolean deletePermission(String permId) {
        if (permId == null || permId.trim().isEmpty()) return false;
        List<PermissionRequest> list = getAllPermissions();
        boolean removed = false;
        for (int i = 0; i < list.size(); i++) {
            if (permId.equalsIgnoreCase(list.get(i).getId())) {
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
