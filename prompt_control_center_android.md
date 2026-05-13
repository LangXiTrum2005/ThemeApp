# Prompt: Generate Android Control Center Core (Kotlin)
---

## PROMPT CHÍNH (copy & paste vào Claude)

---

You are an expert Android developer. Generate a complete, production-ready **Control Center core** in Kotlin for Android (minSdk 26, targetSdk 36). The architecture must mirror a real iOS-style floating control panel that works system-wide on top of any app.

### Project package
`com.example.controlcenter`

---

## REQUIRED MODULES — generate ALL of these:

### 1. `ControlCenterService.kt`
An `AccessibilityService` that is the backbone of the entire system.

- Declare in manifest with:
    - `android:permission="android.permission.BIND_ACCESSIBILITY_SERVICE"`
    - `canPerformGestures="true"`
    - `accessibilityEventTypes="typeAllMask"`
    - `android:foregroundServiceType="specialUse"`
    - `priority="99"`
- On `onServiceConnected()`:
    1. Call `startForeground()` with a persistent notification (channel id: `control_center_channel`)
    2. Call `tryInitControl()`
- `tryInitControl()`: reads `ControllerConfig` from SharedPreferences (key: `controller_config_json`), inflates the overlay window, calls `applyConfig(config)`
- `applyConfig(config: ControllerConfig)`: applies colors, icon assets, layout type from config to the live overlay view
- Register a `BroadcastReceiver` for action `ACTION_ACTIVE_CONTROL_PANEL` — on receive, toggle panel visibility
- On `onDestroy()`, remove overlay view from WindowManager and unregister receivers

---

### 2. `OverlayManager.kt`
Manages the `WindowManager` overlay lifecycle.

- `fun show(context: Context, rootView: View)`:
    - Use `WindowManager.LayoutParams` with:
        - `type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY`
        - `flags = FLAG_NOT_FOCUSABLE or FLAG_LAYOUT_IN_SCREEN or FLAG_WATCH_OUTSIDE_TOUCH`
        - `format = PixelFormat.TRANSLUCENT`
        - `gravity = Gravity.TOP or Gravity.START`
        - `width = MATCH_PARENT`, `height = WRAP_CONTENT`
- `fun hide()`: calls `windowManager.removeView(rootView)` safely
- `fun updateLayoutParams(params: WindowManager.LayoutParams)`: for repositioning

---

### 3. `PullBarView.kt`
A custom `ConstraintLayout` that acts as the swipe-to-open trigger.

- A small pill/handle bar (width: 30% of screen, height: 4dp, centered horizontally) anchored to top of screen
- `GestureDetector` + `OnTouchListener`:
    - Track `ACTION_DOWN`, `ACTION_MOVE`, `ACTION_UP`
    - Emit `onPull(distancePx: Float)` callbacks as user drags down
    - On release: if `distancePx > threshold (120dp)` → call `listener.onExpand()`, else call `listener.onCollapse()`
- `interface PullListener { fun onExpand(); fun onCollapse(); fun onPull(distance: Float) }`
- Animate the pill bar alpha 0.4→1.0 as user pulls

---

### 4. `ControlPanelView.kt`
The main panel `ConstraintLayout` (inflated from layout, shown when expanded).

- Accepts a `ControllerConfig` object and binds:
    - **Top-left 2×2 block**: WiFi, Bluetooth, Mobile Data, Airplane Mode toggle icons (`AnimatedToggleView`)
    - **Top-right block**: Media player card with title, artist, progress seek bar, play/pause button
    - **SeekBarControlView** for Brightness (left) and Volume (right)
    - **Bottom row**: Flashlight, Screen Rotation, Do Not Disturb, Screenshot, Screen Record
- Each toggle calls the appropriate system controller on click
- Panel background: rounded corners (24dp), blur background using `RenderEffect.createBlurEffect(16f, 16f, TILE_MODE)` (API 31+), fallback to semi-transparent dark for API <31
- Entrance animation: slide down + fade in (300ms, `OvershootInterpolator`)
- Exit animation: slide up + fade out (250ms, `AccelerateInterpolator`)

---

### 5. `AnimatedToggleView.kt`
A custom `View` for each control toggle (WiFi, BT, etc.).

- Draws a rounded square background
- Shows an icon (`ImageView`) + label (`TextView`) below
- States: `ACTIVE` (filled background, colored icon), `INACTIVE` (dim background, gray icon)
- `fun toggle()`: animates state change with `ObjectAnimator` scaling 1.0→0.85→1.0 (150ms) + background color crossfade
- `fun setState(state: ToggleState, animate: Boolean)`
- `enum class ToggleState { ACTIVE, INACTIVE, UNAVAILABLE }`

---

### 6. System Controllers (one file each):

#### `VolumeController.kt`
```
- AudioManager.getStreamVolume(STREAM_MUSIC) / setStreamVolume()
- AudioManager.getStreamVolume(STREAM_RING) / setRingerMode()
- Register BroadcastReceiver for VOLUME_CHANGED_ACTION
- fun setMediaVolume(progress: Int)  // 0-100 mapped to stream max
- fun setRingerMode(mode: RingerMode)  // enum: NORMAL, VIBRATE, SILENT
- fun getCurrentVolume(): Int
```

#### `BrightnessController.kt`
```
- Settings.System.SCREEN_BRIGHTNESS_MODE → set to MANUAL first
- Settings.System.putInt(resolver, SCREEN_BRIGHTNESS, value)
- fun setBrightness(progress: Int)  // 0-100 mapped to 0-255
- fun getBrightness(): Int
- Requires WRITE_SETTINGS permission check before calling
```

#### `NetworkController.kt`
```
- WifiManager: isWifiEnabled, setWifiEnabled (API <29), WifiManager.startScan()
- ConnectivityManager + NetworkCallback to monitor connectivity
- BroadcastReceiver for AIRPLANE_MODE_CHANGED_ACTION
- fun toggleWifi()
- fun toggleAirplaneMode(context: Context)  // uses Settings.Global.AIRPLANE_MODE_ON + sendBroadcast
- fun isMobileDataEnabled(): Boolean  // via ConnectivityManager
- var onWifiStateChanged: ((Boolean) -> Unit)?
- var onAirplaneModeChanged: ((Boolean) -> Unit)?
```

#### `BluetoothController.kt`
```
- BluetoothAdapter.getDefaultAdapter()
- fun toggleBluetooth()  // enable/disable via BluetoothAdapter
- fun isEnabled(): Boolean
- Register for BluetoothAdapter.ACTION_STATE_CHANGED
- var onStateChanged: ((Boolean) -> Unit)?
```

#### `FlashlightController.kt`
```
- CameraManager.registerTorchCallback()
- fun toggle()  // setTorchMode(cameraId, !isOn)
- fun isOn(): Boolean
- var onTorchChanged: ((Boolean) -> Unit)?
```

#### `MediaController.kt`
```
- MediaSessionManager.getActiveSessions(notificationListenerToken)
- fun play() / pause() / next() / previous()
- fun getCurrentTrack(): TrackInfo?  // data class: title, artist, albumArt: Bitmap?, duration, position
- fun seekTo(positionMs: Long)
- var onTrackChanged: ((TrackInfo?) -> Unit)?
- var onPlaybackStateChanged: ((Boolean) -> Unit)?  // isPlaying
```

---

### 7. `ControllerConfig.kt`
Data class serializable to/from JSON (use `kotlinx.serialization`):

```kotlin
@Serializable
data class ControllerConfig(
    val pullBarColor: String = "#FFFFFF",      // hex
    val pullBarAlpha: Float = 0.6f,
    val panelBackground: String = "#CC1C1C1E", // ARGB hex
    val panelCornerRadius: Float = 24f,        // dp
    val iconSize: Float = 44f,                 // dp
    val activeColor: String = "#FFFFFF",
    val inactiveColor: String = "#636366",
    val widgetType: String = "DEFAULT",        // "DEFAULT" | "COMPACT" | "EXPANDED"
    val enableBlur: Boolean = true,
    val enableHaptic: Boolean = true,
    val visibleToggles: List<String> = listOf(
        "wifi", "bluetooth", "mobile_data", "airplane",
        "brightness", "volume", "flashlight",
        "rotation", "dnd", "screenshot", "record"
    )
)
```

---

### 8. `BootReceiver.kt`
```kotlin
// BroadcastReceiver for ACTION_BOOT_COMPLETED
// Starts ControlCenterService via startForegroundService()
// Only if user has previously enabled the service (SharedPreferences check)
```

---

### 9. `PermissionHelper.kt`
Utility object with:
```
- fun hasOverlayPermission(context): Boolean  // Settings.canDrawOverlays()
- fun hasAccessibilityEnabled(context): Boolean  // AccessibilityManager check
- fun hasWriteSettings(context): Boolean  // Settings.System.canWrite()
- fun hasNotificationAccess(context): Boolean  // NotificationListenerService check
- fun openOverlaySettings(context)
- fun openAccessibilitySettings(context)
- fun openWriteSettingsPage(context)
- fun openNotificationAccessSettings(context)
- fun getMissingPermissions(context): List<RequiredPermission>
  // returns list of enum: OVERLAY, ACCESSIBILITY, WRITE_SETTINGS, NOTIFICATION_ACCESS
```

---

### 10. `AndroidManifest` entries
Provide the complete `<service>`, `<receiver>`, and `<uses-permission>` XML blocks needed for all the above, including:
- `SYSTEM_ALERT_WINDOW`
- `WRITE_SETTINGS`
- `ACCESS_NOTIFICATION_POLICY`
- `BIND_ACCESSIBILITY_SERVICE`
- `BIND_NOTIFICATION_LISTENER_SERVICE`
- `CAMERA`
- `FOREGROUND_SERVICE`
- `FOREGROUND_SERVICE_SPECIAL_USE`
- `RECEIVE_BOOT_COMPLETED`
- `BLUETOOTH_CONNECT` (API 31+)

---

## CONSTRAINTS:
- Language: **Kotlin only**
- Architecture: **MVVM** where applicable, otherwise plain class/object
- No third-party libraries except: `kotlinx.serialization`, `kotlinx.coroutines`
- Each file must compile standalone (no missing imports)
- Add `// TODO: inject via DI` comments where dependency injection would normally go
- All system calls that require permissions must guard with `try/catch` and log errors with `Log.e("ControlCenter", ...)`
- Minimum Android API: **26** (use `@RequiresApi` annotations for higher API calls)

---

## OUTPUT FORMAT:
For each file, output:
```
### `FileName.kt`
```kotlin
// full file content
```
```

Generate all 10 modules in order. Do not skip any.

---

## OPTIONAL FOLLOW-UP PROMPTS (dùng sau khi gen xong core)

### Thêm SeekBarControlView:
```
Now generate `SeekBarControlView.kt` — a custom View that renders as a tall rounded pill.
Dragging up/down changes the fill level (0–100%). Shows an icon at the bottom.
Used for both brightness and volume. Expose: `var onProgressChanged: ((Int) -> Unit)?` and `fun setProgress(value: Int, animate: Boolean)`.
Use the same ControllerConfig color scheme. Haptic feedback on every 10% step if `config.enableHaptic` is true.
```

### Thêm NotificationListener:
```
Generate `ControlNotificationListener.kt` — a NotificationListenerService.
On `onNotificationPosted()`, extract: app name, title, text, icon.
Emit via a SharedFlow (singleton object `NotificationBus`).
On `onNotificationRemoved()`, emit removal event.
The ControlPanelView should collect from NotificationBus and show a mini badge count on the panel.
```

### Thêm live config từ server:
```
Generate `ConfigRepository.kt` that:
1. Tries to load `controller_config_json` from SharedPreferences (cache)
2. If network available, fetches JSON from a configurable URL (stored in SharedPreferences key `config_url`, default `https://yourserver.com/control_spec.json`)
3. Parses into ControllerConfig using kotlinx.serialization
4. Saves to cache and emits via StateFlow<ControllerConfig>
   Use kotlinx.coroutines + HttpURLConnection (no OkHttp dependency).
```

### Thêm widget providers:
```
Generate `ControlWidget1x1Provider.kt`, `ControlWidget2x1Provider.kt`, `ControlWidget2x2Provider.kt` —
AppWidgetProvider subclasses.
Each widget shows a different subset of toggles based on size.
On widget click, send broadcast `ACTION_ACTIVE_CONTROL_PANEL` to ControlCenterService.
Include the corresponding `res/xml/widget_*_info.xml` AppWidgetProviderInfo files.
```