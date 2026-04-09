# 🏃‍♂️ FocusStep: Sensor-Integrated App Blocker

**FocusStep** is a native Android productivity application that bridges physical activity with digital discipline. Using a "Pay-to-Play" model, the app requires users to earn "Focus Credits" through physical movement (step counting) to unlock access to distracting applications.

## 🌟 Key Features
* **Step-to-Credit Conversion:** Integrates with the device's hardware **Step Counter** sensor. Every 50 steps walked grants the user 1 Focus Credit.
* **Credit-Based Access:** Distracting apps are not just "blocked"—they require a "toll." If the user's balance falls below **10 credits**, the target app is strictly blocked.
* **Temporal Usage Tax:** While using a blocked app, the system deducts 10 credits for every 10 minutes of usage, encouraging shorter sessions.
* **Dynamic App Selection:** Automatically populates a list of all launchable apps on the device for easy targeting.
* **System-Wide Interception:** Utilizes a background `AccessibilityService` to monitor and intercept app launches in real-time.
* **Custom Overlay UI:** Draws a programmatic "Bankruptcy" screen over forbidden apps when credits run out.

## ⚙️ How the Economy Works
1.  **Walk to Earn:** Open the app and start moving. The `SensorEventListener` tracks your steps in the background.
2.  **The Bank:** Your "Wealth" is displayed on the main dashboard, saved persistently in `SharedPreferences`.
3.  **The Entry Fee:** Opening a blocked app (e.g., Instagram) checks your balance.
4.  **The Tax:** If you have credits, you can stay in the app, but you are taxed **10 credits every 10 minutes**.
5.  **The Lockdown:** Once your balance hits **< 10**, the red "Get Back to Work" screen appears and blocks further access.

## 🛠️ Technical Stack
* **Language:** Java
* **Framework:** Android SDK (API 24+)
* **Sensors:** Hardware Step Counter (`Sensor.TYPE_STEP_COUNTER`)
* **Services:** `AccessibilityService` for system-level monitoring.
* **Storage:** `SharedPreferences` for local data persistence.

## 🔐 Permissions Required
FocusStep requires specific high-level permissions to function:
* **`ACTIVITY_RECOGNITION`**: To access step counter data.
* **`SYSTEM_ALERT_WINDOW`**: To draw the "Blocked" screen over other apps.
* **`BIND_ACCESSIBILITY_SERVICE`**: To detect when the user opens a forbidden app.
* **`QUERY_ALL_PACKAGES`**: To list installed applications in the selector.

## 🚀 Installation & Setup
1.  **Build:** Open the project in Android Studio and build the APK.
2.  **Permissions:** Upon first launch, grant the **Physical Activity** permission.
3.  **Draw Over Apps:** Follow the prompt to enable "Appear on top" in system settings.
4.  **Accessibility Service:** Manually go to `Settings > Accessibility > FocusStep` and toggle the service to **ON**.
5.  **Start Walking:** The app will not allow you to open your target app until you have earned at least 10 credits by walking!

## 📂 Project Structure
* `MainActivity.java`: Handles UI, Step Counter logic, and Credit distribution.
* `AppBlockerService.java`: Handles the background monitoring, usage taxation, and the "Bankruptcy" overlay.
* `activity_main.xml`: The main dashboard and app selector UI.
* `AndroidManifest.xml`: Registration of services and system permissions.

***
*Developed as an advanced Android Course Project exploring Hardware-Software integration.*
