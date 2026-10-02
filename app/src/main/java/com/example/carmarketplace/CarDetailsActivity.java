package com.example.carmarketplace;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class CarDetailsActivity extends AppCompatActivity {

    private ImageView carImage;
    private TextView carName;
    private TextView carPrice;
    private TextView carYear;
    private TextView carLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_car_details);

        // FIND VIEWS
        carImage = findViewById(R.id.detailCarImage);
        carName = findViewById(R.id.detailCarName);
        carPrice = findViewById(R.id.detailCarPrice);
        carYear = findViewById(R.id.detailCarYear);
        carLocation = findViewById(R.id.detailCarLocation);

        TextView bodyType =
                findViewById(R.id.detailBodyType);

        TextView fuel =
                findViewById(R.id.detailFuel);

        TextView transmission =
                findViewById(R.id.detailTransmission);

        TextView mileage =
                findViewById(R.id.detailMileage);

        TextView engine =
                findViewById(R.id.detailEngine);

        Button contactButton =
                findViewById(R.id.contactButton);


        // GET DATA FROM DASHBOARD
        String name =
                getIntent().getStringExtra("carName");

        String year =
                getIntent().getStringExtra("carYear");

        String price =
                getIntent().getStringExtra("carPrice");

        String location =
                getIntent().getStringExtra("carLocation");

        int image =
                getIntent().getIntExtra(
                        "carImage",
                        R.drawable.car1
                );


        // SHOW BASIC INFORMATION
        carName.setText(name);

        carPrice.setText(price);

        carYear.setText(
                "Year: " + year
        );

        carLocation.setText(
                "Location: " + location
        );

        carImage.setImageResource(image);


        // DEMO SPECIFICATIONS
        bodyType.setText("SUV / Sedan");

        fuel.setText("Petrol / Hybrid");

        transmission.setText("Automatic");

        mileage.setText("15,000 - 35,000 km");

        engine.setText("1.5L - 2.5L");
        TextView backButton =
                findViewById(R.id.backButton);

        backButton.setOnClickListener(v -> finish());


        // COST ANALYZER BUTTON
        Button btnCostAnalyzer = findViewById(R.id.btnCostAnalyzer);
        if (btnCostAnalyzer != null) {
            btnCostAnalyzer.setOnClickListener(v -> {
                android.content.Intent i = new android.content.Intent(this, CostAnalyzerActivity.class);
                i.putExtra("carName", name);
                startActivity(i);
            });
        }

        // CONTACT BUTTON
        contactButton.setOnClickListener(v -> {

            android.widget.Toast.makeText(
                    this,
                    "Seller contact feature coming soon",
                    android.widget.Toast.LENGTH_SHORT
            ).show();

        });
    }
}