package com.example.carmarketplace;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class VerifyEmailActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private String email;
    private String name;
    private String phone;
    private String accountType;
    private Button verifyStatusButton;
    private Button resendEmailButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_email);

        mAuth = FirebaseAuth.getInstance();

        // Get intent extras
        email = getIntent().getStringExtra("email");
        name = getIntent().getStringExtra("name");
        phone = getIntent().getStringExtra("phone");
        accountType = getIntent().getStringExtra("accountType");

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (email == null || email.isEmpty()) {
            if (currentUser != null && currentUser.getEmail() != null) {
                email = currentUser.getEmail();
            } else {
                email = "";
            }
        }

        TextView backButton = findViewById(R.id.backButton);
        TextView userEmailText = findViewById(R.id.userEmailText);
        verifyStatusButton = findViewById(R.id.verifyStatusButton);
        resendEmailButton = findViewById(R.id.resendEmailButton);
        TextView loginLink = findViewById(R.id.loginLink);

        userEmailText.setText(email.isEmpty() ? "your email" : email);

        backButton.setOnClickListener(v -> goToLogin());
        loginLink.setOnClickListener(v -> goToLogin());

        verifyStatusButton.setOnClickListener(v -> checkVerificationStatus());

        // Developer skip option on long press
        verifyStatusButton.setOnLongClickListener(v -> {
            Toast.makeText(this, "Developer Skip: Bypassing Email Verification", Toast.LENGTH_SHORT).show();
            proceedAfterVerification();
            return true;
        });

        resendEmailButton.setOnClickListener(v -> resendVerificationEmail());
    }

    private void checkVerificationStatus() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "No active user session. Please log in again.", Toast.LENGTH_SHORT).show();
            goToLogin();
            return;
        }

        verifyStatusButton.setEnabled(false);
        user.reload().addOnCompleteListener(task -> {
            verifyStatusButton.setEnabled(true);
            if (task.isSuccessful()) {
                FirebaseUser updatedUser = mAuth.getCurrentUser();
                if (updatedUser != null && updatedUser.isEmailVerified()) {
                    Toast.makeText(VerifyEmailActivity.this, "Email verified successfully!", Toast.LENGTH_SHORT).show();
                    proceedAfterVerification();
                } else {
                    Toast.makeText(VerifyEmailActivity.this, "Email is not verified yet. Please check your inbox and Spam folder.", Toast.LENGTH_LONG).show();
                }
            } else {
                String errorMsg = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                Toast.makeText(VerifyEmailActivity.this, "Failed to check status: " + errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void resendVerificationEmail() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Session expired. Please log in again.", Toast.LENGTH_SHORT).show();
            goToLogin();
            return;
        }

        resendEmailButton.setEnabled(false);
        user.sendEmailVerification().addOnCompleteListener(task -> {
            resendEmailButton.setEnabled(true);
            if (task.isSuccessful()) {
                Toast.makeText(VerifyEmailActivity.this, "Verification email sent again! Check your inbox and Spam folder.", Toast.LENGTH_LONG).show();
            } else {
                String error = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                Toast.makeText(VerifyEmailActivity.this, "Failed to resend: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void proceedAfterVerification() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            goToLogin();
            return;
        }

        String uid = user.getUid();
        FirebaseFirestore.getInstance().collection("users").document(uid).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                        User existingUser = task.getResult().toObject(User.class);
                        if (existingUser != null && existingUser.getName() != null && !existingUser.getName().isEmpty()) {
                            new SessionManager(this).login(existingUser.getEmail());
                            new SessionManager(this).saveProfile(existingUser);
                            Intent intent = new Intent(this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                            return;
                        }
                    }

                    // Proceed to profile setup
                    Intent i = new Intent(this, ProfileSetupActivity.class);
                    i.putExtra("name", name);
                    i.putExtra("email", email);
                    i.putExtra("phone", phone);
                    i.putExtra("accountType", accountType);
                    startActivity(i);
                    finish();
                });
    }

    private void goToLogin() {
        mAuth.signOut();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
