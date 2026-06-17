# 🎯 PROMPTS — RinnSan Creavity
**Thư viện prompt hay dùng cho project này**

> Copy prompt phù hợp, paste vào AI, thay phần `[...]` rồi gửi.
> Luôn paste `CONTEXT.md` trước prompt để AI hiểu project.

---

## ── NHÓM 1: THÊM TÍNH NĂNG MỚI ──

### Thêm màn hình mới
```
Dự án: RinnSan Creavity (Android Kotlin + Jetpack Compose)
[Paste CONTEXT.md vào đây]

Tạo màn hình [TÊN MÀN HÌNH] với:
- Route: thêm vào Routes.kt với constant [ROUTE_NAME]
- File: presentation/[feature]/[ScreenName].kt
- ViewModel: [ViewModelName].kt dùng @HiltViewModel
- Màn hình phải theo design system: VoidBlack background, CyberAcid accent, font Oswald/SpaceMono
- [Mô tả tính năng cụ thể]
```

### Thêm sản phẩm vào The Vault (hàng loạt)
```
[Paste CONTEXT.md]

Tạo script hoặc hướng dẫn để upload hàng loạt sản phẩm lên Firestore collection "artifacts" với format:
{
  id: String,
  title: String,
  category: String,
  price: String (VD: "1.850.000 VND"),
  imageUrl: String,
  affiliateLink: String,
  vendor: String,
  archetype: "GHOST" | "OPERATOR" | "GLITCH" | "NOMAD"
}

Danh sách sản phẩm cần upload:
[Dán danh sách sản phẩm vào đây]
```

### Thêm câu hỏi vào Identity Scanner
```
[Paste CONTEXT.md]

Thêm [N] câu hỏi mới vào QuestionDatabase.kt cho Identity Scanner.
Mỗi câu hỏi có 4 lựa chọn, mỗi lựa chọn ảnh hưởng đến điểm của 1 trong 4 archetype: GHOST, OPERATOR, GLITCH, NOMAD.
Chủ đề câu hỏi: [chủ đề - VD: giày dép, phối màu, dịp mặc, v.v.]
```

---

## ── NHÓM 2: FIX BUG ──

### Fix bug cụ thể
```
[Paste CONTEXT.md]

Bug: [Mô tả bug]
File liên quan: [đường dẫn file]
Error message (nếu có): [paste error]

Tìm nguyên nhân và fix, giữ nguyên kiến trúc hiện tại.
```

### Fix bug Route BRAND trùng (đã biết)
```
[Paste CONTEXT.md]

Fix bug trong AppNavigation.kt: Route BRAND được khai báo 2 lần.
- Dòng 94: placeholder trống (cần xóa)
- Dòng 172: implementation thực (giữ lại)
Đồng thời thêm CREATE_POST = "create_post" vào Routes.kt và cập nhật AppNavigation.
```

---

## ── NHÓM 3: THE VAULT ──

### Tối ưu hiển thị sản phẩm
```
[Paste CONTEXT.md]

Tối ưu ArtifactArchiveScreen.kt:
- [Yêu cầu cụ thể: VD thêm search, thêm sort by price, thêm category filter, v.v.]
Giữ nguyên design: VoidBlack background, CyberAcid accent, brutalist style.
```

### Thêm tracking doanh thu affiliate
```
[Paste CONTEXT.md]

Mở rộng ClickTracker.kt để:
- Lưu click data lên Firestore collection "affiliate_clicks"
- Fields: userId, artifactId, affiliateLink, vendor, timestamp, archetype
- VaultViewModel phải gọi tracker khi user bấm ACQUIRE
```

---

## ── NHÓM 4: AI STYLIST ──

### Cải thiện Gemini prompt
```
[Paste CONTEXT.md]

Cải thiện system prompt trong GeminiRepository.kt (hàm buildSystemPromptV3).
Archetype cần cải thiện: [GHOST / OPERATOR / GLITCH / NOMAD]
Hướng cải thiện: [VD: thêm gợi ý brand cụ thể, thêm context về weather Việt Nam, v.v.]
```

### Thêm tính năng gợi ý sản phẩm từ Vault sau chat
```
[Paste CONTEXT.md]

Sau khi AI stylist trả lời, tự động gợi ý 2-3 sản phẩm liên quan từ Vault (AffiliateArtifact).
Logic: match archetype của user với archetype của sản phẩm.
Hiển thị mini card sản phẩm phía dưới message của AI trong StylistChatScreen.
```

---

## ── NHÓM 5: SIGNAL (SOCIAL) ──

### Hoàn thiện Like / Comment
```
[Paste CONTEXT.md]

Hoàn thiện tính năng Like trong TheSignalScreen.kt:
- Toggle like (thêm/bỏ userId khỏi interactions.likes.userIds)
- Cập nhật Firestore realtime
- Animation khi bấm like (scale + color)
- Dùng SignalRepository để handle Firestore operations
```

---

## ── NHÓM 6: REFACTOR & TỐI ƯU ──

### Review và refactor file
```
[Paste CONTEXT.md]

Review file [đường dẫn file] và:
1. Tìm các vấn đề về performance, memory leak, hoặc anti-pattern
2. Đề xuất cải thiện theo Clean Architecture
3. Không thay đổi logic, chỉ cải thiện code quality
```

### Tách component lớn
```
[Paste CONTEXT.md]

File [đường dẫn file] đang quá lớn ([N] dòng). Hãy tách các composable function thành file riêng trong thư mục components/ cùng level, giữ nguyên logic và tên function.
```

---

## ── NHÓM 7: DESIGN ──

### Tạo component mới theo design system
```
[Paste CONTEXT.md]

Tạo Composable component [Tên Component] với:
- Background: VoidBlack
- Accent: CyberAcid
- Text: TeslaWhite (chính) / TechSilver (phụ)
- Font: Oswald cho tiêu đề, SpaceMono cho labels
- Style: brutalist, angular, không bo góc
- Tính năng: [mô tả]
```

---

## ── TIPS SỬ DỤNG ──

**Prompt ngắn gọn nhất có thể:**
> "Tôi dùng Android Studio + Kotlin + Compose + Hilt + Firebase. [CONTEXT.md]. [Yêu cầu]."

**Luôn kết thúc prompt bằng:**
> "Giữ nguyên kiến trúc, chỉ sửa file cần thiết, không thêm dependency mới trừ khi bắt buộc."
