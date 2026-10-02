
package com.example.carmarketplace;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class WelcomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SessionManager session = new SessionManager(this);
        int mode = session.getThemeMode();
        if (mode != -1) {
            AppCompatDelegate.setDefaultNightMode(mode);
        }

        if (session.isLoggedIn()) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_welcome);

        Button getStartedButton = findViewById(R.id.getStartedButton);
        TextView loginHint = findViewById(R.id.loginHint);

        View.OnClickListener openLogin = v ->
                startActivity(new Intent(this, LoginActivity.class));

        getStartedButton.setOnClickListener(openLogin);
        loginHint.setOnClickListener(openLogin);
    }
}
