# Hướng dẫn chạy Web Admin — RinnSan Creavity

Do ứng dụng web quản trị sử dụng **ES6 Modules** (`import` / `export`) và tích hợp kết nối **Firebase Firestore**, trình duyệt sẽ chặn việc tải tệp cục bộ trực tiếp từ giao diện file (`file:///`) vì chính sách bảo mật CORS. 

Bạn **phải khởi chạy ứng dụng web thông qua máy chủ HTTP cục bộ (Local HTTP Server)**.

---

## ⚡ Các bước thực hiện

### Cách 1: Sử dụng Python (Đơn giản & Khuyên dùng)
Vì trên hệ điều hành Windows của bạn đã cài đặt sẵn Python, đây là phương án nhanh nhất:

1. Mở **Command Prompt (cmd)** hoặc **PowerShell**.
2. Di chuyển tới thư mục của dự án web bằng lệnh:
   * **Nếu dùng Command Prompt (cmd)**:
     ```cmd
     cd /d f:\RinnSanCreavity_Kotlin\admin-web
     ```
   * **Nếu dùng PowerShell (mặc định của Android Studio Terminal)**:
     ```powershell
     cd f:\RinnSanCreavity_Kotlin\admin-web
     ```
3. Khởi chạy máy chủ cục bộ bằng lệnh:
   ```cmd
   python -m http.server 8080
   ```
4. Mở trình duyệt web của bạn và truy cập địa chỉ:
   👉 **[http://localhost:8080](http://localhost:8080)** hoặc **[http://localhost:8080/index.html](http://localhost:8080/index.html)**

---

### Cách 2: Sử dụng Node.js (npx)
Nếu bạn muốn sử dụng Node.js để chạy máy chủ:

1. Mở **Command Prompt (cmd)** hoặc **PowerShell**.
2. Di chuyển tới thư mục của dự án web bằng lệnh:
   * **Nếu dùng Command Prompt (cmd)**:
     ```cmd
     cd /d f:\RinnSanCreavity_Kotlin\admin-web
     ```
   * **Nếu dùng PowerShell**:
     ```powershell
     cd f:\RinnSanCreavity_Kotlin\admin-web
     ```
3. Khởi chạy máy chủ HTTP bằng lệnh:
   ```bash
   npx http-server -p 8080
   ```
4. Truy cập trình duyệt theo địa chỉ:
   👉 **[http://localhost:8080](http://localhost:8080)**

---

## 🔑 Thông tin Đăng nhập (Credentials)

Khi trang đăng nhập của Command Center hiện lên, bạn có thể đăng nhập bằng một trong hai tài khoản dưới đây:

### 1. Tài khoản Quản trị viên thật (Firebase Live)
* **Email**: `dattrandn@gmail.com`
* **Mật khẩu**: `Kaipoh2642k5`

### 2. Tài khoản Demo ngoại tuyến (Demo Bypass)
* Dùng khi muốn kiểm tra giao diện cục bộ nhanh mà không cần kết nối mạng hoặc Firebase:
  * **Email**: `admin@rinnsan.com`
  * **Mật khẩu**: `admin`

---

## 🛠️ Công cụ dọn dẹp dữ liệu thừa (Seeder Cleanup Tool)

Nếu bạn lỡ bấm vào các nút **DATA SEEDERS** trong tab Inventory và làm ngập bảng dữ liệu sản phẩm mẫu, hãy sử dụng công cụ khôi phục tự động đã được tích hợp sẵn:

1. Đảm bảo rằng máy chủ web cục bộ đang hoạt động (ở cổng `8080`).
2. Mở trình duyệt và truy cập: **[http://localhost:8080/inspect.html](http://localhost:8080/inspect.html)**
3. Nhấp nút **LOAD ALL PRODUCTS** để tải toàn bộ danh sách sản phẩm hiện tại từ database.
4. Nhấp nút **DELETE ALL SEEDED PRODUCTS** (Màu đỏ) để tự động xóa toàn bộ sản phẩm mẫu do seeder tạo ra, giữ nguyên các sản phẩm thật với hình ảnh tùy chỉnh của bạn.
