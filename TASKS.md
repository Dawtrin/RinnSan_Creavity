# ✅ TASKS — RinnSan Creavity
**Danh sách việc cần làm & tiến độ**

---

## 🔴 BUG CẦN FIX NGAY

- [ ] **[AppNavigation]** Route `BRAND` bị khai báo 2 lần (dòng 94 + 172) — xóa dòng 94
- [ ] **[Routes]** `create_post` đang hardcode trong AppNavigation — thêm `CREATE_POST` vào `Routes.kt`
- [ ] **[Artifact]** Merge `Artifact.kt` (domain) với `AffiliateArtifact` (data/vault) hoặc document rõ sự khác biệt

---

## 🟡 ĐANG PHÁT TRIỂN

- [ ] **[The Vault]** Thêm sản phẩm thật vào Firestore `artifacts/` collection
- [ ] **[The Vault]** Admin panel để thêm/sửa/xóa sản phẩm
- [ ] **[The Vault]** Tracking revenue từ affiliate clicks (ClickTracker)
- [ ] **[Signal]** Hoàn thiện flow Like / Comment / Share
- [ ] **[Share]** Implement `uplink/share/` folder — tính năng chia sẻ sản phẩm ra ngoài app

---

## 🟢 ĐÃ HOÀN THÀNH

- [x] Authentication (Login / Register / ForgotPassword / Google Sign-In)
- [x] Identity Scanner + IdentityEngine (4 archetypes)
- [x] UplinkScreen + VirtualStylistBox
- [x] StylistChatScreen với Gemini AI
- [x] ArtifactArchiveScreen (The Vault) — hiển thị + filter theo archetype
- [x] Wishlist Toggle (lưu vào Firestore)
- [x] AffiliateLink mở URL khi bấm ACQUIRE
- [x] CreatePostScreen với image upload
- [x] GatewayScreen với Firebase session check
- [x] HomeScreen với 3 sections
- [x] ProfileScreen

---

## 🔵 TƯƠNG LAI (Backlog)

- [ ] **[Commerce]** Thanh toán trực tiếp trong app (không cần redirect affiliate)
- [ ] **[Analytics]** Dashboard thống kê click affiliate, revenue
- [ ] **[Premium]** Gói membership để mở khóa tính năng AI nâng cao
- [ ] **[Notification]** Push notification khi có sản phẩm mới phù hợp archetype
- [ ] **[AR]** Thử đồ ảo (augmented reality)
- [ ] **[3D]** The Lab — xem sản phẩm 3D (route `PRODUCT_DETAIL` đã có trong Routes.kt)
- [ ] **[Shop]** The Data Bank — màn hình shop đầy đủ (route `SHOP` đã có)
- [ ] **[Onboarding]** Màn hình giới thiệu app (route `ONBOARDING` đã có)

---

## 📝 GHI CHÚ KỸ THUẬT

### Cách thêm sản phẩm vào The Vault (hiện tại)
1. Vào Firebase Console → Firestore
2. Tạo document trong collection `artifacts`
3. Điền đủ fields: `id`, `title`, `category`, `price`, `imageUrl`, `affiliateLink`, `vendor`, `archetype`
4. `archetype` phải là: `GHOST` | `OPERATOR` | `GLITCH` | `NOMAD`

### Cách lấy Gemini API Key
1. Vào https://aistudio.google.com/app/apikey
2. Tạo key mới
3. Thêm vào `local.properties`: `GEMINI_API_KEY=your_key`
4. Clean & Rebuild project
