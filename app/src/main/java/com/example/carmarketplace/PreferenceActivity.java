
package com.example.carmarketplace;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class PreferenceActivity extends AppCompatActivity {
    private String preference = "Buy Cars";
    private TextView optionBuy, optionSell, optionBoth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preference);

        // UI components
        findViewById(R.id.backButton).setOnClickListener(v -> finish());
        
        optionBuy = findViewById(R.id.optionBuy);
        optionSell = findViewById(R.id.optionSell);
        optionBoth = findViewById(R.id.optionBoth);
        Button saveButton = findViewById(R.id.saveButton);

        optionBuy.setOnClickListener(v -> select(optionBuy, "Buy Cars"));
        optionSell.setOnClickListener(v -> select(optionSell, "Sell Cars"));
        optionBoth.setOnClickListener(v -> select(optionBoth, "Both"));

        // Initialize selection
        select(optionBuy, "Buy Cars");

        saveButton.setOnClickListener(v -> saveProfile());
    }

    private void saveProfile() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        String name = getIntent().getStringExtra("name");
        String email = getIntent().getStringExtra("email");
        String phone = getIntent().getStringExtra("phone");
        String location = getIntent().getStringExtra("location");
        String accountType = getIntent().getStringExtra("accountType");
        String profileImageUrl = getIntent().getStringExtra("profileImageUrl");

        User user = new User(uid, name, email, phone, location, accountType, preference, profileImageUrl);

        FirebaseFirestore.getInstance().collection("users").document(uid)
                .set(user)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        SessionManager session = new SessionManager(this);
                        session.login(email);
                        session.saveProfile(user);

                        Intent i = new Intent(this, MainActivity.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(i);
                        finish();
                    } else {
                        Toast.makeText(this, "Error saving profile: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void select(TextView selected, String value) {
        preference = value;
        optionBuy.setBackgroundResource(R.drawable.bg_input);
        optionSell.setBackgroundResource(R.drawable.bg_input);
        optionBoth.setBackgroundResource(R.drawable.bg_input);
        
        optionBuy.setTextColor(getColor(R.color.text_primary));
        optionSell.setTextColor(getColor(R.color.text_primary));
        optionBoth.setTextColor(getColor(R.color.text_primary));

        selected.setBackgroundResource(R.drawable.bg_button);
        selected.setTextColor(Color.BLACK);
    }
}
