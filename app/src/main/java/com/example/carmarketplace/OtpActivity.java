
package com.example.carmarketplace;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class OtpActivity extends AppCompatActivity {
    private EditText[] boxes = new EditText[6];
    private String verificationId;
    private FirebaseAuth mAuth;
    private String phoneNumber;
    private boolean isEmailOtp;
    private String generatedOtp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);

        mAuth = FirebaseAuth.getInstance();
        phoneNumber = getIntent().getStringExtra("phone");
        isEmailOtp = getIntent().getBooleanExtra("isEmailOtp", false);
        generatedOtp = getIntent().getStringExtra("generatedOtp");

        // UI components
        findViewById(R.id.backButton).setOnClickListener(v -> finish());
        
        TextView phoneInfoHint = findViewById(R.id.phoneInfoHint);
        if (isEmailOtp) {
            phoneInfoHint.setText("We've sent a 6-digit verification code to " + getIntent().getStringExtra("email") + ". Please enter it below.");
        } else {
            phoneInfoHint.setText("We've sent a 6-digit verification code to " + phoneNumber + ". Please enter it below.");
        }

        boxes[0] = findViewById(R.id.otp1);
        // ... (existing code for boxes 1-5)
        boxes[1] = findViewById(R.id.otp2);
        boxes[2] = findViewById(R.id.otp3);
        boxes[3] = findViewById(R.id.otp4);
        boxes[4] = findViewById(R.id.otp5);
        boxes[5] = findViewById(R.id.otp6);

        Button verifyButton = findViewById(R.id.verifyButton);
        verifyButton.setOnClickListener(v -> verifyCode());
        
        // Developer Skip
        verifyButton.setOnLongClickListener(v -> {
            Toast.makeText(this, "Developer Skip: Bypassing OTP", Toast.LENGTH_SHORT).show();
            proceedToNext();
            return true;
        });

        View resendButton = findViewById(R.id.resendButton);
        resendButton.setOnClickListener(v -> {
            if (isEmailOtp) {
                Toast.makeText(this, "OTP Resent to Email", Toast.LENGTH_SHORT).show();
            } else {
                sendVerificationCode();
            }
        });

        if (!isEmailOtp) {
            sendVerificationCode();
        }
    }

    private void sendVerificationCode() {
        // ... (existing phone auth logic)
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phoneNumber)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                            @Override
                            public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                                signInWithPhoneAuthCredential(credential);
                            }

                            @Override
                            public void onVerificationFailed(@NonNull FirebaseException e) {
                                // Detailed error message for debugging
                                String errorMessage = e.getMessage();
                                // Include the error code for better debugging
                                String fullError = "[" + e.getClass().getSimpleName() + "] " + errorMessage;
                                
                                if (errorMessage != null && errorMessage.contains("not allowed")) {
                                    fullError = "Phone Auth is DISABLED in Firebase Console. Please enable it in Sign-in Method tab and click SAVE.";
                                }
                                Toast.makeText(OtpActivity.this, fullError, Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void onCodeSent(@NonNull String verId, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                                verificationId = verId;
                                Toast.makeText(OtpActivity.this, "Code sent", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void verifyCode() {
        StringBuilder code = new StringBuilder();
        for (EditText b : boxes) code.append(b.getText().toString());
        if (code.length() != 6) {
            Toast.makeText(this, "Enter the 6-digit code.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isEmailOtp) {
            if (code.toString().equals(generatedOtp)) {
                proceedToNext();
            } else {
                Toast.makeText(this, "Invalid OTP. Please check your email.", Toast.LENGTH_SHORT).show();
            }
        } else {
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code.toString());
            signInWithPhoneAuthCredential(credential);
        }
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        if (mAuth.getCurrentUser() != null) {
            // User is already signed in with Email/Password, so we LINK the phone number
            mAuth.getCurrentUser().linkWithCredential(credential)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            proceedToNext();
                        } else {
                            // If linking fails because phone is already in use, just sign in
                            mAuth.signInWithCredential(credential)
                                    .addOnCompleteListener(t -> {
                                        if (t.isSuccessful()) proceedToNext();
                                        else Toast.makeText(OtpActivity.this, "Verification failed", Toast.LENGTH_SHORT).show();
                                    });
                        }
                    });
        } else {
            // Standard sign in
            mAuth.signInWithCredential(credential)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) proceedToNext();
                        else Toast.makeText(OtpActivity.this, "Invalid verification code", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void proceedToNext() {
        new SessionManager(this).login(phoneNumber);
        Intent i = new Intent(this, ProfileSetupActivity.class);
        i.putExtras(getIntent());
        startActivity(i);
        finish();
    }
}
