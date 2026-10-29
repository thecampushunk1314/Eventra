package com.example.sams;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import androidx.annotation.Nullable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class RequestsActivity extends AppCompatActivity {

    private static final int FILTER_ALL = 0;
    private static final int FILTER_SPOT_PASS = 1;
    private static final int FILTER_PERMISSION = 2;
    private static final int FILTER_ACCEPTED = 3;
    private static final int FILTER_PENDING = 4;

    private int currentFilter = FILTER_ALL;

    private UserManager userManager;
    private SpotPassManager spotPassManager;
    private PermissionRequestManager permissionManager;
    private TimetableManager timetableManager;
    private NoticeManager noticeManager;

    private TextView chipAll, chipSpotPass, chipPermission, chipAccepted, chipPending;
    private TextView tvMetricTotal, tvMetricAccepted, tvMetricPending;
    private LinearLayout containerRequestsList;
    private View layoutEmptyRequests;

    private EditText etSearchRequests;
    private android.widget.ImageButton btnClearSearchRequests;
    private String currentSearchQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Keep system navigation bars visible
        WindowCompat.setDecorFitsSystemWindows(getWindow(), true);
        WindowInsetsControllerCompat insetsController = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (insetsController != null) {
            insetsController.show(WindowInsetsCompat.Type.systemBars());
        }

        setContentView(R.layout.activity_requests);

        userManager = UserManager.getInstance(this);
        spotPassManager = SpotPassManager.getInstance(this);
        permissionManager = PermissionRequestManager.getInstance(this);
        timetableManager = TimetableManager.getInstance(this);
        noticeManager = NoticeManager.getInstance(this);

        initViews();
        setupFilterClicks();
        setupNavigation();
        loadAndDisplayRequests();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupNavigation();
        loadAndDisplayRequests();
    }

    private void initViews() {
        chipAll = findViewById(R.id.chipCategoryAll);
        chipSpotPass = findViewById(R.id.chipCategorySpotPass);
        chipPermission = findViewById(R.id.chipCategoryPermission);
        chipAccepted = findViewById(R.id.chipCategoryAccepted);
        chipPending = findViewById(R.id.chipCategoryPending);

        tvMetricTotal = findViewById(R.id.tvMetricTotalCount);
        tvMetricAccepted = findViewById(R.id.tvMetricAcceptedCount);
        tvMetricPending = findViewById(R.id.tvMetricPendingCount);

        containerRequestsList = findViewById(R.id.containerRequestsList);
        layoutEmptyRequests = findViewById(R.id.layoutEmptyRequests);

        etSearchRequests = findViewById(R.id.etSearchRequests);
        btnClearSearchRequests = findViewById(R.id.btnClearSearchRequests);

        if (etSearchRequests != null) {
            etSearchRequests.addTextChangedListener(new android.text.TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    currentSearchQuery = s != null ? s.toString().trim() : "";
                    if (btnClearSearchRequests != null) {
                        btnClearSearchRequests.setVisibility(currentSearchQuery.isEmpty() ? View.GONE : View.VISIBLE);
                    }
                    loadAndDisplayRequests();
                }

                @Override
                public void afterTextChanged(android.text.Editable s) {}
            });
        }

        if (btnClearSearchRequests != null) {
            btnClearSearchRequests.setOnClickListener(v -> {
                if (etSearchRequests != null) {
                    etSearchRequests.setText("");
                }
            });
        }

        findViewById(R.id.btnBackFromRequests).setOnClickListener(v -> finish());
    }

    private void setupFilterClicks() {
        chipAll.setOnClickListener(v -> selectFilter(FILTER_ALL));
        chipSpotPass.setOnClickListener(v -> selectFilter(FILTER_SPOT_PASS));
        chipPermission.setOnClickListener(v -> selectFilter(FILTER_PERMISSION));
        chipAccepted.setOnClickListener(v -> selectFilter(FILTER_ACCEPTED));
        chipPending.setOnClickListener(v -> selectFilter(FILTER_PENDING));
    }

    private void selectFilter(int filter) {
        currentFilter = filter;
        updateFilterTabsUI();
        loadAndDisplayRequests();
    }

    private void updateFilterTabsUI() {
        TextView[] chips = new TextView[]{chipAll, chipSpotPass, chipPermission, chipAccepted, chipPending};
        for (int i = 0; i < chips.length; i++) {
            if (i == currentFilter) {
                chips[i].setBackgroundResource(R.drawable.bg_chip_selected_charcoal);
                chips[i].setTextColor(getColor(R.color.white));
            } else {
                chips[i].setBackgroundResource(R.drawable.bg_chip_unselected_white);
                chips[i].setTextColor(getColor(R.color.charcoal_dark));
            }
        }
    }

    private void setupNavigation() {
        findViewById(R.id.navReqHome).setOnClickListener(v -> finish());

        findViewById(R.id.navReqRequests).setOnClickListener(v -> {
            // Already on Requests
            selectFilter(FILTER_ALL);
        });

        findViewById(R.id.navReqProfile).setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
        });

        User currentUser = userManager.getCurrentUser();
        boolean isStaffOrPrincipal = currentUser != null && (currentUser.isTeacherOrDirector() || currentUser.isPrincipal());
        View btnCreate = findViewById(R.id.navReqCreate);
        if (btnCreate != null) {
            if (isStaffOrPrincipal) {
                btnCreate.setVisibility(View.GONE);
            } else {
                btnCreate.setVisibility(View.VISIBLE);
                btnCreate.setOnClickListener(v -> showCreateRequestChooser());
            }
        }
    }

    private void loadAndDisplayRequests() {
        if (containerRequestsList == null) return;
        containerRequestsList.removeAllViews();

        User currentUser = userManager.getCurrentUser();
        String studentId = currentUser != null ? currentUser.getId() : "";
        boolean isStaffOrPrincipal = currentUser != null && (currentUser.isTeacherOrDirector() || currentUser.isPrincipal());

        List<SpotPass> spotPasses;
        List<PermissionRequest> permissions;

        if (currentUser != null && currentUser.isPrincipal()) {
            spotPasses = spotPassManager.getAllSpotPasses();
            permissions = permissionManager.getAllPermissions();
        } else if (currentUser != null && (currentUser.isTeacherOrDirector() || currentUser.isHOD())) {
            // Filter passes and permissions so Teachers and HODs only see requests from their own stream's students
            String myStream = currentUser.getStream();
            spotPasses = new ArrayList<>();
            for (SpotPass p : spotPassManager.getAllSpotPasses()) {
                User s = userManager.getUserById(p.getStudentId());
                if (s == null) s = userManager.findUserByEmailOrId(p.getStudentId());
                if (s != null && s.getStream().equalsIgnoreCase(myStream)) {
                    spotPasses.add(p);
                } else if (s == null) {
                    String id = p.getStudentId().toUpperCase();
                    if ((myStream.equalsIgnoreCase("Science") && (id.startsWith("SCI") || id.contains("-SCI-"))) ||
                        (myStream.equalsIgnoreCase("Commerce") && (id.startsWith("COM") || id.contains("-COM-"))) ||
                        (myStream.equalsIgnoreCase("Arts") && (id.startsWith("ART") || id.contains("-ART-")))) {
                        spotPasses.add(p);
                    }
                }
            }

            permissions = new ArrayList<>();
            for (PermissionRequest pr : permissionManager.getAllPermissions()) {
                User s = userManager.getUserById(pr.getStudentId());
                if (s == null) s = userManager.findUserByEmailOrId(pr.getStudentId());
                if (s != null && s.getStream().equalsIgnoreCase(myStream)) {
                    permissions.add(pr);
                } else if (s == null) {
                    String id = pr.getStudentId().toUpperCase();
                    if ((myStream.equalsIgnoreCase("Science") && (id.startsWith("SCI") || id.contains("-SCI-"))) ||
                        (myStream.equalsIgnoreCase("Commerce") && (id.startsWith("COM") || id.contains("-COM-"))) ||
                        (myStream.equalsIgnoreCase("Arts") && (id.startsWith("ART") || id.contains("-ART-")))) {
                        permissions.add(pr);
                    }
                }
            }
        } else {
            spotPasses = spotPassManager.getSpotPassesForStudent(studentId);
            permissions = permissionManager.getPermissionsForStudent(studentId);
        }

        // Compute metrics
        int acceptedCount = 0;
        int pendingCount = 0;

        for (SpotPass p : spotPasses) {
            String s = p.getStatus() != null ? p.getStatus().toLowerCase() : "";
            if (s.contains("approved") || s.contains("accepted")) {
                acceptedCount++;
            } else {
                pendingCount++;
            }
        }

        for (PermissionRequest r : permissions) {
            if (r.isApproved()) {
                acceptedCount++;
            } else {
                pendingCount++;
            }
        }

        int totalCount = spotPasses.size() + permissions.size();

        if (tvMetricTotal != null) tvMetricTotal.setText(String.valueOf(totalCount));
        if (tvMetricAccepted != null) tvMetricAccepted.setText(String.valueOf(acceptedCount));
        if (tvMetricPending != null) tvMetricPending.setText(String.valueOf(pendingCount));

        int itemsRendered = 0;

        // Render Spot Passes if applicable
        if (currentFilter == FILTER_ALL || currentFilter == FILTER_SPOT_PASS || currentFilter == FILTER_ACCEPTED || currentFilter == FILTER_PENDING) {
            for (SpotPass pass : spotPasses) {
                if (!matchesSearchQuery(pass, currentSearchQuery)) continue;

                String s = pass.getStatus() != null ? pass.getStatus().toLowerCase() : "";
                boolean isAccepted = s.contains("approved") || s.contains("accepted");
                boolean isPending = !isAccepted;

                if (currentFilter == FILTER_ACCEPTED && !isAccepted) continue;
                if (currentFilter == FILTER_PENDING && !isPending) continue;

                View card = createSpotPassCardView(pass);
                containerRequestsList.addView(card);
                itemsRendered++;
            }
        }

        // Render Permission Requests if applicable
        if (currentFilter == FILTER_ALL || currentFilter == FILTER_PERMISSION || currentFilter == FILTER_ACCEPTED || currentFilter == FILTER_PENDING) {
            for (PermissionRequest perm : permissions) {
                if (!matchesSearchQuery(perm, currentSearchQuery)) continue;

                boolean isAccepted = perm.isApproved();
                boolean isPending = perm.isPending();

                if (currentFilter == FILTER_ACCEPTED && !isAccepted) continue;
                if (currentFilter == FILTER_PENDING && !isPending) continue;

                View card = createPermissionCardView(perm);
                containerRequestsList.addView(card);
                itemsRendered++;
            }
        }

        if (layoutEmptyRequests != null) {
            layoutEmptyRequests.setVisibility(itemsRendered == 0 ? View.VISIBLE : View.GONE);
            TextView tvEmptySubtitle = findViewById(R.id.tvEmptyRequestsSubtitle);
            if (tvEmptySubtitle != null) {
                if (isStaffOrPrincipal) {
                    tvEmptySubtitle.setText("No student spot passes or permission requests found.");
                } else {
                    tvEmptySubtitle.setText("Tap '+ New' to submit a Spot Pass or event permission request.");
                }
            }
        }
    }

    private boolean matchesSearchQuery(SpotPass pass, String query) {
        if (query == null || query.isEmpty()) return true;
        String q = query.toLowerCase();
        return (pass.getStudentName() != null && pass.getStudentName().toLowerCase().contains(q))
                || (pass.getStudentId() != null && pass.getStudentId().toLowerCase().contains(q))
                || (pass.getActivityTitle() != null && pass.getActivityTitle().toLowerCase().contains(q))
                || (pass.getEventType() != null && pass.getEventType().toLowerCase().contains(q))
                || (pass.getReason() != null && pass.getReason().toLowerCase().contains(q))
                || (pass.getDate() != null && pass.getDate().toLowerCase().contains(q))
                || (pass.getTimeSlot() != null && pass.getTimeSlot().toLowerCase().contains(q))
                || (pass.getSubject() != null && pass.getSubject().toLowerCase().contains(q))
                || (pass.getStatus() != null && pass.getStatus().toLowerCase().contains(q))
                || (pass.getId() != null && pass.getId().toLowerCase().contains(q));
    }

    private boolean matchesSearchQuery(PermissionRequest perm, String query) {
        if (query == null || query.isEmpty()) return true;
        String q = query.toLowerCase();
        return (perm.getStudentName() != null && perm.getStudentName().toLowerCase().contains(q))
                || (perm.getStudentId() != null && perm.getStudentId().toLowerCase().contains(q))
                || (perm.getEventTitle() != null && perm.getEventTitle().toLowerCase().contains(q))
                || (perm.getScopeOrDept() != null && perm.getScopeOrDept().toLowerCase().contains(q))
                || (perm.getReason() != null && perm.getReason().toLowerCase().contains(q))
                || (perm.getEventDates() != null && perm.getEventDates().toLowerCase().contains(q))
                || (perm.getStatus() != null && perm.getStatus().toLowerCase().contains(q))
                || (perm.getId() != null && perm.getId().toLowerCase().contains(q));
    }

    private View createSpotPassCardView(SpotPass pass) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundResource(R.drawable.bg_card_teal);
        card.setPadding(dp(16), dp(16), dp(16), dp(16));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, dp(12));
        card.setLayoutParams(params);

        // Header row
        LinearLayout row1 = new LinearLayout(this);
        row1.setOrientation(LinearLayout.HORIZONTAL);
        row1.setGravity(android.view.Gravity.CENTER_VERTICAL);

        ImageView icon = new ImageView(this);
        icon.setLayoutParams(new LinearLayout.LayoutParams(dp(36), dp(36)));
        icon.setBackgroundResource(R.drawable.bg_tile_white);
        icon.setPadding(dp(9), dp(9), dp(9), dp(9));
        icon.setImageResource(R.drawable.ic_file);
        icon.setColorFilter(getColor(R.color.ink));
        row1.addView(icon);

        LinearLayout titleCol = new LinearLayout(this);
        titleCol.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams titleColParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        titleColParams.setMargins(dp(10), 0, 0, 0);
        titleCol.setLayoutParams(titleColParams);

        TextView tvTitle = new TextView(this);
        tvTitle.setText("Spot Pass · " + pass.getActivityTitle());
        tvTitle.setTextColor(getColor(R.color.ink));
        tvTitle.setTextSize(15);
        tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        titleCol.addView(tvTitle);

        TextView tvStudent = new TextView(this);
        tvStudent.setText(pass.getStudentName() + " (" + pass.getStudentClass() + ")");
        tvStudent.setTextColor(getColor(R.color.on_teal_muted));
        tvStudent.setTextSize(11);
        titleCol.addView(tvStudent);

        row1.addView(titleCol);
        card.addView(row1);

        // Status & Slot row
        LinearLayout row2 = new LinearLayout(this);
        row2.setOrientation(LinearLayout.HORIZONTAL);
        row2.setPadding(0, dp(10), 0, 0);

        TextView tvStatus = new TextView(this);
        tvStatus.setBackgroundResource(R.drawable.bg_chip_white);
        String s = pass.getStatus() != null ? pass.getStatus().toLowerCase() : "";
        if (s.contains("approved") || s.contains("active")) {
            tvStatus.setCompoundDrawablesWithIntrinsicBounds(R.drawable.dot_green, 0, 0, 0);
        } else {
            tvStatus.setCompoundDrawablesWithIntrinsicBounds(R.drawable.dot_amber, 0, 0, 0);
        }
        tvStatus.setCompoundDrawablePadding(dp(6));
        tvStatus.setGravity(android.view.Gravity.CENTER_VERTICAL);
        tvStatus.setPadding(dp(12), dp(6), dp(12), dp(6));
        tvStatus.setText(pass.getStatus());
        tvStatus.setTextColor(getColor(R.color.ink));
        tvStatus.setTextSize(12);
        tvStatus.setTypeface(null, android.graphics.Typeface.BOLD);
        row2.addView(tvStatus);

        TextView tvSlot = new TextView(this);
        LinearLayout.LayoutParams slotParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        slotParams.setMargins(dp(8), 0, 0, 0);
        tvSlot.setLayoutParams(slotParams);
        tvSlot.setBackgroundResource(R.drawable.bg_chip_white);
        tvSlot.setPadding(dp(12), dp(6), dp(12), dp(6));
        tvSlot.setText(pass.getTimeSlot());
        tvSlot.setTextColor(getColor(R.color.ink));
        tvSlot.setTextSize(12);
        tvSlot.setTypeface(null, android.graphics.Typeface.BOLD);
        row2.addView(tvSlot);

        card.addView(row2);

        // Progress bar
        LinearProgressIndicator progress = new LinearProgressIndicator(this);
        LinearLayout.LayoutParams progParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        progParams.setMargins(0, dp(12), 0, 0);
        progress.setLayoutParams(progParams);
        progress.setMax(100);
        progress.setProgress(pass.getProgress());
        progress.setIndicatorColor(getColor(pass.getProgress() >= 100 ? R.color.green : R.color.accent));
        progress.setTrackColor(getColor(R.color.track_white));
        progress.setTrackCornerRadius(dp(4));
        progress.setTrackThickness(dp(8));
        card.addView(progress);

        // Stages row
        LinearLayout stagesRow = new LinearLayout(this);
        stagesRow.setOrientation(LinearLayout.HORIZONTAL);
        stagesRow.setPadding(0, dp(6), 0, 0);

        TextView s1 = new TextView(this);
        s1.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        s1.setText("Submitted");
        s1.setTextSize(11);
        s1.setTextColor(getColor(R.color.on_teal_muted));
        stagesRow.addView(s1);

        TextView s2 = new TextView(this);
        s2.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        s2.setGravity(android.view.Gravity.CENTER);
        s2.setText("Class Teacher");
        s2.setTextSize(11);
        s2.setTextColor(getColor(R.color.on_teal_muted));
        stagesRow.addView(s2);

        TextView s3 = new TextView(this);
        s3.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        s3.setGravity(android.view.Gravity.END);
        s3.setText("Subject Teacher");
        s3.setTextSize(11);
        s3.setTextColor(getColor(R.color.ink));
        s3.setTypeface(null, android.graphics.Typeface.BOLD);
        stagesRow.addView(s3);

        card.addView(stagesRow);

        // Footer info (Date & Action prompt)
        LinearLayout footerRow = new LinearLayout(this);
        footerRow.setOrientation(LinearLayout.HORIZONTAL);
        footerRow.setGravity(android.view.Gravity.CENTER_VERTICAL);
        footerRow.setPadding(0, dp(12), 0, 0);

        TextView tvSubjectInfo = new TextView(this);
        tvSubjectInfo.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        tvSubjectInfo.setText("📚 " + pass.getSubject());
        tvSubjectInfo.setTextColor(getColor(R.color.on_teal_muted));
        tvSubjectInfo.setTextSize(12);
        tvSubjectInfo.setTypeface(null, android.graphics.Typeface.BOLD);
        footerRow.addView(tvSubjectInfo);

        TextView tvDate = new TextView(this);
        tvDate.setBackgroundResource(R.drawable.bg_chip_white);
        tvDate.setPadding(dp(12), dp(5), dp(12), dp(5));
        tvDate.setText(pass.getDate());
        tvDate.setTextColor(getColor(R.color.ink));
        tvDate.setTextSize(12);
        tvDate.setTypeface(null, android.graphics.Typeface.BOLD);
        footerRow.addView(tvDate);

        card.addView(footerRow);

        card.setOnClickListener(v -> showSpotPassDetailsDialog(pass));
        return card;
    }

    private View createPermissionCardView(PermissionRequest perm) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundResource(R.drawable.bg_card_periwinkle);
        card.setPadding(dp(16), dp(16), dp(16), dp(16));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, dp(12));
        card.setLayoutParams(params);

        // Header row
        LinearLayout row1 = new LinearLayout(this);
        row1.setOrientation(LinearLayout.HORIZONTAL);
        row1.setGravity(android.view.Gravity.CENTER_VERTICAL);

        ImageView icon = new ImageView(this);
        icon.setLayoutParams(new LinearLayout.LayoutParams(dp(36), dp(36)));
        icon.setBackgroundResource(R.drawable.bg_tile_white);
        icon.setPadding(dp(9), dp(9), dp(9), dp(9));
        icon.setImageResource(R.drawable.ic_file);
        icon.setColorFilter(getColor(R.color.ink));
        row1.addView(icon);

        LinearLayout titleCol = new LinearLayout(this);
        titleCol.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams titleColParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        titleColParams.setMargins(dp(10), 0, 0, 0);
        titleCol.setLayoutParams(titleColParams);

        TextView tvTitle = new TextView(this);
        tvTitle.setText("Permission · " + perm.getEventTitle());
        tvTitle.setTextColor(getColor(R.color.ink));
        tvTitle.setTextSize(15);
        tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        titleCol.addView(tvTitle);

        TextView tvScope = new TextView(this);
        tvScope.setText(perm.getScopeOrDept());
        tvScope.setTextColor(getColor(R.color.on_periwinkle_muted));
        tvScope.setTextSize(11);
        titleCol.addView(tvScope);

        row1.addView(titleCol);
        card.addView(row1);

        // Status & Dates row
        LinearLayout row2 = new LinearLayout(this);
        row2.setOrientation(LinearLayout.HORIZONTAL);
        row2.setPadding(0, dp(10), 0, 0);

        TextView tvStatus = new TextView(this);
        tvStatus.setBackgroundResource(R.drawable.bg_chip_white);
        if (perm.isApproved()) {
            tvStatus.setCompoundDrawablesWithIntrinsicBounds(R.drawable.dot_green, 0, 0, 0);
        } else {
            tvStatus.setCompoundDrawablesWithIntrinsicBounds(R.drawable.dot_amber, 0, 0, 0);
        }
        tvStatus.setCompoundDrawablePadding(dp(6));
        tvStatus.setGravity(android.view.Gravity.CENTER_VERTICAL);
        tvStatus.setPadding(dp(12), dp(6), dp(12), dp(6));
        tvStatus.setText(perm.getStatus());
        tvStatus.setTextColor(getColor(R.color.ink));
        tvStatus.setTextSize(12);
        tvStatus.setTypeface(null, android.graphics.Typeface.BOLD);
        row2.addView(tvStatus);

        TextView tvDates = new TextView(this);
        LinearLayout.LayoutParams datesParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        datesParams.setMargins(dp(8), 0, 0, 0);
        tvDates.setLayoutParams(datesParams);
        tvDates.setBackgroundResource(R.drawable.bg_chip_white);
        tvDates.setPadding(dp(12), dp(6), dp(12), dp(6));
        tvDates.setText(perm.getEventDates());
        tvDates.setTextColor(getColor(R.color.ink));
        tvDates.setTextSize(12);
        tvDates.setTypeface(null, android.graphics.Typeface.BOLD);
        row2.addView(tvDates);

        card.addView(row2);

        // Progress bar
        LinearProgressIndicator progress = new LinearProgressIndicator(this);
        LinearLayout.LayoutParams progParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        progParams.setMargins(0, dp(12), 0, 0);
        progress.setLayoutParams(progParams);
        progress.setMax(100);
        progress.setProgress(perm.getProgress());
        progress.setIndicatorColor(getColor(perm.isApproved() ? R.color.green : R.color.accent));
        progress.setTrackColor(getColor(R.color.track_white));
        progress.setTrackCornerRadius(dp(4));
        progress.setTrackThickness(dp(8));
        card.addView(progress);

        // Stages row
        LinearLayout stagesRow = new LinearLayout(this);
        stagesRow.setOrientation(LinearLayout.HORIZONTAL);
        stagesRow.setPadding(0, dp(6), 0, 0);

        TextView s1 = new TextView(this);
        s1.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        s1.setText("Submitted to HOD");
        s1.setTextSize(11);
        s1.setTextColor(getColor(R.color.ink));
        s1.setTypeface(null, android.graphics.Typeface.BOLD);
        stagesRow.addView(s1);

        TextView s2 = new TextView(this);
        s2.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        s2.setGravity(android.view.Gravity.CENTER);
        s2.setText("Organiser Confirmed");
        s2.setTextSize(11);
        s2.setTextColor(getColor(R.color.on_periwinkle_muted));
        stagesRow.addView(s2);

        TextView s3 = new TextView(this);
        s3.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        s3.setGravity(android.view.Gravity.END);
        s3.setText("Attendance Credited");
        s3.setTextSize(11);
        s3.setTextColor(getColor(R.color.on_periwinkle_muted));
        stagesRow.addView(s3);

        card.addView(stagesRow);

        // Footer info (Supervisor & Duty Leave badge) - matches green spot pass card footer exactly
        LinearLayout footerRow = new LinearLayout(this);
        footerRow.setOrientation(LinearLayout.HORIZONTAL);
        footerRow.setGravity(android.view.Gravity.CENTER_VERTICAL);
        footerRow.setPadding(0, dp(12), 0, 0);

        TextView tvSupervisor = new TextView(this);
        tvSupervisor.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        tvSupervisor.setText("🏛️ " + perm.getSupervisor());
        tvSupervisor.setTextColor(getColor(R.color.on_periwinkle_muted));
        tvSupervisor.setTextSize(12);
        tvSupervisor.setTypeface(null, android.graphics.Typeface.BOLD);
        tvSupervisor.setSingleLine(true);
        tvSupervisor.setEllipsize(android.text.TextUtils.TruncateAt.END);
        footerRow.addView(tvSupervisor);

        TextView tvBadge = new TextView(this);
        tvBadge.setBackgroundResource(R.drawable.bg_chip_white);
        tvBadge.setPadding(dp(12), dp(5), dp(12), dp(5));
        tvBadge.setText("Duty Leave");
        tvBadge.setTextColor(getColor(R.color.ink));
        tvBadge.setTextSize(12);
        tvBadge.setTypeface(null, android.graphics.Typeface.BOLD);
        footerRow.addView(tvBadge);

        card.addView(footerRow);

        card.setOnClickListener(v -> showPermissionDetailsDialog(perm));
        return card;
    }

    private void showCreateRequestChooser() {
        User user = userManager.getCurrentUser();
        if (user == null || user.isTeacherOrDirector() || user.isPrincipal()) {
            Toast.makeText(this, "Only students can create leave requests or spot passes", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] options = new String[]{
                "Spot Pass",
                "Permission"
        };

        new AlertDialog.Builder(this)
                .setTitle("Create Official Request")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        showNewSpotPassDialog();
                    } else {
                        showNewPermissionDialog();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showNewSpotPassDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_new_spot_pass, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        User user = userManager.getCurrentUser();
        if (user == null || user.isTeacherOrDirector() || user.isPrincipal()) {
            Toast.makeText(this, "Only students can request a Spot Pass", Toast.LENGTH_SHORT).show();
            return;
        }

        TextView tvAvatar = dialogView.findViewById(R.id.tvSpotPassStudentAvatar);
        TextView tvName = dialogView.findViewById(R.id.tvSpotPassStudentName);
        TextView tvMeta = dialogView.findViewById(R.id.tvSpotPassStudentMeta);

        EditText etActivityTitle = dialogView.findViewById(R.id.etSpotPassActivityTitle);
        Spinner spinnerEventType = dialogView.findViewById(R.id.spinnerEventType);
        View layoutPickPassDate = dialogView.findViewById(R.id.layoutPickPassDate);
        TextView tvPassSelectedDate = dialogView.findViewById(R.id.tvPassSelectedDate);
        EditText etPassTimingNeeded = dialogView.findViewById(R.id.etPassTimingNeeded);
        EditText etTimeSlot = dialogView.findViewById(R.id.etTimeSlot);
        Spinner spinnerSubject = dialogView.findViewById(R.id.spinnerSubject);
        TextView tvSubjectTeacher = dialogView.findViewById(R.id.tvSubjectTeacherName);
        TextView tvClassTeacher = dialogView.findViewById(R.id.tvClassTeacherName);
        EditText etReason = dialogView.findViewById(R.id.etSpotPassReason);

        MaterialButton btnCancel = dialogView.findViewById(R.id.btnCancelSpotPass);
        MaterialButton btnSubmit = dialogView.findViewById(R.id.btnSubmitSpotPass);

        // Category Options: Sports, Cultural, Technical Event, Volunteering, Other
        TextView chipSports = dialogView.findViewById(R.id.chipActSports);
        TextView chipCultural = dialogView.findViewById(R.id.chipActCultural);
        TextView chipTechnical = dialogView.findViewById(R.id.chipActTechnical);
        TextView chipVolunteering = dialogView.findViewById(R.id.chipActVolunteering);
        TextView chipOther = dialogView.findViewById(R.id.chipActOther);
        View containerOther = dialogView.findViewById(R.id.containerOtherEventType);
        EditText etOther = dialogView.findViewById(R.id.etOtherEventType);

        if (tvAvatar != null) tvAvatar.setText(user.getInitials());
        if (tvName != null) tvName.setText(user.getFullName());
        if (tvMeta != null) {
            tvMeta.setText(user.getId() + " • " + (user.getYearOrClass().isEmpty() ? "First Year (Batch 2026)" : user.getYearOrClass()));
        }

        // Determine Stream HOD (format: "Dr. Pramod Naik (HOD - Science)")
        final String assignedHod;
        String dept = user.getDepartment() != null ? user.getDepartment().toLowerCase() : "";
        if (dept.contains("commerce")) {
            assignedHod = "Dr. Anand Vernekar (HOD - Commerce)";
        } else if (dept.contains("arts")) {
            assignedHod = "Dr. Meenakshi Sundaram (HOD - Arts)";
        } else {
            assignedHod = "Dr. Pramod Naik (HOD - Science)";
        }
        if (tvClassTeacher != null) {
            tvClassTeacher.setText(assignedHod);
        }

        final Calendar calendar = Calendar.getInstance();
        final SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault());
        final String[] selectedDateHolder = new String[]{""};
        if (tvPassSelectedDate != null) {
            tvPassSelectedDate.setText("Select Date");
        }

        View.OnClickListener datePickerClick = v -> {
            new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                calendar.set(year, month, dayOfMonth);
                selectedDateHolder[0] = sdf.format(calendar.getTime());
                if (tvPassSelectedDate != null) {
                    tvPassSelectedDate.setText(selectedDateHolder[0]);
                }
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
        };
        if (layoutPickPassDate != null) {
            layoutPickPassDate.setOnClickListener(datePickerClick);
        }
        if (tvPassSelectedDate != null) {
            tvPassSelectedDate.setOnClickListener(datePickerClick);
        }

        // Subject options from student stream
        List<String> subjects = user.getSubjectsList();
        if (subjects.isEmpty()) {
            subjects = Arrays.asList("Chemistry", "Physics", "Mathematics", "Biology", "English", "Computer Applications");
        }
        ArrayAdapter<String> subAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, subjects);
        spinnerSubject.setAdapter(subAdapter);

        spinnerSubject.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String sub = parent.getItemAtPosition(position).toString();
                String resolvedTeacher = userManager.getTeacherForSubject(sub);
                tvSubjectTeacher.setText(resolvedTeacher);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        final String[] selectedCategory = new String[]{"Sports"};

        // Category Chip Listeners
        View.OnClickListener chipListener = v -> {
            if (chipSports != null) chipSports.setBackgroundResource(R.drawable.bg_role_student_unselected);
            if (chipCultural != null) chipCultural.setBackgroundResource(R.drawable.bg_role_student_unselected);
            if (chipTechnical != null) chipTechnical.setBackgroundResource(R.drawable.bg_role_student_unselected);
            if (chipVolunteering != null) chipVolunteering.setBackgroundResource(R.drawable.bg_role_student_unselected);
            if (chipOther != null) chipOther.setBackgroundResource(R.drawable.bg_role_student_unselected);

            v.setBackgroundResource(R.drawable.bg_role_student_selected);

            if (v == chipSports) {
                selectedCategory[0] = "Sports";
                if (containerOther != null) containerOther.setVisibility(View.GONE);
            } else if (v == chipCultural) {
                selectedCategory[0] = "Cultural";
                if (containerOther != null) containerOther.setVisibility(View.GONE);
            } else if (v == chipTechnical) {
                selectedCategory[0] = "Technical Event";
                if (containerOther != null) containerOther.setVisibility(View.GONE);
            } else if (v == chipVolunteering) {
                selectedCategory[0] = "Volunteering";
                if (containerOther != null) containerOther.setVisibility(View.GONE);
            } else if (v == chipOther) {
                selectedCategory[0] = "Other";
                if (containerOther != null) {
                    containerOther.setVisibility(View.VISIBLE);
                    if (etOther != null) etOther.requestFocus();
                }
            }
        };

        if (chipSports != null) chipSports.setOnClickListener(chipListener);
        if (chipCultural != null) chipCultural.setOnClickListener(chipListener);
        if (chipTechnical != null) chipTechnical.setOnClickListener(chipListener);
        if (chipVolunteering != null) chipVolunteering.setOnClickListener(chipListener);
        if (chipOther != null) chipOther.setOnClickListener(chipListener);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSubmit.setOnClickListener(v -> {
            String actTitle = etActivityTitle.getText().toString().trim();
            if (actTitle.isEmpty()) {
                etActivityTitle.setError("Please enter activity / event title");
                return;
            }

            String selectedEventType = selectedCategory[0];
            if ("Other".equalsIgnoreCase(selectedEventType) && etOther != null) {
                String otherVal = etOther.getText().toString().trim();
                if (!otherVal.isEmpty()) {
                    selectedEventType = "Other (" + otherVal + ")";
                }
            }

            String passDate = selectedDateHolder[0];
            if (passDate.isEmpty() || "Select Date".equals(passDate)) {
                Toast.makeText(this, "Please select the date for the Spot Pass", Toast.LENGTH_SHORT).show();
                return;
            }

            String selectedSlot = etTimeSlot.getText().toString().trim();
            if (selectedSlot.isEmpty()) {
                etTimeSlot.setError("Please enter clashing lecture / period");
                return;
            }
            String selectedSub = spinnerSubject.getSelectedItem().toString();
            String subTeacher = tvSubjectTeacher.getText().toString();
            String ctTeacher = tvClassTeacher.getText().toString();
            String passTiming = etPassTimingNeeded.getText().toString().trim();
            if (passTiming.isEmpty()) {
                passTiming = selectedSlot;
            }

            String reason = etReason.getText().toString().trim();
            if (reason.isEmpty()) {
                reason = selectedEventType + " during " + selectedSlot + ". Attendance duty credit applied.";
            }

            String newId = "SP-2026-" + (int)(100 + Math.random() * 900);

            SpotPass newPass = new SpotPass(
                    newId,
                    user.getId(),
                    user.getFullName(),
                    user.getYearOrClass(),
                    actTitle,
                    passDate,
                    selectedSlot + " (" + passTiming + ")",
                    selectedSub,
                    subTeacher,
                    ctTeacher,
                    reason,
                    "Pending CT",
                    65,
                    "Pending Review",
                    "Notified for " + selectedSub,
                    System.currentTimeMillis(),
                    selectedEventType
            );

            spotPassManager.addSpotPass(newPass);
            loadAndDisplayRequests();
            dialog.dismiss();

            Toast.makeText(this, "🎉 Spot Pass " + newId + " Dispatched!", Toast.LENGTH_LONG).show();
        });

        dialog.show();
    }

    private void showNewPermissionDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_new_permission, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        User user = userManager.getCurrentUser();
        if (user == null || user.isTeacherOrDirector() || user.isPrincipal()) {
            Toast.makeText(this, "Only students can submit a permission request", Toast.LENGTH_SHORT).show();
            return;
        }

        // Student Info
        TextView tvAvatar = dialogView.findViewById(R.id.tvPermStudentAvatar);
        TextView tvName = dialogView.findViewById(R.id.tvPermStudentName);
        TextView tvMeta = dialogView.findViewById(R.id.tvPermStudentMeta);
        TextView tvHod = dialogView.findViewById(R.id.tvPermHodName);

        // Category Chips
        TextView chipSports = dialogView.findViewById(R.id.chipPermSports);
        TextView chipCultural = dialogView.findViewById(R.id.chipPermCultural);
        TextView chipTechnical = dialogView.findViewById(R.id.chipPermTechnical);
        TextView chipVolunteering = dialogView.findViewById(R.id.chipPermVolunteering);
        TextView chipOther = dialogView.findViewById(R.id.chipPermOther);
        View containerOther = dialogView.findViewById(R.id.containerPermOtherEventType);
        EditText etOther = dialogView.findViewById(R.id.etPermOtherEventType);

        // Input Fields
        EditText etTitle = dialogView.findViewById(R.id.etNewPermTitle);
        View layoutStartDate = dialogView.findViewById(R.id.layoutPermStartDate);
        View layoutEndDate = dialogView.findViewById(R.id.layoutPermEndDate);
        TextView tvStartDate = dialogView.findViewById(R.id.tvPermStartDate);
        TextView tvEndDate = dialogView.findViewById(R.id.tvPermEndDate);
        EditText etDates = dialogView.findViewById(R.id.etNewPermDates);
        EditText etTiming = dialogView.findViewById(R.id.etPermDailyTiming);
        EditText etOrganiser = dialogView.findViewById(R.id.etNewPermScope);
        EditText etReason = dialogView.findViewById(R.id.etNewPermReason);

        MaterialButton btnCancel = dialogView.findViewById(R.id.btnCancelNewPerm);
        MaterialButton btnSubmit = dialogView.findViewById(R.id.btnSubmitNewPerm);

        // Populate student info
        if (tvAvatar != null) tvAvatar.setText(user.getInitials());
        if (tvName != null) tvName.setText(user.getFullName());
        String metaText = user.getId() + " • " + (user.getYearOrClass().isEmpty() ? "11th Standard" : user.getYearOrClass())
                + " (" + (user.getDepartment().isEmpty() ? "Science" : user.getDepartment()) + ")";
        if (tvMeta != null) tvMeta.setText(metaText);

        // Determine Stream HOD
        final String assignedHod;
        String dept = user.getDepartment() != null ? user.getDepartment().toLowerCase() : "";
        if (dept.contains("commerce")) {
            assignedHod = "Dr. Anand Vernekar (HOD - Commerce)";
        } else if (dept.contains("arts")) {
            assignedHod = "Dr. Meenakshi Sundaram (HOD - Arts)";
        } else {
            assignedHod = "Dr. Pramod Naik (HOD - Science)";
        }
        if (tvHod != null) tvHod.setText(assignedHod);

        // Default category
        final String[] selectedCategory = new String[]{"Sports"};
        final String[] startDateHolder = new String[]{""};
        final String[] endDateHolder = new String[]{""};

        // Interactive Date Pickers for Start Date & End Date
        if (layoutStartDate != null) {
            layoutStartDate.setOnClickListener(v -> {
                Calendar c = Calendar.getInstance();
                DatePickerDialog dpd = new DatePickerDialog(this, (dp, y, m, d) -> {
                    Calendar sel = Calendar.getInstance();
                    sel.set(y, m, d);
                    SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.US);
                    startDateHolder[0] = sdf.format(sel.getTime());
                    if (tvStartDate != null) tvStartDate.setText(startDateHolder[0]);
                    if (!endDateHolder[0].isEmpty() && etDates != null) {
                        etDates.setText(startDateHolder[0] + " – " + endDateHolder[0]);
                    } else if (etDates != null) {
                        etDates.setText(startDateHolder[0]);
                    }
                }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
                dpd.show();
            });
        }

        if (layoutEndDate != null) {
            layoutEndDate.setOnClickListener(v -> {
                Calendar c = Calendar.getInstance();
                DatePickerDialog dpd = new DatePickerDialog(this, (dp, y, m, d) -> {
                    Calendar sel = Calendar.getInstance();
                    sel.set(y, m, d);
                    SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.US);
                    endDateHolder[0] = sdf.format(sel.getTime());
                    if (tvEndDate != null) tvEndDate.setText(endDateHolder[0]);
                    if (!startDateHolder[0].isEmpty() && etDates != null) {
                        etDates.setText(startDateHolder[0] + " – " + endDateHolder[0]);
                    } else if (etDates != null) {
                        etDates.setText(endDateHolder[0]);
                    }
                }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
                dpd.show();
            });
        }

        // Category selection listeners (Switch category without prefilling demo texts)
        View.OnClickListener catListener = v -> {
            if (chipSports != null) chipSports.setBackgroundResource(R.drawable.bg_role_student_unselected);
            if (chipCultural != null) chipCultural.setBackgroundResource(R.drawable.bg_role_student_unselected);
            if (chipTechnical != null) chipTechnical.setBackgroundResource(R.drawable.bg_role_student_unselected);
            if (chipVolunteering != null) chipVolunteering.setBackgroundResource(R.drawable.bg_role_student_unselected);
            if (chipOther != null) chipOther.setBackgroundResource(R.drawable.bg_role_student_unselected);

            if (chipSports != null) chipSports.setTextColor(getColor(R.color.text_muted));
            if (chipCultural != null) chipCultural.setTextColor(getColor(R.color.text_muted));
            if (chipTechnical != null) chipTechnical.setTextColor(getColor(R.color.text_muted));
            if (chipVolunteering != null) chipVolunteering.setTextColor(getColor(R.color.text_muted));
            if (chipOther != null) chipOther.setTextColor(getColor(R.color.text_muted));

            v.setBackgroundResource(R.drawable.bg_role_student_selected);
            ((TextView) v).setTextColor(getColor(R.color.charcoal_dark));

            if (v == chipSports) {
                selectedCategory[0] = "Sports";
                if (containerOther != null) containerOther.setVisibility(View.GONE);
            } else if (v == chipCultural) {
                selectedCategory[0] = "Cultural";
                if (containerOther != null) containerOther.setVisibility(View.GONE);
            } else if (v == chipTechnical) {
                selectedCategory[0] = "Technical Event";
                if (containerOther != null) containerOther.setVisibility(View.GONE);
            } else if (v == chipVolunteering) {
                selectedCategory[0] = "Volunteering";
                if (containerOther != null) containerOther.setVisibility(View.GONE);
            } else if (v == chipOther) {
                selectedCategory[0] = "Other";
                if (containerOther != null) {
                    containerOther.setVisibility(View.VISIBLE);
                    if (etOther != null) etOther.requestFocus();
                }
            }
        };

        if (chipSports != null) chipSports.setOnClickListener(catListener);
        if (chipCultural != null) chipCultural.setOnClickListener(catListener);
        if (chipTechnical != null) chipTechnical.setOnClickListener(catListener);
        if (chipVolunteering != null) chipVolunteering.setOnClickListener(catListener);
        if (chipOther != null) chipOther.setOnClickListener(catListener);

        if (btnCancel != null) btnCancel.setOnClickListener(v -> dialog.dismiss());

        if (btnSubmit != null) {
            btnSubmit.setOnClickListener(v -> {
                String title = etTitle != null ? etTitle.getText().toString().trim() : "";
                if (title.isEmpty()) {
                    if (etTitle != null) etTitle.setError("Please enter event / purpose title");
                    return;
                }

                String cat = selectedCategory[0];
                if ("Other".equalsIgnoreCase(cat) && etOther != null) {
                    String otherVal = etOther.getText().toString().trim();
                    if (otherVal.isEmpty()) {
                        etOther.setError("Please specify other category");
                        return;
                    }
                    cat = "Other (" + otherVal + ")";
                }

                String dates = etDates != null ? etDates.getText().toString().trim() : "";
                if (dates.isEmpty()) {
                    if (!startDateHolder[0].isEmpty() && !endDateHolder[0].isEmpty()) {
                        dates = startDateHolder[0] + " – " + endDateHolder[0];
                    } else if (!startDateHolder[0].isEmpty()) {
                        dates = startDateHolder[0];
                    } else {
                        if (etDates != null) etDates.setError("Please specify event dates / duration");
                        return;
                    }
                }

                String timing = etTiming != null ? etTiming.getText().toString().trim() : "";
                if (timing.isEmpty()) {
                    if (etTiming != null) etTiming.setError("Please enter daily timing / hours");
                    return;
                }

                String organiser = etOrganiser != null ? etOrganiser.getText().toString().trim() : "";
                if (organiser.isEmpty()) {
                    if (etOrganiser != null) etOrganiser.setError("Please enter event organiser / coach name");
                    return;
                }

                String reason = etReason != null ? etReason.getText().toString().trim() : "";
                if (reason.isEmpty()) {
                    reason = "Prior duty leave for " + title + " (" + timing + ") directed to " + assignedHod + ".";
                }

                String newId = "PR-2026-" + (int)(100 + Math.random() * 900);
                String datesWithTiming = dates + " (" + timing + ")";

                PermissionRequest newReq = new PermissionRequest(
                        newId,
                        user.getId(),
                        user.getFullName(),
                        user.getYearOrClass(),
                        title,
                        datesWithTiming,
                        user.getDepartment() + " • Organiser: " + organiser,
                        reason,
                        "Pending Organiser Confirmation",
                        35,
                        assignedHod,
                        "Directed to HOD. Awaiting confirmation from " + organiser + " for attendance credit.",
                        System.currentTimeMillis()
                );

                permissionManager.addPermission(newReq);
                loadAndDisplayRequests();
                dialog.dismiss();

                Toast.makeText(this, "🎉 Prior Permission " + newId + " Dispatched to " + assignedHod + "!", Toast.LENGTH_LONG).show();
            });
        }

        dialog.show();
    }

    private void showSpotPassDetailsDialog(SpotPass pass) {
        if (pass == null) return;

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_spot_pass_details, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        TextView tvPassId = dialogView.findViewById(R.id.tvDetailPassId);
        TextView tvStatusBadge = dialogView.findViewById(R.id.tvDetailStatusBadge);
        TextView tvTitle = dialogView.findViewById(R.id.tvDetailTitle);
        TextView tvStudentMeta = dialogView.findViewById(R.id.tvDetailStudentMeta);
        TextView tvTimeSlot = dialogView.findViewById(R.id.tvDetailTimeSlot);
        TextView tvSubject = dialogView.findViewById(R.id.tvDetailSubject);
        TextView tvClassTeacher = dialogView.findViewById(R.id.tvDetailClassTeacher);
        TextView tvCtStatus = dialogView.findViewById(R.id.tvDetailCtStatus);
        TextView tvSubjectTeacher = dialogView.findViewById(R.id.tvDetailSubjectTeacher);
        TextView tvStStatus = dialogView.findViewById(R.id.tvDetailStStatus);
        TextView tvReason = dialogView.findViewById(R.id.tvDetailReason);
        MaterialButton btnClose = dialogView.findViewById(R.id.btnCloseDetails);

        tvPassId.setText(pass.getId());
        tvStatusBadge.setText(pass.getStatus());
        tvTitle.setText("Spot Pass · " + pass.getActivityTitle());
        tvStudentMeta.setText(pass.getStudentName() + " • " + pass.getStudentId() + " • " + pass.getStudentClass());
        tvTimeSlot.setText(pass.getTimeSlot());
        tvSubject.setText(pass.getSubject());

        tvClassTeacher.setText(pass.getClassTeacher());
        tvCtStatus.setText(pass.getClassTeacherStatus());

        tvSubjectTeacher.setText(pass.getSubjectTeacher());
        tvStStatus.setText(pass.getSubjectTeacherStatus());

        tvReason.setText(pass.getReason());

        // Audit Trail Action History
        android.widget.LinearLayout containerAuditLogs = dialogView.findViewById(R.id.containerAuditTrailLogs);
        if (containerAuditLogs != null) {
            containerAuditLogs.removeAllViews();
            List<SpotPassAction> history = pass.getActionHistory();
            if (history.isEmpty()) {
                pass.addAction(new SpotPassAction(
                        "Student / Applicant",
                        pass.getStudentName(),
                        "Requested",
                        pass.getReason(),
                        "Auto-Notified via Eventra",
                        "Auto-Notified via Eventra"
                ));
                history = pass.getActionHistory();
            }

            float density = getResources().getDisplayMetrics().density;
            for (SpotPassAction action : history) {
                android.widget.LinearLayout logItem = new android.widget.LinearLayout(this);
                logItem.setOrientation(android.widget.LinearLayout.VERTICAL);
                logItem.setPadding(0, (int)(4 * density), 0, (int)(6 * density));

                android.widget.LinearLayout headerRow = new android.widget.LinearLayout(this);
                headerRow.setOrientation(android.widget.LinearLayout.HORIZONTAL);
                headerRow.setGravity(android.view.Gravity.CENTER_VERTICAL);

                TextView tvActionTag = new TextView(this);
                tvActionTag.setText("🔹 " + action.getAction());
                tvActionTag.setTextSize(11.5f);
                tvActionTag.setTypeface(null, android.graphics.Typeface.BOLD);
                tvActionTag.setTextColor(getColor(action.getAction().contains("Reject") ? R.color.chip_red_text : R.color.pastel_lavender_text));
                headerRow.addView(tvActionTag);

                TextView tvTime = new TextView(this);
                tvTime.setText(" • " + action.getFormattedTimestamp());
                tvTime.setTextSize(10f);
                tvTime.setTextColor(getColor(R.color.text_muted));
                headerRow.addView(tvTime);

                logItem.addView(headerRow);

                TextView tvActor = new TextView(this);
                tvActor.setText("By " + action.getActedByName() + " (" + action.getActedByRole() + ")");
                tvActor.setTextSize(11f);
                tvActor.setTextColor(getColor(R.color.charcoal_dark));
                logItem.addView(tvActor);

                if (action.getRemarks() != null && !action.getRemarks().isEmpty()) {
                    TextView tvRemarks = new TextView(this);
                    tvRemarks.setText("“" + action.getRemarks() + "”");
                    tvRemarks.setTextSize(10.5f);
                    tvRemarks.setTextColor(getColor(R.color.text_secondary));
                    logItem.addView(tvRemarks);
                }

                containerAuditLogs.addView(logItem);
            }
        }

        MaterialButton btnDelete = dialogView.findViewById(R.id.btnDeleteSpotPass);
        User currentUser = userManager.getCurrentUser();
        boolean isStaffOrPrincipal = currentUser != null && (currentUser.isTeacherOrDirector() || currentUser.isPrincipal());

        if (btnDelete != null) {
            if (isStaffOrPrincipal) {
                btnDelete.setVisibility(View.GONE);
            } else {
                btnDelete.setVisibility(View.VISIBLE);
                btnDelete.setOnClickListener(v -> {
                    new AlertDialog.Builder(this)
                            .setTitle("Cancel Spot Pass")
                            .setMessage("Do you want to cancel and remove Spot Pass " + pass.getId() + "? You can submit a new pass with updated timings anytime.")
                            .setPositiveButton("Yes, Cancel Pass", (d, which) -> {
                                spotPassManager.deleteSpotPass(pass.getId());
                                dialog.dismiss();
                                loadAndDisplayRequests();
                                Toast.makeText(this, "Spot Pass cancelled and removed.", Toast.LENGTH_SHORT).show();
                            })
                            .setNegativeButton("Keep Pass", null)
                            .show();
                });
            }
        }

        if (isStaffOrPrincipal) {
            btnClose.setText("Review & Take Action");
            btnClose.setOnClickListener(v -> {
                dialog.dismiss();
                String[] reviewActions = new String[]{
                        "✅ Approve as Faculty (Clashing Lecture)",
                        "✅ Approve as HOD (Department Head)",
                        "🏆 Fully Approve & Verify (Both Faculty & HOD)",
                        "❌ Reject Spot Pass Request"
                };
                new AlertDialog.Builder(this)
                        .setTitle("Spot Pass Review: " + pass.getId())
                        .setItems(reviewActions, (d, which) -> {
                            String role = (currentUser != null && currentUser.isPrincipal()) ? "Principal / HOD" : "Faculty / Teacher";
                            String actor = (currentUser != null && currentUser.getFullName() != null) ? currentUser.getFullName() : "Faculty Staff";
                            String actionName;
                            String remarks;

                            if (which == 0) {
                                // Faculty approved -> HOD status is "Approved & Verified"
                                pass.setSubjectTeacherStatus("Approved by " + actor);
                                pass.setClassTeacherStatus("Approved & Verified");
                                pass.setStatus("Approved & Verified");
                                pass.setProgress(100);
                                actionName = "Approved by Faculty";
                                remarks = "Clashing lecture absence sanctioned with duty credit.";
                            } else if (which == 1) {
                                // HOD approved -> Faculty status is "Approved & Verified"
                                pass.setClassTeacherStatus("Approved by " + actor);
                                pass.setSubjectTeacherStatus("Approved & Verified");
                                pass.setStatus("Approved & Verified");
                                pass.setProgress(100);
                                actionName = "Approved by HOD";
                                remarks = "Department Head authorized student leave for event.";
                            } else if (which == 2) {
                                pass.setClassTeacherStatus("Approved & Verified");
                                pass.setSubjectTeacherStatus("Approved & Verified");
                                pass.setStatus("Approved & Verified");
                                pass.setProgress(100);
                                actionName = "Fully Approved & Verified";
                                remarks = "Executive academic clearance granted.";
                            } else {
                                pass.setStatus("Rejected");
                                pass.setProgress(100);
                                actionName = "Rejected";
                                remarks = "Spot Pass request was declined upon faculty review.";
                            }

                            pass.addAction(new SpotPassAction(
                                    role,
                                    actor,
                                    actionName,
                                    remarks,
                                    "Notified via Eventra",
                                    "Recorded in Attendance Register"
                            ));

                            spotPassManager.updateSpotPass(pass);
                            loadAndDisplayRequests();
                            Toast.makeText(this, "Spot Pass " + pass.getId() + " updated: " + pass.getStatus(), Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });
        } else {
            btnClose.setOnClickListener(v -> dialog.dismiss());
        }

        dialog.show();
    }

    private void showPermissionDetailsDialog(PermissionRequest perm) {
        if (perm == null) return;

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_permission_details, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        TextView tvId = dialogView.findViewById(R.id.tvPermDetailId);
        TextView tvStatus = dialogView.findViewById(R.id.tvPermDetailStatusBadge);
        TextView tvTitle = dialogView.findViewById(R.id.tvPermDetailTitle);
        TextView tvStudentMeta = dialogView.findViewById(R.id.tvPermDetailStudentMeta);
        TextView tvDates = dialogView.findViewById(R.id.tvPermDetailDates);
        TextView tvScope = dialogView.findViewById(R.id.tvPermDetailScope);
        TextView tvSupervisor = dialogView.findViewById(R.id.tvPermDetailSupervisor);
        TextView tvRemarks = dialogView.findViewById(R.id.tvPermDetailRemarks);
        TextView tvReason = dialogView.findViewById(R.id.tvPermDetailReason);
        MaterialButton btnClose = dialogView.findViewById(R.id.btnClosePermDetails);

        tvId.setText(perm.getId());
        tvStatus.setText(perm.getStatus());
        tvTitle.setText("Permission · " + perm.getEventTitle());
        tvStudentMeta.setText(perm.getStudentName() + " • " + perm.getStudentId() + " • " + perm.getStudentClass());
        tvDates.setText(perm.getEventDates());
        tvScope.setText(perm.getScopeOrDept());
        tvSupervisor.setText(perm.getSupervisor());
        tvRemarks.setText(perm.getRemarks());
        tvReason.setText(perm.getReason());

        MaterialButton btnDeletePerm = dialogView.findViewById(R.id.btnDeletePermission);
        User currentUser = userManager.getCurrentUser();
        boolean isStaffOrPrincipal = currentUser != null && (currentUser.isTeacherOrDirector() || currentUser.isPrincipal());

        if (btnDeletePerm != null) {
            if (isStaffOrPrincipal) {
                btnDeletePerm.setVisibility(View.GONE);
            } else {
                btnDeletePerm.setVisibility(View.VISIBLE);
                btnDeletePerm.setOnClickListener(v -> {
                    new AlertDialog.Builder(this)
                            .setTitle("Cancel Permission Request")
                            .setMessage("Do you want to cancel and remove Permission Request " + perm.getId() + "? You can submit a new request with updated timings anytime.")
                            .setPositiveButton("Yes, Cancel Request", (d, which) -> {
                                permissionManager.deletePermission(perm.getId());
                                dialog.dismiss();
                                loadAndDisplayRequests();
                                Toast.makeText(this, "Permission request cancelled and removed.", Toast.LENGTH_SHORT).show();
                            })
                            .setNegativeButton("Keep Request", null)
                            .show();
                });
            }
        }

        if (isStaffOrPrincipal) {
            btnClose.setText("Review & Take Action");
            btnClose.setOnClickListener(v -> {
                dialog.dismiss();
                String[] reviewActions = new String[]{
                        "✅ Approve Permission Request",
                        "📝 Add Remarks & Request Verification",
                        "❌ Reject Permission Request"
                };
                new AlertDialog.Builder(this)
                        .setTitle("Permission Review: " + perm.getId())
                        .setItems(reviewActions, (d, which) -> {
                            if (which == 0) {
                                perm.setStatus("Approved");
                                perm.setProgress(100);
                                perm.setRemarks("Sanctioned by " + currentUser.getFullName());
                            } else if (which == 1) {
                                perm.setStatus("Teacher Notified");
                                perm.setProgress(80);
                                perm.setRemarks("Verified by " + currentUser.getFullName() + ". Attendance credits pending final report.");
                            } else {
                                perm.setStatus("Rejected");
                                perm.setProgress(100);
                                perm.setRemarks("Declined by " + currentUser.getFullName());
                            }
                            permissionManager.updatePermission(perm);
                            loadAndDisplayRequests();
                            Toast.makeText(this, "Permission " + perm.getId() + " updated: " + perm.getStatus(), Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });
        } else {
            btnClose.setOnClickListener(v -> dialog.dismiss());
        }

        dialog.show();
    }

    private void showCalendarDialog(String initialDate) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_calendar_view, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        CalendarView calendarView = dialogView.findViewById(R.id.calendarViewMain);
        TextView tvSelectedDate = dialogView.findViewById(R.id.tvSelectedDateTitle);
        TextView tvDateStatus = dialogView.findViewById(R.id.tvSelectedDateStatus);
        TextView tvDayDutyBadge = dialogView.findViewById(R.id.tvDayDutyBadge);
        TextView tvCalendarSubtitle = dialogView.findViewById(R.id.tvCalendarSubtitle);

        View layoutTimetableSection = dialogView.findViewById(R.id.layoutTimetableSection);
        LinearLayout containerTimetableEntries = dialogView.findViewById(R.id.containerTimetableEntries);
        TextView btnTeacherInterchangeAction = dialogView.findViewById(R.id.btnTeacherInterchangeAction);
        View layoutStudentPassesSection = dialogView.findViewById(R.id.layoutStudentPassesSection);
        View layoutPrincipalAllowedSection = dialogView.findViewById(R.id.layoutPrincipalAllowedSection);
        TextView tvPrincipalNoAllowedRequests = dialogView.findViewById(R.id.tvPrincipalNoAllowedRequests);
        LinearLayout containerPrincipalAllowedList = dialogView.findViewById(R.id.containerPrincipalAllowedList);

        TextView tvCalendarNoPasses = dialogView.findViewById(R.id.tvCalendarNoPasses);
        View cardPassPreview = dialogView.findViewById(R.id.cardCalendarPassPreview);
        TextView tvCalPassTitle = dialogView.findViewById(R.id.tvCalPassTitle);
        TextView tvCalPassStatusBadge = dialogView.findViewById(R.id.tvCalPassStatusBadge);
        TextView tvCalPassDetails = dialogView.findViewById(R.id.tvCalPassDetails);
        MaterialButton btnClose = dialogView.findViewById(R.id.btnCloseCalendar);
        MaterialButton btnCreatePass = dialogView.findViewById(R.id.btnCreatePassForDate);

        User currentUser = userManager.getCurrentUser();
        boolean isPrincipal = currentUser != null && currentUser.isPrincipal();
        boolean isTeacher = currentUser != null && currentUser.isTeacherOrDirector();

        if (isPrincipal || isTeacher) {
            if (tvCalendarSubtitle != null) {
                tvCalendarSubtitle.setText(isPrincipal
                        ? "Institutional Approvals & Granted Student Passes"
                        : (currentUser.getDepartment() + " • Timetable & Routine"));
            }
            if (layoutTimetableSection != null) layoutTimetableSection.setVisibility(isPrincipal ? View.GONE : View.VISIBLE);
            if (layoutStudentPassesSection != null) layoutStudentPassesSection.setVisibility(isPrincipal ? View.GONE : View.VISIBLE);
            if (layoutPrincipalAllowedSection != null) layoutPrincipalAllowedSection.setVisibility(isPrincipal ? View.VISIBLE : View.GONE);
            if (btnCreatePass != null) {
                btnCreatePass.setVisibility(View.GONE);
            }
        } else {
            if (layoutTimetableSection != null) layoutTimetableSection.setVisibility(View.VISIBLE);
            if (layoutStudentPassesSection != null) layoutStudentPassesSection.setVisibility(View.VISIBLE);
            if (layoutPrincipalAllowedSection != null) layoutPrincipalAllowedSection.setVisibility(View.GONE);
            if (tvCalendarSubtitle != null && currentUser != null) {
                tvCalendarSubtitle.setText(currentUser.getDepartment() + " • Timetable & Routine");
            }
        }

        final String[] currentDateHolder = new String[]{
                initialDate != null && !initialDate.isEmpty() ? initialDate : "Wed, 21 Oct 2026"
        };

        boolean isTeacherOrAdmin = currentUser != null && (currentUser.isTeacherOrDirector() || currentUser.isPrincipal());
        if (btnTeacherInterchangeAction != null) {
            btnTeacherInterchangeAction.setVisibility(isTeacherOrAdmin ? View.VISIBLE : View.GONE);
        }

        final Runnable[] updateDateDisplay = new Runnable[1];
        updateDateDisplay[0] = () -> {
            tvSelectedDate.setText(currentDateHolder[0]);

            if (isPrincipal) {
                if (containerPrincipalAllowedList != null) {
                    containerPrincipalAllowedList.removeAllViews();
                }

                List<SpotPass> allPasses = spotPassManager.getAllSpotPasses();
                List<PermissionRequest> allPerms = permissionManager.getAllRequests();
                int allowedCount = 0;

                for (SpotPass p : allPasses) {
                    if (p.isApproved()) {
                        allowedCount++;
                        LinearLayout card = new LinearLayout(this);
                        card.setOrientation(LinearLayout.VERTICAL);
                        card.setBackgroundResource(R.drawable.bg_input_box);
                        card.setPadding(22, 16, 22, 16);

                        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        lp.topMargin = 10;
                        card.setLayoutParams(lp);

                        LinearLayout r1 = new LinearLayout(this);
                        r1.setOrientation(LinearLayout.HORIZONTAL);
                        r1.setGravity(android.view.Gravity.CENTER_VERTICAL);

                        TextView titleTv = new TextView(this);
                        LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
                        titleTv.setLayoutParams(titleLp);
                        titleTv.setText("⚡ [Spot Pass] " + p.getActivityTitle());
                        titleTv.setTextColor(getColor(R.color.charcoal_dark));
                        titleTv.setTextSize(13f);
                        titleTv.setTypeface(null, android.graphics.Typeface.BOLD);
                        r1.addView(titleTv);

                        TextView badgeTv = new TextView(this);
                        badgeTv.setBackgroundResource(R.drawable.bg_chip_mint);
                        badgeTv.setText("Allowed");
                        badgeTv.setTextColor(getColor(R.color.pastel_mint_text));
                        badgeTv.setTextSize(10.5f);
                        badgeTv.setTypeface(null, android.graphics.Typeface.BOLD);
                        badgeTv.setPadding(14, 4, 14, 4);
                        r1.addView(badgeTv);
                        card.addView(r1);

                        TextView metaTv = new TextView(this);
                        metaTv.setText("Student: " + p.getStudentName() + " (" + p.getStudentRoll() + ") • " + p.getTimeSlot());
                        metaTv.setTextColor(getColor(R.color.text_secondary));
                        metaTv.setTextSize(11.5f);
                        metaTv.setPadding(0, 4, 0, 0);
                        card.addView(metaTv);

                        if (containerPrincipalAllowedList != null) {
                            containerPrincipalAllowedList.addView(card);
                        }
                    }
                }

                for (PermissionRequest r : allPerms) {
                    if (r.isApproved()) {
                        allowedCount++;
                        LinearLayout card = new LinearLayout(this);
                        card.setOrientation(LinearLayout.VERTICAL);
                        card.setBackgroundResource(R.drawable.bg_input_box);
                        card.setPadding(22, 16, 22, 16);

                        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        lp.topMargin = 10;
                        card.setLayoutParams(lp);

                        LinearLayout r1 = new LinearLayout(this);
                        r1.setOrientation(LinearLayout.HORIZONTAL);
                        r1.setGravity(android.view.Gravity.CENTER_VERTICAL);

                        TextView titleTv = new TextView(this);
                        LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
                        titleTv.setLayoutParams(titleLp);
                        titleTv.setText("📋 [Permission] " + r.getTitle());
                        titleTv.setTextColor(getColor(R.color.charcoal_dark));
                        titleTv.setTextSize(13f);
                        titleTv.setTypeface(null, android.graphics.Typeface.BOLD);
                        r1.addView(titleTv);

                        TextView badgeTv = new TextView(this);
                        badgeTv.setBackgroundResource(R.drawable.bg_chip_mint);
                        badgeTv.setText("Approved");
                        badgeTv.setTextColor(getColor(R.color.pastel_mint_text));
                        badgeTv.setTextSize(10.5f);
                        badgeTv.setTypeface(null, android.graphics.Typeface.BOLD);
                        badgeTv.setPadding(14, 4, 14, 4);
                        r1.addView(badgeTv);
                        card.addView(r1);

                        TextView metaTv = new TextView(this);
                        metaTv.setText("Applicant: " + r.getApplicantName() + " (" + r.getApplicantRoll() + ") • " + r.getFormattedDateRange());
                        metaTv.setTextColor(getColor(R.color.text_secondary));
                        metaTv.setTextSize(11.5f);
                        metaTv.setPadding(0, 4, 0, 0);
                        card.addView(metaTv);

                        if (containerPrincipalAllowedList != null) {
                            containerPrincipalAllowedList.addView(card);
                        }
                    }
                }

                if (allowedCount == 0) {
                    if (tvPrincipalNoAllowedRequests != null) {
                        tvPrincipalNoAllowedRequests.setVisibility(View.VISIBLE);
                        tvPrincipalNoAllowedRequests.setText("No student passes or permissions allowed for " + currentDateHolder[0]);
                    }
                } else {
                    if (tvPrincipalNoAllowedRequests != null) {
                        tvPrincipalNoAllowedRequests.setVisibility(View.GONE);
                    }
                }

                if (tvDateStatus != null) {
                    tvDateStatus.setText(allowedCount + " Student Duty Passes & Permissions Allowed by Institution");
                }
                if (tvDayDutyBadge != null) {
                    tvDayDutyBadge.setText(allowedCount + " Allowed");
                }

            } else {
                // Dynamic Timetable & Student Spot Pass
                renderTimetableForDate(containerTimetableEntries, currentDateHolder[0], currentUser, dialog, updateDateDisplay[0]);

                if (btnTeacherInterchangeAction != null) {
                    btnTeacherInterchangeAction.setOnClickListener(v -> showInterchangeLectureDialog(dialog, updateDateDisplay[0]));
                }

                User u = userManager.getCurrentUser();
                List<SpotPass> studentPasses = spotPassManager.getSpotPassesForStudent(u != null ? u.getId() : "");

                SpotPass matchingPass = null;
                for (SpotPass p : studentPasses) {
                    if (p.getDate() != null && !p.getDate().trim().isEmpty()) {
                        String passDate = p.getDate().trim().toLowerCase();
                        String curDate = currentDateHolder[0].trim().toLowerCase();
                        if (curDate.contains(passDate) || passDate.contains(curDate)) {
                            matchingPass = p;
                            break;
                        }
                    }
                }

                if (matchingPass != null) {
                    if (tvCalendarNoPasses != null) tvCalendarNoPasses.setVisibility(View.GONE);
                    if (cardPassPreview != null) cardPassPreview.setVisibility(View.VISIBLE);
                    if (tvCalPassTitle != null) tvCalPassTitle.setText(matchingPass.getActivityTitle());
                    if (tvCalPassStatusBadge != null) tvCalPassStatusBadge.setText(matchingPass.getStatus());
                    if (tvCalPassDetails != null) {
                        tvCalPassDetails.setText(matchingPass.getTimeSlot() + " • " + matchingPass.getSubject() + " (" + matchingPass.getSubjectTeacher() + ")");
                    }
                    if (tvDateStatus != null) {
                        tvDateStatus.setText("5 Scheduled Lectures • 1 Duty Pass Active (" + matchingPass.getStatus() + ")");
                    }
                    if (tvDayDutyBadge != null) tvDayDutyBadge.setText("Duty Leave Active");

                    final SpotPass clicked = matchingPass;
                    if (cardPassPreview != null) {
                        cardPassPreview.setOnClickListener(v -> {
                            dialog.dismiss();
                            showSpotPassDetailsDialog(clicked);
                        });
                    }
                } else {
                    if (tvCalendarNoPasses != null) tvCalendarNoPasses.setVisibility(View.VISIBLE);
                    if (cardPassPreview != null) cardPassPreview.setVisibility(View.GONE);
                    if (tvDateStatus != null) {
                        tvDateStatus.setText("Regular Academic Routine • 5 Lectures Scheduled");
                    }
                    if (tvDayDutyBadge != null) tvDayDutyBadge.setText("Regular Day");
                }
            }
        };

        updateDateDisplay[0].run();

        if (calendarView != null) {
            calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
                Calendar c = Calendar.getInstance();
                c.set(year, month, dayOfMonth);
                SimpleDateFormat formatter = new SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault());
                currentDateHolder[0] = formatter.format(c.getTime());
                updateDateDisplay[0].run();
            });
        }

        if (btnClose != null) {
            btnClose.setOnClickListener(v -> dialog.dismiss());
        }

        if (!isPrincipal && !isTeacher && btnCreatePass != null) {
            btnCreatePass.setOnClickListener(v -> {
                dialog.dismiss();
                showNewSpotPassDialog();
            });
        }

        dialog.show();

        if (dialog.getWindow() != null) {
            android.util.DisplayMetrics dm = getResources().getDisplayMetrics();
            int h = (int) (dm.heightPixels * 0.88);
            int w = (int) (dm.widthPixels * 0.94);
            dialog.getWindow().setLayout(w, h);
        }
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }

    private String parseDayOfWeekFromDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) return "Monday";
        String lower = dateStr.toLowerCase();
        if (lower.contains("mon")) return "Monday";
        if (lower.contains("tue")) return "Tuesday";
        if (lower.contains("wed")) return "Wednesday";
        if (lower.contains("thu")) return "Thursday";
        if (lower.contains("fri")) return "Friday";
        if (lower.contains("sat")) return "Saturday";
        if (lower.contains("sun")) return "Sunday";
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy", Locale.US);
            java.util.Date d = sdf.parse(dateStr);
            if (d != null) {
                SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.US);
                return dayFormat.format(d);
            }
        } catch (Exception ignored) {}
        return "Monday";
    }

    private void renderTimetableForDate(LinearLayout container, String dateStr,
                                        User user, @Nullable AlertDialog parentDialog,
                                        @Nullable Runnable onRefreshCallback) {
        if (container == null) return;
        container.removeAllViews();

        String dayOfWeek = parseDayOfWeekFromDate(dateStr);
        if ("Sunday".equalsIgnoreCase(dayOfWeek)) {
            TextView tvSun = new TextView(this);
            tvSun.setText("🎉 Sunday • Institutional Holiday & Self-Study Day\nNo academic lectures scheduled.");
            tvSun.setTextColor(getColor(R.color.charcoal_dark));
            tvSun.setTextSize(13f);
            tvSun.setPadding(dp(16), dp(24), dp(16), dp(24));
            tvSun.setGravity(android.view.Gravity.CENTER);
            container.addView(tvSun);
            return;
        }

        List<TimetableSlot> slots = timetableManager.getTimetableForUser(user, dayOfWeek);
        List<SpotPass> studentPasses = user != null ? spotPassManager.getSpotPassesForStudent(user.getId()) : new ArrayList<>();
        boolean isTeacherOrAdmin = user != null && (user.isTeacherOrDirector() || user.isPrincipal());

        for (int i = 0; i < slots.size(); i++) {
            TimetableSlot slot = slots.get(i);

            boolean hasPassClash = false;
            for (SpotPass p : studentPasses) {
                if (p.getDate() != null && !p.getDate().trim().isEmpty()) {
                    String pDate = p.getDate().trim().toLowerCase();
                    String cDate = dateStr.trim().toLowerCase();
                    if (cDate.contains(pDate) || pDate.contains(cDate)) {
                        if (p.getTimeSlot() != null && (p.getTimeSlot().contains(slot.getStartTime()) || slot.getTimeDisplay().contains(p.getTimeSlot()))) {
                            hasPassClash = true;
                            break;
                        }
                        if (p.getSubject() != null && slot.getSubject() != null && p.getSubject().equalsIgnoreCase(slot.getSubject())) {
                            hasPassClash = true;
                            break;
                        }
                    }
                }
            }

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(android.view.Gravity.CENTER_VERTICAL);
            row.setPadding(0, dp(4), 0, dp(4));

            TextView tvTime = new TextView(this);
            LinearLayout.LayoutParams timeLp = new LinearLayout.LayoutParams(dp(74), LinearLayout.LayoutParams.WRAP_CONTENT);
            tvTime.setLayoutParams(timeLp);
            tvTime.setText(slot.getStartTime());
            tvTime.setTextColor(getColor(R.color.pastel_lavender_text));
            tvTime.setTextSize(11f);
            tvTime.setTypeface(null, android.graphics.Typeface.BOLD);
            row.addView(tvTime);

            LinearLayout midCol = new LinearLayout(this);
            midCol.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams midLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
            midCol.setLayoutParams(midLp);

            TextView tvTitle = new TextView(this);
            tvTitle.setText(slot.getLectureTitle());
            tvTitle.setTextColor(getColor(R.color.charcoal_dark));
            tvTitle.setTextSize(12.5f);
            tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);
            midCol.addView(tvTitle);

            TextView tvMeta = new TextView(this);
            tvMeta.setText(slot.getTeacherName() + " • " + slot.getRoom());
            tvMeta.setTextColor(getColor(R.color.pastel_lavender_text));
            tvMeta.setTextSize(11f);
            midCol.addView(tvMeta);

            if (slot.isInterchanged()) {
                TextView tvSwapNote = new TextView(this);
                tvSwapNote.setText("🔄 " + (slot.getInterchangeNote().isEmpty() ? "Swapped with " + slot.getInterchangedWith() : slot.getInterchangeNote()));
                tvSwapNote.setTextColor(getColor(R.color.amber));
                tvSwapNote.setTextSize(10.5f);
                tvSwapNote.setTypeface(null, android.graphics.Typeface.ITALIC);
                midCol.addView(tvSwapNote);
            }
            row.addView(midCol);

            TextView badge = new TextView(this);
            badge.setTextSize(9.5f);
            badge.setTypeface(null, android.graphics.Typeface.BOLD);
            badge.setPadding(dp(7), dp(3), dp(7), dp(3));

            if (hasPassClash) {
                badge.setBackgroundResource(R.drawable.bg_chip_butter);
                badge.setText("Pass Clashed");
                badge.setTextColor(getColor(R.color.charcoal_dark));
            } else if (slot.isInterchanged()) {
                badge.setBackgroundResource(R.drawable.bg_chip_butter);
                badge.setText("🔄 Swapped");
                badge.setTextColor(getColor(R.color.pastel_butter_text));
            } else {
                badge.setBackgroundResource(R.drawable.bg_chip_white);
                badge.setText("Regular");
                badge.setTextColor(getColor(R.color.charcoal_dark));
            }
            row.addView(badge);

            if (isTeacherOrAdmin) {
                row.setBackgroundResource(R.drawable.bg_chip_white);
                row.setPadding(dp(8), dp(8), dp(8), dp(8));
                row.setClickable(true);
                row.setFocusable(true);
                final TimetableSlot currentSlot = slot;
                row.setOnClickListener(v -> showInterchangeLectureDialog(parentDialog, onRefreshCallback, currentSlot));
            }

            container.addView(row);

            if (slot.getSlotNumber() == 3) {
                View sep1 = new View(this);
                LinearLayout.LayoutParams sepLp1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(1));
                sepLp1.setMargins(0, dp(8), 0, dp(8));
                sep1.setLayoutParams(sepLp1);
                sep1.setBackgroundColor(0xFFEAE3FC);
                container.addView(sep1);

                LinearLayout breakRow = new LinearLayout(this);
                breakRow.setOrientation(LinearLayout.HORIZONTAL);
                breakRow.setGravity(android.view.Gravity.CENTER_VERTICAL);
                breakRow.setBackgroundResource(R.drawable.bg_chip_mint);
                breakRow.setPadding(dp(10), dp(7), dp(10), dp(7));

                TextView tvBreakTime = new TextView(this);
                LinearLayout.LayoutParams bTimeLp = new LinearLayout.LayoutParams(dp(74), LinearLayout.LayoutParams.WRAP_CONTENT);
                tvBreakTime.setLayoutParams(bTimeLp);
                tvBreakTime.setText("11:00 AM");
                tvBreakTime.setTextColor(getColor(R.color.pastel_mint_text));
                tvBreakTime.setTextSize(11f);
                tvBreakTime.setTypeface(null, android.graphics.Typeface.BOLD);
                breakRow.addView(tvBreakTime);

                TextView tvBreakLabel = new TextView(this);
                LinearLayout.LayoutParams bLblLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
                tvBreakLabel.setLayoutParams(bLblLp);
                tvBreakLabel.setText("🥪 Recess / Break (11:00 AM - 11:30 AM)");
                tvBreakLabel.setTextColor(getColor(R.color.pastel_mint_text));
                tvBreakLabel.setTextSize(12f);
                tvBreakLabel.setTypeface(null, android.graphics.Typeface.BOLD);
                breakRow.addView(tvBreakLabel);

                TextView tvBreakBadge = new TextView(this);
                tvBreakBadge.setText("Recess");
                tvBreakBadge.setBackgroundResource(R.drawable.bg_chip_white);
                tvBreakBadge.setTextColor(getColor(R.color.pastel_mint_text));
                tvBreakBadge.setTextSize(9.5f);
                tvBreakBadge.setTypeface(null, android.graphics.Typeface.BOLD);
                tvBreakBadge.setPadding(dp(8), dp(3), dp(8), dp(3));
                breakRow.addView(tvBreakBadge);

                container.addView(breakRow);
            }

            if (i < slots.size() - 1) {
                View sep = new View(this);
                LinearLayout.LayoutParams sepLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(1));
                sepLp.setMargins(0, dp(8), 0, dp(8));
                sep.setLayoutParams(sepLp);
                sep.setBackgroundColor(0xFFEAE3FC);
                container.addView(sep);
            }
        }
    }

    private void showInterchangeLectureDialog(@Nullable AlertDialog parentDialog, @Nullable Runnable onRefreshCallback) {
        showInterchangeLectureDialog(parentDialog, onRefreshCallback, null);
    }

    private void showInterchangeLectureDialog(@Nullable AlertDialog parentDialog, @Nullable Runnable onRefreshCallback, @Nullable TimetableSlot preselectedSlot) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_interchange_lecture, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        User currentUser = userManager.getCurrentUser();
        Spinner spinnerStream = dialogView.findViewById(R.id.spinnerInterchangeStream);
        Spinner spinnerDay = dialogView.findViewById(R.id.spinnerInterchangeDay);
        Spinner spinnerSlot1 = dialogView.findViewById(R.id.spinnerInterchangeSlot1);
        Spinner spinnerSlot2 = dialogView.findViewById(R.id.spinnerInterchangeSlot2);
        EditText etReason = dialogView.findViewById(R.id.etInterchangeReason);
        android.widget.CheckBox cbBroadcast = dialogView.findViewById(R.id.cbBroadcastNotice);

        MaterialButton btnCancel = dialogView.findViewById(R.id.btnCancelInterchange);
        MaterialButton btnConfirm = dialogView.findViewById(R.id.btnConfirmInterchange);

        String[] streams = new String[]{
                TimetableManager.STREAM_SCIENCE + " (B.Sc.)",
                TimetableManager.STREAM_COMMERCE + " (B.Com)",
                TimetableManager.STREAM_ARTS + " (B.A.)"
        };
        ArrayAdapter<String> streamAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, streams);
        spinnerStream.setAdapter(streamAdapter);

        if (preselectedSlot != null) {
            String s = preselectedSlot.getStream();
            if (s.contains("Com")) spinnerStream.setSelection(1);
            else if (s.contains("Art")) spinnerStream.setSelection(2);
            else spinnerStream.setSelection(0);
        } else if (currentUser != null) {
            String dept = currentUser.getDepartment();
            if (dept.contains("Commerce")) spinnerStream.setSelection(1);
            else if (dept.contains("Arts")) spinnerStream.setSelection(2);
            else spinnerStream.setSelection(0);
        }

        ArrayAdapter<String> dayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, TimetableManager.DAYS_OF_WEEK);
        spinnerDay.setAdapter(dayAdapter);

        if (preselectedSlot != null && preselectedSlot.getDayOfWeek() != null) {
            for (int d = 0; d < TimetableManager.DAYS_OF_WEEK.length; d++) {
                if (TimetableManager.DAYS_OF_WEEK[d].equalsIgnoreCase(preselectedSlot.getDayOfWeek())) {
                    spinnerDay.setSelection(d);
                    break;
                }
            }
        }

        final List<TimetableSlot> currentDaySlots = new ArrayList<>();

        Runnable updateSlotSpinners = () -> {
            int streamPos = spinnerStream.getSelectedItemPosition();
            String chosenStream = streamPos == 1 ? TimetableManager.STREAM_COMMERCE : (streamPos == 2 ? TimetableManager.STREAM_ARTS : TimetableManager.STREAM_SCIENCE);
            String chosenDay = spinnerDay.getSelectedItem() != null ? spinnerDay.getSelectedItem().toString() : "Monday";

            currentDaySlots.clear();
            currentDaySlots.addAll(timetableManager.getTimetableForStreamAndDay(chosenStream, chosenDay));

            List<String> slotDescriptions = new ArrayList<>();
            for (TimetableSlot slot : currentDaySlots) {
                slotDescriptions.add("Slot " + slot.getSlotNumber() + ": " + slot.getTimeDisplay() + " • " + slot.getSubject() + " (" + slot.getTeacherName() + ")");
            }

            ArrayAdapter<String> slotAdapter1 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, slotDescriptions);
            spinnerSlot1.setAdapter(slotAdapter1);

            ArrayAdapter<String> slotAdapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, slotDescriptions);
            spinnerSlot2.setAdapter(slotAdapter2);

            if (preselectedSlot != null) {
                int targetIndex = Math.max(0, preselectedSlot.getSlotNumber() - 1);
                if (targetIndex < slotDescriptions.size()) {
                    spinnerSlot1.setSelection(targetIndex);
                }
                int defaultSwapIndex = (targetIndex + 1) % slotDescriptions.size();
                spinnerSlot2.setSelection(defaultSwapIndex);
            } else if (slotDescriptions.size() > 1) {
                spinnerSlot1.setSelection(0);
                spinnerSlot2.setSelection(1);
            }
        };

        spinnerStream.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> p, View v, int pos, long id) { updateSlotSpinners.run(); }
            @Override
            public void onNothingSelected(AdapterView<?> p) {}
        });

        spinnerDay.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> p, View v, int pos, long id) { updateSlotSpinners.run(); }
            @Override
            public void onNothingSelected(AdapterView<?> p) {}
        });

        updateSlotSpinners.run();

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnConfirm.setOnClickListener(v -> {
            int pos1 = spinnerSlot1.getSelectedItemPosition();
            int pos2 = spinnerSlot2.getSelectedItemPosition();

            if (pos1 == pos2) {
                Toast.makeText(this, "Please choose two different lecture slots to interchange.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (pos1 < 0 || pos1 >= currentDaySlots.size() || pos2 < 0 || pos2 >= currentDaySlots.size()) {
                Toast.makeText(this, "Invalid slots selected.", Toast.LENGTH_SHORT).show();
                return;
            }

            TimetableSlot slotA = currentDaySlots.get(pos1);
            TimetableSlot slotB = currentDaySlots.get(pos2);
            String note = etReason.getText() != null ? etReason.getText().toString().trim() : "";
            if (note.isEmpty()) {
                note = "Mutual faculty coordination between " + slotA.getTeacherName() + " and " + slotB.getTeacherName() + ".";
            }

            String teacherName = currentUser != null ? currentUser.getFullName() : "Faculty";
            boolean success = timetableManager.interchangeLectures(slotA.getId(), slotB.getId(), note, teacherName);

            if (success) {
                if (cbBroadcast != null && cbBroadcast.isChecked()) {
                    String noticeTitle = "📢 Timetable Update: " + slotA.getDayOfWeek() + " Lectures Swapped";
                    String noticeContent = "Faculty coordination update for " + slotA.getStream() + ": " +
                            slotA.getTimeDisplay() + " (" + slotA.getSubject() + ") has been interchanged with " +
                            slotB.getTimeDisplay() + " (" + slotB.getSubject() + ").\nReason: " + note;

                    Notice n = new Notice(
                            "NOT-SWAP-" + System.currentTimeMillis(),
                            noticeTitle,
                            "Academic Timetable",
                            teacherName + " (Faculty)",
                            slotA.getStream(),
                            noticeContent,
                            "Today",
                            System.currentTimeMillis(),
                            slotA.getTimeDisplay()
                    );
                    noticeManager.addNotice(n);
                }

                Toast.makeText(this, "🎉 Lectures interchanged successfully! Students' schedule updated.", Toast.LENGTH_LONG).show();
                dialog.dismiss();

                if (onRefreshCallback != null) {
                    onRefreshCallback.run();
                }
            } else {
                Toast.makeText(this, "Failed to interchange slots. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

}
