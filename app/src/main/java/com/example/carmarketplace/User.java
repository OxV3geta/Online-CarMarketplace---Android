package com.example.carmarketplace;

public class User {
    private String uid;
    private String name;
    private String email;
    private String phone;
    private String location;
    private String accountType; // Buyer/Seller
    private String preference; // Buy Cars, Sell Cars, Both
    private String profileImageUrl;

    public User() {
        // Required for Firestore
    }

    public User(String uid, String name, String email, String phone, String location, String accountType, String preference, String profileImageUrl) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.location = location;
        this.accountType = accountType;
        this.preference = preference;
        this.profileImageUrl = profileImageUrl;
    }

    // Simplified constructor for existing logic if needed, but better to update all calls
    public User(String uid, String name, String email, String phone, String location, String accountType, String preference) {
        this(uid, name, email, phone, location, accountType, preference, "");
    }

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }

    public String getPreference() { return preference; }
    public void setPreference(String preference) { this.preference = preference; }

    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }
}
