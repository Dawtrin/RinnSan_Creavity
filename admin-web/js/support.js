// ═══════════════════════════════════════════════════════════════
// SUPPORT v3 — Chat-like Thread UI, Canned Responses, Smooth
// ═══════════════════════════════════════════════════════════════
import { db } from "./firebase-config.js?v=3";
import { collection, query, orderBy, onSnapshot, doc, updateDoc } from "https://www.gstatic.com/firebasejs/10.12.2/firebase-firestore.js";
import { toast, fmtDate, badgeStatus, openModal, closeModal, buildPages, setPill, addNotification } from "./dashboard.js?v=3";

let allTickets = [], curPage = 1, selTicketId = null;
const PAGE = 10;
let prevCount = 0;

const CANNED_RESPONSES = [
  "Cảm ơn bạn đã liên hệ! Chúng tôi sẽ xử lý trong 24h.",
  "Đơn hàng đã được xác nhận. Vui lòng chờ giao hàng.",
  "Chúng tôi đã hoàn tiền. Vui lòng kiểm tra tài khoản.",
  "Sản phẩm hết hàng. Bạn có muốn đổi size/color khác?",
  "Đã cập nhật thông tin. Cảm ơn bạn!",
];

export function initSupport() {
  const q = query(collection(db,"contacts"), orderBy("timestamp","desc"));
  onSnapshot(q, snap => {
    const newTickets = snap.docs.map(d => ({ docId:d.id, ...d.data() }));
    const openCount = newTickets.filter(t => ["new","open"].includes((t.status||"new").toLowerCase())).length;
    // Notify on new tickets
    if (prevCount > 0 && newTickets.length > prevCount) {
      addNotification(`New support ticket received!`, "ticket", "ticket");
    }
    prevCount = newTickets.length;
    allTickets = newTickets;
    document.getElementById("support-meta").textContent = `// ${allTickets.length} TOTAL · ${openCount} OPEN`;
    setPill("pill-support", openCount);
    applyFilter();
  });
  document.getElementById("support-filter")?.addEventListener("change", () => { curPage=1; applyFilter(); });
  setupTicketModal();
}

function applyFilter() {
  const f = document.getElementById("support-filter")?.value || "";
  let list = allTickets;
  if (f === "new")      list = allTickets.filter(t => { const s=(t.status||"new").toLowerCase(); return s==="new"||s==="open"; });
  else if (f === "resolved") list = allTickets.filter(t => (t.status||"new").toLowerCase()==="resolved");
  renderPage(list);
}

function renderPage(list) {
  const paged = list.slice((curPage-1)*PAGE, curPage*PAGE);
  const wrap = document.getElementById("tickets-list");
  wrap.innerHTML = "";
  if (!paged.length) {
    wrap.innerHTML = `<div class="empty"><div class="empty-ico"><svg viewBox="0 0 24 24" fill="none" stroke-width="1.5"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg></div><div class="empty-txt">NO TICKETS FOUND</div></div>`;
    return;
  }
  paged.forEach(t => {
    const status  = (t.status||"new").toLowerCase();
    const isNew   = status === "new" || status === "open";
    const card    = document.createElement("div");
    card.className = `ticket-card t-${isNew?"new":"resolved"}`;
    card.innerHTML = `
      <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:5px">
        <div class="ticket-subj">${t.title||"(No subject)"}</div>
        ${badgeStatus(status)}
      </div>
      <div class="ticket-from">
        👤 ${t.userEmail||t.userId?.slice(0,18)||"Unknown"} · ${fmtDate(t.timestamp)}
      </div>
      <div class="ticket-preview">${t.message||""}</div>
      ${t.adminReply ? `<div class="ticket-reply-prev"><strong style="color:var(--acid);font-size:9px">ADMIN REPLY:</strong><div style="margin-top:4px;font-size:12px">${t.adminReply}</div></div>` : ""}
      <div class="ticket-acts">
        <button class="btn btn-ghost btn-sm" onclick="window._openTicket('${t.docId}')">
          <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
          REPLY / DETAIL
        </button>
        ${isNew ? `<button class="btn btn-danger btn-sm" onclick="window._closeTicket('${t.docId}')">
          <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="20 6 9 17 4 12"/></svg>
          CLOSE
        </button>` : ""}
      </div>`;
    wrap.appendChild(card);
  });
  buildPages("support-pages", Math.ceil(list.length/PAGE), curPage, p => { curPage=p; applyFilter(); });
}

function setupTicketModal() {
  window._openTicket = docId => {
    const t = allTickets.find(x => x.docId === docId);
    if (!t) return;
    selTicketId = docId;
    const status = (t.status||"new").toLowerCase();
    const body   = document.getElementById("ticket-detail-body");

    // Build chat thread
    const messages = [];
    // User message
    messages.push({ sender:"user", text: t.message || "", time: t.timestamp, name: t.userEmail || t.userId?.slice(0,18) || "User" });
    // Admin reply (if exists)
    if (t.adminReply) {
      messages.push({ sender:"admin", text: t.adminReply, time: null, name: "Admin" });
    }

    const chatHTML = messages.map(m => `
      <div class="chat-bubble ${m.sender}">
        <div class="chat-sender">${m.sender === "user" ? "👤 " : "🛡️ "}${m.name}</div>
        <div>${m.text}</div>
        ${m.time ? `<div class="chat-time">${fmtDate(m.time)}</div>` : ""}
      </div>`).join("");

    // Canned responses
    const cannedHTML = `
      <div class="canned-responses">
        ${CANNED_RESPONSES.map((r, i) => `<button class="canned-btn" onclick="document.getElementById('ticket-reply-txt').value='${r.replace(/'/g,"\\'")}'">📋 ${r.slice(0,30)}…</button>`).join("")}
      </div>`;

    body.innerHTML = `
      <div style="display:grid;grid-template-columns:1fr 1fr;gap:12px;margin-bottom:18px">
        <div><div class="form-lbl">TICKET ID</div><div class="td-mono">${docId?.slice(0,14)}…</div></div>
        <div><div class="form-lbl">STATUS</div>${badgeStatus(status)}</div>
        <div><div class="form-lbl">FROM</div><div class="td-mono">${t.userEmail||t.userId?.slice(0,20)||"—"}</div></div>
        <div><div class="form-lbl">DATE</div><div class="td-mono">${fmtDate(t.timestamp)}</div></div>
      </div>
      <div class="form-lbl" style="margin-bottom:6px">SUBJECT</div>
      <div style="font-size:15px;font-weight:500;margin-bottom:14px">${t.title||"(No subject)"}</div>
      <div class="form-lbl" style="margin-bottom:6px">CONVERSATION</div>
      <div class="chat-thread" style="background:rgba(255,255,255,.02);border:1px solid rgba(255,255,255,.06);border-radius:var(--r-lg);padding:14px;margin-bottom:16px">
        ${chatHTML}
      </div>
      <div class="form-lbl" style="margin-bottom:6px">QUICK REPLIES</div>
      ${cannedHTML}
      <div class="form-lbl" style="margin-bottom:6px">ADMIN REPLY</div>
      <textarea class="form-ctrl" id="ticket-reply-txt" rows="4" placeholder="Type your reply...">${t.adminReply||""}</textarea>`;
    openModal("modal-ticket");
  };

  window._closeTicket = async docId => {
    try {
      await updateDoc(doc(db,"contacts",docId), { status:"resolved" });
      toast("// TICKET CLOSED", "ok");
      addNotification("Ticket closed successfully", "ticket", "ticket");
    } catch (e) { toast(`// ERROR: ${e.message}`, "err"); }
  };

  document.getElementById("close-ticket-btn")?.addEventListener("click", async () => {
    if (!selTicketId) return;
    await window._closeTicket(selTicketId);
    closeModal("modal-ticket");
  });

  document.getElementById("send-reply-btn")?.addEventListener("click", async () => {
    if (!selTicketId) return;
    const reply = document.getElementById("ticket-reply-txt")?.value?.trim();
    if (!reply) { toast("// REPLY CANNOT BE EMPTY", "err"); return; }
    const btn = document.getElementById("send-reply-btn");
    btn.disabled = true; btn.textContent = "// SENDING...";
    try {
      await updateDoc(doc(db,"contacts",selTicketId), { adminReply:reply, status:"resolved" });
      toast("// REPLY SENT & TICKET CLOSED", "ok");
      addNotification("Reply sent to support ticket", "ticket", "ticket");
      closeModal("modal-ticket");
    } catch (e) { toast(`// ERROR: ${e.message}`, "err"); }
    btn.disabled = false; btn.textContent = "SEND REPLY";
  });
}
