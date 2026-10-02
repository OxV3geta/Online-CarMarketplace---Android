package com.example.carmarketplace;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.List;

public class DashboardFragment extends Fragment {

    private EditText searchInput;
    private LinearLayout featuredCarsRow;
    private LinearLayout allCarsList;
    private TextView resultTitle;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(
                R.layout.fragment_dashboard,
                container,
                false
        );

        searchInput = view.findViewById(R.id.searchInput);
        featuredCarsRow = view.findViewById(R.id.featuredCarsRow);
        allCarsList = view.findViewById(R.id.allCarsList);
        resultTitle = view.findViewById(R.id.resultTitle);

        // SEARCH BUTTON
        TextView searchButton =
                view.findViewById(R.id.searchButton);

        searchButton.setOnClickListener(v -> performSearch());

        // SEARCH FROM KEYBOARD
        searchInput.setSingleLine(true);
        searchInput.setImeOptions(EditorInfo.IME_ACTION_SEARCH);

        searchInput.setOnEditorActionListener(
                (v, actionId, event) -> {

                    if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                        performSearch();
                        return true;
                    }

                    if (event != null
                            && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {

                        performSearch();
                        return true;
                    }

                    return false;
                }
        );

        // DISPLAY ALL CARS
        displayAllCars(inflater);

        return view;
    }


    // =========================================================
    // SEARCH FUNCTION
    // =========================================================

    private void performSearch() {

        String query = searchInput
                .getText()
                .toString()
                .trim();

        LayoutInflater inflater =
                LayoutInflater.from(requireContext());

        // Remove old results
        allCarsList.removeAllViews();

        List<Car> results =
                CarRepository.search(query);

        // EMPTY SEARCH
        if (query.isEmpty()) {

            resultTitle.setText("All cars");

            for (Car car : results) {

                addCarCard(
                        inflater,
                        allCarsList,
                        car
                );
            }

            return;
        }

        // SEARCH RESULT TITLE
        resultTitle.setText(
                "Search results for \"" + query + "\""
        );

        // NO RESULTS
        if (results.isEmpty()) {

            TextView noResult =
                    new TextView(requireContext());

            noResult.setText(
                    "No cars found.\n\n" +
                            "Try searching for:\n" +
                            "Toyota\n" +
                            "Honda\n" +
                            "Corolla\n" +
                            "Dhaka\n" +
                            "2024"
            );

            noResult.setTextColor(
                    getResources().getColor(
                            R.color.text_secondary
                    )
            );

            noResult.setTextSize(14);

            noResult.setPadding(
                    10,
                    30,
                    10,
                    30
            );

            allCarsList.addView(noResult);

            return;
        }

        // SHOW SEARCH RESULTS
        for (Car car : results) {

            addCarCard(
                    inflater,
                    allCarsList,
                    car
            );
        }
    }


    // =========================================================
    // DISPLAY ALL CARS
    // =========================================================

    private void displayAllCars(LayoutInflater inflater) {

        List<Car> cars =
                CarRepository.getCars();

        featuredCarsRow.removeAllViews();
        allCarsList.removeAllViews();

        // FEATURED CARS
        int featuredCount =
                Math.min(6, cars.size());

        for (int i = 0; i < featuredCount; i++) {

            addMiniCard(
                    inflater,
                    featuredCarsRow,
                    cars.get(i)
            );
        }

        // ALL CARS
        for (Car car : cars) {

            addCarCard(
                    inflater,
                    allCarsList,
                    car
            );
        }
    }


    // =========================================================
    // FEATURED CAR CARD
    // =========================================================

    private void addMiniCard(
            LayoutInflater inflater,
            LinearLayout row,
            Car car) {

        View card =
                inflater.inflate(
                        R.layout.item_car_featured,
                        row,
                        false
                );

        ImageView image =
                card.findViewById(R.id.carImage);

        TextView name =
                card.findViewById(R.id.carName);

        TextView price =
                card.findViewById(R.id.carPrice);

        image.setImageResource(car.imageRes);

        name.setText(car.name);

        price.setText(car.price);

        // MAKE CARD CLICKABLE
        card.setOnClickListener(v ->
                openCarDetails(car)
        );

        row.addView(card);
    }


    // =========================================================
    // NORMAL CAR CARD
    // =========================================================

    private void addCarCard(
            LayoutInflater inflater,
            LinearLayout list,
            Car car) {

        View card =
                inflater.inflate(
                        R.layout.item_car_result,
                        list,
                        false
                );

        ImageView image =
                card.findViewById(R.id.carImage);

        TextView name =
                card.findViewById(R.id.carName);

        TextView info =
                card.findViewById(R.id.carInfo);

        TextView price =
                card.findViewById(R.id.carPrice);

        image.setImageResource(car.imageRes);

        name.setText(car.name);

        info.setText(
                car.year +
                        "  •  " +
                        car.location
        );

        price.setText(car.price);

        // MAKE CARD CLICKABLE
        card.setOnClickListener(v ->
                openCarDetails(car)
        );

        list.addView(card);
    }


    // =========================================================
    // OPEN CAR DETAILS
    // =========================================================

    private void openCarDetails(Car car) {

        Intent intent =
                new Intent(
                        requireContext(),
                        CarDetailsActivity.class
                );

        intent.putExtra("carName", car.name);
        intent.putExtra("carYear", car.year);
        intent.putExtra("carPrice", car.price);
        intent.putExtra("carLocation", car.location);
        intent.putExtra("carImage", car.imageRes);

        startActivity(intent);
    }
}