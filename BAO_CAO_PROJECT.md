# BÁO CÁO DỰ ÁN: NỀN TẢNG THỜI TRANG ẢO VÀ MẠNG XÃ HỘI PHONG CÁCH RINNSAN CREAVITY
## BÀI TẬP LỚN HỌC PHẦN: LẬP TRÌNH DI ĐỘNG

---

### I. LÝ DO CHỌN ĐỀ TÀI

#### 1. Bối cảnh và Thách thức của thị trường thời trang hiện đại
Thời trang là một trong những lĩnh vực phát triển nhanh nhất trên các nền tảng số. Đối với thế hệ trẻ như Gen Z và Gen Alpha, trang phục không đơn thuần chỉ là vật che thân hay giữ ấm mà đã trở thành công cụ chính để bộc lộ cá tính, tuyên ngôn bản thân và định vị xã hội. Tuy nhiên, thị trường thời trang hiện nay đang đối mặt với sự quá tải thông tin: hàng ngàn xu hướng mới xuất hiện liên tục trên mạng xã hội, các trang thương mại điện tử truyền thống hiển thị sản phẩm một cách đại trà và thiếu cá nhân hóa, khiến người dùng gặp nhiều khó khăn trong việc tìm kiếm phong cách thực sự phù hợp với bản thân.

Các ứng dụng mua sắm thời trang truyền thống hiện nay đang gặp phải những hạn chế:
*   **Trải nghiệm mua sắm chung chung (Generic catalog browsing)**: Giao diện giống nhau cho tất cả mọi người, sản phẩm đề xuất chủ yếu dựa trên lượt xem chung thay vì hiểu sâu sắc bản sắc cá nhân của người dùng.
*   **Sự tách rời giữa Cộng đồng và Mua sắm**: Người dùng tìm cảm hứng phối đồ trên mạng xã hội bên ngoài, sau đó tự tìm kiếm sản phẩm tương tự trên các sàn thương mại điện tử. Quy trình này bị đứt gãy, thiếu sự liền mạch.

#### 2. Định hướng tiếp cận của RinnSan Creavity
Để giải quyết những hạn chế trên, dự án **RinnSan Creavity** được xây dựng nhằm tạo ra một hệ sinh thái thời trang cá nhân hóa khép kín: **Định danh phong cách $\rightarrow$ Tư vấn và phối đồ bằng AI $\rightarrow$ Mua sắm tối ưu $\rightarrow$ Chia sẻ phong cách**.

Dự án phát triển dựa trên ba triết lý cốt lõi:
*   **Cá nhân hóa bằng tâm lý thời trang**: Phân loại người dùng vào 1 trong 4 nhóm phong cách (Archetypes) đặc trưng dựa trên hai trục chính: **Conform (Hòa nhập) vs Rebel (Nổi loạn)** và **Show (Thể hiện) vs Hide (Ẩn mình)**:
    *   **GHOST (Hide + Conform)**: Phong cách Tối giản (Minimalist), tinh tế, kín đáo và không phô trương.
    *   **OPERATOR (Show + Conform)**: Phong cách Techwear/Urban utility, đa chức năng, đai khóa kéo hầm hố, mang phong thái tương lai.
    *   **GLITCH (Show + Rebel)**: Phong cách Maximalism/Avant-garde, nổi loạn, kết cấu phức tạp, màu sắc tương phản mạnh mẽ.
    *   **NOMAD (Hide + Rebel)**: Phong cách Vintage/Deconstructed, hoài cổ, phom dáng rộng, bụi bặm, thể hiện triết lý tự do.
*   **Sử dụng Trí tuệ nhân tạo (AI) làm bạn đồng hành**: Tích hợp mô hình ngôn ngữ lớn **Google Gemini API** đóng vai trò là một trợ lý thời trang cá nhân (Virtual Stylist) có khả năng đọc hiểu hồ sơ phong cách và số đo cơ thể (Body Data) của người dùng để tư vấn phối đồ thông qua hội thoại thời gian thực.
*   **Ngôn ngữ thiết kế Brutalist/Cyberpunk nổi loạn**: Phá bỏ những quy chuẩn giao diện thông thường với các đường nét vuông vức, góc cạnh sắc nhọn, font chữ monospace và màu sắc neon tương phản cao (Void Black, Cyber Acid, Glitch Red), tạo ra trải nghiệm thị giác ấn tượng mạnh mẽ cho giới trẻ.

---

### II. CÁC CÔNG NGHỆ SỬ DỤNG (TECH STACK)

Dự án áp dụng mô hình phân tách hoàn toàn giữa Client (Ứng dụng di động Kotlin), Admin Web (Bảng điều khiển quản trị) và Cơ sở dữ liệu đám mây (Firebase).

#### 1. Phân hệ Ứng dụng Di động (Android Client)
*   **Ngôn ngữ**: **Kotlin** - ngôn ngữ lập trình chính thức và tối ưu nhất cho hệ điều hành Android, mang lại hiệu năng cao và an toàn.
*   **UI Framework**: **Jetpack Compose** - bộ công cụ thiết kế giao diện dạng khai báo (declarative UI), giúp tạo ra giao diện brutalist góc cạnh và các hiệu ứng chuyển động mượt mà.
*   **Kiến trúc**: **Clean Architecture** kết hợp mô hình **MVVM (Model-View-ViewModel)**. Dự án được chia làm các layers độc lập: `core/`, `domain/`, `data/`, và `presentation/` giúp tách biệt rõ ràng các nhiệm vụ, dễ nâng cấp và bảo trì.
*   **Dependency Injection**: **Dagger Hilt** giúp quản lý tự động vòng đời của các dependency (Firestore, Auth, Repositories).
*   **Bất đồng bộ**: **Kotlin Coroutines & Flow (StateFlow)** quản lý tối ưu các tiến trình chạy nền như gọi mạng, lưu dữ liệu Firestore, tránh nghẽn luồng giao diện chính.
*   **Gọi API**: **Ktor Client** được cấu hình để gửi các yêu cầu HTTP không đồng bộ một cách tối giản để giao tiếp trực tiếp với Google Gemini API và Cloudinary.
*   **Tải hình ảnh**: **Coil** hỗ trợ tải ảnh từ Cloudinary CDN, quản lý bộ nhớ đệm thông minh.

#### 2. Cơ sở dữ liệu & Xác thực (BaaS)
*   **Firebase Authentication**: Quản lý đăng ký, đăng nhập và bảo mật phiên làm việc của người dùng bằng Email/Password hoặc Google Sign-In.
*   **Firebase Firestore**: Cơ sở dữ liệu đám mây dạng NoSQL, lưu trữ dữ liệu dưới định dạng tài liệu (Document) tương tự JSON và hỗ trợ đồng bộ hóa dữ liệu thời gian thực.
*   **Cloudinary SDK**: Cung cấp API tải lên hình ảnh phối đồ từ thiết bị di động lên đám mây, tự động tối ưu hóa dung lượng hình ảnh.

#### 3. Phân hệ Quản trị (Admin Web Command Center)
*   **Công nghệ**: HTML5, Vanilla CSS và Javascript ES Modules chạy trực tiếp qua trình duyệt.
*   **Firebase JS Web SDK (V9+)**: Kết nối trực tiếp với Firestore từ Web Admin để thực hiện các thao tác thống kê click, kiểm duyệt bài viết, quản lý đơn hàng và tài khoản người dùng trực tiếp.

---

### III. THIẾT KẾ CƠ SỞ DỮ LIỆU

Dự án sử dụng cơ sở dữ liệu đám mây **Firebase Firestore (NoSQL)**. Dưới đây là cấu trúc các tài liệu dữ liệu được mô tả trực quan theo định dạng JSON:

#### 1. Users.json (Tài khoản người dùng & Kết quả trắc nghiệm phong cách)
```json
[
  {
    "uid": "usr_98a72b83c",
    "email": "user1@rinnsan.com",
    "firstName": "Khánh",
    "lastName": "Nguyễn Hoàng",
    "role": "user",
    "status": "active",
    "archetype": "OPERATOR",
    "identityProfile": {
      "dominant_archetype": "OPERATOR",
      "score_map": {
        "GHOST": 0.15,
        "OPERATOR": 0.65,
        "GLITCH": 0.10,
        "NOMAD": 0.10
      },
      "confidence_level": 0.85,
      "is_hybrid": false,
      "timestamp": 1718469852000
    },
    "wishlist": [
      "art_ro_02", 
      "art_nike_01"
    ],
    "bodyData": {
      "height": 175.5,
      "weight": 68.0,
      "chest": 92.0,
      "waist": 78.0,
      "hips": 94.0
    }
  }
]
```

#### 2. Artifacts.json (Kho sản phẩm bán & affiliate - The Vault)
```json
[
  {
    "id": "art_ro_02",
    "title": "RICK OWENS GEODASKET BOOTS",
    "category": "Footwear",
    "price": "24.500.000 VND",
    "imageUrl": "https://cloudinary.com/ro_02.jpg",
    "affiliateLink": "https://shope.ee/ro_geobasket",
    "vendor": "RICK OWENS",
    "archetype": "GLITCH",
    "stock": 15
  }
]
```

#### 3. Signals.json (Bài đăng mạng xã hội - The Signal)
```json
[
  {
    "id": "sig_01b289ac",
    "userId": "usr_98a72b83c",
    "username": "Khánh Nguyễn",
    "userPhotoUrl": "https://cloudinary.com/avatar1.png",
    "type": "OUTFIT",
    "content": {
      "title": "OUTFIT OF THE SIGNAL",
      "description": "Techwear styling with Rick Owens and Glitch accessories",
      "images": ["https://cloudinary.com/post_01.jpg"],
      "tags": ["#techwear", "#operator", "#rickowens"],
      "location": "Đà Nẵng, Việt Nam"
    },
    "style": {
      "colorFilter": "CYBERPUNK",
      "textEffect": "GLITCH",
      "layout": "STANDARD",
      "font": "SPACE_MONO"
    },
    "interactions": {
      "likes": {
        "count": 124,
        "userIds": ["usr_friend1", "usr_friend2"]
      },
      "comments": {
        "count": 3,
        "lastCommentTimestamp": 1718469950000
      },
      "shares": 12,
      "views": 450
    },
    "status": "PUBLISHED",
    "moderationStatus": "APPROVED"
  }
]
```

#### 4. Comments.json (Bình luận bài viết mạng xã hội)
```json
[
  {
    "id": "cmt_77ab890c",
    "postId": "sig_01b289ac",
    "userId": "usr_friend1",
    "username": "Thắng Lê",
    "text": "Phối đồ chất quá bạn ơi!",
    "timestamp": 1718469900000,
    "isDeleted": false
  }
]
```

#### 5. Orders.json (Quản lý đơn hàng mua trực tiếp)
```json
[
  {
    "id": "ord_20260615_01",
    "userId": "usr_98a72b83c",
    "totalAmount": 25400000,
    "status": "pending",
    "paymentMethod": "COD",
    "address": {
      "name": "Khánh",
      "phone": "0905123456",
      "street": "123 Trần Hưng Đạo"
    },
    "timestamp": 1718470000000,
    "itemDetails": [
      {
        "artifactId": "art_ro_02",
        "quantity": 1,
        "price": 24500000
      }
    ]
  }
]
```

#### 6. Contacts.json (Phiếu hỗ trợ - Tickets)
```json
[
  {
    "id": "tkt_12a76f",
    "userId": "usr_98a72b83c",
    "title": "Lỗi kết nối Gemini AI",
    "message": "Tôi không chat được với Stylist...",
    "status": "new",
    "timestamp": 1718469800000
  }
]
```

#### 7. Clicks.json (Log click affiliate kiếm hoa hồng)
```json
[
  {
    "id": "clk_889ab8c",
    "artifactId": "art_ro_02",
    "userId": "usr_98a72b83c",
    "brandId": "RICK OWENS",
    "archetype": "GLITCH",
    "commissionEarned": 5.2,
    "timestamp": 1718469920000
  }
]
```

#### 8. Brands.json (Danh sách thương hiệu và cấu hình hoa hồng)
```json
[
  {
    "id": "RICK OWENS",
    "name": "Rick Owens",
    "rate": 12.5,
    "logoUrl": "https://logo.com/ro.png"
  }
]
```

---

### IV. CHƯƠNG TRÌNH DEMO

#### 1. Màn hình khởi động và xác thực (Gateway & Auth)
*   **Màn hình khởi động (Gateway / Splash Screen)**
*   **Đăng nhập (Login)**
*   **Đăng ký (Register)**
*   **Quên mật khẩu (Forgot Password)**

#### 2. Trắc nghiệm định danh phong cách (Identity Scanner)

#### 3. Trò chuyện tư vấn cùng trợ lý ảo AI (Virtual Stylist Box)

#### 4. Kho sản phẩm thời trang cá nhân hóa (The Vault / Archive)

#### 5. Mạng xã hội thời trang (The Signal Feed)

#### 6. Tạo bài viết chia sẻ phong cách ăn mặc (Create Post)

#### 7. Quản lý danh sách yêu thích và số đo cơ thể (Wishlist & Body Data)

#### 8. Bảng điều khiển quản lý và vận hành (Admin Web Dashboard)
*   **Bảng số liệu thống kê tổng quan (Dashboard Overview)**
*   **Quản lý kho sản phẩm (Inventory Management)**
*   **Trình gieo dữ liệu tự động (Seeder)**
*   **Quản lý đơn đặt hàng (Orders Management)**
*   **Quản lý tài khoản người dùng (Users Management)**
*   **Xử lý yêu cầu hỗ trợ (Support Ticket Center)**

#### 9. Các chức năng khác (nếu có)

---

### V. KẾT LUẬN VÀ HƯỚNG PHÁT TRIỂN

#### 1. Kết quả đạt được của dự án
Dự án **RinnSan Creavity** đã hoàn thành thiết lập một giải pháp ứng dụng di động kết hợp web quản trị toàn diện, đạt được các mục tiêu đề ra:
*   **Về mặt kỹ thuật**: Áp dụng thành công kiến trúc chuẩn Clean Architecture kết hợp MVVM giúp mã nguồn có cấu trúc rõ ràng, dễ bảo trì và mở rộng. Sử dụng Jetpack Compose tạo dựng thành công giao diện brutalist góc cạnh đặc trưng đạt độ hoàn thiện mỹ thuật cao.
*   **Về mặt ứng dụng AI**: Tích hợp hiệu quả Google Gemini API thông qua Prompt Engineering nâng cao giúp giải quyết bài toán tư vấn phối đồ cá nhân hóa dựa trên số đo cơ thể thực tế và gu thời trang định danh của từng người dùng.
*   **Về mặt vận hành**: Xây dựng hoàn chỉnh bảng quản trị web tương tác thời gian thực với Firestore, hỗ trợ quản trị viên quản lý toàn diện sản phẩm, người dùng, đơn hàng và các thống kê hoa hồng affiliate một cách trực quan.

#### 2. Các hạn chế hiện tại
*   Tính năng Chat với AI Stylist mới chỉ lưu lịch sử hội thoại tạm thời trong bộ nhớ RAM (ViewModel state) của phiên hoạt động hiện tại. Khi người dùng thoát app hoàn toàn và mở lại, lịch sử chat cũ sẽ biến mất.
*   Thư mục chia sẻ (`share/`) trên client di động vẫn còn trống, tính năng chia sẻ trực tiếp hình ảnh outfit từ app ra bên ngoài hệ thống (như xuất file ảnh Agent ID card để đăng lên story Instagram hay Facebook) chưa được hiện thực hóa đầy đủ.

#### 3. Định hướng phát triển tương lai
*   **Ứng dụng công nghệ Thử đồ ảo AR (Virtual Try-On)**: Tích hợp các thư viện thực tế tăng cường như ARCore để cho phép người dùng mở camera di động và ướm thử mô phỏng 3D của trang phục trực tiếp lên cơ thể trước khi bấm mua.
*   **Lưu trữ đám mây lịch sử chat**: Chuyển đổi cơ chế lưu trữ lịch sử chat tạm thời sang lưu trữ đồng bộ trên collection `chats/{uid}` của Firestore, giúp người dùng có thể xem lại toàn bộ các tư vấn phối đồ cũ từ AI.
*   **Tối ưu hóa đề xuất thông minh bằng Machine Learning**: Dựa vào hành vi lưu sản phẩm (wishlist) và lượt click mua thực tế để cá nhân hóa hơn nữa các đề xuất sản phẩm xuất hiện trong Vault.
*   **Hoàn thiện Module chia sẻ đa nền tảng**: Xây dựng tính năng sinh ảnh Agent Card độ phân giải cao kèm mã QR cá nhân để người dùng chia sẻ trực tiếp lên các nền tảng mạng xã hội khác nhằm thu hút người dùng mới cho ứng dụng.

---

### VI. TÀI LIỆU THAM KHẢO

1.  **Tài liệu Lập trình Android & Jetpack Compose**:
    *   Trang chủ hướng dẫn lập trình Android: [https://developer.android.com](https://developer.android.com)
    *   Tài liệu Jetpack Compose UI components & State management: [https://developer.android.com/compose](https://developer.android.com/compose)
2.  **Tài liệu Cơ sở dữ liệu Firebase & Cloud Firestore**:
    *   Firebase Firestore Document Database: [https://firebase.google.com/docs/firestore](https://firebase.google.com/docs/firestore)
    *   Firebase Authentication Services: [https://firebase.google.com/docs/auth](https://firebase.google.com/docs/auth)
3.  **Tài liệu Google Gemini API**:
    *   Gemini API Developer Guides & System Prompts: [https://ai.google.dev/gemini-api/docs](https://ai.google.dev/gemini-api/docs)
4.  **Tài liệu các dịch vụ tích hợp bên thứ ba**:
    *   Cloudinary Media Storage & SDK Integration: [https://cloudinary.com/documentation](https://cloudinary.com/documentation)
    *   Ktor Asynchronous HTTP Client: [https://ktor.io/docs/client-getting-started.html](https://ktor.io/docs/client-getting-started.html)
5.  **Tài liệu nội bộ dự án RinnSan Creavity**:
    *   Các tập tin kiến trúc phần mềm và yêu cầu nghiệp vụ: `ARCHITECTURE.md`, `PRD.md`, `RULES.md` tại thư mục gốc dự án.
