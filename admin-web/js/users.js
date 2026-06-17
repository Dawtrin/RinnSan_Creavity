// ═══════════════════════════════════════════════════════════════
// USERS v3 — Archetype visualization, ban/promote, search/filter
// ═══════════════════════════════════════════════════════════════
import { db } from "./firebase-config.js?v=3";
import { collection, onSnapshot, doc, updateDoc, deleteField } from "https://www.gstatic.com/firebasejs/10.12.2/firebase-firestore.js";
import { toast, fmtDate, badgeStatus, fmtNum, openModal, closeModal, buildPages, addNotification } from "./dashboard.js?v=3";

let allUsers = [], curPage = 1, filterRole = "ALL", filterStatus = "ALL";
const PAGE = 12;

export function initUsers() {
  onSnapshot(collection(db, "users"), snap => {
    allUsers = snap.docs.map(d => ({ uid:d.id, ...d.data() }));
    document.getElementById("users-meta").textContent = `// ${allUsers.length} REGISTERED AGENTS`;
    renderStats();
    renderArchBar();
    applyFilter();
  });
  setupFilters();
  setupUserModal();
}

function renderStats() {
  const admins  = allUsers.filter(u => u.role === "admin").length;
  const banned  = allUsers.filter(u => u.banned).length;
  const active  = allUsers.length - banned;
  const wrap    = document.getElementById("user-stats-grid");
  if (!wrap) return;
  wrap.innerHTML = `
    <div class="kpi-card" data-color="white">
      <div class="kpi-val">${fmtNum(allUsers.length)}</div><div class="kpi-lbl">TOTAL</div>
    </div>
    <div class="kpi-card" data-color="acid">
      <div class="kpi-val">${fmtNum(admins)}</div><div class="kpi-lbl">ADMINS</div>
    </div>
    <div class="kpi-card" data-color="red">
      <div class="kpi-val">${fmtNum(banned)}</div><div class="kpi-lbl">BANNED</div>
    </div>`;
}

function renderArchBar() {
  const archMap = {};
  const ARCH_COLORS = {
    VISIONARY:"#C0FF00", ARCHITECT:"#00BFFF", REBEL:"#FF003C",
    GUARDIAN:"#FF8C00", ALCHEMIST:"#BF00FF", NOMAD:"#00FF88",
    ORACLE:"#FFD700", SENTINEL:"#FF6B35"
  };
  allUsers.forEach(u => {
    const a = (u.archetype || "UNKNOWN").toUpperCase();
    archMap[a] = (archMap[a]||0) + 1;
  });
  const total = allUsers.length || 1;
  const entries = Object.entries(archMap).sort((a,b) => b[1]-a[1]);

  const barEl   = document.getElementById("arch-bar");
  const legendEl = document.getElementById("arch-legend");
  if (barEl) {
    barEl.innerHTML = entries.map(([a, cnt]) => {
      const pct = (cnt/total*100).toFixed(1);
      const col = ARCH_COLORS[a] || "var(--silver)";
      return `<div style="flex:${pct};min-width:2px;background:${col};border-radius:3px;transition:flex .8s cubic-bezier(.4,0,.2,1)"></div>`;
    }).join("");
  }
  if (legendEl) {
    legendEl.innerHTML = entries.map(([a, cnt]) => {
      const col = ARCH_COLORS[a] || "var(--silver)";
      return `<div style="display:flex;align-items:center;gap:5px;font-family:'Space Mono',monospace;font-size:8px;color:var(--silver)">
        <div style="width:8px;height:8px;border-radius:50%;background:${col}"></div>
        <span>${a}</span> <span style="color:var(--white);font-weight:700">${cnt}</span>
      </div>`;
    }).join("");
  }
}

function setupFilters() {
  document.querySelectorAll(".chip[data-group='role']").forEach(chip => {
    chip.addEventListener("click", () => {
      document.querySelectorAll(".chip[data-group='role']").forEach(c => c.classList.remove("on"));
      chip.classList.add("on");
      filterRole = chip.dataset.val;
      curPage = 1;
      applyFilter();
    });
  });
  document.querySelectorAll(".chip[data-group='status']").forEach(chip => {
    chip.addEventListener("click", () => {
      document.querySelectorAll(".chip[data-group='status']").forEach(c => c.classList.remove("on"));
      chip.classList.add("on");
      filterStatus = chip.dataset.val;
      curPage = 1;
      applyFilter();
    });
  });
  document.getElementById("users-search")?.addEventListener("input", () => { curPage=1; applyFilter(); });
}

function applyFilter() {
  const q = (document.getElementById("users-search")?.value||"").toLowerCase().trim();
  let list = allUsers;
  if (q) list = list.filter(u => (u.email||"").toLowerCase().includes(q) || (u.uid||"").toLowerCase().includes(q) || (u.username||"").toLowerCase().includes(q));
  if (filterRole !== "ALL") list = list.filter(u => (u.role||"user") === filterRole);
  if (filterStatus === "active") list = list.filter(u => !u.banned);
  if (filterStatus === "banned") list = list.filter(u => u.banned);
  renderPage(list);
}

function renderPage(list) {
  const wrap = document.getElementById("users-list");
  const paged = list.slice((curPage-1)*PAGE, curPage*PAGE);
  if (!paged.length) {
    wrap.innerHTML = `<div class="empty"><div class="empty-ico"><svg viewBox="0 0 24 24" fill="none" stroke-width="1.5"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/></svg></div><div class="empty-txt">NO USERS FOUND</div></div>`;
    buildPages("users-pages",0,0,()=>{});
    return;
  }
  wrap.innerHTML = paged.map(u => {
    const role      = u.role||"user";
    const isBanned  = u.banned;
    const initials  = ((u.username||u.email||"?")[0]).toUpperCase();
    const avatarBg  = role==="admin" ? "linear-gradient(135deg,var(--acid),#00ff88)" : isBanned ? "linear-gradient(135deg,var(--red),#ff6b6b)" : "linear-gradient(135deg,#333,#555)";
    const archColor = {VISIONARY:"var(--acid)",ARCHITECT:"var(--blue)",REBEL:"var(--red)",GUARDIAN:"var(--orange)",ALCHEMIST:"var(--purple)"}[(u.archetype||"").toUpperCase()] || "var(--silver)";
    return `
      <div class="user-card${isBanned?' banned':''}${role==='admin'?' admin-user':''}" onclick="window._openUser('${u.uid}')">
        <div class="u-avatar" style="background:${avatarBg}">
          ${u.profileImageUrl ? `<img src="${u.profileImageUrl}" alt=""/>` : `<span class="u-avatar-txt">${initials}</span>`}
        </div>
        <div class="u-info">
          <div class="u-name">${u.username||u.email||u.uid}</div>
          <div class="u-email">${u.email||"No email"}</div>
          <div class="u-tags">
            ${badgeStatus(role)}
            ${isBanned ? badgeStatus("banned") : ""}
            ${u.archetype ? `<span class="badge" style="background:${archColor}18;color:${archColor};border:1px solid ${archColor}35">${u.archetype}</span>` : ""}
          </div>
        </div>
      </div>`;
  }).join("");
  buildPages("users-pages", Math.ceil(list.length/PAGE), curPage, p => { curPage=p; applyFilter(); });
}

function setupUserModal() {
  window._openUser = uid => {
    const u = allUsers.find(x => x.uid === uid);
    if (!u) return;
    const body = document.getElementById("user-detail-body");
    body.innerHTML = `
      <div class="ud-hero" style="display:flex;align-items:center;gap:16px;flex-wrap:wrap">
        <div class="ud-avatar-lg" style="background:${u.role==='admin'?'linear-gradient(135deg,var(--acid),#00ff88)':'linear-gradient(135deg,#333,#555)'}">
          ${u.profileImageUrl ? `<img src="${u.profileImageUrl}" alt=""/>` : `<span style="font-family:'Oswald',sans-serif;font-size:24px;font-weight:700;color:#000">${((u.username||u.email||"?")[0]).toUpperCase()}</span>`}
        </div>
        <div>
          <div style="font-size:17px;font-weight:600;margin-bottom:4px">${u.username||u.email||u.uid}</div>
          <div style="display:flex;gap:6px">
            ${badgeStatus(u.role||"user")}
            ${u.banned ? badgeStatus("banned") : badgeStatus("active")}
            ${u.archetype ? `<span class="badge b-acid">${u.archetype}</span>` : ""}
          </div>
        </div>
      </div>
      <div style="padding:14px 0">
        ${udRow("UID", u.uid||"—")}
        ${udRow("EMAIL", u.email||"—")}
        ${udRow("USERNAME", u.username||"—")}
        ${udRow("ARCHETYPE", u.archetype||"—")}
        ${udRow("JOINED", fmtDate(u.createdAt))}
        ${udRow("SKIN TYPE", u.skinType||"—")}
        ${u.bodyData ? udRow("BODY DATA", typeof u.bodyData === "object" ? JSON.stringify(u.bodyData) : u.bodyData) : ""}
        ${u.fcmToken ? udRow("FCM", (u.fcmToken||"").slice(0,24)+"…") : ""}
      </div>`;

    // Set buttons
    const banBtn  = document.getElementById("ud-ban-btn");
    const roleBtn = document.getElementById("ud-role-btn");
    if (u.banned) { banBtn.textContent = "UNBAN"; banBtn.className = "btn btn-ghost"; }
    else          { banBtn.textContent = "BAN USER"; banBtn.className = "btn btn-danger"; }
    if (u.role === "admin") { roleBtn.textContent = "DEMOTE"; roleBtn.className = "btn btn-orange"; }
    else                    { roleBtn.textContent = "PROMOTE ADMIN"; roleBtn.className = "btn btn-acid"; }

    banBtn.onclick = async () => {
      const newBan = !u.banned;
      try {
        await updateDoc(doc(db, "users", uid), { banned: newBan });
        toast(`// USER ${newBan?"BANNED":"UNBANNED"}`, newBan?"err":"ok");
        addNotification(`User ${(u.email||u.uid).slice(0,20)} ${newBan?"banned":"unbanned"}`, "user", "user");
        closeModal("modal-user");
      } catch (e) { toast(`// ${e.message}`, "err"); }
    };
    roleBtn.onclick = async () => {
      const newRole = u.role === "admin" ? "user" : "admin";
      try {
        await updateDoc(doc(db, "users", uid), { role: newRole });
        toast(`// ROLE → ${newRole.toUpperCase()}`, "ok");
        addNotification(`User ${(u.email||u.uid).slice(0,20)} → ${newRole.toUpperCase()}`, "user", "user");
        closeModal("modal-user");
      } catch (e) { toast(`// ${e.message}`, "err"); }
    };

    openModal("modal-user");
  };
}

function udRow(lbl, val) {
  return `<div class="ud-data-row"><span class="ud-data-lbl">${lbl}</span><span class="ud-data-val">${val}</span></div>`;
}
