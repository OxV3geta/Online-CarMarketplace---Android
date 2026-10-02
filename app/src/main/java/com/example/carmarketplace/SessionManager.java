package com.example.carmarketplace;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF = "car_market_session";
    private static final String LOGGED_IN = "logged_in";
    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(LOGGED_IN, false);
    }

    public void login(String email) {
        prefs.edit().putBoolean(LOGGED_IN, true).putString("email", email).apply();
    }

    public void logout() {
        prefs.edit().clear().apply();
    }

    public String getEmail() {
        return prefs.getString("email", "");
    }

    public void saveProfile(String name, String email, String phone, String location, String accountType, String preference, String profileImageUrl) {
        prefs.edit()
                .putString("name", name)
                .putString("email", email)
                .putString("phone", phone)
                .putString("location", location)
                .putString("accountType", accountType)
                .putString("preference", preference)
                .putString("profileImageUrl", profileImageUrl)
                .apply();
    }

    public void saveProfile(User user) {
        saveProfile(user.getName(), user.getEmail(), user.getPhone(), user.getLocation(), user.getAccountType(), user.getPreference(), user.getProfileImageUrl());
    }

    public String getName() {
        return prefs.getString("name", "");
    }

    public String getPhone() {
        return prefs.getString("phone", "");
    }

    public String getLocation() {
        return prefs.getString("location", "");
    }

    public String getAccountType() {
        return prefs.getString("accountType", "Buyer");
    }

    public String getPreference() {
        return prefs.getString("preference", "Buy Cars");
    }

    public String getProfileImageUrl() {
        return prefs.getString("profileImageUrl", "");
    }

    // New Settings Preferences
    public void setTheme(int mode) {
        prefs.edit().putInt("theme_mode", mode).apply();
    }

    public int getThemeMode() {
        return prefs.getInt("theme_mode", -1); // -1 = System default
    }

    public void setNotification(String key, boolean enabled) {
        prefs.edit().putBoolean("notif_" + key, enabled).apply();
    }

    public boolean isNotificationEnabled(String key) {
        return prefs.getBoolean("notif_" + key, true);
    }
}
