# Add "Forgot Password" Feature

Implement a password reset flow using Firebase Authentication. This includes adding a link on the login screen, creating a new activity to request the reset email, and handling the Firebase logic.

## User Review Required

> [!IMPORTANT]
> The "Forgot Password" feature relies on Firebase Authentication. Ensure that the project is correctly configured with Firebase and that the `google-services.json` file is valid.

## Proposed Changes

### UI Components

#### [MODIFY] [activity_login.xml](file:///media/oxv3geta/Work/Car project Final/other feature/CarMarketplace_Android_Java_Figma_preview fix/app/src/main/res/layout/activity_login.xml)
- Add a "Forgot Password?" TextView below the password field.

#### [NEW] [activity_forgot_password.xml](file:///media/oxv3geta/Work/Car project Final/other feature/CarMarketplace_Android_Java_Figma_preview fix/app/src/main/res/layout/activity_forgot_password.xml)
- Create a layout with an email input field and a "Send Reset Link" button.
- Include a back button to return to the login screen.

### App Logic

#### [MODIFY] [LoginActivity.java](file:///media/oxv3geta/Work/Car project Final/other feature/CarMarketplace_Android_Java_Figma_preview fix/app/src/main/java/com/example/carmarketplace/LoginActivity.java)
- Initialize the "Forgot Password?" link.
- Set a click listener to navigate to `ForgotPasswordActivity`.

#### [NEW] [ForgotPasswordActivity.java](file:///media/oxv3geta/Work/Car project Final/other feature/CarMarketplace_Android_Java_Figma_preview fix/app/src/main/java/com/example/carmarketplace/ForgotPasswordActivity.java)
- Handle the password reset logic using `FirebaseAuth.getInstance().sendPasswordResetEmail(email)`.
- Show success/failure feedback to the user via Toast.

### Configuration

#### [MODIFY] [AndroidManifest.xml](file:///media/oxv3geta/Work/Car project Final/other feature/CarMarketplace_Android_Java_Figma_preview fix/app/src/main/AndroidManifest.xml)
- Register `ForgotPasswordActivity`.

## Verification Plan

### Manual Verification
1.  Navigate to the Login screen.
2.  Click on "Forgot Password?".
3.  Enter a registered email address and click "Send Reset Link".
4.  Verify that a success message is shown and an email is received (if possible).
5.  Enter an invalid/unregistered email and verify that an error message is shown.
