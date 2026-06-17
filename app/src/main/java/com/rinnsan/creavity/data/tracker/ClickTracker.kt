package com.rinnsan.creavity.data.tracker

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.rinnsan.creavity.data.vault.AffiliateArtifact

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// CLICK EVENT — 1 document trong collection "clicks"
// Dùng cho AdminDashboard để parse data từ Firestore
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
@Suppress("unused")
data class ClickEvent(
    val artifactId: String,
    val artifactTitle: String,
    val vendor: String,
    val archetype: String,
    val priceRaw: Long,          // giá gốc (VND, đã parse)
    val commissionRate: Double,  // rate của brand (0.08 = 8%)
    val commissionEarned: Long,  // priceRaw × commissionRate
    val userId: String,
    val timestamp: Long = System.currentTimeMillis()
)

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// BRAND COMMISSION RATES
// Lưu ở đây để fallback khi Firestore chưa có — đồng thời
// VaultViewModel cũng sẽ lookup từ Firestore brands/ collection
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
object BrandRates {
    // Key phải khớp với field "vendor" trong Firestore artifacts
    val default = mapOf(
        "NIKE"            to 0.08,
        "ADIDAS"          to 0.07,
        "ACRONYM_REPLICA" to 0.10,
        "DISTRICT_SUPPLY" to 0.09,
        "NOCTURNE_LAB"    to 0.10,
        "IRON_PROTOCOL"   to 0.08,
        "STEEL_DIVISION"  to 0.07,
        "STATIC_WORKS"    to 0.09,
        "CORRUPT_FABRIC"  to 0.09,
        "WAYPOINT_SUPPLY" to 0.08,
        "CARRY_PROTOCOL"  to 0.08,
        "RINNSAN_LAB"     to 0.12  // brand riêng margin cao nhất
    )

    fun getRate(vendor: String): Double =
        default[vendor.uppercase()] ?: 0.08 // fallback 8%
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// PRICE PARSER
// "1.850.000 VND" → 1850000L
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
fun parsePrice(priceStr: String): Long {
    return try {
        priceStr
            .replace(".", "")
            .replace(",", "")
            .replace("VND", "")
            .replace("đ", "")
            .replace(" ", "")
            .trim()
            .toLong()
    } catch (e: Exception) {
        0L
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// CLICK TRACKER — singleton object
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
object ClickTracker {

    private val db   = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    /**
     * Gọi hàm này MỖI KHI user bấm "INITIATE ACQUISITION"
     * Luồng:
     *  1. Lookup commission rate từ Firestore brands/ (fallback BrandRates.default)
     *  2. Tính commission = price × rate
     *  3. Ghi document vào clicks/
     *  4. Cập nhật summary tổng hợp trong stats/summary (atomic increment)
     */
    fun recordClick(artifact: AffiliateArtifact) {
        val uid = auth.currentUser?.uid ?: "anonymous"

        // Lookup rate từ Firestore brands/ trước, fallback local nếu lỗi
        db.collection("brands").document(artifact.vendor.uppercase()).get()
            .addOnSuccessListener { brandDoc ->
                val rate = if (brandDoc.exists()) {
                    brandDoc.getDouble("rate") ?: BrandRates.getRate(artifact.vendor)
                } else {
                    BrandRates.getRate(artifact.vendor)
                }
                writeClick(artifact, uid, rate)
            }
            .addOnFailureListener {
                // Firestore lỗi → dùng local rate
                writeClick(artifact, uid, BrandRates.getRate(artifact.vendor))
            }
    }

    private fun writeClick(
        artifact: AffiliateArtifact,
        uid: String,
        rate: Double
    ) {
        val priceRaw        = parsePrice(artifact.price)
        val commissionEarned = (priceRaw * rate).toLong()

        // ── 1. Ghi click event ─────────────────────────────────────
        val clickData = hashMapOf(
            "artifactId"       to artifact.id,
            "artifactTitle"    to artifact.title,
            "vendor"           to artifact.vendor,
            "archetype"        to artifact.archetype.name,
            "priceRaw"         to priceRaw,
            "commissionRate"   to rate,
            "commissionEarned" to commissionEarned,
            "userId"           to uid,
            "timestamp"        to System.currentTimeMillis(),
            // Thêm date parts để dễ query theo ngày/tháng
            "dateKey"          to dateKey(),   // "2026-04-10"
            "monthKey"         to monthKey(),  // "2026-04"
            "weekKey"          to weekKey()    // "2026-W15"
        )

        db.collection("clicks").add(clickData)

        // ── 2. Cập nhật summary tổng hợp (atomic) ─────────────────
        // Document stats/summary luôn giữ tổng running total
        db.collection("stats").document("summary")
            .update(
                "totalClicks",      FieldValue.increment(1),
                "totalCommission",  FieldValue.increment(commissionEarned)
            )
            .addOnFailureListener {
                // Nếu document chưa tồn tại → tạo mới
                db.collection("stats").document("summary")
                    .set(hashMapOf(
                        "totalClicks"     to 1L,
                        "totalCommission" to commissionEarned
                    ))
            }

        // ── 3. Cập nhật stats theo brand ──────────────────────────
        db.collection("stats").document("by_brand")
            .update(
                "${artifact.vendor}.clicks",     FieldValue.increment(1),
                "${artifact.vendor}.commission", FieldValue.increment(commissionEarned)
            )
            .addOnFailureListener {
                db.collection("stats").document("by_brand")
                    .set(hashMapOf(
                        artifact.vendor to hashMapOf(
                            "clicks"     to 1L,
                            "commission" to commissionEarned,
                            "name"       to artifact.vendor
                        )
                    ))
            }

        // ── 4. Cập nhật stats theo ngày ───────────────────────────
        val dayKey = dateKey()
        db.collection("stats").document("by_day")
            .update(
                "$dayKey.clicks",     FieldValue.increment(1),
                "$dayKey.commission", FieldValue.increment(commissionEarned)
            )
            .addOnFailureListener {
                db.collection("stats").document("by_day")
                    .set(hashMapOf(
                        dayKey to hashMapOf(
                            "clicks"     to 1L,
                            "commission" to commissionEarned
                        )
                    ))
            }

        // ── 5. Cập nhật stats theo artifact ───────────────────────
        db.collection("stats").document("by_artifact")
            .update(
                "${artifact.id}.clicks",     FieldValue.increment(1),
                "${artifact.id}.commission", FieldValue.increment(commissionEarned),
                "${artifact.id}.title",      artifact.title,
                "${artifact.id}.vendor",     artifact.vendor
            )
            .addOnFailureListener {
                db.collection("stats").document("by_artifact")
                    .set(hashMapOf(
                        artifact.id to hashMapOf(
                            "clicks"     to 1L,
                            "commission" to commissionEarned,
                            "title"      to artifact.title,
                            "vendor"     to artifact.vendor,
                            "archetype"  to artifact.archetype.name
                        )
                    ))
            }
    }

    // ── Date helpers ───────────────────────────────────────────────
    private fun dateKey(): String {
        val cal = java.util.Calendar.getInstance()
        return "%04d-%02d-%02d".format(
            cal.get(java.util.Calendar.YEAR),
            cal.get(java.util.Calendar.MONTH) + 1,
            cal.get(java.util.Calendar.DAY_OF_MONTH)
        )
    }

    private fun monthKey(): String {
        val cal = java.util.Calendar.getInstance()
        return "%04d-%02d".format(
            cal.get(java.util.Calendar.YEAR),
            cal.get(java.util.Calendar.MONTH) + 1
        )
    }

    private fun weekKey(): String {
        val cal = java.util.Calendar.getInstance()
        return "%04d-W%02d".format(
            cal.get(java.util.Calendar.YEAR),
            cal.get(java.util.Calendar.WEEK_OF_YEAR)
        )
    }
}