// ═══════════════════════════════════════════════════════════════
// BRANDS v3 — Realtime commission rates + click stats + Notifs
// Mirrors Android BrandsTab
// ═══════════════════════════════════════════════════════════════
import { db } from "./firebase-config.js?v=3";
import { collection, onSnapshot, doc, setDoc } from "https://www.gstatic.com/firebasejs/10.12.2/firebase-firestore.js";
import { toast, fmtVnd, openModal, closeModal, cmdLog, addNotification } from "./dashboard.js?v=3";

let brandsData   = {};  // {vendor: {rate, products, clicks, commission}}
let allArtifacts = [];
let allClicks    = [];

export function initBrands() {
  // Parallel realtime listeners
  onSnapshot(collection(db,"artifacts"), snap => {
    allArtifacts = snap.docs.map(d => ({ docId:d.id, ...d.data() }));
    recompute();
  });
  onSnapshot(collection(db,"clicks"), snap => {
    allClicks = snap.docs.map(d => d.data());
    recompute();
  });
  onSnapshot(collection(db,"brandRates"), snap => {
    brandsData = {};
    snap.docs.forEach(d => { brandsData[d.id] = { ...d.data() }; });
    recompute();
  });

  setupBrandModal();
}

function recompute() {
  // Build summary from artifacts
  const summary = {};
  allArtifacts.filter(a => !a.isDirectSale).forEach(a => {
    const v = (a.vendor||"unknown").toLowerCase();
    if (!summary[v]) summary[v] = { vendor:v, products:0, clicks:0, commission:0, rate: (a.commissionRate||0.08) };
    summary[v].products++;
    if (brandsData[v]?.rate !== undefined) summary[v].rate = brandsData[v].rate;
  });

  allClicks.forEach(c => {
    const v = (c.vendor||"unknown").toLowerCase();
    if (!summary[v]) summary[v] = { vendor:v, products:0, clicks:0, commission:0, rate:0.08 };
    summary[v].clicks++;
    summary[v].commission += Number(c.commissionEarned)||0;
  });

  const list = Object.values(summary).sort((a,b) => b.commission - a.commission);
  const maxComm = Math.max(1, ...list.map(b => b.commission));
  renderBrands(list, maxComm);
}

function renderBrands(list, maxComm) {
  const wrap = document.getElementById("brands-list");
  if (!wrap) return;
  if (!list.length) {
    wrap.innerHTML = `<div class="empty"><div class="empty-ico"><svg viewBox="0 0 24 24" fill="none" stroke-width="1.5"><line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg></div><div class="empty-txt">NO BRANDS YET</div></div>`;
    return;
  }

  const BRAND_ICONS = {
    nike:"#00ff88", puma:"#FF8C00", balenciaga:"#BF00FF", rick_owens:"#FF0040",
    adidas:"#C0FF00", converse:"#00BFFF", new_balance:"#ff6b35", rinnsan:"#C0FF00"
  };

  wrap.innerHTML = list.map(b => {
    const iconColor = BRAND_ICONS[(b.vendor||"").replace(" ","_")] || "var(--acid)";
    const pct = (b.commission/maxComm)*100;
    return `
      <div class="brand-card">
        <div class="brand-ico-wrap" style="background:${iconColor}18;border:1px solid ${iconColor}30">
          <svg viewBox="0 0 24 24" fill="none" stroke="${iconColor}" stroke-width="1.8" stroke-linecap="round">
            <path d="M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z"/>
            <line x1="7" y1="7" x2="7.01" y2="7"/>
          </svg>
        </div>
        <div class="brand-info">
          <div class="brand-name">${(b.vendor||"—").toUpperCase()}</div>
          <div class="brand-stat-txt">${b.products} artifacts · ${b.clicks} clicks · ${fmtVnd(b.commission)} earned</div>
          <div class="progress-bar" style="margin-top:8px">
            <div class="progress-fill" style="width:${pct.toFixed(1)}%;background:${iconColor}"></div>
          </div>
        </div>
        <div style="text-align:right">
          <div class="brand-rate-big" style="color:${iconColor}">${((b.rate||0)*100).toFixed(0)}%</div>
          <div style="font-family:'Space Mono',monospace;font-size:7.5px;color:var(--silver)">COMMISSION</div>
          <button class="btn btn-ghost btn-xs" style="margin-top:8px" onclick="window._editBrand('${b.vendor}',${b.rate||0.08})">EDIT RATE</button>
        </div>
      </div>`;
  }).join("");
}

function setupBrandModal() {
  window._editBrand = (vendor, rate) => {
    document.getElementById("brand-vendor-h").value  = vendor;
    document.getElementById("brand-name-disp").value = vendor.toUpperCase();
    document.getElementById("brand-rate-in").value   = rate;
    openModal("modal-brand");
  };

  document.getElementById("save-brand-btn")?.addEventListener("click", async () => {
    const vendor = document.getElementById("brand-vendor-h").value;
    const rate   = parseFloat(document.getElementById("brand-rate-in").value);
    if (!vendor || isNaN(rate)) { toast("// INVALID RATE", "err"); return; }
    const btn = document.getElementById("save-brand-btn");
    btn.disabled = true; btn.textContent = "// SAVING...";
    try {
      await setDoc(doc(db,"brandRates",vendor), { rate, vendor }, { merge:true });
      toast(`// ${vendor.toUpperCase()} RATE → ${(rate*100).toFixed(0)}%`, "ok");
      addNotification(`${vendor.toUpperCase()} commission rate updated to ${(rate*100).toFixed(0)}%`, "info", "bell");
      closeModal("modal-brand");
      cmdLog("brands-console", `// RATE UPDATED: ${vendor.toUpperCase()} = ${(rate*100).toFixed(0)}%`, true);
    } catch (e) { toast(`// ERROR: ${e.message}`, "err"); }
    btn.disabled = false; btn.textContent = "SAVE RATE";
  });
}
