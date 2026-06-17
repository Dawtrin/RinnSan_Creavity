package com.rinnsan.creavity.domain.models

import java.util.UUID

/**
 * ═══════════════════════════════════════════════════════════════════
 * QUESTION & ANSWER MODELS
 * ═══════════════════════════════════════════════════════════════════
 */

/**
 * QuestionCategory
 *
 * Categories for balanced question distribution:
 * - AESTHETIC: Visual preferences, color, silhouette
 * - BEHAVIOR: Shopping habits, wear frequency
 * - VALUES: Sustainability, brand perception
 * - CONTEXT: Where/how/why they wear clothes
 */
enum class QuestionCategory {
    AESTHETIC,
    BEHAVIOR,
    VALUES,
    CONTEXT
}

/**
 * Question
 *
 * Represents a single question in the identity scanner.
 */
data class Question(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val answers: List<Answer>,
    val weight: Float = 1.0f,
    val category: QuestionCategory? = null,
    val version: Int = 1
) {
    init {
        require(answers.size in 2..6) {
            "Question must have 2-6 answers"
        }
        require(weight > 0f) {
            "Question weight must be positive"
        }
    }
}

/**
 * Answer
 *
 * Each answer maps to archetype scores (normalized).
 */
data class Answer(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val archetypeScores: Map<Archetype, Float>,
    val imageUrl: String? = null
) {
    init {
        require(archetypeScores.isNotEmpty()) {
            "Answer must have at least one archetype score"
        }
        require(archetypeScores.values.all { it >= 0f }) {
            "All scores must be non-negative"
        }
        val sum = archetypeScores.values.sum()
        require(sum > 0f) {
            "Total archetype scores must be positive"
        }
    }

    /**
     * Get normalized scores (sum = 1.0)
     */
    fun normalizedScores(): Map<Archetype, Float> {
        val sum = archetypeScores.values.sum()
        return archetypeScores.mapValues { (_, score) -> score / sum }
    }
}

/**
 * QuestionResponse
 *
 * User's response to a question.
 */
data class QuestionResponse(
    val questionId: String,
    val answerId: String,
    val answer: Answer,
    val weight: Float = 1.0f
)