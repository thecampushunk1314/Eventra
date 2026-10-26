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
    private View btnCopyEmail;

    // Academic Details
    private TextView tvProfileYearLabel;
    private TextView tvProfileYearOrClass;
    private TextView tvProfileDegreeLabel;
    private TextView tvProfileDegree;
    private TextView tvProfileDepartment;

    // Subjects
    private TextView tvSubjectsCountBadge;
    private LinearLayout containerSubjectsList;
    private TextView tvNoSubjectsNotice;

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
        btnCopyEmail = findViewById(R.id.btnCopyEmail);

        tvProfileYearLabel = findViewById(R.id.tvProfileYearLabel);
        tvProfileYearOrClass = findViewById(R.id.tvProfileYearOrClass);
        tvProfileDegreeLabel = findViewById(R.id.tvProfileDegreeLabel);
        tvProfileDegree = findViewById(R.id.tvProfileDegree);
        tvProfileDepartment = findViewById(R.id.tvProfileDepartment);

        tvSubjectsCountBadge = findViewById(R.id.tvSubjectsCountBadge);
        containerSubjectsList = findViewById(R.id.containerSubjectsList);
        tvNoSubjectsNotice = findViewById(R.id.tvNoSubjectsNotice);

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
        String instType = user.getInstitutionType() != null ? user.getInstitutionType() : User.INSTITUTION_UNIVERSITY;
        if (user.isSchoolOrHigherSec()) {
            tvProfileInstitutionBadge.setText("🏫 Higher Secondary");
        } else if (User.INSTITUTION_COLLEGE.equalsIgnoreCase(instType)) {
            tvProfileInstitutionBadge.setText("🏛️ College");
        } else {
            tvProfileInstitutionBadge.setText("🎓 University");
        }

        // Email & Roll No
        tvProfileEmail.setText(user.getEmail() != null ? user.getEmail() : "user@eventra.ac.in");
        tvProfileRollNo.setText(user.getId() != null ? user.getId() : "N/A");

        // Buyer-Adaptive Academic details
        boolean isSchool = user.isSchoolOrHigherSec();
        if (isSchool) {
            tvProfileYearLabel.setText("Class / Standard");
            String cls = user.getYearOrClass();
            tvProfileYearOrClass.setText(!cls.isEmpty() ? cls : "11th Standard");

            tvProfileDegreeLabel.setText("Stream / Specialization");
            String stream = user.getDepartment();
            tvProfileDegree.setText(!stream.isEmpty() ? stream : "Science Stream");

            tvProfileDepartment.setText(!user.getDepartment().isEmpty() ? user.getDepartment() : "Higher Secondary Academic Division");
        } else {
            tvProfileYearLabel.setText("Level of Year / Class");
            String yr = user.getYearOrClass();
            tvProfileYearOrClass.setText(!yr.isEmpty() ? yr : "2nd Year (Batch 2026)");

            tvProfileDegreeLabel.setText("Degree / Programme");
            String deg = user.getDegree();
            tvProfileDegree.setText(!deg.isEmpty() ? deg : 
                    (!user.getProgramme().isEmpty() ? user.getProgramme() : "Master of Computer Applications (MCA)"));

            tvProfileDepartment.setText(!user.getDepartment().isEmpty() ? user.getDepartment() : "Department of Computer Applications");
        }

        // Enrolled Subjects
        renderSubjects(user.getSubjectsList());

        // Personal Details
        tvProfilePhone.setText(user.getPhoneNumber() != null && !user.getPhoneNumber().isEmpty() ? 
                user.getPhoneNumber() : "Not provided");
        tvProfileDob.setText(user.getDob() != null && !user.getDob().isEmpty() ? 
                user.getDob() : "Not provided");
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

        EditText etSubjects = dialogView.findViewById(R.id.etEditSubjects);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btnCancelEdit);
        MaterialButton btnSave = dialogView.findViewById(R.id.btnSaveProfile);

        // Pre-fill values
        tvBadge.setText(currentUser.getInstitutionType());
        etName.setText(currentUser.getFullName());
        etPhone.setText(currentUser.getPhoneNumber());
        etDob.setText(currentUser.getDob());
        etSubjects.setText(currentUser.getSubjects());

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
    }

    private void confirmLogout() {
        new AlertDialog.Builder(this)
                .setTitle("Log Out of Eventra")
                .setMessage("Are you sure you want to log out of your @" + "eventra.ac.in account on this device?")
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
