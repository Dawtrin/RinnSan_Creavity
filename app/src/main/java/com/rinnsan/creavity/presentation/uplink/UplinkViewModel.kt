package com.rinnsan.creavity.presentation.uplink

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.rinnsan.creavity.data.repository.GeminiRepository
import com.rinnsan.creavity.data.repository.IdentityRepository
import com.rinnsan.creavity.domain.model.ChatMessage
import com.rinnsan.creavity.domain.models.IdentityProfile
import com.rinnsan.creavity.presentation.uplink.stylist.StylistState
import com.rinnsan.creavity.presentation.uplink.stylist.Suggestion
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

sealed class UplinkUiState {
    object Loading : UplinkUiState()
    object NoIdentity : UplinkUiState()
    data class HasIdentity(val profile: IdentityProfile) : UplinkUiState()
}

@HiltViewModel
class UplinkViewModel @Inject constructor(
    private val identityRepository: IdentityRepository,
    private val geminiRepository: GeminiRepository
) : ViewModel() {

    companion object {
        private const val TAG = "UplinkVM"
    }

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow<UplinkUiState>(UplinkUiState.Loading)
    val uiState: StateFlow<UplinkUiState> = _uiState.asStateFlow()

    private val _identityProfile = MutableStateFlow<IdentityProfile?>(null)
    val identityProfile: StateFlow<IdentityProfile?> = _identityProfile.asStateFlow()

    private val _stylistState = MutableStateFlow<StylistState>(StylistState.OFFLINE)
    val stylistState: StateFlow<StylistState> = _stylistState.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages = _chatMessages.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing = _isProcessing.asStateFlow()

    init {
        loadSavedProfile()
        observeScannerEvents()
    }

    private fun loadSavedProfile() {
        viewModelScope.launch {
            _uiState.value = UplinkUiState.Loading
            val savedProfile = identityRepository.loadProfile()
            if (savedProfile != null) {
                applyProfile(savedProfile, shouldPersist = false)
            } else {
                _uiState.value = UplinkUiState.NoIdentity
            }
        }
    }

    private fun observeScannerEvents() {
        viewModelScope.launch {
            identityRepository.profileEvents.collect { profile ->
                val currentProfile = _identityProfile.value
                if (currentProfile?.id != profile.id) {
                    applyProfile(profile, shouldPersist = false)
                }
            }
        }
    }

    fun setIdentityProfile(profile: IdentityProfile) {
        viewModelScope.launch { identityRepository.saveProfile(profile) }
        applyProfile(profile, shouldPersist = false)
    }

    fun clearIdentity() {
        viewModelScope.launch { identityRepository.clearProfile() }
        _identityProfile.value = null
        _uiState.value = UplinkUiState.NoIdentity
        _stylistState.value = StylistState.OFFLINE
        _chatMessages.value = emptyList()
        _isProcessing.value = false
    }

    private fun applyProfile(profile: IdentityProfile, shouldPersist: Boolean) {
        _identityProfile.value = profile
        _uiState.value = UplinkUiState.HasIdentity(profile)
        _stylistState.value = StylistState.ONLINE

        if (_chatMessages.value.isEmpty()) {
            _chatMessages.value = listOf(
                ChatMessage(
                    text = "SYSTEM INITIALIZED. ARCHETYPE DETECTED: " +
                            "${profile.dominantArchetype.displayName}. AWAITING DIRECTIVE.",
                    isFromUser = false
                )
            )
        }

        if (shouldPersist) {
            viewModelScope.launch { identityRepository.saveProfile(profile) }
        }
    }

    // ══════════════════════════════════════════════════════════════
    // 🧠 GEMINI + FIRESTORE — HYBRID AI ENGINE
    // ══════════════════════════════════════════════════════════════

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        val currentProfile = _identityProfile.value ?: run {
            showError("IDENTITY NOT LOADED. Please rescan.")
            return
        }
        if (_isProcessing.value) return

        viewModelScope.launch {
            try {
                _isProcessing.value = true

                // 1. Lưu tin nhắn User
                val userMsg = ChatMessage(text = text, isFromUser = true)
                _chatMessages.update { msgs ->
                    val newMsgs = msgs + userMsg
                    if (newMsgs.size > 50) newMsgs.takeLast(50) else newMsgs
                }
                delay(300L)

                // 2. Kích hoạt hiệu ứng ANALYZE
                val previousSuggestions = (_stylistState.value as? StylistState.SUGGEST)?.suggestions ?: emptyList()
                _stylistState.value = StylistState.ANALYZE(userMessage = text)

                // 3. Fetch body data và Vault products TRƯỚC
                //    → Vault products được truyền vào Gemini để AI giới thiệu đúng hàng
                val bodyDataDeferred = async { fetchBodyData() }
                val vaultProductsDeferred = async { fetchArtifactSuggestions(text, currentProfile) }

                val (h, w) = bodyDataDeferred.await()
                // BUG #3 FIX: fetchArtifactSuggestions giờ trả về Pair(suggestions, isProductQuery)
                // isProductQuery = true  → câu hỏi liên quan đến sản phẩm (giữ/cập nhật suggestions)
                // isProductQuery = false → câu hỏi không liên quan (xóa suggestions cũ)
                val (vaultSuggestions, isProductQuery) = vaultProductsDeferred.await()

                // 4. Gọi Gemini với context sản phẩm Vault thực tế
                val aiResponseText = fetchGeminiResponse(
                    text = text,
                    profile = currentProfile,
                    height = h,
                    weight = w,
                    vaultProducts = vaultSuggestions
                )

                // BUG #3 FIX: Phân biệt 2 trường hợp empty suggestions:
                // - isProductQuery = false → câu hỏi không liên quan → XÓA suggestions cũ
                // - isProductQuery = true, vaultSuggestions empty → query thất bại → GIỮ lại
                val finalSuggestions = when {
                    vaultSuggestions.isNotEmpty() -> vaultSuggestions
                    isProductQuery -> previousSuggestions  // Query attempted nhưng empty → giữ cũ
                    else -> emptyList()                    // Câu hỏi không liên quan → xóa sạch
                }

                Log.d(TAG, "✓ Gemini: ${aiResponseText.take(80)}...")
                Log.d(TAG, "✓ Vault: ${vaultSuggestions.size} products | isProductQuery=$isProductQuery | final=${finalSuggestions.size}")

                // 5. Lưu response vào chat
                val aiMsg = ChatMessage(text = aiResponseText, isFromUser = false)
                _chatMessages.update { msgs ->
                    val newMsgs = msgs + aiMsg
                    if (newMsgs.size > 50) newMsgs.takeLast(50) else newMsgs
                }

                // 6. Đẩy kết quả ra giao diện
                _stylistState.value = StylistState.SUGGEST(
                    userMessage = text,
                    systemResponse = aiResponseText,
                    suggestions = finalSuggestions
                )

            } catch (e: Exception) {
                Log.e(TAG, "sendMessage error: ${e.message}", e)
                showError("SYSTEM ERROR\n${e.message}")
            } finally {
                _isProcessing.value = false
            }
        }
    }

    // ──────────────────────────────────────────────────────────────
    // GEMINI API — Với fallback sang Local AI
    // ──────────────────────────────────────────────────────────────

    private suspend fun fetchGeminiResponse(
        text: String,
        profile: IdentityProfile,
        height: Float = 0f,
        weight: Float = 0f,
        vaultProducts: List<Suggestion> = emptyList()
    ): String {
        return try {
            Log.d(TAG, "▶ Calling Gemini with ${vaultProducts.size} Vault products in context")
            val response = kotlinx.coroutines.withTimeoutOrNull(15_000L) {
                geminiRepository.getStylistResponse(
                    userMessage = text,
                    profile = profile,
                    conversationHistory = buildConversationHistory(),
                    height = height,
                    weight = weight,
                    vaultProducts = vaultProducts
                )
            } ?: throw Exception("API Timeout")

            Log.d(TAG, "✓ Gemini OK: ${response.take(60)}...")
            response
        } catch (e: Exception) {
            Log.e(TAG, "✗ Gemini FAILED: ${e::class.simpleName} — ${e.message}")
            val localResponse = generateLocalAIResponse(text.lowercase(), profile, height, weight)
            "❆ $localResponse"
        }
    }

    // ──────────────────────────────────────────────────────────────
    // FIRESTORE — Query sản phẩm theo archetype + category
    // ──────────────────────────────────────────────────────────────

    /**
     * Fetch sản phẩm từ Firestore dựa trên intent + category + brand trong câu hỏi.
     *
     * @return Pair(
     *   suggestions: List<Suggestion> — sản phẩm phù hợp (rỗng nếu không có),
     *   isProductQuery: Boolean — true nếu câu hỏi liên quan đến sản phẩm/mua sắm
     * )
     *
     * isProductQuery dùng để phân biệt:
     *   - false → câu hỏi không liên quan → UI nên XÓA suggestions cũ
     *   - true  → câu hỏi liên quan nhưng DB trả về empty → UI GIỮ lại suggestions cũ
     */
    private suspend fun fetchArtifactSuggestions(
        text: String,
        profile: IdentityProfile
    ): Pair<List<Suggestion>, Boolean> {
        val lowerText = text.lowercase()

        val explicitIntent = listOf(
            "gợi ý", "tư vấn", "recommend", "suggest",
            "tìm", "mua", "shop", "mua gì", "mặc gì",
            "phối đồ", "outfit"
        ).any { lowerText.contains(it) }

        val mentionsProduct = listOf(
            "giày", "sneaker", "boots", "áo", "quần", "jacket",
            "hoodie", "shirt", "balo", "túi", "phụ kiện",
            "trang phục"
        ).any { lowerText.contains(it) }

        // BUG #2 FIX: Detect brand cụ thể trong câu hỏi
        val targetBrand = detectBrand(lowerText)
        val targetCategory = detectCategory(lowerText)

        // Nếu có brand cụ thể → cũng coi là product query ngay cả khi không có keyword mua/gợi ý
        val shouldQuery = explicitIntent || mentionsProduct || targetBrand != null

        if (!shouldQuery) {
            Log.d(TAG, "⊘ No product query — intent=$explicitIntent, product=$mentionsProduct, brand=$targetBrand")
            return emptyList<Suggestion>() to false  // isProductQuery = false → xóa suggestions
        }

        return try {
            // BUG #2 FIX: Khi có brand cụ thể → query theo vendor thay vì archetype
            // Vì user hỏi "gợi ý Rick Owens" muốn thấy Rick Owens, không phải Nike cùng archetype
            // BUG FIX: Khi có brand cụ thể → query vendor + archetype của user trước
            // Nếu không có sản phẩm phù hợp archetype → fallback query toàn bộ brand
            val result = if (targetBrand != null) {
                Log.d(TAG, "🏷 Brand query: vendor=$targetBrand + archetype=${profile.dominantArchetype.name}" +
                        (if (targetCategory != null) " cat=$targetCategory" else ""))

                // Attempt 1: brand + archetype của user (kết quả chính xác nhất)
                var brandArchetypeQuery = db.collection("artifacts")
                    .whereEqualTo("vendor", targetBrand)
                    .whereEqualTo("archetype", profile.dominantArchetype.name)

                if (targetCategory != null) {
                    brandArchetypeQuery = brandArchetypeQuery.whereEqualTo("category", targetCategory)
                }

                val primaryResult = try {
                    brandArchetypeQuery.limit(6).get().await()
                } catch (e: Exception) {
                    // Firestore có thể báo thiếu composite index → fallback
                    Log.w(TAG, "⚠ vendor+archetype query failed (missing index?): ${e.message}")
                    null
                }

                if (!primaryResult?.documents.isNullOrEmpty()) {
                    Log.d(TAG, "✓ Found ${primaryResult!!.documents.size} ${targetBrand} items for archetype ${profile.dominantArchetype.name}")
                    primaryResult
                } else {
                    // Fallback: brand bất kỳ archetype (user hỏi brand không có trong archetype của họ)
                    Log.d(TAG, "⚠ No archetype match → fallback to brand-only query")
                    var fallbackQuery = db.collection("artifacts")
                        .whereEqualTo("vendor", targetBrand)

                    if (targetCategory != null) {
                        fallbackQuery = fallbackQuery.whereEqualTo("category", targetCategory)
                    }

                    fallbackQuery.limit(6).get().await()
                }
            } else {
                // Không có brand cụ thể → query theo archetype như cũ
                Log.d(TAG, "🗂 Archetype query: ${profile.dominantArchetype.name}" +
                        (if (targetCategory != null) " cat=$targetCategory" else ""))

                var archetypeQuery = db.collection("artifacts")
                    .whereEqualTo("archetype", profile.dominantArchetype.name)

                if (targetCategory != null) {
                    archetypeQuery = archetypeQuery.whereEqualTo("category", targetCategory)
                }

                archetypeQuery.limit(6).get().await()
            }

            Log.d(TAG, "Firestore: ${result.documents.size} artifacts returned")

            val suggestions = result.documents.mapNotNull { doc ->
                val title = doc.getString("title") ?: return@mapNotNull null
                val isDirect = doc.getBoolean("isDirectSale") ?: false
                val stock = doc.getLong("stock") ?: 0L
                // Seeder dùng "affiliateLink" chứ không phải "affiliateUrl"
                val affiliateUrl = doc.getString("affiliateUrl")
                    ?: doc.getString("affiliateLink")
                    ?: doc.getString("link")
                    ?: ""

                // Mô tả rõ loại sản phẩm
                val desc = when {
                    isDirect && stock > 0 -> "DIRECT SALE — Còn hàng"
                    isDirect && stock <= 0 -> "DIRECT SALE — Hết hàng"
                    affiliateUrl.isNotEmpty() -> "AFFILIATE"
                    else -> doc.getString("category") ?: ""
                }

                Suggestion(
                    title = title,
                    description = desc,
                    imageUrl = doc.getString("imageUrl") ?: doc.getString("image") ?: "",
                    artifactId = doc.id,
                    price = doc.getString("price") ?: "",
                    isDirectSale = isDirect,
                    affiliateUrl = affiliateUrl,
                    inStock = stock > 0
                )
            }
            // Ưu tiên: Direct Sale có hàng → Affiliate → Direct Sale hết hàng
            .sortedWith(
                compareByDescending<Suggestion> { it.isDirectSale && it.inStock }
                    .thenByDescending { it.affiliateUrl.isNotEmpty() }
                    .thenByDescending { it.price.isNotEmpty() }
            )
            .take(3)

            suggestions to true  // isProductQuery = true dù suggestions rỗng
        } catch (e: Exception) {
            Log.e(TAG, "Firestore query failed: ${e.message}", e)
            emptyList<Suggestion>() to true  // Query thất bại → vẫn là product query → giữ suggestions cũ
        }
    }

    // ──────────────────────────────────────────────────────────────
    // HELPER FUNCTIONS
    // ──────────────────────────────────────────────────────────────

    private suspend fun fetchBodyData(): Pair<Float, Float> {
        val uid = auth.currentUser?.uid ?: return 0f to 0f
        return try {
            val doc = db.collection("users").document(uid).get().await()
            // FIX: Firestore có thể lưu là String hoặc Number → xử lý cả hai
            @Suppress("UNCHECKED_CAST")
            val bodyData = doc.get("bodyData") as? Map<String, Any>
            val h = when (val hv = bodyData?.get("height")) {
                is Number -> hv.toFloat()
                is String -> hv.toFloatOrNull() ?: 0f
                else -> 0f
            }
            val w = when (val wv = bodyData?.get("weight")) {
                is Number -> wv.toFloat()
                is String -> wv.toFloatOrNull() ?: 0f
                else -> 0f
            }
            Log.d(TAG, "✓ Body data: height=$h, weight=$w")
            h to w
        } catch (e: Exception) {
            Log.e(TAG, "fetchBodyData failed: ${e.message}")
            0f to 0f
        }
    }


    // Category mapping khớp với Firestore seeder data
    private fun detectCategory(text: String): String? = when {
        text.containsAny("jacket", "áo khoác", "coat", "bomber") -> "JACKET"
        text.containsAny("hoodie") -> "HOODIE"
        text.containsAny("sweater", "len", "knit") -> "SWEATER"
        text.containsAny("quần", "pants", "jeans") -> "PANTS"
        text.containsAny("shorts", "quần đùi", "quần ngắn") -> "SHORTS"
        text.containsAny("balo", "túi", "bag", "backpack") -> "BAG"
        text.containsAny("giày", "sneaker", "sneakers") -> "SNEAKERS"
        text.containsAny("boots", "boot") -> "BOOTS"
        text.containsAny("áo", "shirt", "tee", "top", "polo") -> "TOP"
        else -> null  // null = query tất cả category → trả kết quả chung
    }

    // BUG #2 FIX: Brand detection — map tên brand trong câu hỏi → vendor field trong Firestore
    // Thêm brand mới vào đây khi có seeder mới
    private fun detectBrand(text: String): String? = when {
        text.containsAny("rick owens", "rick owen") -> "RICK OWENS"
        text.containsAny("nike acg", "nike") -> "NIKE"
        text.containsAny("puma") -> "PUMA"
        text.containsAny("balenciaga") -> "BALENCIAGA"
        text.containsAny("rinnsan", "rinn san") -> "RINNSAN"
        else -> null  // null = không detect brand cụ thể → query theo archetype
    }

    private fun String.containsAny(vararg keywords: String): Boolean =
        keywords.any { this.contains(it) }

    private fun buildConversationHistory(): List<Pair<String, String>> {
        val msgs = _chatMessages.value
        val pairs = mutableListOf<Pair<String, String>>()
        var i = 0
        while (i < msgs.size - 1) {
            if (msgs[i].isFromUser && !msgs[i + 1].isFromUser) {
                // Strip offline prefix nếu có — Gemini không cần biết fallback
                val cleanResponse = msgs[i + 1].text
                    .removePrefix("❆ ")
                    .removePrefix("[Chế độ offline] ") // backward compat
                pairs.add(msgs[i].text to cleanResponse)
            }
            i++
        }
        return pairs.takeLast(5)
    }

    // ──────────────────────────────────────────────────────────────
    // LOCAL AI FALLBACK — Dùng khi Gemini không khả dụng
    // ──────────────────────────────────────────────────────────────

    private fun generateLocalAIResponse(
        input: String,
        profile: IdentityProfile,
        height: Float,
        weight: Float
    ): String {
        val archetype = profile.dominantArchetype.displayName
        val archetypeName = profile.dominantArchetype.name

        val size = when {
            height == 0f -> "chưa xác định"
            height > 180f || weight > 80f -> "XL"
            height > 170f || weight > 65f -> "L"
            else -> "M"
        }

        return when {
            // ── Gợi ý / Tư vấn trang phục ──
            input.containsAny("gợi ý", "tư vấn", "mặc gì", "phối đồ", "outfit") -> {
                "Dựa trên phong cách $archetype (Size: $size), tôi đề xuất:\n" +
                        getArchetypeOutfitTip(archetypeName) +
                        "\n\nHãy thử hỏi cụ thể hơn: 'gợi ý giày', 'tư vấn áo khoác'..."
            }

            // ── Xu hướng / Trend ──
            input.containsAny("xu hướng", "trend", "hot", "mới nhất", "2025", "2026") -> {
                getTrendResponse(archetypeName)
            }

            // ── Màu sắc ──
            input.containsAny("màu", "color", "tone", "phối màu") -> {
                getColorAdvice(archetypeName)
            }

            // ── Thời tiết ──
            input.containsAny("nóng", "lạnh", "mưa", "hè", "đông", "thu", "xuân", "weather") -> {
                getWeatherAdvice(input, archetypeName)
            }

            // ── Thương hiệu ──
            input.containsAny("brand", "hãng", "thương hiệu", "nike", "rick owens", "balenciaga", "puma") -> {
                getBrandAdvice(archetypeName)
            }

            // ── Sự kiện / Dịp đặc biệt ──
            input.containsAny("đi chơi", "đi làm", "tiệc", "date", "hẹn hò", "công sở", "dạo phố") -> {
                getOccasionAdvice(input, archetypeName)
            }

            // ── Số đo / Body data ──
            input.containsAny("số đo", "cơ thể", "body", "chiều cao", "cân nặng") -> {
                if (height > 0) "Dữ liệu sinh trắc: Cao ${height.toInt()}cm, Nặng ${weight.toInt()}kg → Size $size.\nPhong cách $archetype phù hợp với tỷ lệ cơ thể của bạn."
                else "Chưa có dữ liệu sinh trắc. Hãy cập nhật tại mục BODY DATA để nhận gợi ý chính xác hơn."
            }

            // ── Chào hỏi ──
            input.containsAny("chào", "hello", "hi", "xin chào", "hey") -> {
                "Hệ thống trực tuyến. Phong cách $archetype đã được nạp. Bạn cần tư vấn gì hôm nay?"
            }

            // ── Cảm ơn ──
            input.containsAny("cảm ơn", "thanks", "thank", "tks") -> {
                "Không có gì. Hệ thống luôn sẵn sàng hỗ trợ phong cách $archetype của bạn."
            }

            // ── Tìm sản phẩm cụ thể ──
            input.containsAny("giày", "sneaker", "boots", "áo", "quần", "jacket", "hoodie",
                "phụ kiện", "kính", "mũ", "balo", "túi", "shirt") -> {
                "Tôi đang tìm kiếm trong kho lưu trữ cho phong cách $archetype.\nHãy thử: 'gợi ý giày' hoặc 'tư vấn áo khoác' để xem sản phẩm cụ thể."
            }

            // ── FALLBACK THÔNG MINH — không bao giờ "Lệnh không xác định" ──
            else -> {
                "Câu hỏi thú vị! Với phong cách $archetype, tôi có thể tư vấn:\n" +
                        "• Gợi ý trang phục theo dịp\n" +
                        "• Xu hướng thời trang mới nhất\n" +
                        "• Phối màu theo archetype\n" +
                        "• Tìm sản phẩm trong kho lưu trữ\n\n" +
                        "Hãy thử hỏi cụ thể hơn nhé!"
            }
        }
    }

    // ── Archetype-specific outfit tips ──
    private fun getArchetypeOutfitTip(archetype: String): String = when (archetype) {
        "GHOST" -> "• Layer tối giản: áo thun đen + quần cargo slim + giày chunky đen\n• Tông màu: đen, xám đậm, navy — tránh pattern nổi bật"
        "OPERATOR" -> "• Technical wear: áo gió chống nước + quần jogger tapered + sneaker utility\n• Chất liệu: Gore-Tex, ripstop, nylon — ưu tiên chức năng"
        "GLITCH" -> "• Pattern clash: oversized graphic tee + quần wide-leg + giày platform\n• Đừng sợ mix: asymmetric cut, layer chồng layer, phá vỡ quy tắc"
        "NOMAD" -> "• Vintage remix: áo flannel + quần denim washed + boots da vintage\n• Layer storytelling: mỗi item có lịch sử, texture, patina tự nhiên"
        else -> "• Bắt đầu với basics chất lượng cao, sau đó thêm statement pieces theo phong cách riêng"
    }

    private fun getTrendResponse(archetype: String): String {
        val baseTrend = "Xu hướng đáng chú ý:\n" +
                "• Quiet Luxury — chất liệu cao cấp, thiết kế tối giản\n" +
                "• Gorpcore — outdoor aesthetic trong đời thường\n" +
                "• Archive Fashion — tái sử dụng các mẫu kinh điển\n" +
                "• Techwear Evolution — chức năng + thẩm mỹ"
        val personalNote = when (archetype) {
            "GHOST" -> "\n\nVới phong cách Ghost: Quiet Luxury là sân chơi của bạn."
            "OPERATOR" -> "\n\nVới phong cách Operator: Techwear Evolution phù hợp nhất."
            "GLITCH" -> "\n\nVới phong cách Glitch: Archive Fashion + phá cách là lợi thế."
            "NOMAD" -> "\n\nVới phong cách Nomad: Gorpcore + Vintage remix rất hợp."
            else -> ""
        }
        return baseTrend + personalNote
    }

    private fun getColorAdvice(archetype: String): String = when (archetype) {
        "GHOST" -> "Bảng màu Ghost: Đen, trắng ngà, xám than, navy tối. Monochrome là chìa khóa. Tránh màu neon hay pattern sặc sỡ."
        "OPERATOR" -> "Bảng màu Operator: Đen, olive, xám khói, cam đất. Tone quân sự + công nghệ. Accent màu neon nhẹ để điểm nhấn."
        "GLITCH" -> "Bảng màu Glitch: Không có giới hạn! Neon, tím, acid green, clash màu có chủ đích. Càng bất ngờ càng tốt."
        "NOMAD" -> "Bảng màu Nomad: Nâu đất, kem, rêu, rust, denim wash. Tone ấm, vintage, organic. Tránh quá bóng bẩy."
        else -> "Chọn 3-4 màu chủ đạo và xoay quanh chúng để tạo wardrobe nhất quán."
    }

    private fun getWeatherAdvice(input: String, archetype: String): String {
        return when {
            input.containsAny("nóng", "hè") ->
                "Thời tiết nóng → Ưu tiên chất liệu thoáng: cotton, linen, mesh.\nVới phong cách $archetype: áo tay ngắn + quần short tailored + sneaker nhẹ."
            input.containsAny("lạnh", "đông") ->
                "Thời tiết lạnh → Layer game là chìa khóa.\nVới phong cách $archetype: inner tee + mid-layer hoodie/sweater + outer jacket + accessories (beanie, scarf)."
            input.containsAny("mưa") ->
                "Trời mưa → Chất liệu chống nước là must.\nGợi ý: rain jacket + quần quick-dry + giày waterproof boots."
            else ->
                "Thời tiết chuyển mùa → Layer mỏng, dễ tháo rời.\nÁo khoác nhẹ + áo thun bên trong = combo an toàn cho mọi archetype."
        }
    }

    private fun getBrandAdvice(archetype: String): String = when (archetype) {
        "GHOST" -> "Brands phù hợp Ghost: COS, Lemaire, Jil Sander, Auralee. Minimalism tinh tế, không logo."
        "OPERATOR" -> "Brands phù hợp Operator: ACRONYM, Stone Island, Nike ACG, Salomon. Chức năng + kỹ thuật."
        "GLITCH" -> "Brands phù hợp Glitch: Rick Owens, Comme des Garçons, Margiela, Vetements. Phá cách có nghệ thuật."
        "NOMAD" -> "Brands phù hợp Nomad: Kapital, Engineered Garments, RRL, Needles. Vintage soul + craft."
        else -> "Hãy chọn brands phù hợp với triết lý thời trang của bạn, không chỉ theo trend."
    }

    private fun getOccasionAdvice(input: String, archetype: String): String {
        return when {
            input.containsAny("đi làm", "công sở") ->
                "Đi làm với phong cách $archetype: Smart casual — giữ DNA phong cách nhưng tiết chế. Áo sơ mi relaxed + quần tailored + giày sạch."
            input.containsAny("tiệc", "party") ->
                "Đi tiệc: Statement piece là bắt buộc. Một item nổi bật + còn lại tối giản = cân bằng hoàn hảo."
            input.containsAny("date", "hẹn hò") ->
                "Hẹn hò: Tự tin là outfit đẹp nhất. Layer vừa phải, fit body, clean sneaker hoặc boots. Đừng quá cố — chính bạn là style."
            else ->
                "Dạo phố: Thoải mái nhưng có chủ đích. Với $archetype, hãy để phong cách nói thay bạn."
        }
    }

    private fun showError(message: String) {
        val errorMsg = ChatMessage(text = message, isFromUser = false, isError = true)
        _chatMessages.update { it + errorMsg }
        _stylistState.value = StylistState.ONLINE
    }

    fun shouldRefreshIdentity(): Boolean {
        return _identityProfile.value?.shouldRefresh() ?: true
    }
}