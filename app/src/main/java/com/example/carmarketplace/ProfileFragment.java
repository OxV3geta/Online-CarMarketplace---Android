package com.example.carmarketplace;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class ProfileFragment extends Fragment {
    private EditText nameInput, emailInput, phoneInput;
    private CheckBox notificationsCheckbox;
    private ImageView profileImage;
    private String currentImageUrl = "";

    private final ActivityResultLauncher<String> mGetContent = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    processImageToBase64(uri);
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        profileImage = view.findViewById(R.id.profileImage);
        nameInput = view.findViewById(R.id.nameInput);
        emailInput = view.findViewById(R.id.emailInput);
        phoneInput = view.findViewById(R.id.phoneInput);
        notificationsCheckbox = view.findViewById(R.id.notificationsCheckbox);
        Button saveButton = view.findViewById(R.id.saveButton);
        View logoutButton = view.findViewById(R.id.logoutButton);

        SessionManager session = new SessionManager(requireContext());
        nameInput.setText(session.getName());
        emailInput.setText(session.getEmail());
        phoneInput.setText(session.getPhone());
        currentImageUrl = session.getProfileImageUrl();

        if (!currentImageUrl.isEmpty()) {
            Glide.with(this).load(currentImageUrl).into(profileImage);
        }

        profileImage.setOnClickListener(v -> mGetContent.launch("image/*"));
        view.findViewById(R.id.changePhotoText).setVisibility(View.VISIBLE);
        view.findViewById(R.id.changePhotoText).setOnClickListener(v -> mGetContent.launch("image/*"));

        saveButton.setOnClickListener(v -> {
            session.saveProfile(
                nameInput.getText().toString().trim(),
                emailInput.getText().toString().trim(),
                phoneInput.getText().toString().trim(),
                session.getLocation(),
                session.getAccountType(),
                session.getPreference(),
                currentImageUrl
            );
            
            updateFirestore(session);
        });

        logoutButton.setOnClickListener(v -> {
            session.logout();
            Intent intent = new Intent(requireContext(), LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            requireActivity().finish();
        });

        return view;
    }

    private void processImageToBase64(Uri uri) {
        try {
            InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
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
            currentImageUrl = "data:image/jpeg;base64," + Base64.encodeToString(byteArray, Base64.DEFAULT);
            
            Glide.with(this).load(currentImageUrl).into(profileImage);
            Toast.makeText(requireContext(), "Image updated locally", Toast.LENGTH_SHORT).show();

            // Auto-save to session/cloud after photo change
            SessionManager session = new SessionManager(requireContext());
            session.saveProfile(
                    nameInput.getText().toString(),
                    emailInput.getText().toString(),
                    phoneInput.getText().toString(),
                    session.getLocation(),
                    session.getAccountType(),
                    session.getPreference(),
                    currentImageUrl
            );
            updateFirestore(session);

        } catch (Exception e) {
            Toast.makeText(requireContext(), "Image processing failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateFirestore(SessionManager session) {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        User user = new User(uid, session.getName(), session.getEmail(), session.getPhone(), 
                session.getLocation(), session.getAccountType(), session.getPreference(), currentImageUrl);

        FirebaseFirestore.getInstance().collection("users").document(uid).set(user)
                .addOnSuccessListener(aVoid -> Toast.makeText(requireContext(), "Profile updated in cloud", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(requireContext(), "Cloud update failed", Toast.LENGTH_SHORT).show());
    }
}
