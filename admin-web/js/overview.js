// ═══════════════════════════════════════════════════════════════
// OVERVIEW v3 — Smooth animated KPIs, Realtime data pulse,
// Matches Android OverviewTab design closely
// ═══════════════════════════════════════════════════════════════
import { db } from "./firebase-config.js?v=3";
import { collection, query, orderBy, limit, onSnapshot } from "https://www.gstatic.com/firebasejs/10.12.2/firebase-firestore.js";
import { toast, fmtVnd, fmtNum, timeAgo, badgeStatus, setPill, animateValue } from "./dashboard.js?v=3";

Chart.defaults.color = "#8A8A8A";
Chart.defaults.font.family = "'Space Mono', monospace";
Chart.defaults.font.size = 9;

const charts = {};
function destroyChart(id) { charts[id]?.destroy(); delete charts[id]; }
function getLast14() {
  const d = [];
  for (let i = 13; i >= 0; i--) {
    const dt = new Date();
    dt.setDate(dt.getDate() - i);
    d.push(dt.toLocaleDateString("en-GB",{day:"2-digit",month:"2-digit"}));
  }
  return d;
}
function dateKey(ts) {
  const d = new Date(typeof ts === "object" ? ts.toDate?.() || 0 : ts);
  return d.toLocaleDateString("en-GB",{day:"2-digit",month:"2-digit"});
}

function setKpi(id, val, removeSkel = true) {
  const el = document.getElementById(id);
  if (!el) return;
  animateValue(el, val);
  if (removeSkel) el.classList.remove("skel");
  el.style.minWidth = el.style.minHeight = "";
}

function getThemeColor() {
  const theme = document.documentElement.getAttribute("data-theme") || "";
  if (theme === "cyber-blue") return "#00BFFF";
  if (theme === "crimson") return "#FF003C";
  if (theme === "neon-purple") return "#BF00FF";
  return "#C0FF00"; // default Acid Neon
}

function renderTopSellers(orders, artifacts) {
  const listEl = document.getElementById("top-sellers-list");
  if (!listEl) return;

  const sales = {};
  const validOrders = orders.filter(o => (o.status || "pending").toLowerCase() !== "cancelled");

  validOrders.forEach(o => {
    const details = o.itemDetails || [];
    details.forEach(item => {
      const title = (item.title || item.artifactId || "Unknown Product").toUpperCase();
      const qty = Number(item.quantity) || 1;
      const subtotal = Number(item.subtotal) || 0;

      if (!sales[title]) {
        const match = artifacts.find(a => (a.title || "").toUpperCase() === title || (a.id || "").toUpperCase() === title);
        sales[title] = {
          title: title,
          quantity: 0,
          revenue: 0,
          imageUrl: match?.imageUrl || item.imageUrl || ""
        };
      }

      sales[title].quantity += qty;
      sales[title].revenue += subtotal;
    });
  });

  const sorted = Object.values(sales).sort((a, b) => b.quantity - a.quantity).slice(0, 5);

  if (!sorted.length) {
    listEl.innerHTML = '<div class="notif-empty">// NO SALES YET</div>';
    return;
  }

  listEl.innerHTML = sorted.map((s, idx) => {
    const medal = idx === 0 ? "👑" : idx === 1 ? "🥈" : idx === 2 ? "🥉" : `${idx + 1}.`;
    return `
      <div class="activity-row" style="padding:8px 10px; margin-bottom:0">
        ${s.imageUrl 
          ? `<img src="${s.imageUrl}" style="width:30px;height:30px;object-fit:cover;border-radius:4px" onerror="this.style.display='none'"/>` 
          : `<div style="width:30px;height:30px;background:var(--surface);border-radius:4px;display:flex;align-items:center;justify-content:center;font-size:10px">${medal}</div>`}
        <div class="activity-body" style="margin-left:${s.imageUrl ? '8px' : '0'}">
          <div class="activity-name" style="font-size:11.5px; font-weight:600; text-overflow:ellipsis; white-space:nowrap; overflow:hidden" title="${s.title}">${s.title}</div>
          <div class="activity-meta" style="font-size:8.5px; color:var(--silver)">
            <span style="color:var(--acid);font-weight:700">${s.quantity} sold</span> · ${s.revenue.toLocaleString("vi-VN")}₫
          </div>
        </div>
      </div>
    `;
  }).join("");
}

export function initOverview() {
  const state = {
    users: [], artifacts: [], orders: [], clicks: [], contacts: []
  };
  let rendered = false;

  function recompute() {
    const { users, artifacts, orders, clicks, contacts } = state;

    // Revenue / Commission
    const allRevenue = orders.filter(o => (o.status||"pending").toLowerCase() !== "cancelled")
                             .reduce((s, o) => s + (Number(o.totalAmount)||0), 0);
    const commission = clicks.reduce((s, c) => s + (Number(c.commissionEarned)||0), 0);
    const gross = allRevenue + commission;

    const pending   = orders.filter(o => (o.status||"").toLowerCase() === "pending").length;
    const delivered = orders.filter(o => (o.status||"").toLowerCase() === "delivered").length;
    const paid      = orders.filter(o => (o.status||"").toLowerCase() === "paid").length;
    const openTick  = contacts.filter(c => ["new","open"].includes((c.status||"new").toLowerCase())).length;

    // Animate KPIs
    document.getElementById("kpi-gross").textContent = fmtVnd(gross);
    document.getElementById("kpi-revenue").textContent = fmtVnd(allRevenue);
    document.getElementById("kpi-commission").textContent = fmtVnd(commission);
    setKpi("kpi-users", fmtNum(users.length));
    setKpi("kpi-artifacts", fmtNum(artifacts.length));
    setKpi("kpi-tickets", fmtNum(openTick));
    setKpi("kpi-orders", fmtNum(orders.length));
    setKpi("kpi-pending", fmtNum(pending));
    setKpi("kpi-delivered", fmtNum(delivered));
    setKpi("kpi-clicks", fmtNum(clicks.length));
    setKpi("kpi-paid", fmtNum(paid));

    // Pills
    setPill("pill-orders", pending);
    setPill("pill-support", openTick);
    setPill("pill-inventory", artifacts.length);

    renderCharts(clicks, orders, artifacts, state);
    renderTopSellers(orders, artifacts);
    if (!rendered) {
      rendered = true;
      renderLiveFeed(orders, clicks);
    }
  }

  // onSnapshot listeners — parallel, fast
  const unsubs = [
    onSnapshot(collection(db, "users"),    s => { state.users    = s.docs.map(d=>({id:d.id,...d.data()})); recompute(); }),
    onSnapshot(collection(db, "artifacts"),s => { state.artifacts= s.docs.map(d=>({id:d.id,...d.data()})); recompute(); }),
    onSnapshot(collection(db, "contacts"), s => { state.contacts = s.docs.map(d=>({id:d.id,...d.data()})); recompute(); }),
    onSnapshot(query(collection(db,"orders"), orderBy("timestamp","desc")),
      s => { state.orders = s.docs.map(d=>({docId:d.id,...d.data()})); recompute(); renderLiveFeed(state.orders, state.clicks); }),
    onSnapshot(query(collection(db,"clicks"), orderBy("timestamp","desc")),
      s => { state.clicks = s.docs.map(d=>({id:d.id,...d.data()})); recompute(); renderLiveFeed(state.orders, state.clicks); }),
  ];

  // Feed tab toggle
  document.getElementById("ft-orders")?.addEventListener("click", () => {
    document.getElementById("ft-orders").classList.add("active");
    document.getElementById("ft-clicks").classList.remove("active");
    document.getElementById("feed-orders").style.display = "";
    document.getElementById("feed-clicks").style.display = "none";
  });
  document.getElementById("ft-clicks")?.addEventListener("click", () => {
    document.getElementById("ft-clicks").classList.add("active");
    document.getElementById("ft-orders").classList.remove("active");
    document.getElementById("feed-clicks").style.display = "";
    document.getElementById("feed-orders").style.display = "none";
  });

  if (window._overviewThemeListener) {
    window.removeEventListener("theme-changed", window._overviewThemeListener);
  }
  window._overviewThemeListener = () => {
    if (state.clicks.length || state.orders.length) {
      renderCharts(state.clicks, state.orders, state.artifacts, state);
    }
  };
  window.addEventListener("theme-changed", window._overviewThemeListener);
}

// ─── Charts ────────────────────────────────────────────────────
function renderCharts(clicks, orders, artifacts, state) {
  const days14 = getLast14();
  const ACID = getThemeColor();
  const ACID_DIM = ACID + "38";
  const GREEN="#00FF88", RED="#FF0040", ORANGE="#FF8C00";

  // ── Commission bar chart (14 days)
  const commMap = {};
  clicks.forEach(c => {
    const k = dateKey(c.timestamp);
    commMap[k] = (commMap[k]||0) + (Number(c.commissionEarned)||0);
  });
  destroyChart("chart-commission");
  charts["chart-commission"] = new Chart(document.getElementById("chart-commission"), {
    type:"bar",
    data:{
      labels: days14,
      datasets:[{ data: days14.map(k=>commMap[k]||0),
        backgroundColor: ACID_DIM, borderColor:ACID, borderWidth:1, borderRadius:3,
        hoverBackgroundColor: ACID }]
    },
    options:{
      plugins:{legend:{display:false},tooltip:{callbacks:{label:v=>" "+v.raw.toLocaleString("vi-VN")+"₫"}}},
      scales:{
        x:{grid:{color:"rgba(255,255,255,.03)"},ticks:{maxRotation:0,maxTicksLimit:7}},
        y:{grid:{color:"rgba(255,255,255,.03)"},beginAtZero:true,ticks:{callback:v=>v>=1e6?(v/1e6).toFixed(1)+"M":v}}
      },animation:{duration:800,easing:"easeOutQuart"}
    }
  });

  // ── Revenue bar chart (14 days)
  const revMap = {};
  orders.filter(o=>(o.status||"pending").toLowerCase()!=="cancelled").forEach(o=>{
    const k = dateKey(o.timestamp);
    revMap[k] = (revMap[k]||0) + (Number(o.totalAmount)||0);
  });
  destroyChart("chart-revenue");
  charts["chart-revenue"] = new Chart(document.getElementById("chart-revenue"), {
    type:"line",
    data:{
      labels: days14,
      datasets:[{ data: days14.map(k=>revMap[k]||0), borderColor:GREEN,
        backgroundColor:"rgba(0,255,136,.07)", borderWidth:2, fill:true, tension:.4,
        pointBackgroundColor:GREEN, pointRadius:3, pointHoverRadius:6 }]
    },
    options:{
      plugins:{legend:{display:false},tooltip:{callbacks:{label:v=>" "+v.raw.toLocaleString("vi-VN")+"₫"}}},
      scales:{
        x:{grid:{color:"rgba(255,255,255,.03)"},ticks:{maxRotation:0,maxTicksLimit:7}},
        y:{grid:{color:"rgba(255,255,255,.03)"},beginAtZero:true,ticks:{callback:v=>v>=1e6?(v/1e6).toFixed(1)+"M":v}}
      },animation:{duration:800,easing:"easeOutQuart"}
    }
  });

  // ── Brand share doughnut
  const brandMap = {};
  clicks.forEach(c => {
    const v = (c.vendor||"unknown").toLowerCase();
    brandMap[v] = (brandMap[v]||0) + (Number(c.commissionEarned)||0);
  });
  const brandEntries = Object.entries(brandMap).sort((a,b)=>b[1]-a[1]).slice(0,6);
  const colors = [ACID,"#00BFFF",ORANGE,"#BF00FF",GREEN,RED];
  destroyChart("chart-brand-share");
  if (brandEntries.length > 0) {
    charts["chart-brand-share"] = new Chart(document.getElementById("chart-brand-share"), {
      type:"doughnut",
      data:{
        labels: brandEntries.map(([k])=>k.toUpperCase()),
        datasets:[{ data: brandEntries.map(([,v])=>v),
          backgroundColor: colors.map(c=>c+"44"),
          borderColor: colors, borderWidth:2, hoverOffset:6 }]
      },
      options:{
        cutout:"62%",
        plugins:{legend:{position:"bottom",labels:{boxWidth:8,padding:10,font:{size:8}}}},
        animation:{animateRotate:true,duration:900,easing:"easeOutQuart"}
      }
    });
  }

  // ── Order status doughnut
  const statusMap = {pending:0,paid:0,delivered:0,cancelled:0};
  orders.forEach(o => {
    const s = (o.status||"pending").toLowerCase();
    if (statusMap[s]!==undefined) statusMap[s]++;
    else statusMap.pending++;
  });
  destroyChart("chart-order-status");
  charts["chart-order-status"] = new Chart(document.getElementById("chart-order-status"), {
    type:"doughnut",
    data:{
      labels:["PENDING","PAID","DELIVERED","CANCELLED"],
      datasets:[{
        data:[statusMap.pending,statusMap.paid,statusMap.delivered,statusMap.cancelled],
        backgroundColor:["rgba(255,140,0,.6)","rgba(0,191,255,.6)","rgba(0,255,136,.6)","rgba(255,0,64,.6)"],
        borderColor:[ORANGE,"#00BFFF",GREEN,RED], borderWidth:2, hoverOffset:6
      }]
    },
    options:{
      cutout:"60%",
      plugins:{legend:{position:"bottom",labels:{boxWidth:8,padding:10,font:{size:8}}}},
      animation:{animateRotate:true,duration:900,easing:"easeOutQuart"}
    }
  });
}

// ─── Live Feed ─────────────────────────────────────────────────
function renderLiveFeed(orders, clicks) {
  // Orders feed
  const of = document.getElementById("feed-orders");
  if (of) {
    const recent = orders.slice(0, 7);
    if (!recent.length) {
      of.innerHTML = emptyState("No orders yet");
    } else {
      of.innerHTML = recent.map(o => {
        const status = (o.status||"pending").toLowerCase();
        const icoColor = {pending:"rgba(255,140,0,.15)",paid:"rgba(0,191,255,.15)",delivered:"rgba(0,255,136,.15)",cancelled:"rgba(255,0,64,.15)"}[status]||"rgba(255,255,255,.05)";
        const strokeColor = {pending:"#FF8C00",paid:"#00BFFF",delivered:"#00FF88",cancelled:"#FF0040"}[status]||"#8A8A8A";
        return `
        <div class="activity-row">
          <div class="activity-ico" style="background:${icoColor}">
            <svg viewBox="0 0 24 24" fill="none" stroke="${strokeColor}" stroke-width="2" stroke-linecap="round">
              <path d="M6 2 3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"/><line x1="3" y1="6" x2="21" y2="6"/>
            </svg>
          </div>
          <div class="activity-body">
            <div class="activity-name">#${(o.docId||"").slice(-8).toUpperCase()} · ${(o.items||[]).length || "?"}  items</div>
            <div class="activity-meta">${badgeStatus(o.status)} · ${o.paymentMethod||""} · ${timeAgo(o.timestamp)}</div>
          </div>
          <div class="activity-amt" style="color:var(--green)">${fmtVnd(o.totalAmount)}</div>
        </div>`;
      }).join("");
    }
  }

  // Clicks feed
  const cf = document.getElementById("feed-clicks");
  if (cf) {
    const recent = clicks.slice(0, 7);
    if (!recent.length) {
      cf.innerHTML = emptyState("No affiliate clicks yet");
    } else {
      cf.innerHTML = recent.map(c => `
        <div class="activity-row">
          <div class="activity-ico" style="background:rgba(192,255,0,.08)">
            <svg viewBox="0 0 24 24" fill="none" stroke="var(--acid)" stroke-width="2" stroke-linecap="round">
              <path d="M15 3h4a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-4"/><polyline points="10 17 15 12 10 7"/><line x1="15" y1="12" x2="3" y2="12"/>
            </svg>
          </div>
          <div class="activity-body">
            <div class="activity-name">${c.artifactTitle||"Unknown"}</div>
            <div class="activity-meta">${(c.vendor||"").toUpperCase()} · ${c.archetype||""} · ${timeAgo(c.timestamp)}</div>
          </div>
          <div class="activity-amt" style="color:var(--acid)">+${fmtVnd(c.commissionEarned||0)}</div>
        </div>`).join("");
    }
  }
}

function emptyState(txt) {
  return `<div class="empty"><div class="empty-ico"><svg viewBox="0 0 24 24" fill="none" stroke-width="1.5"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg></div><div class="empty-txt">${txt}</div></div>`;
}
