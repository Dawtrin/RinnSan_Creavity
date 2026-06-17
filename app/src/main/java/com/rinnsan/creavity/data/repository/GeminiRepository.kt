package com.rinnsan.creavity.data.repository

import android.util.Log
import com.rinnsan.creavity.BuildConfig
import com.rinnsan.creavity.domain.models.Archetype
import com.rinnsan.creavity.domain.models.IdentityProfile
import com.rinnsan.creavity.presentation.uplink.stylist.Suggestion
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ═══════════════════════════════════════════════════════════════════
 * GEMINI REPOSITORY V3 - DIAGNOSTIC EDITION
 * ═══════════════════════════════════════════════════════════════════
 *
 * [+] Chi tiết lỗi HTTP theo từng mã (401, 403, 429, 500...)
 * [+] Log rõ ràng hơn để debug trong Logcat
 * [+] Phân loại GeminiException để UplinkViewModel hiển thị đúng
 */

// ─── Custom Exception ─────────────────────────────────────────────
/**
 * Phân loại lỗi để UplinkViewModel hiển thị thông báo phù hợp.
 */
sealed class GeminiException(message: String) : Exception(message) {
    /** API Key sai hoặc chưa được kích hoạt */
    class InvalidApiKey(detail: String) : GeminiException("INVALID_API_KEY: $detail")

    /** Hết quota ngày hoặc bị rate limit */
    class QuotaExceeded(detail: String) : GeminiException("QUOTA_EXCEEDED: $detail")

    /** Server Gemini lỗi tạm thời */
    class ServerError(code: Int, detail: String) : GeminiException("SERVER_ERROR_$code: $detail")

    /** Không có internet hoặc timeout */
    class NetworkError(detail: String) : GeminiException("NETWORK_ERROR: $detail")

    /** Nội dung bị chặn bởi safety filter */
    class SafetyBlocked(detail: String) : GeminiException("SAFETY_BLOCKED: $detail")

    /** Response trống hoặc format sai */
    class EmptyResponse(detail: String) : GeminiException("EMPTY_RESPONSE: $detail")
}

@Singleton
class GeminiRepository @Inject constructor() {

    private val apiKey = BuildConfig.GEMINI_API_KEY
    private val primaryModel = "gemini-2.5-flash"
    private val fallbackModel = "gemini-2.0-flash"  // Backup khi model chính bị 503
    private val baseUrl = "https://generativelanguage.googleapis.com/v1beta/models"

    private var lastRequestTime = 0L
    private val minRequestInterval = 4000L  // 4 giây giữa các request — an toàn cho free tier (15 RPM)

    private val httpClient = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                prettyPrint = false
            })
        }

        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    val redacted = message
                        .replace(Regex("key=[^&\\s]+"), "key=***REDACTED***")
                        .replace(Regex("\"text\"\\s*:\\s*\"[^\"]{50,}"), "\"text\":\"***TRUNCATED***")
                    Log.d(TAG, "HTTP: $redacted")
                }
            }
            level = if (BuildConfig.DEBUG) LogLevel.HEADERS else LogLevel.NONE
        }
    }

    // ──────────────────────────────────────────────────────────────
    // MAIN ENTRY POINT
    // ──────────────────────────────────────────────────────────────

    suspend fun getStylistResponse(
        userMessage: String,
        profile: IdentityProfile?,
        conversationHistory: List<Pair<String, String>> = emptyList(),
        height: Float = 0f,
        weight: Float = 0f,
        vaultProducts: List<Suggestion> = emptyList()
    ): String {
        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        Log.d(TAG, "▶ getStylistResponse START | profile=${profile?.dominantArchetype?.name ?: "NULL"} | vault=${vaultProducts.size} products")

        if (apiKey.isBlank() || apiKey == "YOUR_API_KEY_HERE") {
            Log.e(TAG, "✗ API KEY IS BLANK OR PLACEHOLDER")
            throw GeminiException.InvalidApiKey("BuildConfig.GEMINI_API_KEY is empty. Check local.properties.")
        }

        if (profile == null) {
            return "Please complete your style profile first to get personalized advice."
        }

        applyRateLimiting()

        return try {
            retryWithBackoff(
                maxAttempts = 2,
                onAttempt = { attempt ->
                    Log.d(TAG, "➳ Attempt $attempt/2 [$primaryModel]")
                    callGeminiAPI(userMessage, profile, conversationHistory, primaryModel, height, weight, vaultProducts)
                },
                onFallback = {
                    Log.w(TAG, "⚠ Primary model failed — trying fallback: $fallbackModel")
                    try {
                        callGeminiAPI(userMessage, profile, conversationHistory, fallbackModel, height, weight, vaultProducts)
                    } catch (e: Exception) {
                        Log.w(TAG, "⚠ Fallback model also failed — using local fallback")
                        getIntelligentFallback(profile, userMessage)
                    }
                }
            )
        } catch (e: GeminiException.InvalidApiKey) {
            throw e
        }
    }

    // ──────────────────────────────────────────────────────────────
    // API CALL WITH DETAILED ERROR LOGGING
    // ──────────────────────────────────────────────────────────────

    private suspend fun callGeminiAPI(
        userMessage: String,
        profile: IdentityProfile,
        conversationHistory: List<Pair<String, String>>,
        modelName: String = primaryModel,
        height: Float = 0f,
        weight: Float = 0f,
        vaultProducts: List<Suggestion> = emptyList()
    ): String {
        val systemPrompt = buildSystemPromptV3(profile, height, weight, vaultProducts)

        Log.d(TAG, "▶ System prompt: ${systemPrompt.length} chars")
        Log.d(TAG, "▶ History turns: ${conversationHistory.size}")
        Log.d(TAG, "▶ User message: ${userMessage.take(80)}")
        Log.d(TAG, "▶ Calling: $baseUrl/$modelName:generateContent")

        // Build multi-turn contents: history + current message
        // FIX #1: Dùng systemInstruction đúng chuẩn Gemini API
        // (không inject system prompt vào role=user nữa — tránh lệch conversation flow)
        val contents = mutableListOf<Content>()

        // Add full conversation history as proper user/model turns
        for ((userQ, modelA) in conversationHistory) {
            contents.add(Content(role = "user", parts = listOf(Part(text = userQ))))
            contents.add(Content(role = "model", parts = listOf(Part(text = modelA))))
        }

        // Add current user message
        contents.add(Content(role = "user", parts = listOf(Part(text = userMessage))))

        val requestBody = GeminiRequest(
            contents = contents,
            // FIX #1: systemInstruction field — Gemini nhận đúng role, không bị hiểu nhầm là user message
            systemInstruction = SystemInstruction(
                parts = listOf(Part(text = systemPrompt))
            ),
            generationConfig = GenerationConfig(
                temperature = 0.8,
                topK = 40,
                topP = 0.95,
                // Tăng lên 1500 để tránh cắt giữa câu khi AI trả lời dài
                maxOutputTokens = 1500
            )
        )

        val httpResponse = try {
            httpClient.post("$baseUrl/$modelName:generateContent") {
                parameter("key", apiKey)
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }
        } catch (e: Exception) {
            // Lỗi tầng mạng (không có internet, timeout, DNS fail)
            Log.e(TAG, "✗ NETWORK EXCEPTION: ${e::class.simpleName} — ${e.message}")
            throw GeminiException.NetworkError(
                "Cannot reach Gemini server. Check internet. Detail: ${e.message}"
            )
        }

        val statusCode = httpResponse.status.value
        Log.d(TAG, "◀ HTTP Status: $statusCode ${httpResponse.status.description}")

        // ─── Phân loại lỗi HTTP ──────────────────────────────────
        if (statusCode != 200) {
            val errorBody = try {
                httpResponse.body<String>()
            } catch (e: Exception) {
                "Could not read error body: ${e.message}"
            }

            Log.e(TAG, "✗ HTTP $statusCode ERROR BODY:")
            Log.e(TAG, errorBody.take(500)) // Giới hạn để không spam logcat

            // Phân loại theo mã HTTP
            throw when (statusCode) {
                400 -> {
                    Log.e(TAG, "✗ 400 Bad Request — Thường do request body sai format")
                    GeminiException.ServerError(400, errorBody)
                }
                401 -> {
                    Log.e(TAG, "✗ 401 Unauthorized — API Key không hợp lệ hoặc chưa enabled Gemini API")
                    Log.e(TAG, "✗ Kiểm tra: https://aistudio.google.com/app/apikey")
                    GeminiException.InvalidApiKey("401 - Key rejected by server. $errorBody")
                }
                403 -> {
                    Log.e(TAG, "✗ 403 Forbidden — API Key không có quyền hoặc project bị disable")
                    GeminiException.InvalidApiKey("403 - Access denied. $errorBody")
                }
                429 -> {
                    Log.e(TAG, "✗ 429 Too Many Requests — Đã vượt rate limit hoặc hết quota ngày")
                    Log.e(TAG, "✗ Kiểm tra quota tại: https://console.cloud.google.com/apis/api/generativelanguage.googleapis.com")
                    GeminiException.QuotaExceeded("429 - Rate limit hit. $errorBody")
                }
                500, 503 -> {
                    Log.e(TAG, "✗ $statusCode Server Error — Gemini server đang có vấn đề, thử lại sau")
                    GeminiException.ServerError(statusCode, errorBody)
                }
                else -> {
                    Log.e(TAG, "✗ Unknown HTTP $statusCode")
                    GeminiException.ServerError(statusCode, "HTTP $statusCode: $errorBody")
                }
            }
        }

        // ─── Parse response thành công ───────────────────────────
        val response: GeminiResponse = try {
            httpResponse.body()
        } catch (e: Exception) {
            Log.e(TAG, "✗ JSON PARSE ERROR: ${e.message}")
            throw GeminiException.EmptyResponse("Cannot parse response JSON: ${e.message}")
        }

        // Check safety block
        val finishReason = response.candidates?.firstOrNull()?.finishReason
        if (finishReason == "SAFETY") {
            Log.w(TAG, "⚠ Response blocked by safety filter")
            throw GeminiException.SafetyBlocked("Content blocked. finishReason=SAFETY")
        }

        val text = response.candidates
            ?.firstOrNull()
            ?.content
            ?.parts
            ?.firstOrNull()
            ?.text
            ?: run {
                Log.e(TAG, "✗ Empty candidates. Full response: $response")
                throw GeminiException.EmptyResponse("No text in response. finishReason=$finishReason")
            }

        Log.d(TAG, "✓ Response OK: ${text.length} chars")
        return text.trim()
    }

    // ──────────────────────────────────────────────────────────────
    // PROMPTS (giữ nguyên logic V2)
    // ──────────────────────────────────────────────────────────────

    private fun buildSystemPromptV3(
        profile: IdentityProfile,
        height: Float = 0f,
        weight: Float = 0f,
        vaultProducts: List<Suggestion> = emptyList()
    ): String {
        val archetype = profile.dominantArchetype
        val styleFocus = getArchetypeStyleFocus(archetype)
        val hybridNote = if (profile.isHybrid && profile.secondaryArchetype != null) {
            "\nSecondary style influence: ${profile.secondaryArchetype.name} (${getArchetypeStyleFocus(profile.secondaryArchetype)})"
        } else ""

        val bodyNote = if (height > 0f && weight > 0f) {
            val size = when {
                height > 180f || weight > 80f -> "XL"
                height > 170f || weight > 65f -> "L"
                else -> "M"
            }
            "\nBody Data: Height=${height.toInt()}cm, Weight=${weight.toInt()}kg, Recommended Size=$size"
        } else ""

        // Danh sách brand affiliate hiện có trong Vault
        val affiliateBrands = listOf("Rick Owens", "Nike", "Puma", "Balenciaga", "Rinnsan Creavity")
        val affiliateBrandList = affiliateBrands.joinToString(", ")

        // Build danh sách sản phẩm Vault thực tế để inject vào prompt
        val vaultContext = if (vaultProducts.isNotEmpty()) {
            val lines = vaultProducts.joinToString("\n") { p ->
                val type = when {
                    p.isDirectSale && p.inStock -> "[BÁN TRỰC TIẾP - Còn hàng]"
                    p.isDirectSale && !p.inStock -> "[BÁN TRỰC TIẾP - Hết hàng]"
                    p.affiliateUrl.isNotEmpty() -> "[AFFILIATE]"
                    else -> "[SẢN PHẨM]"
                }
                val priceStr = if (p.price.isNotEmpty()) " | ${p.price}" else ""
                "• ${p.title}$priceStr $type"
            }
            """

═══ KHO HÀNG VAULT — CÁC SẢN PHẨM ĐANG HIỂN THỊ ═══
Các sản phẩm dưới đây đang được hiển thị ở cuối màn hình chat. Đây là kết quả tìm kiếm chính xác từ hệ thống:
$lines

QUY TẮC AFFILIATE — BẮT BUỘC TUÂN THỦ:
1. CHỈ được nhắc đến tên sản phẩm CÓ TRONG DANH SÁCH trên khi tư vấn cụ thể
2. Đối với [BÁN TRỰC TIẾP]: dùng cụm "có thể mua ngay trong app"
3. Đối với [AFFILIATE]: dùng cụm "xem thêm qua link đối tác bên dưới"
4. KHÔNG được tự bịa thêm sản phẩm, tên model, hay link ngoài danh sách trên
5. Có thể tư vấn về phong cách/styling tổng quát mà không cần nhắc tên sản phẩm cụ thể nếu không chắc""".trimIndent()
        } else {
            // Khi không có sản phẩm Vault → nhắc khéo về brand hợp tác
            """

═══ GHI CHÚ AFFILIATE ═══
Rinnsan Creavity hiện đang hợp tác với các brand: $affiliateBrandList
• Nếu user hỏi về brand KHÔNG có trong danh sách trên: tư vấn bình thường về phong cách/styling của brand đó, KHÔNG cần đề xuất sản phẩm cụ thể
• Có thể thêm tự nhiên vào cuối: "Rinnsan Creavity hiện có một số sản phẩm từ [brand phù hợp trong list] nếu bạn muốn xem thêm"
• KHÔNG bịa đặt sản phẩm hay link khi không có data""".trimIndent()
        }

        return """
Bạn là CREAVITY — AI fashion director của Rinnsan Creavity, nền tảng thời trang streetwear & avant-garde tại Việt Nam.
Bạn KHÔNG phải chatbot thông thường. Bạn là creative director với kiến thức thời trang sâu rộng, gu thẩm mỹ editorial, và am hiểu văn hóa Việt.

═══ HỒ SƠ NGƯỜI DÙNG ═══
Archetype: ${archetype.displayName} (${archetype.name})
Style DNA: $styleFocus$hybridNote$bodyNote
Độ tin cậy phân tích: ${(profile.confidenceLevel * 100).toInt()}%$vaultContext

═══ NHÂN CÁCH CỦA BẠN ═══
• Tên: CREAVITY
• Vai trò: Virtual Stylist & Creative Director
• Giọng điệu: Tự tin, am hiểu, mang tính editorial — như một editor tạp chí thời trang cũng là người bạn thân chill
• Nói chuyện tự nhiên, có cá tính — không cứng nhắc, không chung chung
• Có chính kiến nhưng tôn trọng gu của người dùng

═══ QUY TẮc TRẢ LỜI ═══

1. CẤU TRÚC câu trả lời rõ ràng:
   • Mở đầu bằng insight trực tiếp hoặc nhận định chính
   • Hỗ trợ bằng chi tiết cụ thể: tên item, màu sắc, brand
   • Kết thúc bằng gợi ý hành động hoặc câu hỏi follow-up khi phù hợp

2. ĐỘ DÀI câu trả lời:
   • Câu hỏi ngắn (chào hỏi, cảm ơn) → 1-2 câu, ngắn gọn
   • Tư vấn phống cách → 3-5 câu, có tên item/màu sắc/brand cụ thể
   • Phân tích xu hướng → có cấu trúc với bullet points hoặc danh sách
   • Chủ đề phức tạp → chia section rõ ràng, dùng ngắc dòng
   • KHÔNG bao giờ dừng câu giữa chừng — luôn hoàn thành ý trước khi kết thúc

3. LUÔN cá nhân hóa theo archetype ${archetype.name}:
   • Liên hệ Style DNA trong mọi gợi ý
   • Đề xuất items phù hợp với aesthetic của họ
   • Kết nối cultural reference với identity của người dùng

4. KIẾN THỨC THỜI TRANG cần vận dụng:
   • Xu hướng runway FW25/SS26 hiện tại
   • Streetwear culture (Japanese, Korean, Vietnamese street style)
   • Avant-garde designers (Rick Owens, Comme des Garçons, Maison Margiela)
   • Accessible brands (Nike, Puma, Uniqlo, COS, local Vietnamese brands)
   • Bối cảnh thời trang Việt Nam: khí hậu nhiệt đới, văn hóa đường phố Sài Gòn/Hà Nội, thương hiệu địa phương

5. FLOW HỘI THOẠI:
   • Nhớ ngữ cảnh từ các tin nhắn trước
   • Xây dựng tiếp trên gợi ý trước đó, không lặp lại
   • Nếu câu hỏi mơ hồ: đưa ra câu trả lời cụ thể rồi hỏi follow-up để làm rõ
   • Không bao giờ lặp lại cùng một lời khuyên trong một cuộc hội thoại

6. NGÔN NGỮ — ĐÂY LÀ QUY TẮc QUAN TRỌNG NHẤT:
   • Người dùng nhắn tiếng Việt → BẮc BUỘC trả lời hoàn toàn bằng tiếng Việt
   • Người dùng nhắn tiếng Anh → trả lời bằng tiếng Anh
   • Khi nói tiếng Việt: dùng tiếng Việt tự nhiên, hiện đại — không quá trang trọng
   • Thuật ngữ thời trang chuyên ngành (như "techwear", "gorpcore", "silhouette") có thể giữ nguyên tiếng Anh, KHÔNG cần dịch
   • KHÔNG MIX tiếng Anh vào câu tiếng Việt trừ thuật ngữ chuyên ngành

7. GIỚI HẠN:
   • Nếu được hỏi về chủ đề ngoài thời trang (toán, lập trình, chính trị): thừa nhận ngắn gọn rồi redirect: "Dồ ngoài chuyên môn của tôi, nhưng về style thì..."
   • Không thuyết giáo, không lặp từ, không dùng câu mở đầu sáo rỗng như "Câu hỏi hay đấy!"

═══ ĐỊNH DẠNG ═══
• Dùng ngắc dòng giữa các ý để dễ đọc
• TUYỆT ĐỐI KHÔNG dùng Markdown (như **, *, #, -). Chỉ dùng văn bản thuần túy (plain text) vì hệ thống không hiển thị được Markdown.
• Đoạn văn ngắn (tối đa 2-3 câu)
• Emoji: dùng tiết kiệm, chỉ emoji liên quan thời trang nếu cần
        """.trimIndent()
    }


    // buildUserPromptV3 đã được loại bỏ — history giờ được xử lý
    // trực tiếp trong callGeminiAPI() qua multi-turn format

    private fun getArchetypeStyleFocus(archetype: Archetype): String = when (archetype) {
        Archetype.OPERATOR -> "Function-first, technical fabrics, tactical precision"
        Archetype.GHOST    -> "Minimalist, monochrome, refined silhouettes"
        Archetype.GLITCH   -> "Experimental, asymmetric, creative disruption"
        Archetype.NOMAD    -> "Vintage, sustainable, layered storytelling"
    }

    // ──────────────────────────────────────────────────────────────
    // FALLBACK
    // ──────────────────────────────────────────────────────────────

    private fun getIntelligentFallback(profile: IdentityProfile, userMessage: String): String {
        val isVi = detectVietnamese(userMessage)
        val archetype = profile.dominantArchetype

        // Trả lời hữu ích dựa trên archetype thay vì chỉ nói "thử lại"
        val tips = when (archetype) {
            Archetype.GHOST -> if (isVi) {
                "Hiện tại kết nối AI đang tạm gián đoạn. Nhưng với phong cách GHOST của bạn — tập trung vào tông monochrome, silhouette sạch sẽ, chất liệu cao cấp. Những brand như COS, Lemaire, Jil Sander sẽ rất hợp. Thử hỏi lại sau vài phút nhé!"
            } else {
                "AI connection is temporarily busy. For your GHOST aesthetic — focus on monochrome palettes, clean silhouettes, premium fabrics. Brands like COS, Lemaire, Jil Sander align perfectly. Try again in a moment!"
            }
            Archetype.OPERATOR -> if (isVi) {
                "Kết nối AI đang bận. Với phong cách OPERATOR — hãy tìm những items functional, chất liệu technical, thiết kế tactical. Nghĩ đến ACRONYM, Stone Island, Nike ACG. Quay lại sau vài phút nhé!"
            } else {
                "AI connection is busy. For your OPERATOR style — seek functional pieces, technical fabrics, tactical design. Think ACRONYM, Stone Island, Nike ACG. Try again shortly!"
            }
            Archetype.GLITCH -> if (isVi) {
                "AI đang quá tải tạm thời. Nhưng GLITCH như bạn — hãy tìm những thứ phá vỡ quy tắc: asymmetric cuts, pattern clashing, layering bất ngờ. Comme des Garçons, Margiela, Rick Owens là kim chỉ nam. Thử lại sau nhé!"
            } else {
                "AI is temporarily overloaded. For your GLITCH energy — break rules: asymmetric cuts, pattern clashing, unexpected layering. Comme des Garçons, Margiela, Rick Owens are your compass. Try again soon!"
            }
            Archetype.NOMAD -> if (isVi) {
                "Kết nối AI tạm gián đoạn. Với NOMAD spirit — vintage remix, thrift culture, layered storytelling. Mix đồ secondhand với pieces hiện đại, tạo narrative riêng. Kapital, visvim, hoặc local thrift sẽ phù hợp. Hỏi lại sau nhé!"
            } else {
                "AI connection interrupted. For your NOMAD spirit — vintage remix, thrift culture, layered storytelling. Mix secondhand with modern pieces. Kapital, visvim, or local thrift shops align with your aesthetic. Try again soon!"
            }
        }
        return tips
    }

    private fun detectVietnamese(text: String): Boolean {
        val t = text.lowercase()
        val diacritics = "àáảãạăắằẳẵặâấầẩẫậèéẻẽẹêếềểễệìíỉĩịòóỏõọôốồổỗộơớờởỡợùúủũụưứừửữựỳýỷỹỵđ"
        if (t.any { it in diacritics }) return true
        return listOf("không", "khong", "được", "duoc", "cửa hàng", "mặc gì", "phối đồ")
            .any { t.contains(it) }
    }

    // ──────────────────────────────────────────────────────────────
    // RETRY + RATE LIMIT
    // ──────────────────────────────────────────────────────────────

    private suspend fun applyRateLimiting() {
        val now = System.currentTimeMillis()
        val elapsed = now - lastRequestTime
        if (elapsed < minRequestInterval) delay(minRequestInterval - elapsed)
        lastRequestTime = System.currentTimeMillis()
    }

    private suspend fun <T> retryWithBackoff(
        maxAttempts: Int,
        onAttempt: suspend (Int) -> T,
        onFallback: suspend () -> T
    ): T {
        for (attempt in 1..maxAttempts) {
            try {
                return onAttempt(attempt)
            } catch (e: GeminiException.InvalidApiKey) {
                // Không retry — key sai thì retry cũng vô ích
                Log.e(TAG, "✗ Non-retryable: Invalid API Key")
                throw e
            } catch (e: GeminiException.QuotaExceeded) {
                // Không retry — hết quota thì retry trong session vô nghĩa
                Log.e(TAG, "✗ Non-retryable: Quota exceeded")
                throw e
            } catch (e: GeminiException.SafetyBlocked) {
                Log.e(TAG, "✗ Non-retryable: Safety blocked")
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "✗ Attempt $attempt failed: ${e.message}")
                if (attempt < maxAttempts) {
                    val backoffMs = 1000L * attempt
                    Log.d(TAG, "⏳ Waiting ${backoffMs}ms before retry...")
                    delay(backoffMs)
                }
            }
        }
        return onFallback()
    }

    companion object {
        private const val TAG = "GeminiRepo"
    }
}

// ─── Data classes (Serializable cho Ktor) ─────────────────────────

@Serializable
data class GeminiRequest(
    val contents: List<Content>,
    // FIX #1: systemInstruction field — Gemini API nhận đúng system role
    @SerialName("system_instruction")
    val systemInstruction: SystemInstruction? = null,
    @SerialName("generation_config")
    val generationConfig: GenerationConfig? = null
)

@Serializable
data class SystemInstruction(
    val parts: List<Part>
)

@Serializable
data class Content(val role: String, val parts: List<Part>)

@Serializable
data class Part(val text: String)

@Serializable
data class GenerationConfig(
    val temperature: Double? = null,
    @SerialName("top_k")
    val topK: Int? = null,
    @SerialName("top_p")
    val topP: Double? = null,
    @SerialName("max_output_tokens")
    val maxOutputTokens: Int? = null
)

@Serializable
data class GeminiResponse(
    val candidates: List<Candidate>? = null,
    @SerialName("prompt_feedback")
    val promptFeedback: PromptFeedback? = null
)

@Serializable
data class Candidate(
    val content: Content? = null,
    @SerialName("finish_reason")
    val finishReason: String? = null,
    @SerialName("safety_ratings")
    val safetyRatings: List<SafetyRating>? = null
)

@Serializable
data class PromptFeedback(
    @SerialName("safety_ratings")
    val safetyRatings: List<SafetyRating>? = null
)

@Serializable
data class SafetyRating(val category: String, val probability: String)