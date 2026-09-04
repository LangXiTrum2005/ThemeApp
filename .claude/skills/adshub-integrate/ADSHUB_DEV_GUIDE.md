# ADSHUB SDK - TÀI LIỆU TÍCH HỢP CHO DEVELOPER (ANDROID)

Tài liệu này hướng dẫn các Developer cách tích hợp và sử dụng **AdsHub SDK (Kotlin)** vào ứng dụng Android. Triết lý của AdsHub là **"Code 1 lần - Cấu hình vạn năng"**. Toàn bộ logic tần suất, vị trí, mạng quảng cáo đều được định nghĩa trên Web Admin, App Dev chỉ việc "đặt sẵn bẫy" và gọi hàm.

---

## 1. KHỞI TẠO SDK (Initialization)

Việc khởi tạo SDK được chia làm **2 bước bắt buộc**. Nếu bỏ sót bất kỳ bước nào, quảng cáo sẽ không hiển thị. File cấu hình gốc phải được đặt tên là `ads_config.json` và bỏ vào thư mục `assets`.

### Bước 1.1: Khởi tạo ở Application (Nền tảng SDK)
Trong class `Application` của app, gọi hàm `init` ở hàm `onCreate()`. 

```kotlin
import android.app.Application
import com.adshub.monetization.AdsController

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Khởi tạo SDK: Nạp cấu hình từ assets/ads_config.json
        // Khởi động các Tracker theo dõi Lifecycle, Open Ad, tự động dọn dẹp RAM...
        AdsController.init(this)
    }
}
```

### Bước 1.2: Xin quyền UMP & Khởi động AdMob (Bắt buộc ở Activity đầu tiên)
Google BẮT BUỘC phải gọi màn hình xin quyền UMP (Châu Âu/Anh) trước khi gọi API của AdMob. Nếu không, toàn bộ Quảng cáo sẽ bị khóa.
Tại **SplashActivity** hoặc **MainActivity**, bạn cần gọi luồng Coroutine sau:

```kotlin
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.adshub.monetization.init.CmpManager
import com.adshub.monetization.init.AdNetworkInitializer
import com.adshub.monetization.engine.cache.PreloadEngine

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        lifecycleScope.launch {
            // 1. Hiển thị form xin quyền UMP (Tự động bypass nếu user không ở EU/Anh)
            CmpManager.requestConsent(this@MainActivity)
            
            // 2. Kích hoạt toàn bộ SDK Quảng cáo (AdMob, AppLovin...)
            AdNetworkInitializer.initNetworks(applicationContext)
            
            // 3. Bắn tín hiệu "App Start" để SDK nạp đạn (Preload) trước cho Splash/Onboarding
            PreloadEngine.onAppStartOrConsentReady()
        }
    }
}
}
```

### Bước 1.2.1: Chờ tải Ads (Wait For Inventory) tại Màn Hình Splash (Jetpack Compose)
Để tránh hiện tượng tải mạng 2 lần hoặc lỗi hiển thị Skeleton không cần thiết ở màn Onboarding, bạn cần ép màn hình Splash đứng chờ quá trình Preload hoàn tất (với thời gian chờ giới hạn lấy từ `timeout_ms` trong cấu hình Inventory):

```kotlin
// Trong AppNavigation.kt
composable(Routes.SPLASH) {
    SplashScreen()
    BindAdScreen("splash") // Rất quan trọng để tracking
    
    val activity = com.magnifier.glass.camera.util.getActivitySafely()
    
    LaunchedEffect(isFirstLaunch) {
        if (isFirstLaunch != null) {
            // Chỉ bắt khách mới chờ tải Native của Onboarding
            if (isFirstLaunch == true) {
                // Đọc timeout_ms của 'inv_native_lan' từ JSON (VD: 4000ms) để chờ
                com.adshub.monetization.AdsController.waitForInventory("inv_native_lan")
            }
            
            // Xong việc thì show App Open (Nếu có)
            com.adshub.monetization.AdsController.trigger(activity, "app_open") {
                if (isFirstLaunch == true) {
                    navController.navigate(Routes.ONBOARDING) { popUpTo(Routes.SPLASH) { inclusive = true } }
                } else {
                    navController.navigate(Routes.MAGNIFIER) { popUpTo(Routes.SPLASH) { inclusive = true } }
                }
            }
        }
    }
}
```

### Bước 1.3: Cấu hình Manifest (Xử lý xung đột App ID)
Vì SDK AdsHub đã chứa sẵn một mã Test App ID bên trong thư viện để dự phòng, nên khi tích hợp vào App thực tế, bạn bắt buộc phải khai báo App ID thật và ghi đè (override) lên mã test đó để tránh lỗi `Manifest Merger Failed` khi Build.

Trong file `app/src/main/AndroidManifest.xml` của bạn:
1. Thêm dòng `xmlns:tools="http://schemas.android.com/tools"` vào thẻ `<manifest>`.
2. Thêm thuộc tính `tools:replace="android:value"` vào thẻ `<meta-data>` của AdMob.

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"> <!-- 1. Thêm dòng này -->

    <application>
        <!-- 2. Khai báo App ID thật của bạn -->
        <meta-data
            android:name="com.google.android.gms.ads.APPLICATION_ID"
            android:value="ca-app-pub-XXXXXXXXXXXXXXXX~XXXXXXXXXX"
            tools:replace="android:value" /> <!-- 3. BẮT BUỘC thêm cờ này -->
    </application>
</manifest>
```

### 1.4. Cập nhật cấu hình từ Remote Config (Firebase)
Ngay khi app tải được chuỗi JSON cấu hình mới từ Firebase, hãy bơm nó vào SDK để ghi đè cấu hình cũ.

**⚠️ CẢNH BÁO QUAN TRỌNG (LỖI RACE CONDITION CHẾT NGƯỜI):**
Tuyệt đối KHÔNG gọi `PreloadEngine.onAppStartOrConsentReady()` chạy song song với tiến trình tải Remote Config. Nếu làm vậy, tốc độ code chạy local luôn nhanh hơn mạng, SDK sẽ nạp quảng cáo với cấu hình cũ. (Ví dụ: Trên Web Admin bạn đã tắt Ads, nhưng App vẫn nạp bằng config cũ và đẩy Request lên AdMob, làm sụt giảm nghiêm trọng Show Rate vì Request sinh ra nhưng không được hiển thị).

**Giải pháp:** Bắt buộc phải ép luồng khởi tạo Splash **đợi Remote Config tải xong** (có kèm Timeout) trước khi bắt đầu Preload:

```kotlin
import kotlinx.coroutines.async
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.delay

// Đặt đoạn mã này bên trong luồng khởi tạo Coroutine (MainActivity)
lifecycleScope.launch {
    // 1. Chạy SONG SONG tiến trình tải Remote Config (Không chờ UMP để tiết kiệm mạng)
    val rcDeferred = async {
        val remoteConfig = com.google.firebase.remoteconfig.FirebaseRemoteConfig.getInstance()
        try {
            remoteConfig.fetchAndActivate().await()
        } catch (e: Exception) {
            false
        }
    }
    
    // 2. Chạy tiến trình xin quyền UMP (Có thể hiện Form đè lên màn hình Splash)
    com.adshub.monetization.init.CmpManager.requestConsent(this@MainActivity)
    
    // 3. Chờ CẢ HAI tiến trình (UMP và Remote Config) hoàn tất
    val rcSuccess = rcDeferred.await()
    if (rcSuccess == true) {
        val configJson = com.google.firebase.remoteconfig.FirebaseRemoteConfig.getInstance().getString("ads_config_v1")
        if (configJson.isNotEmpty() && configJson != "{}") {
            com.adshub.monetization.AdsController.updateRemoteConfig(configJson)
        }
    }

    // 4. Báo cho SDK biết user này là khách mới hay cũ để SDK nạp đúng cấu hình quảng cáo (quan trọng)
    val prefs = getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
    val isFinished = prefs.getBoolean("has_completed_onboarding", false)
    com.adshub.monetization.AdsController.setUserProperty("is_onboarding_completed", isFinished)

    // 5. Chỉ khởi tạo mạng Ads và nạp đạn SAU KHI đã chắc chắn có Config mới và User đã trả lời Form Consent
    com.adshub.monetization.init.AdNetworkInitializer.initNetworks(applicationContext)
    
    delay(300L) // Đợi một nhịp nhỏ cho I/O ghi file an toàn
    com.adshub.monetization.engine.cache.PreloadEngine.onAppStartOrConsentReady()
}
```

### 1.4.1. Khóa cứng (Freeze) Màn Hình Splash chờ SDK
Nếu bạn dùng Jetpack Compose, màn hình Splash thường có Animation. Nếu Animation chạy hết trước khi `requestConsent` (UMP Form) hoặc Remote Config tải xong, App sẽ nhảy sang màn tiếp theo, lúc này Form UMP mới hiện lên đè vào (Gây lỗi logic nghiêm trọng).

**Giải pháp chuẩn:** Ép luồng Compose phải đợi cờ `PreloadEngine.hasTriggeredAppStart` bật lên `true`. (Cờ này tự động bật bởi SDK sau khi gọi `onAppStartOrConsentReady`).

```kotlin
@Composable
fun SplashScreen(isFinished: Boolean, onFinish: () -> Unit) {
    LaunchedEffect(Unit) {
        // Có thể để Animation chạy song song ở đây
        // delay(3000)
        
        // BƯỚC 1: Bắt buộc Splash phải dừng lại chờ SDK khởi tạo xong (Chờ User bấm UMP Form nếu có)
        // Loại bỏ bộ đếm thời gian (timeout) vì nếu User ở Châu Âu, họ có thể tốn 10-20 giây 
        // để đọc và bấm nút Agree. Splash bắt buộc phải chờ mỏi mòn!
        while (!com.adshub.monetization.engine.cache.PreloadEngine.hasTriggeredAppStart) {
            kotlinx.coroutines.delay(100)
        }
        
        // BƯỚC 2: Cực kỳ quan trọng - Chờ quảng cáo TẢI TỪ MẠNG VỀ (AdMob)
        // Nếu không gọi hàm này, trigger() sẽ kiểm tra thấy cache rỗng và bỏ qua luôn quảng cáo!
        if (!isFinished) {
            // Khách mới: Đợi Inter Splash. Nếu Inter show, native kế tiếp có thể được preload
            // bằng trigger.preload_after_show trong lúc user đang xem countdown.
            val isSplashInterReady = com.adshub.monetization.AdsController.waitForInventory("inv_inter_splash")

            // Fallback: nếu Inter Splash không có ad, chủ động preload Native Language
            // trước khi chuyển màn để slot đầu tiên không vào cache rỗng.
            if (!isSplashInterReady) {
                com.adshub.monetization.AdsController.preload("inv_native_lfo1")
                com.adshub.monetization.AdsController.waitForInventory("inv_native_lfo1")
            }
        } else {
            // Khách cũ: Chỉ cần đợi tải Interstitial Splash cho khách cũ
            com.adshub.monetization.AdsController.waitForInventory("inv_inter_splash_2nd")
        }
            
        onFinish() // Chỉ chuyển màn khi mọi thứ đã tải xong xuôi (hoặc timeout mạng)
    }
}
```

### 1.5. Thiết lập hệ thống Tracking (Analytics)
SDK AdsHub đi kèm với bộ `TrackingManager` rất mạnh mẽ để theo dõi toàn bộ phễu quảng cáo (Load -> Show -> Click). Tuy nhiên, để tiết kiệm chi phí Server, bạn nên thiết lập cầu nối chuyển hướng Log như sau:

```kotlin
import com.adshub.monetization.engine.tracking.TrackingManager

// 1. Chạy trong môi trường Dev (Debug) thì bật cờ DebugMode để SDK in Log ra Console
TrackingManager.isDebugMode = BuildConfig.DEBUG

if (BuildConfig.DEBUG) {
    // Bản DEV: Đẩy Log thẳng lên Firebase Realtime Database (RTDB) để AdOps dễ soi theo thời gian thực
    TrackingManager.setProvider(FirebaseLogProvider()) 
} else {
    // Bản RELEASE: Đẩy Log gộp ngầm vào Firebase Analytics chuẩn để không tốn tiền Server
    TrackingManager.setProvider(FirebaseAnalyticsProvider(context))
}
```
*Lưu ý: `FirebaseLogProvider` và `FirebaseAnalyticsProvider` là 2 class bạn tự implement theo Interface `AnalyticsProvider` của SDK.*

---

## 2. TÍCH HỢP IN-APP PURCHASE (IAP / BÁN GÓI VIP)

Từ phiên bản mới nhất, AdsHub SDK đã nhúng sẵn module quản lý thanh toán Google Play Billing thông qua `AdsHubBillingManager`. Mọi thao tác kết nối, đồng bộ trạng thái, verify biên lai, và tự động ẩn quảng cáo đều được SDK lo 100%.

**LƯU Ý QUAN TRỌNG:** Để tối ưu dung lượng và tránh ép các App miễn phí phải xin quyền Billing, SDK sử dụng cơ chế `compileOnly` cho thư viện Billing. Do đó, nếu App của bạn **CÓ BÁN** In-App Purchase, bạn **BẮT BUỘC** phải thêm dòng sau vào file `app/build.gradle.kts` của App trước khi làm các bước tiếp theo:
```kotlin
dependencies {
    implementation("com.android.billingclient:billing-ktx:7.0.0")
}
```

Sau khi đã thêm thư viện vào App, bạn chỉ cần làm 3 bước rất đơn giản sau:

### Bước 2.1: Khởi tạo danh sách Gói cước (Tại App.kt)
Sau khi gọi `AdsController.init()`, hãy khai báo ngay các `ProductID` (chính xác như trên Google Play Console) mà App của bạn muốn bán:

```kotlin
// Khởi tạo hệ thống thanh toán (Truyền danh sách gói muốn bán)
com.adshub.monetization.AdsController.initBilling(
    context = this, 
    productIds = listOf("file_yearly", "file_monthly")
)
```

### Bước 2.2: Gọi hàm thanh toán (Tại màn hình mua VIP)
Khi User bấm nút "Mua ngay", chỉ cần gọi duy nhất 1 dòng lệnh dưới đây. SDK sẽ tự lo việc bung bảng giá Google Play (bao gồm cả Dùng thử miễn phí Free Trial nếu có):

```kotlin
// activity là Context của màn hình hiện tại
com.adshub.monetization.billing.AdsHubBillingManager.getInstance(context)
    .launchBillingFlow(activity, "file_yearly")
```

### Bước 2.3: Lắng nghe kết quả & Tặng thưởng (Tại ViewModel/MainActivity)
SDK đã lo việc gạt cờ vô hiệu hoá Quảng Cáo rồi, việc của App là gài một cái "tai nghe" để tặng thêm các phần quà đặc thù (VD: Tặng Coin, Mở khóa tính năng Pro) nếu User mua thành công:

```kotlin
lifecycleScope.launch {
    // Lắng nghe tín hiệu thanh toán thành công từ SDK
    com.adshub.monetization.billing.AdsHubBillingManager.getInstance(context)
        .purchaseEvents.collect { productId ->
            when (productId) {
                "coin_pack_100" -> Database.addCoin(100)
                "file_yearly" -> FeatureManager.unlockProFeatures()
            }
        }
}
```

*Lưu ý: Bạn vẫn có thể dùng hàm `AdsController.setPurchasedUserStatus(true)` thủ công nếu App sử dụng hệ thống thanh toán riêng ngoài Google Play.*

---

## 3. TÍCH HỢP QUẢNG CÁO NỘI TUYẾN (NATIVE & BANNER)

Thay vì phải code rườm rà `loadAd()`, `showAd()`, `NativeAdView`, v.v... AdsHub cung cấp cơ chế **Auto-Bind**.

### Bước 1: Chuẩn bị giao diện (XML)
Đặt một cái `FrameLayout` vào vị trí bạn muốn hiện Ads trong file XML (hoặc Jetpack Compose AndroidView). Bạn **BẮT BUỘC PHẢI** gán thuộc tính `android:tag` cho nó theo cú pháp `ads_slot:TÊN_SLOT_ID`. Tên này phải khớp với trường `slot_id` trong cấu hình Inventory trên Web Admin.

```xml
<FrameLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:tag="ads_slot:home_bottom_banner" />
```

*Mẹo: SDK tích hợp sẵn công nghệ **Zero Layout Shift (Chống giật Layout)**. Khi bạn đặt Slot, SDK sẽ tự động bơm 1 cái khung xám nhấp nháy (Skeleton) giữ chỗ. Nếu mạng rớt không tải được quảng cáo, khung này sẽ được thu nhỏ mượt mà dần về 0 rồi biến mất, đảm bảo user không bị giật màn hình. Phù hợp hoàn hảo cho các trang **Lướt vô tận (Infinite Feed / RecyclerView)**!*

### Bước 2: Khai báo Màn hình
Trong `onCreate` của Activity hoặc `onViewCreated` của Fragment, bạn chỉ cần báo cáo cho SDK biết User vừa vào màn hình nào. 

```kotlin
// Hàm này sẽ tự động:
// 1. Quét toàn bộ rootView tìm các FrameLayout có id khớp với slot_id trên Web Admin để đắp Ads vào.
// 2. Kích hoạt tính năng tải ngầm (Preload) nếu Web Admin cấu hình "Preload Trigger: screen_enter".
AdsController.bindScreen(
    screenId = "home_screen", // Trùng với chữ 'screen' khai báo trên Web Admin
    rootView = binding.root,
    lifecycleOwner = this
)
```

### Bước 3: Cấu hình Jetpack Compose (Bắt buộc nếu App dùng Compose)
Với Jetpack Compose, hàm `on_resume` có thể bị gọi nhiều lần do đặc thù Recomposition. AdsHub SDK đã trang bị sẵn cơ chế **Deduplication (Chống trùng lặp)** bằng cách sử dụng `Map` lưu trữ các Task trong hàng đợi `pendingTasksOnAdClose`.
Bạn cần bọc Native View vào trong một `AndroidView` và gắn `DisposableEffect` để tự quản lý vòng đời cho từng Slot thay vì dựa vào Activity Lifecycle:

```kotlin
@Composable
fun AdsHubSlot(slotId: String, currentScreen: String, modifier: Modifier = Modifier) {
    val lifecycleOwner = LocalLifecycleOwner.current

    AndroidView(
        modifier = modifier.fillMaxWidth().wrapContentHeight(),
        factory = { context ->
            FrameLayout(context).apply {
                // Đổi tag để AdsSlotScanner không quét nhầm root
                tag = "compose_ads_slot:$slotId"
            }
        },
        update = { view ->
            // Mỗi khi Recompose, SDK tự động Deduplicate (Loại bỏ yêu cầu tải trùng lặp)
            com.adshub.monetization.engine.inline.AdsSlotScanner.processSingleSlot(currentScreen, slotId, view)
        }
    )

    DisposableEffect(slotId, lifecycleOwner) {
        var isInitialResume = true
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                if (isInitialResume) {
                    isInitialResume = false
                    return@LifecycleEventObserver
                }
                // Hỗ trợ tự refresh Ads khi user thoát app rồi quay lại
                val slotConfig = com.adshub.monetization.config.ConfigManager.currentConfig.slots[slotId]
                if (slotConfig?.refreshPolicy == "on_resume") {
                    val view = com.adshub.monetization.engine.inline.AdsSlotScanner.getSlotContainer(slotId)
                    com.adshub.monetization.engine.inline.AdsSlotScanner.removeSlotFromRegistry(slotId)
                    if (view != null) {
                        com.adshub.monetization.engine.inline.AdsSlotScanner.processSingleSlot(currentScreen, slotId, view)
                    }
                }
            } else if (event == androidx.lifecycle.Lifecycle.Event.ON_DESTROY) {
                com.adshub.monetization.engine.inline.AdsSlotScanner.removeSlotFromRegistry(slotId)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            com.adshub.monetization.engine.inline.AdsSlotScanner.removeSlotFromRegistry(slotId)
        }
    }
}
```

#### Native Ad load xong sau khi Compose Slot đã rời màn hình

Native Ad có thể mất vài giây để tải. Trong thời gian đó Compose có thể dispose container cũ do user chuyển màn hình, `AnimatedContent` đổi state, hoặc recomposition tạo container mới.

AdsHub kiểm tra lại identity của container trước mọi lần render:

```kotlin
AdsSlotScanner.getSlotContainer(slotId) === container
```

Nếu kết quả là `false`, SDK sẽ:

1. Không render Native Ad vào View cũ.
2. Không ghi `show_request` cho lần render không tồn tại.
3. Trả Native Ad về cache theo đúng `inventory_id`.
4. Hoàn lại bộ đếm `ScreenRuleEvaluator` để slot mới không bị chặn nhầm.

Khi cùng inventory được yêu cầu lần sau, ad trong cache sẽ được lấy ra để render. Log chẩn đoán:

```text
AdsHub: [DEBUG] Native Ad '...' tải xong nhưng slot ... đã rời màn hình. Giữ lại trong cache.
```

`AdCacheManager.putAd()` cũng chống lưu trùng cùng một Ad Object. Vì vậy việc trả ad về cache an toàn cả với `pollAd()` và `peekAd()`.

#### Tối ưu Show Rate cho Native Inline

Show rate của Native Inline giảm khi ad đã matched/load thành công nhưng user chưa bao giờ đi tới slot hoặc rời màn hình trước impression. Để hạn chế:

*   Đặt `preload_triggers.screen_enter` ở màn ngay trước màn chứa slot, không preload quá sớm nhiều bước.
*   Với Native nằm ngay sau Splash Inter, có thể để `preload_triggers` rỗng và cấu hình `preload_after_show` trên trigger Inter Splash. SDK chỉ preload sau khi fullscreen thật sự show, ví dụ sau `1000ms`, giúp tận dụng thời gian countdown nhưng tránh request khi Inter không show.
*   Nếu dùng `preload_after_show`, host app nên fallback khi Inter không ready: gọi `AdsController.preload(inventoryId)` rồi `waitForInventory(inventoryId)` trước khi chuyển sang màn chứa slot.
*   Chỉ bật `refresh_policy = "on_resume"` khi thực sự cần làm mới slot (sẽ gây giật UI do hiện Skeleton). Khuyến nghị dùng `refresh_policy = "on_click"` để làm mới mượt mà (Seamless Reload) ngay khi người dùng click vào Ads.
*   Dùng Logcat để so sánh `load_success`, `show_request`, và `show_success`. `show_success` chỉ xuất hiện khi AdMob gửi callback impression.

### Bước 4: Tuỳ biến Giao diện Toàn Cục (Global Native Ad Style)
Với AdsHub, bạn **KHÔNG CẦN** phải copy và sửa từng file XML để đổi màu, bo góc cho hợp với Design System của App. Thay vào đó, hãy cấu hình 1 lần duy nhất tại `MyApplication` (hoặc sau khi có cấu hình Theme):

```kotlin
AdsController.globalNativeAdStyle = com.adshub.monetization.model.NativeAdStyleConfig(
    backgroundColorRes = R.color.ds_card_bg,         // Màu nền
    cornerRadiusDp = 24f,                            // Bo góc viền ngoài
    elevationDp = 0f,                                // Đổ bóng
    borderSizeDp = 1f,                               // Kích thước viền
    borderColorRes = R.color.ds_surface_variant,     // Màu viền
    
    // Padding 4 hướng
    paddingLeftDp = 16f,
    paddingTopDp = 12f,
    paddingRightDp = 16f,
    paddingBottomDp = 12f,
    
    // Đổi màu Text hỗ trợ Dark/Light Theme (Nên truyền Resource ID)
    textColorPrimaryRes = R.color.ds_text_primary,
    textColorSecondaryRes = R.color.ds_text_secondary,
    
    // Tuỳ chỉnh Nút bấm Call-To-Action
    buttonBackgroundColorRes = R.color.ds_primary,
    buttonTextColorRes = R.color.ds_background,
    buttonCornerRadiusDp = 12f,
    
    // Kích thước nút bấm TỰ ĐỘNG ĐIỀU CHỈNH theo kích thước Template từ Web Admin
    buttonHeightSDp = 40f,   // Dùng cho native_small
    buttonHeightMDp = 48f,   // Dùng cho native_medium
    buttonHeightLDp = 54f,   // Dùng cho native_large
    buttonHeightXlDp = 60f   // Dùng cho native_xlarge, native_full
)
```
*Mẹo: SDK sử dụng `templateType.contains()` để ánh xạ chiều cao nút bấm. Tức là nếu App của bạn tự nhét thêm file XML `admob_native_large.xml` vào thư mục của App, SDK vẫn tự động nhận diện chữ `large` và gán chiều cao 54dp cho nút bấm của file đó!*

### Bước 4.1: Tuỳ biến cấp cao bằng Hệ Thống Thẻ Tag (Advanced Tagging System)
Mặc định, `NativeAdRenderer` sẽ tự động tìm kiếm gốc thẻ `NativeAdView` để tô màu nền/bo góc, và tìm thẻ nội dung (content) để đẩy padding. Tuy nhiên, nếu bạn tạo ra các file XML phức tạp (như mẫu `full` hay `xfull`) và muốn chỉ định **chính xác** thẻ nào chịu hiệu ứng nào, hãy gắn thuộc tính `android:tag` vào Layout của bạn:

*   **`ad_background_container`**: View nhận thuộc tính bo góc (Radius), viền (Stroke) và màu nền (Background). Nếu không tìm thấy, mặc định chọn Root View.
*   **`ad_content_container`**: View nhận 4 thông số Padding. Nếu không tìm thấy, mặc định chọn Root View.

**Thẻ Miễn Nhiễm (Exceptions):**
Để SDK không phá vỡ thiết kế tự chế của bạn (Ví dụ: Bạn vẽ sẵn một Background Gradient xịn xò), hãy gắn thêm các đuôi `ignore_...`:
*   `ignore_bg`: Bỏ qua việc ghi đè Màu Nền (Background Color) và Viền (Border). Vẫn giữ lại Bo Góc (Radius).
*   `ignore_padding_v`: Không ghi đè Padding Top và Padding Bottom. (Chỉ áp dụng Padding trái/phải).
*   `ignore_padding_h`: Không ghi đè Padding Left và Padding Right.

*Ví dụ cấu hình cho màn XFull (Vừa tràn viền ảnh Media, vừa có background Gradient dưới chân):*
```xml
<com.google.android.gms.ads.nativead.NativeAdView
    ...
    android:tag="ad_background_container,ignore_bg"> <!-- Root chỉ nhận bo góc, không nhận màu -->

    <LinearLayout
        android:id="@+id/ad_content_container"
        android:tag="ad_content_container,ignore_padding_v" <!-- Chỉ nhận padding ngang, bỏ qua dọc -->
        android:background="@drawable/gradient_custom">
```

### Bước 4.2: Đổi Hiệu ứng Loading (Lottie) Toàn Cục
Ngoài giao diện Native, bạn cũng có thể tuỳ chỉnh vòng xoay chờ quảng cáo (Loading Animation) cho toàn bộ SDK bằng cách gán `Resource ID` của 1 file Lottie (.json) nằm trong thư mục `res/raw`.

```kotlin
// Ví dụ: Muốn dùng file res/raw/hourglass_loading.json
AdsController.globalLoadingLottieRes = R.raw.hourglass_loading
```
SDK sẽ tự động đọc cấu hình này để hiển thị Lottie thay vì ProgressBar mặc định ở các tính năng như Native Reload-On-Show, App Open Ad Loading, v.v.

---

## 4. TÍCH HỢP QUẢNG CÁO TOÀN MÀN HÌNH (FULLSCREEN)

Với AdsHub, Developer KHÔNG cần kiểm tra mạng, KHÔNG cần kiểm tra quảng cáo đã tải xong chưa. Cứ gọi lệnh `Show`, mọi thứ còn lại (bao gồm chặn Capping, giãn cách thời gian Pacing) SDK sẽ tự tính toán.

### 4.1. Interstitial (Quảng cáo chuyển cảnh)
Sử dụng khi user chuyển trang hoặc hoàn thành một tác vụ (không có phần thưởng).

```kotlin
binding.btnNextPage.setOnClickListener {
    AdsController.trigger(
        activity = this,
        triggerId = "click_next_btn", // Khớp với ID của Trigger trên Web Admin
        onFinished = {
            // Callback này LUÔN ĐƯỢC GỌI dù Ads có hiện thành công hay bị chặn bởi Capping/Safety Rules.
            // Bắt đầu xử lý logic App (Ví dụ: chuyển trang) tại đây.
            navigateToNextPage()
        }
    )
}
```

### 4.2. Rewarded Ads (Quảng cáo có thưởng)
Sử dụng khi User chủ động click xem quảng cáo để đổi lấy vật phẩm/tính năng.

```kotlin
// 1. Khai báo một Object/File quản lý quà tặng chung cho Toàn App
object AppRewardManager {
    fun processRewards(rewards: List<com.adshub.monetization.model.RewardConfig>) {
        rewards.forEach { qua ->
            when (qua.type) {
                "coin" -> Wallet.addCoin(qua.amount) // Cộng xu tự động
                "save_file" -> DownloadManager.startDownload() // Thực thi lệnh đang chờ
            }
        }
    }
}

// 2. Tại nút bấm, sử dụng mô hình "Lưu Trạng Thái Trước - Xem Ads - Chạy Sau"
binding.btnDownload.setOnClickListener {
    // Bước 1: Nói cho App biết là mình định tải cái gì (Nhưng chưa tải vội)
    DownloadManager.setTargetUrl("https://link_file_cua_ban.mp4")

    // Bước 2: Kích nổ Ads và truyền tham chiếu hàm (Siêu ngắn)
    AdsController.trigger(
        activity = this,
        triggerId = "trigger_tai_file",
        onRewardEarned = AppRewardManager::processRewards, // <-- TRUYỀN THAM CHIẾU HÀM
        onFinished = {
            // Tắt Loading, dọn dẹp UI
            hideLoadingSpinner()
        }
    )
}
```

### 4.3. Nút bấm thông minh (AdsHubClickableBox - Jetpack Compose)
Trong các dự án sử dụng Jetpack Compose, bạn **được khuyến khích** phân biệt 2 loại Trigger để giao diện được trực quan nhất:

**Loại 1: Gắn vào thao tác bấm của người dùng (UI Buttons, Back, Save, Item Click)**
Nên sử dụng `AdsHubClickableBox` thay cho `Box/Button` thông thường. Component này tự động đọc cấu hình JSON để **vẽ Tag "Ad"** ở góc phải (nếu quảng cáo bật), và tự động gọi `AdsController.trigger` khi user click.

```kotlin
import com.adshub.monetization.ui.AdsHubClickableBox

AdsHubClickableBox(
    triggerId = "trigger_tai_file",
    adEnabled = true, // (Tuỳ chọn) Nếu false thì không hiện icon Ad và gọi onAdFinished luôn khi click
    onBeforeAd = {
        // Thực thi các logic trước khi gọi quảng cáo (ví dụ: rung máy, set loading)
    },
    onAdFinished = {
        // Thực thi logic tiếp theo sau khi đã xem xong (ví dụ: bắt đầu tải, onBack)
    }
) {
    // UI bên trong của nút (Chỉ dùng để lấy UI, không set onClick ở component bên trong)
    Text("Tải xuống")
}
```

**Loại 2: Gọi tự động ngầm dưới nền (Programmatic Triggers)**
Không nên dùng `AdsHubClickableBox` cho các hành động không có tương tác Click trực tiếp (ví dụ: Tự động qua màn sau khi nhập đúng mã PIN/Pattern Lock, Tải xong file).
Thay vào đó, bạn nên gọi trực tiếp API `triggerAd` trong logic code của bạn:

```kotlin
// Ví dụ: Nhập PIN xong tự lưu rồi qua màn
if (isPinCorrect) {
    savePin(pin)
    com.onetabb.voice_lock.ui.component.triggerAd(context, "pin_lock_next") {
        onNextScreen()
    }
}
```

### 4.4. Native Full-screen (Tính năng Siêu Mượt / Zero-Flash)
Sử dụng khi bạn muốn hiển thị Quảng Cáo Native (Thường có Fill Rate cao và doanh thu tốt) dưới dạng Toàn Màn Hình nhưng **không làm đứt gãy lịch sử Back Stack** của App.

*   Định dạng này hiển thị dưới dạng **Overlay Dialog**. Nó đè lên màn hình hiện tại thay vì đẩy 1 Fragment mới vào lịch sử.
*   **Sticky PeekAd:** Nhờ tích hợp cơ chế Sticky, nếu bạn vuốt Back từ màn hình tiếp theo, bạn có thể gọi lại trigger để SDK lôi lại **ĐÚNG cái quảng cáo cũ** lên hiển thị mà không làm tốn thêm 1 Network Request nào! (Tuyệt đối an toàn chống Spam).

```kotlin
// Ở màn hình 1 (Ví dụ OB2)
binding.btnNext.setOnClickListener {
    AdsController.trigger(
        activity = this,
        triggerId = "trigger_native_fullscreen", // Gọi Trigger
        onFinished = { navigateTo(OB3Fragment()) } // Nhảy sang màn tiếp theo
    )
}

// Ở màn hình 2 (Ví dụ OB3) - Muốn vuốt Back hiện lại đúng cái Ad đó
requireActivity().onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
    override fun handleOnBackPressed() {
        AdsController.trigger(
            activity = requireActivity(),
            triggerId = "trigger_native_fullscreen_back", // Gọi Trigger phụ của màn 2
            onFinished = { 
                isEnabled = false 
                requireActivity().onBackPressedDispatcher.onBackPressed() // Lùi về OB2
            }
        )
    }
})
```

*Mẹo: Nếu bạn sử dụng Layout Tuỳ biến (Custom Layout XML) cho màn Native Full-screen và muốn tự thiết kế Nút Close/Get Started riêng, hãy gán thuộc tính `android:tag="native_ad_close_button"` vào View đó. SDK sẽ tự động nhận diện và gán sự kiện Đóng Quảng Cáo (Dismiss) vào nó, thay vì tự động đẻ ra thêm một nút [X] dự phòng ở góc màn hình!*

---

## 5. HỖ TRỢ CÁC ĐIỀU KIỆN TRIGGER PHỨC TẠP TỪ WEB ADMIN

AdsHub Web Admin hỗ trợ PM tạo ra các rule hiển thị rất phức tạp. Dưới đây là những việc Dev cần làm để các rule đó hoạt động trơn tru:

### 5.1. Điều kiện "Action Count Required" (Trong Tab Show Rules)
*   **Không cần code thêm gì cả!**
*   Nếu trên Web Admin, tab **Show Rules** có cài đặt `Action Count Required = 3`. Bạn chỉ việc gọi hàm `trigger()` như bình thường ở mọi nút bấm.
*   SDK sẽ tự đếm ngầm: Lần 1 gọi hàm (Bỏ qua) -> Lần 2 gọi hàm (Bỏ qua) -> Lần 3 gọi hàm (Cho phép nổ Ads).

### 5.2. Điều kiện "after_action_count" (Trong Tab Triggers)
Khác với quy tắc tự động ở trên, nếu PM cài đặt trực tiếp Điều kiện (Condition) của một Trigger cụ thể là `after_action_count`, và điền Action Name là `download_action`, bạn chỉ việc gọi hàm `trigger()`:

```kotlin
// Ở nút Download, bạn gọi hàm trigger. Nếu Web Admin đang TẮT trigger "download_action", hàm này chỉ đóng vai trò ĐẾM NGẦM +1.
AdsController.trigger(
    activity = this,
    triggerId = "download_action",
    onFinished = { startDownload() }
)

// Ở một màn hình khác, Web Admin cài luật: "Chờ tải đủ 3 lần mới hiện Quảng cáo này"
AdsController.trigger(
    activity = this,
    triggerId = "show_result_trigger", 
    onFinished = { goToResultScreen() }
)
```

### 5.3. Điều kiện "First Show Delay" và "Min Background Time"
*   **Không cần code thêm gì cả!** 
*   SDK đã tự động tích hợp `AppLifecycleTracker`. Mọi bộ đếm thời gian (từ lúc mở app, hoặc thời gian nằm dưới Background) đều được SDK tự tính toán. Khi bạn gọi `trigger()`, SDK sẽ tự chặn nếu chưa đủ thời gian delay mà PM đã cấu hình.

### 5.4. Truyền Biến Tùy Chỉnh (Dynamic Variables & User Properties)
Để PM có thể thiết lập Điều kiện động (Dynamic Routing) trên Web Admin dựa vào trạng thái hiện tại của App (Ví dụ: "Chỉ hiện quảng cáo nếu User Level > 5", hoặc "Chỉ nạp Ads nếu User đã xem xong Onboarding"), Developer cần cập nhật trạng thái biến vào SDK thông qua `userProperties`:

```kotlin
// Cập nhật giá trị vào UserProperties. Giá trị có thể là Boolean, Int, Double, String.
com.adshub.monetization.AdsController.setUserProperty("user_level", 6)
com.adshub.monetization.AdsController.setUserProperty("is_onboarding_completed", true)
com.adshub.monetization.AdsController.setUserProperty("subscription_type", "free")
```

Bên cạnh đó, với các điều kiện điều hướng màn hình (như `from_screen` hay `not_on_screens`), khi gọi hàm `trigger()`, Developer có thể truyền thêm tham số `fromScreen` để khai báo ngữ cảnh:
```kotlin
com.adshub.monetization.AdsController.trigger(
    activity = this,
    triggerId = "trigger_interstitial_back",
    fromScreen = "vip_screen", // Truyền tên màn hình User đang đứng (hoặc vừa đứng)
    onFinished = { /* ... */ }
)
```

---

## 6. QUY TRÌNH KIỂM THỬ (DEBUGGING)

*   **Logcat:** Lọc chuỗi `AdsHub:` trên Logcat Android Studio để xem luồng đi của SDK. Bạn sẽ thấy rõ SDK chặn hiện Ads vì lý do gì (VD: `[CAPPING] Bỏ qua Ads vì chưa đủ Action Count Required`, hoặc `[SAFETY] Chặn Ads back-to-back`).
*   **Chế độ Test:** Không cần đổi mã AdUnit sang Test thủ công! Nhờ PM bật cờ `Use Test Ads` trên Web Admin. SDK sẽ tự tráo mã để bảo vệ tài khoản thật của bạn. Môi trường Production hãy nhờ PM tắt cờ này.

### 6.1. Đọc Log Tracking

Logcat và Firebase Realtime Database dùng cùng định dạng:

```text
AdsHub: [TRACKING] <waterfall_id>_<tier_level>_<event>
```

Ví dụ:

```text
AdsHub: [TRACKING] native_home_high_load_request
AdsHub: [TRACKING] native_home_high_load_success
AdsHub: [TRACKING] native_home_high_show_request
AdsHub: [TRACKING] native_home_high_show_success
```

Ý nghĩa:

*   `load_success`: Mạng quảng cáo đã trả về Ad Object, chưa đồng nghĩa user đã thấy ad.
*   `show_request`: SDK đã yêu cầu render/show trên một slot hoặc fullscreen hợp lệ.
*   `show_success`: Chỉ ghi khi SDK nhận callback impression từ mạng quảng cáo.
*   `time_out`: Tier hiện tại quá thời gian chờ; Waterfall dừng và không tiếp tục gọi Tier dưới.

SDK không phát event `impression_recorded` riêng. Impression được biểu diễn bằng `show_success`.
