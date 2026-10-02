package com.example.carmarketplace;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private View carDetailCard, navigationTopCard, navControlsCard, initialTopBar;
    private ImageView carImage;
    private TextView carName, carPrice, carDistance, destinationText, navEtaText;
    private EditText searchInput;
    private View navigateButton, btnStartNav, btnCloseNav, btnMyLocation, btnSwap;
    
    private Car selectedCar;
    private LatLng destinationLatLng;
    private String destinationTitle = "";
    private Marker customDestinationMarker;

    private boolean isNavigating = false;
    private boolean isOriginDestinationSwapped = false;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private Location currentLocation;

    private Polyline primaryBestRoutePolyline;
    private final List<Polyline> alternativePolylines = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // UI Components
        carDetailCard = findViewById(R.id.carDetailCard);
        navigationTopCard = findViewById(R.id.navigationTopCard);
        navControlsCard = findViewById(R.id.navControlsCard);
        initialTopBar = findViewById(R.id.initialTopBar);
        searchInput = findViewById(R.id.searchInput);
        
        carImage = findViewById(R.id.carImage);
        carName = findViewById(R.id.carName);
        carPrice = findViewById(R.id.carPrice);
        carDistance = findViewById(R.id.carDistance);
        destinationText = findViewById(R.id.destinationText);
        navEtaText = findViewById(R.id.navEtaText);
        
        navigateButton = findViewById(R.id.navigateButton);
        btnStartNav = findViewById(R.id.btnStartNav);
        btnCloseNav = findViewById(R.id.btnCloseNav);
        btnMyLocation = findViewById(R.id.btnMyLocation);
        btnSwap = findViewById(R.id.btnSwap);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                currentLocation = locationResult.getLastLocation();
                if (destinationLatLng != null && !isNavigating) {
                    calculateAndDrawBestRoute();
                }
                if (isNavigating) {
                    updateNavigationCamera();
                }
            }
        };

        findViewById(R.id.backButton).setOnClickListener(v -> finish());

        // Obtain SupportMapFragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        navigateButton.setOnClickListener(v -> {
            toggleNavigationUI(true);
            launchGoogleMapsTurnByTurn();
        });

        btnStartNav.setOnClickListener(v -> {
            startNavigation();
            launchGoogleMapsTurnByTurn();
        });

        btnCloseNav.setOnClickListener(v -> toggleNavigationUI(false));
        btnMyLocation.setOnClickListener(v -> centerOnCurrentLocation());

        btnSwap.setOnClickListener(v -> {
            isOriginDestinationSwapped = !isOriginDestinationSwapped;
            Toast.makeText(this, isOriginDestinationSwapped ? "Route reversed: Destination ➔ Current Location" : "Route: Current Location ➔ Destination", Toast.LENGTH_SHORT).show();
            calculateAndDrawBestRoute();
        });

        // Search bar listener
        searchInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                performSearch(searchInput.getText().toString().trim());
                return true;
            }
            return false;
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Apply dark theme to map
        try {
            mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style));
        } catch (Exception e) {
            Log.e("MapActivity", "Map style error", e);
        }

        mMap.setTrafficEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            startLocationUpdates();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
        }

        addCarMarkers();

        // Marker Click Listener -> Select Car Destination
        mMap.setOnMarkerClickListener(marker -> {
            Car car = (Car) marker.getTag();
            if (car != null) {
                selectedCar = car;
                destinationLatLng = new LatLng(car.latitude, car.longitude);
                destinationTitle = car.name;
                showCarDetail(car);
                calculateAndDrawBestRoute();
            } else if ("CUSTOM_DEST".equals(marker.getTag())) {
                calculateAndDrawBestRoute();
            }
            return false;
        });

        // Map Click Listener -> Tap anywhere to choose custom destination
        mMap.setOnMapClickListener(latLng -> {
            if (!isNavigating) {
                setCustomDestination(latLng, "Selected Destination");
            }
        });

        // Map Long Click Listener -> Long press anywhere to choose custom destination
        mMap.setOnMapLongClickListener(latLng -> {
            if (!isNavigating) {
                setCustomDestination(latLng, "Custom Pinned Location");
            }
        });

        // Click on alternative polyline to switch to that route
        mMap.setOnPolylineClickListener(polyline -> {
            if (polyline != primaryBestRoutePolyline) {
                Toast.makeText(this, "Switched to alternative road route", Toast.LENGTH_SHORT).show();
                if (primaryBestRoutePolyline != null) {
                    primaryBestRoutePolyline.setColor(getResources().getColor(R.color.route_alternative));
                    primaryBestRoutePolyline.setWidth(8f);
                    primaryBestRoutePolyline.setZIndex(1f);
                    alternativePolylines.add(primaryBestRoutePolyline);
                }
                primaryBestRoutePolyline = polyline;
                primaryBestRoutePolyline.setColor(getResources().getColor(R.color.route_primary));
                primaryBestRoutePolyline.setWidth(12f);
                primaryBestRoutePolyline.setZIndex(10f);
            }
        });

        centerOnCurrentLocation();
    }

    private void setCustomDestination(LatLng latLng, String title) {
        selectedCar = null;
        carDetailCard.setVisibility(View.GONE);
        destinationLatLng = latLng;
        destinationTitle = title + String.format(Locale.US, " (%.3f, %.3f)", latLng.latitude, latLng.longitude);

        if (customDestinationMarker != null) {
            customDestinationMarker.remove();
        }

        customDestinationMarker = mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title(destinationTitle)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        
        if (customDestinationMarker != null) {
            customDestinationMarker.setTag("CUSTOM_DEST");
            customDestinationMarker.showInfoWindow();
        }

        destinationText.setText(destinationTitle);
        calculateAndDrawBestRoute();
    }

    private void performSearch(String query) {
        if (query.isEmpty()) return;

        List<Car> results = CarRepository.search(query);
        if (!results.isEmpty()) {
            Car match = results.get(0);
            selectedCar = match;
            destinationLatLng = new LatLng(match.latitude, match.longitude);
            destinationTitle = match.name;

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(destinationLatLng, 15));
            showCarDetail(match);
            calculateAndDrawBestRoute();
            Toast.makeText(this, "Found: " + match.name, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "No matching locations found for: " + query, Toast.LENGTH_SHORT).show();
        }
    }

    private void centerOnCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    currentLocation = location;
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 14));
                }
            });
        }
    }

    private void toggleNavigationUI(boolean active) {
        if (active) {
            if (destinationLatLng == null) {
                Toast.makeText(this, "Please select a destination on the map first", Toast.LENGTH_SHORT).show();
                return;
            }
            initialTopBar.setVisibility(View.GONE);
            carDetailCard.setVisibility(View.GONE);
            navigationTopCard.setVisibility(View.VISIBLE);
            navControlsCard.setVisibility(View.VISIBLE);
            destinationText.setText(destinationTitle.isEmpty() ? "Destination" : destinationTitle);
        } else {
            isNavigating = false;
            initialTopBar.setVisibility(View.VISIBLE);
            carDetailCard.setVisibility(selectedCar != null ? View.VISIBLE : View.GONE);
            navigationTopCard.setVisibility(View.GONE);
            navControlsCard.setVisibility(View.GONE);

            if (currentLocation != null) {
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
                        .target(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()))
                        .zoom(14)
                        .tilt(0)
                        .bearing(0)
                        .build()));
            }
        }
    }

    private void startNavigation() {
        if (destinationLatLng == null || currentLocation == null) {
            Toast.makeText(this, "Destination or location missing", Toast.LENGTH_SHORT).show();
            return;
        }
        isNavigating = true;
        Toast.makeText(this, "Starting road navigation to " + (destinationTitle.isEmpty() ? "Destination" : destinationTitle), Toast.LENGTH_SHORT).show();
        updateNavigationCamera();
    }

    private void launchGoogleMapsTurnByTurn() {
        if (destinationLatLng == null) return;

        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + destinationLatLng.latitude + "," + destinationLatLng.longitude + "&mode=d");
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");

        try {
            startActivity(mapIntent);
        } catch (Exception e) {
            // Web browser fallback if Google Maps app is not available
            Uri webUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=" + destinationLatLng.latitude + "," + destinationLatLng.longitude + "&travelmode=driving");
            startActivity(new Intent(Intent.ACTION_VIEW, webUri));
        }
    }

    private void updateNavigationCamera() {
        if (currentLocation == null || destinationLatLng == null) return;

        LatLng userPos = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());

        float bearing = currentLocation.bearingTo(new Location("") {{
            setLatitude(destinationLatLng.latitude);
            setLongitude(destinationLatLng.longitude);
        }});

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(userPos)
                .zoom(18)
                .tilt(45)
                .bearing(bearing)
                .build();

        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 4000)
                .setWaitForAccurateLocation(false)
                .setMinUpdateIntervalMillis(2000)
                .setMaxUpdateDelayMillis(8000)
                .build();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        }
    }

    /**
     * Calculates and renders the real road route following actual streets and highways.
     * Uses Google Directions API first; if unavailable, uses OpenStreetMap OSRM Real-Road Engine.
     */
    private void calculateAndDrawBestRoute() {
        if (destinationLatLng == null || currentLocation == null || mMap == null) return;

        LatLng startPos = isOriginDestinationSwapped ? destinationLatLng : new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        LatLng endPos = isOriginDestinationSwapped ? new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()) : destinationLatLng;

        fetchBestRoadRoute(startPos, endPos);
    }

    private void fetchBestRoadRoute(LatLng origin, LatLng dest) {
        String apiKey = "";
        try {
            android.content.pm.ApplicationInfo ai = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            apiKey = ai.metaData.getString("com.google.android.geo.API_KEY");
        } catch (Exception e) {
            Log.e("MapActivity", "Failed to load API key", e);
        }

        if (apiKey == null || apiKey.isEmpty()) {
            fetchOsrmRealRoadRoute(origin, dest);
            return;
        }

        String urlString = "https://maps.googleapis.com/maps/api/directions/json?" +
                "origin=" + origin.latitude + "," + origin.longitude +
                "&destination=" + dest.latitude + "," + dest.longitude +
                "&alternatives=true" +
                "&mode=driving" +
                "&key=" + apiKey;

        new Thread(() -> {
            HttpURLConnection conn = null;
            try {
                URL url = new URL(urlString);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                int responseCode = conn.getResponseCode();
                if (responseCode != 200) {
                    runOnUiThread(() -> fetchOsrmRealRoadRoute(origin, dest));
                    return;
                }

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder json = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    json.append(line);
                }

                JSONObject jsonObject = new JSONObject(json.toString());
                String status = jsonObject.optString("status");

                if ("OK".equals(status)) {
                    JSONArray routes = jsonObject.getJSONArray("routes");

                    int bestRouteIndex = 0;
                    double minScore = Double.MAX_VALUE;

                    List<List<LatLng>> candidatePoints = new ArrayList<>();
                    List<String> distances = new ArrayList<>();
                    List<String> durations = new ArrayList<>();

                    for (int i = 0; i < routes.length(); i++) {
                        JSONObject r = routes.getJSONObject(i);
                        JSONObject overviewPolyline = r.getJSONObject("overview_polyline");
                        String encodedPoints = overviewPolyline.getString("points");
                        List<LatLng> points = PolyUtil.decode(encodedPoints);
                        candidatePoints.add(points);

                        JSONObject leg = r.getJSONArray("legs").getJSONObject(0);
                        int durationSec = leg.getJSONObject("duration").getInt("value");
                        int distanceMeters = leg.getJSONObject("distance").getInt("value");

                        String distText = leg.getJSONObject("distance").getString("text");
                        String durText = leg.getJSONObject("duration").getString("text");
                        distances.add(distText);
                        durations.add(durText);

                        double score = durationSec + (distanceMeters * 0.05);
                        if (score < minScore) {
                            minScore = score;
                            bestRouteIndex = i;
                        }
                    }

                    int finalBestIndex = bestRouteIndex;

                    runOnUiThread(() -> {
                        clearPolylines();

                        for (int i = 0; i < candidatePoints.size(); i++) {
                            if (i == finalBestIndex) continue;
                            Polyline altPoly = mMap.addPolyline(new PolylineOptions()
                                    .addAll(candidatePoints.get(i))
                                    .width(8f)
                                    .color(getResources().getColor(R.color.route_alternative))
                                    .clickable(true)
                                    .jointType(com.google.android.gms.maps.model.JointType.ROUND));
                            alternativePolylines.add(altPoly);
                        }

                        primaryBestRoutePolyline = mMap.addPolyline(new PolylineOptions()
                                .addAll(candidatePoints.get(finalBestIndex))
                                .width(12f)
                                .color(getResources().getColor(R.color.route_primary))
                                .clickable(true)
                                .zIndex(10f)
                                .jointType(com.google.android.gms.maps.model.JointType.ROUND)
                                .startCap(new com.google.android.gms.maps.model.RoundCap())
                                .endCap(new com.google.android.gms.maps.model.RoundCap()));

                        String bestDistance = distances.get(finalBestIndex);
                        String bestDuration = durations.get(finalBestIndex);

                        carDistance.setText(bestDistance + " away (Real Road Route)");
                        navEtaText.setText(bestDuration + " (" + bestDistance + ")");
                    });
                } else {
                    // Google Directions returned non-OK status -> fallback to OpenStreetMap OSRM real road engine
                    runOnUiThread(() -> fetchOsrmRealRoadRoute(origin, dest));
                }
            } catch (Exception e) {
                Log.e("MapActivity", "Error fetching Google routes", e);
                runOnUiThread(() -> fetchOsrmRealRoadRoute(origin, dest));
            } finally {
                if (conn != null) conn.disconnect();
            }
        }).start();
    }

    /**
     * OpenStreetMap OSRM Real Road Engine:
     * Fetches exact real-road geometry following actual streets, highways, intersections, and turns globally.
     */
    private void fetchOsrmRealRoadRoute(LatLng origin, LatLng dest) {
        String urlString = String.format(Locale.US,
                "https://router.project-osrm.org/route/v1/driving/%.6f,%.6f;%.6f,%.6f?overview=full&geometries=geojson&alternatives=true",
                origin.longitude, origin.latitude, dest.longitude, dest.latitude);

        new Thread(() -> {
            HttpURLConnection conn = null;
            try {
                URL url = new URL(urlString);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("User-Agent", "CarMarketplace/1.0 (Android)");
                conn.setConnectTimeout(8000);
                conn.setReadTimeout(8000);

                if (conn.getResponseCode() == 200) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder json = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        json.append(line);
                    }

                    JSONObject jsonObject = new JSONObject(json.toString());
                    if ("Ok".equals(jsonObject.optString("code"))) {
                        JSONArray routes = jsonObject.getJSONArray("routes");

                        int bestRouteIndex = 0;
                        double minScore = Double.MAX_VALUE;

                        List<List<LatLng>> candidatePoints = new ArrayList<>();
                        List<String> distances = new ArrayList<>();
                        List<String> durations = new ArrayList<>();

                        for (int i = 0; i < routes.length(); i++) {
                            JSONObject r = routes.getJSONObject(i);
                            JSONObject geometry = r.getJSONObject("geometry");
                            JSONArray coordinates = geometry.getJSONArray("coordinates");

                            List<LatLng> points = new ArrayList<>();
                            for (int c = 0; c < coordinates.length(); c++) {
                                JSONArray coord = coordinates.getJSONArray(c);
                                double lng = coord.getDouble(0);
                                double lat = coord.getDouble(1);
                                points.add(new LatLng(lat, lng));
                            }
                            candidatePoints.add(points);

                            double distanceMeters = r.getDouble("distance");
                            double durationSec = r.getDouble("duration");

                            String distText = String.format(Locale.US, "%.1f km", distanceMeters / 1000.0);
                            int mins = Math.max(1, (int) Math.round(durationSec / 60.0));
                            String durText = mins + " mins";

                            distances.add(distText);
                            durations.add(durText);

                            double score = durationSec + (distanceMeters * 0.05);
                            if (score < minScore) {
                                minScore = score;
                                bestRouteIndex = i;
                            }
                        }

                        int finalBestIndex = bestRouteIndex;

                        runOnUiThread(() -> {
                            clearPolylines();

                            for (int i = 0; i < candidatePoints.size(); i++) {
                                if (i == finalBestIndex) continue;
                                Polyline altPoly = mMap.addPolyline(new PolylineOptions()
                                        .addAll(candidatePoints.get(i))
                                        .width(8f)
                                        .color(getResources().getColor(R.color.route_alternative))
                                        .clickable(true)
                                        .jointType(com.google.android.gms.maps.model.JointType.ROUND));
                                alternativePolylines.add(altPoly);
                            }

                            primaryBestRoutePolyline = mMap.addPolyline(new PolylineOptions()
                                    .addAll(candidatePoints.get(finalBestIndex))
                                    .width(12f)
                                    .color(getResources().getColor(R.color.route_primary))
                                    .clickable(true)
                                    .zIndex(10f)
                                    .jointType(com.google.android.gms.maps.model.JointType.ROUND)
                                    .startCap(new com.google.android.gms.maps.model.RoundCap())
                                    .endCap(new com.google.android.gms.maps.model.RoundCap()));

                            String bestDistance = distances.get(finalBestIndex);
                            String bestDuration = durations.get(finalBestIndex);

                            carDistance.setText(bestDistance + " away (Real Road Route)");
                            navEtaText.setText(bestDuration + " (" + bestDistance + ")");
                        });
                        return;
                    }
                }
            } catch (Exception e) {
                Log.e("MapActivity", "Error fetching OSRM road route", e);
            } finally {
                if (conn != null) conn.disconnect();
            }

            // Final fallback if completely offline
            runOnUiThread(() -> drawSmartFallbackRoute(origin, dest));
        }).start();
    }

    /**
     * Fallback curve pathfinder when device is offline.
     */
    private void drawSmartFallbackRoute(LatLng origin, LatLng dest) {
        clearPolylines();

        List<LatLng> points = new ArrayList<>();
        int steps = 25;
        double dx = dest.longitude - origin.longitude;
        double dy = dest.latitude - origin.latitude;

        double perpX = -dy * 0.15;
        double perpY = dx * 0.15;

        for (int i = 0; i <= steps; i++) {
            double t = (double) i / steps;
            double curveOffset = Math.sin(t * Math.PI);
            double lat = origin.latitude + t * dy + curveOffset * perpY;
            double lng = origin.longitude + t * dx + curveOffset * perpX;
            points.add(new LatLng(lat, lng));
        }

        primaryBestRoutePolyline = mMap.addPolyline(new PolylineOptions()
                .addAll(points)
                .width(10f)
                .color(getResources().getColor(R.color.route_primary))
                .geodesic(true)
                .startCap(new com.google.android.gms.maps.model.RoundCap())
                .endCap(new com.google.android.gms.maps.model.RoundCap()));

        float[] results = new float[1];
        Location.distanceBetween(origin.latitude, origin.longitude, dest.latitude, dest.longitude, results);
        float distanceKm = results[0] / 1000f;
        int estimatedMinutes = Math.max(1, (int) ((distanceKm / 40.0f) * 60));

        carDistance.setText(String.format(Locale.US, "%.1f km away", distanceKm));
        navEtaText.setText(String.format(Locale.US, "%d mins (%.1f km)", estimatedMinutes, distanceKm));
    }

    private void clearPolylines() {
        if (primaryBestRoutePolyline != null) {
            primaryBestRoutePolyline.remove();
            primaryBestRoutePolyline = null;
        }
        for (Polyline p : alternativePolylines) {
            if (p != null) p.remove();
        }
        alternativePolylines.clear();
    }

    private void addCarMarkers() {
        List<Car> cars = CarRepository.getCars();
        for (Car car : cars) {
            LatLng pos = new LatLng(car.latitude, car.longitude);
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(pos)
                    .title(car.name)
                    .snippet(car.price)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
            if (marker != null) {
                marker.setTag(car);
            }
        }
    }

    private void showCarDetail(Car car) {
        carName.setText(car.name);
        carPrice.setText(car.price);
        carImage.setImageResource(car.imageRes);
        carDetailCard.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
                startLocationUpdates();
            }
        }
    }
}
