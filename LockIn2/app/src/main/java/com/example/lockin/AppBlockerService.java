package com.example.lockin;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.view.Gravity;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class AppBlockerService extends AccessibilityService {

    private LinearLayout blockViewLayout;
    private WindowManager windowManager;
    private boolean isShowingBlockScreen = false;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            if (event.getPackageName() != null) {
                String openedAppPackage = event.getPackageName().toString();

                // Read the invisible notepad
                SharedPreferences prefs = getSharedPreferences("BlockerPrefs", MODE_PRIVATE);
                boolean isActive = prefs.getBoolean("is_active", false);
                String targetApp = prefs.getString("blocked_app", "");

                // Check if the blocker is turned on
                if (isActive) {
                    if (openedAppPackage.equals(targetApp)) {
                        // The banned app was opened! Show the screen.
                        if (!isShowingBlockScreen) {
                            showBlockScreen();
                        }
                    } else if (!openedAppPackage.equals(getPackageName()) && !openedAppPackage.equals("com.android.systemui")) {
                        // Only remove the red screen if the user switched to a DIFFERENT, safe app.
                        removeBlockScreen();
                    }
                } else {
                    // If the switch is off, make sure no screen is showing
                    removeBlockScreen();
                }
            }
        }
    }

    // ==========================================
    // BANKING HELPER METHODS
    // ==========================================

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

    // ==========================================
    // UI METHODS
    // ==========================================

    private void showBlockScreen() {
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        // Set up the rules for our overlay window
        int layoutType = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_PHONE;

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                layoutType,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );

        // Create the Red Screen UI completely in Java!
        blockViewLayout = new LinearLayout(this);
        blockViewLayout.setBackgroundColor(Color.parseColor("#D32F2F")); // A nice deep red
        blockViewLayout.setOrientation(LinearLayout.VERTICAL);
        blockViewLayout.setGravity(Gravity.CENTER);

        // --- THE FIX IS HERE ---
        // 1. Deduct the penalty credits first
        updateBalance(-10);

        // 2. Create the text view
        TextView warningText = new TextView(this);

        // 3. Set the text to show the penalty and the new balance!
        warningText.setText("Distraction Detected!\n-10 Credits\nBalance: " + getBalance());
        warningText.setTextSize(32f);
        warningText.setTextColor(Color.WHITE);
        warningText.setGravity(Gravity.CENTER);
        warningText.setPadding(0, 0, 0, 64);
        // -----------------------

        // Add a button to let them escape back to the home screen
        Button homeButton = new Button(this);
        homeButton.setText("Go Home");
        homeButton.setOnClickListener(v -> {
            // Send the user to the Android home screen
            Intent homeIntent = new Intent(Intent.ACTION_MAIN);
            homeIntent.addCategory(Intent.CATEGORY_HOME);
            homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(homeIntent);
        });

        // Put the text and button onto the red screen
        blockViewLayout.addView(warningText);
        blockViewLayout.addView(homeButton);

        // Slap the whole thing onto the phone's window!
        windowManager.addView(blockViewLayout, params);
        isShowingBlockScreen = true;
    }

    private void removeBlockScreen() {
        if (isShowingBlockScreen && windowManager != null && blockViewLayout != null) {
            windowManager.removeView(blockViewLayout);
            isShowingBlockScreen = false;
        }
    }

    @Override
    public void onInterrupt() {}
}