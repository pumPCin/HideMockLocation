package com.google.android.hml;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.ActivityCompat;

import com.google.android.hml.databinding.ActivityMainBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getName();
    ActivityMainBinding binding;
    private LocationManager locationManager;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater()); // inflating our xml layout in our activity main binding
        setModuleState(binding);

        binding.menuDetectionTest.setOnClickListener(view -> {
            Log.v(TAG, "View MenuDetectionTest");
            getMockLocationSetting();
        });
        binding.menuAbout.setOnClickListener(view -> {
            Log.v(TAG, "Starting About Activity");
            startActivity(new Intent(MainActivity.this, AboutActivity.class));
        });
        setContentView(binding.getRoot()); // set content view for our layout

        // Initialize the location fields
        this.locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
    }

    public String getLocationProvider(LocationManager locationManager) {
        Criteria criteria = new Criteria();
        return locationManager.getBestProvider(criteria, false);
    }

    public void getMockLocationSetting() {
        // Check location permissions
        if (!checkLocationPermission()) {
            return;
        }

        // Get location from location manager
        Location location = null;
        int maxAttempts = 2;
        for (int count = 0; count < maxAttempts; count++) {
            try {
                String provider = getLocationProvider(locationManager);
                location = locationManager.getLastKnownLocation(provider);
                // location could return null if no location updates have been provided since device boot. IE: opened Google maps.
                if (location == null) {
                    locationManager.requestLocationUpdates(provider, 0, 0, locationListener);
                    location = locationManager.getLastKnownLocation(provider);
                    if (location == null) {
                        throwErrorDialog("Location is null");
                        return;
                    }
                }
                count = maxAttempts;
            } catch (Exception e) {
                throwErrorDialog(e.toString());
                return;
            }
        }

        // Create Material Dialog
        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(MainActivity.this,
                R.style.AlertDialogTheme);
        dialogBuilder.setTitle(getString(R.string.alert_dialog_title));

        // Gather system location metadata
        boolean isMockProvider = location.isFromMockProvider();

        String infoText = "Enable/Disable a mock location provider application and then view the below info.";
        String isMockProviderText = "\n\nlocation.isFromMockProvider(): ";

        int infoTextCount = infoText.length();

        int isMockSettingsNewerThanAndroid6Color;
        if (isMockProvider) {
            isMockSettingsNewerThanAndroid6Color = Color.RED;
        } else {
            isMockSettingsNewerThanAndroid6Color = Color.GREEN;
        }

        int textPosition = infoTextCount;
        SpannableString string = new SpannableString(infoText + isMockProviderText + isMockProvider);
        textPosition += isMockProviderText.length();
        string.setSpan(new ForegroundColorSpan(isMockSettingsNewerThanAndroid6Color),
                textPosition,
                textPosition + String.valueOf(isMockProvider).length(),
                SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);

        dialogBuilder.setMessage(string);
        dialogBuilder.setNegativeButton(getString(R.string.alert_dialog_close), (dialogInterface, i) -> {
        });
        dialogBuilder.show();
    }

    /**
     * Location Listener
     */
    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(android.location.Location location) {
            Log.d(TAG, "GPS LocationChanged");
            double lat = location.getLatitude();
            double lng = location.getLongitude();
            Log.d(TAG, "Received GPS request for " + lat + "," + lng);
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }
    };


    /**
     * Creates an alert dialog window with the supplied exception message
     *
     * @param e Pass Exception object as parameter
     */
    public void throwErrorDialog(String e) {
        new AlertDialog.Builder(this).setTitle("Exception Thrown").setMessage(e).setPositiveButton(R.string.alert_dialog_ok, (dialogInterface, i) -> startActivity(new Intent(MainActivity.this, MainActivity.class))).create().show();
    }

    /**
     * Check if location permission is granted
     */
    private boolean checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(MainActivity.this, "Location Permission Required", Toast.LENGTH_LONG).show();
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                        .setTitle("Location Permission Required")
                        .setMessage("This application requires location permissions")
                        .setPositiveButton(R.string.alert_dialog_ok, (dialogInterface, i) -> {
                            // Prompt the user once explanation has been shown
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
                        }).create().show();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    /**
     * Check if this module is enabled in LSPosed
     *
     * @param binding Pass ActivityMainBinding object as parameter
     */
    private void setModuleState(ActivityMainBinding binding) {
        if (isModuleEnabled()) {
            binding.moduleStatusCard.setCardBackgroundColor(getColor(R.color.purple_500));
            binding.moduleStatusIcon.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.baseline_check_circle_24));
            binding.moduleStatusText.setText(getString(R.string.card_title_activated));
            binding.serviceStatusText.setText(getString(R.string.card_detail_activated));
            binding.serveTimes.setText(getString(R.string.card_serve_time));
        } else {
            binding.moduleStatusCard.setCardBackgroundColor(getColor(R.color.red_500));
            binding.moduleStatusIcon.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.baseline_error_24));
            binding.moduleStatusText.setText(getText(R.string.card_title_not_activated));
            binding.serviceStatusText.setText(getText(R.string.card_detail_not_activated));
            binding.serveTimes.setVisibility(View.GONE);
        }
    }

    /**
     * Self-hook method.
     * Logging and Boolean object are present to avoid ART optimization.
     */
    @SuppressWarnings("all")
    private static boolean isModuleEnabled() {
        Log.i(TAG, "Xposed module not active.");
        return Boolean.valueOf(true);
    }
}
