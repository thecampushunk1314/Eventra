package com.example.sams;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.text.HtmlCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.google.android.material.button.MaterialButton;

public class MainActivity extends AppCompatActivity {

    private static final String EVENTRA_DOMAIN = "@eventra.ac.in";

    // Header & Tabs
    private View headerCard;
    private TextView tabRegister, tabLogin;
    private View containerRegister, containerLogin;
    private TextView tvFooterPrompt;

    // Register Form Views
    private EditText etRegisterFullName;
    private EditText etRegisterRollNo;
    private EditText etRegisterEmail;
    private EditText etRegisterPassword;
    private ImageView btnRegisterTogglePassword;
    private TextView roleStudent, roleFaculty, roleOrganizer;
    private TextView instHigherSec, instCollege, instUniversity;
    private EditText etRegisterPhone;
    private EditText etRegisterDob;
    private ImageView btnPickDob;
    private View layoutPickDob;
    private View containerSchoolFields, containerCollegeFields;
    private EditText etRegisterClass, etRegisterStream, etRegisterSubjectsSchool;
    private EditText etRegisterDegree, etRegisterYearCollege, etRegisterDepartment, etRegisterSubjectsCollege;
    private CheckBox cbTerms;
    private TextView tvTerms;
    private MaterialButton btnCreateAccount;

    // Login Form Views
    private EditText etLoginEmail;
    private EditText etLoginPassword;
    private ImageView btnLoginTogglePassword;
    private TextView btnForgotPassword;
    private MaterialButton btnLoginSubmit;

    // State
    private boolean isLoginMode = false;
    private boolean isRegisterPasswordVisible = false;
    private boolean isLoginPasswordVisible = false;
    private String selectedRole = User.ROLE_STUDENT;
    private String selectedInstitutionType = User.INSTITUTION_UNIVERSITY;

    private UserManager userManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Allow phone navigation buttons (Back, Home, Recents) to always stay visible
        WindowCompat.setDecorFitsSystemWindows(getWindow(), true);
        WindowInsetsControllerCompat insetsController = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (insetsController != null) {
            insetsController.show(WindowInsetsCompat.Type.systemBars());
        }

        setContentView(R.layout.activity_login);

        View mainRoot = findViewById(R.id.mainRoot);
        if (mainRoot != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainRoot, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        userManager = UserManager.getInstance(this);

        // Check if user is already logged in
        User currentUser = userManager.getCurrentUser();
        if (currentUser != null) {
            goToHome(currentUser);
            return;
        }

        initViews();
        setupTabs();
        setupRoleSelector();
        setupInstitutionSelector();
        setupDobPicker();
        setupPasswordToggles();
        setupTermsAndFooter();
        setupActions();

        // Default to Register mode (matching left screen in reference design)
        showRegisterMode();
    }

    private void initViews() {
        headerCard = findViewById(R.id.headerCard);
        tabRegister = findViewById(R.id.tabRegister);
        tabLogin = findViewById(R.id.tabLogin);
        containerRegister = findViewById(R.id.containerRegister);
        containerLogin = findViewById(R.id.containerLogin);
        tvFooterPrompt = findViewById(R.id.tvFooterPrompt);

        // Register form
        etRegisterFullName = findViewById(R.id.etRegisterFullName);
        etRegisterRollNo = findViewById(R.id.etRegisterRollNo);
        etRegisterEmail = findViewById(R.id.etRegisterEmail);
        etRegisterPassword = findViewById(R.id.etRegisterPassword);
        btnRegisterTogglePassword = findViewById(R.id.btnRegisterTogglePassword);
        roleStudent = findViewById(R.id.roleStudent);
        roleFaculty = findViewById(R.id.roleFaculty);
        roleOrganizer = findViewById(R.id.roleOrganizer);

        // Institution Buyer selector
        instHigherSec = findViewById(R.id.instHigherSec);
        instCollege = findViewById(R.id.instCollege);
        instUniversity = findViewById(R.id.instUniversity);

        // Personal details
        etRegisterPhone = findViewById(R.id.etRegisterPhone);
        etRegisterDob = findViewById(R.id.etRegisterDob);
        btnPickDob = findViewById(R.id.btnPickDob);
        layoutPickDob = findViewById(R.id.layoutPickDob);

        // Adaptive fields
        containerSchoolFields = findViewById(R.id.containerSchoolFields);
        containerCollegeFields = findViewById(R.id.containerCollegeFields);
        etRegisterClass = findViewById(R.id.etRegisterClass);
        etRegisterStream = findViewById(R.id.etRegisterStream);
        etRegisterSubjectsSchool = findViewById(R.id.etRegisterSubjectsSchool);

        etRegisterDegree = findViewById(R.id.etRegisterDegree);
        etRegisterYearCollege = findViewById(R.id.etRegisterYearCollege);
        etRegisterDepartment = findViewById(R.id.etRegisterDepartment);
        etRegisterSubjectsCollege = findViewById(R.id.etRegisterSubjectsCollege);

        cbTerms = findViewById(R.id.cbTerms);
        tvTerms = findViewById(R.id.tvTerms);
        btnCreateAccount = findViewById(R.id.btnCreateAccount);

        // Login form
        etLoginEmail = findViewById(R.id.etLoginEmail);
        etLoginPassword = findViewById(R.id.etLoginPassword);
        btnLoginTogglePassword = findViewById(R.id.btnLoginTogglePassword);
        btnForgotPassword = findViewById(R.id.btnForgotPassword);
        btnLoginSubmit = findViewById(R.id.btnLoginSubmit);
    }

    private void setupTabs() {
        tabRegister.setOnClickListener(v -> showRegisterMode());
        tabLogin.setOnClickListener(v -> showLoginMode());
    }

    private void showRegisterMode() {
        isLoginMode = false;

        // Header Background -> Pastel Lavender (#EAE4FC)
        headerCard.setBackgroundResource(R.drawable.bg_header_lavender);

        // Tab Switcher Styling
        tabRegister.setBackgroundResource(R.drawable.bg_tab_active);
        tabRegister.setTextColor(getColor(R.color.charcoal_dark));
        tabLogin.setBackgroundResource(android.R.color.transparent);
        tabLogin.setTextColor(getColor(R.color.text_muted));

        // Form Visibility
        containerRegister.setVisibility(View.VISIBLE);
        containerLogin.setVisibility(View.GONE);

        // Footer Prompt
        updateFooterPrompt("Already registered? ", "Log in", v -> showLoginMode());
    }

    private void showLoginMode() {
        isLoginMode = true;

        // Header Background -> Pastel Mint (#D4F4E4)
        headerCard.setBackgroundResource(R.drawable.bg_header_mint);

        // Tab Switcher Styling
        tabLogin.setBackgroundResource(R.drawable.bg_tab_active);
        tabLogin.setTextColor(getColor(R.color.charcoal_dark));
        tabRegister.setBackgroundResource(android.R.color.transparent);
        tabRegister.setTextColor(getColor(R.color.text_muted));

        // Form Visibility
        containerRegister.setVisibility(View.GONE);
        containerLogin.setVisibility(View.VISIBLE);

        // Footer Prompt
        updateFooterPrompt("Not registered yet? ", "Register", v -> showRegisterMode());
    }

    private void updateFooterPrompt(String normalText, String actionText, View.OnClickListener onClick) {
        String full = normalText + actionText;
        SpannableString ss = new SpannableString(full);
        int start = full.indexOf(actionText);
        int end = start + actionText.length();

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                if (onClick != null) onClick.onClick(widget);
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(true);
                ds.setColor(getColor(R.color.charcoal_dark));
                ds.setFakeBoldText(true);
            }
        };

        ss.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ss.setSpan(new UnderlineSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        tvFooterPrompt.setText(ss);
        tvFooterPrompt.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void setupRoleSelector() {
        roleStudent.setOnClickListener(v -> selectRole(User.ROLE_STUDENT));
        roleFaculty.setOnClickListener(v -> selectRole(User.ROLE_TEACHER));
        roleOrganizer.setOnClickListener(v -> selectRole(User.ROLE_ORGANIZER));
        selectRole(User.ROLE_STUDENT);
    }

    private void selectRole(String role) {
        selectedRole = role;

        // Reset all role pills
        roleStudent.setBackgroundResource(R.drawable.bg_role_student_unselected);
        roleFaculty.setBackgroundResource(R.drawable.bg_role_faculty_unselected);
        roleOrganizer.setBackgroundResource(R.drawable.bg_role_organizer_unselected);

        // Set selected pill with dark outline
        if (User.ROLE_STUDENT.equals(role)) {
            roleStudent.setBackgroundResource(R.drawable.bg_role_student_selected);
        } else if (User.ROLE_TEACHER.equals(role)) {
            roleFaculty.setBackgroundResource(R.drawable.bg_role_faculty_selected);
        } else if (User.ROLE_ORGANIZER.equals(role)) {
            roleOrganizer.setBackgroundResource(R.drawable.bg_role_organizer_selected);
        }
    }

    private void setupInstitutionSelector() {
        instHigherSec.setOnClickListener(v -> selectInstitution(User.INSTITUTION_HIGHER_SECONDARY));
        instCollege.setOnClickListener(v -> selectInstitution(User.INSTITUTION_COLLEGE));
        instUniversity.setOnClickListener(v -> selectInstitution(User.INSTITUTION_UNIVERSITY));
        selectInstitution(User.INSTITUTION_UNIVERSITY);
    }

    private void selectInstitution(String institutionType) {
        selectedInstitutionType = institutionType;

        // Reset backgrounds
        instHigherSec.setBackgroundResource(R.drawable.bg_role_student_unselected);
        instCollege.setBackgroundResource(R.drawable.bg_role_student_unselected);
        instUniversity.setBackgroundResource(R.drawable.bg_role_student_unselected);

        if (User.INSTITUTION_HIGHER_SECONDARY.equals(institutionType) || User.INSTITUTION_SCHOOL.equals(institutionType)) {
            instHigherSec.setBackgroundResource(R.drawable.bg_role_student_selected);
            containerSchoolFields.setVisibility(View.VISIBLE);
            containerCollegeFields.setVisibility(View.GONE);
        } else if (User.INSTITUTION_COLLEGE.equals(institutionType)) {
            instCollege.setBackgroundResource(R.drawable.bg_role_student_selected);
            containerSchoolFields.setVisibility(View.GONE);
            containerCollegeFields.setVisibility(View.VISIBLE);
        } else {
            instUniversity.setBackgroundResource(R.drawable.bg_role_student_selected);
            containerSchoolFields.setVisibility(View.GONE);
            containerCollegeFields.setVisibility(View.VISIBLE);
        }
    }

    private void setupDobPicker() {
        View.OnClickListener pickListener = v -> {
            Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR) - 18;
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog dialog = new DatePickerDialog(this, (view, y, m, d) -> {
                String formatted = String.format(Locale.getDefault(), "%02d/%02d/%04d", d, m + 1, y);
                etRegisterDob.setText(formatted);
            }, year, month, day);
            dialog.show();
        };

        if (etRegisterDob != null) etRegisterDob.setOnClickListener(pickListener);
        if (btnPickDob != null) btnPickDob.setOnClickListener(pickListener);
        if (layoutPickDob != null) layoutPickDob.setOnClickListener(pickListener);
    }

    private void setupPasswordToggles() {
        btnRegisterTogglePassword.setOnClickListener(v -> {
            isRegisterPasswordVisible = !isRegisterPasswordVisible;
            if (isRegisterPasswordVisible) {
                etRegisterPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                btnRegisterTogglePassword.setImageResource(R.drawable.ic_eye_off_outline);
            } else {
                etRegisterPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                btnRegisterTogglePassword.setImageResource(R.drawable.ic_eye_outline);
            }
            etRegisterPassword.setSelection(etRegisterPassword.getText().length());
        });

        btnLoginTogglePassword.setOnClickListener(v -> {
            isLoginPasswordVisible = !isLoginPasswordVisible;
            if (isLoginPasswordVisible) {
                etLoginPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                btnLoginTogglePassword.setImageResource(R.drawable.ic_eye_off_outline);
            } else {
                etLoginPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                btnLoginTogglePassword.setImageResource(R.drawable.ic_eye_outline);
            }
            etLoginPassword.setSelection(etLoginPassword.getText().length());
        });
    }

    private void setupTermsAndFooter() {
        // Styled Terms and Privacy Policy text
        String termsHtml = "I agree to the <b><u>Terms</u></b> and <b><u>Privacy Policy</u></b>";
        tvTerms.setText(HtmlCompat.fromHtml(termsHtml, HtmlCompat.FROM_HTML_MODE_LEGACY));
        tvTerms.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Eventra Terms & Privacy")
                    .setMessage("By registering with Eventra, you agree that your institutional attendance, spot passes, and event duties are managed according to campus guidelines.\n\nAll data is restricted to authorized @eventra.ac.in members.")
                    .setPositiveButton("Understood", null)
                    .show();
        });

        // Underlined Forgot password link
        btnForgotPassword.setText(HtmlCompat.fromHtml("<u>Forgot password?</u>", HtmlCompat.FROM_HTML_MODE_LEGACY));
        btnForgotPassword.setOnClickListener(v -> showPasswordResetDialog());
    }

    private void setupActions() {
        btnCreateAccount.setOnClickListener(v -> performRegister());
        btnLoginSubmit.setOnClickListener(v -> performLogin());
    }

    private void performRegister() {
        String fullName = etRegisterFullName.getText().toString().trim();
        String rollNo = etRegisterRollNo.getText().toString().trim();
        String email = etRegisterEmail.getText().toString().trim().toLowerCase();
        String password = etRegisterPassword.getText().toString().trim();
        String phone = etRegisterPhone != null ? etRegisterPhone.getText().toString().trim() : "";
        String dob = etRegisterDob != null ? etRegisterDob.getText().toString().trim() : "";

        if (fullName.isEmpty()) {
            etRegisterFullName.setError("Please enter your full name");
            etRegisterFullName.requestFocus();
            return;
        }

        if (rollNo.isEmpty()) {
            etRegisterRollNo.setError("Please enter your roll number or staff ID");
            etRegisterRollNo.requestFocus();
            return;
        }

        // Email validation: must end with @eventra.ac.in
        if (email.isEmpty()) {
            etRegisterEmail.setError("Please enter your email");
            etRegisterEmail.requestFocus();
            return;
        }

        if (!email.contains("@")) {
            email = email + EVENTRA_DOMAIN;
            etRegisterEmail.setText(email);
        } else if (!email.endsWith(EVENTRA_DOMAIN)) {
            etRegisterEmail.setError("Must be an official " + EVENTRA_DOMAIN + " address");
            etRegisterEmail.requestFocus();
            Toast.makeText(this, "⚠️ Please use your official " + EVENTRA_DOMAIN + " account", Toast.LENGTH_LONG).show();
            return;
        }

        if (password.length() < 6) {
            etRegisterPassword.setError("Password must be at least 6 characters");
            etRegisterPassword.requestFocus();
            return;
        }

        if (!cbTerms.isChecked()) {
            Toast.makeText(this, "Please agree to the Terms and Privacy Policy to continue", Toast.LENGTH_SHORT).show();
            return;
        }

        String yearOrClass = "";
        String degree = "";
        String subjects = "";
        String department = "";

        if (User.INSTITUTION_HIGHER_SECONDARY.equals(selectedInstitutionType) || User.INSTITUTION_SCHOOL.equals(selectedInstitutionType)) {
            yearOrClass = etRegisterClass != null ? etRegisterClass.getText().toString().trim() : "";
            if (yearOrClass.isEmpty()) yearOrClass = "11th Standard";
            String stream = etRegisterStream != null ? etRegisterStream.getText().toString().trim() : "Science";
            department = stream.contains("Stream") ? stream : stream + " Stream";
            degree = "Higher Secondary Certificate (HSC)";
            subjects = etRegisterSubjectsSchool != null ? etRegisterSubjectsSchool.getText().toString().trim() : "";
        } else {
            degree = etRegisterDegree != null ? etRegisterDegree.getText().toString().trim() : "";
            if (degree.isEmpty()) degree = "Bachelor / Master Degree";
            yearOrClass = etRegisterYearCollege != null ? etRegisterYearCollege.getText().toString().trim() : "";
            if (yearOrClass.isEmpty()) yearOrClass = "1st Year";
            department = etRegisterDepartment != null ? etRegisterDepartment.getText().toString().trim() : "Academics";
            subjects = etRegisterSubjectsCollege != null ? etRegisterSubjectsCollege.getText().toString().trim() : "";
        }

        User registered = userManager.registerCleanUser(
                fullName, rollNo, email, password, selectedRole, 
                selectedInstitutionType, phone, dob, yearOrClass, degree, subjects, department
        );

        if (registered == null) {
            Toast.makeText(this, "An account with this email already exists. Please log in.", Toast.LENGTH_LONG).show();
            showLoginMode();
            etLoginEmail.setText(email);
            return;
        }

        Toast.makeText(this, "🎉 Welcome to Eventra, " + fullName + "!", Toast.LENGTH_SHORT).show();
        userManager.setCurrentUser(registered);
        goToHome(registered);
    }

    private void performLogin() {
        String identifier = etLoginEmail.getText().toString().trim();
        String password = etLoginPassword.getText().toString().trim();

        if (identifier.isEmpty()) {
            etLoginEmail.setError("Please enter your email or ID");
            etLoginEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etLoginPassword.setError("Please enter your password");
            etLoginPassword.requestFocus();
            return;
        }

        // If user typed name without domain, check and auto-append @eventra.ac.in
        if (!identifier.contains("@") && !identifier.matches(".*[0-9_].*")) {
            identifier = identifier + EVENTRA_DOMAIN;
            etLoginEmail.setText(identifier);
        }

        User user = userManager.authenticate(identifier, password);
        if (user != null) {
            userManager.setCurrentUser(user);
            Toast.makeText(this, "👋 Welcome back, " + user.getFullName() + "!", Toast.LENGTH_SHORT).show();
            goToHome(user);
        } else {
            Toast.makeText(this, "❌ Invalid credentials. Please verify your @eventra.ac.in email and password.", Toast.LENGTH_LONG).show();
        }
    }

    private void showPasswordResetDialog() {
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(60, 30, 60, 20);

        TextView tvInfo = new TextView(this);
        tvInfo.setText("Enter your registered @eventra.ac.in email or ID to choose a new password.");
        tvInfo.setTextColor(getColor(R.color.text_secondary));
        tvInfo.setTextSize(13);
        layout.addView(tvInfo);

        EditText etId = new EditText(this);
        etId.setHint("name@eventra.ac.in");
        String currentInput = etLoginEmail.getText().toString().trim();
        if (!currentInput.isEmpty()) {
            etId.setText(currentInput);
        }
        layout.addView(etId);

        EditText etCurrent = new EditText(this);
        etCurrent.setHint("Current / Default Password");
        etCurrent.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(etCurrent);

        EditText etNew = new EditText(this);
        etNew.setHint("New Secure Password");
        etNew.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(etNew);

        new AlertDialog.Builder(this)
                .setTitle("Reset Password")
                .setView(layout)
                .setPositiveButton("Update", (dialog, which) -> {
                    String id = etId.getText().toString().trim();
                    String currPass = etCurrent.getText().toString().trim();
                    String newPass = etNew.getText().toString().trim();

                    if (id.isEmpty() || currPass.isEmpty() || newPass.isEmpty()) {
                        Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    boolean success = userManager.resetPassword(id, currPass, newPass);
                    if (success) {
                        etLoginEmail.setText(id);
                        etLoginPassword.setText(newPass);
                        Toast.makeText(this, "✅ Password reset successfully! You can now sign in.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "❌ Verification failed. Check your ID/Email and password.", Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void goToHome(User user) {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish();
    }
}