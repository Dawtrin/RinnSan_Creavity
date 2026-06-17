# Hoàn tất Triển khai Tính năng Vận chuyển theo Vị trí (Location-based Shipping)

Tôi đã hoàn tất việc tích hợp tính năng tự động lấy vị trí và tính toán phí vận chuyển (Shipping Fee) trong màn hình Checkout.

## 🛠️ Các thay đổi đã thực hiện

### 1. Cấu hình Quyền & Thư viện
- Đã thêm quyền `ACCESS_FINE_LOCATION` và `ACCESS_COARSE_LOCATION` vào `AndroidManifest.xml`.
- Tích hợp thư viện `play-services-location` của Google vào `build.gradle.kts` để lấy tọa độ GPS chính xác mà không tốn phí gọi API từ Google Maps Platform.

### 2. Dịch vụ Định vị (Location Service)
- **[NEW]** Tạo class `LocationService` (tại `core/location`). Dịch vụ này sử dụng `FusedLocationProviderClient` để lấy tọa độ (Lat/Lng) hiện tại.
- Sử dụng `Geocoder` có sẵn của Android để dịch ngược tọa độ (Reverse Geocoding) ra địa chỉ văn bản, Thành phố, và Mã quốc gia.
- Cài đặt Logic tính giá vận chuyển (Shipping Logic):
  - Khách quốc tế (mã quốc gia khác "VN"): Tạm tính `100.000đ`.
  - Khách nội địa:
    - Nếu Thành phố chứa "Đà Nẵng" hoặc "Da Nang": Tính khoảng cách đường chim bay từ tọa độ cửa hàng. <= 5km: `10.000đ`, > 5km: `15.000đ`.
    - Các Tỉnh/Thành phố khác: `30.000đ`.

### 3. Cập nhật Model & Database
- Đã thêm trường `shippingFee: Long = 0L` vào Model `Order`.
- Bổ sung `lat`, `lng`, và `countryCode` vào `ShippingAddress` để lưu trữ cùng đơn hàng.

### 4. Giao diện Checkout
- Màn hình `CheckoutScreen` giờ đây có thêm nút bấm **"DÙNG VỊ TRÍ"** kèm icon MyLocation.
- Khi người dùng nhấn nút, hệ thống sẽ tự động hiển thị popup xin cấp quyền truy cập Vị trí (nếu chưa cấp).
- Địa chỉ (Đường, Thành phố) và **Phí vận chuyển (Shipping Fee)** sẽ tự động được điền và cập nhật vào tổng tiền thanh toán.

## ✅ Hướng dẫn Kiểm tra (Verification)
Bạn hãy chạy lại ứng dụng (Run App) và thực hiện các bước sau:
1. Thêm 1 món hàng bất kỳ vào giỏ hàng và chuyển đến trang Checkout.
2. Bạn sẽ thấy dòng **SHIPPING FEE** đang ghi là *"Tính theo vị trí"*.
3. Nhấn vào dòng chữ **"DÙNG VỊ TRÍ"** ở khu vực Shipping Address.
4. Chấp nhận quyền truy cập vị trí trên điện thoại/máy ảo.
5. Quan sát ô Địa chỉ, Thành phố tự động được điền, và **Shipping Fee** được cập nhật thành số tiền cụ thể (VD: 15.000đ hoặc 30.000đ tùy vị trí máy ảo). Tổng tiền đơn hàng cũng sẽ tự động cộng thêm phí ship này.
