
package com.example.carmarketplace;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import java.util.List;

public class SearchFragment extends Fragment {
    private LinearLayout resultsContainer;
    private EditText searchInput;
    private TextView resultsCountHint;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        resultsContainer = view.findViewById(R.id.resultsContainer);
        searchInput = view.findViewById(R.id.searchInput);
        resultsCountHint = view.findViewById(R.id.resultsCountHint);
        Button searchButton = view.findViewById(R.id.searchButton);

        String initial = getArguments() == null ? "" : getArguments().getString("query", "");
        searchInput.setText(initial);

        searchButton.setOnClickListener(v -> updateResults(inflater));

        updateResults(inflater);
        return view;
    }

    private void updateResults(LayoutInflater inflater) {
        if (resultsContainer == null) return;
        resultsContainer.removeAllViews();
        List<Car> cars = CarRepository.search(searchInput.getText().toString());
        android.content.Context c = requireContext();

        resultsCountHint.setText(cars.size() + " result(s)");
        resultsCountHint.setTextColor(Color.WHITE);
        resultsCountHint.setTypeface(null, android.graphics.Typeface.BOLD);
        resultsCountHint.setTextSize(17);

        if (cars.isEmpty()) {
            TextView empty = UiUtils.text(c, "No cars found. Try \"Toyota\", \"Dhaka\" or \"2024\".", 14, 0xFFAAB2B8, false);
            resultsContainer.addView(empty, new LinearLayout.LayoutParams(-1, UiUtils.dp(c, 70)));
            return;
        }

        for (Car car : cars) {
            addResultCard(inflater, car);
        }
    }

    private void addResultCard(LayoutInflater inflater, Car car) {
        View card = inflater.inflate(R.layout.item_car_result, resultsContainer, false);
        
        ImageView img = card.findViewById(R.id.carImage);
        TextView name = card.findViewById(R.id.carName);
        TextView info = card.findViewById(R.id.carInfo);
        TextView price = card.findViewById(R.id.carPrice);
        
        img.setImageResource(car.imageRes);
        name.setText(car.name);
        info.setText(car.year + "  •  " + car.location);
        price.setText(car.price);
        
        resultsContainer.addView(card);
    }
}
