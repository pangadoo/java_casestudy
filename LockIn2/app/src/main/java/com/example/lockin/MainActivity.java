package com.example.lockin;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Switch blockerSwitch;
    private Spinner appSpinner;

    // We need two lists: One for the readable names (Instagram), one for the secret names (com.instagram...)
    private List<String> appNames = new ArrayList<>();
    private List<String> packageNames = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        blockerSwitch = findViewById(R.id.switch_blocker);
        appSpinner = findViewById(R.id.spinner_app_selector); // Find the new Spinner

        // 1. Load the apps into the Spinner
        loadInstalledApps();

        blockerSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences prefs = getSharedPreferences("BlockerPrefs", MODE_PRIVATE);

                if (isChecked) {
                    long startTime = System.currentTimeMillis();
                    prefs.edit().putLong("start_time", startTime).apply();

                    if (!Settings.canDrawOverlays(MainActivity.this)) {
                        Intent intent = new Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:" + getPackageName())
                        );
                        startActivity(intent);
                        blockerSwitch.setChecked(false);
                        Toast.makeText(MainActivity.this, "Please grant 'Appear on top' permission.", Toast.LENGTH_LONG).show();
                    } else {
                        // --- THE NEW LOGIC ---
                        // Get the index of the app they selected in the dropdown
                        int selectedPosition = appSpinner.getSelectedItemPosition();

                        // Use that index to grab the correct package name from our list!
                        String appToBlock = packageNames.get(selectedPosition);
                        String readableName = appNames.get(selectedPosition);

                        // Save the package name to the notepad just like before
                        prefs.edit().putString("blocked_app", appToBlock).putBoolean("is_active", true).apply();

                        Toast.makeText(MainActivity.this, "Ready to block: " + readableName, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    long startTime = prefs.getLong("start_time", 0);
                    long endTime = System.currentTimeMillis();

                    if (startTime != 0) {
                        long minutesFocused = (endTime - startTime) / (1000 * 60);
                        long earned = minutesFocused * 50;
                        updateBalance(earned);
                        Toast.makeText(MainActivity.this, "Earned " + earned + " credits!", Toast.LENGTH_LONG).show();
                    }

                    prefs.edit().putBoolean("is_active", false).apply();
                    Toast.makeText(MainActivity.this, "Blocker disabled.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // ==========================================
    // NEW METHOD: FETCH INSTALLED APPS
    // ==========================================
    private void loadInstalledApps() {
        PackageManager pm = getPackageManager();

        // We only want to find apps that the user can actually launch from their home screen
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> appList = pm.queryIntentActivities(mainIntent, 0);

        for (ResolveInfo info : appList) {
            // Get the human-readable name (e.g., "YouTube")
            appNames.add(info.loadLabel(pm).toString());
            // Get the system package name (e.g., "com.google.android.youtube")
            packageNames.add(info.activityInfo.packageName);
        }

        // Create an adapter to visually plug our list of names into the Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, appNames);
        appSpinner.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        TextView balanceText = findViewById(R.id.text_view_balance);
        balanceText.setText("Wealth: " + getBalance() + " Credits");
    }

    private long getBalance() {
        return getSharedPreferences("BlockerPrefs", MODE_PRIVATE).getLong("balance", 0);
    }

    private void updateBalance(long amount) {
        long currentBalance = getBalance();
        getSharedPreferences("BlockerPrefs", MODE_PRIVATE)
                .edit()
                .putLong("balance", currentBalance + amount)
                .apply();
    }
}