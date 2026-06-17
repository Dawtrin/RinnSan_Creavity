# ⚡ CONTEXT — RinnSan Creavity
> Paste file này vào đầu bất kỳ AI chat nào để AI hiểu ngay project.

---

## App là gì?
Android app thời trang (Kotlin + Jetpack Compose). Kết hợp:
- **AI Stylist** cá nhân hóa theo archetype (Google Gemini API)
- **The Vault** — Kho sản phẩm affiliate + sản phẩm tự bán, phân loại theo phong cách
- **The Signal** — Mạng xã hội thời trang nội bộ
- **Identity Scanner** — Quiz xác định 1 trong 4 archetype: GHOST / OPERATOR / GLITCH / NOMAD

**Package:** `com.rinnsan.creavity` | **IDE:** Android Studio | **Min SDK:** 26

---

## Tech Stack
Jetpack Compose · Hilt DI · Firebase Auth + Firestore · Gemini API (Ktor) · Coil · Cloudinary · Navigation Compose · Room

---

## Kiến trúc
Clean Architecture: `core/ → data/ → domain/ → presentation/`

```
core/
  constants/ · di/ · router/Routes.kt · theme/ · untils/
data/
  remote/CloudinaryApi · repository/(Gemini|Identity|Signal)Repository · tracker/ClickTracker · vault/MockVault+AffiliateArtifact
domain/
  engine/IdentityEngine · model/(Archetype|IdentityProfile|SignalPost|StylistState|ChatMessage)
presentation/
  navigation/AppNavigation · auth/ · gateway/ · home/ · signal/ · uplink/(The Stylist) · chat/ · archive/(The Vault) · brand/ · contact/ · profile/ · wishlist/ · bodydata/
```

---

## Routes (Routes.kt)
`GATEWAY → LOGIN/HOME → SIGNAL/STYLIST/BRAND/CONTACT → IDENTITY_SCANNER/STYLIST_CHAT → ARTIFACT_ARCHIVE`

---

## The Vault — Kênh kiếm tiền chính
- Hiển thị sản phẩm từ Firestore `artifacts/` collection
- Filter theo archetype của user
- Nút `[ACQUIRE]` → mở affiliate link (Shopee/Lazada) → hoa hồng
- Nút `[SAVE]` → lưu wishlist vào `users/{uid}.wishlist[]`
- Model: `AffiliateArtifact(id, title, category, price, imageUrl, affiliateLink, vendor, archetype)`

---

## 4 Archetypes
| | CONFORM | REBEL |
|---|---|---|
| SHOW | OPERATOR (techwear) | GLITCH (maximalist) |
| HIDE | GHOST (minimalist) | NOMAD (vintage) |

---

## Design System
- **Colors:** `VoidBlack(#000)` · `CyberAcid(#C0FF00)` · `TeslaWhite(#F5F5F5)` · `TechSilver(#8A8A8A)` · `GlitchRed(#FF0040)`
- **Fonts:** `AppFonts.oswald` (tiêu đề) · `AppFonts.spaceMono` (labels) · `AppFonts.inter` (body)
- **Style:** Brutalist/cyberpunk, angular (không bo góc), text UPPERCASE

---

## Rules quan trọng nhất
1. Route → luôn dùng `Routes.kt`, không hardcode string
2. ViewModel → luôn `@HiltViewModel` + `hiltViewModel()`, không new thủ công
3. API Key → chỉ từ `BuildConfig.GEMINI_API_KEY` (đọc từ `local.properties`)
4. Firestore write → luôn dùng `SetOptions.merge()`
5. `UplinkViewModel` là **shared ViewModel** — khởi tạo 1 lần ở AppNavigation
6. Colors/fonts → dùng token, không hardcode hex/font-name
7. UI text copy → viết HOA theo phong cách techwear

---

## Bugs đã biết
- Route `BRAND` khai báo 2 lần trong AppNavigation (dòng 94 + 172)
- `create_post` hardcode chưa có trong Routes.kt
- `share/` folder còn trống
