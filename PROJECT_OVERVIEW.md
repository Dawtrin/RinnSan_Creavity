# TỔNG QUAN DỰ ÁN: RINNSAN CREAVITY

## 1. Giới thiệu chung
**RinnSan Creavity** là một nền tảng thời trang ảo (Virtual Fashion) kết hợp hệ sinh thái thương mại điện tử (E-commerce) và mạng xã hội chia sẻ phong cách. Ứng dụng mang hơi hướng tương lai (Cyberpunk, Techwear, Avant-Garde) với giao diện người dùng độc đáo, tập trung vào trải nghiệm trực quan sống động và sự cá nhân hóa thông qua Trí tuệ nhân tạo (AI).

Dự án không chỉ là một cửa hàng bán quần áo mà còn là một thế giới để người dùng định hình danh tính phong cách cá nhân, tương tác với cộng đồng và nhận những lời khuyên phối đồ chuyên nghiệp từ trợ lý ảo.

## 2. Tệp người dùng mục tiêu (Target Audience)
- **Tín đồ thời trang ngách:** Những người đam mê các phong cách đặc thù như Techwear, Darkwear, Avant-Garde, Y2K...
- **Thế hệ Gen Z & Alpha:** Những người trẻ yêu thích giao diện phá cách (Cyber/Glitch), thích trải nghiệm mua sắm mới lạ mang tính tương tác cao.
- **Người dùng tìm kiếm sự khác biệt:** Những người cần công cụ để xây dựng thương hiệu cá nhân qua phong cách ăn mặc (thông qua bài trắc nghiệm Identity/Archetype).
- **Cộng đồng (Community):** Những cá nhân muốn khoe các "outfit" của mình và lấy cảm hứng từ người khác.

## 3. Kiến trúc và Công nghệ sử dụng (Tech Stack)

### Frontend (Giao diện & Logic Client)
- **Ngôn ngữ:** Kotlin
- **UI Framework:** Jetpack Compose (Modern UI toolkit cho Android) với các hiệu ứng Animation, Parallax và Glassmorphism phức tạp.
- **Kiến trúc:** MVVM (Model-View-ViewModel) kết hợp Clean Architecture (tách biệt Domain/Data/Presentation layers).
- **Dependency Injection:** Dagger Hilt.
- **Bất đồng bộ:** Kotlin Coroutines & StateFlow/SharedFlow.

### Backend & Cơ sở dữ liệu (BaaS)
- **Cơ sở dữ liệu:** Firebase Firestore (NoSQL, Realtime updates).
- **Xác thực:** Firebase Authentication (Email/Password).
- **Lưu trữ hình ảnh:** Cloudinary API (Upload và tối ưu hóa hình ảnh tốc độ cao).

### Tích hợp Trí tuệ nhân tạo (AI)
- **Core AI:** Google Gemini API (`gemini-2.0-flash-lite`) (Sử dụng cho tính năng Virtual Stylist tư vấn thời trang).

## 4. Chi tiết các chức năng cốt lõi (Core Features)

### 4.1. Hệ thống định danh và Xác thực (Identity & Auth)
- Đăng ký và quản lý tài khoản bảo mật.
- **Archetype Test (Trắc nghiệm tính cách thời trang):** Khi tham gia, người dùng sẽ được kiểm tra để phân loại vào các phong cách (Ghost, Operator, Glitch, Nomad...). Từ đó, hệ thống cá nhân hóa giao diện và đề xuất trang phục tương ứng.

### 4.2. Hệ sinh thái thương mại (The Vault / Archive)
- **Duyệt sản phẩm:** Hiển thị danh mục quần áo như một "kho lưu trữ" (Archive) với hiệu ứng Parallax mượt mà.
- **Thông tin chi tiết:** Hỗ trợ thư viện ảnh (Galleries), mô tả chi tiết, bảng kích cỡ (Size Variants) và số lượng tồn kho theo kích cỡ.
- **Checkout (Thanh toán nội bộ) & Affiliate:** Người dùng có thể mua trực tiếp trên app với luồng giỏ hàng (Cart) và lưu đơn hàng, hoặc dẫn link sang các trang thương mại liên kết.

### 4.3. Mạng xã hội phong cách (The Signal)
- **Bảng tin (Feed):** Nơi người dùng đăng tải hình ảnh (OOTD - Outfit Of The Day).
- **Tương tác:** Like, Comment thời gian thực (Real-time).
- **Quản lý nội dung:** Đăng bài kèm thẻ tag (Tags) và tải ảnh trực tiếp lên Cloudinary. Cung cấp tính năng quản lý, xóa bài đăng của chính mình.

### 4.4. Trợ lý ảo thời trang (Virtual Stylist)
- **Trò chuyện với AI:** Một hộp thoại tương tác với trí tuệ nhân tạo Gemini.
- **Tư vấn phối đồ:** AI phân tích dựa trên hồ sơ (Archetype) của người dùng và kho dữ liệu (Vault) để đưa ra lời khuyên trang phục hàng ngày hoặc cho các sự kiện cụ thể.

### 4.5. Trung tâm chỉ huy quản trị (Admin Command Center)
- Là giao diện CMS dành riêng cho ban quản trị, được tích hợp thẳng vào app nhưng được khóa đằng sau cơ chế phân quyền (Role-based access).
- **Thống kê (Dashboard):** Xem tổng quan doanh thu, đơn hàng, lượt truy cập, tỷ lệ chuyển đổi của các hãng thời trang.
- **Quản lý Inventory:** Thêm, Sửa, Xóa thông tin sản phẩm, cấu hình hoa hồng, quản lý kho hàng. Trình tạo dữ liệu mẫu (Seeders) cho các thương hiệu như Nike, Puma, Rick Owens, Balenciaga...
- **Quản lý Orders:** Xem đơn đặt hàng, cập nhật trạng thái (Pending, Paid, Delivered...).
- **Quản lý Users:** Xem danh sách người dùng, cấp quyền Admin, hoặc Khóa/Mở khóa (Ban/Unban) tài khoản.
- **Hỗ trợ (Tickets):** Xử lý phản hồi và yêu cầu hỗ trợ từ khách hàng.

### 4.6. Trang cá nhân (Profile & Wishlist)
- Hiển thị ID người dùng (theo phong cách thẻ Agent).
- Quản lý danh sách sản phẩm yêu thích (Wishlist).
- Theo dõi lịch sử đơn hàng và quản lý các bài đăng cá nhân.
