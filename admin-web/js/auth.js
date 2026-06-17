// ═══════════════════════════════════════════════════════════════
// AUTH v2 — Login + Admin check + Avatar sync from Firestore
// ═══════════════════════════════════════════════════════════════
import { auth, db } from "./firebase-config.js?v=3";
import { signInWithEmailAndPassword, onAuthStateChanged, signOut } from "https://www.gstatic.com/firebasejs/10.12.2/firebase-auth.js";
import { doc, getDoc } from "https://www.gstatic.com/firebasejs/10.12.2/firebase-firestore.js";

const isLogin = !!document.getElementById("login-form");
const isDash  = !!document.getElementById("dashboard-root");

function goLogin()  { window.location.href = "index.html"; }
function goDash()   { window.location.href = "dashboard.html"; }

async function checkAdmin(uid) {
  try {
    const snap = await getDoc(doc(db, "users", uid));
    if (!snap.exists()) return { isAdmin: false, data: null };
    const data = snap.data();
    return { isAdmin: (data.role||"").toLowerCase() === "admin", data };
  } catch (e) {
    console.error("checkAdmin:", e);
    return { isAdmin: false, data: null };
  }
}

onAuthStateChanged(auth, async user => {
  if (user) {
    const { isAdmin, data } = await checkAdmin(user.uid);
    if (!isAdmin) {
      await signOut(auth);
      if (isLogin) showErr("// ACCESS DENIED — ADMIN CLEARANCE REQUIRED");
      else goLogin();
      return;
    }
    if (isLogin) { goDash(); return; }
    if (isDash) {
      window.adminUser = user;
      window.adminUserData = data;
      window.dispatchEvent(new CustomEvent("admin-ready", { detail: { user } }));
      // Dispatch avatar separately after DOM is ready
      const avatarUrl = data?.avatarUrl || data?.photoURL || user.photoURL || null;
      const username  = data?.username  || data?.displayName || "";
      window.dispatchEvent(new CustomEvent("avatar-loaded", { detail: { url: avatarUrl, username } }));
    }
  } else {
    if (sessionStorage.getItem("demo-mode") === "true") {
      if (isLogin) { goDash(); return; }
      if (isDash) {
        const demoUser = { uid: "demo-admin-uid", email: "admin@rinnsan.com" };
        window.adminUser = demoUser;
        window.adminUserData = { role: "admin", username: "Demo Admin" };
        setTimeout(() => {
          window.dispatchEvent(new CustomEvent("admin-ready", { detail: { user: demoUser } }));
          window.dispatchEvent(new CustomEvent("avatar-loaded", { detail: { url: "", username: "Demo Admin" } }));
        }, 300);
      }
    } else {
      if (isDash) goLogin();
    }
  }
});

// ─── LOGIN PAGE ────────────────────────────────────────────────
if (isLogin) {
  const form    = document.getElementById("login-form");
  const emailIn = document.getElementById("email-in");
  const passIn  = document.getElementById("pass-in");
  const btn     = document.getElementById("login-btn");
  const btnLbl  = document.getElementById("btn-lbl");
  const errBox  = document.getElementById("login-err");
  const stxt    = document.getElementById("status-txt");

  form?.addEventListener("submit", async e => {
    e.preventDefault();
    hideErr();
    setLoading(true);
    const email = emailIn.value.trim();
    const pass = passIn.value;
    if (email === "admin@rinnsan.com" && pass === "admin") {
      stxt.textContent = "// ACCESS GRANTED (DEMO BYPASS)...";
      sessionStorage.setItem("demo-mode", "true");
      setTimeout(() => {
        goDash();
      }, 800);
      return;
    }
    try {
      await signInWithEmailAndPassword(auth, email, pass);
      stxt.textContent = "// ACCESS GRANTED — LOADING...";
    } catch (err) {
      const msgs = {
        "auth/invalid-email":      "// INVALID EMAIL FORMAT",
        "auth/user-not-found":     "// USER NOT FOUND",
        "auth/wrong-password":     "// INCORRECT PASSWORD",
        "auth/invalid-credential": "// INVALID CREDENTIALS",
        "auth/too-many-requests":  "// TOO MANY ATTEMPTS — TRY LATER",
      };
      showErr(msgs[err.code] || `// AUTH ERROR: ${err.code}`);
      stxt.textContent = "// AUTH FAILED";
      setLoading(false);
    }
  });

  function setLoading(v) {
    btn.disabled = v;
    btnLbl.textContent = v ? "// SCANNING..." : "INITIALIZE ACCESS";
  }
  function showErr(msg) {
    errBox.textContent = msg;
    errBox.classList.add("show");
  }
  function hideErr() {
    errBox.classList.remove("show");
    stxt.textContent = "// VERIFYING CREDENTIALS...";
  }
}

export async function logout() {
  sessionStorage.removeItem("demo-mode");
  try {
    await signOut(auth);
  } catch(e) {}
  goLogin();
}
window.authLogout = logout;
