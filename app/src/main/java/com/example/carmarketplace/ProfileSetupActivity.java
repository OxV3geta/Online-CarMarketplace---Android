
package com.example.carmarketplace;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class ProfileSetupActivity extends AppCompatActivity {
    private EditText nameInput, emailInput, phoneInput, locationInput;
    private ImageView avatarImage;
    private Uri imageUri;
    private String profileImageUrl = "";

    private final ActivityResultLauncher<String> mGetContent = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    imageUri = uri;
                    avatarImage.setImageURI(uri);
                    findViewById(R.id.uploadAvatar).setVisibility(View.GONE);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_setup);

        // UI components
        findViewById(R.id.backButton).setOnClickListener(v -> finish());
        
        nameInput = findViewById(R.id.nameInput);
        emailInput = findViewById(R.id.emailInput);
        phoneInput = findViewById(R.id.phoneInput);
        locationInput = findViewById(R.id.locationInput);
        avatarImage = findViewById(R.id.avatarImage);
        Button continueButton = findViewById(R.id.continueButton);

        // Fill data from intent
        nameInput.setText(getIntent().getStringExtra("name"));
        emailInput.setText(getIntent().getStringExtra("email"));
        phoneInput.setText(getIntent().getStringExtra("phone"));

        findViewById(R.id.uploadAvatar).setOnClickListener(v -> 
            mGetContent.launch("image/*")
        );
        avatarImage.setOnClickListener(v -> mGetContent.launch("image/*"));

        continueButton.setOnClickListener(v -> {
            if (validate()) {
                if (imageUri != null) {
                    processImageToBase64();
                } else {
                    navigateToPreference();
                }
            }
        });
    }

    private void processImageToBase64() {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            
            // Resize to keep Firestore document size small (Max 400x400)
            int maxSize = 400;
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            if (width > maxSize || height > maxSize) {
                float ratio = (float) width / height;
                if (ratio > 1) {
                    width = maxSize;
                    height = (int) (maxSize / ratio);
                } else {
                    height = maxSize;
                    width = (int) (maxSize * ratio);
                }
                bitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 60, outputStream);
            byte[] byteArray = outputStream.toByteArray();
            profileImageUrl = "data:image/jpeg;base64," + Base64.encodeToString(byteArray, Base64.DEFAULT);
            
            navigateToPreference();
        } catch (Exception e) {
            Toast.makeText(this, "Image processing failed", Toast.LENGTH_SHORT).show();
            navigateToPreference();
        }
    }

    private void navigateToPreference() {
        Intent i = new Intent(this, PreferenceActivity.class);
        i.putExtras(getIntent());
        i.putExtra("name", nameInput.getText().toString());
        i.putExtra("email", emailInput.getText().toString());
        i.putExtra("phone", phoneInput.getText().toString());
        i.putExtra("location", locationInput.getText().toString());
        i.putExtra("profileImageUrl", profileImageUrl);
        startActivity(i);
    }

    private boolean validate() {
        if (nameInput.getText().toString().trim().isEmpty()) {
            nameInput.setError("Enter your name");
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(emailInput.getText().toString()).matches()) {
            emailInput.setError("Enter a valid email");
            return false;
        }
        return true;
    }
}
