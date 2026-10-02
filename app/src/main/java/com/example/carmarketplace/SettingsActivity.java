package com.example.carmarketplace;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class SettingsActivity extends AppCompatActivity {

    private SessionManager session;
    private LinearLayout themeLight, themeDark, themeSystem;
    private Switch pushSwitch, priceDropSwitch, messagesSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        session = new SessionManager(this);

        // UI Components
        findViewById(R.id.backButton).setOnClickListener(v -> finish());
        
        themeLight = findViewById(R.id.themeLight);
        themeDark = findViewById(R.id.themeDark);
        themeSystem = findViewById(R.id.themeSystem);
        
        pushSwitch = findViewById(R.id.pushSwitch);
        priceDropSwitch = findViewById(R.id.priceDropSwitch);
        messagesSwitch = findViewById(R.id.messagesSwitch);

        // Theme listeners
        themeLight.setOnClickListener(v -> applyTheme(AppCompatDelegate.MODE_NIGHT_NO));
        themeDark.setOnClickListener(v -> applyTheme(AppCompatDelegate.MODE_NIGHT_YES));
        themeSystem.setOnClickListener(v -> applyTheme(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM));

        // Switch listeners
        pushSwitch.setOnCheckedChangeListener((b, isChecked) -> session.setNotification("push", isChecked));
        priceDropSwitch.setOnCheckedChangeListener((b, isChecked) -> session.setNotification("price", isChecked));
        messagesSwitch.setOnCheckedChangeListener((b, isChecked) -> session.setNotification("messages", isChecked));

        // Account & Data
        findViewById(R.id.editProfileItem).setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileSetupActivity.class));
        });

        findViewById(R.id.clearCacheBtn).setOnClickListener(v -> {
            Toast.makeText(this, "Cache cleared successfully", Toast.LENGTH_SHORT).show();
        });

        // Logout
        findViewById(R.id.logoutButton).setOnClickListener(v -> {
            session.logout();
            Intent i = new Intent(this, WelcomeActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            finish();
        });

        loadSettings();
    }

    private void loadSettings() {
        int mode = session.getThemeMode();
        updateThemeUI(mode);

        pushSwitch.setChecked(session.isNotificationEnabled("push"));
        priceDropSwitch.setChecked(session.isNotificationEnabled("price"));
        messagesSwitch.setChecked(session.isNotificationEnabled("messages"));
    }

    private void applyTheme(int mode) {
        session.setTheme(mode);
        AppCompatDelegate.setDefaultNightMode(mode);
        updateThemeUI(mode);
        Toast.makeText(this, "Theme updated", Toast.LENGTH_SHORT).show();
    }

    private void updateThemeUI(int mode) {
        // Reset all
        resetThemeBox(themeLight);
        resetThemeBox(themeDark);
        resetThemeBox(themeSystem);

        if (mode == AppCompatDelegate.MODE_NIGHT_NO) {
            selectThemeBox(themeLight);
        } else if (mode == AppCompatDelegate.MODE_NIGHT_YES) {
            selectThemeBox(themeDark);
        } else {
            selectThemeBox(themeSystem);
        }
    }

    private void resetThemeBox(LinearLayout box) {
        box.setBackgroundResource(R.drawable.bg_input);
        for (int i = 0; i < box.getChildCount(); i++) {
            View child = box.getChildAt(i);
            if (child instanceof TextView) {
                ((TextView) child).setTextColor(getColor(R.color.text_primary));
            }
        }
    }

    private void selectThemeBox(LinearLayout box) {
        box.setBackgroundResource(R.drawable.bg_button); // Lime
        for (int i = 0; i < box.getChildCount(); i++) {
            View child = box.getChildAt(i);
            if (child instanceof TextView) {
                ((TextView) child).setTextColor(Color.BLACK);
                ((TextView) child).setTypeface(null, android.graphics.Typeface.BOLD);
            }
        }
    }
}
