# 🚀 Cách chạy Admin Web Dashboard

## Phương pháp tối ưu: VS Code Live Server

### Bước 1: Cài extension Live Server
Trong VS Code → Extensions → tìm **"Live Server"** (Ritwick Dey) → Install

### Bước 2: Mở thư mục admin-web
```
File → Open Folder → f:\RinnSanCreavity_Kotlin\admin-web\
```

### Bước 3: Launch
Click chuột phải vào `index.html` → **"Open with Live Server"**

Browser sẽ tự mở tại: `http://127.0.0.1:5500/index.html`

---

## Phương pháp thay thế: Python HTTP Server

Mở PowerShell tại thư mục `admin-web`:
```powershell
cd f:\RinnSanCreavity_Kotlin\admin-web
python -m http.server 8080
```
Mở browser: `http://localhost:8080`

---

## ⚠️ KHÔNG mở file:// trực tiếp!
Firebase JS SDK (ES Modules) **yêu cầu HTTP server** để hoạt động.
File `file://` sẽ bị CORS error.

---

## Login
Dùng tài khoản Firebase Admin của bạn (có `role: "admin"` trong Firestore).
