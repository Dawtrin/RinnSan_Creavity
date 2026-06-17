# Kế hoạch phát triển (Next Steps)

Tài liệu này vạch ra lộ trình các bước tiếp theo nhằm nâng cao bảo mật và chất lượng mã nguồn cho ứng dụng RinnSan Creavity, sau khi đã hoàn thành việc tái cấu trúc `AdminViewModel`.

---

## Giai đoạn 1: Bảo mật cấp độ Database (Firebase Security Rules)

Hiện tại, việc kiểm tra quyền Admin chỉ đang diễn ra ở cấp độ giao diện (UI). Chúng ta cần thiết lập lớp bảo mật ở cấp độ Backend.

**Hành động đề xuất:**
- Viết và cấu hình `firestore.rules` trên Firebase Console.
- **Rules chi tiết:**
  - `users`: Người dùng chỉ được sửa thông tin của mình. **Riêng field `role` và `status` chỉ Admin mới được thay đổi.**
  - `artifacts` (Vault): Người dùng thường chỉ có quyền Đọc (Read). Chỉ Admin mới có quyền Tạo/Sửa/Xóa (Write).
  - `orders`: Người dùng chỉ có thể Đọc và Tạo đơn hàng của chính mình. Admin có quyền xem tất cả và cập nhật trạng thái đơn hàng.
  - `contacts` (Tickets): Người dùng có thể Tạo. Admin có quyền Đọc và Sửa (cập nhật trạng thái).

---

## Giai đoạn 2: Chuẩn hóa Clean Architecture cho toàn dự án

Sau thành công của `AdminRepository`, chúng ta sẽ nhân rộng mô hình này ra các tính năng cốt lõi khác để giảm tải cho ViewModel và tăng khả năng bảo trì.

**Các thành phần cần tái cấu trúc:**
1. **Vault (E-commerce):** Tạo `VaultRepository` để quản lý logic lấy danh sách sản phẩm, quản lý giỏ hàng và thanh toán. Thay thế các lệnh gọi trực tiếp `FirebaseFirestore` trong `VaultViewModel`.
2. **Signal (Social Feed):** Tối ưu hóa `SignalRepository` (hiện đã có) hoặc tách triệt để các logic lấy dữ liệu bài đăng, comment từ `SignalViewModel` sang Data Layer.
3. **Wishlist:** Tạo `WishlistRepository` để quản lý các món đồ được lưu.

---

## Giai đoạn 3: Tự động hóa kiểm thử (Unit Testing)

Tạo một lá chắn an toàn (safety net) để các bản cập nhật trong tương lai không làm hỏng tính năng hiện tại.

**Các module cần viết Test:**
- `AdminRepositoryImpl`: Viết các test case kiểm tra logic thống kê doanh thu (Revenue logic) và đếm số lượng thống kê (Summary logic).
- Mock `FirebaseFirestore` và `FirebaseAuth` bằng thư viện MockK để giả lập các phản hồi từ database mà không cần kết nối mạng.
- Đảm bảo độ bao phủ code (Code coverage) ở Data Layer, nơi chứa các phép tính toán tài chính và nghiệp vụ quan trọng.
