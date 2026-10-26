package com.example.sams;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private TextView tvHeaderAvatar;
    private TextView tvHeaderStudentName;
    private View headerProfileSection;
    private UserManager userManager;
    private SpotPassManager spotPassManager;

    // Spot Pass Card views on Home screen
    private View cardSpotPass;
    private TextView tvSpotPassCardTitle;
    private TextView tvSpotPassCardStatus;
    private TextView tvSpotPassCardSlot;
    private LinearProgressIndicator progressSpotPassCard;
    private TextView tvSpotPassAvatarCt;
    private TextView tvSpotPassAvatarSt;

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

        tvHeaderAvatar = findViewById(R.id.tvHeaderAvatar);
        tvHeaderStudentName = findViewById(R.id.tvHeaderStudentName);
        headerProfileSection = findViewById(R.id.headerProfileSection);

        cardSpotPass = findViewById(R.id.cardSpotPass);
        tvSpotPassCardTitle = findViewById(R.id.tvSpotPassCardTitle);
        tvSpotPassCardStatus = findViewById(R.id.tvSpotPassCardStatus);
        tvSpotPassCardSlot = findViewById(R.id.tvSpotPassCardSlot);
        progressSpotPassCard = findViewById(R.id.progressSpotPassCard);
        tvSpotPassAvatarCt = findViewById(R.id.tvSpotPassAvatarCt);
        tvSpotPassAvatarSt = findViewById(R.id.tvSpotPassAvatarSt);

        // Header Actions
        click(R.id.btnNotifications, "Notifications");
        click(R.id.btnSearch, "Search");

        // Profile / Account option in Header
        View.OnClickListener openProfileListener = v -> {
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
        };
        if (headerProfileSection != null) {
            headerProfileSection.setOnClickListener(openProfileListener);
        }

        // Quick action: New Spot Pass (Done by students -> routes to Class Teacher & Subject Teacher)
        View btnNewSpotPass = findViewById(R.id.btnNewSpotPass);
        if (btnNewSpotPass != null) {
            btnNewSpotPass.setOnClickListener(v -> showNewSpotPassDialog());
        }

        click(R.id.btnPermission, "Permission Request");

        // Section links and cards
        click(R.id.linkOpenCalendar, "Calendar");
        click(R.id.linkSeeAll, "All requests");

        if (cardSpotPass != null) {
            cardSpotPass.setOnClickListener(v -> {
                User u = userManager.getCurrentUser();
                SpotPass latest = spotPassManager.getLatestSpotPassForStudent(u != null ? u.getId() : "");
                if (latest != null) {
                    showSpotPassDetailsDialog(latest);
                } else {
                    Toast.makeText(this, "No active Spot Pass found.", Toast.LENGTH_SHORT).show();
                }
            });
        }

        click(R.id.cardPermission, "Permission details");
        click(R.id.cardNotice, "Circulation notice");

        // Bottom navigation
        findViewById(R.id.navHome).setOnClickListener(v -> {
            // Already on Home
        });
        click(R.id.navCalendar, "Calendar");
        click(R.id.navRequests, "Requests");

        // Account / Profile option in Bottom Bar
        View navProfile = findViewById(R.id.navProfile);
        if (navProfile != null) {
            navProfile.setOnClickListener(openProfileListener);
        }

        // Floating action button also triggers New Spot Pass
        View navCreate = findViewById(R.id.navCreate);
        if (navCreate != null) {
            navCreate.setOnClickListener(v -> showNewSpotPassDialog());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCurrentUserData();
        loadSpotPassCardData();
    }

    private void loadCurrentUserData() {
        User user = userManager.getCurrentUser();
        if (user != null) {
            if (tvHeaderStudentName != null && user.getFullName() != null) {
                tvHeaderStudentName.setText(user.getFullName());
            }
            if (tvHeaderAvatar != null) {
                tvHeaderAvatar.setText(user.getInitials());
            }
        }
    }

    private void loadSpotPassCardData() {
        User user = userManager.getCurrentUser();
        String studentId = user != null ? user.getId() : "";
        SpotPass latest = spotPassManager.getLatestSpotPassForStudent(studentId);

        if (latest != null) {
            if (tvSpotPassCardTitle != null) {
                tvSpotPassCardTitle.setText("Spot Pass · " + latest.getActivityTitle());
            }
            if (tvSpotPassCardSlot != null) {
                tvSpotPassCardSlot.setText(latest.getTimeSlot());
            }
            if (tvSpotPassCardStatus != null) {
                tvSpotPassCardStatus.setText(latest.getStatus());
            }
            if (progressSpotPassCard != null) {
                progressSpotPassCard.setProgress(latest.getProgress());
            }
        }
    }

    /**
     * Modal dialog allowing students to create a New Spot Pass.
     * Routes automatically to:
     * 1. Class Teacher (Primary absence & duty approval)
     * 2. Subject Teacher of that particular lecture/time period (Period attendance duty credit)
     */
    private void showNewSpotPassDialog() {
        User user = userManager.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Please log in as a student to apply for a Spot Pass", Toast.LENGTH_SHORT).show();
            return;
        }

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_new_spot_pass, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        TextView tvAvatar = dialogView.findViewById(R.id.tvSpotPassStudentAvatar);
        TextView tvName = dialogView.findViewById(R.id.tvSpotPassStudentName);
        TextView tvMeta = dialogView.findViewById(R.id.tvSpotPassStudentMeta);

        EditText etActivityTitle = dialogView.findViewById(R.id.etSpotPassActivityTitle);
        Spinner spinnerTimeSlot = dialogView.findViewById(R.id.spinnerTimeSlot);
        Spinner spinnerSubject = dialogView.findViewById(R.id.spinnerSubject);
        TextView tvClassTeacher = dialogView.findViewById(R.id.tvClassTeacherName);
        TextView tvSubjectTeacher = dialogView.findViewById(R.id.tvSubjectTeacherName);
        EditText etReason = dialogView.findViewById(R.id.etSpotPassReason);

        TextView chipFootball = dialogView.findViewById(R.id.chipActFootball);
        TextView chipCultural = dialogView.findViewById(R.id.chipActCultural);
        TextView chipEventDuty = dialogView.findViewById(R.id.chipActEventDuty);
        TextView chipNss = dialogView.findViewById(R.id.chipActNss);

        MaterialButton btnCancel = dialogView.findViewById(R.id.btnCancelSpotPass);
        MaterialButton btnSubmit = dialogView.findViewById(R.id.btnSubmitSpotPass);

        // Populate Student Header
        tvAvatar.setText(user.getInitials());
        tvName.setText(user.getFullName());
        String metaText = user.getId() + " • " + (user.getYearOrClass().isEmpty() ? "11th Standard" : user.getYearOrClass())
                + " (" + (user.getDepartment().isEmpty() ? "Science" : user.getDepartment()) + ")";
        tvMeta.setText(metaText);

        // Pre-determine Class Teacher based on student stream / division
        final String assignedClassTeacher;
        if (user.getDepartment().toLowerCase().contains("commerce")) {
            assignedClassTeacher = "Sujata Kamat (Commerce HOD / Class Teacher)";
        } else if (user.getDepartment().toLowerCase().contains("arts")) {
            assignedClassTeacher = "Sanjay Bandodkar (Arts HOD / Class Teacher)";
        } else if (user.isCollegeOrUniversity()) {
            assignedClassTeacher = "Dr. Robert Vance (Programme Director / Mentor)";
        } else {
            assignedClassTeacher = "Pramod Naik (Science HOD / Class Teacher)";
        }
        tvClassTeacher.setText(assignedClassTeacher);

        // Populate Time Slots
        List<String> timeSlots = Arrays.asList(
                "Lecture slot 1 (8:00 – 9:00 AM)",
                "Lecture slot 2 (9:00 – 10:00 AM)",
                "Lecture slot 1–2 (8:00 – 10:00 AM)",
                "Lecture slot 3 (10:15 – 11:15 AM)",
                "Lecture slot 4 (11:15 AM – 12:15 PM)",
                "Afternoon Lab (1:00 – 3:30 PM)"
        );
        ArrayAdapter<String> timeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, timeSlots);
        spinnerTimeSlot.setAdapter(timeAdapter);

        // Populate Subjects (from user's enrolled subjects or stream defaults)
        List<String> subjects = user.getSubjectsList();
        if (subjects.isEmpty()) {
            subjects = Arrays.asList("Chemistry", "Physics", "Mathematics", "Biology", "English", "Computer Science");
        }
        ArrayAdapter<String> subAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, subjects);
        spinnerSubject.setAdapter(subAdapter);

        // Dynamically update Subject Teacher when the particular subject/time is picked
        spinnerSubject.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String sub = parent.getItemAtPosition(position).toString().toLowerCase();
                String teacher;
                if (sub.contains("chem")) {
                    teacher = "Sneha Gaonkar (Chemistry Subject Teacher)";
                } else if (sub.contains("phys")) {
                    teacher = "Pramod Naik (Physics Subject Teacher)";
                } else if (sub.contains("math")) {
                    teacher = "Dr. Shashikant Mayekar (Mathematics Teacher)";
                } else if (sub.contains("bio") || sub.contains("computer")) {
                    teacher = "Vikram Dessai (Biology / CS Teacher)";
                } else if (sub.contains("account") || sub.contains("business")) {
                    teacher = "Aarti Faldesai (Commerce Subject Teacher)";
                } else {
                    teacher = "Maria Rodrigues (Subject Teacher)";
                }
                tvSubjectTeacher.setText(teacher);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Quick Activity Chip Click Listeners
        View.OnClickListener chipListener = v -> {
            chipFootball.setBackgroundResource(R.drawable.bg_role_student_unselected);
            chipCultural.setBackgroundResource(R.drawable.bg_role_student_unselected);
            chipEventDuty.setBackgroundResource(R.drawable.bg_role_student_unselected);
            chipNss.setBackgroundResource(R.drawable.bg_role_student_unselected);

            v.setBackgroundResource(R.drawable.bg_role_student_selected);

            if (v == chipFootball) {
                etActivityTitle.setText("Football practice");
                etReason.setText("Annual inter-college athletic practice trials on Main Ground.");
            } else if (v == chipCultural) {
                etActivityTitle.setText("Cultural rehearsal");
                etReason.setText("Annual Day choir & drama rehearsal in Main Auditorium.");
            } else if (v == chipEventDuty) {
                etActivityTitle.setText("Campus Event Duty");
                etReason.setText("Official volunteer duty for science exhibition and registration desk.");
            } else if (v == chipNss) {
                etActivityTitle.setText("NSS Volunteer Camp");
                etReason.setText("NSS Unit coordination and blood donation camp assistance.");
            }
        };

        chipFootball.setOnClickListener(chipListener);
        chipCultural.setOnClickListener(chipListener);
        chipEventDuty.setOnClickListener(chipListener);
        chipNss.setOnClickListener(chipListener);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSubmit.setOnClickListener(v -> {
            String actTitle = etActivityTitle.getText().toString().trim();
            if (actTitle.isEmpty()) {
                etActivityTitle.setError("Please enter activity title");
                return;
            }

            String selectedSlot = spinnerTimeSlot.getSelectedItem().toString();
            String selectedSub = spinnerSubject.getSelectedItem().toString();
            String subTeacher = tvSubjectTeacher.getText().toString();
            String ctTeacher = tvClassTeacher.getText().toString();
            String reason = etReason.getText().toString().trim();
            if (reason.isEmpty()) {
                reason = "Event duty during " + selectedSlot + ". Attendance duty credit applied.";
            }

            String newId = "SP-2026-" + (int)(100 + Math.random() * 900);

            SpotPass newPass = new SpotPass(
                    newId,
                    user.getId(),
                    user.getFullName(),
                    user.getYearOrClass(),
                    actTitle,
                    "Today",
                    selectedSlot,
                    selectedSub,
                    subTeacher,
                    ctTeacher,
                    reason,
                    "Pending CT",
                    65,
                    "Pending Review",
                    "Notified for " + selectedSub,
                    System.currentTimeMillis()
            );

            spotPassManager.addSpotPass(newPass);
            loadSpotPassCardData();
            dialog.dismiss();

            // Celebratory confirmation showing explicit dispatch to Class Teacher and Subject Teacher
            new AlertDialog.Builder(this)
                    .setTitle("🎉 Spot Pass Dispatched!")
                    .setMessage("Your Spot Pass for \"" + actTitle + "\" (" + selectedSlot + ") has been created and routed to:\n\n"
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

        btnClose.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    /** Placeholder click handler: shows a toast. Replace with startActivity(...) or fragment navigation. */
    private void click(int viewId, String label) {
        View v = findViewById(viewId);
        if (v != null) {
            v.setOnClickListener(view ->
                    Toast.makeText(this, label, Toast.LENGTH_SHORT).show());
        }
    }
}

