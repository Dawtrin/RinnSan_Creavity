// ═══════════════════════════════════════════════════════════════
// FIREBASE CONFIG + CLOUDINARY SETTINGS
// ═══════════════════════════════════════════════════════════════
import { initializeApp }        from "https://www.gstatic.com/firebasejs/10.12.2/firebase-app.js";
import { getFirestore }         from "https://www.gstatic.com/firebasejs/10.12.2/firebase-firestore.js";
import { getAuth }              from "https://www.gstatic.com/firebasejs/10.12.2/firebase-auth.js";

const firebaseConfig = {
  apiKey:            "AIzaSyAX6eZ8RbrcM0ckSQdHoApgrWuSPeP7TvU",
  authDomain:        "rinnsan-creavity-703dc.firebaseapp.com",
  projectId:         "rinnsan-creavity-703dc",
  storageBucket:     "rinnsan-creavity-703dc.firebasestorage.app",
  messagingSenderId: "808431509649",
  appId:             "1:808431509649:web:f4234c8d6d00e48d4e32a6"
};

const app = initializeApp(firebaseConfig);
export const db   = getFirestore(app);
export const auth = getAuth(app);

export const CLOUDINARY = {
  cloudName:    "dsdhckzwo",
  uploadPreset: "noteapp_unsigned",
  uploadUrl:    "https://api.cloudinary.com/v1_1/dsdhckzwo/image/upload"
};
