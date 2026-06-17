# 📐 RULES — RinnSan Creavity
**Quy tắc bắt buộc cho AI & Developer**

---

> [!IMPORTANT]
> Đây là file AI phải đọc đầu tiên trước khi viết bất kỳ dòng code nào cho dự án này.

---

## ── RULE 01: KHÔNG BAO GIỜ THAY ĐỔI ARCHITECTURE ──

- Luôn giữ đúng `core / data / domain / presentation` layer
- Không di chuyển files giữa các layer mà không nói rõ lý do
- Repository chỉ sống trong `data/repository/`
- Model chỉ sống trong `domain/model/`
- ViewModel chỉ sống trong `presentation/{feature}/`

---

## ── RULE 02: NAVIGATION — LUÔN DÙNG Routes.kt ──

```kotlin
// ✅ ĐÚNG
navController.navigate(Routes.SIGNAL)

// ❌ SAI — không hardcode route string
navController.navigate("signal_screen")
```

- Mọi route constant phải được khai báo trong `Routes.kt`
- Nếu thêm màn hình mới → thêm vào `Routes.kt` trước, rồi mới dùng
- `create_post` hiện đang hardcode → **TODO: thêm vào Routes.kt**

---

## ── RULE 03: HILT DI — KHÔNG NEW VIEWMODEL THỦ CÔNG ──

```kotlin
// ✅ ĐÚNG
val viewModel: VaultViewModel = hiltViewModel()

// ❌ SAI
val viewModel = VaultViewModel()
```

- Tất cả ViewModel phải annotate `@HiltViewModel`
- Tất cả Repository phải annotate `@Singleton` + `@Inject constructor()`
- Không khởi tạo dependency thủ công bên trong class

---

## ── RULE 04: THE VAULT — QUY TẮC THÊM SẢN PHẨM ──

- Sản phẩm được quản lý qua **Firestore** collection `artifacts/`
- Model bắt buộc có field: `id`, `title`, `category`, `price`, `imageUrl`, `affiliateLink`, `vendor`, `archetype`
- `archetype` phải là một trong: `GHOST`, `OPERATOR`, `GLITCH`, `NOMAD` (uppercase)
- `affiliateLink` là link affiliate (Shopee/Lazada) hoặc link sản phẩm tự bán
- Không hardcode sản phẩm vào MockVault khi thêm sản phẩm thật — MockVault chỉ dùng cho development/demo
- Khi mở affiliate link phải gọi `ClickTracker.recordClick(artifact)` để tracking

---

## ── RULE 05: GEMINI API KEY — SECURITY ──

```kotlin
// ✅ ĐÚNG — đọc từ BuildConfig
private val apiKey = BuildConfig.GEMINI_API_KEY

// ❌ NGHIÊM CẤM — không bao giờ hardcode
private val apiKey = "AIzaSy..." // CẤM TUYỆT ĐỐI
```

- API key chỉ được đặt trong `local.properties` (file này không được commit)
- `local.properties` phải có dòng: `GEMINI_API_KEY=your_key_here`
- Không bao giờ log API key ra Logcat

---

## ── RULE 06: FIREBASE FIRESTORE — CẤU TRÚC DOCUMENT ──

```
users/{uid}
  ├── wishlist: String[]     ← Mảng artifact ID đã lưu
  ├── identityProfile: Map   ← IdentityProfile serialized
  └── archetype: String      ← Dominant archetype name

artifacts/{id}
  ├── id: String
  ├── title: String
  ├── category: String
  ├── price: String          ← VD: "1.850.000 VND"
  ├── imageUrl: String
  ├── affiliateLink: String
  ├── vendor: String
  └── archetype: String      ← "GHOST" | "OPERATOR" | "GLITCH" | "NOMAD"
```

- Khi write Firestore, **luôn dùng** `SetOptions.merge()` để không ghi đè dữ liệu khác
- Không dùng `.set()` không có `merge` options

---

## ── RULE 07: IDENTITY PROFILE — IMMUTABLE ──

- `IdentityProfile` là **immutable data class** — không được sửa sau khi tạo
- Chỉ `IdentityEngine` được tạo `IdentityProfile`
- Các screen khác chỉ được đọc profile, không viết
- Khi cần update thì tạo mới hoàn toàn

---

## ── RULE 08: SHARED VIEWMODEL ──

- `UplinkViewModel` là **shared ViewModel** được dùng qua nhiều màn hình
- Khởi tạo tại cấp `AppNavigation` (hoặc activity scope)
- Không tạo instance mới trong từng screen riêng lẻ

```kotlin
// ✅ ĐÚNG — tạo 1 lần ở navigation level
val uplinkViewModel: UplinkViewModel = hiltViewModel()

// Truyền qua composable destination
composable(Routes.STYLIST_CHAT) {
    StylistChatScreen(viewModel = uplinkViewModel, ...)
}
```

---

## ── RULE 09: COMPOSE — STATE HOISTING ──

- Tất cả state phải được **hoist** lên ViewModel, không giữ trong composable
- Dùng `StateFlow` trong ViewModel, collect bằng `collectAsState()` trong UI
- Không dùng `mutableStateOf` cho business data (chỉ cho local UI state như animation)

---

## ── RULE 10: THEME & DESIGN ──

- Luôn dùng color token (`VoidBlack`, `CyberAcid`, ...) — không hardcode hex trong UI code
- Luôn dùng font token (`AppFonts.oswald`, `AppFonts.spaceMono`, ...) — không dùng default font
- Tất cả text label, status indicator phải viết HOA (UPPERCASE) theo thiết kế brutalist
- Không thêm bo góc (rounded corner) trừ khi có yêu cầu đặc biệt — design này là angular/sharp

---

## ── RULE 11: LỖI BUG CẦN FIX ──

Những vấn đề AI cần biết để không lặp lại:

1. **Route BRAND trùng** — `AppNavigation.kt` line 94 (placeholder trống) và line 172 (thực tế). Dòng 94 cần xóa.
2. **`create_post` hardcode** — cần thêm `const val CREATE_POST = "create_post"` vào `Routes.kt`
3. **`share/` folder trống** — chưa implement share feature
4. **`Artifact.kt` vs `AffiliateArtifact`** — có 2 model khác nhau. `AffiliateArtifact` trong `data/vault/` là model chính cho The Vault. `Artifact.kt` trong `domain/model/` là legacy, có thể merge sau.

---

## ── RULE 12: NGÔN NGỮ & NỘI DUNG ──

- Copy UI (labels, buttons, headers) bằng **tiếng Anh viết hoa** theo phong cách techwear/military
- Comment trong code có thể bằng tiếng Việt
- AI chat (Gemini) hỗ trợ cả tiếng Việt lẫn tiếng Anh — auto-detect
- Tên màn hình: dùng tên code như "The Vault", "The Signal", "The Stylist" — không đổi sang tiếng Việt
