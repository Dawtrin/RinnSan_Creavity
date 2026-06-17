// ═══════════════════════════════════════════════════════════════
// ORDERS v3 — Print Invoice, Export CSV, Timeline, Smooth Data
// ═══════════════════════════════════════════════════════════════
import { db } from "./firebase-config.js?v=3";
import { collection, query, orderBy, onSnapshot, doc, updateDoc } from "https://www.gstatic.com/firebasejs/10.12.2/firebase-firestore.js";
import { toast, fmtVnd, fmtDate, badgeStatus, openModal, closeModal, buildPages, addNotification } from "./dashboard.js?v=3";

let allOrders = [], curPage = 1, selOrderId = null;
const PAGE = 15;
let prevCount = 0;

export function initOrders() {
  const q = query(collection(db, "orders"), orderBy("timestamp", "desc"));
  onSnapshot(q, snap => {
    const newOrders = snap.docs.map(d => ({ docId:d.id, ...d.data() }));
    // Notify on new orders
    if (prevCount > 0 && newOrders.length > prevCount) {
      const diff = newOrders.length - prevCount;
      addNotification(`${diff} new order${diff>1?'s':''} received!`, "order", "order");
      toast(`// ${diff} NEW ORDER${diff>1?'S':''}`, "ok");
    }
    prevCount = newOrders.length;
    allOrders = newOrders;
    document.getElementById("orders-meta").textContent = `// ${allOrders.length} ORDERS`;
    applyFilter();
  });
  document.getElementById("orders-filter")?.addEventListener("change", () => { curPage=1; applyFilter(); });
  document.getElementById("orders-search")?.addEventListener("input", () => { curPage=1; applyFilter(); });
  document.getElementById("export-csv-btn")?.addEventListener("click", exportCSV);
  setupOrderModal();
}

function applyFilter() {
  const f = document.getElementById("orders-filter")?.value || "";
  const q = (document.getElementById("orders-search")?.value || "").toLowerCase().trim();

  let list = allOrders;
  if (f) {
    list = list.filter(o => (o.status||"").toLowerCase() === f);
  }
  if (q) {
    list = list.filter(o =>
      (o.docId||"").toLowerCase().includes(q) ||
      (o.userId||"").toLowerCase().includes(q) ||
      (o.shippingAddress?.name||"").toLowerCase().includes(q) ||
      (o.shippingAddress?.email||"").toLowerCase().includes(q)
    );
  }
  renderPage(list);
}

function renderPage(list) {
  const tbody = document.getElementById("orders-tbody");
  const paged = list.slice((curPage-1)*PAGE, curPage*PAGE);
  tbody.innerHTML = "";
  if (!paged.length) {
    tbody.innerHTML = `<tr><td colspan="7" style="text-align:center;padding:32px;color:var(--silver)">NO ORDERS FOUND</td></tr>`;
  } else {
    paged.forEach(o => {
      const tr = document.createElement("tr");
      tr.style.cursor = "pointer";
      tr.innerHTML = `
        <td class="td-mono">#${(o.docId||"").slice(-8).toUpperCase()}</td>
        <td class="td-mono">${(o.userId||"—").slice(0,14)}…</td>
        <td class="td-acid">${fmtVnd(o.totalAmount)}</td>
        <td class="td-mono">${(o.paymentMethod||"—").toUpperCase()}</td>
        <td>${badgeStatus(o.status)}</td>
        <td class="td-mono" style="font-size:10.5px">${fmtDate(o.timestamp)}</td>
        <td><button class="btn btn-ghost btn-xs" onclick="window._openOrder('${o.docId}')">DETAIL</button></td>`;
      tbody.appendChild(tr);
    });
  }
  buildPages("orders-pages", Math.ceil(list.length/PAGE), curPage, p => { curPage=p; applyFilter(); });
}

// ─── Export CSV ─────────────────────────────────────────────────
function exportCSV() {
  if (!allOrders.length) { toast("// NO DATA TO EXPORT", "err"); return; }
  const headers = ["Order ID","User ID","Amount","Payment","Status","Date","Items"];
  const rows = allOrders.map(o => [
    o.docId || "",
    o.userId || "",
    o.totalAmount || 0,
    o.paymentMethod || "",
    o.status || "",
    fmtDate(o.timestamp),
    (o.itemDetails||o.items||[]).length
  ]);
  const csv = [headers.join(","), ...rows.map(r => r.map(v => `"${v}"`).join(","))].join("\n");
  const blob = new Blob([csv], { type: "text/csv;charset=utf-8;" });
  const url = URL.createObjectURL(blob);
  const a = document.createElement("a");
  a.href = url;
  a.download = `rinnsan_orders_${new Date().toISOString().slice(0,10)}.csv`;
  a.click();
  URL.revokeObjectURL(url);
  toast("// CSV EXPORTED", "ok");
  addNotification("Orders exported to CSV", "info", "bell");
}

// ─── Order Modal ───────────────────────────────────────────────
function setupOrderModal() {
  window._openOrder = docId => {
    const o = allOrders.find(x => x.docId === docId);
    if (!o) return;
    selOrderId = docId;
    const body = document.getElementById("order-detail-body");

    // Order timeline
    const status = (o.status||"pending").toLowerCase();
    const steps = ["pending","paid","delivered"];
    const currentIdx = steps.indexOf(status);
    const isCancelled = status === "cancelled";
    const timelineHTML = `
      <div class="order-timeline">
        ${steps.map((s, i) => {
          let dotClass = "pending";
          if (isCancelled) dotClass = "pending";
          else if (i < currentIdx) dotClass = "done";
          else if (i === currentIdx) dotClass = "current";
          return `<div class="timeline-step">
            <div class="timeline-dot ${dotClass}">${dotClass === "done" ? "✓" : dotClass === "current" ? "●" : "○"}</div>
            <div class="timeline-info">
              <div class="timeline-lbl" style="color:${dotClass==='done'?'var(--green)':dotClass==='current'?'var(--acid)':'var(--silver)'}">${s.toUpperCase()}</div>
              ${dotClass !== "pending" ? `<div class="timeline-time">${fmtDate(o.timestamp)}</div>` : ""}
            </div>
          </div>`;
        }).join("")}
        ${isCancelled ? `<div class="timeline-step">
          <div class="timeline-dot" style="border-color:var(--red);color:var(--red);background:rgba(255,0,64,.1)">✕</div>
          <div class="timeline-info">
            <div class="timeline-lbl" style="color:var(--red)">CANCELLED</div>
            <div class="timeline-time">${fmtDate(o.timestamp)}</div>
          </div>
        </div>` : ""}
      </div>`;

    // Items list
    const itemsHTML = (o.itemDetails||[]).length > 0
      ? (o.itemDetails||[]).map(it => `
        <div style="display:flex;justify-content:space-between;padding:10px 0;border-bottom:1px solid rgba(255,255,255,.04)">
          <div>
            <div style="font-size:13px">${it.title||it.artifactId||"—"}</div>
            <div class="td-mono">Size: ${it.size||"—"} · Qty: ${it.quantity||1}</div>
          </div>
          <div class="td-acid">${fmtVnd(it.subtotal)}</div>
        </div>`).join("")
      : (o.items||[]).map(id=>`<div class="td-mono" style="padding:6px 0;border-bottom:1px solid rgba(255,255,255,.04)">${id}</div>`).join("") || `<div class="td-mono" style="padding:10px 0;color:var(--silver)">No item details</div>`;

    // Shipping address formatted
    const shipAddr = o.shippingAddress;
    let shippingHTML = "";
    if (shipAddr) {
      if (typeof shipAddr === "object") {
        shippingHTML = `<div class="form-lbl" style="margin-bottom:6px">SHIPPING ADDRESS</div>
          <div class="shipping-card">
            ${shipAddr.name ? `<div class="ship-row"><span class="ship-lbl">NAME</span><span class="ship-val">${shipAddr.name}</span></div>` : ""}
            ${shipAddr.phone ? `<div class="ship-row"><span class="ship-lbl">PHONE</span><span class="ship-val">${shipAddr.phone}</span></div>` : ""}
            ${shipAddr.address ? `<div class="ship-row"><span class="ship-lbl">ADDRESS</span><span class="ship-val">${shipAddr.address}</span></div>` : ""}
            ${shipAddr.city ? `<div class="ship-row"><span class="ship-lbl">CITY</span><span class="ship-val">${shipAddr.city}</span></div>` : ""}
            ${shipAddr.district ? `<div class="ship-row"><span class="ship-lbl">DISTRICT</span><span class="ship-val">${shipAddr.district}</span></div>` : ""}
            ${shipAddr.ward ? `<div class="ship-row"><span class="ship-lbl">WARD</span><span class="ship-val">${shipAddr.ward}</span></div>` : ""}
            ${shipAddr.note ? `<div class="ship-row"><span class="ship-lbl">NOTE</span><span class="ship-val">${shipAddr.note}</span></div>` : ""}
          </div>`;
      } else {
        shippingHTML = `<div class="form-lbl" style="margin-bottom:6px">SHIPPING</div>
          <div class="shipping-card"><div class="td-mono" style="font-size:11px;word-break:break-word">${shipAddr}</div></div>`;
      }
    }

    body.innerHTML = `
      <div class="invoice-print-hdr">
        <h2>RINNSAN CREAVITY</h2>
        <p>// INVOICE — ORDER #${(o.docId||"").slice(-8).toUpperCase()}</p>
      </div>
      ${timelineHTML}
      <div style="display:grid;grid-template-columns:1fr 1fr;gap:14px;margin-bottom:18px">
        ${dataRow("ORDER ID", "#"+(o.docId||"").slice(-8).toUpperCase())}
        ${dataRow("STATUS", badgeStatus(o.status), true)}
        ${dataRow("USER", (o.userId||"—").slice(0,18)+"…")}
        ${dataRow("PAYMENT", (o.paymentMethod||"—").toUpperCase())}
        ${dataRow("DATE", fmtDate(o.timestamp))}
        ${dataRow("TOTAL", `<span class="td-acid">${fmtVnd(o.totalAmount)}</span>`, true)}
      </div>
      <div class="form-lbl" style="margin-bottom:8px">ORDER ITEMS</div>
      <div style="background:rgba(255,255,255,.02);border-radius:6px;padding:6px 12px;margin-bottom:16px">${itemsHTML}</div>
      ${shippingHTML}`;
    document.getElementById("order-status-sel").value = (o.status||"pending").toLowerCase();
    openModal("modal-order");
  };

  // Print invoice
  document.getElementById("print-invoice-btn")?.addEventListener("click", () => {
    window.print();
  });

  document.getElementById("update-order-btn")?.addEventListener("click", async () => {
    if (!selOrderId) return;
    const newStatus = document.getElementById("order-status-sel").value;
    const btn = document.getElementById("update-order-btn");
    btn.disabled = true; btn.textContent = "// UPDATING...";
    try {
      await updateDoc(doc(db,"orders",selOrderId), { status:newStatus });
      toast(`// ORDER → ${newStatus.toUpperCase()}`, "ok");
      addNotification(`Order #${selOrderId.slice(-6).toUpperCase()} → ${newStatus.toUpperCase()}`, "order", "order");
      closeModal("modal-order");
    } catch (e) { toast(`// ERROR: ${e.message}`, "err"); }
    btn.disabled = false; btn.textContent = "UPDATE STATUS";
  });
}

function dataRow(lbl, val, isHtml=false) {
  return `<div><div class="form-lbl">${lbl}</div>${isHtml?val:`<div class="td-mono" style="font-size:10.5px">${val}</div>`}</div>`;
}
