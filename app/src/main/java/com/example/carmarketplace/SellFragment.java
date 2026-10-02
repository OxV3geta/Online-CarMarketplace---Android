
package com.example.carmarketplace;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class SellFragment extends Fragment {
    private EditText makeModelInput, yearInput, priceInput, locationInput;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sell, container, false);

        makeModelInput = view.findViewById(R.id.makeModelInput);
        yearInput = view.findViewById(R.id.yearInput);
        priceInput = view.findViewById(R.id.priceInput);
        locationInput = view.findViewById(R.id.locationInput);
        Button postListingButton = view.findViewById(R.id.postListingButton);

        postListingButton.setOnClickListener(v -> {
            if (validate()) {
                Toast.makeText(requireContext(), "Demo listing created", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private boolean validate() {
        if (makeModelInput.getText().toString().trim().isEmpty()) {
            makeModelInput.setError("Enter car make and model");
            return false;
        }
        if (yearInput.getText().toString().trim().isEmpty()) {
            yearInput.setError("Enter year");
            return false;
        }
        if (priceInput.getText().toString().trim().isEmpty()) {
            priceInput.setError("Enter price");
            return false;
        }
        return true;
    }
}
