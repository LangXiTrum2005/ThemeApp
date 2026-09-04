# `:ontabb_rate` — module đánh giá

Hộp thoại chấm sao dùng chung cho mọi app, không phụ thuộc app nhúng nó. Chỉ cần appcompat,
constraintlayout, material và lottie.

Luật xử lý số sao:

- **1-3 sao** — mở `FeedbackActivity` để user viết góp ý, gửi về `RateConfig.contactEmail`.
- **4-5 sao** — mở thẳng trang app trên Play Store để user đánh giá ở đó.

Cả hai nhánh đều đánh dấu "đã đánh giá", nên mọi điểm kích hoạt tự động im vĩnh viễn sau đó.
Riêng nút "Rate us" thì vẫn bung, vì cờ đó sinh ra để app khỏi làm phiền chứ không phải để từ
chối user tự tay bấm.

## Nối vào app

```kotlin
// Application.onCreate
OneTabbRate.init(
    RateConfig(
        contactEmail = Constants.EMAIL_SUPPORT,
        storeId = BuildConfig.STORE_ID,   // bản debug có .dev vẫn trỏ về listing thật
        style = RateStyle.DIALOG,
        isEnabled = true,                 // giá trị chạy khi chưa fetch được khoá `enable`
        fontRegular = R.font.nunito_medium,
        fontBold = R.font.nunito_semibold,
    ),
)
OneTabbRate.recordAppOpen(this)
```

## Điểm kích hoạt

| Điểm kích hoạt | Gọi ở | Điều kiện |
|---|---|---|
| Lần mở app thứ N | `showOnHome` | `RateConfig.secondAppOpenAt`, đúng một lần đời máy |
| Vào / quay lại tab Home | `showOnHome` trong `MainActivity.onResume` | lần thứ nằm trong `home_show_rating` |
| Bấm back của máy để thoát app | `showOnBackPressed` trong `MainActivity.onHandleBackPressed` | lần thứ nằm trong `back_device_rating` |
| Nút "Rate us" trong Settings | `showOnDemand` | bấm là bung, bỏ qua mọi bộ đếm và cả công tắc `enable` |

`showOnBackPressed` trả về `true` nghĩa là hộp thoại đã bung và nơi gọi **phải hoãn `finish()`**,
không thì user thấy hộp thoại loé lên rồi app đóng luôn. Bấm back lần nữa, lượt đó không còn nằm
trong danh sách nên app thoát bình thường.

`showOnHome` cố ý chỉ đếm ở Activity, không đếm trong Fragment: đổi tab trong ViewPager không
đụng vòng đời Activity nên một lần vào Home vẫn tính đúng một lần. Lần bị quảng cáo toàn màn
hình che cũng không tính.

## Remote Config `rating_show`

Một tham số duy nhất, giá trị là chuỗi JSON, đổi được mà không cần build lại app:

```
Key:   rating_show
Value: {"enable":true,"home_show_rating":[2,4,6],"back_device_rating":[1,3,5]}
```

| Khoá | Nghĩa |
|---|---|
| `enable` | công tắc tổng cho các điểm bung **tự động** |
| `home_show_rating` | bung ở lần vào / quay lại tab Home thứ mấy |
| `back_device_rating` | bung ở lần bấm back của máy để thoát app thứ mấy |

Quy tắc đọc:

- Thiếu khoá nào thì khoá đó **giữ nguyên giá trị đang chạy**, nên đẩy nửa cấu hình vẫn an toàn.
  Riêng `enable` thiếu thì KHÔNG bị hiểu thành `false`.
- Danh sách rỗng `[]` tắt hẳn điểm kích hoạt đó.
- Số 0 và số âm bị loại vì bộ đếm bắt đầu từ 1.
- Chuỗi hỏng cú pháp bị bỏ qua **nguyên khối** — cả ba khoá, kể cả `enable` — và app chạy tiếp
  bằng giá trị đang có. Console không kiểm cú pháp JSON của tham số String, nên dán qua một
  trình kiểm JSON trước khi publish.

`enable = false` làm im ba điểm bung tự động: lần mở app thứ N, vào lại Home, bấm back thoát app.
Nút "Rate us" trong Settings **không bị ảnh hưởng**, bấm vẫn bung — công tắc này để chặn app tự
làm phiền, không phải chặn user. Tắt xong bộ đếm cũng đứng yên, nên bật lại user vẫn ở đúng lượt
cũ chứ không nhảy cóc qua mấy lượt lẽ ra được bung.

App đọc tham số rồi bơm xuống module bằng `OneTabbRate.setEnabled(...)` và
`OneTabbRate.updateShowRules(...)`. Đổi danh sách giữa chừng chỉ đổi việc lần tới có bung hay
không, user đang ở lần thứ 5 không bị tụt về 0.

Trong template này, phần đọc nằm ở `app/.../util/remoteconfig/RatingRemoteConfig.kt` và được áp hai
lần — `Application.onCreate` dùng giá trị cache của phiên trước, rồi Splash áp lại ngay sau khi
cold start fetch xong. Phần đọc chuỗi có unit test ở `app/src/test`.

## Hai kiểu hiển thị

Chọn bằng `RateConfig.style`; nội dung và luật xử lý số sao y hệt nhau, chỉ khác khung chứa.

| `RateStyle` | Khung chứa |
|---|---|
| `DIALOG` (mặc định) | hộp thoại nổi giữa màn hình |
| `BOTTOM_SHEET` | sheet trượt từ mép dưới, tràn hết bề ngang |

Phần vẽ sao và đổi mặt cười / tiêu đề nằm ở `RatePanel`, hai kiểu dùng chung — sửa một chỗ là cả
hai đổi theo. Hai layout đặt cùng bộ id để dùng chung được lớp đó.

## Ghi chú

- Màu của thẻ đánh giá cố định theo bộ nhận diện, **không** bám theo theme app: thẻ là một khối
  tối tự đứng, đổi màu theo app sáng/tối sẽ vỡ tương phản với ảnh Lottie đè lên nó. Nền và chữ
  màn `FeedbackActivity` thì ngược lại, dùng theme attr nên tự bám theo app.
- Mọi tên resource đều mang tiền tố `ontabb_rate_`: tên trần như `close` hay `feedback` sẽ bị
  string cùng tên của app nhúng đè lên.
- Font truyền từ app qua `RateConfig.fontRegular` / `fontBold`; không truyền thì dùng font hệ
  thống. Module cố ý không hardcode `@font/...` vào XML.
