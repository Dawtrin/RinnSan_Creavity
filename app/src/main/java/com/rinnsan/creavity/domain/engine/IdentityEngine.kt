package com.rinnsan.creavity.domain.engine

import com.rinnsan.creavity.domain.models.*

/**
 * ═══════════════════════════════════════════════════════════════════
 * IDENTITY ENGINE - COMPUTATION LOGIC
 * ═══════════════════════════════════════════════════════════════════
 *
 * Philosophy:
 * This is the ONLY place where IdentityProfile is created.
 * Single source of truth for identity computation.
 *
 * Algorithm:
 * 1. Accumulate weighted scores from all responses
 * 2. Normalize scores to 0.0-1.0 range
 * 3. Identify dominant archetype (highest score)
 * 4. Calculate confidence based on gap between #1 and #2
 * 5. Determine hybrid status
 * 6. Lock profile with timestamp
 */
object IdentityEngine {

    private const val HYBRID_THRESHOLD = 0.60f // Dominant must be >60% to be pure

    /**
     * Compute IdentityProfile from user responses.
     *
     * @param responses List of user's answers to questions
     * @param userId Optional user ID for logged-in users
     * @return Computed IdentityProfile
     */
    fun computeIdentity(
        responses: List<QuestionResponse>,
        userId: String? = null
    ): IdentityProfile {
        if (responses.isEmpty()) {
            return IdentityProfile.EMPTY
        }

        // Step 1: Accumulate weighted scores
        val rawScores = accumulateScores(responses)

        if (rawScores.isEmpty() || rawScores.values.sum() == 0f) {
            return IdentityProfile.EMPTY
        }

        // Step 2: Normalize scores to 0.0-1.0
        val normalizedScores = normalizeScores(rawScores)

        // Step 3: Sort archetypes by score (descending)
        val sortedScores = normalizedScores.entries
            .sortedByDescending { it.value }
            .toList()

        val dominantArchetype = sortedScores[0].key
        val dominantScore = sortedScores[0].value

        val secondaryArchetype = sortedScores.getOrNull(1)?.key
        val secondaryScore = sortedScores.getOrNull(1)?.value ?: 0f

        // Step 4: Calculate confidence (gap between #1 and #2)
        val confidence = calculateConfidence(dominantScore, secondaryScore)

        // Step 5: Determine hybrid status
        val isHybrid = dominantScore < HYBRID_THRESHOLD

        // Step 6: Extract answered question IDs
        val answeredQuestions = responses.map { it.questionId }

        // Step 7: Create profile
        return IdentityProfile(
            dominantArchetype = dominantArchetype,
            scoreMap = normalizedScores,
            confidenceLevel = confidence,
            isHybrid = isHybrid,
            secondaryArchetype = if (isHybrid) secondaryArchetype else null,
            timestamp = System.currentTimeMillis(),
            userId = userId,
            answeredQuestions = answeredQuestions
        )
    }

    /**
     * Accumulate raw scores from responses.
     *
     * Each answer contributes to multiple archetypes with weights.
     */
    private fun accumulateScores(
        responses: List<QuestionResponse>
    ): Map<Archetype, Float> {
        val scores = mutableMapOf<Archetype, Float>()

        for (response in responses) {
            val answer = response.answer
            val questionWeight = response.weight

            // Get normalized scores from answer
            val answerScores = answer.normalizedScores()

            // Accumulate weighted scores
            for ((archetype, score) in answerScores) {
                val weightedScore = score * questionWeight
                scores[archetype] = scores.getOrDefault(archetype, 0f) + weightedScore
            }
        }

        return scores
    }

    /**
     * Normalize scores to 0.0-1.0 range (sum = 1.0).
     */
    private fun normalizeScores(
        rawScores: Map<Archetype, Float>
    ): Map<Archetype, Float> {
        val totalScore = rawScores.values.sum()

        if (totalScore == 0f) {
            // All zeros - return equal distribution
            return Archetype.entries.associateWith { 0.25f }
        }

        return rawScores.mapValues { (_, score) ->
            score / totalScore
        }
    }

    /**
     * Calculate confidence based on gap between top 2 scores.
     *
     * High confidence = clear winner
     * Low confidence = close race
     */
    private fun calculateConfidence(
        topScore: Float,
        secondScore: Float
    ): Float {
        // Gap method: larger gap = higher confidence
        val gap = topScore - secondScore

        // Normalize to 0.0-1.0
        // Gap of 0.0 → confidence 0.0
        // Gap of 1.0 → confidence 1.0
        return gap.coerceIn(0f, 1f)
    }

    /**
     * Format profile as diagnostic signal.
     */
    fun formatSignal(profile: IdentityProfile): String {
        return buildString {
            appendLine("═══════════════════════════════════════")
            appendLine("IDENTITY_ACQUIRED")
            appendLine("═══════════════════════════════════════")
            appendLine("ARCHETYPE: ${profile.getArchetypeLabel()}")
            appendLine("CONFIDENCE: ${profile.getConfidencePercentage()}%")

            if (profile.isHybrid) {
                appendLine("STATUS: HYBRID_DETECTED")
            } else {
                appendLine("STATUS: PURE_ARCHETYPE")
            }

            appendLine("───────────────────────────────────────")
            appendLine("SCORE BREAKDOWN:")
            profile.getSortedArchetypes().forEach { (archetype, score) ->
                val percentage = (score * 100).toInt()
                val bar = "█".repeat(percentage / 5)
                appendLine("${"%-12s".format(archetype.name)} ${"%-20s".format(bar)} $percentage%")
            }

            appendLine("───────────────────────────────────────")
            appendLine("TIMESTAMP: ${profile.timestamp}")
            appendLine("SESSION_ID: ${profile.sessionId}")
            appendLine("═══════════════════════════════════════")
        }
    }
}