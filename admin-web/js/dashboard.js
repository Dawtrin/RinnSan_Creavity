// ═══════════════════════════════════════════════════════════════
// DASHBOARD CONTROLLER v3 — Full-featured: Theme, Notifications,
// Keyboard Shortcuts, Mobile Sidebar, Quick Actions, Smooth Data
// ═══════════════════════════════════════════════════════════════
import "./auth.js?v=3";
import { initOverview }   from "./overview.js?v=3";
import { initInventory }  from "./inventory.js?v=3";
import { initOrders }     from "./orders.js?v=3";
import { initBrands }     from "./brands.js?v=3";
import { initUsers }      from "./users.js?v=3";
import { initSupport }    from "./support.js?v=3";

// ─── Tab Config ────────────────────────────────────────────────
const TABS = {
  overview:  { title:"OVERVIEW",   sub:"// DASHBOARD COMMAND CENTER",       init: initOverview  },
  inventory: { title:"INVENTORY",  sub:"// THE VAULT — PRODUCT MANAGEMENT", init: initInventory },
  orders:    { title:"ORDERS",     sub:"// ORDER MANAGEMENT SYSTEM",        init: initOrders    },
  brands:    { title:"BRANDS",     sub:"// AFFILIATE COMMISSION RATES",     init: initBrands    },
  users:     { title:"USERS",      sub:"// USER REGISTRY & PERMISSIONS",    init: initUsers     },
  support:   { title:"SUPPORT",    sub:"// TICKET MANAGEMENT CENTER",       init: initSupport   },
};

const inited = new Set();
let curTab = "overview";

// ─── Toast ─────────────────────────────────────────────────────
export function toast(msg, type = "inf", dur = 3200) {
  const box = document.getElementById("toast-box");
  if (!box) return;
  const el = document.createElement("div");
  el.className = `toast ${type}`;
  el.textContent = msg;
  box.appendChild(el);
  setTimeout(() => {
    el.style.opacity = "0";
    el.style.transition = "opacity .3s";
    setTimeout(() => el.remove(), 300);
  }, dur);
}

// ─── Format helpers ────────────────────────────────────────────
export function fmtVnd(n) {
  if (!n && n !== 0) return "—";
  n = Number(n);
  if (n >= 1_000_000_000) return (n/1_000_000_000).toFixed(1) + "B₫";
  if (n >= 1_000_000)     return (n/1_000_000).toFixed(1) + "M₫";
  if (n >= 1_000)         return Math.round(n/1_000) + "K₫";
  return n.toLocaleString("vi-VN") + "₫";
}
export function fmtNum(n) {
  if (!n && n !== 0) return "0";
  return Number(n).toLocaleString("vi-VN");
}
export function fmtDate(ts) {
  if (!ts) return "—";
  const d = new Date(typeof ts === "object" ? ts.toDate?.() || ts : ts);
  return d.toLocaleDateString("vi-VN", {day:"2-digit",month:"2-digit",year:"numeric",hour:"2-digit",minute:"2-digit"});
}
export function timeAgo(ts) {
  const diff = Date.now() - (typeof ts === "object" ? ts.toDate?.()?.getTime() || 0 : ts);
  const m = Math.floor(diff/60000);
  if (m < 1) return "just now";
  if (m < 60) return `${m}m ago`;
  const h = Math.floor(m/60);
  if (h < 24) return `${h}h ago`;
  return `${Math.floor(h/24)}d ago`;
}
export function badgeStatus(status) {
  const s = (status||"").toLowerCase();
  const map = {pending:"b-orange",paid:"b-blue",delivered:"b-green",cancelled:"b-red",new:"b-orange",open:"b-orange",resolved:"b-acid",active:"b-green",banned:"b-red",admin:"b-acid",user:"b-gray"};
  return `<span class="badge ${map[s]||'b-gray'}">${s.toUpperCase()}</span>`;
}

// ─── Modal ─────────────────────────────────────────────────────
export function openModal(id) { document.getElementById(id)?.classList.add("show"); }
export function closeModal(id) { document.getElementById(id)?.classList.remove("show"); }

document.querySelectorAll("[data-close]").forEach(btn =>
  btn.addEventListener("click", () => closeModal(btn.dataset.close))
);
document.querySelectorAll(".overlay").forEach(o =>
  o.addEventListener("click", e => { if (e.target === o) closeModal(o.id); })
);

// ─── Pagination ────────────────────────────────────────────────
export function buildPages(containerId, total, cur, onPage) {
  const el = document.getElementById(containerId);
  if (!el) return;
  el.innerHTML = "";
  if (total <= 1) return;
  const prev = mkBtn("←", cur === 1, () => onPage(cur - 1));
  el.appendChild(prev);
  // Show limited page numbers for large counts
  const maxShow = 7;
  let start = Math.max(1, cur - Math.floor(maxShow/2));
  let end = Math.min(total, start + maxShow - 1);
  if (end - start < maxShow - 1) start = Math.max(1, end - maxShow + 1);
  if (start > 1) { el.appendChild(mkBtn(1, false, () => onPage(1))); if (start > 2) el.appendChild(mkEllipsis()); }
  for (let i = start; i <= end; i++) {
    const b = mkBtn(i, false, () => onPage(i));
    if (i === cur) b.classList.add("on");
    el.appendChild(b);
  }
  if (end < total) { if (end < total - 1) el.appendChild(mkEllipsis()); el.appendChild(mkBtn(total, false, () => onPage(total))); }
  el.appendChild(mkBtn("→", cur === total, () => onPage(cur + 1)));
}
function mkBtn(lbl, disabled, fn) {
  const b = document.createElement("button");
  b.className = "page-btn";
  b.textContent = lbl;
  b.disabled = disabled;
  b.onclick = fn;
  return b;
}
function mkEllipsis() {
  const s = document.createElement("span");
  s.textContent = "…";
  s.style.cssText = "color:var(--silver);font-size:12px;padding:0 4px";
  return s;
}

// ─── Console log ───────────────────────────────────────────────
export function cmdLog(id, msg, isOk = false) {
  const el = document.getElementById(id);
  if (!el) return;
  el.textContent = msg;
  el.classList.toggle("err", !isOk);
  el.classList.add("show");
  setTimeout(() => el.classList.remove("show"), 4000);
}

// ─── Pill badge ────────────────────────────────────────────────
export function setPill(id, count) {
  const el = document.getElementById(id);
  if (!el) return;
  if (count > 0) { el.textContent = count; el.classList.add("show"); }
  else el.classList.remove("show");
}

// ─── Animated KPI update ───────────────────────────────────────
export function animateValue(el, newVal) {
  if (!el) return;
  const old = el.textContent;
  el.textContent = newVal;
  if (old !== newVal && old !== "—" && old.trim() !== "" && old !== "\xa0") {
    el.classList.remove("count-up");
    void el.offsetWidth; // trigger reflow
    el.classList.add("count-up");
    // Pulse parent card
    const card = el.closest(".kpi-card");
    if (card) {
      card.classList.remove("data-pulse");
      void card.offsetWidth;
      card.classList.add("data-pulse");
    }
  }
}

// ─── Tab switching ─────────────────────────────────────────────
export function switchTab(name) {
  if (!TABS[name]) return;
  curTab = name;
  document.querySelectorAll(".nav-btn").forEach(b => b.classList.remove("active"));
  document.getElementById(`nav-${name}`)?.classList.add("active");
  document.querySelectorAll(".tab-pane").forEach(p => p.classList.remove("active"));
  document.getElementById(`tab-${name}`)?.classList.add("active");
  document.getElementById("hdr-title").textContent = TABS[name].title;
  document.getElementById("hdr-sub").textContent = TABS[name].sub;
  if (!inited.has(name)) {
    inited.add(name);
    TABS[name].init();
  }
  // Close mobile sidebar
  closeMobileSidebar();
}

document.querySelectorAll(".nav-btn[data-tab]").forEach(b =>
  b.addEventListener("click", () => switchTab(b.dataset.tab))
);

document.getElementById("refresh-btn")?.addEventListener("click", () => {
  const btn = document.getElementById("refresh-btn");
  btn.classList.add("spin");
  inited.delete(curTab);
  switchTab(curTab);
  setTimeout(() => btn.classList.remove("spin"), 800);
});

// ─── Mobile Sidebar ────────────────────────────────────────────
const hamburger = document.getElementById("hamburger-btn");
const sidebar   = document.getElementById("sidebar");
const backdrop  = document.getElementById("sidebar-backdrop");

function closeMobileSidebar() {
  sidebar?.classList.remove("open");
  hamburger?.classList.remove("open");
  backdrop?.classList.remove("show");
}

hamburger?.addEventListener("click", () => {
  const isOpen = sidebar.classList.toggle("open");
  hamburger.classList.toggle("open", isOpen);
  backdrop?.classList.toggle("show", isOpen);
});
backdrop?.addEventListener("click", closeMobileSidebar);

// ─── Theme Switcher ────────────────────────────────────────────
const themeBtn   = document.getElementById("theme-btn");
const themePanel = document.getElementById("theme-panel");

themeBtn?.addEventListener("click", e => {
  e.stopPropagation();
  themePanel.classList.toggle("show");
  // Close notif
  document.getElementById("notif-panel")?.classList.remove("show");
});

document.querySelectorAll(".theme-option").forEach(opt => {
  opt.addEventListener("click", () => {
    const theme = opt.dataset.theme;
    document.documentElement.setAttribute("data-theme", theme);
    localStorage.setItem("rinnsan-theme", theme);
    document.querySelectorAll(".theme-option").forEach(o => o.classList.remove("active"));
    opt.classList.add("active");
    themePanel.classList.remove("show");
    toast(`// THEME → ${opt.querySelector(".theme-lbl").textContent}`, "inf");
    window.dispatchEvent(new CustomEvent("theme-changed", { detail: { theme } }));
  });
});

// Load saved theme
const savedTheme = localStorage.getItem("rinnsan-theme") || "";
if (savedTheme) {
  document.documentElement.setAttribute("data-theme", savedTheme);
  document.querySelectorAll(".theme-option").forEach(o => {
    o.classList.toggle("active", o.dataset.theme === savedTheme);
  });
  setTimeout(() => {
    window.dispatchEvent(new CustomEvent("theme-changed", { detail: { theme: savedTheme } }));
  }, 1000);
}

// ─── Notification Center ───────────────────────────────────────
const notifBtn   = document.getElementById("notif-btn");
const notifPanel = document.getElementById("notif-panel");
const notifList  = document.getElementById("notif-list");
const notifCount = document.getElementById("notif-count");
let notifications = [];

notifBtn?.addEventListener("click", e => {
  e.stopPropagation();
  notifPanel.classList.toggle("show");
  themePanel?.classList.remove("show");
});

document.getElementById("notif-clear-btn")?.addEventListener("click", () => {
  notifications = [];
  renderNotifications();
  notifPanel.classList.remove("show");
});

// Close panels on outside click
document.addEventListener("click", e => {
  if (!e.target.closest("#notif-wrap")) notifPanel?.classList.remove("show");
  if (!e.target.closest("#theme-wrap")) themePanel?.classList.remove("show");
});

function playNotifySound() {
  try {
    const ctx = new (window.AudioContext || window.webkitAudioContext)();
    const osc = ctx.createOscillator();
    const gain = ctx.createGain();
    osc.connect(gain);
    gain.connect(ctx.destination);
    osc.type = "sine";
    osc.frequency.setValueAtTime(880, ctx.currentTime);
    osc.frequency.exponentialRampToValueAtTime(1760, ctx.currentTime + 0.12);
    gain.gain.setValueAtTime(0.05, ctx.currentTime);
    gain.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + 0.12);
    osc.start(ctx.currentTime);
    osc.stop(ctx.currentTime + 0.12);
  } catch (e) {
    console.log("Audio notify sound blocked or unsupported:", e);
  }
}

export function addNotification(msg, type = "info", icon = "bell") {
  const ICON_MAP = {
    bell:  `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/></svg>`,
    order: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M6 2 3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"/><line x1="3" y1="6" x2="21" y2="6"/></svg>`,
    ticket:`<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>`,
    user:  `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/></svg>`,
  };
  const COLOR_MAP = { info:"var(--acid)", order:"var(--green)", ticket:"var(--orange)", user:"var(--blue)" };
  notifications.unshift({
    msg, type, icon: ICON_MAP[icon] || ICON_MAP.bell,
    color: COLOR_MAP[type] || COLOR_MAP.info,
    time: Date.now(), unread: true
  });
  if (notifications.length > 30) notifications.pop();
  renderNotifications();
  playNotifySound();
}

function renderNotifications() {
  if (!notifList) return;
  const unread = notifications.filter(n => n.unread).length;
  if (notifCount) {
    if (unread > 0) { notifCount.textContent = unread > 9 ? "9+" : unread; notifCount.style.display = "flex"; }
    else notifCount.style.display = "none";
  }
  if (!notifications.length) {
    notifList.innerHTML = '<div class="notif-empty">// NO NOTIFICATIONS</div>';
    return;
  }
  notifList.innerHTML = notifications.slice(0, 20).map((n, i) => `
    <div class="notif-item${n.unread ? ' unread' : ''}" onclick="window._readNotif(${i})">
      <div class="notif-ico" style="background:${n.color}15;border:1px solid ${n.color}30">${n.icon}</div>
      <div class="notif-body">
        <div class="notif-msg">${n.msg}</div>
        <div class="notif-time">${timeAgo(n.time)}</div>
      </div>
    </div>`).join("");
}

window._readNotif = idx => {
  if (notifications[idx]) notifications[idx].unread = false;
  renderNotifications();
};

// ─── Keyboard Shortcuts ────────────────────────────────────────
const TAB_KEYS = ["overview","inventory","orders","brands","users","support"];
document.addEventListener("keydown", e => {
  // Don't trigger in inputs
  if (e.target.matches("input,textarea,select")) return;

  // Ctrl+1-6 switch tabs
  if (e.ctrlKey && e.key >= "1" && e.key <= "6") {
    e.preventDefault();
    switchTab(TAB_KEYS[parseInt(e.key) - 1]);
    return;
  }
  // Ctrl+N open add artifact
  if (e.ctrlKey && e.key.toLowerCase() === "n") {
    e.preventDefault();
    switchTab("inventory");
    setTimeout(() => document.getElementById("add-art-btn")?.click(), 200);
    return;
  }
  // Ctrl+/ show shortcuts
  if (e.ctrlKey && e.key === "/") {
    e.preventDefault();
    document.getElementById("shortcuts-overlay")?.classList.toggle("show");
    return;
  }
  // Escape close modals
  if (e.key === "Escape") {
    document.querySelectorAll(".overlay.show").forEach(o => o.classList.remove("show"));
    document.getElementById("shortcuts-overlay")?.classList.remove("show");
    closeMobileSidebar();
    notifPanel?.classList.remove("show");
    themePanel?.classList.remove("show");
  }
});

// ─── Quick Actions Bar ─────────────────────────────────────────
document.getElementById("qa-add")?.addEventListener("click", () => {
  switchTab("inventory");
  setTimeout(() => document.getElementById("add-art-btn")?.click(), 200);
});
document.getElementById("qa-orders")?.addEventListener("click", () => switchTab("orders"));
document.getElementById("qa-support")?.addEventListener("click", () => switchTab("support"));
document.getElementById("qa-shortcuts")?.addEventListener("click", () => {
  document.getElementById("shortcuts-overlay")?.classList.toggle("show");
});

// ─── Loading Overlay ───────────────────────────────────────────
function showLoading() { document.getElementById("loading-overlay")?.classList.add("show"); }
function hideLoading() {
  const el = document.getElementById("loading-overlay");
  if (el) { el.style.transition = "opacity .5s ease"; el.classList.remove("show"); }
}

// ─── Auth ready ────────────────────────────────────────────────
showLoading();

window.addEventListener("admin-ready", e => {
  const user = e.detail.user;

  // Set user email in sidebar
  document.getElementById("sb-email").textContent = user.email || "";

  // Set avatar
  const initials = (user.email || "A")[0].toUpperCase();
  const avatarEl = document.getElementById("sb-avatar");
  if (avatarEl) avatarEl.textContent = initials;

  hideLoading();
  switchTab("overview");

  // Welcome notification
  addNotification(`Welcome back, ${user.email?.split("@")[0] || "Admin"}! Command Center ready.`, "info", "bell");
});

window.addEventListener("avatar-loaded", e => {
  const { url, username } = e.detail;
  const avatarEl = document.getElementById("sb-avatar");
  if (!avatarEl) return;
  if (url) {
    avatarEl.innerHTML = `<img src="${url}" alt="avatar"/>`;
  } else if (username) {
    avatarEl.textContent = username.slice(0,2).toUpperCase();
  }
});
