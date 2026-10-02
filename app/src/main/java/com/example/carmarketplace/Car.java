package com.example.carmarketplace;

public class Car {
    public final String name;
    public final String year;
    public final String price;
    public final String location;
    public final int imageRes;
    public final double latitude;
    public final double longitude;

    public Car(String name, String year, String price, String location, int imageRes, double latitude, double longitude) {
        this.name = name;
        this.year = year;
        this.price = price;
        this.location = location;
        this.imageRes = imageRes;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Car(String name, String year, String price, String location, int imageRes) {
        this(name, year, price, location, imageRes, 0, 0);
    }
}
