
package com.example.carmarketplace;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 9001;
    private EditText emailInput;
    private EditText passwordInput;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (new SessionManager(this).isLoggedIn()) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Initialize UI components
        View backButton = findViewById(R.id.backButton);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        Button loginButton = findViewById(R.id.loginButton);
        Button googleLoginButton = findViewById(R.id.googleLoginButton);
        View signUpLink = findViewById(R.id.signUpLink);
        View forgotPasswordLink = findViewById(R.id.forgotPasswordLink);

        // Listeners
        backButton.setOnClickListener(v -> finish());
        loginButton.setOnClickListener(v -> doLogin());
        googleLoginButton.setOnClickListener(v -> signInWithGoogle());
        signUpLink.setOnClickListener(v -> startActivity(new Intent(this, SignupActivity.class)));
        forgotPasswordLink.setOnClickListener(v -> startActivity(new Intent(this, ForgotPasswordActivity.class)));
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
                    if (task.isSuccessful()) {
                        fetchProfile(mAuth.getCurrentUser().getUid());
                    } else {
                        Toast.makeText(this, "Authentication Failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchProfile(String uid) {
        FirebaseFirestore.getInstance().collection("users").document(uid).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().exists()) {
                        User user = task.getResult().toObject(User.class);
                        if (user != null) {
                            new SessionManager(this).login(user.getEmail());
                            new SessionManager(this).saveProfile(user);
                            startActivity(new Intent(this, MainActivity.class));
                            finish();
                        }
                    } else {
                        // Profile missing but authenticated - redirect to profile setup
                        Intent i = new Intent(this, ProfileSetupActivity.class);
                        i.putExtra("name", mAuth.getCurrentUser().getDisplayName());
                        i.putExtra("email", mAuth.getCurrentUser().getEmail());
                        startActivity(i);
                        finish();
                    }
                });
    }

    private void doLogin() {
        String email = normalizeEmail(emailInput.getText().toString().trim());
        String password = passwordInput.getText().toString();

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Enter a valid email address");
            emailInput.requestFocus();
            return;
        }

        if (password.length() < 6) {
            passwordInput.setError("Password must be at least 6 characters");
            passwordInput.requestFocus();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        if (mAuth.getCurrentUser() != null && mAuth.getCurrentUser().isEmailVerified()) {
                            fetchProfile(mAuth.getCurrentUser().getUid());
                        } else {
                            Toast.makeText(this, "Please verify your email first.", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(this, VerifyEmailActivity.class);
                            intent.putExtra("email", email);
                            startActivity(intent);
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private String normalizeEmail(String value) {
        if (value.startsWith("[") && value.contains("]")) {
            int end = value.indexOf(']');
            String inside = value.substring(1, end);
            if (Patterns.EMAIL_ADDRESS.matcher(inside).matches()) return inside;
        }
        if (value.startsWith("mailto:")) return value.substring(7);
        int mailto = value.indexOf("mailto:");
        if (mailto >= 0) {
            String rest = value.substring(mailto + 7);
            int close = rest.indexOf(')');
            if (close >= 0) rest = rest.substring(0, close);
            return rest.replace("(", "").replace("[", "").replace("]", "").trim();
        }
        return value;
    }
}
