package com.example.sams;

import android.app.DatePickerDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {

    private UserManager userManager;
    private User currentUser;

    // Header Views
    private TextView tvProfileAvatar;
    private TextView tvProfileFullName;
    private TextView tvProfileRoleBadge;
    private TextView tvProfileInstitutionBadge;

    // Credentials
    private TextView tvProfileEmail;
    private TextView tvProfileRollNo;
    private TextView tvProfileAddress;
    private View btnCopyEmail;

    // Academic Details
    private TextView tvProfileYearLabel;
    private TextView tvProfileYearOrClass;
    private TextView tvProfileDegreeLabel;
    private TextView tvProfileDegree;
    private TextView tvProfileDepartment;

    // Subjects
    private View cardEnrolledSubjects;
    private TextView tvSubjectsCountBadge;
    private LinearLayout containerSubjectsList;
    private TextView tvNoSubjectsNotice;

    // Principal Minimal Roster - Two Separate Distinct Rows
    private View layoutPrincipalRosterSection;
    private TextView tvFacultyCountBadge;
    private LinearLayout containerPrincipalFacultyMinimal;
    private TextView tvStudentsDirectoryCountBadge;
    private LinearLayout containerPrincipalStudentsMinimal;

    private View rowHeaderPrincipalTeachers;
    private View layoutTeachersDataContent;
    private TextView tvTeachersChevron;
    private boolean isTeachersExpanded = true;

    private View rowHeaderPrincipalStudents;
    private View layoutStudentsDataContent;
    private TextView tvStudentsChevron;
    private boolean isStudentsExpanded = true;

    // Stream filters for students row
    private TextView chipFilterAll;
    private TextView chipFilterScience;
    private TextView chipFilterCommerce;
    private TextView chipFilterArts;
    private String selectedStreamFilter = "ALL";

    // Personal Details
    private TextView tvProfilePhone;
    private TextView tvProfileDob;

    // Action Buttons
    private View btnBack;
    private View btnTopEdit;
    private MaterialButton btnEditProfile;
    private MaterialButton btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Allow phone navigation buttons (Back, Home, Recents) to always stay visible
        WindowCompat.setDecorFitsSystemWindows(getWindow(), true);
        WindowInsetsControllerCompat insetsController = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (insetsController != null) {
            insetsController.show(WindowInsetsCompat.Type.systemBars());
        }

        setContentView(R.layout.activity_profile);

        userManager = UserManager.getInstance(this);
        currentUser = userManager.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "Session expired. Please log in.", Toast.LENGTH_SHORT).show();
            navigateToLogin();
            return;
        }

        initViews();
        bindUserData(currentUser);
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnTopEdit = findViewById(R.id.btnTopEdit);

        tvProfileAvatar = findViewById(R.id.tvProfileAvatar);
        tvProfileFullName = findViewById(R.id.tvProfileFullName);
        tvProfileRoleBadge = findViewById(R.id.tvProfileRoleBadge);
        tvProfileInstitutionBadge = findViewById(R.id.tvProfileInstitutionBadge);

        tvProfileEmail = findViewById(R.id.tvProfileEmail);
        tvProfileRollNo = findViewById(R.id.tvProfileRollNo);
        tvProfileAddress = findViewById(R.id.tvProfileAddress);
        btnCopyEmail = findViewById(R.id.btnCopyEmail);

        tvProfileYearLabel = findViewById(R.id.tvProfileYearLabel);
        tvProfileYearOrClass = findViewById(R.id.tvProfileYearOrClass);
        tvProfileDegreeLabel = findViewById(R.id.tvProfileDegreeLabel);
        tvProfileDegree = findViewById(R.id.tvProfileDegree);
        tvProfileDepartment = findViewById(R.id.tvProfileDepartment);

        cardEnrolledSubjects = findViewById(R.id.cardEnrolledSubjects);
        tvSubjectsCountBadge = findViewById(R.id.tvSubjectsCountBadge);
        containerSubjectsList = findViewById(R.id.containerSubjectsList);
        tvNoSubjectsNotice = findViewById(R.id.tvNoSubjectsNotice);

        layoutPrincipalRosterSection = findViewById(R.id.layoutPrincipalRosterSection);
        tvFacultyCountBadge = findViewById(R.id.tvFacultyCountBadge);
        containerPrincipalFacultyMinimal = findViewById(R.id.containerPrincipalFacultyMinimal);
        tvStudentsDirectoryCountBadge = findViewById(R.id.tvStudentsDirectoryCountBadge);
        containerPrincipalStudentsMinimal = findViewById(R.id.containerPrincipalStudentsMinimal);

        rowHeaderPrincipalTeachers = findViewById(R.id.rowHeaderPrincipalTeachers);
        layoutTeachersDataContent = findViewById(R.id.layoutTeachersDataContent);
        tvTeachersChevron = findViewById(R.id.tvTeachersChevron);

        rowHeaderPrincipalStudents = findViewById(R.id.rowHeaderPrincipalStudents);
        layoutStudentsDataContent = findViewById(R.id.layoutStudentsDataContent);
        tvStudentsChevron = findViewById(R.id.tvStudentsChevron);

        chipFilterAll = findViewById(R.id.chipFilterAll);
        chipFilterScience = findViewById(R.id.chipFilterScience);
        chipFilterCommerce = findViewById(R.id.chipFilterCommerce);
        chipFilterArts = findViewById(R.id.chipFilterArts);

        tvProfilePhone = findViewById(R.id.tvProfilePhone);
        tvProfileDob = findViewById(R.id.tvProfileDob);

        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnLogout = findViewById(R.id.btnLogout);
    }

    private void bindUserData(User user) {
        if (user == null) return;

        // Avatar & Name
        tvProfileAvatar.setText(user.getInitials());
        tvProfileFullName.setText(user.getFullName() != null && !user.getFullName().isEmpty() ? 
                user.getFullName() : "Eventra Member");

        // Role Badge Styling
        tvProfileRoleBadge.setText(user.getRoleDisplayName());
        if (user.isOrganizer()) {
            tvProfileRoleBadge.setBackgroundResource(R.drawable.bg_chip_butter);
            tvProfileRoleBadge.setTextColor(getColor(R.color.pastel_butter_text));
        } else if (user.isStudent()) {
            tvProfileRoleBadge.setBackgroundResource(R.drawable.bg_chip_mint);
            tvProfileRoleBadge.setTextColor(getColor(R.color.pastel_mint_text));
        } else {
            tvProfileRoleBadge.setBackgroundResource(R.drawable.bg_chip_lavender);
            tvProfileRoleBadge.setTextColor(getColor(R.color.pastel_lavender_text));
        }

        // Institution Badge
        String instType = user.getInstitutionType() != null ? user.getInstitutionType() : User.INSTITUTION_COLLEGE;
        if (User.INSTITUTION_COLLEGE.equalsIgnoreCase(instType) || user.isSchoolOrHigherSec()) {
            tvProfileInstitutionBadge.setText("🏛️ College");
        } else {
            tvProfileInstitutionBadge.setText("🎓 University");
        }

        // Email & Roll No
        tvProfileEmail.setText(user.getEmail() != null ? user.getEmail() : "user@ycasc.com");
        tvProfileRollNo.setText(user.getId() != null ? user.getId() : "N/A");
        if (tvProfileAddress != null) {
            tvProfileAddress.setText(user.getAddress() != null && !user.getAddress().isEmpty() ? 
                    user.getAddress() : "Address not provided (Tap Edit to update)");
        }

        // Buyer-Adaptive Academic details (College: Degree, Year, Department)
        tvProfileYearLabel.setText("Academic Year / Class");
        String yr = user.getYearOrClass();
        tvProfileYearOrClass.setText(!yr.isEmpty() ? yr : "First Year (Batch 2026)");

        tvProfileDegreeLabel.setText("Degree / Stream");
        String deg = user.getDegree();
        if (deg.isEmpty() && !user.getDepartment().isEmpty()) {
            deg = user.getDepartment();
        }
        tvProfileDegree.setText(!deg.isEmpty() ? deg : "Bachelor of Science (B.Sc.)");

        tvProfileDepartment.setText(!user.getDepartment().isEmpty() ? user.getDepartment() : "Yashoda College of Arts, Science and Commerce");

        // Adaptive: If Principal, render minimal institutional directory. If Student/Teacher, render subjects.
        if (user.isPrincipal()) {
            if (cardEnrolledSubjects != null) cardEnrolledSubjects.setVisibility(View.GONE);
            if (layoutPrincipalRosterSection != null) {
                layoutPrincipalRosterSection.setVisibility(View.VISIBLE);
                renderPrincipalMinimalRoster();
            }
        } else {
            if (cardEnrolledSubjects != null) cardEnrolledSubjects.setVisibility(View.VISIBLE);
            if (layoutPrincipalRosterSection != null) layoutPrincipalRosterSection.setVisibility(View.GONE);
            renderSubjects(user.getSubjectsList());
        }

        // Personal Details
        tvProfilePhone.setText(user.getPhoneNumber() != null && !user.getPhoneNumber().isEmpty() ? 
                user.getPhoneNumber() : "Not provided");
        tvProfileDob.setText(user.getDob() != null && !user.getDob().isEmpty() ? 
                user.getDob() : "Not provided");
    }

    /**
     * Renders institutional roster for Principal in two separate distinct rows:
     * - Row 1 (Faculty & Staff Data): Name • ID • Subjects Teaching
     * - Row 2 (Enrolled Students Directory Data): Roll No • Stream • Name (with stream filter support)
     */
    private void renderPrincipalMinimalRoster() {
        if (containerPrincipalFacultyMinimal == null || containerPrincipalStudentsMinimal == null) return;

        // 1. ROW 1: Faculty Minimal List
        containerPrincipalFacultyMinimal.removeAllViews();
        List<User> facultyList = userManager.getFacultyAndStaff();
        if (tvFacultyCountBadge != null) {
            tvFacultyCountBadge.setText(facultyList.size() + (facultyList.size() == 1 ? " Member" : " Members"));
        }

        for (int i = 0; i < facultyList.size(); i++) {
            User f = facultyList.get(i);
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.VERTICAL);
            row.setBackgroundResource(R.drawable.bg_input_box);
            row.setPadding(24, 16, 24, 16);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            if (i > 0) lp.topMargin = 12;
            row.setLayoutParams(lp);

            // Row 1: Name and ID
            LinearLayout topRow = new LinearLayout(this);
            topRow.setOrientation(LinearLayout.HORIZONTAL);
            topRow.setGravity(android.view.Gravity.CENTER_VERTICAL);

            TextView tvName = new TextView(this);
            LinearLayout.LayoutParams nameLp = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
            tvName.setLayoutParams(nameLp);
            tvName.setText("👩‍🏫 " + f.getFullName());
            tvName.setTextColor(getColor(R.color.charcoal_dark));
            tvName.setTextSize(13.5f);
            tvName.setTypeface(null, android.graphics.Typeface.BOLD);
            topRow.addView(tvName);

            TextView tvIdBadge = new TextView(this);
            tvIdBadge.setBackgroundResource(R.drawable.bg_chip_lavender);
            tvIdBadge.setText(f.getId());
            tvIdBadge.setTextColor(getColor(R.color.pastel_lavender_text));
            tvIdBadge.setTextSize(11f);
            tvIdBadge.setTypeface(null, android.graphics.Typeface.BOLD);
            tvIdBadge.setPadding(16, 6, 16, 6);
            topRow.addView(tvIdBadge);

            row.addView(topRow);

            // Row 2: Subjects Teaching
            TextView tvSubs = new TextView(this);
            String subs = f.getSubjects() != null && !f.getSubjects().isEmpty() ? f.getSubjects() : "General";
            tvSubs.setText("Subjects: " + subs + " (" + f.getDepartment() + ")");
            tvSubs.setTextColor(getColor(R.color.text_secondary));
            tvSubs.setTextSize(11.5f);
            tvSubs.setPadding(2, 6, 2, 0);
            row.addView(tvSubs);

            containerPrincipalFacultyMinimal.addView(row);
        }

        // 2. ROW 2: Students Minimal List
        renderFilteredStudents(selectedStreamFilter);
    }

    private void renderFilteredStudents(String streamFilter) {
        if (containerPrincipalStudentsMinimal == null) return;
        containerPrincipalStudentsMinimal.removeAllViews();

        List<User> allStudents = userManager.getStudents();
        List<User> filteredList = new ArrayList<>();

        for (User s : allStudents) {
            String dept = s.getDepartment() != null ? s.getDepartment().toLowerCase() : "";
            if ("ALL".equalsIgnoreCase(streamFilter) || dept.contains(streamFilter.toLowerCase())) {
                filteredList.add(s);
            }
        }

        if (tvStudentsDirectoryCountBadge != null) {
            tvStudentsDirectoryCountBadge.setText(filteredList.size() + (filteredList.size() == 1 ? " Student" : " Students"));
        }

        if (filteredList.isEmpty()) {
            TextView tvEmpty = new TextView(this);
            tvEmpty.setText("No students enrolled under " + streamFilter + " stream.");
            tvEmpty.setTextColor(getColor(R.color.text_muted));
            tvEmpty.setTextSize(12f);
            tvEmpty.setPadding(0, 16, 0, 16);
            containerPrincipalStudentsMinimal.addView(tvEmpty);
            return;
        }

        for (int i = 0; i < filteredList.size(); i++) {
            User s = filteredList.get(i);
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.VERTICAL);
            row.setBackgroundResource(R.drawable.bg_input_box);
            row.setPadding(24, 16, 24, 16);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            if (i > 0) lp.topMargin = 12;
            row.setLayoutParams(lp);

            // Row 1: Roll No & Stream
            LinearLayout topRow = new LinearLayout(this);
            topRow.setOrientation(LinearLayout.HORIZONTAL);
            topRow.setGravity(android.view.Gravity.CENTER_VERTICAL);

            TextView tvRoll = new TextView(this);
            LinearLayout.LayoutParams rollLp = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
            tvRoll.setLayoutParams(rollLp);
            tvRoll.setText("🆔 " + s.getId());
            tvRoll.setTextColor(getColor(R.color.charcoal_dark));
            tvRoll.setTextSize(13f);
            tvRoll.setTypeface(null, android.graphics.Typeface.BOLD);
            topRow.addView(tvRoll);

            TextView tvStreamBadge = new TextView(this);
            tvStreamBadge.setBackgroundResource(R.drawable.bg_chip_mint);
            String st = s.getDepartment() != null && !s.getDepartment().isEmpty() ? s.getDepartment() : "Science Stream";
            tvStreamBadge.setText(st);
            tvStreamBadge.setTextColor(getColor(R.color.pastel_mint_text));
            tvStreamBadge.setTextSize(10.5f);
            tvStreamBadge.setTypeface(null, android.graphics.Typeface.BOLD);
            tvStreamBadge.setPadding(16, 6, 16, 6);
            topRow.addView(tvStreamBadge);

            row.addView(topRow);

            // Row 2: Student Name & Class
            TextView tvName = new TextView(this);
            tvName.setText("Student: " + s.getFullName() + " (" + s.getYearOrClass() + ")");
            tvName.setTextColor(getColor(R.color.text_secondary));
            tvName.setTextSize(12f);
            tvName.setPadding(2, 6, 2, 0);
            row.addView(tvName);

            containerPrincipalStudentsMinimal.addView(row);
        }
    }

    private void selectStudentStreamFilter(String filter) {
        selectedStreamFilter = filter;
        updateStreamFilterChipStyles();
        renderFilteredStudents(filter);
    }

    private void updateStreamFilterChipStyles() {
        resetFilterChipStyle(chipFilterAll, "ALL".equalsIgnoreCase(selectedStreamFilter));
        resetFilterChipStyle(chipFilterScience, "Science".equalsIgnoreCase(selectedStreamFilter));
        resetFilterChipStyle(chipFilterCommerce, "Commerce".equalsIgnoreCase(selectedStreamFilter));
        resetFilterChipStyle(chipFilterArts, "Arts".equalsIgnoreCase(selectedStreamFilter));
    }

    private void resetFilterChipStyle(TextView chip, boolean isSelected) {
        if (chip == null) return;
        if (isSelected) {
            chip.setBackgroundResource(R.drawable.bg_chip_selected_charcoal);
            chip.setTextColor(getColor(R.color.white));
        } else {
            chip.setBackgroundResource(R.drawable.bg_chip_unselected_white);
            chip.setTextColor(getColor(R.color.charcoal_dark));
        }
    }

    private void renderSubjects(List<String> subjects) {
        containerSubjectsList.removeAllViews();

        if (subjects == null || subjects.isEmpty()) {
            tvSubjectsCountBadge.setText("0 Subjects");
            tvNoSubjectsNotice.setVisibility(View.VISIBLE);
            return;
        }

        tvNoSubjectsNotice.setVisibility(View.GONE);
        tvSubjectsCountBadge.setText(subjects.size() + (subjects.size() == 1 ? " Subject" : " Subjects"));

        LayoutInflater inflater = LayoutInflater.from(this);
        for (int i = 0; i < subjects.size(); i++) {
            String subject = subjects.get(i);

            LinearLayout itemLayout = new LinearLayout(this);
            itemLayout.setOrientation(LinearLayout.HORIZONTAL);
            itemLayout.setGravity(android.view.Gravity.CENTER_VERTICAL);
            itemLayout.setBackgroundResource(R.drawable.bg_input_box);
            itemLayout.setPadding(32, 22, 32, 22);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            if (i > 0) {
                lp.topMargin = 16;
            }
            itemLayout.setLayoutParams(lp);

            // Bullet Dot / Icon
            ImageView dot = new ImageView(this);
            dot.setLayoutParams(new LinearLayout.LayoutParams(22, 22));
            dot.setImageResource(R.drawable.bg_dot_accent);
            itemLayout.addView(dot);

            // Subject Title
            TextView tvSub = new TextView(this);
            LinearLayout.LayoutParams textLp = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
            textLp.setMarginStart(24);
            tvSub.setLayoutParams(textLp);
            tvSub.setText(subject);
            tvSub.setTextColor(getColor(R.color.charcoal_dark));
            tvSub.setTextSize(13.5f);
            tvSub.setTypeface(null, android.graphics.Typeface.BOLD);
            itemLayout.addView(tvSub);

            // Enrolled Chip
            TextView chip = new TextView(this);
            chip.setBackgroundResource(R.drawable.bg_chip_mint);
            chip.setText("Enrolled");
            chip.setTextColor(getColor(R.color.pastel_mint_text));
            chip.setTextSize(10.5f);
            chip.setTypeface(null, android.graphics.Typeface.BOLD);
            chip.setPadding(20, 8, 20, 8);
            itemLayout.addView(chip);

            containerSubjectsList.addView(itemLayout);
        }
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        View.OnClickListener editListener = v -> showEditProfileDialog();
        btnTopEdit.setOnClickListener(editListener);
        btnEditProfile.setOnClickListener(editListener);

        btnCopyEmail.setOnClickListener(v -> {
            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Eventra Email", tvProfileEmail.getText().toString());
            cm.setPrimaryClip(clip);
            Toast.makeText(this, "📋 Email copied to clipboard", Toast.LENGTH_SHORT).show();
        });

        btnLogout.setOnClickListener(v -> confirmLogout());

        // Principal Two Separate Rows Expansion Listeners
        if (rowHeaderPrincipalTeachers != null) {
            rowHeaderPrincipalTeachers.setOnClickListener(v -> {
                isTeachersExpanded = !isTeachersExpanded;
                if (layoutTeachersDataContent != null) {
                    layoutTeachersDataContent.setVisibility(isTeachersExpanded ? View.VISIBLE : View.GONE);
                }
                if (tvTeachersChevron != null) {
                    tvTeachersChevron.setText(isTeachersExpanded ? "▼" : "▲");
                }
            });
        }

        if (rowHeaderPrincipalStudents != null) {
            rowHeaderPrincipalStudents.setOnClickListener(v -> {
                isStudentsExpanded = !isStudentsExpanded;
                if (layoutStudentsDataContent != null) {
                    layoutStudentsDataContent.setVisibility(isStudentsExpanded ? View.VISIBLE : View.GONE);
                }
                if (tvStudentsChevron != null) {
                    tvStudentsChevron.setText(isStudentsExpanded ? "▼" : "▲");
                }
            });
        }

        // Stream Filter Chips for Students Row
        if (chipFilterAll != null) chipFilterAll.setOnClickListener(v -> selectStudentStreamFilter("ALL"));
        if (chipFilterScience != null) chipFilterScience.setOnClickListener(v -> selectStudentStreamFilter("Science"));
        if (chipFilterCommerce != null) chipFilterCommerce.setOnClickListener(v -> selectStudentStreamFilter("Commerce"));
        if (chipFilterArts != null) chipFilterArts.setOnClickListener(v -> selectStudentStreamFilter("Arts"));
    }

    private void showEditProfileDialog() {
        if (currentUser == null) return;

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_profile, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        TextView tvBadge = dialogView.findViewById(R.id.tvEditInstitutionBadge);
        EditText etName = dialogView.findViewById(R.id.etEditFullName);
        EditText etAddress = dialogView.findViewById(R.id.etEditAddress);
        EditText etPhone = dialogView.findViewById(R.id.etEditPhone);
        EditText etDob = dialogView.findViewById(R.id.etEditDob);
        View layoutDob = dialogView.findViewById(R.id.layoutEditDob);
        View btnPickDob = dialogView.findViewById(R.id.btnEditPickDob);

        View containerSchool = dialogView.findViewById(R.id.containerEditSchool);
        EditText etClass = dialogView.findViewById(R.id.etEditClass);
        EditText etStream = dialogView.findViewById(R.id.etEditStream);

        View containerCollege = dialogView.findViewById(R.id.containerEditCollege);
        EditText etDegree = dialogView.findViewById(R.id.etEditDegree);
        EditText etYear = dialogView.findViewById(R.id.etEditYear);
        EditText etDept = dialogView.findViewById(R.id.etEditDepartment);

        TextView btnSelectSubjects = dialogView.findViewById(R.id.btnSelectSubjectsList);
        EditText etSubjects = dialogView.findViewById(R.id.etEditSubjects);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btnCancelEdit);
        MaterialButton btnSave = dialogView.findViewById(R.id.btnSaveProfile);

        // Pre-fill values
        tvBadge.setText(currentUser.getInstitutionType());
        etName.setText(currentUser.getFullName());
        if (etAddress != null) etAddress.setText(currentUser.getAddress());
        etPhone.setText(currentUser.getPhoneNumber());
        etDob.setText(currentUser.getDob());
        etSubjects.setText(currentUser.getSubjects());

        // Select subjects from curriculum list
        if (btnSelectSubjects != null) {
            btnSelectSubjects.setOnClickListener(v -> {
                String[] allSubjects = new String[]{
                        "Physics", "Chemistry", "Mathematics", "Computer Science", 
                        "Biology", "Electronics", "Quantum Physics", "Organic Chemistry",
                        "Financial Accounting", "Business Economics", "Corporate Law", 
                        "Cost Accounting", "Auditing", "Banking & Finance", "Macroeconomics",
                        "History", "Psychology", "Political Science", "English Literature", 
                        "Sociology", "Environmental Studies"
                };

                String currentSubs = etSubjects.getText().toString().trim();
                boolean[] checkedItems = new boolean[allSubjects.length];
                for (int i = 0; i < allSubjects.length; i++) {
                    checkedItems[i] = currentSubs.toLowerCase().contains(allSubjects[i].toLowerCase());
                }

                new AlertDialog.Builder(this)
                        .setTitle("Select Enrolled Subjects")
                        .setMultiChoiceItems(allSubjects, checkedItems, (d, which, isChecked) -> {
                            checkedItems[which] = isChecked;
                        })
                        .setPositiveButton("Select", (d, which) -> {
                            StringBuilder sb = new StringBuilder();
                            for (int i = 0; i < allSubjects.length; i++) {
                                if (checkedItems[i]) {
                                    if (sb.length() > 0) sb.append(", ");
                                    sb.append(allSubjects[i]);
                                }
                            }
                            etSubjects.setText(sb.toString());
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });
        }

        boolean isSchool = currentUser.isSchoolOrHigherSec();
        if (isSchool) {
            containerSchool.setVisibility(View.VISIBLE);
            containerCollege.setVisibility(View.GONE);
            etClass.setText(currentUser.getYearOrClass());
            etStream.setText(currentUser.getDepartment());
        } else {
            containerSchool.setVisibility(View.GONE);
            containerCollege.setVisibility(View.VISIBLE);
            etDegree.setText(currentUser.getDegree());
            etYear.setText(currentUser.getYearOrClass());
            etDept.setText(currentUser.getDepartment());
        }

        // DOB Picker
        View.OnClickListener dobListener = v -> {
            Calendar c = Calendar.getInstance();
            int y = 2005, m = 0, d = 1;
            String curDob = etDob.getText().toString().trim();
            if (curDob.contains("/")) {
                String[] p = curDob.split("/");
                if (p.length == 3) {
                    try {
                        d = Integer.parseInt(p[0]);
                        m = Integer.parseInt(p[1]) - 1;
                        y = Integer.parseInt(p[2]);
                    } catch (Exception ignored) {}
                }
            }

            DatePickerDialog dpd = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                etDob.setText(String.format(Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, month + 1, year));
            }, y, m, d);
            dpd.show();
        };

        etDob.setOnClickListener(dobListener);
        btnPickDob.setOnClickListener(dobListener);
        layoutDob.setOnClickListener(dobListener);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String updatedName = etName.getText().toString().trim();
            if (updatedName.isEmpty()) {
                etName.setError("Full name is required");
                return;
            }

            currentUser.setFullName(updatedName);
            if (etAddress != null) currentUser.setAddress(etAddress.getText().toString().trim());
            currentUser.setPhoneNumber(etPhone.getText().toString().trim());
            currentUser.setDob(etDob.getText().toString().trim());
            currentUser.setSubjects(etSubjects.getText().toString().trim());

            if (isSchool) {
                currentUser.setYearOrClass(etClass.getText().toString().trim());
                String str = etStream.getText().toString().trim();
                if (!str.isEmpty()) {
                    currentUser.setDepartment(str);
                }
            } else {
                currentUser.setDegree(etDegree.getText().toString().trim());
                currentUser.setYearOrClass(etYear.getText().toString().trim());
                String dept = etDept.getText().toString().trim();
                if (!dept.isEmpty()) {
                    currentUser.setDepartment(dept);
                }
            }

            boolean success = userManager.updateUser(currentUser);
            if (success) {
                Toast.makeText(this, "✅ Profile updated successfully!", Toast.LENGTH_SHORT).show();
                bindUserData(currentUser);
                dialog.dismiss();
            } else {
                Toast.makeText(this, "Failed to save profile changes.", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();

        if (dialog.getWindow() != null) {
            android.util.DisplayMetrics dm = getResources().getDisplayMetrics();
            int h = (int) (dm.heightPixels * 0.85);
            int w = (int) (dm.widthPixels * 0.94);
            dialog.getWindow().setLayout(w, h);
            dialog.getWindow().setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
    }

    private void confirmLogout() {
        new AlertDialog.Builder(this)
                .setTitle("Log Out of Eventra")
                .setMessage("Are you sure you want to log out of your @ycasc.com account on this device?")
                .setPositiveButton("Log Out", (dialog, which) -> {
                    userManager.logout();
                    Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                    navigateToLogin();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
