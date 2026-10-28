package com.example.sams;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
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

public class MainActivity extends AppCompatActivity {

    private static final String EVENTRA_DOMAIN = "@ycasc";

    // Login Form Views
    private EditText etLoginEmail;
    private EditText etLoginPassword;
    private ImageView btnLoginTogglePassword;
    private MaterialButton btnLoginSubmit;


    private boolean isLoginPasswordVisible = false;
    private UserManager userManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Keep system navigation bars visible
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
        setupListeners();
    }

    private void initViews() {
        etLoginEmail = findViewById(R.id.etLoginEmail);
        etLoginPassword = findViewById(R.id.etLoginPassword);
        btnLoginTogglePassword = findViewById(R.id.btnLoginTogglePassword);
        btnLoginSubmit = findViewById(R.id.btnLoginSubmit);

    }

    private void setupListeners() {
        // Password Visibility Toggle
        if (btnLoginTogglePassword != null) {
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



        // Login Submit
        if (btnLoginSubmit != null) {
            btnLoginSubmit.setOnClickListener(v -> performLogin());
        }
    }

    private void performLogin() {
        String identifier = etLoginEmail.getText().toString().trim();
        String password = etLoginPassword.getText().toString().trim();

        if (identifier.isEmpty()) {
            etLoginEmail.setError("Please enter your email or UserID");
            etLoginEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etLoginPassword.setError("Please enter your password");
            etLoginPassword.requestFocus();
            return;
        }

        // Auto-append @ycasc if username was typed without domain
        if (!identifier.contains("@") && !identifier.matches(".*[0-9_].*")) {
            identifier = identifier + EVENTRA_DOMAIN;
            etLoginEmail.setText(identifier);
        }

        User user = userManager.authenticate(identifier, password);
        if (user != null) {
            userManager.setCurrentUser(user);

            // Flow 1: If user hasn't changed default password yet, prompt password change first
            if (!user.isPrincipal() && !user.isPasswordChanged()) {
                showMandatoryChangePasswordDialog(user);
            } else {
                Toast.makeText(this, "👋 Welcome, " + user.getFullName() + "!", Toast.LENGTH_SHORT).show();
                goToHome(user);
            }
        } else {
            Toast.makeText(this, "❌ Invalid credentials. Please verify your email / UserID and password.", Toast.LENGTH_LONG).show();
        }
    }

    private void showMandatoryChangePasswordDialog(User user) {
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(60, 30, 60, 20);

        TextView tvInfo = new TextView(this);
        tvInfo.setText("🔐 Security Notice:\nYou are currently using the initial default password. Please choose a new secure personal password to continue.");
        tvInfo.setTextColor(getColor(R.color.charcoal_dark));
        tvInfo.setTextSize(13.5f);
        tvInfo.setPadding(0, 0, 0, 16);
        layout.addView(tvInfo);

        EditText etNew = new EditText(this);
        etNew.setHint("New Personal Password *");
        etNew.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(etNew);

        EditText etConfirm = new EditText(this);
        etConfirm.setHint("Confirm New Password *");
        etConfirm.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(etConfirm);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Change Initial Password")
                .setView(layout)
                .setCancelable(false)
                .setPositiveButton("Save Password", null)
                .create();

        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String p1 = etNew.getText().toString().trim();
                String p2 = etConfirm.getText().toString().trim();

                if (p1.isEmpty() || p1.length() < 4) {
                    etNew.setError("Password must be at least 4 characters");
                    return;
                }
                if (!p1.equals(p2)) {
                    etConfirm.setError("Passwords do not match");
                    return;
                }

                user.setPassword(p1);
                user.setPasswordChanged(true);
                userManager.updateUser(user);
                Toast.makeText(this, "✅ Password updated successfully!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();

                goToHome(user);
            });
        });

        dialog.show();
    }

    private void showMandatoryCompleteProfileDialog(User user) {
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(60, 30, 60, 20);

        TextView tvInfo = new TextView(this);
        tvInfo.setText("📋 Complete Your Profile:\nPlease provide your Address, Phone Number, and Date of Birth to unlock all app features.");
        tvInfo.setTextColor(getColor(R.color.charcoal_dark));
        tvInfo.setTextSize(13.5f);
        tvInfo.setPadding(0, 0, 0, 16);
        layout.addView(tvInfo);

        EditText etPhone = new EditText(this);
        etPhone.setHint("Phone Number *");
        etPhone.setInputType(InputType.TYPE_CLASS_PHONE);
        if (user.getPhoneNumber() != null) etPhone.setText(user.getPhoneNumber());
        layout.addView(etPhone);

        EditText etAddress = new EditText(this);
        etAddress.setHint("Residential Address *");
        etAddress.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS);
        if (user.getAddress() != null) etAddress.setText(user.getAddress());
        layout.addView(etAddress);

        EditText etDob = new EditText(this);
        etDob.setHint("Date of Birth (DD/MM/YYYY) *");
        if (user.getDob() != null) etDob.setText(user.getDob());
        layout.addView(etDob);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Complete Profile")
                .setView(layout)
                .setCancelable(false)
                .setPositiveButton("Complete Setup", null)
                .create();

        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String phone = etPhone.getText().toString().trim();
                String address = etAddress.getText().toString().trim();
                String dob = etDob.getText().toString().trim();

                if (phone.isEmpty()) {
                    etPhone.setError("Phone number is required");
                    return;
                }
                if (address.isEmpty()) {
                    etAddress.setError("Address is required");
                    return;
                }
                if (dob.isEmpty()) {
                    etDob.setError("Date of birth is required");
                    return;
                }

                user.setPhoneNumber(phone);
                user.setAddress(address);
                user.setDob(dob);
                userManager.updateUser(user);

                Toast.makeText(this, "Profile setup complete!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                goToHome(user);
            });
        });

        dialog.show();
    }


    private void goToHome(User user) {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
}