package com.example.carmarketplace;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class CostAnalyzerActivity extends AppCompatActivity {

    private List<Car> carList;
    private Car selectedCar;

    private TextView selectedCarTitle, reliabilityBadge;
    private TextView totalCostText, valueRetentionPill;
    private ProgressBar efficiencyProgressBar;
    private TextView efficiencyScoreText;

    private TextView costMaintenanceText, costInsuranceText, costTaxText, costHiddenText;
    private TextView costRoutineText, costDepreciationText, costFuelText;
    private TextView insightText, reliabilityRatingText, histResaleText;
    private ImageView resaleCarImage;
    private TextView resaleValueText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cost_analyzer);

        // Fetch available car list
        carList = CarRepository.getCars();

        // UI Binding
        findViewById(R.id.backButton).setOnClickListener(v -> finish());
        
        selectedCarTitle = findViewById(R.id.selectedCarTitle);
        reliabilityBadge = findViewById(R.id.reliabilityBadge);
        
        totalCostText = findViewById(R.id.totalCostText);
        valueRetentionPill = findViewById(R.id.valueRetentionPill);
        
        efficiencyProgressBar = findViewById(R.id.efficiencyProgressBar);
        efficiencyScoreText = findViewById(R.id.efficiencyScoreText);

        costMaintenanceText = findViewById(R.id.costMaintenanceText);
        costInsuranceText = findViewById(R.id.costInsuranceText);
        costTaxText = findViewById(R.id.costTaxText);
        costHiddenText = findViewById(R.id.costHiddenText);

        costRoutineText = findViewById(R.id.costRoutineText);
        costDepreciationText = findViewById(R.id.costDepreciationText);
        costFuelText = findViewById(R.id.costFuelText);

        insightText = findViewById(R.id.insightText);
        reliabilityRatingText = findViewById(R.id.reliabilityRatingText);
        histResaleText = findViewById(R.id.histResaleText);
        
        resaleCarImage = findViewById(R.id.resaleCarImage);
        resaleValueText = findViewById(R.id.resaleValueText);

        // Car selection picker
        findViewById(R.id.carSelectorButton).setOnClickListener(v -> showCarPickerDialog());

        // Check intent for pre-selected car
        String preselectedCarName = getIntent().getStringExtra("carName");
        selectedCar = carList.get(0); // Default to first car
        if (preselectedCarName != null && !preselectedCarName.isEmpty()) {
            for (Car c : carList) {
                if (c.name.equalsIgnoreCase(preselectedCarName)) {
                    selectedCar = c;
                    break;
                }
            }
        }

        calculateAndDisplayCost(selectedCar);
    }

    private void showCarPickerDialog() {
        String[] carNames = new String[carList.size()];
        for (int i = 0; i < carList.size(); i++) {
            carNames[i] = carList.get(i).year + " " + carList.get(i).name + " (" + carList.get(i).price + ")";
        }

        new AlertDialog.Builder(this)
                .setTitle("Select a Car to Analyze")
                .setItems(carNames, (dialog, which) -> {
                    selectedCar = carList.get(which);
                    calculateAndDisplayCost(selectedCar);
                })
                .show();
    }

    /**
     * Calculates ownership cost deterministically based on raw car specs (Price, Year, Category).
     */
    private void calculateAndDisplayCost(Car car) {
        selectedCarTitle.setText(car.year + " " + car.name);
        resaleCarImage.setImageResource(car.imageRes);

        // Parse base price
        long basePrice = parsePrice(car.price);
        int carYear = parseYear(car.year);
        int age = Math.max(1, 2026 - carYear);

        // Deterministic raw cost calculations
        long maintenance5Yr = (long) (basePrice * (0.035 + age * 0.012) * 5);
        long insurance5Yr = (long) (basePrice * (0.028 - age * 0.001) * 5);
        long tax5Yr = (long) ((20000 + basePrice * 0.008) * 5);
        long hiddenFees = (long) (basePrice * 0.01 + 12000);

        long annualRoutineMaint = maintenance5Yr / 5;
        long annualDepreciation = (long) (basePrice * (0.07 + age * 0.004));
        long annualFuel = 120000; // 12k km/yr standard usage

        long total5YrCost = maintenance5Yr + insurance5Yr + tax5Yr + hiddenFees + (annualDepreciation * 5) + (annualFuel * 5);

        int efficiency = Math.min(96, Math.max(68, 92 - age * 3));
        long resaleValue3Yr = (long) (basePrice * Math.max(0.45, 0.90 - age * 0.08));

        // Format and set UI values
        totalCostText.setText(formatBdt(total5YrCost));
        valueRetentionPill.setText("⚡ Top " + (10 + age * 2) + "% in Value Retention");

        efficiencyProgressBar.setProgress(efficiency);
        efficiencyScoreText.setText(efficiency + "%");

        costMaintenanceText.setText(formatBdt(maintenance5Yr));
        costInsuranceText.setText(formatBdt(insurance5Yr));
        costTaxText.setText(formatBdt(tax5Yr));
        costHiddenText.setText(formatBdt(hiddenFees));

        costRoutineText.setText(formatBdt(annualRoutineMaint));
        costDepreciationText.setText("-" + formatBdt(annualDepreciation));
        costFuelText.setText(formatBdt(annualFuel));

        // Insight Hack calculation
        long potentialSavings = (long) (annualRoutineMaint * 0.15 + insurance5Yr * 0.03);
        insightText.setText("Save " + formatBdt(potentialSavings) + " by pre-paying annual maintenance and bundling insurance.");

        if (age <= 2) {
            reliabilityBadge.setText("🛡 High Reliability Rating");
            reliabilityRatingText.setText("A+ RELIABILITY");
        } else if (age <= 4) {
            reliabilityBadge.setText("🛡 Good Reliability Rating");
            reliabilityRatingText.setText("A RELIABILITY");
        } else {
            reliabilityBadge.setText("🛡 Average Reliability Rating");
            reliabilityRatingText.setText("B+ RELIABILITY");
        }

        double resaleTrend = Math.max(1.2, 5.8 - age * 0.7);
        histResaleText.setText(String.format(Locale.US, "+%.1f%%", resaleTrend));

        resaleValueText.setText("3 yr expected value: " + formatBdt(resaleValue3Yr));
    }

    private long parsePrice(String priceStr) {
        if (priceStr == null) return 3000000;
        String clean = priceStr.replaceAll("[^0-9]", "");
        if (clean.isEmpty()) return 3000000;
        try {
            return Long.parseLong(clean);
        } catch (Exception e) {
            return 3000000;
        }
    }

    private int parseYear(String yearStr) {
        if (yearStr == null) return 2022;
        try {
            return Integer.parseInt(yearStr.trim());
        } catch (Exception e) {
            return 2022;
        }
    }

    private String formatBdt(long value) {
        NumberFormat format = NumberFormat.getInstance(new Locale("en", "BD"));
        return "৳ " + format.format(value);
    }
}
