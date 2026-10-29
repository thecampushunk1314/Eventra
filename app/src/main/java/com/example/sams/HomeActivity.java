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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import android.widget.CheckBox;
import android.widget.RadioGroup;
import android.widget.RadioButton;
import android.content.ClipData;
import android.content.ClipboardManager;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class HomeActivity extends AppCompatActivity {

    private TextView tvHeaderAvatar;
    private TextView tvHeaderStudentName;
    private View headerProfileSection;
    private User currentUser;
    private UserManager userManager;
    private SpotPassManager spotPassManager;
    private NoticeManager noticeManager;
    private PermissionRequestManager permissionRequestManager;
    private TimetableManager timetableManager;
    private EventManager eventManager;

    // Principal and Teacher dashboard & records
    private View layoutPrincipalDashboard;
    private View layoutTeacherDashboard;
    private View layoutStudentHero;
    private View layoutStudentQuickActions;
    private View layoutStudentWeek;

    private TextView tabRecordStudents;
    private TextView tabRecordTeachers;
    private View layoutStudentFilterSection;
    private View layoutTeacherFilterSection;

    private Spinner spinnerRecordStream;
    private Spinner spinnerRecordSubject;
    private TextView tvStudentsRecordCount;
    private android.widget.LinearLayout containerStudentsRecords;

    private Spinner spinnerRecordTeacherSubject;
    private TextView tvTeachersRecordCount;
    private android.widget.LinearLayout containerTeachersRecords;

    // Notice card
    private View cardNotice;
    private TextView tvNoticeCardTitle;
    private TextView tvNoticeCardMeta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Allow phone navigation buttons (Back, Home, Recents) to always stay visible
        WindowCompat.setDecorFitsSystemWindows(getWindow(), true);
        WindowInsetsControllerCompat insetsController = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (insetsController != null) {
            insetsController.show(WindowInsetsCompat.Type.systemBars());
        }

        setContentView(R.layout.activity_home);

        userManager = UserManager.getInstance(this);
        spotPassManager = SpotPassManager.getInstance(this);
        noticeManager = NoticeManager.getInstance(this);
        permissionRequestManager = PermissionRequestManager.getInstance(this);
        timetableManager = TimetableManager.getInstance(this);
        eventManager = EventManager.getInstance(this);
        currentUser = userManager.getCurrentUser();

        tvHeaderAvatar = findViewById(R.id.tvHeaderAvatar);
        tvHeaderStudentName = findViewById(R.id.tvHeaderStudentName);
        headerProfileSection = findViewById(R.id.headerProfileSection);

        layoutPrincipalDashboard = findViewById(R.id.layoutPrincipalDashboard);
        layoutTeacherDashboard = findViewById(R.id.layoutTeacherDashboard);
        layoutStudentHero = findViewById(R.id.layoutStudentHero);
        layoutStudentQuickActions = findViewById(R.id.layoutStudentQuickActions);
        layoutStudentWeek = findViewById(R.id.layoutStudentWeek);

        // Header Actions
        View btnNotifications = findViewById(R.id.btnNotifications);
        if (btnNotifications != null) {
            btnNotifications.setOnClickListener(v -> showNotificationsDialog());
        }
        updateNotificationBadge();

        // Profile / Account option in Header
        View.OnClickListener openProfileListener = v -> {
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
        };
        if (headerProfileSection != null) {
            headerProfileSection.setOnClickListener(openProfileListener);
        }

        // Student Quick actions
        View btnNewSpotPass = findViewById(R.id.btnNewSpotPass);
        if (btnNewSpotPass != null) {
            btnNewSpotPass.setOnClickListener(v -> showNewSpotPassDialog());
        }

        View btnPermission = findViewById(R.id.btnPermission);
        if (btnPermission != null) {
            btnPermission.setOnClickListener(v -> showNewPermissionDialog());
        }

        // Principal Dashboard buttons & Records setup
        initPrincipalDashboard();

        // Teacher Dashboard buttons setup
        initTeacherDashboard();

        // Section links and cards
        View linkOpenCalendar = findViewById(R.id.linkOpenCalendar);
        if (linkOpenCalendar != null) {
            linkOpenCalendar.setOnClickListener(v -> showCalendarDialog(null));
        }

        // Enable interactive week day selection
        setupWeekDayClicks();

        // Notice card setup
        cardNotice = findViewById(R.id.cardNotice);
        tvNoticeCardTitle = findViewById(R.id.tvNoticeCardTitle);
        tvNoticeCardMeta = findViewById(R.id.tvNoticeCardMeta);
        if (cardNotice != null) {
            cardNotice.setOnClickListener(v -> showNoticeDetailsDialog(noticeManager.getLatestNotice()));
        }

        // Bottom navigation
        findViewById(R.id.navHome).setOnClickListener(v -> {
            // Already on Home
        });


        View navRequests = findViewById(R.id.navRequests);
        if (navRequests != null) {
            navRequests.setOnClickListener(v -> startActivity(new Intent(this, RequestsActivity.class)));
        }

        // Account / Profile option in Bottom Bar
        View navProfile = findViewById(R.id.navProfile);
        if (navProfile != null) {
            navProfile.setOnClickListener(openProfileListener);
        }
    }

    private void updateNotificationBadge() {
        View badgeDot = findViewById(R.id.badgeNotificationDot);
        if (badgeDot == null) return;
        User current = userManager.getCurrentUser();
        String studentId = current != null ? current.getId() : "";
        List<SpotPass> approvedPasses = spotPassManager.getApprovedPassesForStudent(studentId);
        List<PermissionRequest> approvedPerms = permissionRequestManager.getApprovedRequestsForStudent(studentId);
        int total = approvedPasses.size() + approvedPerms.size();
        badgeDot.setVisibility(total > 0 ? View.VISIBLE : View.GONE);
    }

    private void initPrincipalDashboard() {
        View btnAddFaculty = findViewById(R.id.btnPrincipalAddFaculty);
        if (btnAddFaculty != null) {
            btnAddFaculty.setOnClickListener(v -> showAddTeacherDialog());
        }

        View btnEnrollStudent = findViewById(R.id.btnPrincipalEnrollStudent);
        if (btnEnrollStudent != null) {
            btnEnrollStudent.setOnClickListener(v -> showAddStudentDialog());
        }

        View btnUploadNotice = findViewById(R.id.btnPrincipalUploadNotice);
        if (btnUploadNotice != null) {
            btnUploadNotice.setOnClickListener(v -> showAllNoticesDialog());
        }

        View btnPrincipalRequests = findViewById(R.id.btnPrincipalRequests);
        if (btnPrincipalRequests != null) {
            btnPrincipalRequests.setOnClickListener(v -> startActivity(new Intent(this, RequestsActivity.class)));
        }

        View btnPrincipalMonthlyRegister = findViewById(R.id.btnPrincipalMonthlyRegister);
        if (btnPrincipalMonthlyRegister != null) {
            btnPrincipalMonthlyRegister.setOnClickListener(v -> showSelectStudentForMonthlyAttendanceDialog());
        }

        View btnPrincipalEventsAndPractice = findViewById(R.id.btnPrincipalEventsAndPractice);
        if (btnPrincipalEventsAndPractice != null) {
            btnPrincipalEventsAndPractice.setOnClickListener(v -> showEventsAndPracticeDialog());
        }

        View btnPrincipalAppointIncharge = findViewById(R.id.btnPrincipalAppointIncharge);
        if (btnPrincipalAppointIncharge != null) {
            btnPrincipalAppointIncharge.setOnClickListener(v -> showAppointEventInchargeDialog());
        }

        tabRecordStudents = findViewById(R.id.tabRecordStudents);
        tabRecordTeachers = findViewById(R.id.tabRecordTeachers);
        layoutStudentFilterSection = findViewById(R.id.layoutStudentFilterSection);
        layoutTeacherFilterSection = findViewById(R.id.layoutTeacherFilterSection);

        spinnerRecordStream = findViewById(R.id.spinnerRecordStream);
        spinnerRecordSubject = findViewById(R.id.spinnerRecordSubject);
        tvStudentsRecordCount = findViewById(R.id.tvStudentsRecordCount);
        containerStudentsRecords = findViewById(R.id.containerStudentsRecords);

        spinnerRecordTeacherSubject = findViewById(R.id.spinnerRecordTeacherSubject);
        tvTeachersRecordCount = findViewById(R.id.tvTeachersRecordCount);
        containerTeachersRecords = findViewById(R.id.containerTeachersRecords);

        if (tabRecordStudents != null && tabRecordTeachers != null) {
            tabRecordStudents.setOnClickListener(v -> {
                tabRecordStudents.setBackgroundResource(R.drawable.bg_chip_selected_charcoal);
                tabRecordStudents.setTextColor(getColor(R.color.white));
                tabRecordTeachers.setBackgroundResource(R.drawable.bg_chip_unselected_white);
                tabRecordTeachers.setTextColor(getColor(R.color.text_secondary));

                if (layoutStudentFilterSection != null) layoutStudentFilterSection.setVisibility(View.VISIBLE);
                if (layoutTeacherFilterSection != null) layoutTeacherFilterSection.setVisibility(View.GONE);
            });

            tabRecordTeachers.setOnClickListener(v -> {
                tabRecordTeachers.setBackgroundResource(R.drawable.bg_chip_selected_charcoal);
                tabRecordTeachers.setTextColor(getColor(R.color.white));
                tabRecordStudents.setBackgroundResource(R.drawable.bg_chip_unselected_white);
                tabRecordStudents.setTextColor(getColor(R.color.text_secondary));

                if (layoutTeacherFilterSection != null) layoutTeacherFilterSection.setVisibility(View.VISIBLE);
                if (layoutStudentFilterSection != null) layoutStudentFilterSection.setVisibility(View.GONE);
            });
        }

        // Setup Spinners
        if (spinnerRecordStream != null) {
            String[] streams;
            if (currentUser != null && !currentUser.isPrincipal() && (currentUser.isTeacherOrDirector() || currentUser.isHOD())) {
                String myStream = currentUser.getStream();
                if ("Commerce".equalsIgnoreCase(myStream)) {
                    streams = new String[]{"Commerce Stream (B.Com)"};
                } else if ("Arts".equalsIgnoreCase(myStream)) {
                    streams = new String[]{"Arts Stream (B.A.)"};
                } else {
                    streams = new String[]{"Science Stream (B.Sc.)"};
                }
            } else {
                streams = new String[]{"All Streams", "Science (B.Sc.)", "Commerce (B.Com)", "Arts (B.A.)"};
            }
            ArrayAdapter<String> streamAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, streams);
            spinnerRecordStream.setAdapter(streamAdapter);
            spinnerRecordStream.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> p, View v, int pos, long id) { filterAndRenderStudents(); }
                @Override
                public void onNothingSelected(AdapterView<?> p) {}
            });
        }

        if (spinnerRecordSubject != null) {
            String[] subjects = new String[]{"All Subjects", "Physics", "Chemistry", "Mathematics", "Computer Science", "Accounting", "Corporate Law", "Financial Markets", "History", "English Literature", "Political Science"};
            ArrayAdapter<String> subjectAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, subjects);
            spinnerRecordSubject.setAdapter(subjectAdapter);
            spinnerRecordSubject.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> p, View v, int pos, long id) { filterAndRenderStudents(); }
                @Override
                public void onNothingSelected(AdapterView<?> p) {}
            });
        }

        if (spinnerRecordTeacherSubject != null) {
            String[] teacherSubs = new String[]{"All Teaching Subjects", "Physics", "Chemistry", "Mathematics", "Accounting", "Corporate Accounts", "Business Management", "History", "English Literature", "Political Science"};
            ArrayAdapter<String> teacherSubAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, teacherSubs);
            spinnerRecordTeacherSubject.setAdapter(teacherSubAdapter);
            spinnerRecordTeacherSubject.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> p, View v, int pos, long id) { filterAndRenderTeachers(); }
                @Override
                public void onNothingSelected(AdapterView<?> p) {}
            });
        }
    }

    private void initTeacherDashboard() {
        View btnTeacherAddStudent = findViewById(R.id.btnTeacherAddStudent);
        if (btnTeacherAddStudent != null) {
            btnTeacherAddStudent.setOnClickListener(v -> showAddStudentDialog());
        }

        View btnTeacherReviewRequests = findViewById(R.id.btnTeacherReviewRequests);
        if (btnTeacherReviewRequests != null) {
            btnTeacherReviewRequests.setOnClickListener(v -> startActivity(new Intent(this, RequestsActivity.class)));
        }

        View btnTeacherNoticeAction = findViewById(R.id.btnTeacherNoticeAction);
        if (btnTeacherNoticeAction != null) {
            btnTeacherNoticeAction.setOnClickListener(v -> showAllNoticesDialog());
        }

        View btnTeacherInterchangeLectures = findViewById(R.id.btnTeacherInterchangeLectures);
        if (btnTeacherInterchangeLectures != null) {
            btnTeacherInterchangeLectures.setOnClickListener(v -> showInterchangeLectureDialog(null, null));
        }

        View btnTeacherMonthlyRegister = findViewById(R.id.btnTeacherMonthlyRegister);
        if (btnTeacherMonthlyRegister != null) {
            btnTeacherMonthlyRegister.setOnClickListener(v -> showSelectStudentForMonthlyAttendanceDialog());
        }

        View btnTeacherEventsAndPractice = findViewById(R.id.btnTeacherEventsAndPractice);
        if (btnTeacherEventsAndPractice != null) {
            btnTeacherEventsAndPractice.setOnClickListener(v -> showEventsAndPracticeDialog());
        }
    }

    private void filterAndRenderStudents() {
        if (containerStudentsRecords == null || spinnerRecordStream == null || spinnerRecordSubject == null) return;
        String selStream = spinnerRecordStream.getSelectedItem() != null ? spinnerRecordStream.getSelectedItem().toString() : "All Streams";
        String selSubject = spinnerRecordSubject.getSelectedItem() != null ? spinnerRecordSubject.getSelectedItem().toString() : "All Subjects";

        List<User> filtered = userManager.filterStudents(currentUser, selStream, selSubject);
        
        // Calculate cohort attendance statistics for counting total attendance feature
        double avg = userManager.calculateAverageAttendance(filtered);
        int below70 = userManager.getStudentsBelowAttendanceThreshold(filtered, 70.0);
        int safe = userManager.getStudentsAboveAttendanceThreshold(filtered, 70.0);

        TextView tvRecordAvgAttendance = findViewById(R.id.tvRecordAvgAttendance);
        TextView tvRecordBelow70Count = findViewById(R.id.tvRecordBelow70Count);
        TextView tvRecordSafeCount = findViewById(R.id.tvRecordSafeCount);

        if (tvRecordAvgAttendance != null) {
            tvRecordAvgAttendance.setText(String.format(java.util.Locale.US, "%.1f%%", avg));
        }
        if (tvRecordBelow70Count != null) {
            tvRecordBelow70Count.setText("⚠️ " + below70 + " Below 70%");
        }
        if (tvRecordSafeCount != null) {
            tvRecordSafeCount.setText("✅ " + safe + " Safe (≥70%)");
        }

        if (tvStudentsRecordCount != null) {
            tvStudentsRecordCount.setText("Enrolled Students (" + filtered.size() + ") • Average Attendance: " + String.format(java.util.Locale.US, "%.1f%%", avg));
        }
        containerStudentsRecords.removeAllViews();

        if (filtered.isEmpty()) {
            TextView emptyTv = new TextView(this);
            emptyTv.setText("No students match the selected stream and subject.");
            emptyTv.setTextColor(getColor(R.color.text_muted));
            emptyTv.setTextSize(12f);
            emptyTv.setPadding(16, 20, 16, 20);
            containerStudentsRecords.addView(emptyTv);
            return;
        }

        float density = getResources().getDisplayMetrics().density;
        int cardPadH = (int) (16 * density);
        int cardPadV = (int) (12 * density);
        int cardMarginTop = (int) (10 * density);
        int badgePadH = (int) (8 * density);
        int badgePadV = (int) (3 * density);

        for (int i = 0; i < filtered.size(); i++) {
            User s = filtered.get(i);
            android.widget.LinearLayout card = new android.widget.LinearLayout(this);
            card.setOrientation(android.widget.LinearLayout.VERTICAL);
            card.setBackgroundResource(R.drawable.bg_input_box);
            card.setPadding(cardPadH, cardPadV, cardPadH, cardPadV);

            android.widget.LinearLayout.LayoutParams lp = new android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.MATCH_PARENT, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
            if (i > 0) lp.topMargin = cardMarginTop;
            card.setLayoutParams(lp);

            // Row 1: Roll No & Stream badge
            android.widget.LinearLayout r1 = new android.widget.LinearLayout(this);
            r1.setOrientation(android.widget.LinearLayout.HORIZONTAL);
            r1.setGravity(android.view.Gravity.CENTER_VERTICAL);

            TextView tvRoll = new TextView(this);
            android.widget.LinearLayout.LayoutParams rollLp = new android.widget.LinearLayout.LayoutParams(0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
            tvRoll.setLayoutParams(rollLp);
            tvRoll.setText("🆔 " + s.getId() + " • " + s.getFullName());
            tvRoll.setTextColor(getColor(R.color.charcoal_dark));
            tvRoll.setTextSize(13.5f);
            tvRoll.setTypeface(null, android.graphics.Typeface.BOLD);
            r1.addView(tvRoll);

            TextView tvBadge = new TextView(this);
            tvBadge.setBackgroundResource(R.drawable.bg_chip_mint);
            tvBadge.setText(s.getDepartment().isEmpty() ? "Science Stream" : s.getDepartment());
            tvBadge.setTextColor(getColor(R.color.pastel_mint_text));
            tvBadge.setTextSize(10.5f);
            tvBadge.setTypeface(null, android.graphics.Typeface.BOLD);
            tvBadge.setPadding(badgePadH, badgePadV, badgePadH, badgePadV);
            r1.addView(tvBadge);
            card.addView(r1);

            // Row 2: Class & Subjects
            TextView tvMeta = new TextView(this);
            tvMeta.setText("Class: " + (s.getYearOrClass().isEmpty() ? "11th Standard" : s.getYearOrClass()) + " • Subjects: " + s.getSubjects());
            tvMeta.setTextColor(getColor(R.color.text_secondary));
            tvMeta.setTextSize(11.5f);
            tvMeta.setPadding(0, 6, 0, 0);
            card.addView(tvMeta);

            // Row 3: Institutional Email
            TextView tvEmail = new TextView(this);
            tvEmail.setText("📧 " + s.getEmail());
            tvEmail.setTextColor(getColor(R.color.pastel_lavender_text));
            tvEmail.setTextSize(11f);
            tvEmail.setPadding(0, 4, 0, 0);
            card.addView(tvEmail);

            // Row 4: Academic Attendance Metric Badge & Lecture Counts
            android.widget.LinearLayout r4 = new android.widget.LinearLayout(this);
            r4.setOrientation(android.widget.LinearLayout.HORIZONTAL);
            r4.setGravity(android.view.Gravity.CENTER_VERTICAL);
            r4.setPadding(0, 8, 0, 0);

            TextView tvAttBadge = new TextView(this);
            tvAttBadge.setTextSize(10.5f);
            tvAttBadge.setTypeface(null, android.graphics.Typeface.BOLD);
            tvAttBadge.setPadding(12, 4, 12, 4);

            double attPct = s.getAttendancePercentage();
            if (attPct < 70.0) {
                tvAttBadge.setBackgroundResource(R.drawable.bg_chip_red);
                tvAttBadge.setText("⚠️ " + String.format(java.util.Locale.US, "%.1f%% Low (Defaulter)", attPct));
                tvAttBadge.setTextColor(getColor(R.color.chip_red_text));
            } else if (attPct < 75.0) {
                tvAttBadge.setBackgroundResource(R.drawable.bg_chip_yellow);
                tvAttBadge.setText("⚡ " + String.format(java.util.Locale.US, "%.1f%% Borderline", attPct));
                tvAttBadge.setTextColor(getColor(R.color.chip_yellow_text));
            } else {
                tvAttBadge.setBackgroundResource(R.drawable.bg_chip_green);
                tvAttBadge.setText("✅ " + String.format(java.util.Locale.US, "%.1f%% Safe", attPct));
                tvAttBadge.setTextColor(getColor(R.color.chip_green_text));
            }
            r4.addView(tvAttBadge);

            TextView tvAttDetail = new TextView(this);
            tvAttDetail.setText("  •  " + s.getAttendedClasses() + "/" + s.getTotalClasses() + " lectures (" + s.getDutyLeaveApprovedCount() + " duty leaves)");
            tvAttDetail.setTextColor(getColor(R.color.text_muted));
            tvAttDetail.setTextSize(11f);
            r4.addView(tvAttDetail);

            card.addView(r4);

            // Row 5: Action button to Update Monthly Attendance & Merge Duty Passes
            User currentUser = userManager.getCurrentUser();
            boolean isStaff = currentUser != null && (currentUser.isTeacherOrDirector() || currentUser.isPrincipal());
            if (isStaff) {
                com.google.android.material.button.MaterialButton btnUpdateAtt = new com.google.android.material.button.MaterialButton(this, null, com.google.android.material.R.attr.materialButtonOutlinedStyle);
                btnUpdateAtt.setText("📊 Update Monthly Attendance & Merge Passes");
                btnUpdateAtt.setTextSize(11f);
                btnUpdateAtt.setTypeface(null, android.graphics.Typeface.BOLD);
                btnUpdateAtt.setTextColor(getColor(R.color.charcoal_dark));
                btnUpdateAtt.setStrokeColorResource(R.color.outline);
                btnUpdateAtt.setCornerRadius((int) (12 * density));
                btnUpdateAtt.setInsetTop(0);
                btnUpdateAtt.setInsetBottom(0);
                android.widget.LinearLayout.LayoutParams btnLp = new android.widget.LinearLayout.LayoutParams(
                        android.widget.LinearLayout.LayoutParams.MATCH_PARENT, (int) (40 * density));
                btnLp.topMargin = (int) (8 * density);
                btnUpdateAtt.setLayoutParams(btnLp);
                btnUpdateAtt.setOnClickListener(v -> showMonthlyAttendanceDialog(s));
                card.addView(btnUpdateAtt);
            }

            containerStudentsRecords.addView(card);
        }
    }

    private void filterAndRenderTeachers() {
        if (containerTeachersRecords == null || spinnerRecordTeacherSubject == null) return;
        String selSubject = spinnerRecordTeacherSubject.getSelectedItem() != null ? spinnerRecordTeacherSubject.getSelectedItem().toString() : "All Teaching Subjects";

        List<User> filtered = userManager.filterTeachers(selSubject);
        if (tvTeachersRecordCount != null) {
            tvTeachersRecordCount.setText("Faculty & Staff (" + filtered.size() + ")");
        }
        containerTeachersRecords.removeAllViews();

        if (filtered.isEmpty()) {
            TextView emptyTv = new TextView(this);
            emptyTv.setText("No faculty members found for this subject.");
            emptyTv.setTextColor(getColor(R.color.text_muted));
            emptyTv.setTextSize(12f);
            emptyTv.setPadding(16, 20, 16, 20);
            containerTeachersRecords.addView(emptyTv);
            return;
        }

        float density = getResources().getDisplayMetrics().density;
        int cardPadH = (int) (16 * density);
        int cardPadV = (int) (12 * density);
        int cardMarginTop = (int) (10 * density);
        int badgePadH = (int) (8 * density);
        int badgePadV = (int) (3 * density);

        for (int i = 0; i < filtered.size(); i++) {
            User t = filtered.get(i);
            android.widget.LinearLayout card = new android.widget.LinearLayout(this);
            card.setOrientation(android.widget.LinearLayout.VERTICAL);
            card.setBackgroundResource(R.drawable.bg_input_box);
            card.setPadding(cardPadH, cardPadV, cardPadH, cardPadV);

            android.widget.LinearLayout.LayoutParams lp = new android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.MATCH_PARENT, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
            if (i > 0) lp.topMargin = cardMarginTop;
            card.setLayoutParams(lp);

            // Row 1: Name and ID badge
            android.widget.LinearLayout r1 = new android.widget.LinearLayout(this);
            r1.setOrientation(android.widget.LinearLayout.HORIZONTAL);
            r1.setGravity(android.view.Gravity.CENTER_VERTICAL);

            TextView tvName = new TextView(this);
            android.widget.LinearLayout.LayoutParams nameLp = new android.widget.LinearLayout.LayoutParams(0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
            tvName.setLayoutParams(nameLp);
            tvName.setText("👩‍🏫 " + t.getFullName());
            tvName.setTextColor(getColor(R.color.charcoal_dark));
            tvName.setTextSize(13.5f);
            tvName.setTypeface(null, android.graphics.Typeface.BOLD);
            r1.addView(tvName);

            TextView tvId = new TextView(this);
            tvId.setBackgroundResource(R.drawable.bg_chip_lavender);
            tvId.setText(t.getId());
            tvId.setTextColor(getColor(R.color.pastel_lavender_text));
            tvId.setTextSize(10.5f);
            tvId.setTypeface(null, android.graphics.Typeface.BOLD);
            tvId.setPadding(badgePadH, badgePadV, badgePadH, badgePadV);
            r1.addView(tvId);
            card.addView(r1);

            // Row 2: Designation & Dept
            TextView tvDesig = new TextView(this);
            tvDesig.setText(t.getAdditionalInfo() + " (" + t.getDepartment() + ")");
            tvDesig.setTextColor(getColor(R.color.text_secondary));
            tvDesig.setTextSize(11.5f);
            tvDesig.setPadding(0, 6, 0, 0);
            card.addView(tvDesig);

            // Row 3: Teaching Subjects
            TextView tvSubs = new TextView(this);
            tvSubs.setText("📚 Subjects: " + t.getSubjects());
            tvSubs.setTextColor(getColor(R.color.charcoal_dark));
            tvSubs.setTextSize(11.5f);
            tvSubs.setTypeface(null, android.graphics.Typeface.BOLD);
            tvSubs.setPadding(0, 4, 0, 0);
            card.addView(tvSubs);

            // Row 4: Email & Phone
            TextView tvContact = new TextView(this);
            tvContact.setText("📧 " + t.getEmail() + " • 📞 " + (t.getPhoneNumber().isEmpty() ? "9823000000" : UserManager.normalizePhone(t.getPhoneNumber())));
            tvContact.setTextColor(getColor(R.color.text_muted));
            tvContact.setTextSize(11f);
            tvContact.setPadding(0, 4, 0, 0);
            card.addView(tvContact);

            containerTeachersRecords.addView(card);
        }
    }

    /**
     * Dialog for Teachers and Principal to select a student to update their monthly attendance
     * and merge availed spot passes.
     */
    private void showSelectStudentForMonthlyAttendanceDialog() {
        List<User> students = userManager.getStudentsForUser(currentUser);
        if (students.isEmpty()) {
            Toast.makeText(this, "No enrolled students found in records.", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] items = new String[students.size()];
        for (int i = 0; i < students.size(); i++) {
            User s = students.get(i);
            items[i] = "🆔 " + s.getId() + " • " + s.getFullName() + " (" + String.format(java.util.Locale.US, "%.1f%%", s.getAttendancePercentage()) + ")";
        }

        new AlertDialog.Builder(this)
                .setTitle("📊 Select Student for Monthly Attendance")
                .setItems(items, (d, which) -> {
                    User selected = students.get(which);
                    showMonthlyAttendanceDialog(selected);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Dialog for Teachers and Principal to update a student's monthly attendance
     * and automatically consider & merge all approved Spot Passes and Event Duty Leaves.
     */
    private void showMonthlyAttendanceDialog(User student) {
        if (student == null) return;

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_monthly_attendance, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        TextView tvAvatar = dialogView.findViewById(R.id.tvAttStudentAvatar);
        TextView tvName = dialogView.findViewById(R.id.tvAttStudentName);
        TextView tvMeta = dialogView.findViewById(R.id.tvAttStudentMeta);
        TextView tvCurrentBadge = dialogView.findViewById(R.id.tvAttCurrentPctBadge);

        Spinner spMonth = dialogView.findViewById(R.id.spinnerAttMonthCycle);
        EditText etTotal = dialogView.findViewById(R.id.etAttTotalConducted);
        EditText etAttended = dialogView.findViewById(R.id.etAttClassroomAttended);

        TextView tvDutyBadge = dialogView.findViewById(R.id.tvAttDutyCountBadge);
        android.widget.LinearLayout containerPasses = dialogView.findViewById(R.id.containerAvailedPassesList);

        TextView tvMergedFormula = dialogView.findViewById(R.id.tvMergedAttFormula);
        TextView tvMergedPct = dialogView.findViewById(R.id.tvMergedAttPercentage);
        TextView tvMergedStatus = dialogView.findViewById(R.id.tvMergedAttStatusText);

        MaterialButton btnCancel = dialogView.findViewById(R.id.btnCancelMonthlyAtt);
        MaterialButton btnMerge = dialogView.findViewById(R.id.btnMergeAttendance);

        // Populate student profile
        if (tvAvatar != null) tvAvatar.setText(student.getInitials());
        if (tvName != null) tvName.setText(student.getFullName());
        if (tvMeta != null) {
            tvMeta.setText(student.getId() + " • " + (student.getYearOrClass().isEmpty() ? "FY B.Sc." : student.getYearOrClass())
                    + " (" + (student.getDepartment().isEmpty() ? "Science" : student.getDepartment()) + ")");
        }
        if (tvCurrentBadge != null) {
            tvCurrentBadge.setText("Current: " + String.format(java.util.Locale.US, "%.1f%%", student.getAttendancePercentage()));
        }

        // Setup Month Cycle Spinner
        String[] months = new String[]{
                "October 2026 (Current Academic Month)",
                "September 2026 (Previous Cycle)",
                "November 2026 (Upcoming Consolidation)",
                "Cumulative Term 2025-2026"
        };
        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, months);
        if (spMonth != null) spMonth.setAdapter(monthAdapter);

        // Pre-fill lecture values
        int defaultTotal = student.getTotalClasses() > 0 ? student.getTotalClasses() : 60;
        int defaultAttended = student.getAttendedClasses() > 0 ? student.getAttendedClasses() : (int) Math.round(defaultTotal * 0.70);
        if (etTotal != null) etTotal.setText(String.valueOf(defaultTotal));
        if (etAttended != null) etAttended.setText(String.valueOf(defaultAttended));

        // Query Approved Spot Passes & Permission Requests for this student
        List<SpotPass> approvedPasses = spotPassManager.getApprovedPassesForStudent(student.getId());
        List<PermissionRequest> approvedPerms = permissionRequestManager.getApprovedRequestsForStudent(student.getId());

        int totalDutyLeaves = approvedPasses.size() + approvedPerms.size();
        if (totalDutyLeaves == 0 && student.getDutyLeaveApprovedCount() > 0) {
            totalDutyLeaves = student.getDutyLeaveApprovedCount();
        }

        if (tvDutyBadge != null) {
            tvDutyBadge.setText("+" + totalDutyLeaves + " Duty Credits");
        }

        if (containerPasses != null) {
            containerPasses.removeAllViews();
            float density = getResources().getDisplayMetrics().density;

            if (approvedPasses.isEmpty() && approvedPerms.isEmpty() && student.getDutyLeaveApprovedCount() == 0) {
                TextView tvEmpty = new TextView(this);
                tvEmpty.setText("No spot passes or permission requests availed yet for this student.");
                tvEmpty.setTextColor(getColor(R.color.text_muted));
                tvEmpty.setTextSize(11f);
                tvEmpty.setPadding(0, (int)(4 * density), 0, (int)(4 * density));
                containerPasses.addView(tvEmpty);
            } else {
                for (SpotPass p : approvedPasses) {
                    android.widget.LinearLayout item = new android.widget.LinearLayout(this);
                    item.setOrientation(android.widget.LinearLayout.HORIZONTAL);
                    item.setGravity(android.view.Gravity.CENTER_VERTICAL);
                    item.setPadding(0, (int)(4 * density), 0, (int)(4 * density));

                    TextView tvPassInfo = new TextView(this);
                    android.widget.LinearLayout.LayoutParams lp = new android.widget.LinearLayout.LayoutParams(0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
                    tvPassInfo.setLayoutParams(lp);
                    tvPassInfo.setText("✅ " + p.getId() + ": " + p.getActivityTitle() + " (" + p.getDate() + " • " + p.getTimeSlot() + ")");
                    tvPassInfo.setTextColor(getColor(R.color.charcoal_dark));
                    tvPassInfo.setTextSize(11.5f);
                    tvPassInfo.setTypeface(null, android.graphics.Typeface.BOLD);
                    item.addView(tvPassInfo);

                    TextView tvTag = new TextView(this);
                    tvTag.setText("+1 Credit");
                    tvTag.setTextColor(getColor(R.color.pastel_mint_text));
                    tvTag.setTextSize(10.5f);
                    tvTag.setTypeface(null, android.graphics.Typeface.BOLD);
                    item.addView(tvTag);

                    containerPasses.addView(item);
                }

                for (PermissionRequest r : approvedPerms) {
                    android.widget.LinearLayout item = new android.widget.LinearLayout(this);
                    item.setOrientation(android.widget.LinearLayout.HORIZONTAL);
                    item.setGravity(android.view.Gravity.CENTER_VERTICAL);
                    item.setPadding(0, (int)(4 * density), 0, (int)(4 * density));

                    TextView tvPermInfo = new TextView(this);
                    android.widget.LinearLayout.LayoutParams lp = new android.widget.LinearLayout.LayoutParams(0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
                    tvPermInfo.setLayoutParams(lp);
                    tvPermInfo.setText("✅ " + r.getId() + ": " + r.getEventTitle() + " (" + r.getEventDates() + ")");
                    tvPermInfo.setTextColor(getColor(R.color.charcoal_dark));
                    tvPermInfo.setTextSize(11.5f);
                    tvPermInfo.setTypeface(null, android.graphics.Typeface.BOLD);
                    item.addView(tvPermInfo);

                    TextView tvTag = new TextView(this);
                    tvTag.setText("+1 Credit");
                    tvTag.setTextColor(getColor(R.color.pastel_mint_text));
                    tvTag.setTextSize(10.5f);
                    tvTag.setTypeface(null, android.graphics.Typeface.BOLD);
                    item.addView(tvTag);

                    containerPasses.addView(item);
                }

                List<PracticeSession> allPracticeSessions = eventManager.getAllPracticeSessions();
                for (PracticeSession ps : allPracticeSessions) {
                    if (ps.isStudentPresent(student.getId())) {
                        android.widget.LinearLayout item = new android.widget.LinearLayout(this);
                        item.setOrientation(android.widget.LinearLayout.HORIZONTAL);
                        item.setGravity(android.view.Gravity.CENTER_VERTICAL);
                        item.setPadding(0, (int)(4 * density), 0, (int)(4 * density));

                        TextView tvSessionInfo = new TextView(this);
                        android.widget.LinearLayout.LayoutParams lp = new android.widget.LinearLayout.LayoutParams(0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
                        tvSessionInfo.setLayoutParams(lp);
                        tvSessionInfo.setText("⚽ Ground Roll-Call: " + ps.getEventName() + " (" + ps.getPracticeDate() + " • " + ps.getTimeSlot() + ")\n↪️ Verified by " + ps.getCoachName() + " (Redirected to Missed Subject Lectures)");
                        tvSessionInfo.setTextColor(getColor(R.color.charcoal_dark));
                        tvSessionInfo.setTextSize(11f);
                        tvSessionInfo.setTypeface(null, android.graphics.Typeface.BOLD);
                        item.addView(tvSessionInfo);

                        TextView tvTag = new TextView(this);
                        tvTag.setText("+Duty Leave");
                        tvTag.setTextColor(getColor(R.color.pastel_mint_text));
                        tvTag.setTextSize(10.5f);
                        tvTag.setTypeface(null, android.graphics.Typeface.BOLD);
                        item.addView(tvTag);

                        containerPasses.addView(item);
                    }
                }

                if (approvedPasses.isEmpty() && approvedPerms.isEmpty() && student.getDutyLeaveApprovedCount() > 0) {
                    TextView tvPreseeded = new TextView(this);
                    tvPreseeded.setText("✅ Verified Co-Curricular Event Participation (Inter-Collegiate Sports & Youth Fest)");
                    tvPreseeded.setTextColor(getColor(R.color.charcoal_dark));
                    tvPreseeded.setTextSize(11.5f);
                    tvPreseeded.setTypeface(null, android.graphics.Typeface.BOLD);
                    containerPasses.addView(tvPreseeded);
                }
            }
        }

        final int finalDutyLeaves = totalDutyLeaves;

        Runnable updateFormula = () -> {
            int tot = 60;
            int att = 44;
            try {
                if (etTotal != null) tot = Integer.parseInt(etTotal.getText().toString().trim());
                if (etAttended != null) att = Integer.parseInt(etAttended.getText().toString().trim());
            } catch (Exception ignored) {}
            if (tot <= 0) tot = 60;
            int mergedAttended = Math.min(tot, att + finalDutyLeaves);
            double pct = ((double) mergedAttended / tot) * 100.0;

            if (tvMergedFormula != null) {
                tvMergedFormula.setText(att + " (Class) + " + finalDutyLeaves + " (Duty) = " + mergedAttended + " / " + tot);
            }
            if (tvMergedPct != null) {
                tvMergedPct.setText(String.format(java.util.Locale.US, "%.1f%%", pct));
            }
            if (tvMergedStatus != null) {
                if (pct >= 75.0) {
                    tvMergedStatus.setText("✅ Safe (≥75% Attendance Requirement Met)");
                    tvMergedStatus.setTextColor(getColor(R.color.pastel_mint_text));
                } else if (pct >= 70.0) {
                    tvMergedStatus.setText("⚡ Borderline (70% - 74.9% Attendance)");
                    tvMergedStatus.setTextColor(getColor(R.color.chip_yellow_text));
                } else {
                    tvMergedStatus.setText("⚠️ Defaulter (<70% - Warning Notice Triggered)");
                    tvMergedStatus.setTextColor(getColor(R.color.chip_red_text));
                }
            }
        };

        android.text.TextWatcher watcher = new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { updateFormula.run(); }
            @Override public void afterTextChanged(android.text.Editable s) {}
        };

        if (etTotal != null) etTotal.addTextChangedListener(watcher);
        if (etAttended != null) etAttended.addTextChangedListener(watcher);
        updateFormula.run();

        if (btnCancel != null) btnCancel.setOnClickListener(v -> dialog.dismiss());

        if (btnMerge != null) {
            btnMerge.setOnClickListener(v -> {
                int tot = 60;
                int att = 44;
                try {
                    if (etTotal != null) tot = Integer.parseInt(etTotal.getText().toString().trim());
                    if (etAttended != null) att = Integer.parseInt(etAttended.getText().toString().trim());
                } catch (Exception ex) {
                    Toast.makeText(this, "Please enter valid numeric attendance counts", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (tot <= 0) {
                    if (etTotal != null) etTotal.setError("Total lectures must be greater than 0");
                    return;
                }
                if (att > tot) {
                    if (etAttended != null) etAttended.setError("Attended lectures cannot exceed total conducted");
                    return;
                }

                int merged = Math.min(tot, att + finalDutyLeaves);
                student.setTotalClasses(tot);
                student.setAttendedClasses(merged);
                student.setDutyLeaveApprovedCount(finalDutyLeaves);
                double newPct = ((double) merged / tot) * 100.0;
                student.setAttendancePercentage(newPct);

                userManager.updateUser(student);
                dialog.dismiss();

                filterAndRenderStudents();
                Toast.makeText(this, "⚡ Monthly Attendance Merged & Credited for " + student.getFullName(), Toast.LENGTH_LONG).show();

                new AlertDialog.Builder(this)
                        .setTitle("🎉 Monthly Attendance Consolidated!")
                        .setMessage("Consolidated attendance record saved for:\n"
                                + "👤 Student: " + student.getFullName() + " (" + student.getId() + ")\n"
                                + "📅 Cycle: " + (spMonth != null && spMonth.getSelectedItem() != null ? spMonth.getSelectedItem().toString() : "Current Month") + "\n\n"
                                + "Breakdown:\n"
                                + "• Classroom Lectures Attended: " + att + "\n"
                                + "• Availed Spot & Duty Passes: +" + finalDutyLeaves + " lectures credited\n"
                                + "• Consolidated Total Attended: " + merged + " / " + tot + " lectures\n"
                                + "• Final Cumulative Attendance: " + String.format(java.util.Locale.US, "%.1f%%", newPct) + "\n\n"
                                + (newPct >= 75.0 ? "✅ This student is SAFE. Co-curricular duty credits have removed attendance defaults and warning letters."
                                                  : "ℹ️ Student attendance updated in institutional records."))
                        .setPositiveButton("OK", null)
                        .show();
            });
        }

        dialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCurrentUserData();
        updateNotificationBadge();
    }

    private void loadCurrentUserData() {
        User user = userManager.getCurrentUser();
        if (user == null) {
            user = userManager.getPrincipalUser();
            if (user != null) {
                userManager.setCurrentUser(user);
            }
        }
        if (user != null) {
            currentUser = user;
            if (tvHeaderStudentName != null && user.getFullName() != null) {
                tvHeaderStudentName.setText(user.getFullName());
            }
            if (tvHeaderAvatar != null) {
                tvHeaderAvatar.setText(user.getInitials());
            }
            // Update latest notice on Home page card
            refreshNoticeCard();

            if (user.isPrincipal()) {
                // Show Principal Dashboard, hide teacher & student widgets
                if (layoutPrincipalDashboard != null) layoutPrincipalDashboard.setVisibility(View.VISIBLE);
                if (layoutTeacherDashboard != null) layoutTeacherDashboard.setVisibility(View.GONE);
                if (layoutStudentHero != null) layoutStudentHero.setVisibility(View.GONE);
                if (layoutStudentQuickActions != null) layoutStudentQuickActions.setVisibility(View.GONE);
                if (layoutStudentWeek != null) layoutStudentWeek.setVisibility(View.GONE);

                filterAndRenderStudents();
                filterAndRenderTeachers();

            } else if (user.isTeacherOrDirector()) {
                // Show Teacher Dashboard, hide principal and student widgets
                if (layoutPrincipalDashboard != null) layoutPrincipalDashboard.setVisibility(View.GONE);
                if (layoutTeacherDashboard != null) layoutTeacherDashboard.setVisibility(View.VISIBLE);
                if (layoutStudentHero != null) layoutStudentHero.setVisibility(View.GONE);
                if (layoutStudentQuickActions != null) layoutStudentQuickActions.setVisibility(View.GONE);
                if (layoutStudentWeek != null) layoutStudentWeek.setVisibility(View.VISIBLE);

                TextView tvTeacherDeptBadge = findViewById(R.id.tvTeacherDeptBadge);
                TextView tvTeacherHeroName = findViewById(R.id.tvTeacherHeroName);
                TextView tvTeacherHeroMeta = findViewById(R.id.tvTeacherHeroMeta);

                if (user.isEventCoordinatorOrIncharge()) {
                    if (tvTeacherDeptBadge != null) {
                        tvTeacherDeptBadge.setText("🏆 EVENT INCHARGE CONSOLE");
                    }
                    if (tvTeacherHeroName != null) {
                        tvTeacherHeroName.setText(user.getFullName());
                    }
                    if (tvTeacherHeroMeta != null) {
                        tvTeacherHeroMeta.setText("🎯 " + user.getProgramme() + " • Login: " + user.getEmail() + " • Ground Roll-Call & Duty Redirection Active");
                    }
                } else {
                    if (tvTeacherDeptBadge != null) {
                        tvTeacherDeptBadge.setText(user.getDepartment().isEmpty() ? "Faculty" : user.getDepartment());
                    }
                    if (tvTeacherHeroName != null) {
                        tvTeacherHeroName.setText(user.getFullName() + " • " + (user.getDesignation().isEmpty() ? "Faculty Lecturer" : user.getDesignation()));
                    }
                    if (tvTeacherHeroMeta != null) {
                        String sub = user.getSubjects().isEmpty() ? "General" : user.getSubjects();
                        tvTeacherHeroMeta.setText("Subjects: " + sub + " • Staff ID: " + user.getId() + " • YCASC Academic Council");
                    }
                }

            } else {
                // Student Account
                if (layoutPrincipalDashboard != null) layoutPrincipalDashboard.setVisibility(View.GONE);
                if (layoutTeacherDashboard != null) layoutTeacherDashboard.setVisibility(View.GONE);
                if (layoutStudentHero != null) layoutStudentHero.setVisibility(View.VISIBLE);
                if (layoutStudentQuickActions != null) layoutStudentQuickActions.setVisibility(View.VISIBLE);
                if (layoutStudentWeek != null) layoutStudentWeek.setVisibility(View.VISIBLE);

                MaterialButton btnNewSpotPass = findViewById(R.id.btnNewSpotPass);
                MaterialButton btnPermission = findViewById(R.id.btnPermission);
                if (btnNewSpotPass != null) {
                    btnNewSpotPass.setText(R.string.new_spot_pass);
                    btnNewSpotPass.setOnClickListener(v -> showNewSpotPassDialog());
                }
                if (btnPermission != null) {
                    btnPermission.setText(R.string.permission);
                    btnPermission.setOnClickListener(v -> showNewPermissionDialog());
                }

                // Dynamic Academic Attendance Card for Student
                TextView tvAttendanceValue = findViewById(R.id.tvAttendanceValue);
                TextView tvAttendanceBreakdown = findViewById(R.id.tvAttendanceBreakdown);
                TextView tvAttendanceStatus = findViewById(R.id.tvAttendanceStatus);
                com.google.android.material.progressindicator.CircularProgressIndicator ringAttendance = findViewById(R.id.ringAttendance);

                double pct = user.getAttendancePercentage();
                int total = user.getTotalClasses() > 0 ? user.getTotalClasses() : 60;
                int attended = user.getAttendedClasses() > 0 ? user.getAttendedClasses() : (int) Math.round(total * (pct / 100.0));
                int duty = user.getDutyLeaveApprovedCount();

                if (tvAttendanceValue != null) {
                    tvAttendanceValue.setText(String.format(java.util.Locale.US, "%.0f%%", pct));
                }
                if (ringAttendance != null) {
                    ringAttendance.setProgress((int) Math.round(pct));
                    if (pct < 70.0) {
                        ringAttendance.setIndicatorColor(getColor(R.color.coral));
                    } else if (pct < 75.0) {
                        ringAttendance.setIndicatorColor(getColor(R.color.pastel_butter_primary));
                    } else {
                        ringAttendance.setIndicatorColor(getColor(R.color.accent_ring));
                    }
                }
                if (tvAttendanceStatus != null) {
                    if (pct < 70.0) {
                        tvAttendanceStatus.setText("Alert");
                        tvAttendanceStatus.setTextColor(getColor(R.color.coral));
                    } else if (pct < 75.0) {
                        tvAttendanceStatus.setText("Warning");
                        tvAttendanceStatus.setTextColor(getColor(R.color.pastel_butter_primary));
                    } else {
                        tvAttendanceStatus.setText(R.string.attendance_safe);
                        tvAttendanceStatus.setTextColor(getColor(R.color.white));
                    }
                }
                if (tvAttendanceBreakdown != null) {
                    if (pct < 70.0) {
                        tvAttendanceBreakdown.setText("Attended " + attended + "/" + total + " classes • ⚠️ Low Attendance (Below 70%)");
                    } else {
                        tvAttendanceBreakdown.setText("Attended " + attended + "/" + total + " classes • " + duty + " duty leaves counted");
                    }
                }
            }
        }
    }



    /**
     * Connects week day cards (Mon - Sat) in the "This week" horizontal bar
     * to open the interactive Academic Calendar for that specific date.
     */
    private void setupWeekDayClicks() {
        int[] dayIds = new int[]{R.id.dayMon, R.id.dayTue, R.id.dayWed, R.id.dayThu, R.id.dayFri, R.id.daySat};
        String[] dayDates = new String[]{
                "Mon, 19 Oct 2026",
                "Tue, 20 Oct 2026",
                "Wed, 21 Oct 2026",
                "Thu, 22 Oct 2026",
                "Fri, 23 Oct 2026",
                "Sat, 24 Oct 2026"
        };
        for (int i = 0; i < dayIds.length; i++) {
            View dayView = findViewById(dayIds[i]);
            if (dayView != null) {
                final String d = dayDates[i];
                dayView.setOnClickListener(v -> showCalendarDialog(d));
            }
        }
    }

    /**
     * Interactive Academic & Spot Pass Calendar Dialog.
     * Scrollable calendar view.
     * For Principal: Displays all permissions and spot passes the Principal/Institution has allowed to students.
     * For Students: Displays lecture timetable, clashing periods, and student spot passes on selected date.
     */
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
        android.widget.LinearLayout containerTimetableEntries = dialogView.findViewById(R.id.containerTimetableEntries);
        TextView btnTeacherInterchangeAction = dialogView.findViewById(R.id.btnTeacherInterchangeAction);
        View layoutStudentPassesSection = dialogView.findViewById(R.id.layoutStudentPassesSection);
        View layoutPrincipalAllowedSection = dialogView.findViewById(R.id.layoutPrincipalAllowedSection);
        TextView tvPrincipalNoAllowedRequests = dialogView.findViewById(R.id.tvPrincipalNoAllowedRequests);
        android.widget.LinearLayout containerPrincipalAllowedList = dialogView.findViewById(R.id.containerPrincipalAllowedList);

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
                        : (currentUser.getDepartment() + " • Timetable & Student Requests"));
            }
            if (layoutTimetableSection != null) layoutTimetableSection.setVisibility(isPrincipal ? View.GONE : View.VISIBLE);
            if (layoutStudentPassesSection != null) layoutStudentPassesSection.setVisibility(isPrincipal ? View.GONE : View.VISIBLE);
            if (layoutPrincipalAllowedSection != null) layoutPrincipalAllowedSection.setVisibility(isPrincipal ? View.VISIBLE : View.GONE);
            if (btnCreatePass != null) {
                btnCreatePass.setText("Review All Requests");
                btnCreatePass.setIconResource(R.drawable.ic_file);
                btnCreatePass.setOnClickListener(v -> {
                    dialog.dismiss();
                    startActivity(new Intent(this, RequestsActivity.class));
                });
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
                // Principal View: Allowed student passes and permissions
                if (containerPrincipalAllowedList != null) {
                    containerPrincipalAllowedList.removeAllViews();
                }

                List<SpotPass> allPasses = spotPassManager.getAllSpotPasses();
                List<PermissionRequest> allPerms = permissionRequestManager.getAllRequests();
                int allowedCount = 0;

                // 1. Allowed Spot Passes
                for (SpotPass p : allPasses) {
                    if (p.isApproved()) {
                        allowedCount++;
                        android.widget.LinearLayout card = new android.widget.LinearLayout(this);
                        card.setOrientation(android.widget.LinearLayout.VERTICAL);
                        card.setBackgroundResource(R.drawable.bg_input_box);
                        card.setPadding(22, 16, 22, 16);

                        android.widget.LinearLayout.LayoutParams lp = new android.widget.LinearLayout.LayoutParams(
                                android.widget.LinearLayout.LayoutParams.MATCH_PARENT, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
                        lp.topMargin = 10;
                        card.setLayoutParams(lp);

                        // Row 1
                        android.widget.LinearLayout r1 = new android.widget.LinearLayout(this);
                        r1.setOrientation(android.widget.LinearLayout.HORIZONTAL);
                        r1.setGravity(android.view.Gravity.CENTER_VERTICAL);

                        TextView titleTv = new TextView(this);
                        android.widget.LinearLayout.LayoutParams titleLp = new android.widget.LinearLayout.LayoutParams(0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
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

                        // Row 2: Student info
                        TextView metaTv = new TextView(this);
                        metaTv.setText("Student: " + p.getStudentName() + " (" + p.getStudentRoll() + ") • " + p.getTimeSlot());
                        metaTv.setTextColor(getColor(R.color.text_secondary));
                        metaTv.setTextSize(11.5f);
                        metaTv.setPadding(0, 4, 0, 0);
                        card.addView(metaTv);

                        card.setOnClickListener(v -> {
                            dialog.dismiss();
                            showSpotPassDetailsDialog(p);
                        });

                        if (containerPrincipalAllowedList != null) {
                            containerPrincipalAllowedList.addView(card);
                        }
                    }
                }

                // 2. Allowed Permissions
                for (PermissionRequest r : allPerms) {
                    if (r.isApproved()) {
                        allowedCount++;
                        android.widget.LinearLayout card = new android.widget.LinearLayout(this);
                        card.setOrientation(android.widget.LinearLayout.VERTICAL);
                        card.setBackgroundResource(R.drawable.bg_input_box);
                        card.setPadding(22, 16, 22, 16);

                        android.widget.LinearLayout.LayoutParams lp = new android.widget.LinearLayout.LayoutParams(
                                android.widget.LinearLayout.LayoutParams.MATCH_PARENT, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
                        lp.topMargin = 10;
                        card.setLayoutParams(lp);

                        // Row 1
                        android.widget.LinearLayout r1 = new android.widget.LinearLayout(this);
                        r1.setOrientation(android.widget.LinearLayout.HORIZONTAL);
                        r1.setGravity(android.view.Gravity.CENTER_VERTICAL);

                        TextView titleTv = new TextView(this);
                        android.widget.LinearLayout.LayoutParams titleLp = new android.widget.LinearLayout.LayoutParams(0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
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

                        // Row 2: Student info
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
                // Student & Teacher Dynamic Timetable & Spot Passes View
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

                    final SpotPass passToOpen = matchingPass;
                    if (cardPassPreview != null) {
                        cardPassPreview.setOnClickListener(v -> {
                            dialog.dismiss();
                            showSpotPassDetailsDialog(passToOpen);
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
                SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy", Locale.US);
                currentDateHolder[0] = sdf.format(c.getTime());
                updateDateDisplay[0].run();
            });
        }

        if (btnClose != null) {
            btnClose.setOnClickListener(v -> dialog.dismiss());
        }

        if (!isPrincipal && !isTeacher && btnCreatePass != null) {
            btnCreatePass.setOnClickListener(v -> {
                dialog.dismiss();
                showNewSpotPassDialog(currentDateHolder[0]);
            });
        }

        dialog.show();

        // Ensure comfortable height and scrollability
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

    /**
     * Dynamically renders the weekly timetable for the student's enrolled subjects or teacher's stream.
     * Includes all 5 daily lecture slots + 11:00 AM - 11:30 AM Recess/Break.
     * Displays Pass Clashed or Swapped badges.
     */
    private void renderTimetableForDate(android.widget.LinearLayout container, String dateStr,
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

            // Check if slot clashes with student's approved or pending spot pass
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

            android.widget.LinearLayout row = new android.widget.LinearLayout(this);
            row.setOrientation(android.widget.LinearLayout.HORIZONTAL);
            row.setGravity(android.view.Gravity.CENTER_VERTICAL);
            row.setPadding(0, dp(4), 0, dp(4));

            // Time Column
            TextView tvTime = new TextView(this);
            android.widget.LinearLayout.LayoutParams timeLp = new android.widget.LinearLayout.LayoutParams(dp(74), android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
            tvTime.setLayoutParams(timeLp);
            tvTime.setText(slot.getStartTime());
            tvTime.setTextColor(getColor(R.color.pastel_lavender_text));
            tvTime.setTextSize(11f);
            tvTime.setTypeface(null, android.graphics.Typeface.BOLD);
            row.addView(tvTime);

            // Subject & Teacher Details Column
            android.widget.LinearLayout midCol = new android.widget.LinearLayout(this);
            midCol.setOrientation(android.widget.LinearLayout.VERTICAL);
            android.widget.LinearLayout.LayoutParams midLp = new android.widget.LinearLayout.LayoutParams(0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
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

            // Status Badge
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

            // If teacher, allow clicking slot to directly interchange it!
            if (isTeacherOrAdmin) {
                row.setBackgroundResource(R.drawable.bg_chip_white);
                row.setPadding(dp(8), dp(8), dp(8), dp(8));
                row.setClickable(true);
                row.setFocusable(true);
                final TimetableSlot currentSlot = slot;
                row.setOnClickListener(v -> showInterchangeLectureDialog(parentDialog, onRefreshCallback, currentSlot));
            }

            container.addView(row);

            // Insert Recess / Break (11:00 AM - 11:30 AM) right after Lecture Slot 3!
            if (slot.getSlotNumber() == 3) {
                View sep1 = new View(this);
                android.widget.LinearLayout.LayoutParams sepLp1 = new android.widget.LinearLayout.LayoutParams(android.widget.LinearLayout.LayoutParams.MATCH_PARENT, dp(1));
                sepLp1.setMargins(0, dp(8), 0, dp(8));
                sep1.setLayoutParams(sepLp1);
                sep1.setBackgroundColor(0xFFEAE3FC);
                container.addView(sep1);

                android.widget.LinearLayout breakRow = new android.widget.LinearLayout(this);
                breakRow.setOrientation(android.widget.LinearLayout.HORIZONTAL);
                breakRow.setGravity(android.view.Gravity.CENTER_VERTICAL);
                breakRow.setBackgroundResource(R.drawable.bg_chip_mint);
                breakRow.setPadding(dp(10), dp(7), dp(10), dp(7));

                TextView tvBreakTime = new TextView(this);
                android.widget.LinearLayout.LayoutParams bTimeLp = new android.widget.LinearLayout.LayoutParams(dp(74), android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
                tvBreakTime.setLayoutParams(bTimeLp);
                tvBreakTime.setText("11:00 AM");
                tvBreakTime.setTextColor(getColor(R.color.pastel_mint_text));
                tvBreakTime.setTextSize(11f);
                tvBreakTime.setTypeface(null, android.graphics.Typeface.BOLD);
                breakRow.addView(tvBreakTime);

                TextView tvBreakLabel = new TextView(this);
                android.widget.LinearLayout.LayoutParams bLblLp = new android.widget.LinearLayout.LayoutParams(0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
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
                android.widget.LinearLayout.LayoutParams sepLp = new android.widget.LinearLayout.LayoutParams(android.widget.LinearLayout.LayoutParams.MATCH_PARENT, dp(1));
                sepLp.setMargins(0, dp(8), 0, dp(8));
                sep.setLayoutParams(sepLp);
                sep.setBackgroundColor(0xFFEAE3FC);
                container.addView(sep);
            }
        }
    }

    /**
     * Interchange Dialog allowing teachers to coordinate and swap lecture slots across streams and days.
     * Updates the persistent schedule and posts an institutional notice.
     */
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

        // Populate Streams: Science Stream, Commerce Stream, Arts Stream
        String[] streams = new String[]{
                TimetableManager.STREAM_SCIENCE + " (B.Sc.)",
                TimetableManager.STREAM_COMMERCE + " (B.Com)",
                TimetableManager.STREAM_ARTS + " (B.A.)"
        };
        ArrayAdapter<String> streamAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, streams);
        spinnerStream.setAdapter(streamAdapter);

        // Pre-select stream
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

        // Populate Days: Monday through Saturday
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
                // Guaranteed advance preparation notification broadcast to students of this stream
                String noticeTitle = "🚨 Lecture Swap Alert: " + slotA.getSubject() + " ⇄ " + slotB.getSubject() + " (" + slotA.getDayOfWeek() + ")";
                String noticeContent = "ATTENTION STUDENTS — ADVANCE LECTURE SCHEDULE CHANGE:\n\n"
                        + "Please be advised that your lecture timetable for " + slotA.getDayOfWeek() + " has been officially swapped by faculty:\n\n"
                        + "• " + slotA.getTimeDisplay() + " is now: " + slotB.getSubject() + " (Taught by " + slotB.getTeacherName() + " • Room: " + slotB.getRoom() + ")\n"
                        + "• " + slotB.getTimeDisplay() + " is now: " + slotA.getSubject() + " (Taught by " + slotA.getTeacherName() + " • Room: " + slotA.getRoom() + ")\n\n"
                        + "Faculty Coordination Note: " + note + "\n"
                        + "Coordination by: " + teacherName + "\n\n"
                        + "⚠️ Action Required: Students must prepare and bring necessary textbooks, assignments, and study materials for " + slotB.getSubject() + " and " + slotA.getSubject() + " in advance according to this swapped timetable.";

                Notice n = new Notice(
                        "NOT-SWAP-" + System.currentTimeMillis(),
                        noticeTitle,
                        "Timetable & Lecture Swap Notice",
                        teacherName + " (Faculty Coordination)",
                        "Students of " + slotA.getStream(),
                        noticeContent,
                        slotA.getDayOfWeek(),
                        System.currentTimeMillis(),
                        slotA.getTimeDisplay() + " / " + slotB.getTimeDisplay()
                );
                noticeManager.addNotice(n);
                refreshNoticeCard();
                updateNotificationBadge();

                Toast.makeText(this, "🎉 Lectures swapped! Advance preparation alert sent to " + slotA.getStream() + " students.", Toast.LENGTH_LONG).show();
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

    /**
     * Dialog for Principal to upload/publish an official notice.
     */
    private void showNewNoticeDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_new_notice, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        EditText etTitle = dialogView.findViewById(R.id.etNoticeTitle);
        Spinner spCategory = dialogView.findViewById(R.id.spinnerNoticeCategory);
        TextView tvDate = dialogView.findViewById(R.id.tvNoticeDateDisplay);
        EditText etEventTime = dialogView.findViewById(R.id.etNoticeEventTime);
        EditText etContent = dialogView.findViewById(R.id.etNoticeContent);
        CheckBox cbNoticeCcPrincipal = dialogView.findViewById(R.id.cbNoticeCcPrincipal);
        EditText etNoticeMeetingOutcomes = dialogView.findViewById(R.id.etNoticeMeetingOutcomes);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btnCancelNotice);
        MaterialButton btnPublish = dialogView.findViewById(R.id.btnPublishNotice);

        String[] categories = new String[]{
                "Sports & Athletics",
                "Academic Council",
                "Examination Notice",
                "Duty Leave & Passes",
                "Cultural & Annual Day",
                "General Administration"
        };
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categories);
        if (spCategory != null) spCategory.setAdapter(catAdapter);

        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault());
        String todayDate = sdf.format(new java.util.Date());
        if (tvDate != null) tvDate.setText(todayDate);

        if (btnCancel != null) btnCancel.setOnClickListener(v -> dialog.dismiss());

        if (btnPublish != null) {
            btnPublish.setOnClickListener(v -> {
                String title = etTitle != null ? etTitle.getText().toString().trim() : "";
                if (title.isEmpty()) {
                    if (etTitle != null) etTitle.setError("Please enter notice title");
                    return;
                }

                String category = spCategory != null ? spCategory.getSelectedItem().toString() : "General";
                String content = etContent != null ? etContent.getText().toString().trim() : "";
                if (content.isEmpty()) {
                    if (etContent != null) etContent.setError("Please enter notice content");
                    return;
                }

                String eventTime = etEventTime != null ? etEventTime.getText().toString().trim() : "";
                User current = userManager.getCurrentUser();
                String authorRole = (current != null && current.isPrincipal()) ? " (Principal)" :
                        ((current != null && current.isTeacherOrDirector()) ? " (Faculty / Class Teacher)" : "");
                String author = (current != null && current.getFullName() != null) ?
                        current.getFullName() + authorRole : "Principal / Academic Office";

                String notId = "NOT-2026-" + (int)(100 + Math.random() * 900);
                Notice newNotice = new Notice(
                        notId,
                        title,
                        category,
                        author,
                        "All Students & Faculty",
                        content,
                        todayDate,
                        System.currentTimeMillis(),
                        eventTime
                );

                if (cbNoticeCcPrincipal != null && cbNoticeCcPrincipal.isChecked()) {
                    newNotice.setCcNotice(true);
                    String outcomes = etNoticeMeetingOutcomes != null ? etNoticeMeetingOutcomes.getText().toString().trim() : "";
                    newNotice.setMeetingOutcomes(outcomes);
                }

                noticeManager.addNotice(newNotice);
                dialog.dismiss();

                // Immediately update home page card
                refreshNoticeCard();

                Toast.makeText(this, "📢 Official Notice Submitted & Published!", Toast.LENGTH_SHORT).show();
            });
        }

        dialog.show();
        if (dialog.getWindow() != null) {
            android.util.DisplayMetrics dm = getResources().getDisplayMetrics();
            int w = (int) (dm.widthPixels * 0.94);
            int maxH = (int) (dm.heightPixels * 0.86);
            dialog.getWindow().setLayout(w, maxH);
        }
    }

    /**
     * Refreshes the Notice card on the Home dashboard with the latest notice information.
     */
    private void refreshNoticeCard() {
        User user = userManager.getCurrentUser();
        List<Notice> all = noticeManager.getAllNotices();
        Notice latest = (all != null && !all.isEmpty()) ? all.get(0) : null;
        if (latest != null) {
            if (tvNoticeCardTitle != null) {
                tvNoticeCardTitle.setText(latest.getTitle());
            }
            if (tvNoticeCardMeta != null) {
                String meta = latest.getCategory() + " • " + latest.getAuthor() + " • " + latest.getDate();
                if (latest.getEventTime() != null && !latest.getEventTime().trim().isEmpty()) {
                    meta += " • " + latest.getEventTime();
                }
                if (all.size() > 1) {
                    meta += " • 📋 (" + all.size() + " Notices)";
                }
                tvNoticeCardMeta.setText(meta);
                tvNoticeCardMeta.setVisibility(View.VISIBLE);
            }
        } else {
            if (tvNoticeCardTitle != null) {
                tvNoticeCardTitle.setText("No Official Notices Yet");
            }
            if (tvNoticeCardMeta != null) {
                tvNoticeCardMeta.setText("");
                tvNoticeCardMeta.setVisibility(View.GONE);
            }
        }
    }

    /**
     * Dialog to view full notice details with staff controls (Edit & Remove).
     */
    private void showNoticeDetailsDialog(Notice notice) {
        User currentUser = userManager.getCurrentUser();
        boolean isStaff = currentUser != null && (currentUser.isTeacherOrDirector() || currentUser.isPrincipal());

        if (notice == null) {
            if (isStaff) {
                new AlertDialog.Builder(this)
                        .setTitle("📢 Official Notices")
                        .setMessage("No official notices published yet.\n\nWould you like to publish an official notice for students and faculty?")
                        .setPositiveButton("Publish Notice", (d, w) -> showNewNoticeDialog())
                        .setNegativeButton("Dismiss", null)
                        .show();
            } else {
                new AlertDialog.Builder(this)
                        .setTitle("📢 Notice Board")
                        .setMessage("There are no official notices published yet.\n\nOfficial circulars and notices from teachers will appear here once posted.")
                        .setPositiveButton("OK", null)
                        .show();
            }
            return;
        }

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_notice_details, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        TextView tvTitle = dialogView.findViewById(R.id.tvNoticeDetailTitle);
        TextView tvAuthor = dialogView.findViewById(R.id.tvNoticeDetailAuthor);
        TextView tvCategory = dialogView.findViewById(R.id.tvNoticeDetailCategoryBadge);
        TextView tvDate = dialogView.findViewById(R.id.tvNoticeDetailDate);
        TextView tvContent = dialogView.findViewById(R.id.tvNoticeDetailContent);
        View layoutStaffActions = dialogView.findViewById(R.id.layoutNoticeStaffActions);
        View btnEdit = dialogView.findViewById(R.id.btnEditNoticeDetail);
        View btnDelete = dialogView.findViewById(R.id.btnDeleteNoticeDetail);
        MaterialButton btnViewAll = dialogView.findViewById(R.id.btnViewAllNotices);
        View btnClose = dialogView.findViewById(R.id.btnCloseNoticeDetail);

        if (tvTitle != null) tvTitle.setText(notice.getTitle());
        if (tvAuthor != null) tvAuthor.setText(notice.getAuthor());
        if (tvCategory != null) tvCategory.setText(notice.getCategory());
        if (tvDate != null) {
            String dateText = "Issued: " + notice.getDate() + " • Target: " + notice.getTargetAudience();
            if (notice.getEventTime() != null && !notice.getEventTime().trim().isEmpty()) {
                dateText += "\n⏰ Event Timing: " + notice.getEventTime();
            }
            tvDate.setText(dateText);
        }
        if (tvContent != null) tvContent.setText(notice.getContent());

        // Cc Higher Authority & Meeting Outcomes Section
        View layoutNoticeCcSection = dialogView.findViewById(R.id.layoutNoticeCcSection);
        TextView tvNoticeCcStatusBadge = dialogView.findViewById(R.id.tvNoticeCcStatusBadge);
        TextView tvNoticeMeetingOutcomes = dialogView.findViewById(R.id.tvNoticeMeetingOutcomes);
        MaterialButton btnAcknowledgeNotice = dialogView.findViewById(R.id.btnAcknowledgeNotice);

        if (notice.isCcNotice()) {
            if (layoutNoticeCcSection != null) layoutNoticeCcSection.setVisibility(View.VISIBLE);
            if (tvNoticeMeetingOutcomes != null) {
                String outcomes = notice.getMeetingOutcomes();
                if (outcomes != null && !outcomes.trim().isEmpty()) {
                    tvNoticeMeetingOutcomes.setText(outcomes);
                } else {
                    tvNoticeMeetingOutcomes.setText("Formal meeting resolutions and co-curricular duty credits recorded for this circulation.");
                }
            }
            if (tvNoticeCcStatusBadge != null) {
                if (notice.isAcknowledgedByPrincipal()) {
                    String ackBy = notice.getAcknowledgedBy() != null ? notice.getAcknowledgedBy() : "Principal";
                    tvNoticeCcStatusBadge.setText("✅ Acknowledged by Principal (" + ackBy + ")");
                    tvNoticeCcStatusBadge.setBackgroundResource(R.drawable.bg_chip_mint);
                    if (btnAcknowledgeNotice != null) btnAcknowledgeNotice.setVisibility(View.GONE);
                } else {
                    tvNoticeCcStatusBadge.setText("⏳ Pending Principal Review");
                    tvNoticeCcStatusBadge.setBackgroundResource(R.drawable.bg_chip_yellow);
                    if (currentUser != null && currentUser.isPrincipal()) {
                        if (btnAcknowledgeNotice != null) {
                            btnAcknowledgeNotice.setVisibility(View.VISIBLE);
                            btnAcknowledgeNotice.setOnClickListener(v -> {
                                notice.setAcknowledgedByPrincipal(true);
                                notice.setAcknowledgedBy(currentUser.getFullName());
                                notice.setAcknowledgedTimestamp(System.currentTimeMillis());
                                noticeManager.updateNotice(notice);
                                dialog.dismiss();
                                showNoticeDetailsDialog(notice);
                                Toast.makeText(this, "🏛️ Circular acknowledged and signed off by Principal!", Toast.LENGTH_SHORT).show();
                            });
                        }
                    } else {
                        if (btnAcknowledgeNotice != null) btnAcknowledgeNotice.setVisibility(View.GONE);
                    }
                }
            }
        } else {
            if (layoutNoticeCcSection != null) layoutNoticeCcSection.setVisibility(View.GONE);
        }

        // Teachers and Principal can Edit and Remove notices
        if (isStaff && layoutStaffActions != null) {
            layoutStaffActions.setVisibility(View.VISIBLE);
            if (btnEdit != null) {
                btnEdit.setOnClickListener(v -> {
                    dialog.dismiss();
                    showEditNoticeDialog(notice);
                });
            }
            if (btnDelete != null) {
                btnDelete.setOnClickListener(v -> {
                    confirmDeleteNotice(notice, dialog);
                });
            }
        } else if (layoutStaffActions != null) {
            layoutStaffActions.setVisibility(View.GONE);
        }

        List<Notice> allNotices = noticeManager.getAllNotices();
        if (btnViewAll != null) {
            if (allNotices.size() > 1 || isStaff) {
                btnViewAll.setVisibility(View.VISIBLE);
                btnViewAll.setText("📋 View Notice Board (" + allNotices.size() + ")");
                btnViewAll.setOnClickListener(v -> {
                    dialog.dismiss();
                    showAllNoticesDialog();
                });
            } else {
                btnViewAll.setVisibility(View.GONE);
            }
        }

        if (btnClose != null) {
            btnClose.setOnClickListener(v -> dialog.dismiss());
        }

        dialog.show();
    }

    /**
     * Dialog for Teachers and Principal to edit an existing official notice.
     */
    private void showEditNoticeDialog(Notice notice) {
        if (notice == null) return;

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_new_notice, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        TextView tvHeaderTitle = dialogView.findViewById(R.id.tvNoticeDialogHeaderTitle);
        if (tvHeaderTitle != null) {
            tvHeaderTitle.setText("Edit Notice");
        }

        EditText etTitle = dialogView.findViewById(R.id.etNoticeTitle);
        Spinner spCategory = dialogView.findViewById(R.id.spinnerNoticeCategory);
        TextView tvDate = dialogView.findViewById(R.id.tvNoticeDateDisplay);
        EditText etEventTime = dialogView.findViewById(R.id.etNoticeEventTime);
        EditText etContent = dialogView.findViewById(R.id.etNoticeContent);
        CheckBox cbNoticeCcPrincipal = dialogView.findViewById(R.id.cbNoticeCcPrincipal);
        EditText etNoticeMeetingOutcomes = dialogView.findViewById(R.id.etNoticeMeetingOutcomes);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btnCancelNotice);
        MaterialButton btnPublish = dialogView.findViewById(R.id.btnPublishNotice);

        if (btnPublish != null) {
            btnPublish.setText("Save Changes");
        }

        String[] categories = new String[]{
                "Sports & Athletics",
                "Academic Council",
                "Examination Notice",
                "Duty Leave & Passes",
                "Cultural & Annual Day",
                "General Administration"
        };
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categories);
        if (spCategory != null) {
            spCategory.setAdapter(catAdapter);
            for (int i = 0; i < categories.length; i++) {
                if (categories[i].equalsIgnoreCase(notice.getCategory())) {
                    spCategory.setSelection(i);
                    break;
                }
            }
        }

        if (etTitle != null) etTitle.setText(notice.getTitle());
        if (tvDate != null) tvDate.setText(notice.getDate());
        if (etEventTime != null) etEventTime.setText(notice.getEventTime());
        if (etContent != null) etContent.setText(notice.getContent());
        if (cbNoticeCcPrincipal != null) cbNoticeCcPrincipal.setChecked(notice.isCcNotice());
        if (etNoticeMeetingOutcomes != null && notice.getMeetingOutcomes() != null) {
            etNoticeMeetingOutcomes.setText(notice.getMeetingOutcomes());
        }

        if (btnCancel != null) btnCancel.setOnClickListener(v -> dialog.dismiss());

        if (btnPublish != null) {
            btnPublish.setOnClickListener(v -> {
                String title = etTitle != null ? etTitle.getText().toString().trim() : "";
                if (title.isEmpty()) {
                    if (etTitle != null) etTitle.setError("Please enter notice title");
                    return;
                }

                String category = spCategory != null ? spCategory.getSelectedItem().toString() : notice.getCategory();
                String content = etContent != null ? etContent.getText().toString().trim() : "";
                if (content.isEmpty()) {
                    if (etContent != null) etContent.setError("Please enter notice content");
                    return;
                }

                String eventTime = etEventTime != null ? etEventTime.getText().toString().trim() : "";

                notice.setTitle(title);
                notice.setCategory(category);
                notice.setEventTime(eventTime);
                notice.setContent(content);
                if (cbNoticeCcPrincipal != null) {
                    notice.setCcNotice(cbNoticeCcPrincipal.isChecked());
                }
                if (etNoticeMeetingOutcomes != null) {
                    notice.setMeetingOutcomes(etNoticeMeetingOutcomes.getText().toString().trim());
                }

                noticeManager.updateNotice(notice);
                dialog.dismiss();

                refreshNoticeCard();
                Toast.makeText(this, "✏️ Official Notice Updated Successfully!", Toast.LENGTH_SHORT).show();

                // Re-open updated notice detail
                showNoticeDetailsDialog(notice);
            });
        }

        dialog.show();
        if (dialog.getWindow() != null) {
            android.util.DisplayMetrics dm = getResources().getDisplayMetrics();
            int w = (int) (dm.widthPixels * 0.94);
            int maxH = (int) (dm.heightPixels * 0.86);
            dialog.getWindow().setLayout(w, maxH);
        }
    }

    /**
     * Confirmation dialog for Teachers and Principal to delete a notice.
     */
    private void confirmDeleteNotice(Notice notice, AlertDialog parentDialog) {
        if (notice == null) return;

        new AlertDialog.Builder(this)
                .setTitle("🗑️ Remove Notice?")
                .setMessage("Are you sure you want to remove this notice?\n\n\"" + notice.getTitle() + "\"\n\nIt will be permanently deleted from the notice board.")
                .setPositiveButton("Remove Notice", (d, w) -> {
                    noticeManager.deleteNotice(notice.getId());
                    if (parentDialog != null) {
                        parentDialog.dismiss();
                    }
                    refreshNoticeCard();
                    Toast.makeText(this, "🗑️ Notice removed from board", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Dialog showing the full Notice Board with all notices,
     * allowing Teachers and Principal to edit, delete, or add notices.
     */
    private void showAllNoticesDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_all_notices, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        User currentUser = userManager.getCurrentUser();
        boolean isStaff = currentUser != null && (currentUser.isTeacherOrDirector() || currentUser.isPrincipal());

        View btnNewNotice = dialogView.findViewById(R.id.btnAllNoticesNewNotice);
        View layoutEmpty = dialogView.findViewById(R.id.layoutEmptyAllNotices);
        android.widget.LinearLayout containerList = dialogView.findViewById(R.id.containerAllNoticesList);
        View btnClose = dialogView.findViewById(R.id.btnCloseAllNotices);

        if (isStaff && btnNewNotice != null) {
            btnNewNotice.setVisibility(View.VISIBLE);
            btnNewNotice.setOnClickListener(v -> {
                dialog.dismiss();
                showNewNoticeDialog();
            });
        }

        List<Notice> notices = noticeManager.getAllNotices();
        if (notices.isEmpty()) {
            if (layoutEmpty != null) layoutEmpty.setVisibility(View.VISIBLE);
            if (containerList != null) containerList.setVisibility(View.GONE);
        } else {
            if (layoutEmpty != null) layoutEmpty.setVisibility(View.GONE);
            if (containerList != null) {
                containerList.setVisibility(View.VISIBLE);
                containerList.removeAllViews();

                for (Notice n : notices) {
                    View itemView = LayoutInflater.from(this).inflate(R.layout.item_notice_card, containerList, false);

                    TextView tvCategory = itemView.findViewById(R.id.tvItemNoticeCategory);
                    TextView tvDate = itemView.findViewById(R.id.tvItemNoticeDate);
                    TextView tvTime = itemView.findViewById(R.id.tvItemNoticeEventTime);
                    TextView tvTitle = itemView.findViewById(R.id.tvItemNoticeTitle);
                    TextView tvContent = itemView.findViewById(R.id.tvItemNoticeContent);
                    TextView tvAuthor = itemView.findViewById(R.id.tvItemNoticeAuthor);
                    View layoutActions = itemView.findViewById(R.id.layoutItemNoticeActions);
                    View btnItemEdit = itemView.findViewById(R.id.btnItemNoticeEdit);
                    View btnItemDelete = itemView.findViewById(R.id.btnItemNoticeDelete);

                    if (tvCategory != null) tvCategory.setText(n.getCategory());
                    if (tvDate != null) tvDate.setText(n.getDate());
                    if (tvTime != null) {
                        if (n.getEventTime() != null && !n.getEventTime().trim().isEmpty()) {
                            tvTime.setText("⏰ " + n.getEventTime());
                            tvTime.setVisibility(View.VISIBLE);
                        } else {
                            tvTime.setVisibility(View.GONE);
                        }
                    }
                    if (tvTitle != null) tvTitle.setText(n.getTitle());
                    if (tvContent != null) tvContent.setText(n.getContent());
                    if (tvAuthor != null) tvAuthor.setText("By: " + n.getAuthor());

                    if (isStaff && layoutActions != null) {
                        layoutActions.setVisibility(View.VISIBLE);
                        if (btnItemEdit != null) {
                            btnItemEdit.setOnClickListener(v -> {
                                dialog.dismiss();
                                showEditNoticeDialog(n);
                            });
                        }
                        if (btnItemDelete != null) {
                            btnItemDelete.setOnClickListener(v -> {
                                confirmDeleteNotice(n, null);
                                dialog.dismiss();
                                showAllNoticesDialog();
                            });
                        }
                    } else if (layoutActions != null) {
                        layoutActions.setVisibility(View.GONE);
                    }

                    itemView.setOnClickListener(v -> {
                        dialog.dismiss();
                        showNoticeDetailsDialog(n);
                    });

                    containerList.addView(itemView);
                }
            }
        }

        if (btnClose != null) {
            btnClose.setOnClickListener(v -> dialog.dismiss());
        }

        dialog.show();
    }

    /**
     * Dialog showing student's official alerts and notifications,
     * including all passes and permissions approved by Principal or Class Teacher.
     */
    private void showNotificationsDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_notifications, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        TextView tvSubtitle = dialogView.findViewById(R.id.tvNotificationsSubtitle);
        TextView tvCountBadge = dialogView.findViewById(R.id.tvNotificationsCountBadge);
        TextView tvNoNotifs = dialogView.findViewById(R.id.tvNoNotifications);
        android.widget.LinearLayout container = dialogView.findViewById(R.id.containerNotificationsList);
        MaterialButton btnClose = dialogView.findViewById(R.id.btnCloseNotifications);
        MaterialButton btnViewAll = dialogView.findViewById(R.id.btnViewAllRequests);

        if (btnClose != null) {
            btnClose.setOnClickListener(v -> dialog.dismiss());
        }

        if (btnViewAll != null) {
            btnViewAll.setOnClickListener(v -> {
                dialog.dismiss();
                startActivity(new Intent(this, RequestsActivity.class));
            });
        }

        User current = userManager.getCurrentUser();
        String studentId = current != null ? current.getId() : "";
        List<SpotPass> approvedPasses = spotPassManager.getApprovedPassesForStudent(studentId);
        List<PermissionRequest> approvedPerms = permissionRequestManager.getApprovedRequestsForStudent(studentId);

        int totalCount = approvedPasses.size() + approvedPerms.size();
        if (tvCountBadge != null) {
            tvCountBadge.setText(totalCount + (totalCount == 1 ? " Approved Alert" : " Approved Alerts"));
        }

        if (container != null) {
            container.removeAllViews();
        }

        if (totalCount == 0) {
            if (tvNoNotifs != null) tvNoNotifs.setVisibility(View.VISIBLE);
        } else {
            if (tvNoNotifs != null) tvNoNotifs.setVisibility(View.GONE);

            // 1. Render Approved Spot Passes (Approved by Class Teacher / Subject Teacher)
            for (SpotPass p : approvedPasses) {
                android.widget.LinearLayout card = new android.widget.LinearLayout(this);
                card.setOrientation(android.widget.LinearLayout.VERTICAL);
                card.setBackgroundResource(R.drawable.bg_input_box);
                card.setPadding(24, 18, 24, 18);

                android.widget.LinearLayout.LayoutParams lp = new android.widget.LinearLayout.LayoutParams(
                        android.widget.LinearLayout.LayoutParams.MATCH_PARENT, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
                lp.topMargin = 12;
                card.setLayoutParams(lp);

                // Row 1: Header Badge & Status
                android.widget.LinearLayout headerRow = new android.widget.LinearLayout(this);
                headerRow.setOrientation(android.widget.LinearLayout.HORIZONTAL);
                headerRow.setGravity(android.view.Gravity.CENTER_VERTICAL);

                TextView tvApproverBadge = new TextView(this);
                android.widget.LinearLayout.LayoutParams badgeLp = new android.widget.LinearLayout.LayoutParams(0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
                tvApproverBadge.setLayoutParams(badgeLp);
                tvApproverBadge.setText("👩‍🏫 Approved by Class Teacher (" + p.getClassTeacher() + ")");
                tvApproverBadge.setTextColor(getColor(R.color.pastel_mint_text));
                tvApproverBadge.setTextSize(11f);
                tvApproverBadge.setTypeface(null, android.graphics.Typeface.BOLD);
                headerRow.addView(tvApproverBadge);

                TextView tvStatusBadge = new TextView(this);
                tvStatusBadge.setBackgroundResource(R.drawable.bg_chip_mint);
                tvStatusBadge.setText(p.getStatus() != null && !p.getStatus().isEmpty() ? p.getStatus() : "Approved");
                tvStatusBadge.setTextColor(getColor(R.color.pastel_mint_text));
                tvStatusBadge.setTextSize(10.5f);
                tvStatusBadge.setTypeface(null, android.graphics.Typeface.BOLD);
                tvStatusBadge.setPadding(14, 4, 14, 4);
                headerRow.addView(tvStatusBadge);

                card.addView(headerRow);

                // Row 2: Title
                TextView tvTitle = new TextView(this);
                tvTitle.setText("⚡ " + p.getActivityTitle());
                tvTitle.setTextColor(getColor(R.color.charcoal_dark));
                tvTitle.setTextSize(14f);
                tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);
                tvTitle.setPadding(0, 8, 0, 0);
                card.addView(tvTitle);

                // Row 3: Details & Timings
                TextView tvDetails = new TextView(this);
                tvDetails.setText("Date: " + p.getDate() + " • Slot: " + p.getTimeSlot() + " • Subject: " + p.getSubject());
                tvDetails.setTextColor(getColor(R.color.text_secondary));
                tvDetails.setTextSize(11.5f);
                tvDetails.setPadding(0, 4, 0, 0);
                card.addView(tvDetails);

                if (p.getReason() != null && !p.getReason().trim().isEmpty()) {
                    TextView tvReason = new TextView(this);
                    tvReason.setText("Reason: " + p.getReason());
                    tvReason.setTextColor(getColor(R.color.text_muted));
                    tvReason.setTextSize(11f);
                    tvReason.setPadding(0, 4, 0, 0);
                    card.addView(tvReason);
                }

                // Tap to view full pass details
                card.setOnClickListener(v -> {
                    dialog.dismiss();
                    showSpotPassDetailsDialog(p);
                });

                if (container != null) {
                    container.addView(card);
                }
            }

            // 2. Render Approved Permissions (Approved by Principal / Programme Director)
            for (PermissionRequest r : approvedPerms) {
                android.widget.LinearLayout card = new android.widget.LinearLayout(this);
                card.setOrientation(android.widget.LinearLayout.VERTICAL);
                card.setBackgroundResource(R.drawable.bg_input_box);
                card.setPadding(24, 18, 24, 18);

                android.widget.LinearLayout.LayoutParams lp = new android.widget.LinearLayout.LayoutParams(
                        android.widget.LinearLayout.LayoutParams.MATCH_PARENT, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
                lp.topMargin = 12;
                card.setLayoutParams(lp);

                // Row 1: Header Badge & Status
                android.widget.LinearLayout headerRow = new android.widget.LinearLayout(this);
                headerRow.setOrientation(android.widget.LinearLayout.HORIZONTAL);
                headerRow.setGravity(android.view.Gravity.CENTER_VERTICAL);

                TextView tvApproverBadge = new TextView(this);
                android.widget.LinearLayout.LayoutParams badgeLp = new android.widget.LinearLayout.LayoutParams(0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
                tvApproverBadge.setLayoutParams(badgeLp);
                String supervisor = r.getSupervisor() != null && !r.getSupervisor().isEmpty() ? r.getSupervisor() : "Principal";
                tvApproverBadge.setText("🎓 Approved by " + supervisor);
                tvApproverBadge.setTextColor(getColor(R.color.pastel_lavender_text));
                tvApproverBadge.setTextSize(11f);
                tvApproverBadge.setTypeface(null, android.graphics.Typeface.BOLD);
                headerRow.addView(tvApproverBadge);

                TextView tvStatusBadge = new TextView(this);
                tvStatusBadge.setBackgroundResource(R.drawable.bg_chip_mint);
                tvStatusBadge.setText(r.getStatus() != null && !r.getStatus().isEmpty() ? r.getStatus() : "Approved");
                tvStatusBadge.setTextColor(getColor(R.color.pastel_mint_text));
                tvStatusBadge.setTextSize(10.5f);
                tvStatusBadge.setTypeface(null, android.graphics.Typeface.BOLD);
                tvStatusBadge.setPadding(14, 4, 14, 4);
                headerRow.addView(tvStatusBadge);

                card.addView(headerRow);

                // Row 2: Title
                TextView tvTitle = new TextView(this);
                tvTitle.setText("📋 " + r.getEventTitle());
                tvTitle.setTextColor(getColor(R.color.charcoal_dark));
                tvTitle.setTextSize(14f);
                tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);
                tvTitle.setPadding(0, 8, 0, 0);
                card.addView(tvTitle);

                // Row 3: Event Dates & Dept
                TextView tvDetails = new TextView(this);
                tvDetails.setText("Dates: " + r.getEventDates() + " • Dept: " + r.getScopeOrDept());
                tvDetails.setTextColor(getColor(R.color.text_secondary));
                tvDetails.setTextSize(11.5f);
                tvDetails.setPadding(0, 4, 0, 0);
                card.addView(tvDetails);

                if (r.getRemarks() != null && !r.getRemarks().trim().isEmpty()) {
                    TextView tvRemarks = new TextView(this);
                    tvRemarks.setText("Sanction Remarks: " + r.getRemarks());
                    tvRemarks.setTextColor(getColor(R.color.text_muted));
                    tvRemarks.setTextSize(11f);
                    tvRemarks.setPadding(0, 4, 0, 0);
                    card.addView(tvRemarks);
                }

                // Tap to view full permission details
                card.setOnClickListener(v -> {
                    dialog.dismiss();
                    showPermissionDetailsDialog(r);
                });

                if (container != null) {
                    container.addView(card);
                }
            }
        }

        dialog.show();

        if (dialog.getWindow() != null) {
            android.util.DisplayMetrics dm = getResources().getDisplayMetrics();
            int h = (int) (dm.heightPixels * 0.85);
            int w = (int) (dm.widthPixels * 0.94);
            dialog.getWindow().setLayout(w, h);
        }
    }

    /**
     * Dialog to view full permission request details.
     */
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

        if (tvId != null) tvId.setText(perm.getId());
        if (tvStatus != null) tvStatus.setText(perm.getStatus());
        if (tvTitle != null) tvTitle.setText("Permission · " + perm.getEventTitle());
        if (tvStudentMeta != null) tvStudentMeta.setText(perm.getStudentName() + " • " + perm.getStudentId() + " • " + perm.getStudentClass());
        if (tvDates != null) tvDates.setText(perm.getEventDates());
        if (tvScope != null) tvScope.setText(perm.getScopeOrDept());
        if (tvSupervisor != null) tvSupervisor.setText(perm.getSupervisor());
        if (tvRemarks != null) tvRemarks.setText(perm.getRemarks());
        if (tvReason != null) tvReason.setText(perm.getReason());

        if (btnClose != null) {
            btnClose.setOnClickListener(v -> dialog.dismiss());
        }

        dialog.show();
    }

    public void showNewSpotPassDialog() {
        showNewSpotPassDialog(null);
    }

    /**
     * Modal dialog allowing students to apply for a New Spot Pass.
     * All student personal info (Name & Roll number) is auto-attached.
     * Student enters event type, timing needed, clashing lecture/period, clashing subject,
     * and optional duty leave reason.
     * Teacher teaching the clashing subject is dynamically resolved and populated.
     */
    public void showNewSpotPassDialog(String preSelectedDate) {
        User user = userManager.getCurrentUser();
        if (user == null || user.isTeacherOrDirector() || user.isPrincipal()) {
            Toast.makeText(this, "Only students can apply for Spot Passes", Toast.LENGTH_SHORT).show();
            return;
        }

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_new_spot_pass, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        // Student Info (Auto-attached, read-only)
        TextView tvAvatar = dialogView.findViewById(R.id.tvSpotPassStudentAvatar);
        TextView tvName = dialogView.findViewById(R.id.tvSpotPassStudentName);
        TextView tvMeta = dialogView.findViewById(R.id.tvSpotPassStudentMeta);

        // Event Type & Title
        Spinner spinnerEventType = dialogView.findViewById(R.id.spinnerEventType);
        EditText etActivityTitle = dialogView.findViewById(R.id.etSpotPassActivityTitle);

        // Category Options: Sports, Cultural, Technical Event, Volunteering, Other
        TextView chipSports = dialogView.findViewById(R.id.chipActSports);
        TextView chipCultural = dialogView.findViewById(R.id.chipActCultural);
        TextView chipTechnical = dialogView.findViewById(R.id.chipActTechnical);
        TextView chipVolunteering = dialogView.findViewById(R.id.chipActVolunteering);
        TextView chipOther = dialogView.findViewById(R.id.chipActOther);
        View containerOther = dialogView.findViewById(R.id.containerOtherEventType);
        EditText etOther = dialogView.findViewById(R.id.etOtherEventType);

        // Date & Timing
        View layoutPickPassDate = dialogView.findViewById(R.id.layoutPickPassDate);
        TextView tvPassSelectedDate = dialogView.findViewById(R.id.tvPassSelectedDate);
        EditText etPassTimingNeeded = dialogView.findViewById(R.id.etPassTimingNeeded);

        // Clashing Lecture & Subject
        EditText etTimeSlot = dialogView.findViewById(R.id.etTimeSlot);
        Spinner spinnerSubject = dialogView.findViewById(R.id.spinnerSubject);
        TextView tvClassTeacher = dialogView.findViewById(R.id.tvClassTeacherName);
        TextView tvSubjectTeacher = dialogView.findViewById(R.id.tvSubjectTeacherName);
        EditText etReason = dialogView.findViewById(R.id.etSpotPassReason);

        MaterialButton btnCancel = dialogView.findViewById(R.id.btnCancelSpotPass);
        MaterialButton btnSubmit = dialogView.findViewById(R.id.btnSubmitSpotPass);

        // 1. Populate Student Header (Auto-filled from user profile)
        tvAvatar.setText(user.getInitials());
        tvName.setText(user.getFullName());
        String metaText = user.getId() + " • " + (user.getYearOrClass().isEmpty() ? "First Year (Batch 2026)" : user.getYearOrClass())
                + " (" + (user.getDepartment().isEmpty() ? "Science" : user.getDepartment()) + ")";
        tvMeta.setText(metaText);

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
        tvClassTeacher.setText(assignedHod);

        final String[] selectedCategory = new String[]{"Sports"};

        // 3. Date Selection & DatePicker (Kept as is)
        final String[] selectedDateHolder = new String[]{
                preSelectedDate != null && !preSelectedDate.isEmpty() ? preSelectedDate : ""
        };
        tvPassSelectedDate.setText(!selectedDateHolder[0].isEmpty() ? selectedDateHolder[0] : "Select Date");

        if (layoutPickPassDate != null) {
            layoutPickPassDate.setOnClickListener(v -> {
                Calendar c = Calendar.getInstance();
                DatePickerDialog dpd = new DatePickerDialog(this, (dp, y, m, d) -> {
                    Calendar sel = Calendar.getInstance();
                    sel.set(y, m, d);
                    SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy", Locale.US);
                    selectedDateHolder[0] = sdf.format(sel.getTime());
                    tvPassSelectedDate.setText(selectedDateHolder[0]);
                }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
                dpd.show();
            });
        }

        // 5. Populate Clashing Subjects from student's stream
        List<String> subjects = user.getSubjectsList();
        if (subjects.isEmpty()) {
            subjects = Arrays.asList("Chemistry", "Physics", "Mathematics", "Biology", "English", "Computer Applications");
        }
        ArrayAdapter<String> subAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, subjects);
        spinnerSubject.setAdapter(subAdapter);

        // Dynamically update Subject Teacher when clashing subject is selected
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

        // 6. Category Chip Listeners (Categories select type without prefilling dummy demo values)
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

        // 7. Submit Action
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
            dialog.dismiss();

            // Celebratory confirmation showing explicit dispatch
            new AlertDialog.Builder(this)
                    .setTitle("🎉 Spot Pass Dispatched!")
                    .setMessage("Your Spot Pass for \"" + actTitle + "\" (" + selectedEventType + ")\n"
                            + "📅 Date: " + passDate + "\n"
                            + "⏰ Time: " + passTiming + "\n\n"
                            + "Routed to:\n"
                            + "1. Class Teacher:\n   " + ctTeacher + " (Approval Required)\n\n"
                            + "2. Subject Teacher:\n   " + subTeacher + " (" + selectedSub + " period attendance credit)\n\n"
                            + "Once approved, your absence is recorded as 'Duty Present'.")
                    .setPositiveButton("View in Requests", (d, which) -> {
                        showSpotPassDetailsDialog(newPass);
                    })
                    .setNegativeButton("OK", null)
                    .show();
        });

        dialog.show();
    }

    /**
     * Modal dialog allowing students to apply for Prior Permission for specific upcoming days.
     * Directed straight to their Department HOD.
     * Advance absence notice is logged and attendance is credited upon confirmation of the designated organiser/coach.
     */
    public void showNewPermissionDialog() {
        User user = userManager.getCurrentUser();
        if (user == null || user.isTeacherOrDirector() || user.isPrincipal()) {
            Toast.makeText(this, "Only students can apply for Prior Permission", Toast.LENGTH_SHORT).show();
            return;
        }

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_new_permission, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        // Student Info (Auto-attached, read-only)
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
        String metaText = user.getId() + " • " + (user.getYearOrClass().isEmpty() ? "First Year (Batch 2026)" : user.getYearOrClass())
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

                permissionRequestManager.addPermission(newReq);
                dialog.dismiss();

                new AlertDialog.Builder(this)
                        .setTitle("🎉 Prior Permission Dispatched!")
                        .setMessage("Your Prior Permission Request for \"" + title + "\" (" + cat + ")\n\n"
                                + "📅 Dates & Timing:\n   " + datesWithTiming + "\n\n"
                                + "🏛️ Directed To:\n   " + assignedHod + "\n\n"
                                + "👤 Attendance Confirmation By:\n   " + organiser + "\n\n"
                                + "The department has logged prior info of your absence. Full attendance credit will be issued upon organiser verification.")
                        .setPositiveButton("OK", null)
                        .show();
            });
        }

        dialog.show();
    }

    /**
     * Details Modal for any Spot Pass showing dual-approval tracking
     * (Class Teacher and Subject Teacher of that particular time).
     */
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
                            .setMessage("Do you want to cancel and remove Spot Pass " + pass.getId() + "? You can create a new pass with updated timings.")
                            .setPositiveButton("Yes, Cancel Pass", (d, which) -> {
                                spotPassManager.deleteSpotPass(pass.getId());
                                dialog.dismiss();
                                updateNotificationBadge();
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
                            Toast.makeText(this, "Spot Pass " + pass.getId() + " status updated: " + pass.getStatus(), Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });
        } else {
            btnClose.setOnClickListener(v -> dialog.dismiss());
        }

        dialog.show();
    }

    /**
     * Dialog showing Active Inter-Collegiate & State Events along with
     * Scheduled Ground & Venue Practice Sessions for coaches/teachers.
     */
    private void showEventsAndPracticeDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_events_and_practice, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        android.widget.LinearLayout containerEvents = dialogView.findViewById(R.id.containerEventsList);
        android.widget.LinearLayout containerSessions = dialogView.findViewById(R.id.containerPracticeSessionsList);
        MaterialButton btnClose = dialogView.findViewById(R.id.btnCloseEventsDialog);
        MaterialButton btnAppointIncharge = dialogView.findViewById(R.id.btnAppointInchargeInDialog);

        if (btnAppointIncharge != null) {
            boolean isPrincipal = currentUser != null && currentUser.isPrincipal();
            btnAppointIncharge.setVisibility(isPrincipal ? View.VISIBLE : View.GONE);
            btnAppointIncharge.setOnClickListener(v -> {
                dialog.dismiss();
                showAppointEventInchargeDialog();
            });
        }

        if (btnClose != null) {
            btnClose.setOnClickListener(v -> dialog.dismiss());
        }

        float density = getResources().getDisplayMetrics().density;

        // 1. Render Events List
        if (containerEvents != null) {
            containerEvents.removeAllViews();
            List<Event> events = eventManager.getAllEvents();
            if (events.isEmpty()) {
                TextView tvEmpty = new TextView(this);
                tvEmpty.setText("No active events listed at present.");
                tvEmpty.setTextColor(getColor(R.color.text_muted));
                tvEmpty.setTextSize(12f);
                containerEvents.addView(tvEmpty);
            } else {
                for (Event ev : events) {
                    android.widget.LinearLayout card = new android.widget.LinearLayout(this);
                    card.setOrientation(android.widget.LinearLayout.VERTICAL);
                    card.setBackgroundResource(R.drawable.bg_input_box);
                    card.setPadding((int)(12 * density), (int)(10 * density), (int)(12 * density), (int)(10 * density));
                    android.widget.LinearLayout.LayoutParams lp = new android.widget.LinearLayout.LayoutParams(
                            android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    lp.setMargins(0, 0, 0, (int)(8 * density));
                    card.setLayoutParams(lp);

                    // Row: Title and Level Badge
                    android.widget.LinearLayout topRow = new android.widget.LinearLayout(this);
                    topRow.setOrientation(android.widget.LinearLayout.HORIZONTAL);
                    topRow.setGravity(android.view.Gravity.CENTER_VERTICAL);

                    TextView tvTitle = new TextView(this);
                    android.widget.LinearLayout.LayoutParams titleLp = new android.widget.LinearLayout.LayoutParams(0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
                    tvTitle.setLayoutParams(titleLp);
                    tvTitle.setText(ev.getName());
                    tvTitle.setTextColor(getColor(R.color.charcoal_dark));
                    tvTitle.setTextSize(13.5f);
                    tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);
                    topRow.addView(tvTitle);

                    TextView tvLevel = new TextView(this);
                    tvLevel.setText(ev.getLevel());
                    tvLevel.setTextColor(getColor(R.color.pastel_lavender_text));
                    tvLevel.setBackgroundResource(R.drawable.bg_chip_white);
                    tvLevel.setPadding((int)(8 * density), (int)(3 * density), (int)(8 * density), (int)(3 * density));
                    tvLevel.setTextSize(10f);
                    tvLevel.setTypeface(null, android.graphics.Typeface.BOLD);
                    topRow.addView(tvLevel);

                    card.addView(topRow);

                    // Dates and Organiser
                    TextView tvMeta = new TextView(this);
                    tvMeta.setText("📅 " + ev.getDatesDisplay() + " • " + ev.getType() + " • " + ev.getOrganiser());
                    tvMeta.setTextColor(getColor(R.color.text_secondary));
                    tvMeta.setTextSize(11f);
                    tvMeta.setPadding(0, (int)(4 * density), 0, 0);
                    card.addView(tvMeta);

                    TextView tvCoord = new TextView(this);
                    tvCoord.setText("📍 " + ev.getVenue() + " | Coordinator: " + ev.getCoordinatorName());
                    tvCoord.setTextColor(getColor(R.color.text_muted));
                    tvCoord.setTextSize(10.5f);
                    tvCoord.setPadding(0, (int)(2 * density), 0, 0);
                    card.addView(tvCoord);

                    containerEvents.addView(card);
                }
            }
        }

        // 2. Render Practice Sessions List
        if (containerSessions != null) {
            containerSessions.removeAllViews();
            List<PracticeSession> sessions = eventManager.getAllPracticeSessions();
            if (sessions.isEmpty()) {
                TextView tvEmpty = new TextView(this);
                tvEmpty.setText("No scheduled ground practice sessions.");
                tvEmpty.setTextColor(getColor(R.color.text_muted));
                tvEmpty.setTextSize(12f);
                containerSessions.addView(tvEmpty);
            } else {
                for (PracticeSession ps : sessions) {
                    android.widget.LinearLayout card = new android.widget.LinearLayout(this);
                    card.setOrientation(android.widget.LinearLayout.VERTICAL);
                    card.setBackgroundResource(R.drawable.bg_card_lavender);
                    card.setPadding((int)(12 * density), (int)(12 * density), (int)(12 * density), (int)(12 * density));
                    android.widget.LinearLayout.LayoutParams lp = new android.widget.LinearLayout.LayoutParams(
                            android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    lp.setMargins(0, 0, 0, (int)(10 * density));
                    card.setLayoutParams(lp);

                    // Row: Session Title and Attendees count
                    android.widget.LinearLayout topRow = new android.widget.LinearLayout(this);
                    topRow.setOrientation(android.widget.LinearLayout.HORIZONTAL);
                    topRow.setGravity(android.view.Gravity.CENTER_VERTICAL);

                    TextView tvTitle = new TextView(this);
                    android.widget.LinearLayout.LayoutParams titleLp = new android.widget.LinearLayout.LayoutParams(0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
                    tvTitle.setLayoutParams(titleLp);
                    tvTitle.setText(ps.getEventName());
                    tvTitle.setTextColor(getColor(R.color.charcoal_dark));
                    tvTitle.setTextSize(13.5f);
                    tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);
                    topRow.addView(tvTitle);

                    TextView tvCount = new TextView(this);
                    int presentCount = ps.getPresentStudentIds().size();
                    tvCount.setText("⚽ " + presentCount + " Present");
                    tvCount.setTextColor(getColor(R.color.pastel_mint_text));
                    tvCount.setBackgroundResource(R.drawable.bg_chip_mint);
                    tvCount.setPadding((int)(8 * density), (int)(3 * density), (int)(8 * density), (int)(3 * density));
                    tvCount.setTextSize(10.5f);
                    tvCount.setTypeface(null, android.graphics.Typeface.BOLD);
                    topRow.addView(tvCount);

                    card.addView(topRow);

                    // Meta: Date, time, venue, coach
                    TextView tvMeta = new TextView(this);
                    tvMeta.setText("⏰ " + ps.getPracticeDate() + " (" + ps.getTimeSlot() + ") • " + ps.getSchedulingType());
                    tvMeta.setTextColor(getColor(R.color.text_secondary));
                    tvMeta.setTextSize(11f);
                    tvMeta.setPadding(0, (int)(4 * density), 0, 0);
                    card.addView(tvMeta);

                    TextView tvVenue = new TextView(this);
                    tvVenue.setText("📍 " + ps.getVenue() + " • Coach / Mentor: " + ps.getCoachName());
                    tvVenue.setTextColor(getColor(R.color.text_muted));
                    tvVenue.setTextSize(10.5f);
                    tvVenue.setPadding(0, (int)(2 * density), 0, 0);
                    card.addView(tvVenue);

                    // Action Button: Take Ground Attendance (only for Event Incharge / Coordinator)
                    Event parentEv = eventManager.getEventById(ps.getEventId());
                    boolean isCoordinator = currentUser != null && currentUser.isCoordinatorForSession(ps, parentEv);

                    if (isCoordinator) {
                        MaterialButton btnTakeAttendance = new MaterialButton(this);
                        btnTakeAttendance.setText("⚽ Take Ground Roll-Call (Event Incharge)");
                        btnTakeAttendance.setTextColor(getColor(R.color.white));
                        btnTakeAttendance.setTextSize(12f);
                        btnTakeAttendance.setTypeface(null, android.graphics.Typeface.BOLD);
                        btnTakeAttendance.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getColor(R.color.charcoal_dark)));
                        btnTakeAttendance.setCornerRadius((int)(12 * density));
                        android.widget.LinearLayout.LayoutParams btnLp = new android.widget.LinearLayout.LayoutParams(
                                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                                (int)(44 * density)
                        );
                        btnLp.setMargins(0, (int)(10 * density), 0, 0);
                        btnTakeAttendance.setLayoutParams(btnLp);
                        btnTakeAttendance.setOnClickListener(v -> {
                            dialog.dismiss();
                            showGroundAttendanceDialog(ps);
                        });
                        card.addView(btnTakeAttendance);
                    } else {
                        TextView tvRestricted = new TextView(this);
                        tvRestricted.setText("🔒 Attendance managed by Event Incharge: " + ps.getCoachName());
                        tvRestricted.setTextColor(getColor(R.color.text_secondary));
                        tvRestricted.setTextSize(11f);
                        tvRestricted.setBackgroundResource(R.drawable.bg_chip_white);
                        tvRestricted.setPadding((int)(10 * density), (int)(8 * density), (int)(10 * density), (int)(8 * density));
                        android.widget.LinearLayout.LayoutParams resLp = new android.widget.LinearLayout.LayoutParams(
                                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                        );
                        resLp.setMargins(0, (int)(10 * density), 0, 0);
                        tvRestricted.setLayoutParams(resLp);
                        card.addView(tvRestricted);
                    }

                    containerSessions.addView(card);
                }
            }
        }

        dialog.show();
    }

    /**
     * Dialog for Coaches / Event Coordinators to mark Ground Practice Attendance.
     * Physically marking students as present will immediately credit co-curricular duty leaves.
     */
    private void showGroundAttendanceDialog(PracticeSession session) {
        if (session == null) return;

        Event parentEv = eventManager.getEventById(session.getEventId());
        if (currentUser == null || !currentUser.isCoordinatorForSession(session, parentEv)) {
            Toast.makeText(this, "⚠️ Access Restricted: Only the Event Incharge or Coordinator (" + session.getCoachName() + ") can take attendance for this event.", Toast.LENGTH_LONG).show();
            return;
        }

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_ground_attendance, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        TextView tvMeta = dialogView.findViewById(R.id.tvGroundSessionMeta);
        TextView tvPresentBadge = dialogView.findViewById(R.id.tvGroundPresentCountBadge);
        android.widget.LinearLayout containerRoster = dialogView.findViewById(R.id.containerGroundRosterList);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btnCancelGroundAtt);
        MaterialButton btnSave = dialogView.findViewById(R.id.btnSaveGroundAtt);

        if (tvMeta != null) {
            tvMeta.setText(session.getEventName() + " • " + session.getVenue() + " (" + session.getPracticeDate() + " " + session.getStartTime() + ")");
        }

        List<User> students = userManager.getStudents();
        List<String> currentPresent = new ArrayList<>(session.getPresentStudentIds());
        Map<String, CheckBox> checkBoxMap = new HashMap<>();

        Runnable updateBadge = () -> {
            int count = 0;
            for (CheckBox cb : checkBoxMap.values()) {
                if (cb.isChecked()) count++;
            }
            if (tvPresentBadge != null) {
                tvPresentBadge.setText(count + " Present");
            }
        };

        if (containerRoster != null) {
            containerRoster.removeAllViews();
            float density = getResources().getDisplayMetrics().density;

            for (User s : students) {
                android.widget.LinearLayout item = new android.widget.LinearLayout(this);
                item.setOrientation(android.widget.LinearLayout.HORIZONTAL);
                item.setGravity(android.view.Gravity.CENTER_VERTICAL);
                item.setPadding(0, (int)(6 * density), 0, (int)(6 * density));

                CheckBox cb = new CheckBox(this);
                cb.setChecked(currentPresent.contains(s.getId()));
                cb.setOnCheckedChangeListener((btn, isChecked) -> updateBadge.run());
                checkBoxMap.put(s.getId(), cb);
                item.addView(cb);

                android.widget.LinearLayout info = new android.widget.LinearLayout(this);
                info.setOrientation(android.widget.LinearLayout.VERTICAL);
                android.widget.LinearLayout.LayoutParams infoLp = new android.widget.LinearLayout.LayoutParams(0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
                info.setLayoutParams(infoLp);

                TextView tvName = new TextView(this);
                tvName.setText(s.getFullName() + " (" + s.getId() + ")");
                tvName.setTextColor(getColor(R.color.charcoal_dark));
                tvName.setTextSize(12.5f);
                tvName.setTypeface(null, android.graphics.Typeface.BOLD);
                info.addView(tvName);

                TextView tvSub = new TextView(this);
                tvSub.setText(s.getYearOrClass() + " • " + s.getDepartment() + " • Current: " + String.format(java.util.Locale.US, "%.1f%%", s.getAttendancePercentage()));
                tvSub.setTextColor(getColor(R.color.text_muted));
                tvSub.setTextSize(10.5f);
                info.addView(tvSub);

                item.addView(info);
                containerRoster.addView(item);
            }
        }

        updateBadge.run();

        if (btnCancel != null) btnCancel.setOnClickListener(v -> dialog.dismiss());

        if (btnSave != null) {
            btnSave.setOnClickListener(v -> {
                List<String> selectedIds = new ArrayList<>();
                for (Map.Entry<String, CheckBox> entry : checkBoxMap.entrySet()) {
                    if (entry.getValue().isChecked()) {
                        selectedIds.add(entry.getKey());
                    }
                }

                eventManager.recordGroundAttendance(session.getId(), selectedIds);

                // --- ATTENDANCE REDIRECTION TO MISSED SUBJECT TEACHERS ---
                String dayOfWeek = getDayOfWeekFromDate(session.getPracticeDate());
                int sessionStartMin = parseTimeToMinutes(session.getStartTime());
                int sessionEndMin = parseTimeToMinutes(session.getEndTime());
                if (sessionEndMin <= sessionStartMin) sessionEndMin = sessionStartMin + 150; // default 2.5h

                List<String> redirectionSummaries = new ArrayList<>();
                int totalRedirections = 0;

                for (String studentId : selectedIds) {
                    User student = userManager.getUserById(studentId);
                    if (student == null) student = userManager.findUserByEmailOrId(studentId);
                    if (student == null) continue;

                    String stream = student.getStream();
                    if (stream.isEmpty()) stream = "Science";

                    List<TimetableSlot> daySlots = timetableManager.getTimetableForStreamAndDay(stream, dayOfWeek);
                    List<TimetableSlot> clashingSlots = new ArrayList<>();

                    for (TimetableSlot slot : daySlots) {
                        int slotStartMin = parseTimeToMinutes(slot.getStartTime());
                        int slotEndMin = parseTimeToMinutes(slot.getEndTime());
                        if (slotStartMin >= 0 && slotEndMin >= 0) {
                            if (sessionStartMin < slotEndMin && sessionEndMin > slotStartMin) {
                                clashingSlots.add(slot);
                            }
                        }
                    }

                    if (clashingSlots.isEmpty() && !daySlots.isEmpty()) {
                        clashingSlots.add(daySlots.get(0));
                    }

                    for (TimetableSlot slot : clashingSlots) {
                        totalRedirections++;
                        String noticeId = "RED-ATT-" + (System.currentTimeMillis() % 1000000) + "-" + slot.getId();
                        String noticeTitle = "📢 Duty Leave Redirection: " + student.getFullName() + " (" + slot.getSubject() + ")";
                        String noticeContent = "OFFICIAL ACADEMIC ATTENDANCE REDIRECTION & DUTY LEAVE:\n\n"
                                + "• Student: " + student.getFullName() + " (Roll No: " + student.getId() + " • " + stream + " Stream)\n"
                                + "• Event: " + session.getEventName() + "\n"
                                + "• Practice Date: " + session.getPracticeDate() + " (" + session.getTimeSlot() + ")\n"
                                + "• Ground Venue: " + session.getVenue() + "\n"
                                + "• Missed Classroom Lecture: " + slot.getLectureTitle() + " (" + slot.getTimeDisplay() + ", Room: " + slot.getRoom() + ")\n"
                                + "• Subject Teacher: " + slot.getTeacherName() + " (" + slot.getTeacherEmail() + ")\n"
                                + "• Verifying Event Incharge: " + session.getCoachName() + "\n\n"
                                + "Directive from Principal's Office: The student was physically verified on the practice field by the appointed Event Incharge. Academic attendance for this lecture slot is officially REDIRECTED & CREDITED as institutional Duty Leave (OD).";

                        Notice redirNotice = new Notice(
                                noticeId,
                                noticeTitle,
                                "Attendance Redirection",
                                session.getCoachName() + " (Event Incharge)",
                                "Faculty: " + slot.getTeacherName() + " • " + stream + " Stream",
                                noticeContent,
                                session.getPracticeDate(),
                                System.currentTimeMillis(),
                                slot.getTimeDisplay()
                        );
                        noticeManager.addNotice(redirNotice);

                        String summaryItem = "• " + student.getFullName() + " ➔ " + slot.getSubject() + " (" + slot.getTeacherName() + " • " + slot.getTimeDisplay() + ")";
                        if (!redirectionSummaries.contains(summaryItem)) {
                            redirectionSummaries.add(summaryItem);
                        }
                    }

                    // Credit co-curricular Duty Leave
                    int credits = Math.max(1, clashingSlots.size());
                    student.setDutyLeaveApprovedCount(student.getDutyLeaveApprovedCount() + credits);
                    student.calculateAttendance();
                    userManager.updateUser(student);
                }

                dialog.dismiss();
                filterAndRenderStudents();
                refreshNoticeCard();

                // Show Redirection Confirmation Dialog
                showAttendanceRedirectionSummaryDialog(session, selectedIds.size(), totalRedirections, redirectionSummaries);
            });
        }

        dialog.show();
    }

    /**
     * Dialog for Teachers and Principal to enroll a student.
     * The enrolled student immediately gets access to the app for the student interface.
     */
    private void showAddStudentDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_student, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        EditText etName = dialogView.findViewById(R.id.etAddStudentName);
        EditText etRoll = dialogView.findViewById(R.id.etAddStudentRollNo);
        Spinner spClass = dialogView.findViewById(R.id.spinnerAddStudentClass);
        Spinner spStream = dialogView.findViewById(R.id.spinnerAddStudentStream);
        EditText etSubjects = dialogView.findViewById(R.id.etAddStudentSubjects);
        EditText etEmail = dialogView.findViewById(R.id.etAddStudentEmail);
        EditText etPassword = dialogView.findViewById(R.id.etAddStudentPassword);
        TextView btnAutoEmail = dialogView.findViewById(R.id.btnAutoGenerateEmail);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btnCancelAddStudent);
        MaterialButton btnSubmit = dialogView.findViewById(R.id.btnSubmitAddStudent);

        // Spinners setup
        String[] classOptions = new String[]{"First Year (FY)", "Second Year (SY)", "Third Year (TY)"};
        ArrayAdapter<String> classAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, classOptions);
        if (spClass != null) spClass.setAdapter(classAdapter);

        String[] streamOptions;
        if (currentUser != null && !currentUser.isPrincipal() && (currentUser.isTeacherOrDirector() || currentUser.isHOD())) {
            String myStream = currentUser.getStream();
            if ("Commerce".equalsIgnoreCase(myStream)) {
                streamOptions = new String[]{"Commerce Stream (B.Com)"};
            } else if ("Arts".equalsIgnoreCase(myStream)) {
                streamOptions = new String[]{"Arts Stream (B.A.)"};
            } else {
                streamOptions = new String[]{"Science Stream (B.Sc.)"};
            }
        } else {
            streamOptions = new String[]{"Science Stream (B.Sc.)", "Commerce Stream (B.Com)", "Arts Stream (B.A.)"};
        }
        ArrayAdapter<String> streamAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, streamOptions);
        if (spStream != null) spStream.setAdapter(streamAdapter);


        // Auto email generation action
        if (btnAutoEmail != null) {
            btnAutoEmail.setOnClickListener(v -> {
                String name = etName != null ? etName.getText().toString().trim() : "";
                String roll = etRoll != null ? etRoll.getText().toString().trim() : "";
                if (name.isEmpty()) {
                    Toast.makeText(this, "Please enter student's full name first", Toast.LENGTH_SHORT).show();
                    return;
                }
                String genEmail = userManager.generateInstitutionalEmail(name, roll);
                if (etEmail != null) etEmail.setText(genEmail);
            });
        }

        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> dialog.dismiss());
        }

        if (btnSubmit != null) {
            btnSubmit.setOnClickListener(v -> {
                String name = etName != null ? etName.getText().toString().trim() : "";
                if (name.isEmpty()) {
                    if (etName != null) etName.setError("Student name is required");
                    return;
                }

                String roll = etRoll != null ? etRoll.getText().toString().trim() : "";
                if (roll.isEmpty()) {
                    roll = "STU-" + (int)(1000 + Math.random() * 9000);
                }

                String selectedClass = spClass != null ? spClass.getSelectedItem().toString() : "11th Standard";
                String selectedStream = spStream != null ? spStream.getSelectedItem().toString() : "Science Stream";
                String subjects = etSubjects != null ? etSubjects.getText().toString().trim() : "General Academics";
                if (subjects.isEmpty()) subjects = "Physics, Chemistry, Mathematics, English";

                String email = etEmail != null ? etEmail.getText().toString().trim() : "";
                if (email.isEmpty()) {
                    email = userManager.generateInstitutionalEmail(name, roll);
                }
                if (!email.endsWith("@ycasc")) {
                    email = email.split("@")[0] + "@ycasc";
                }

                String phone = UserManager.normalizePhone("");
                String password = etPassword != null ? etPassword.getText().toString().trim() : "";
                if (password.isEmpty() || password.equals("123456789")) {
                    password = phone;
                }

                User newStudent = userManager.enrollStudent(name, roll, selectedClass, selectedStream, subjects, email, password, phone);
                if (newStudent == null) {
                    Toast.makeText(this, "An account with this email or roll already exists!", Toast.LENGTH_LONG).show();
                    return;
                }

                dialog.dismiss();

                final String finalEmail = email;
                final String finalPass = password;

                new AlertDialog.Builder(this)
                        .setTitle("🎓 Student Successfully Enrolled!")
                        .setMessage("Student details have been added to the institutional database.\n\n"
                                + "👤 Name: " + name + "\n"
                                + "🆔 Roll No: " + roll + "\n"
                                + "🏫 Class: " + selectedClass + " (" + selectedStream + ")\n"
                                + "📚 Subjects: " + subjects + "\n\n"
                                + "Official Login Credentials:\n"
                                + "📧 Email ID: " + finalEmail + "\n"
                                + "🔑 Default Password: " + finalPass + "\n\n"
                                + "The student now has immediate access to log into the Eventra Student Interface. They can update their password under their profile after login.")
                        .setPositiveButton("OK", null)
                        .setNeutralButton("Copy Credentials", (d, w) -> {
                            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                            android.content.ClipData clip = android.content.ClipData.newPlainText("Student Credentials",
                                    "Eventra Student Login:\nEmail: " + finalEmail + "\nPassword: " + finalPass);
                            if (clipboard != null) clipboard.setPrimaryClip(clip);
                            Toast.makeText(this, "Login credentials copied to clipboard!", Toast.LENGTH_SHORT).show();
                        })
                        .show();
            });
        }

        dialog.show();
    }

    /**
     * Dialog for Principal to provision faculty members / teachers.
     * Callable by Principal.
     */
    private void showAddTeacherDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_teacher, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        EditText etName = dialogView.findViewById(R.id.etAddTeacherName);
        EditText etId = dialogView.findViewById(R.id.etAddTeacherId);
        EditText etDesig = dialogView.findViewById(R.id.etAddTeacherDesignation);
        Spinner spDept = dialogView.findViewById(R.id.spinnerAddTeacherDept);
        EditText etSubjects = dialogView.findViewById(R.id.etAddTeacherSubjects);
        EditText etEmail = dialogView.findViewById(R.id.etAddTeacherEmail);
        EditText etPassword = dialogView.findViewById(R.id.etAddTeacherPassword);
        TextView btnAutoEmail = dialogView.findViewById(R.id.btnAutoGenerateTeacherEmail);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btnCancelAddTeacher);
        MaterialButton btnSubmit = dialogView.findViewById(R.id.btnSubmitAddTeacher);

        // Spinners setup
        String[] deptOptions = new String[]{"Science Stream", "Commerce Stream", "Arts Stream", "Computer Science", "Mathematics", "Physical Education"};
        ArrayAdapter<String> deptAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, deptOptions);
        if (spDept != null) spDept.setAdapter(deptAdapter);

        // Auto email generation action
        if (btnAutoEmail != null) {
            btnAutoEmail.setOnClickListener(v -> {
                String name = etName != null ? etName.getText().toString().trim() : "";
                String staffId = etId != null ? etId.getText().toString().trim() : "";
                if (name.isEmpty()) {
                    Toast.makeText(this, "Please enter faculty full name first", Toast.LENGTH_SHORT).show();
                    return;
                }
                String genEmail = userManager.generateInstitutionalEmail(name, staffId);
                if (etEmail != null) etEmail.setText(genEmail);
            });
        }

        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> dialog.dismiss());
        }

        if (btnSubmit != null) {
            btnSubmit.setOnClickListener(v -> {
                String name = etName != null ? etName.getText().toString().trim() : "";
                if (name.isEmpty()) {
                    if (etName != null) etName.setError("Faculty name is required");
                    return;
                }

                String staffId = etId != null ? etId.getText().toString().trim() : "";
                String selectedDept = spDept != null ? spDept.getSelectedItem().toString() : "Science Stream";
                if (staffId.isEmpty()) {
                    staffId = userManager.generateTeacherId(selectedDept, name);
                }

                String desig = etDesig != null ? etDesig.getText().toString().trim() : "Class Teacher & Lecturer";
                if (desig.isEmpty()) desig = "Class Teacher & Lecturer";

                String subjects = etSubjects != null ? etSubjects.getText().toString().trim() : "Physics";
                if (subjects.isEmpty()) subjects = "General";

                String email = etEmail != null ? etEmail.getText().toString().trim() : "";
                if (email.isEmpty()) {
                    email = userManager.generateInstitutionalEmail(name, staffId);
                }
                if (!email.endsWith("@ycasc")) {
                    email = email.split("@")[0] + "@ycasc";
                }

                String phone = UserManager.normalizePhone("");
                String password = etPassword != null ? etPassword.getText().toString().trim() : "";
                if (password.isEmpty() || password.equals("123456789")) {
                    password = phone;
                }

                User newTeacher = userManager.provisionTeacher(name, staffId, desig, selectedDept, subjects, email, password, phone);
                if (newTeacher == null) {
                    Toast.makeText(this, "An account with this email already exists!", Toast.LENGTH_LONG).show();
                    return;
                }

                dialog.dismiss();

                final String finalEmail = email;
                final String finalPass = password;

                new AlertDialog.Builder(this)
                        .setTitle("👩‍🏫 Faculty Member Provisioned!")
                        .setMessage("Faculty account has been registered in the database.\n\n"
                                + "👤 Name: " + name + "\n"
                                + "🆔 Staff ID: " + staffId + "\n"
                                + "🏢 Department: " + selectedDept + "\n"
                                + "💼 Designation: " + desig + "\n"
                                + "📚 Subjects Taught: " + subjects + "\n\n"
                                + "Official Login Credentials:\n"
                                + "📧 Email ID: " + finalEmail + "\n"
                                + "🔑 Default Password: " + finalPass + "\n\n"
                                + "This teacher can now log in to review and approve Spot Passes, manage attendance, and enroll students.")
                        .setPositiveButton("OK", null)
                        .setNeutralButton("Copy Credentials", (d, w) -> {
                            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                            android.content.ClipData clip = android.content.ClipData.newPlainText("Faculty Credentials",
                                    "Eventra Faculty Login:\nEmail: " + finalEmail + "\nPassword: " + finalPass);
                            if (clipboard != null) clipboard.setPrimaryClip(clip);
                            Toast.makeText(this, "Faculty login credentials copied to clipboard!", Toast.LENGTH_SHORT).show();
                        })
                        .show();
            });
        }

        dialog.show();
    }

    /**
     * Principal Admin Quick Action Menu
     */
    private void showPrincipalActionMenu() {
        String[] options = new String[]{
                "➕ Provision New Faculty / Teacher",
                "🎓 Enroll New Student",
                "👥 Institutional Directory & Roster",
                "🔄 Reset Database to Principal-Only"
        };

        new AlertDialog.Builder(this)
                .setTitle("🏛️ Principal Administration")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            showAddTeacherDialog();
                            break;
                        case 1:
                            showAddStudentDialog();
                            break;
                        case 2:
                            showInstitutionalRosterDialog();
                            break;
                        case 3:
                            new AlertDialog.Builder(this)
                                    .setTitle("Reset to Principal-Only?")
                                    .setMessage("This will remove all added teachers and students, restoring the database to contain only Dr. Shashikant V. Mayekar (Principal).")
                                    .setPositiveButton("Reset", (d, w) -> {
                                        userManager.resetToPrincipalOnly();
                                        Toast.makeText(this, "Database reset to Principal-Only.", Toast.LENGTH_SHORT).show();
                                        loadCurrentUserData();
                                    })
                                    .setNegativeButton("Cancel", null)
                                    .show();
                            break;
                    }
                })
                .setNegativeButton("Close", null)
                .show();
    }

    /**
     * Teacher / Faculty Quick Action Menu (FAB / Navigation action)
     */
    private void showTeacherActionMenu() {
        String[] options = new String[]{
                "📢 Upload Institutional Notice / Announcement",
                "🎓 Enroll New Student",
                "📝 Review Pending Spot Passes",
                "📁 Institutional Requests & Permissions"
        };

        new AlertDialog.Builder(this)
                .setTitle("👩‍🏫 Faculty Administration")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            showNewNoticeDialog();
                            break;
                        case 1:
                            showAddStudentDialog();
                            break;
                        case 2:
                            showTeacherPendingPassesDialog();
                            break;
                        case 3:
                            startActivity(new Intent(this, RequestsActivity.class));
                            break;
                    }
                })
                .setNegativeButton("Close", null)
                .show();
    }

    /**
     * Institutional Roster displaying all provisioned users (Principal, Teachers, Students)
     */
    private void showInstitutionalRosterDialog() {
        List<User> allUsers = userManager.getAllUsers();
        if (allUsers.isEmpty()) {
            Toast.makeText(this, "No registered users found.", Toast.LENGTH_SHORT).show();
            return;
        }

        List<User> users = new ArrayList<>();
        if (currentUser != null && !currentUser.isPrincipal() && (currentUser.isTeacherOrDirector() || currentUser.isHOD())) {
            String myStream = currentUser.getStream();
            for (User u : allUsers) {
                if (!u.isStudent() || u.getStream().equalsIgnoreCase(myStream)) {
                    users.add(u);
                }
            }
        } else {
            users = allUsers;
        }

        StringBuilder sb = new StringBuilder();
        int teachersCount = 0;
        int studentsCount = 0;

        for (User u : users) {
            String roleTag;
            if (u.isPrincipal()) {
                roleTag = "🏛️ [PRINCIPAL]";
            } else if (u.isTeacherOrDirector()) {
                roleTag = "👩‍🏫 [FACULTY]";
                teachersCount++;
            } else {
                roleTag = "🎓 [STUDENT]";
                studentsCount++;
            }

            sb.append(roleTag).append(" ").append(u.getFullName()).append("\n");
            sb.append("   ID: ").append(u.getId()).append(" • Email: ").append(u.getEmail()).append("\n");
            if (u.getSubjects() != null && !u.getSubjects().isEmpty()) {
                sb.append("   Subjects: ").append(u.getSubjects()).append("\n");
            }
            sb.append("\n");
        }

        new AlertDialog.Builder(this)
                .setTitle("📋 Institutional Roster (" + (teachersCount + studentsCount + 1) + " Total)")
                .setMessage("Faculty: " + teachersCount + " | Students: " + studentsCount + " | Principal: 1\n\n" + sb.toString().trim())
                .setPositiveButton("Close", null)
                .show();
    }

    /**
     * Lists Spot Passes for teachers to review and approve/reject.
     * Filtered so teachers only see passes for students in their own academic stream.
     */
    private void showTeacherPendingPassesDialog() {
        List<SpotPass> allPasses = spotPassManager.getAllSpotPasses();
        final List<SpotPass> passes;

        if (currentUser != null && !currentUser.isPrincipal() && (currentUser.isTeacherOrDirector() || currentUser.isHOD())) {
            String myStream = currentUser.getStream();
            List<SpotPass> streamOnly = new ArrayList<>();
            for (SpotPass p : allPasses) {
                User s = userManager.getUserById(p.getStudentId());
                if (s == null) s = userManager.findUserByEmailOrId(p.getStudentId());
                if (s != null && s.getStream().equalsIgnoreCase(myStream)) {
                    streamOnly.add(p);
                } else if (s == null) {
                    String id = p.getStudentId().toUpperCase();
                    if ((myStream.equalsIgnoreCase("Science") && (id.startsWith("SCI") || id.contains("-SCI-"))) ||
                        (myStream.equalsIgnoreCase("Commerce") && (id.startsWith("COM") || id.contains("-COM-"))) ||
                        (myStream.equalsIgnoreCase("Arts") && (id.startsWith("ART") || id.contains("-ART-")))) {
                        streamOnly.add(p);
                    }
                }
            }
            passes = streamOnly;
        } else {
            passes = allPasses;
        }

        if (passes.isEmpty()) {
            Toast.makeText(this, "No Spot Passes currently pending review for your stream.", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] passItems = new String[passes.size()];
        for (int i = 0; i < passes.size(); i++) {
            SpotPass p = passes.get(i);
            passItems[i] = p.getId() + " · " + p.getStudentName() + " (" + p.getActivityTitle() + ") [" + p.getStatus() + "]";
        }

        new AlertDialog.Builder(this)
                .setTitle("📝 Duty Leave & Spot Passes")
                .setItems(passItems, (dialog, which) -> {
                    SpotPass selectedPass = passes.get(which);
                    showSpotPassDetailsDialog(selectedPass);
                })
                .setNegativeButton("Close", null)
                .show();
    }

    /** Placeholder click handler: shows a toast. Replace with startActivity(...) or fragment navigation. */
    private void click(int viewId, String label) {
        View v = findViewById(viewId);
        if (v != null) {
            v.setOnClickListener(view ->
                    Toast.makeText(this, label, Toast.LENGTH_SHORT).show());
        }
    }

    /**
     * Dialog for the Principal to appoint a Teacher or Student as Event Incharge.
     * Generates a dedicated account solely created for the event, schedules multi-day practice sessions,
     * and sets up attendance redirection to missed subject lectures.
     */
    private void showAppointEventInchargeDialog() {
        if (currentUser == null || !currentUser.isPrincipal()) {
            Toast.makeText(this, "⚠️ Principal administrative privilege required.", Toast.LENGTH_SHORT).show();
            return;
        }

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_appoint_event_incharge, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        // Section 1: Event
        Spinner spinnerEvent = dialogView.findViewById(R.id.spinnerInchargeEvent);
        View layoutNewEvent = dialogView.findViewById(R.id.layoutInchargeNewEvent);
        EditText etNewEventName = dialogView.findViewById(R.id.etInchargeNewEventName);
        EditText etNewEventVenue = dialogView.findViewById(R.id.etInchargeNewEventVenue);
        Spinner spNewEventType = dialogView.findViewById(R.id.spinnerInchargeNewEventType);
        Spinner spNewEventLevel = dialogView.findViewById(R.id.spinnerInchargeNewEventLevel);

        // Section 2: Incharge Category & Person
        RadioGroup rgCategory = dialogView.findViewById(R.id.rgInchargeCategory);
        RadioButton rbTeacher = dialogView.findViewById(R.id.rbCategoryTeacher);
        RadioButton rbStudent = dialogView.findViewById(R.id.rbCategoryStudent);
        Spinner spinnerPerson = dialogView.findViewById(R.id.spinnerInchargePerson);
        TextView tvPersonDetails = dialogView.findViewById(R.id.tvInchargePersonDetails);

        // Section 3: Dedicated Account
        EditText etEmail = dialogView.findViewById(R.id.etInchargeLoginEmail);
        EditText etPassword = dialogView.findViewById(R.id.etInchargeLoginPassword);

        // Section 4: Multi-Day Schedule
        EditText etStartDate = dialogView.findViewById(R.id.etPracticeStartDate);
        Spinner spDuration = dialogView.findViewById(R.id.spinnerPracticeDuration);
        Spinner spSlot = dialogView.findViewById(R.id.spinnerPracticeSlot);
        EditText etVenue = dialogView.findViewById(R.id.etPracticeGroundVenue);

        // Section 5: Participating Students
        Spinner spStreamFilter = dialogView.findViewById(R.id.spinnerStudentStreamFilter);
        TextView tvSelectAll = dialogView.findViewById(R.id.tvSelectAllStudentsToggle);
        TextView tvSquadBadge = dialogView.findViewById(R.id.tvSquadCountBadge);
        android.widget.LinearLayout containerStudents = dialogView.findViewById(R.id.containerInchargeStudents);

        MaterialButton btnCancel = dialogView.findViewById(R.id.btnCancelAppointIncharge);
        MaterialButton btnSubmit = dialogView.findViewById(R.id.btnSubmitAppointIncharge);

        // Setup Events Spinner
        List<Event> existingEvents = eventManager.getAllEvents();
        List<String> eventNames = new ArrayList<>();
        for (Event e : existingEvents) {
            eventNames.add(e.getName() + " (" + e.getDatesDisplay() + ")");
        }
        eventNames.add("➕ [+ Create New Co-Curricular Event]");
        ArrayAdapter<String> eventAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, eventNames);
        spinnerEvent.setAdapter(eventAdapter);

        // Setup Event Type and Level Spinners
        String[] types = new String[]{"Sports", "Cultural", "Academic", "Technical"};
        String[] levels = new String[]{"Inter-Collegiate", "State Level", "District Level", "University Level"};
        spNewEventType.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, types));
        spNewEventLevel.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, levels));

        // Setup Practice Duration Spinner
        String[] durations = new String[]{
                "3 Days Practice Camp",
                "5 Days Intensive Camp (Tournament Prep)",
                "7 Days State Championship Camp",
                "10 Days Comprehensive Training"
        };
        spDuration.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, durations));
        spDuration.setSelection(1); // default 5 days

        // Setup Practice Slot Spinner
        String[] practiceSlots = new String[]{
                "09:00 AM – 11:30 AM (Morning Session • Clashes with Slot 2 & 3)",
                "08:00 AM – 10:00 AM (Early Session • Clashes with Slot 1 & 2)",
                "10:00 AM – 01:00 PM (Midday Session • Clashes with Slot 3, 4 & 5)",
                "11:30 AM – 01:30 PM (Afternoon Session • Clashes with Slot 4 & 5)"
        };
        spSlot.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, practiceSlots));

        // Pre-fill Start Date with today/tomorrow
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.US);
        etStartDate.setText(sdf.format(new java.util.Date()));

        // Incharge Person Lists
        List<User> facultyList = userManager.getFacultyAndStaff();
        List<User> studentList = userManager.getStudents();
        List<User> currentPersonPool = new ArrayList<>(facultyList);

        Runnable updatePersonSpinner = () -> {
            boolean isStudentCat = rbStudent.isChecked();
            currentPersonPool.clear();
            if (isStudentCat) {
                currentPersonPool.addAll(studentList);
            } else {
                currentPersonPool.addAll(facultyList);
            }

            List<String> labels = new ArrayList<>();
            for (User u : currentPersonPool) {
                labels.add(u.getFullName() + " (" + u.getId() + " • " + u.getDepartment() + ")");
            }
            ArrayAdapter<String> pAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, labels);
            spinnerPerson.setAdapter(pAdapter);
        };

        updatePersonSpinner.run();

        rgCategory.setOnCheckedChangeListener((group, checkedId) -> updatePersonSpinner.run());

        spinnerPerson.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < currentPersonPool.size()) {
                    User sel = currentPersonPool.get(position);
                    tvPersonDetails.setText("Selected: " + sel.getFullName() + " (" + sel.getId() + " • " + sel.getDepartment() + " • 📞 " + (sel.getPhoneNumber().isEmpty() ? "9823000000" : UserManager.normalizePhone(sel.getPhoneNumber())) + ")");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Event Selection Listener to auto-fill slug & toggle new event fields
        Runnable updateEmailSlug = () -> {
            int pos = spinnerEvent.getSelectedItemPosition();
            String slug = "event";
            if (pos < existingEvents.size()) {
                Event selEv = existingEvents.get(pos);
                slug = selEv.getName().replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
            } else {
                String typed = etNewEventName.getText().toString().trim();
                if (!typed.isEmpty()) {
                    slug = typed.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
                }
            }
            if (slug.length() > 8) slug = slug.substring(0, 8);
            if (slug.isEmpty()) slug = "event";
            etEmail.setText("incharge." + slug + "@ycasc.com");
        };

        spinnerEvent.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == existingEvents.size()) {
                    layoutNewEvent.setVisibility(View.VISIBLE);
                } else {
                    layoutNewEvent.setVisibility(View.GONE);
                }
                updateEmailSlug.run();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Participating Students Squad setup
        Set<String> selectedSquadStudentIds = new HashSet<>();
        for (int i = 0; i < Math.min(6, studentList.size()); i++) {
            selectedSquadStudentIds.add(studentList.get(i).getId());
        }

        String[] streamFilterOptions = new String[]{"All Streams", "Science Stream", "Commerce Stream", "Arts Stream"};
        spStreamFilter.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, streamFilterOptions));

        Runnable renderStudentCheckboxes = () -> {
            containerStudents.removeAllViews();
            int filterPos = spStreamFilter.getSelectedItemPosition();
            String chosenFilter = filterPos == 1 ? "Science" : (filterPos == 2 ? "Commerce" : (filterPos == 3 ? "Arts" : ""));

            float density = getResources().getDisplayMetrics().density;
            for (User s : studentList) {
                if (!chosenFilter.isEmpty() && !s.getStream().equalsIgnoreCase(chosenFilter)) {
                    continue;
                }

                android.widget.LinearLayout row = new android.widget.LinearLayout(this);
                row.setOrientation(android.widget.LinearLayout.HORIZONTAL);
                row.setGravity(android.view.Gravity.CENTER_VERTICAL);
                row.setPadding(0, (int)(5 * density), 0, (int)(5 * density));

                CheckBox cb = new CheckBox(this);
                cb.setChecked(selectedSquadStudentIds.contains(s.getId()));
                cb.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        selectedSquadStudentIds.add(s.getId());
                    } else {
                        selectedSquadStudentIds.remove(s.getId());
                    }
                    tvSquadBadge.setText(selectedSquadStudentIds.size() + " student(s) selected for multi-day permission");
                });
                row.addView(cb);

                TextView tvInfo = new TextView(this);
                tvInfo.setText(s.getFullName() + " (" + s.getId() + " • " + s.getDepartment() + " • " + s.getYearOrClass() + ")");
                tvInfo.setTextColor(getColor(R.color.charcoal_dark));
                tvInfo.setTextSize(12f);
                row.addView(tvInfo);

                containerStudents.addView(row);
            }
            tvSquadBadge.setText(selectedSquadStudentIds.size() + " student(s) selected for multi-day permission");
        };

        renderStudentCheckboxes.run();
        spStreamFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> p, View v, int pos, long id) { renderStudentCheckboxes.run(); }
            @Override
            public void onNothingSelected(AdapterView<?> p) {}
        });

        tvSelectAll.setOnClickListener(v -> {
            boolean allSelected = selectedSquadStudentIds.size() == studentList.size();
            if (allSelected) {
                selectedSquadStudentIds.clear();
                tvSelectAll.setText("Select All");
            } else {
                for (User s : studentList) {
                    selectedSquadStudentIds.add(s.getId());
                }
                tvSelectAll.setText("Deselect All");
            }
            renderStudentCheckboxes.run();
        });

        if (btnCancel != null) btnCancel.setOnClickListener(v -> dialog.dismiss());

        if (btnSubmit != null) {
            btnSubmit.setOnClickListener(v -> {
                int eventPos = spinnerEvent.getSelectedItemPosition();
                boolean isNew = (eventPos == existingEvents.size());
                String eventName;
                String eventVenue;
                String eventType = spNewEventType.getSelectedItem() != null ? spNewEventType.getSelectedItem().toString() : "Sports";
                String eventLevel = spNewEventLevel.getSelectedItem() != null ? spNewEventLevel.getSelectedItem().toString() : "Inter-Collegiate";

                Event targetEvent;
                if (isNew) {
                    eventName = etNewEventName.getText().toString().trim();
                    eventVenue = etNewEventVenue.getText().toString().trim();
                    if (eventName.isEmpty()) {
                        etNewEventName.setError("Event title is required");
                        return;
                    }
                    if (eventVenue.isEmpty()) eventVenue = "Yashoda College Main Ground";
                    String evId = "EV-" + (System.currentTimeMillis() % 100000);
                    targetEvent = new Event(evId, eventName, etStartDate.getText().toString().trim(), "", eventType, eventLevel, "YCASC Co-Curricular Council", "", eventVenue, "Inter-Collegiate tournament with sanctioned multi-day ground practice");
                } else {
                    targetEvent = existingEvents.get(eventPos);
                    eventName = targetEvent.getName();
                    eventVenue = targetEvent.getVenue();
                }

                int personPos = spinnerPerson.getSelectedItemPosition();
                if (personPos < 0 || personPos >= currentPersonPool.size()) {
                    Toast.makeText(this, "Please select an incharge person.", Toast.LENGTH_SHORT).show();
                    return;
                }
                User appointedPerson = currentPersonPool.get(personPos);
                boolean isStudentIncharge = rbStudent.isChecked();

                String inchargeEmail = etEmail.getText().toString().trim();
                String inchargePassword = etPassword.getText().toString().trim();
                if (inchargeEmail.isEmpty()) {
                    etEmail.setError("Login email is required");
                    return;
                }
                if (inchargePassword.isEmpty()) inchargePassword = "event123";

                String startDateStr = etStartDate.getText().toString().trim();
                if (startDateStr.isEmpty()) startDateStr = "29 Oct 2026";

                int durPos = spDuration.getSelectedItemPosition();
                int numDays = durPos == 0 ? 3 : (durPos == 1 ? 5 : (durPos == 2 ? 7 : 10));

                String chosenSlot = spSlot.getSelectedItem() != null ? spSlot.getSelectedItem().toString() : "09:00 AM – 11:30 AM";
                String slotStart = "09:00 AM";
                String slotEnd = "11:30 AM";
                if (chosenSlot.contains("–")) {
                    String clean = chosenSlot.split("\\(")[0].trim();
                    String[] parts = clean.split("–");
                    if (parts.length == 2) {
                        slotStart = parts[0].trim();
                        slotEnd = parts[1].trim();
                    }
                }

                String groundVenue = etVenue.getText().toString().trim();
                if (groundVenue.isEmpty()) groundVenue = "Yashoda College Main Ground";

                // 1. Save or Update Event
                targetEvent.setCoordinatorName(appointedPerson.getFullName());
                if (isNew) {
                    eventManager.addEvent(targetEvent);
                } else {
                    eventManager.saveEvents(eventManager.getAllEvents());
                }

                // 2. Appoint Incharge & Provision Dedicated Account
                User inchargeAccount = userManager.appointEventIncharge(targetEvent, appointedPerson, inchargeEmail, inchargePassword, isStudentIncharge);

                // 3. Schedule Multi-Day Practice Sessions
                List<String> practiceDates = generateDateRange(startDateStr, numDays);
                for (int i = 0; i < practiceDates.size(); i++) {
                    String pDate = practiceDates.get(i);
                    String sessionId = "PRAC-" + targetEvent.getId() + "-" + (i + 1);
                    PracticeSession ps = new PracticeSession(
                            sessionId,
                            targetEvent.getId(),
                            targetEvent.getName(),
                            pDate,
                            slotStart,
                            slotEnd,
                            groundVenue,
                            "Day " + (i + 1) + " Practice Camp",
                            inchargeAccount.getFullName()
                    );
                    eventManager.addPracticeSession(ps);
                }

                // 4. Sanction Approved Permission Requests for all selected students
                String endDateStr = practiceDates.get(practiceDates.size() - 1);
                for (String sId : selectedSquadStudentIds) {
                    User st = userManager.getUserById(sId);
                    if (st == null) continue;
                    String permId = "PERM-EV-" + (System.currentTimeMillis() % 100000) + "-" + st.getId();
                    PermissionRequest req = new PermissionRequest(
                            permId,
                            st.getId(),
                            st.getFullName(),
                            st.getYearOrClass(),
                            targetEvent.getName(),
                            startDateStr + " to " + endDateStr + " (" + numDays + " Practice Days)",
                            st.getStream() + " Stream",
                            "Official tournament squad participation sanctioned under Event Incharge " + inchargeAccount.getFullName(),
                            "Approved by Principal",
                            100,
                            "Dr. Shashikant Mayekar (Principal)",
                            "Multi-day practice permission sanctioned. Attendance tracked on dedicated incharge console; missed lectures redirected as Duty Leave.",
                            System.currentTimeMillis()
                    );
                    permissionRequestManager.addPermission(req);
                }

                dialog.dismiss();
                filterAndRenderStudents();

                // Show Credentials Summary Dialog for Principal
                showInchargeCredentialsDialog(targetEvent, inchargeAccount, startDateStr, numDays, slotStart + " – " + slotEnd, selectedSquadStudentIds.size());
            });
        }

        dialog.show();
    }

    /**
     * Dialog showing the generated credentials of the dedicated Event Incharge account.
     */
    private void showInchargeCredentialsDialog(Event event, User inchargeUser, String startDate, int numDays, String timeSlot, int squadSize) {
        if (event == null || inchargeUser == null) return;

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_incharge_credentials, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        TextView tvEvent = dialogView.findViewById(R.id.tvCredEventTitle);
        TextView tvName = dialogView.findViewById(R.id.tvCredInchargeName);
        TextView tvEmail = dialogView.findViewById(R.id.tvCredEmail);
        TextView tvPassword = dialogView.findViewById(R.id.tvCredPassword);
        TextView tvMultiDay = dialogView.findViewById(R.id.tvCredMultiDayInfo);
        TextView tvSquad = dialogView.findViewById(R.id.tvCredSquadInfo);

        View btnCopyEmail = dialogView.findViewById(R.id.btnCopyEmail);
        View btnCopyPass = dialogView.findViewById(R.id.btnCopyPassword);
        MaterialButton btnCopyFull = dialogView.findViewById(R.id.btnCopyFullCredentials);
        MaterialButton btnClose = dialogView.findViewById(R.id.btnCloseCredentials);

        tvEvent.setText("🏆 " + event.getName());
        tvName.setText("Appointed Incharge: " + inchargeUser.getFullName());
        tvEmail.setText(inchargeUser.getEmail());
        tvPassword.setText(inchargeUser.getPassword());
        tvMultiDay.setText("📅 " + numDays + " Days Practice Camp: Starting " + startDate + " (" + timeSlot + ")");
        tvSquad.setText("👥 " + squadSize + " Participating Students Permitted & Squad Enrolled");

        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

        if (btnCopyEmail != null) {
            btnCopyEmail.setOnClickListener(v -> {
                if (clipboard != null) {
                    clipboard.setPrimaryClip(ClipData.newPlainText("Incharge Email", inchargeUser.getEmail()));
                    Toast.makeText(this, "📋 Email copied to clipboard!", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (btnCopyPass != null) {
            btnCopyPass.setOnClickListener(v -> {
                if (clipboard != null) {
                    clipboard.setPrimaryClip(ClipData.newPlainText("Incharge Password", inchargeUser.getPassword()));
                    Toast.makeText(this, "📋 Password copied to clipboard!", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (btnCopyFull != null) {
            btnCopyFull.setOnClickListener(v -> {
                String fullCreds = "🏆 EVENTRA - DEDICATED EVENT INCHARGE CREDENTIALS\n\n"
                        + "Event: " + event.getName() + "\n"
                        + "Appointed Incharge: " + inchargeUser.getFullName() + "\n"
                        + "Login Email: " + inchargeUser.getEmail() + "\n"
                        + "Login Password: " + inchargeUser.getPassword() + "\n"
                        + "Practice Schedule: " + numDays + " Days from " + startDate + " (" + timeSlot + ")\n"
                        + "Permitted Squad: " + squadSize + " Students\n\n"
                        + "Login using these credentials to take ground roll-call. Attendance is automatically redirected to subject teachers whose lectures students miss.";
                if (clipboard != null) {
                    clipboard.setPrimaryClip(ClipData.newPlainText("Eventra Incharge Credentials", fullCreds));
                    Toast.makeText(this, "📋 Full credentials copied to clipboard!", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (btnClose != null) {
            btnClose.setOnClickListener(v -> {
                dialog.dismiss();
                showEventsAndPracticeDialog();
            });
        }

        dialog.show();
    }

    /**
     * Dialog showing summary of ground attendance recorded and the automatic redirection
     * of duty leaves to the subject teachers whose lectures were missed.
     */
    private void showAttendanceRedirectionSummaryDialog(PracticeSession session, int presentCount, int redirectionCount, List<String> summaries) {
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);

        TextView tvHeader = new TextView(this);
        tvHeader.setText("⚽ Ground Roll-Call & Duty Redirection Complete");
        tvHeader.setTextColor(getColor(R.color.charcoal_dark));
        tvHeader.setTextSize(15f);
        tvHeader.setTypeface(null, android.graphics.Typeface.BOLD);
        layout.addView(tvHeader);

        TextView tvSub = new TextView(this);
        tvSub.setText("Event: " + session.getEventName() + "\nSession: " + session.getPracticeDate() + " (" + session.getTimeSlot() + ") • " + session.getVenue() + "\nVerified Present: " + presentCount + " student(s)\nRedirected Missed Lectures: " + redirectionCount + " slot(s)");
        tvSub.setTextColor(getColor(R.color.text_secondary));
        tvSub.setTextSize(12f);
        tvSub.setPadding(0, 10, 0, 16);
        layout.addView(tvSub);

        if (!summaries.isEmpty()) {
            TextView tvListTitle = new TextView(this);
            tvListTitle.setText("Redirected Subject Lectures (Notified Teachers):");
            tvListTitle.setTextColor(getColor(R.color.charcoal_dark));
            tvListTitle.setTextSize(12.5f);
            tvListTitle.setTypeface(null, android.graphics.Typeface.BOLD);
            layout.addView(tvListTitle);

            for (String s : summaries) {
                TextView item = new TextView(this);
                item.setText(s);
                item.setTextColor(getColor(R.color.pastel_lavender_text));
                item.setTextSize(11.5f);
                item.setPadding(0, 4, 0, 0);
                layout.addView(item);
            }
        }

        TextView tvNote = new TextView(this);
        tvNote.setText("\n✅ Official Duty Leave notifications have been routed to each subject teacher's dashboard, crediting academic attendance for the missed lecture slots.");
        tvNote.setTextColor(getColor(R.color.text_muted));
        tvNote.setTextSize(11f);
        layout.addView(tvNote);

        new AlertDialog.Builder(this)
                .setView(layout)
                .setPositiveButton("Done", (d, w) -> showEventsAndPracticeDialog())
                .show();
    }

    public static int parseTimeToMinutes(String timeStr) {
        if (timeStr == null) return -1;
        timeStr = timeStr.trim().toUpperCase();
        try {
            boolean isPm = timeStr.contains("PM");
            boolean isAm = timeStr.contains("AM");
            String clean = timeStr.replace("AM", "").replace("PM", "").trim();
            String[] parts = clean.split(":");
            int hour = Integer.parseInt(parts[0].trim());
            int minute = parts.length > 1 ? Integer.parseInt(parts[1].trim()) : 0;
            if (isPm && hour < 12) hour += 12;
            if (isAm && hour == 12) hour = 0;
            return hour * 60 + minute;
        } catch (Exception e) {
            return -1;
        }
    }

    public static String getDayOfWeekFromDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) return "Monday";
        String[] formats = new String[]{"dd MMM yyyy", "d MMM yyyy", "yyyy-MM-dd", "dd/MM/yyyy", "d/M/yyyy"};
        for (String fmt : formats) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(fmt, Locale.US);
                sdf.setLenient(false);
                java.util.Date d = sdf.parse(dateStr.trim());
                if (d != null) {
                    SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.US);
                    return dayFormat.format(d);
                }
            } catch (Exception ignored) {}
        }
        String lower = dateStr.toLowerCase();
        for (String day : TimetableManager.DAYS_OF_WEEK) {
            if (lower.contains(day.toLowerCase())) return day;
        }
        return "Thursday";
    }

    public static List<String> generateDateRange(String startDateStr, int numDays) {
        List<String> dates = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.US);
        Calendar cal = Calendar.getInstance();
        try {
            cal.setTime(sdf.parse(startDateStr.trim()));
        } catch (Exception e) {
            cal.setTime(new java.util.Date());
        }
        for (int i = 0; i < numDays; i++) {
            dates.add(sdf.format(cal.getTime()));
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }
        return dates;
    }
}

