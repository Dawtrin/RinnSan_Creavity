package com.rinnsan.creavity.domain.models

/**
 * ═══════════════════════════════════════════════════════════════════
 * ARCHETYPE - FASHION IDENTITY TAXONOMY
 * ═══════════════════════════════════════════════════════════════════
 *
 * Philosophy:
 * Based on two axes:
 * - Visibility (HIDE ←→ SHOW)
 * - System (CONFORM ←→ REBEL)
 *
 * GHOST: Hide + Conform = Minimalist, invisible, monochrome
 * OPERATOR: Show + Conform = Functional luxury, techwear, tactical
 * GLITCH: Show + Rebel = Maximalist chaos, pattern clash, asymmetry
 * NOMAD: Hide + Rebel = Vintage remix, thrift culture, anti-brand
 */
enum class Archetype(
    val displayName: String,
    val tagline: String,
    val hexCode: String
) {
    GHOST(
        displayName = "THE GHOST",
        tagline = "I disappear into the architecture",
        hexCode = "#0A0A0A"
    ),

    OPERATOR(
        displayName = "THE OPERATOR",
        tagline = "Function is my fashion statement",
        hexCode = "#E0E0E0"
    ),

    GLITCH(
        displayName = "THE GLITCH",
        tagline = "I am the beautiful error",
        hexCode = "#C0FF00"
    ),

    NOMAD(
        displayName = "THE NOMAD",
        tagline = "I remix what already exists",
        hexCode = "#00D9FF"
    );

    companion object {
        fun fromOrdinal(ordinal: Int): Archetype? =
            entries.getOrNull(ordinal)

        fun fromName(name: String): Archetype? =
            entries.find { it.name.equals(name, ignoreCase = true) }
    }
}