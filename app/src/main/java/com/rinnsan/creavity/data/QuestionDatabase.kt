package com.rinnsan.creavity.data

import com.rinnsan.creavity.domain.models.*

/**
 * ═══════════════════════════════════════════════════════════════════
 * QUESTION DATABASE - IDENTITY SCANNER v2.0
 * ═══════════════════════════════════════════════════════════════════
 *
 * 15 Questions | 4 Categories | Poetic prompts × Fashion-real answers
 * Images via Unsplash (auto-resized at 600×800 for Grid 2-col perf)
 *
 * Distribution:
 *  AESTHETIC  → q1, q4, q5, q9, q12
 *  BEHAVIOR   → q2, q6, q8, q11, q14
 *  VALUES     → q3, q7, q10, q13, q15
 *  CONTEXT    → (integrated via weight calibration)
 */

val IDENTITY_QUESTIONS = listOf(

    // ─────────────────────────────────────────────────────────────
    // Q1 | AESTHETIC | weight 1.5 — HIGH SIGNAL
    // ─────────────────────────────────────────────────────────────
    Question(
        id = "q1",
        text = "CORE SILHOUETTE",
        answers = listOf(
            Answer(
                text = "Invisible\nPhom tối giản, all-black, cắt may chính xác",
                archetypeScores = mapOf(Archetype.GHOST to 1.0f),
                imageUrl = "https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?w=600&h=800&fit=crop&q=70"
            ),
            Answer(
                text = "Engineered\nPhom structured, vai vuông, kỹ thuật may cao",
                archetypeScores = mapOf(Archetype.OPERATOR to 1.0f),
                imageUrl = "https://images.unsplash.com/photo-1594938298603-c8148c4dae35?w=600&h=800&fit=crop&q=70"
            ),
            Answer(
                text = "Deconstructed\nPhom bất đối xứng, đường may lộ, raw hem",
                archetypeScores = mapOf(Archetype.GLITCH to 1.0f),
                imageUrl = "https://images.unsplash.com/photo-1509631179647-0177331693ae?w=600&h=800&fit=crop&q=70"
            ),
            Answer(
                text = "Layered\nNhiều lớp vải, phom rộng, dễ tháo lắp",
                archetypeScores = mapOf(Archetype.NOMAD to 1.0f),
                imageUrl = "https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=600&h=800&fit=crop&q=70"
            )
        ),
        weight = 1.5f,
        category = QuestionCategory.AESTHETIC
    ),

    // ─────────────────────────────────────────────────────────────
    // Q2 | BEHAVIOR | weight 1.2
    // ─────────────────────────────────────────────────────────────
    Question(
        id = "q2",
        text = "YOU MOVE THROUGH SPACES",
        answers = listOf(
            Answer(
                text = "Like smoke\nGiày soft-sole, vải không phát tiếng",
                archetypeScores = mapOf(Archetype.GHOST to 0.7f, Archetype.NOMAD to 0.3f),
                imageUrl = "https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=600&h=800&fit=crop&q=70"
            ),
            Answer(
                text = "Like code\nOxford shoes, trench coat, mọi thứ có chức năng",
                archetypeScores = mapOf(Archetype.OPERATOR to 0.7f, Archetype.GLITCH to 0.3f),
                imageUrl = "https://images.unsplash.com/photo-1549298916-b41d501d3772?w=600&h=800&fit=crop&q=70"
            ),
            Answer(
                text = "Like static\nChunky sneakers, loud print, không thể bỏ qua",
                archetypeScores = mapOf(Archetype.GLITCH to 1.0f),
                imageUrl = "https://images.unsplash.com/photo-1595950653106-6c9ebd614d3a?w=600&h=800&fit=crop&q=70"
            ),
            Answer(
                text = "Like wind\nSandals, vải linen, ba lô nhỏ gọn",
                archetypeScores = mapOf(Archetype.NOMAD to 0.7f, Archetype.GHOST to 0.3f),
                imageUrl = "https://images.unsplash.com/photo-1553361371-9b22f78e8b1d?w=600&h=800&fit=crop&q=70"
            )
        ),
        weight = 1.2f,
        category = QuestionCategory.BEHAVIOR
    ),

    // ─────────────────────────────────────────────────────────────
    // Q3 | VALUES | weight 1.3
    // ─────────────────────────────────────────────────────────────
    Question(
        id = "q3",
        text = "YOUR WEAPON IS",
        answers = listOf(
            Answer(
                text = "Invisibility\nKhông trend, không logo, không cần chú ý",
                archetypeScores = mapOf(Archetype.GHOST to 1.0f),
                imageUrl = "https://images.unsplash.com/photo-1490481651871-ab68de25d43d?w=600&h=800&fit=crop&q=70"
            ),
            Answer(
                text = "Information\nChất liệu premium, cắt may nói lên tất cả",
                archetypeScores = mapOf(Archetype.OPERATOR to 1.0f),
                imageUrl = "https://images.unsplash.com/photo-1441986300917-64674bd600d8?w=600&h=800&fit=crop&q=70"
            ),
            Answer(
                text = "Chaos\nMix pattern táo bạo, clash màu có chủ đích",
                archetypeScores = mapOf(Archetype.GLITCH to 1.0f),
                imageUrl = "https://images.unsplash.com/photo-1483985988355-763728e1935b?w=600&h=800&fit=crop&q=70"
            ),
            Answer(
                text = "Freedom\nHandmade details, vintage finds, kể câu chuyện",
                archetypeScores = mapOf(Archetype.NOMAD to 1.0f),
                imageUrl = "https://images.unsplash.com/photo-1469334031218-e382a71b716b?w=600&h=800&fit=crop&q=70"
            )
        ),
        weight = 1.3f,
        category = QuestionCategory.VALUES
    ),

    // ─────────────────────────────────────────────────────────────
    // Q4 | AESTHETIC | weight 1.4
    // ─────────────────────────────────────────────────────────────
    Question(
        id = "q4",
        text = "FABRIC AS ARMOR",
        answers = listOf(
            Answer(
                text = "Minimal, tactical\nNylon kỹ thuật, microfiber mỏng nhẹ",
                archetypeScores = mapOf(Archetype.GHOST to 0.6f, Archetype.OPERATOR to 0.4f),
                imageUrl = "https://images.unsplash.com/photo-1620799140408-edc6dcb6d633?w=600&h=800&fit=crop&q=70"
            ),
            Answer(
                text = "Structured, engineered\nWool blend, canvas cứng, vải có memory",
                archetypeScores = mapOf(Archetype.OPERATOR to 1.0f),
                imageUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=600&h=800&fit=crop&q=70"
            ),
            Answer(
                text = "Deconstructed, raw\nDenim rách, vải chắp vá, texture thô",
                archetypeScores = mapOf(Archetype.GLITCH to 0.6f, Archetype.NOMAD to 0.4f),
                imageUrl = "https://images.unsplash.com/photo-1572635196237-14b3f281503f?w=600&h=800&fit=crop&q=70"
            ),
            Answer(
                text = "Layered, adaptive\nLinen, cotton hữu cơ, vải tự nhiên breathable",
                archetypeScores = mapOf(Archetype.NOMAD to 1.0f),
                imageUrl = "https://images.unsplash.com/photo-1434389677669-e08b4cac3105?w=600&h=800&fit=crop&q=70"
            )
        ),
        weight = 1.4f,
        category = QuestionCategory.AESTHETIC
    ),

    // ─────────────────────────────────────────────────────────────
    // Q5 | AESTHETIC | weight 1.5 — HIGH SIGNAL
    // ─────────────────────────────────────────────────────────────
    Question(
        id = "q5",
        text = "YOUR COLOR SIGNAL",
        answers = listOf(
            Answer(
                text = "Void / Monochrome\nAll-black, charcoal, deep navy",
                archetypeScores = mapOf(Archetype.GHOST to 1.0f),
                imageUrl = "https://images.unsplash.com/photo-1523381210434-271e8be1f52b?w=600&h=800&fit=crop&q=70"
            ),
            Answer(
                text = "Tech / Metallic\nSilver, slate grey, cold white, chrome",
                archetypeScores = mapOf(Archetype.OPERATOR to 1.0f),
                imageUrl = "https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?w=600&h=800&fit=crop&q=70"
            ),
            Answer(
                text = "Acid / Neon\nCyber green, UV pink, electric blue",
                archetypeScores = mapOf(Archetype.GLITCH to 1.0f),
                imageUrl = "https://images.unsplash.com/photo-1576566588028-4147f3842f27?w=600&h=800&fit=crop&q=70"
            ),
            Answer(
                text = "Earth / Organic\nSand, rust, sage, terracotta, off-white",
                archetypeScores = mapOf(Archetype.NOMAD to 1.0f),
                imageUrl = "https://images.unsplash.com/photo-1485231183945-fffde7c13a7d?w=600&h=800&fit=crop&q=70"
            )
        ),
        weight = 1.5f,
        category = QuestionCategory.AESTHETIC
    ),

    // ─────────────────────────────────────────────────────────────
    // Q6 | BEHAVIOR | weight 1.0
    // ─────────────────────────────────────────────────────────────
    Question(
        id = "q6",
        text = "HOW YOU SHOP",
        answers = listOf(
            Answer(
                text = "In echoes\nChỉ mua lại những gì đã mặc tốt trước đây",
                archetypeScores = mapOf(Archetype.GHOST to 1.0f),
                imageUrl = "https://images.unsplash.com/photo-1441984904996-e0b6ba687e04?w=600&h=800&fit=crop&q=70"
            ),
            Answer(
                text = "In sequences\nResearch kỹ, so sánh giá, mua có kế hoạch",
                archetypeScores = mapOf(Archetype.OPERATOR to 1.0f),
                imageUrl = "https://images.unsplash.com/photo-1472851294608-062f824d29cc?w=600&h=800&fit=crop&q=70"
            ),
            Answer(
                text = "In fragments\nImpulse buy, thrift haul, mix thứ không ai nghĩ tới",
                archetypeScores = mapOf(Archetype.GLITCH to 1.0f),
                imageUrl = "https://images.unsplash.com/photo-1567401893414-76b7b1e5a7a5?w=600&h=800&fit=crop&q=70"
            ),
            Answer(
                text = "In cycles\nMarket địa phương, thrift store, trao đổi với bạn bè",
                archetypeScores = mapOf(Archetype.NOMAD to 1.0f),
                imageUrl = "https://images.unsplash.com/photo-1445205170230-053b83016050?w=600&h=800&fit=crop&q=70"
            )
        ),
        weight = 1.0f,
        category = QuestionCategory.BEHAVIOR
    ),

    // ─────────────────────────────────────────────────────────────
    // Q7 | VALUES | weight 1.2
    // ─────────────────────────────────────────────────────────────
    Question(
        id = "q7",
        text = "YOU TRUST",
        answers = listOf(
            Answer(
                text = "Nothing\nKhông logo, không trend, tự tạo ngôn ngữ riêng",
                archetypeScores = mapOf(Archetype.GHOST to 1.0f),
                imageUrl = "https://images.unsplash.com/photo-1509631179647-0177331693ae?w=600&h=800&fit=crop&q=70"
            ),
            Answer(
                text = "Systems\nBrand có heritage, chất liệu có chứng nhận",
                archetypeScores = mapOf(Archetype.OPERATOR to 1.0f),
                imageUrl = "https://images.unsplash.com/photo-1490481651871-ab68de25d43d?w=600&h=800&fit=crop&q=70"
            ),
            Answer(
                text = "Instinct\nThấy đúng là mặc, không cần giải thích",
                archetypeScores = mapOf(Archetype.GLITCH to 0.6f, Archetype.NOMAD to 0.4f),
                imageUrl = "https://images.unsplash.com/photo-1483985988355-763728e1935b?w=600&h=800&fit=crop&q=70"
            ),
            Answer(
                text = "Movement\nMặc phù hợp địa điểm, khí hậu, văn hóa",
                archetypeScores = mapOf(Archetype.NOMAD to 1.0f),
                imageUrl = "https://images.unsplash.com/photo-1469334031218-e382a71b716b?w=600&h=800&fit=crop&q=70"
            )
        ),
        weight = 1.2f,
        category = QuestionCategory.VALUES
    ),

    // ─────────────────────────────────────────────────────────────
    // Q8 | BEHAVIOR | weight 1.1
    // ─────────────────────────────────────────────────────────────
    Question(
        id = "q8",
        text = "OUTERWEAR PROTOCOL",
        answers = listOf(
            Answer(
                text = "Silence\nMono-coat tối màu, không detail thừa",
                archetypeScores = mapOf(Archetype.GHOST to 1.0f),
                imageUrl = "https://images.unsplash.com/photo-1434389677669-e08b4cac3105?w=600&h=800&fit=crop&q=70"
            ),
            Answer(
                text = "Binary\nTrench coat hoặc bomber — không có option thứ ba",
                archetypeScores = mapOf(Archetype.OPERATOR to 1.0f),
                imageUrl = "https://images.unsplash.com/photo-1594938298603-c8148c4dae35?w=600&h=800&fit=crop&q=70"
            ),
            Answer(
                text = "Symbols\nPatchwork jacket, graphic oversize, statement piece",
                archetypeScores = mapOf(Archetype.GLITCH to 1.0f),
                imageUrl = "https://images.unsplash.com/photo-1576566588028-4147f3842f27?w=600&h=800&fit=crop&q=70"
            ),
            Answer(
                text = "Stories\nFleece vest, quilted jacket, poncho — theo thời tiết",
                archetypeScores = mapOf(Archetype.NOMAD to 1.0f),
                imageUrl = "https://images.unsplash.com/photo-1485231183945-fffde7c13a7d?w=600&h=800&fit=crop&q=70"
            )
        ),
        weight = 1.1f,
        category = QuestionCategory.BEHAVIOR
    ),

    // ─────────────────────────────────────────────────────────────
    // Q9 | AESTHETIC | weight 1.3
    // ─────────────────────────────────────────────────────────────
    Question(
        id = "q9",
        text = "THE SYSTEM SEES YOU AS",
        answers = listOf(
            Answer(
                text = "Error 404\nKhông trace được, không định nghĩa được",
                archetypeScores = mapOf(Archetype.GHOST to 1.0f),
                imageUrl = "https://images.unsplash.com/photo-1523381210434-271e8be1f52b?w=600&h=800&fit=crop&q=70"
            ),
            Answer(
                text = "Admin\nDress code phù hợp mọi môi trường, luôn đọc phòng",
                archetypeScores = mapOf(Archetype.OPERATOR to 1.0f),
                imageUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=600&h=800&fit=crop&q=70"
            ),
            Answer(
                text = "Corrupted file\nMix era, mix gender, không theo rule nào",
                archetypeScores = mapOf(Archetype.GLITCH to 1.0f),
                imageUrl = "https://images.unsplash.com/photo-1572635196237-14b3f281503f?w=600&h=800&fit=crop&q=70"
            ),
            Answer(
                text = "Unlisted\nKhông có profile rõ ràng — và điều đó là chủ đích",
                archetypeScores = mapOf(Archetype.NOMAD to 1.0f),
                imageUrl = "https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=600&h=800&fit=crop&q=70"
            )
        ),
        weight = 1.3f,
        category = QuestionCategory.AESTHETIC
    ),

    // ─────────────────────────────────────────────────────────────
    // Q10 | VALUES | weight 1.5 — HIGH SIGNAL
    // ─────────────────────────────────────────────────────────────
    Question(
        id = "q10",
        text = "YOUR FINAL FORM",
        answers = listOf(
            Answer(
                text = "Disappear\nCapsuled wardrobe — 10 món, infinite combinations",
                archetypeScores = mapOf(Archetype.GHOST to 1.0f),
                imageUrl = "https://images.unsplash.com/photo-1490481651871-ab68de25d43d?w=600&h=800&fit=crop&q=70"
            ),
            Answer(
                text = "Optimize\nEvery item có ROI rõ ràng, không có deadstock",
                archetypeScores = mapOf(Archetype.OPERATOR to 1.0f),
                imageUrl = "https://images.unsplash.com/photo-1441984904996-e0b6ba687e04?w=600&h=800&fit=crop&q=70"
            ),
            Answer(
                text = "Corrupt\nOutfit challenge social norms — đó là bài phát biểu",
                archetypeScores = mapOf(Archetype.GLITCH to 1.0f),
                imageUrl = "https://images.unsplash.com/photo-1509631179647-0177331693ae?w=600&h=800&fit=crop&q=70"
            ),
            Answer(
                text = "Transcend\nMặc để sống — không để nhìn",
                archetypeScores = mapOf(Archetype.NOMAD to 1.0f),
                imageUrl = "https://images.unsplash.com/photo-1445205170230-053b83016050?w=600&h=800&fit=crop&q=70"
            )
        ),
        weight = 1.5f,
        category = QuestionCategory.VALUES
    ),

    // ─────────────────────────────────────────────────────────────
    // Q11 | BEHAVIOR | weight 1.1
    // ─────────────────────────────────────────────────────────────
    Question(
        id = "q11",
        text = "FOOTWEAR SYSTEM",
        answers = listOf(
            Answer(
                text = "Stealth\nCommon Projects, clean leather, không ai để ý brand",
                archetypeScores = mapOf(Archetype.GHOST to 1.0f),
                imageUrl = "https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=600&h=800&fit=crop&q=70"
            ),
            Answer(
                text = "Precision\nDerby cổ điển, Chelsea boot, đánh xi thường xuyên",
                archetypeScores = mapOf(Archetype.OPERATOR to 1.0f),
                imageUrl = "https://images.unsplash.com/photo-1549298916-b41d501d3772?w=600&h=800&fit=crop&q=70"
            ),
            Answer(
                text = "Overload\nPlatform boots, ugly sneaker, maximalist sole",
                archetypeScores = mapOf(Archetype.GLITCH to 1.0f),
                imageUrl = "https://images.unsplash.com/photo-1595950653106-6c9ebd614d3a?w=600&h=800&fit=crop&q=70"
            ),
            Answer(
                text = "Journey\nBirkenstock, trail runner, sandal handmade",
                archetypeScores = mapOf(Archetype.NOMAD to 1.0f),
                imageUrl = "https://images.unsplash.com/photo-1553361371-9b22f78e8b1d?w=600&h=800&fit=crop&q=70"
            )
        ),
        weight = 1.1f,
        category = QuestionCategory.BEHAVIOR
    ),

    // ─────────────────────────────────────────────────────────────
    // Q12 | AESTHETIC | weight 1.2
    // ─────────────────────────────────────────────────────────────
    Question(
        id = "q12",
        text = "ACCESSORIES AS DATA",
        answers = listOf(
            Answer(
                text = "No data\nKhông đeo gì — hoặc một thứ duy nhất, tối nghĩa",
                archetypeScores = mapOf(Archetype.GHOST to 1.0f),
                imageUrl = "https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?w=600&h=800&fit=crop&q=70"
            ),
            Answer(
                text = "Encrypted\nĐồng hồ cơ học, brief case, bút ký tốt",
                archetypeScores = mapOf(Archetype.OPERATOR to 1.0f),
                imageUrl = "https://images.unsplash.com/photo-1620799140408-edc6dcb6d633?w=600&h=800&fit=crop&q=70"
            ),
            Answer(
                text = "Corrupted\nStack rings, layered necklace, mismatched earrings",
                archetypeScores = mapOf(Archetype.GLITCH to 1.0f),
                imageUrl = "https://images.unsplash.com/photo-1576566588028-4147f3842f27?w=600&h=800&fit=crop&q=70"
            ),
            Answer(
                text = "Collected\nTote bag vải, jewelry thủ công, túi từ chợ địa phương",
                archetypeScores = mapOf(Archetype.NOMAD to 1.0f),
                imageUrl = "https://images.unsplash.com/photo-1434389677669-e08b4cac3105?w=600&h=800&fit=crop&q=70"
            )
        ),
        weight = 1.2f,
        category = QuestionCategory.AESTHETIC
    ),

    // ─────────────────────────────────────────────────────────────
    // Q13 | VALUES | weight 1.3
    // ─────────────────────────────────────────────────────────────
    Question(
        id = "q13",
        text = "GETTING DRESSED IS",
        answers = listOf(
            Answer(
                text = "Ritual\nCùng một quy trình mỗi ngày, không suy nghĩ",
                archetypeScores = mapOf(Archetype.GHOST to 1.0f),
                imageUrl = "https://images.unsplash.com/photo-1523381210434-271e8be1f52b?w=600&h=800&fit=crop&q=70"
            ),
            Answer(
                text = "Strategy\nChọn outfit theo lịch ngày hôm đó",
                archetypeScores = mapOf(Archetype.OPERATOR to 1.0f),
                imageUrl = "https://images.unsplash.com/photo-1441984904996-e0b6ba687e04?w=600&h=800&fit=crop&q=70"
            ),
            Answer(
                text = "Experiment\nThay đổi theo mood — không có ngày nào giống ngày nào",
                archetypeScores = mapOf(Archetype.GLITCH to 1.0f),
                imageUrl = "https://images.unsplash.com/photo-1483985988355-763728e1935b?w=600&h=800&fit=crop&q=70"
            ),
            Answer(
                text = "Adaptation\nMặc theo môi trường, không theo calendar",
                archetypeScores = mapOf(Archetype.NOMAD to 1.0f),
                imageUrl = "https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=600&h=800&fit=crop&q=70"
            )
        ),
        weight = 1.3f,
        category = QuestionCategory.VALUES
    ),

    // ─────────────────────────────────────────────────────────────
    // Q14 | BEHAVIOR | weight 1.2
    // ─────────────────────────────────────────────────────────────
    Question(
        id = "q14",
        text = "WHEN PHOTOGRAPHED",
        answers = listOf(
            Answer(
                text = "Erase\nTránh frame, không muốn bị lưu lại",
                archetypeScores = mapOf(Archetype.GHOST to 1.0f),
                imageUrl = "https://images.unsplash.com/photo-1509631179647-0177331693ae?w=600&h=800&fit=crop&q=70"
            ),
            Answer(
                text = "Calculate\nBiết góc nào đẹp nhất, pose có ý thức",
                archetypeScores = mapOf(Archetype.OPERATOR to 1.0f),
                imageUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=600&h=800&fit=crop&q=70"
            ),
            Answer(
                text = "Perform\nLook vào camera, outfit là main character",
                archetypeScores = mapOf(Archetype.GLITCH to 1.0f),
                imageUrl = "https://images.unsplash.com/photo-1572635196237-14b3f281503f?w=600&h=800&fit=crop&q=70"
            ),
            Answer(
                text = "Exist\nKhông cần pose — context tự nói",
                archetypeScores = mapOf(Archetype.NOMAD to 1.0f),
                imageUrl = "https://images.unsplash.com/photo-1469334031218-e382a71b716b?w=600&h=800&fit=crop&q=70"
            )
        ),
        weight = 1.2f,
        category = QuestionCategory.BEHAVIOR
    ),

    // ─────────────────────────────────────────────────────────────
    // Q15 | VALUES | weight 1.5 — HIGH SIGNAL (FINAL)
    // ─────────────────────────────────────────────────────────────
    Question(
        id = "q15",
        text = "TRANSMISSION COMPLETE // FINAL INPUT",
        answers = listOf(
            Answer(
                text = "Leave no trace\nPhong cách là quyền lực khi không ai biết bạn mặc gì",
                archetypeScores = mapOf(Archetype.GHOST to 1.0f),
                imageUrl = "https://images.unsplash.com/photo-1490481651871-ab68de25d43d?w=600&h=800&fit=crop&q=70"
            ),
            Answer(
                text = "Execute perfectly\nMỗi outfit là một quyết định — không có lỗi nào được phép",
                archetypeScores = mapOf(Archetype.OPERATOR to 1.0f),
                imageUrl = "https://images.unsplash.com/photo-1594938298603-c8148c4dae35?w=600&h=800&fit=crop&q=70"
            ),
            Answer(
                text = "Break the signal\nThời trang là ngôn ngữ — tôi đang phá vỡ ngữ pháp",
                archetypeScores = mapOf(Archetype.GLITCH to 1.0f),
                imageUrl = "https://images.unsplash.com/photo-1576566588028-4147f3842f27?w=600&h=800&fit=crop&q=70"
            ),
            Answer(
                text = "Keep moving\nKhông cần định nghĩa — chỉ cần tiếp tục",
                archetypeScores = mapOf(Archetype.NOMAD to 1.0f),
                imageUrl = "https://images.unsplash.com/photo-1485231183945-fffde7c13a7d?w=600&h=800&fit=crop&q=70"
            )
        ),
        weight = 1.5f,
        category = QuestionCategory.VALUES
    )
)

/**
 * Get question by ID.
 */
fun getQuestionById(id: String): Question? {
    return IDENTITY_QUESTIONS.find { it.id == id }
}

/**
 * Get questions by category.
 */
fun getQuestionsByCategory(category: QuestionCategory): List<Question> {
    return IDENTITY_QUESTIONS.filter { it.category == category }
}