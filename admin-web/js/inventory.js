// ═══════════════════════════════════════════════════════════════
// INVENTORY v3 — 2 sub-tabs (AFFILIATE / DIRECT SALE)
// Matches Android InventoryTab + Notifications
// ═══════════════════════════════════════════════════════════════
import { db, CLOUDINARY } from "./firebase-config.js?v=3";
import {
  collection, onSnapshot, addDoc, doc, updateDoc, deleteDoc, serverTimestamp
} from "https://www.gstatic.com/firebasejs/10.12.2/firebase-firestore.js";
import { toast, fmtVnd, fmtNum, openModal, closeModal, buildPages, cmdLog, setPill, addNotification } from "./dashboard.js?v=3";

const SIZES = ["S","M","L","XL","XXL"];
const ARCHETYPES = ["GHOST","OPERATOR","GLITCH","NOMAD","ALL"];
const ARCH_COLORS = { GHOST:"#9B9BFF", OPERATOR:"#C0FF00", GLITCH:"#FF003C", NOMAD:"#FF8C00", ALL:"#8A8A8A" };

let allArtifacts = [];
let allClicks    = [];  // for stats
let curSub = "aff";    // "aff" | "dir"
let curPage = 1;
const PAGE = 16;

export function initInventory() {
  // Realtime artifacts
  onSnapshot(collection(db, "artifacts"), snap => {
    allArtifacts = snap.docs.map(d => ({ docId:d.id, ...d.data() }));
    renderAll();
    setPill("pill-inventory", allArtifacts.length);
  });

  // Realtime clicks (for stats)
  onSnapshot(collection(db, "clicks"), snap => {
    allClicks = snap.docs.map(d => d.data());
    renderStats();
  });

  setupSubTabs();
  setupAddModal();
  setupEditModal();
  setupSeeders();
  setupFilters();
  setupViewToggle();
}

// ─── Sub-tabs ──────────────────────────────────────────────────
function setupSubTabs() {
  document.getElementById("subtab-aff")?.addEventListener("click", () => setSubTab("aff"));
  document.getElementById("subtab-dir")?.addEventListener("click", () => setSubTab("dir"));
}

function setSubTab(sub) {
  curSub = sub;
  curPage = 1;

  const affBtn = document.getElementById("subtab-aff");
  const dirBtn = document.getElementById("subtab-dir");
  affBtn.className = "inv-subtab" + (sub==="aff" ? " active-aff" : "");
  dirBtn.className = "inv-subtab" + (sub==="dir" ? " active-dir" : "");

  // Description
  document.getElementById("inv-sub-desc").textContent = sub === "aff"
    ? "Sản phẩm dẫn link ngoài · Commission rate · Click tracking"
    : "Sản phẩm bán trong app · Tồn kho · Giá nội bộ";

  // Reseed button (direct only)
  document.getElementById("inv-reseed-wrap").style.display = sub === "dir" ? "" : "none";
  document.getElementById("inv-seeders-wrap").style.display = sub === "aff" ? "" : "none";

  renderStats();
  renderAll();
}

// ─── Stats bar ─────────────────────────────────────────────────
function renderStats() {
  const bar = document.getElementById("inv-stats-bar");
  if (!bar) return;
  const items = curSub === "aff"
    ? allArtifacts.filter(a => !a.isDirectSale)
    : allArtifacts.filter(a =>  a.isDirectSale);

  if (curSub === "aff") {
    const clickMap = {};
    allClicks.forEach(c => {
      const id = c.artifactId || "";
      if (!clickMap[id]) clickMap[id] = { clicks:0, comm:0 };
      clickMap[id].clicks++;
      clickMap[id].comm += Number(c.commissionEarned)||0;
    });
    const totalClicks = allClicks.length;
    const totalComm   = allClicks.reduce((s,c) => s+(Number(c.commissionEarned)||0), 0);
    bar.innerHTML = `
      ${kpiSmall("AFFILIATE PRODUCTS", items.length, "var(--acid)")}
      ${kpiSmall("TOTAL CLICKS", fmtNum(totalClicks), "var(--acid)")}
      ${kpiSmall("TOTAL EARNED", fmtVnd(totalComm), "var(--acid)")}`;
  } else {
    const inStock  = items.filter(a => (Number(a.stock)||0) > 5).length;
    const lowStock = items.filter(a => { const s = Number(a.stock)||0; return s>=1&&s<=5; }).length;
    const outStock = items.filter(a => (Number(a.stock)||0) === 0).length;
    bar.innerHTML = `
      ${kpiSmall("IN STOCK", inStock, "var(--green)")}
      ${kpiSmall("LOW STOCK", lowStock, "var(--orange)")}
      ${kpiSmall("OUT OF STOCK", outStock, "var(--red)")}`;
  }

  // Counts in sub-tabs
  const affCount = allArtifacts.filter(a => !a.isDirectSale).length;
  const dirCount = allArtifacts.filter(a =>  a.isDirectSale).length;
  document.getElementById("cnt-aff").textContent = affCount;
  document.getElementById("cnt-dir").textContent = dirCount;
}

function kpiSmall(lbl, val, color) {
  return `<div class="kpi-card" style="color:${color}">
    <div class="kpi-val" style="font-family:'Oswald',sans-serif;font-size:22px;color:${color}">${val}</div>
    <div class="kpi-lbl">${lbl}</div>
  </div>`;
}

// ─── Render ────────────────────────────────────────────────────
function renderAll() {
  const items = getFiltered();
  document.getElementById("inv-meta").textContent = `// ${items.length} OF ${allArtifacts.length} ARTIFACTS`;
  const isGrid = (document.getElementById("inv-view")?.value || "grid") === "grid";
  const paged  = items.slice((curPage-1)*PAGE, curPage*PAGE);
  isGrid ? renderGrid(paged) : renderTable(paged);
  buildPages("inv-pages", Math.ceil(items.length/PAGE), curPage, p => { curPage=p; renderAll(); });
}

function getFiltered() {
  const q = (document.getElementById("inv-search")?.value || "").toLowerCase();
  const v = document.getElementById("inv-filter-vendor")?.value || "";
  return allArtifacts.filter(a => {
    const matchSub = curSub === "aff" ? !a.isDirectSale : !!a.isDirectSale;
    const matchQ   = !q || (a.title||"").toLowerCase().includes(q) || (a.id||"").toLowerCase().includes(q);
    const matchV   = !v || (a.vendor||"").toLowerCase() === v.toLowerCase();
    return matchSub && matchQ && matchV;
  });
}

function renderGrid(items) {
  const grid = document.getElementById("art-grid");
  grid.style.display = "";
  document.getElementById("art-tbl-wrap").style.display = "none";
  grid.innerHTML = "";

  if (!items.length) {
    grid.innerHTML = `<div class="empty" style="grid-column:1/-1">
      <div class="empty-ico"><svg viewBox="0 0 24 24" fill="none" stroke-width="1.5"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/></svg></div>
      <div class="empty-txt">NO ARTIFACTS FOUND</div>
    </div>`;
    return;
  }

  items.forEach(a => {
    const stockNum = Number(a.stock)||0;
    const stockCls = stockNum === 0 ? "out" : stockNum <= 5 ? "low" : "ok";
    const stockLbl = stockNum === 0 ? "OUT OF STOCK" : stockNum <= 5 ? "LOW STOCK" : "IN STOCK";
    const clickStats = allClicks.filter(c => c.artifactId === a.id || c.artifactId === a.docId);
    const earned = clickStats.reduce((s,c)=>s+(Number(c.commissionEarned)||0),0);

    const card = document.createElement("div");
    card.className = "art-card";
    card.innerHTML = `
      ${a.imageUrl
        ? `<img class="art-img" src="${a.imageUrl}" alt="${a.title}" loading="lazy" onerror="this.parentElement.querySelector('.art-img-ph').style.display='flex';this.style.display='none'"/><div class="art-img-ph" style="display:none"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><rect x="3" y="3" width="18" height="18" rx="2"/><circle cx="8.5" cy="8.5" r="1.5"/><polyline points="21 15 16 10 5 21"/></svg></div>`
        : `<div class="art-img-ph"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><rect x="3" y="3" width="18" height="18" rx="2"/><circle cx="8.5" cy="8.5" r="1.5"/><polyline points="21 15 16 10 5 21"/></svg></div>`}
      <div class="art-badge ${a.isDirectSale?'dir':'aff'}">${a.isDirectSale?'DIRECT':'AFFILIATE'}</div>
      <div class="art-body">
        <div class="art-vendor">${(a.vendor||"").toUpperCase()}</div>
        <div class="art-name" title="${a.title||""}">${a.title||"Untitled"}</div>
        <div style="display:flex;justify-content:space-between;align-items:center;margin-top:5px">
          <div class="art-price">${a.price||"—"}</div>
          ${a.isDirectSale
            ? `<div class="art-stock ${stockCls}">${stockNum} · ${stockLbl}</div>`
            : `<div class="art-stock ok">${clickStats.length} clicks</div>`}
        </div>
        ${!a.isDirectSale && earned > 0 ? `<div style="font-family:'Space Mono',monospace;font-size:9px;color:var(--acid);margin-top:3px">+${fmtVnd(earned)} earned</div>` : ""}
        ${a.isDirectSale && a.sizes?.length > 0 ? renderSizeMini(a) : ""}
      </div>
      <div class="art-foot">
        <button class="btn btn-ghost btn-xs" style="flex:1" onclick="window._editArtifact('${a.docId}')">
          <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
          EDIT
        </button>
        <button class="btn btn-danger btn-xs" onclick="window._delArtifact('${a.docId}','${(a.title||"").replace(/'/g,"\\'")}')">
          <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a1 1 0 0 1 1-1h4a1 1 0 0 1 1 1v2"/></svg>
        </button>
      </div>`;
    grid.appendChild(card);
  });
}

function renderSizeMini(a) {
  const sizes = a.sizes || SIZES;
  return `<div style="display:flex;gap:3px;flex-wrap:wrap;margin-top:6px">${
    sizes.map(s => {
      const qty = Number(a.sizeStock?.[s]||0);
      const c = qty===0?"rgba(255,0,64,.7)":qty<=3?"rgba(255,140,0,.7)":"rgba(0,255,136,.7)";
      return `<span style="font-family:'Oswald',sans-serif;font-size:10px;padding:1px 5px;border:1px solid ${c};color:${c};border-radius:3px">${s}:${qty}</span>`;
    }).join("")
  }</div>`;
}

function renderTable(items) {
  document.getElementById("art-grid").style.display = "none";
  const wrap = document.getElementById("art-tbl-wrap");
  wrap.style.display = "";
  const tbody = document.getElementById("art-tbody");
  tbody.innerHTML = "";

  if (!items.length) {
    tbody.innerHTML = `<tr><td colspan="7" style="text-align:center;padding:32px;color:var(--silver)">NO ARTIFACTS</td></tr>`;
    return;
  }
  items.forEach(a => {
    const stockNum = Number(a.stock)||0;
    const stockCls = stockNum===0?"b-red":stockNum<=5?"b-orange":"b-green";
    const tr = document.createElement("tr");
    tr.innerHTML = `
      <td><div class="td-img">
        ${a.imageUrl
          ? `<img src="${a.imageUrl}" alt="${a.title}" onerror="this.nextElementSibling.style.display='flex';this.style.display='none'"/><div class="td-img-ph" style="display:none"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><rect x="3" y="3" width="18" height="18" rx="2"/></svg></div>`
          : `<div class="td-img-ph"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><rect x="3" y="3" width="18" height="18" rx="2"/></svg></div>`}
        <div>
          <div style="font-size:12.5px;font-weight:500">${a.title||"Untitled"}</div>
          <div class="td-mono">${a.id||a.docId?.slice(0,8)}</div>
        </div>
      </div></td>
      <td class="td-mono">${(a.vendor||"").toUpperCase()}</td>
      <td><span class="badge b-gray">${a.archetype||"—"}</span></td>
      <td class="td-acid">${a.price||"—"}</td>
      <td>${a.isDirectSale ? `<span class="badge ${stockCls}">${stockNum}</span>` : `<span class="badge b-acid">${(allClicks.filter(c=>c.artifactId===a.id||c.artifactId===a.docId).length)} clicks</span>`}</td>
      <td><span class="badge ${a.isDirectSale?'b-orange':'b-acid'}">${a.isDirectSale?'DIRECT':'AFFILIATE'}</span></td>
      <td><div style="display:flex;gap:5px">
        <button class="btn btn-ghost btn-xs" onclick="window._editArtifact('${a.docId}')">EDIT</button>
        <button class="btn btn-danger btn-xs" onclick="window._delArtifact('${a.docId}','${(a.title||"").replace(/'/g,"\\'")}')">DEL</button>
      </div></td>`;
    tbody.appendChild(tr);
  });
}

// ─── Filters ───────────────────────────────────────────────────
function setupFilters() {
  document.getElementById("inv-search")?.addEventListener("input", () => { curPage=1; renderAll(); });

  // Populate vendor filter on data load
  function updateVendorFilter() {
    const sel = document.getElementById("inv-filter-vendor");
    if (!sel) return;
    const cur = sel.value;
    const vendors = [...new Set(allArtifacts.map(a=>(a.vendor||"").toLowerCase()).filter(Boolean))].sort();
    sel.innerHTML = '<option value="">ALL BRANDS</option>' + vendors.map(v=>`<option value="${v}">${v.toUpperCase()}</option>`).join("");
    sel.value = cur;
  }
  // Called after snapshot; we must hook dynamically
  const origRenderAll = renderAll;
  document.getElementById("inv-filter-vendor")?.addEventListener("change", () => { curPage=1; renderAll(); });

  onSnapshot(collection(db, "artifacts"), () => updateVendorFilter());
}

// ─── View toggle ───────────────────────────────────────────────
function setupViewToggle() {
  document.getElementById("inv-view")?.addEventListener("change", () => { curPage=1; renderAll(); });
}

// ─── Add Artifact Modal ────────────────────────────────────────
let uploadedUrl = "";
let editUploadedUrl = "";
let modalType   = "aff"; // "aff" | "dir"
let archSel     = "GHOST";
let dirSizes    = { S:0, M:0, L:0, XL:0, XXL:0 };

function setupAddModal() {
  document.getElementById("add-art-btn")?.addEventListener("click", () => {
    resetAddForm();
    openModal("modal-artifact");
  });

  // Artifact type tabs
  document.querySelectorAll("[data-mtab]").forEach(btn => {
    btn.addEventListener("click", () => {
      modalType = btn.dataset.mtab;
      updateModalType();
    });
  });

  // Archetype chips
  buildArchChips();

  // Image file
  const zone    = document.getElementById("upload-zone");
  const fileIn  = document.getElementById("af-img-file");
  zone?.addEventListener("dragover", e => { e.preventDefault(); zone.classList.add("drag"); });
  zone?.addEventListener("dragleave", () => zone.classList.remove("drag"));
  zone?.addEventListener("drop", e => { e.preventDefault(); zone.classList.remove("drag"); if (e.dataTransfer.files[0]) doUpload(e.dataTransfer.files[0]); });
  fileIn?.addEventListener("change", () => { if (fileIn.files[0]) doUpload(fileIn.files[0]); });

  document.getElementById("save-art-btn")?.addEventListener("click", saveArtifact);
}

function buildArchChips() {
  const wrap = document.getElementById("arch-chips");
  if (!wrap) return;
  wrap.innerHTML = ARCHETYPES.map(a => `
    <button type="button" class="chip${a===archSel?" on":""}" data-arch="${a}" style="border-color:${ARCH_COLORS[a]||"rgba(255,255,255,.1)"};color:${a===archSel?(ARCH_COLORS[a]||"var(--acid)"):"var(--silver)"}">
      ${a}
    </button>`).join("");
  wrap.querySelectorAll("[data-arch]").forEach(b => {
    b.addEventListener("click", () => {
      archSel = b.dataset.arch;
      buildArchChips();
    });
  });
}

function buildSizeInputs() {
  const wrap = document.getElementById("af-sizes-grid");
  if (!wrap) return;
  wrap.innerHTML = SIZES.map(s => `
    <div class="size-row">
      <span class="size-tag" style="border-color:rgba(255,255,255,.2);color:var(--white)">${s}</span>
      <div class="stock-ctrl">
        <button type="button" class="stock-btn minus" onclick="window._adjSize('${s}',-1)">-</button>
        <div class="stock-val" id="sz-val-${s}">${dirSizes[s]||0}</div>
        <button type="button" class="stock-btn plus" onclick="window._adjSize('${s}',+1)">+</button>
        <button type="button" class="stock-btn plus" onclick="window._adjSize('${s}',+5)" style="margin-left:3px">+5</button>
      </div>
    </div>`).join("");

  window._adjSize = (size, delta) => {
    dirSizes[size] = Math.max(0, (dirSizes[size]||0) + delta);
    document.getElementById(`sz-val-${size}`).textContent = dirSizes[size];
    document.getElementById("af-stock").value = Object.values(dirSizes).reduce((s,v)=>s+v,0);
  };
}

function updateModalType() {
  const isAff = modalType === "aff";
  // Tab styles
  document.getElementById("m-tab-aff").style.background = isAff ? "rgba(192,255,0,.07)" : "none";
  document.getElementById("m-tab-aff").style.color = isAff ? "var(--acid)" : "var(--silver)";
  document.getElementById("m-tab-dir").style.background = !isAff ? "rgba(255,140,0,.07)" : "none";
  document.getElementById("m-tab-dir").style.color = !isAff ? "var(--orange)" : "var(--silver)";
  // Show/hide fields
  document.getElementById("af-comm-wrap").style.display = isAff ? "" : "none";
  document.getElementById("af-stock-wrap").style.display = !isAff ? "" : "none";
  document.getElementById("af-link-wrap").style.display  = isAff ? "" : "none";
  document.getElementById("af-sizes-wrap").style.display = !isAff ? "" : "none";
  if (!isAff) { dirSizes = Object.fromEntries(SIZES.map(s=>[s,0])); buildSizeInputs(); }
  if (modalType === "dir") document.getElementById("af-vendor").value = "RINNSAN LAB";
}

function resetAddForm() {
  modalType = "aff";
  archSel   = "GHOST";
  dirSizes  = Object.fromEntries(SIZES.map(s=>[s,0]));
  uploadedUrl = "";
  ["af-id","af-title","af-vendor","af-cat","af-price","af-int-price","af-comm","af-stock","af-link","af-img-url","af-desc"]
    .forEach(id => { const el = document.getElementById(id); if (el) el.value = ""; });
  document.getElementById("upload-preview").innerHTML = "";
  document.getElementById("upload-progress").style.display = "none";
  buildArchChips();
  updateModalType();
}

// ─── Cloudinary Upload ─────────────────────────────────────────
async function doUpload(file) {
  const preview  = document.getElementById("upload-preview");
  const progress = document.getElementById("upload-progress");
  const fill     = document.getElementById("upload-fill");
  preview.innerHTML = `<div style="width:64px;height:64px;border-radius:5px;background:var(--surface);display:flex;align-items:center;justify-content:center"><div class="spinner" style="width:20px;height:20px"></div></div>`;
  progress.style.display = "block";
  fill.style.width = "30%";
  try {
    const fd = new FormData();
    fd.append("file", file);
    fd.append("upload_preset", CLOUDINARY.uploadPreset);
    fd.append("folder", "admin_artifacts");
    fill.style.width = "60%";
    const resp = await fetch(CLOUDINARY.uploadUrl, { method:"POST", body:fd });
    const json = await resp.json();
    uploadedUrl = json.secure_url;
    document.getElementById("af-img-url").value = uploadedUrl;
    preview.innerHTML = `<img src="${uploadedUrl}" style="width:72px;height:72px;object-fit:cover;border-radius:5px;border:1px solid rgba(192,255,0,.3)"/>`;
    fill.style.width = "100%";
    toast("// IMAGE UPLOADED", "ok");
    setTimeout(() => { progress.style.display = "none"; fill.style.width = "0%"; }, 1200);
  } catch (e) {
    preview.innerHTML = `<span style="color:var(--red);font-size:10px">UPLOAD FAILED</span>`;
    progress.style.display = "none";
    toast("// UPLOAD ERROR", "err");
  }
}

// ─── Save New Artifact ─────────────────────────────────────────
async function saveArtifact() {
  const imageUrl = document.getElementById("af-img-url").value.trim() || uploadedUrl;
  const title    = document.getElementById("af-title").value.trim();
  if (!title) { toast("// TITLE REQUIRED", "err"); return; }

  const isAff = modalType === "aff";
  const commPct = parseFloat(document.getElementById("af-comm").value) || 8;
  const sizeStockMap = isAff ? {} : { ...dirSizes };
  const totalStock   = isAff ? 0 : Object.values(dirSizes).reduce((s,v)=>s+v,0);

  const data = {
    id:            (document.getElementById("af-id").value.trim()||"").toUpperCase(),
    title:         title.toUpperCase(),
    vendor:        (document.getElementById("af-vendor").value.trim()||"").toUpperCase(),
    archetype:     archSel,
    category:      (document.getElementById("af-cat").value.trim()||"").toUpperCase(),
    price:         document.getElementById("af-price").value.trim(),
    internalPrice: Number(document.getElementById("af-int-price").value)||0,
    imageUrl,
    affiliateLink: isAff ? document.getElementById("af-link").value.trim() : "",
    commissionRate: isAff ? commPct/100 : 0,
    isDirectSale:  !isAff,
    stock:         totalStock,
    sizes:         isAff ? [] : SIZES,
    sizeStock:     sizeStockMap,
    description:   document.getElementById("af-desc").value.trim(),
    createdAt:     serverTimestamp()
  };

  const btn = document.getElementById("save-art-btn");
  btn.disabled = true; btn.textContent = "// SAVING...";
  try {
    await addDoc(collection(db, "artifacts"), data);
    toast("// ARTIFACT ADDED", "ok");
    closeModal("modal-artifact");
    cmdLog("inv-console", `// ARTIFACT "${data.title}" ADDED`, true);
  } catch (e) { toast(`// SAVE ERROR: ${e.message}`, "err"); }
  btn.disabled = false; btn.textContent = "SAVE ARTIFACT";
}

// ─── Edit Artifact Modal ───────────────────────────────────────
async function doEditUpload(file) {
  const preview  = document.getElementById("edit-upload-preview");
  const progress = document.getElementById("edit-upload-progress");
  const fill     = document.getElementById("edit-upload-fill");
  preview.innerHTML = `<div style="width:64px;height:64px;border-radius:5px;background:var(--surface);display:flex;align-items:center;justify-content:center"><div class="spinner" style="width:20px;height:20px"></div></div>`;
  progress.style.display = "block";
  fill.style.width = "30%";
  try {
    const fd = new FormData();
    fd.append("file", file);
    fd.append("upload_preset", CLOUDINARY.uploadPreset);
    fd.append("folder", "admin_artifacts");
    fill.style.width = "60%";
    const resp = await fetch(CLOUDINARY.uploadUrl, { method:"POST", body:fd });
    const json = await resp.json();
    editUploadedUrl = json.secure_url;
    document.getElementById("edit-img-url").value = editUploadedUrl;
    preview.innerHTML = `<img src="${editUploadedUrl}" style="width:72px;height:72px;object-fit:cover;border-radius:5px;border:1px solid rgba(192,255,0,.3)"/>`;
    fill.style.width = "100%";
    toast("// IMAGE UPLOADED", "ok");
    setTimeout(() => { progress.style.display = "none"; fill.style.width = "0%"; }, 1200);
  } catch (e) {
    preview.innerHTML = `<span style="color:var(--red);font-size:10px">UPLOAD FAILED</span>`;
    progress.style.display = "none";
    toast("// UPLOAD ERROR", "err");
  }
}

function setupEditModal() {
  window._editArtifact = docId => {
    const a = allArtifacts.find(x => x.docId === docId);
    if (!a) return;
    document.getElementById("edit-docid").value = docId;
    document.getElementById("edit-type").value  = a.isDirectSale ? "dir" : "aff";
    document.getElementById("edit-title").value = a.title || "";
    document.getElementById("edit-vendor").value = a.vendor || "";
    document.getElementById("edit-cat").value = a.category || "";
    document.getElementById("edit-price").value = a.price || "";
    document.getElementById("edit-int-price").value = a.internalPrice || 0;
    document.getElementById("edit-comm").value  = ((a.commissionRate||0)*100).toFixed(1);
    document.getElementById("edit-stock").value = a.stock || 0;
    document.getElementById("edit-desc").value  = a.description || "";
    document.getElementById("edit-img-url").value = a.imageUrl || "";
    editUploadedUrl = a.imageUrl || "";

    const preview = document.getElementById("edit-upload-preview");
    if (preview) {
      if (a.imageUrl) {
        preview.innerHTML = `<img src="${a.imageUrl}" style="width:72px;height:72px;object-fit:cover;border-radius:5px;border:1px solid rgba(192,255,0,.3)"/>`;
      } else {
        preview.innerHTML = "";
      }
    }

    // Show correct fields
    const isDir = !!a.isDirectSale;
    document.getElementById("edit-comm-grp").style.display  = isDir ? "none" : "";
    document.getElementById("edit-stock-grp").style.display = isDir ? "none" : "";
    document.getElementById("edit-sizes-wrap").style.display = isDir && (a.sizes||[]).length > 0 ? "" : "none";

    if (isDir && (a.sizes||[]).length > 0) {
      buildEditSizes(a);
    }
    openModal("modal-edit-art");
  };

  // Drag & drop upload for edit
  const zone    = document.getElementById("edit-upload-zone");
  const fileIn  = document.getElementById("edit-img-file");
  zone?.addEventListener("dragover", e => { e.preventDefault(); zone.classList.add("drag"); });
  zone?.addEventListener("dragleave", () => zone.classList.remove("drag"));
  zone?.addEventListener("drop", e => { e.preventDefault(); zone.classList.remove("drag"); if (e.dataTransfer.files[0]) doEditUpload(e.dataTransfer.files[0]); });
  fileIn?.addEventListener("change", () => { if (fileIn.files[0]) doEditUpload(fileIn.files[0]); });

  document.getElementById("update-art-btn")?.addEventListener("click", updateArtifact);

  window._delArtifact = async (docId, title) => {
    if (!confirm(`// DELETE: ${title}?\nCannot be undone.`)) return;
    try {
      await deleteDoc(doc(db, "artifacts", docId));
      toast("// ARTIFACT DELETED", "ok");
    } catch (e) { toast(`// ERROR: ${e.message}`, "err"); }
  };
}

function buildEditSizes(a) {
  const list = document.getElementById("edit-sizes-list");
  if (!list) return;
  let editedSizes = { ...(a.sizeStock||{}) };

  function updateTotal() {
    const t = Object.values(editedSizes).reduce((s,v)=>s+(Number(v)||0),0);
    document.getElementById("edit-size-total").textContent = t;
    document.getElementById("edit-stock").value = t;
    list.querySelectorAll("[data-size-val]").forEach(el => {
      editedSizes[el.dataset.sizeVal] = Number(el.value)||0;
    });
  }

  list.innerHTML = (a.sizes||SIZES).map(s => {
    const qty = Number(a.sizeStock?.[s]||0);
    const col = qty===0?"var(--red)":qty<=3?"var(--orange)":"var(--green)";
    return `
      <div class="size-row">
        <span class="size-tag" style="border-color:${col};color:${col}">${s}</span>
        <div class="stock-ctrl">
          <button type="button" class="stock-btn minus" onclick="adjEditSize('${s}',-1)">-</button>
          <input type="number" data-size-val="${s}" class="stock-val" value="${qty}" min="0" style="width:48px;text-align:center" onchange="updateEditSizes()"/>
          <button type="button" class="stock-btn plus" onclick="adjEditSize('${s}',+1)">+</button>
          <button type="button" class="stock-btn plus" onclick="adjEditSize('${s}',+5)" style="margin-left:3px">+5</button>
        </div>
      </div>`;
  }).join("");

  window.adjEditSize = (size, delta) => {
    const inp = list.querySelector(`[data-size-val="${size}"]`);
    if (inp) { inp.value = Math.max(0, Number(inp.value)+delta); editedSizes[size] = Number(inp.value); updateTotal(); }
  };
  window.updateEditSizes = () => { list.querySelectorAll("[data-size-val]").forEach(el => { editedSizes[el.dataset.sizeVal]=Number(el.value)||0; }); updateTotal(); };
  window._editedSizes = editedSizes;
  updateTotal();
}

async function updateArtifact() {
  const docId  = document.getElementById("edit-docid").value;
  const isDir  = document.getElementById("edit-type").value === "dir";
  if (!docId) return;
  const btn = document.getElementById("update-art-btn");
  btn.disabled = true; btn.textContent = "// UPDATING...";
  try {
    const newStock = isDir && window._editedSizes
      ? Object.values(window._editedSizes).reduce((s,v)=>s+v,0)
      : Number(document.getElementById("edit-stock").value)||0;

    const imgUrl = document.getElementById("edit-img-url").value.trim() || editUploadedUrl;
    const updates = {
      title:          document.getElementById("edit-title").value.trim().toUpperCase(),
      vendor:         document.getElementById("edit-vendor").value.trim().toUpperCase(),
      category:       document.getElementById("edit-cat").value.trim().toUpperCase(),
      price:          document.getElementById("edit-price").value.trim(),
      internalPrice:  Number(document.getElementById("edit-int-price").value)||0,
      commissionRate: isDir ? undefined : (Number(document.getElementById("edit-comm").value)||8)/100,
      stock:          newStock,
      description:    document.getElementById("edit-desc").value.trim(),
      imageUrl:       imgUrl
    };
    if (isDir && window._editedSizes) updates.sizeStock = { ...window._editedSizes };
    // Clean undefined
    Object.keys(updates).forEach(k => updates[k] === undefined && delete updates[k]);

    await updateDoc(doc(db, "artifacts", docId), updates);
    toast("// ARTIFACT UPDATED", "ok");
    closeModal("modal-edit-art");
  } catch (e) { toast(`// ERROR: ${e.message}`, "err"); }
  btn.disabled = false; btn.textContent = "UPDATE";
}

// ─── SEEDERS ──────────────────────────────────────────────────
function setupSeeders() {
  const seeds = {
    "seed-nike":       { brand:"nike",       count:20 },
    "seed-puma":       { brand:"puma",       count:10 },
    "seed-balenciaga": { brand:"balenciaga", count:9  },
    "seed-rick":       { brand:"rick_owens", count:9  },
    "seed-rinnsan":    { brand:"rinnsan",    count:5, direct:true },
  };
  Object.entries(seeds).forEach(([id, cfg]) =>
    document.getElementById(id)?.addEventListener("click", () => runSeeder(id, cfg))
  );
  // Reseed btn
  document.getElementById("reseed-btn")?.addEventListener("click", () =>
    runSeeder("reseed-btn", { brand:"rinnsan", count:5, direct:true, reseed:true })
  );
}

const BRAND_IMGS = {
  nike:      "https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=400&q=80",
  puma:      "https://images.unsplash.com/photo-1608231387042-66d1773070a5?w=400&q=80",
  balenciaga:"https://images.unsplash.com/photo-1606107557195-0e29a4b5b4aa?w=400&q=80",
  rick_owens:"https://images.unsplash.com/photo-1548036328-c9fa89d128fa?w=400&q=80",
  rinnsan:   "https://images.unsplash.com/photo-1489987707025-afc232f7ea0f?w=400&q=80",
};

async function runSeeder(id, cfg) {
  const card = document.getElementById(id);
  const origHTML = card?.innerHTML;
  if (card) { card.innerHTML = `<div style="display:flex;justify-content:center;align-items:center;height:60px"><div class="spinner"></div></div>`; card.style.pointerEvents="none"; }
  cmdLog("inv-console", `// SEEDING ${cfg.brand.toUpperCase()}...`);
  try {
    const products = generateSeed(cfg.brand, cfg.count, cfg.direct||false);
    await Promise.all(products.map(p => addDoc(collection(db, "artifacts"), { ...p, createdAt:serverTimestamp() })));
    cmdLog("inv-console", `// ${products.length} ${cfg.brand.toUpperCase()} ARTIFACTS SEEDED`, true);
    toast(`// ${products.length} ${cfg.brand.toUpperCase()} SEEDED`, "ok");
  } catch (e) { toast("// SEED ERROR", "err"); cmdLog("inv-console", `// SEED ERROR: ${e.message}`); }
  if (card && origHTML) { card.innerHTML = origHTML; card.style.pointerEvents=""; }
}

function generateSeed(brand, count, isDirect) {
  const base = {
    nike:      { titles:["Air Max 270","Air Force 1","Dunk Low","Jordan 1","Pegasus 40","Vaporfly 3","Blazer Mid","React Element","SB Dunk","Infinity Run"], cats:["Sneakers","Giày"], price:{min:2800000,max:6500000}, comm:0.08, arch:["NOMAD","OPERATOR","ALL"] },
    puma:      { titles:["Suede Classic","RS-X","Future Rider","Cali Sport","Deviate Nitro","Velocity Nitro","Ultra Match","Smash v2","Graviton","Cell Phase"], cats:["Sneakers","Giày"], price:{min:1500000,max:3500000}, comm:0.10, arch:["NOMAD","ALL"] },
    balenciaga:{ titles:["Triple S","Speed Trainer","Track Runner","Phantom","Defender","BB Low","Runner","Pool","CROCS"], cats:["Sneakers","Phụ kiện"], price:{min:22000000,max:48000000}, comm:0.05, arch:["GHOST","GLITCH"] },
    rick_owens:{ titles:["DRKSHDW Ramones","Geobasket","Dunks","Larry Bozo","Turbowpn","Mega Bump","Abstract","Island","Minimal"], cats:["Sneakers","Áo khoác","Áo"], price:{min:30000000,max:80000000}, comm:0.04, arch:["GHOST","GLITCH"] },
    rinnsan:   { titles:["Operator Jacket","Ghost Hoodie","Void Tee","Signal Cargo","Glitch Windbreaker","NOMAD Pants","GHOST Shell","Techwear Set","Stealth Kit","Operator Vest"], cats:["Áo khoác","Áo","Quần"], price:{min:3500000,max:12000000}, comm:0.15, arch:["GHOST","OPERATOR","GLITCH","NOMAD"] },
  }[brand] || base.nike;
  const img = BRAND_IMGS[brand];
  const variants = ["Black","White","Volt","Shadow","Cobalt","Olive","Bone","Cream","Phantom","Core"];

  return Array.from({length:Math.min(count,base.titles.length)}, (_,i) => {
    const priceNum = Math.round((Math.random()*(base.price.max-base.price.min)+base.price.min)/1000)*1000;
    const archetype = base.arch[i % base.arch.length];
    const category  = base.cats[i % base.cats.length];
    const sizeStockMap = isDirect ? Object.fromEntries(SIZES.map(s=>[s,Math.floor(Math.random()*12)+2])) : {};
    const totalStock   = isDirect ? Object.values(sizeStockMap).reduce((s,v)=>s+v,0) : 0;
    return {
      id:            `${brand.replace("_","").slice(0,3).toUpperCase()}-${String(Date.now()+i).slice(-4)}`,
      title:         `${base.titles[i]} ${variants[i % variants.length]}`.toUpperCase(),
      vendor:        brand.toUpperCase(),
      archetype,
      category,
      price:         priceNum.toLocaleString("vi-VN") + "₫",
      internalPrice: priceNum,
      imageUrl:      img + `&sig=${i}`,
      affiliateLink: isDirect ? "" : `https://example.com/${brand}/${i}`,
      commissionRate:isDirect ? 0 : base.comm,
      isDirectSale:  isDirect,
      stock:         totalStock,
      sizes:         isDirect ? SIZES : [],
      sizeStock:     sizeStockMap,
      description:   `${base.titles[i]} — Premium ${brand.replace("_"," ")} quality.`
    };
  });
}
