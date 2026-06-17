# Kế Hoạch Triển Khai: The Vault (Affiliate + E-commerce)

## 1. Tổng Quan Kiến Trúc
Hệ thống "The Vault" trước đây tập trung vào luồng Affiliate (chuyển hướng người dùng qua nền tảng khác để ăn chênh lệch hoa hồng). Tuy nhiên, để đáp ứng nhu cầu mới, hệ thống sẽ được nâng cấp thành **Hybrid E-commerce Hub**, kết hợp cả:
1. **Affiliate Flow:** Chuyển hướng mua ngoài (ví dụ: Nike, Shopee).
2. **Direct E-commerce Flow:** Mua bán và thanh toán trực tiếp ngay bên trong app (In-App Purchase).

## 2. Lược Đồ Cơ Sở Dữ Liệu (Firestore Schema)

Cấu trúc Firestore cần được mở rộng để phục vụ cả 2 flow:

- **`artifacts/`** *(Cập nhật)*
  - Thêm `commissionRate` *(number, vd: 0.08)* - Mức hoa hồng (dành cho affiliate).
  - Thêm `isDirectSale` *(boolean)* - Xác định đây là sản phẩm nội bộ (mua trên app) hay external affiliate.
  - Thêm `internalPrice` *(number)* - Giá bán nội bộ nếu thanh toán in-app.
  - Thêm `stock` *(number)* - Số lượng tồn kho (chỉ dành cho hàng bán trực tiếp).

- **`brands/`** *(Tạo mới - Quản lý Affiliate & Đối tác)*
  - `{brandId}: { name: string, rate: number, logoUrl: string }`
  - *Ví dụ:* `NIKE: { name: "Nike", rate: 0.08 }`

- **`clicks/`** *(Tạo mới - Tracking Affiliate giả lập/thật)*
  - `{clickId}: { artifactId, userId, timestamp, commissionEarned, brand, archetype }`

- **`orders/`** *(Tạo mới - Tracking E-commerce bán hàng trực tiếp)*
  - `{orderId}: { userId, items: [], totalAmount, status: "pending"|"paid"|"shipped", paymentMethod: "COD"|"Banking", timestamp }`

- **`contacts/`** *(Tạo mới - Phản hồi khách hàng & Support)*
  - `{contactId}: { userId, title, message, status: "new"|"resolved", timestamp }`

- **`users/`** *(Cập nhật)*
  - Thêm `role: "admin" | "user"`

## 3. Các Luồng Nghiệp Vụ (User Flows)

### 3.1. Luồng Affiliate (Outbound)
1. Giao diện The Vault: Các sản phẩm có `isDirectSale == false`.
2. Button hiển thị: **INITIATE ACQUISITION** (hoặc **GET IT ON NIKE**).
3. Logic:
   - Ghi nhận `click` vào bảng `clicks/`.
   - Tính toán tạm thời: `commissionEarned = parse(price) * brands/{vendor}.rate` (VD: 1.850.000 * 8% = 148.000).
4. Thực thi: Mở Link ngoài (Web/Deep link) tới Shopee/Nike để user hoàn tất mua.

### 3.2. Luồng Bán Hàng Trực Tiếp (In-App E-commerce)
1. Giao diện The Vault: Các sản phẩm có `isDirectSale == true`.
2. Button hiển thị: **BUY NOW** / **ADD TO CART**.
3. Logic Checkout:
   - Chuyển hướng user tới màn hình Thanh Toán nội bộ.
   - Cho phép chọn Địa chỉ nhận hàng, Hình thức thanh toán.
4. Thực thi: 
   - Sau khi thanh toán/xác nhận thành công, tạo record trong `orders/` với status `pending`.
   - Trừ số lượng `stock` trong bảng `artifacts/`.

## 4. UI/UX - Màn Hình Cần Phát Triển

### 4.1. Trong App (Android - Kotlin)
- **The Vault / Artifact Detail:** UI phân biệt giữa hàng Affiliate và hàng Direct (nút bấm khác màu, có tag riêng).
- **Checkout & Payment Flow (Tạo mới):** Màn hình xác nhận đơn hàng, giỏ hàng nhỏ.
- **Admin Dashboard Screen:**
  - *Tab 1 (Tổng quan):* Tổng số click, tổng revenue (từ Direct), tổng commission (từ Affiliate).
  - *Tab 2 (Theo sản phẩm):* Danh sách Top Products.
  - *Tab 3 (Theo brand):* Brand breakdown (chỉ số của từng nhãn hàng).
  - *Tab 4 (Biểu đồ):* Line chart/Bar chart theo Ngày/Tuần/Tháng.

### 4.2. Hệ Thống Quản Trị Quản Lý Đa Năng (Modern Web Admin CMS)
- Ứng dụng Web Dashboard cao cấp (kiểu dáng hiện đại như các SaaS CMS) với Firebase Web SDK.
- **Các Module chức năng chính của Web Admin:**
  - **Overview Dashboards:** Trực quan hóa dữ liệu bằng các biểu đồ (Line, Bar, Doughnut) cho Doanh số (Sales), Hoa hồng Affiliate (Commission), Tương tác Click, theo thời gian thực (Real-time).
  - **Quản Lý Tồn Kho & Sản Phẩm (Inventory/Artifacts):** Thêm/Sửa/Xóa kho hàng (`stock`), điều chỉnh giá gốc và theo dõi mức chiết khấu `commissionRate` liên tục.
  - **Quản Lý Đơn Hàng (Orders):** Xử lý quy trình mua/bán in-app (Pending -> Shipped -> Delivered/Paid).
  - **Quản Lý Đối Tác (Affiliate Brands):** Giám sát hiệu quả từ các bên thứ 3 (Nike, Shopee...) trên hệ thống, điều chỉnh `rate` tương thích.
  - **Quản Lý Người Dùng (User Accounts):** Kiểm soát phân quyền danh sách tài khoản (phân tách role Admin/User), cảnh cáo hoặc khoá tài khoản có giao dịch đen.
  - **Chăm Sóc & Phản Hồi (Customer Support):** Màn hình nhận yêu cầu/Ticket xử lý và giải quyết khiếu nại (Contact) từ người dùng. Mở ra cổng kết nối tốt với khách.

## 5. Roadmap Triển Khai (Phases)

| Giai Đoạn | Công Việc Chính | Trạng Thái |
|:---:|---|:---:|
| **Phase 1** | Chuẩn bị Firestore Data (`brands/`, cập nhật `artifacts/`). Setup Models/Entities. | 🔄 Sắp thực hiện |
| **Phase 2** | Code Affiliate Flow: Nút INITIATE ACQUISITION, ghi nhận data xuống `clicks/`. | ⏳ Chờ Phase 1 |
| **Phase 3** | Admin Dashboard Mobile: Xây dựng Dashboard UI với 4 tabs & Fetch thống kê. | ⏳ Chờ Phase 2 |
| **Phase 4** | E-Commerce Logic & Checkout In-app: Nút BUY NOW, bảng `orders/`, luồng thanh toán. | ⏳ Kế hoạch tiến tới |
| **Phase 5** | Dashboard trên nền web cho Admin. | 🎯 Mục tiêu dài hạn |

---
**💡 Yêu Cầu Chuẩn Bị Trước Khi Code Phase 1 & 2:**
Tạo mới collection `brands` trong Firestore với dữ liệu:
```json
{
  "brands": {
    "NIKE": {
      "name": "Nike",
      "rate": 0.08
    },
    "ADIDAS": {
      "name": "Adidas",
      "rate": 0.07
    },
    "RINNSAN_LAB": {
      "name": "RinnSan Lab",
      "rate": 0.12
    }
  }
}
```
Và đối với collection `artifacts`, hãy đảm bảo thêm field `commissionRate`.
