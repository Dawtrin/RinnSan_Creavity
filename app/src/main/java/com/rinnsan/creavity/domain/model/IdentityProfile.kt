package com.rinnsan.creavity.domain.models

import java.util.UUID

/**
 * ═══════════════════════════════════════════════════════════════════
 * IDENTITY PROFILE - THE DATA CONTRACT
 * ═══════════════════════════════════════════════════════════════════
 *
 * IMMUTABLE DATA CONTRACT between Identity Scanner and all other systems.
 *
 * This is the BOUNDARY:
 * - Box A (Identity Scanner): WRITES once, LOCKS, NEVER reads
 * - Box B (Everything else): READS only, NEVER writes
 */
data class IdentityProfile(
    val id: String = UUID.randomUUID().toString(),
    val dominantArchetype: Archetype,
    val scoreMap: Map<Archetype, Float>,
    val confidenceLevel: Float,
    val isHybrid: Boolean,
    val secondaryArchetype: Archetype? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val userId: String? = null,
    val sessionId: String = UUID.randomUUID().toString(),
    val answeredQuestions: List<String> = emptyList(),
    val version: Int = 1
) {

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // VALIDATION
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    init {
        require(confidenceLevel in 0f..1f) {
            "Confidence level must be between 0.0 and 1.0, got $confidenceLevel"
        }

        require(scoreMap.containsKey(dominantArchetype)) {
            "Score map must contain dominant archetype $dominantArchetype"
        }

        require(scoreMap.values.all { it in 0f..1f }) {
            "All scores must be between 0.0 and 1.0"
        }

        if (isHybrid) {
            require(secondaryArchetype != null) {
                "Hybrid profiles must have a secondary archetype"
            }
            require(secondaryArchetype != dominantArchetype) {
                "Secondary archetype must differ from dominant archetype"
            }
        }

        require(version > 0) {
            "Version must be positive, got $version"
        }
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // READ-ONLY ACCESS METHODS
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    /**
     * Get formatted archetype label.
     * Examples: "THE GHOST", "THE GHOST × THE NOMAD"
     */
    fun getArchetypeLabel(): String {
        return if (isHybrid && secondaryArchetype != null) {
            "${dominantArchetype.displayName} × ${secondaryArchetype.displayName}"
        } else {
            dominantArchetype.displayName
        }
    }

    /**
     * Get short label without "THE" prefix.
     * Examples: "GHOST", "GHOST × NOMAD"
     */
    fun getShortLabel(): String {
        return if (isHybrid && secondaryArchetype != null) {
            "${dominantArchetype.name} × ${secondaryArchetype.name}"
        } else {
            dominantArchetype.name
        }
    }

    /**
     * Get profile age in days.
     */
    fun getAgeInDays(): Long {
        val now = System.currentTimeMillis()
        return (now - timestamp) / (1000 * 60 * 60 * 24)
    }

    /**
     * Check if profile should be refreshed (default: 90 days).
     */
    fun shouldRefresh(maxAgeDays: Long = 90): Boolean {
        return getAgeInDays() > maxAgeDays
    }

    /**
     * Get sorted archetypes by score (descending).
     */
    fun getSortedArchetypes(): List<Pair<Archetype, Float>> {
        return scoreMap.entries
            .sortedByDescending { it.value }
            .map { it.key to it.value }
    }

    /**
     * Get score for specific archetype.
     */
    fun getScore(archetype: Archetype): Float {
        return scoreMap[archetype] ?: 0f
    }

    /**
     * Get confidence as percentage (0-100).
     */
    fun getConfidencePercentage(): Int {
        return (confidenceLevel * 100).toInt()
    }

    /**
     * Check if profile is anonymous (no user ID).
     */
    fun isAnonymous(): Boolean {
        return userId == null
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // SERIALIZATION HELPERS
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    /**
     * Convert to map for JSON serialization.
     */
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "dominant_archetype" to dominantArchetype.name,
            "score_map" to scoreMap.mapKeys { it.key.name },
            "confidence_level" to confidenceLevel,
            "is_hybrid" to isHybrid,
            "secondary_archetype" to secondaryArchetype?.name,
            "timestamp" to timestamp,
            "user_id" to userId,
            "session_id" to sessionId,
            "answered_questions" to answeredQuestions,
            "version" to version
        )
    }

    companion object {
        /**
         * Empty/invalid profile for error states.
         */
        val EMPTY = IdentityProfile(
            dominantArchetype = Archetype.GHOST,
            scoreMap = Archetype.entries.associateWith { 0f },
            confidenceLevel = 0f,
            isHybrid = false,
            secondaryArchetype = null
        )

        /**
         * Create from serialized map.
         */
        fun fromMap(map: Map<String, Any?>): IdentityProfile? {
            return try {
                val dominantArchetypeName = map["dominant_archetype"] as? String
                    ?: return null
                val dominantArchetype = Archetype.fromName(dominantArchetypeName)
                    ?: return null

                @Suppress("UNCHECKED_CAST")
                val scoreMapRaw = map["score_map"] as? Map<String, Any?>
                    ?: return null
                val scoreMap = scoreMapRaw.mapNotNull { (key, value) ->
                    val archetype = Archetype.fromName(key) ?: return@mapNotNull null
                    val score = (value as? Number)?.toFloat() ?: return@mapNotNull null
                    archetype to score
                }.toMap()

                val confidenceLevel = (map["confidence_level"] as? Number)?.toFloat()
                    ?: return null

                val isHybrid = map["is_hybrid"] as? Boolean ?: false

                val secondaryArchetypeName = map["secondary_archetype"] as? String
                val secondaryArchetype = secondaryArchetypeName?.let {
                    Archetype.fromName(it)
                }

                @Suppress("UNCHECKED_CAST")
                val answeredQuestions = (map["answered_questions"] as? List<String>)
                    ?: emptyList()

                IdentityProfile(
                    id = map["id"] as? String ?: UUID.randomUUID().toString(),
                    dominantArchetype = dominantArchetype,
                    scoreMap = scoreMap,
                    confidenceLevel = confidenceLevel,
                    isHybrid = isHybrid,
                    secondaryArchetype = secondaryArchetype,
                    timestamp = (map["timestamp"] as? Number)?.toLong()
                        ?: System.currentTimeMillis(),
                    userId = map["user_id"] as? String,
                    sessionId = map["session_id"] as? String
                        ?: UUID.randomUUID().toString(),
                    answeredQuestions = answeredQuestions,
                    version = (map["version"] as? Number)?.toInt() ?: 1
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}