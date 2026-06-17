# 📋 PRD — RinnSan Creavity
**Product Requirements Document** | Version 1.0 | Cập nhật: 2026-04

---

## 1. Tổng Quan Sản Phẩm

**RinnSan Creavity** là ứng dụng Android thời trang cá nhân hóa, kết hợp:
- 🤖 **AI Stylist** — Tư vấn phong cách dựa trên nhận diện archetype
- 🛒 **Affiliate Commerce** — Bán & giới thiệu sản phẩm thời trang kiếm hoa hồng
- 📸 **Social Signal** — Mạng xã hội thời trang nội bộ
- 🧬 **Identity System** — Hệ thống phân loại phong cách theo 4 archetype

**Package:** `com.rinnsan.creavity`
**Min SDK:** 26 | **Target SDK:** 36

---

## 2. Đối Tượng Người Dùng

- Giới trẻ Việt Nam 18–30 tuổi
- Quan tâm thời trang streetwear, avant-garde, techwear
- Muốn được tư vấn phong cách cá nhân, không generic
- Có thể mua hàng qua app (affiliate hoặc trực tiếp)

---

## 3. Màn Hình & Tính Năng

### 3.1 GATEWAY — Splash Screen
- Kiểm tra Firebase session
- Route: `→ HOME` (đã login) hoặc `→ LOGIN` (chưa login)

### 3.2 AUTH — Đăng nhập / Đăng ký
- Login bằng Email/Password
- Google Sign-In
- Forgot Password
- Backend: Firebase Auth

### 3.3 HOME — "The Runway"
- Scrollytelling experience theo phong cách editorial
- Sections: `DiagonalScrollSection`, `RunwayPinnedSection`, `SoloArtifactSection`
- Nav tới các màn hình chính

### 3.4 THE VAULT — Kho Sản Phẩm (⭐ CORE FEATURE)
> Route: `artifact_archive_screen`
> ViewModel: `VaultViewModel`

**Đây là kênh kiếm tiền chính của app.** The Vault là nơi:

1. **Hiển thị sản phẩm** được phân loại theo 4 Archetype (GHOST / OPERATOR / GLITCH / NOMAD)
2. **Lọc thông minh** — User có thể xem "Global Arsenal" (tất cả) hoặc "Archetype Arsenal" (chỉ đúng phong cách của họ)
3. **Affiliate link** — Nút `[ACQUIRE]` mở link affiliate (Shopee, v.v.) → kiếm hoa hồng
4. **Sản phẩm tự bán** — Cũng được đăng qua Firestore `artifacts` collection, có thể là hàng RinnSan tự kinh doanh
5. **Wishlist** — User lưu sản phẩm yêu thích, đồng bộ Firebase Firestore

**Data flow:**
```
Firestore (collection: artifacts) → VaultViewModel → ArtifactArchiveScreen
User wishlist → Firestore (users/{uid}.wishlist[])
```

**AffiliateArtifact model:**
```kotlin
data class AffiliateArtifact(
    val id: String,
    val title: String,
    val category: String,
    val price: String,         // VD: "1.850.000 VND"
    val imageUrl: String,
    val affiliateLink: String, // Link Shopee/Lazada/v.v. hoặc link nội bộ
    val vendor: String,        // Tên nhà cung cấp / thương hiệu
    val archetype: Archetype   // Phân loại phong cách
)
```

**Cách thêm sản phẩm:** Upload document vào Firestore `artifacts` collection với các field tương ứng.

### 3.5 THE SIGNAL — Mạng Xã Hội
- Feed bài đăng thời trang
- Tạo bài đăng mới (hình ảnh, tags, style customization)
- Like, comment, share
- Upload ảnh lên Cloudinary

### 3.6 THE STYLIST (UPLINK) — AI Fashion Advisor
- Nhận diện archetype qua Identity Scanner (questionnaire)
- Chat với AI (Google Gemini `gemini-2.0-flash-lite`)
- Tư vấn cá nhân hóa theo archetype
- Lưu hội thoại (ArchiveScreen)

### 3.7 BRAND — "The Origin"
- Giới thiệu thương hiệu RinnSan

### 3.8 CONTACT — "The Uplink"
- Liên lạc với RinnSan

### 3.9 PROFILE
- Hồ sơ người dùng
- Wishlist
- Body Data (số đo, để AI gợi ý size/phối đồ phù hợp)

---

## 4. Mô Hình Kiếm Tiền

| Nguồn | Cơ chế | Màn hình |
|---|---|---|
| **Affiliate** | Người dùng click link và mua → nhận hoa hồng | The Vault |
| **Bán trực tiếp** | Sản phẩm do RinnSan tự kinh doanh trong Vault | The Vault |
| **Premium** | (Tương lai) Gói membership để unlock AI features | - |

---

## 5. Tech Stack

| Thành phần | Công nghệ |
|---|---|
| UI | Jetpack Compose + Material 3 |
| Navigation | Navigation Compose 2.7.7 |
| DI | Hilt (Dagger 2.48) |
| AI | Google Gemini API (`gemini-2.0-flash-lite`) qua Ktor |
| Auth | Firebase Auth |
| Database | Firebase Firestore |
| Image | Coil 2.6.0, Cloudinary |
| Video | ExoPlayer / Media3 |

---

## 6. Firestore Collections

| Collection | Mục đích |
|---|---|
| `artifacts` | Kho sản phẩm affiliate + sản phẩm bán |
| `users/{uid}` | Profile, wishlist, identityProfile, archetype |
| `signals` (giả định) | Bài đăng mạng xã hội |
