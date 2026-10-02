package com.example.carmarketplace;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CarRepository {

    public static List<Car> getCars() {

        List<Car> cars = new ArrayList<>();

        // 1
        cars.add(new Car(
                "BYD Atto 3",
                "2024",
                "৳ 42,50,000",
                "Dhaka",
                R.drawable.car1,
                23.8103,
                90.4125
        ));

        // 2
        cars.add(new Car(
                "Hyundai Tucson",
                "2023",
                "৳ 51,50,000",
                "Chattogram",
                R.drawable.car2,
                22.3569,
                91.7832
        ));

        // 3
        cars.add(new Car(
                "Toyota RAV4",
                "2022",
                "৳ 48,50,000",
                "Dhaka",
                R.drawable.car3,
                23.7940,
                90.4043
        ));

        // 4
        cars.add(new Car(
                "Kia Sportage",
                "2024",
                "৳ 49,50,000",
                "Sylhet",
                R.drawable.car4,
                24.8949,
                91.8687
        ));

        // 5
        cars.add(new Car(
                "Nissan X-Trail",
                "2021",
                "৳ 37,50,000",
                "Dhaka",
                R.drawable.car5,
                23.7509,
                90.3935
        ));

        // 6
        cars.add(new Car(
                "Toyota Corolla",
                "2020",
                "৳ 28,50,000",
                "Rajshahi",
                R.drawable.car6,
                24.3745,
                88.6042
        ));

        // 7
        cars.add(new Car(
                "Honda Civic",
                "2022",
                "৳ 39,50,000",
                "Dhaka",
                R.drawable.car7,
                23.7806,
                90.4070
        ));

        // 8
        cars.add(new Car(
                "Honda Vezel",
                "2021",
                "৳ 36,50,000",
                "Chattogram",
                R.drawable.car8,
                22.3569,
                91.7832
        ));

        // 9
        cars.add(new Car(
                "Toyota Harrier",
                "2023",
                "৳ 62,50,000",
                "Dhaka",
                R.drawable.car9,
                23.8103,
                90.4125
        ));

        // 10
        cars.add(new Car(
                "Toyota Axio",
                "2019",
                "৳ 24,50,000",
                "Dhaka",
                R.drawable.car10,
                23.7509,
                90.3935
        ));

        // 11
        cars.add(new Car(
                "Mitsubishi Outlander",
                "2022",
                "৳ 43,50,000",
                "Sylhet",
                R.drawable.car11,
                24.8949,
                91.8687
        ));

        // 12
        cars.add(new Car(
                "Mazda CX-5",
                "2023",
                "৳ 52,50,000",
                "Dhaka",
                R.drawable.car12,
                23.8103,
                90.4125
        ));

        // 13
        cars.add(new Car(
                "Nissan Qashqai",
                "2022",
                "৳ 41,50,000",
                "Chattogram",
                R.drawable.car13,
                22.3569,
                91.7832
        ));

        // 14
        cars.add(new Car(
                "Toyota Prius",
                "2020",
                "৳ 32,50,000",
                "Dhaka",
                R.drawable.car14,
                23.7940,
                90.4043
        ));

        // 15
        cars.add(new Car(
                "Hyundai Creta",
                "2023",
                "৳ 38,50,000",
                "Rajshahi",
                R.drawable.car15,
                24.3745,
                88.6042
        ));

        // 16
        cars.add(new Car(
                "Kia Seltos",
                "2022",
                "৳ 39,50,000",
                "Dhaka",
                R.drawable.car16,
                23.8103,
                90.4125
        ));

        // 17
        cars.add(new Car(
                "Toyota Premio",
                "2018",
                "৳ 27,50,000",
                "Sylhet",
                R.drawable.car17,
                24.8949,
                91.8687
        ));

        // 18
        cars.add(new Car(
                "Honda CR-V",
                "2021",
                "৳ 45,50,000",
                "Dhaka",
                R.drawable.car18,
                23.7806,
                90.4070
        ));

        // 19
        cars.add(new Car(
                "Suzuki Swift",
                "2020",
                "৳ 19,50,000",
                "Chattogram",
                R.drawable.car19,
                22.3569,
                91.7832
        ));

        // 20
        cars.add(new Car(
                "Toyota Noah",
                "2022",
                "৳ 47,50,000",
                "Dhaka",
                R.drawable.car20,
                23.8103,
                90.4125
        ));

        return cars;
    }


    // SEARCH FUNCTION
    public static List<Car> search(String query) {

        String q = query == null
                ? ""
                : query.trim().toLowerCase(Locale.US);

        List<Car> result = new ArrayList<>();

        // If search box is empty, show all cars
        if (q.isEmpty()) {
            result.addAll(getCars());
            return result;
        }

        // Separate multiple words
        String[] keywords = q.split("\\s+");

        for (Car car : getCars()) {

            String searchableText =
                    car.name + " "
                            + car.year + " "
                            + car.location + " "
                            + car.price;

            searchableText =
                    searchableText.toLowerCase(Locale.US);

            boolean match = true;

            // Every entered word must exist
            for (String keyword : keywords) {

                if (!searchableText.contains(keyword)) {
                    match = false;
                    break;
                }
            }

            if (match) {
                result.add(car);
            }
        }

        return result;
    }
}

