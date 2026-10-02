
package com.example.carmarketplace;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class SignupActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 9001;
    private EditText fullNameInput, emailInput, phoneInput, passwordInput;
    private CheckBox termsCheckbox;
    private String accountType = "Buyer";
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Initialize UI components
        View backButton = findViewById(R.id.backButton);
        TextView buyerSegment = findViewById(R.id.buyerSegment);
        TextView sellerSegment = findViewById(R.id.sellerSegment);
        fullNameInput = findViewById(R.id.fullNameInput);
        emailInput = findViewById(R.id.emailInput);
        phoneInput = findViewById(R.id.phoneInput);
        passwordInput = findViewById(R.id.passwordInput);
        termsCheckbox = findViewById(R.id.termsCheckbox);
        Button signUpButton = findViewById(R.id.signUpButton);
        View loginLink = findViewById(R.id.loginLink);
        Button googleSignupButton = findViewById(R.id.googleSignupButton);

        // Back button
        backButton.setOnClickListener(v -> finish());

        // Account Type Selector
        buyerSegment.setOnClickListener(v -> {
            accountType = "Buyer";
            buyerSegment.setBackgroundResource(R.drawable.bg_button);
            buyerSegment.setTextColor(getColor(R.color.dark_bg));
            buyerSegment.setTypeface(null, android.graphics.Typeface.BOLD);
            sellerSegment.setBackground(null);
            sellerSegment.setTextColor(getColor(R.color.text_secondary));
            sellerSegment.setTypeface(null, android.graphics.Typeface.NORMAL);
        });

        sellerSegment.setOnClickListener(v -> {
            accountType = "Seller";
            sellerSegment.setBackgroundResource(R.drawable.bg_button);
            sellerSegment.setTextColor(getColor(R.color.dark_bg));
            sellerSegment.setTypeface(null, android.graphics.Typeface.BOLD);
            buyerSegment.setBackground(null);
            buyerSegment.setTextColor(getColor(R.color.text_secondary));
            buyerSegment.setTypeface(null, android.graphics.Typeface.NORMAL);
        });

        // Initialize with Buyer selected
        buyerSegment.performClick();

        // Sign Up button
        signUpButton.setOnClickListener(v -> createAccount());

        googleSignupButton.setOnClickListener(v -> signInWithGoogle());

        // Login link
        loginLink.setOnClickListener(v -> finish());
    }

    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Toast.makeText(this, "Google sign in failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful() && mAuth.getCurrentUser() != null) {
                        checkGoogleUserAndVerify(mAuth.getCurrentUser());
                    } else {
                        Toast.makeText(this, "Google Sign In Failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkGoogleUserAndVerify(com.google.firebase.auth.FirebaseUser googleUser) {
        String uid = googleUser.getUid();
        String name = googleUser.getDisplayName() != null ? googleUser.getDisplayName() : "";
        String email = googleUser.getEmail() != null ? googleUser.getEmail() : "";

        FirebaseFirestore.getInstance().collection("users").document(uid).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                        User user = task.getResult().toObject(User.class);
                        if (user != null && googleUser.isEmailVerified()) {
                            new SessionManager(this).login(user.getEmail());
                            new SessionManager(this).saveProfile(user);
                            startActivity(new Intent(this, MainActivity.class));
                            finish();
                            return;
                        }
                    }

                    // Send Email Verification for Google Sign In Registration
                    googleUser.sendEmailVerification().addOnCompleteListener(verifyTask -> {
                        if (verifyTask.isSuccessful()) {
                            Toast.makeText(this, "Verification email sent to " + email + ". Check inbox & spam folder!", Toast.LENGTH_LONG).show();
                        } else {
                            String err = verifyTask.getException() != null ? verifyTask.getException().getMessage() : "Unknown error";
                            Toast.makeText(this, "Verification note: " + err, Toast.LENGTH_SHORT).show();
                        }
                    });

                    // Save initial user profile
                    User newUser = new User(uid, name, email, "", "", accountType, "");
                    FirebaseFirestore.getInstance().collection("users").document(uid).set(newUser);

                    // Redirect to VerifyEmailActivity
                    Intent intent = new Intent(this, VerifyEmailActivity.class);
                    intent.putExtra("email", email);
                    intent.putExtra("name", name);
                    intent.putExtra("accountType", accountType);
                    startActivity(intent);
                    finish();
                });
    }

    private void createAccount() {
        String n = fullNameInput.getText().toString().trim();
        String e = emailInput.getText().toString().trim();
        String p = passwordInput.getText().toString();
        String phInput = phoneInput.getText().toString().trim();

        if (n.isEmpty()) { fullNameInput.setError("Enter your name"); return; }
        if (!Patterns.EMAIL_ADDRESS.matcher(e).matches()) { emailInput.setError("Enter a valid email"); return; }
        if (phInput.length() < 7) { phoneInput.setError("Enter a phone number"); return; }
        if (p.length() < 8) { passwordInput.setError("Use at least 8 characters"); return; }
        if (!termsCheckbox.isChecked()) {
            Toast.makeText(this, "Please accept the Terms and Privacy Policy", Toast.LENGTH_SHORT).show();
            return;
        }

        // Format phone number to international E.164 format (e.g., +88017...)
        String finalPhone = phInput;
        if (!finalPhone.startsWith("+")) {
            if (finalPhone.startsWith("0")) {
                finalPhone = "+88" + finalPhone;
            } else {
                finalPhone = "+880" + finalPhone;
            }
        }

        String ph = finalPhone;
        mAuth.createUserWithEmailAndPassword(e, p)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful() && mAuth.getCurrentUser() != null) {
                        String uid = mAuth.getCurrentUser().getUid();
                        // Save initial profile data in Firestore
                        User user = new User(uid, n, e, ph, "", accountType, "");
                        FirebaseFirestore.getInstance().collection("users").document(uid).set(user);

                        // Send Firebase Verification Email
                        mAuth.getCurrentUser().sendEmailVerification()
                                .addOnCompleteListener(verifyTask -> {
                                    if (verifyTask.isSuccessful()) {
                                        Toast.makeText(this, "Verification email sent to " + e + ". Check your inbox & spam folder!", Toast.LENGTH_LONG).show();
                                    } else {
                                        String err = verifyTask.getException() != null ? verifyTask.getException().getMessage() : "Unknown error";
                                        Toast.makeText(this, "Verification email error: " + err, Toast.LENGTH_LONG).show();
                                    }
                                });

                        // Open VerifyEmailActivity so user can confirm verification or resend email
                        Intent intent = new Intent(this, VerifyEmailActivity.class);
                        intent.putExtra("email", e);
                        intent.putExtra("name", n);
                        intent.putExtra("phone", ph);
                        intent.putExtra("accountType", accountType);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(SignupActivity.this, "Signup failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
