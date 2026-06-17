package com.rinnsan.creavity.data.vault

import com.rinnsan.creavity.domain.models.Archetype

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// DATA MODEL — AFFILIATE ARTIFACT
// Each item is a "classified asset" in the RinnSan dossier system
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

data class AffiliateArtifact(
    val id: String,
    val title: String,
    val category: String,
    val price: String,
    val imageUrl: String,
    val affiliateLink: String,
    val vendor: String,
    val archetype: Archetype,
    // ── Direct Sale fields ─────────────────────────────────────────
    val isDirectSale: Boolean    = false,
    val internalPrice: Long      = 0L,
    val stock: Long              = 0L,
    val commissionRate: Double   = 0.08,
    // ── New: multi-image + size variant ───────────────────────────
    val images: List<String>     = emptyList(),  // 5-7 product images
    val sizes: List<String>      = emptyList()   // ["S","M","L","XL"]
)

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// MOCK VAULT — CLASSIFIED INVENTORY
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

object MockVault {

    val allArtifacts: List<AffiliateArtifact> = listOf(

        // ── GHOST ────────────────────────────────────────────────────
        AffiliateArtifact(
            id            = "GH-001",
            title         = "PHANTOM SHELL JACKET",
            category      = "OUTER LAYER // TACTICAL",
            price         = "1.850.000 VND",
            imageUrl      = "https://images.unsplash.com/photo-1551488831-00ddcb6c6bd3?w=800&q=80",
            affiliateLink = "https://shopee.vn",
            vendor        = "ACRONYM_REPLICA",
            archetype     = Archetype.GHOST
        ),
        AffiliateArtifact(
            id            = "GH-002",
            title         = "VOID CARGO TROUSERS",
            category      = "LOWER UNIT // CARGO",
            price         = "920.000 VND",
            imageUrl      = "https://images.unsplash.com/photo-1624378439575-d8705ad7ae80?w=800&q=80",
            affiliateLink = "https://shopee.vn",
            vendor        = "DISTRICT_SUPPLY",
            archetype     = Archetype.GHOST
        ),
        AffiliateArtifact(
            id            = "GH-003",
            title         = "SHADOW BALACLAVA MK.II",
            category      = "HEAD UNIT // CONCEALMENT",
            price         = "320.000 VND",
            imageUrl      = "https://images.unsplash.com/photo-1614093302611-8efc45b9b421?w=800&q=80",
            affiliateLink = "https://shopee.vn",
            vendor        = "NOCTURNE_LAB",
            archetype     = Archetype.GHOST
        ),

        // ── OPERATOR ─────────────────────────────────────────────────
        AffiliateArtifact(
            id            = "OP-001",
            title         = "FIELD OPS VEST SYSTEM",
            category      = "TORSO RIG // MODULAR",
            price         = "1.250.000 VND",
            imageUrl      = "https://images.unsplash.com/photo-1520975954732-35dd22299614?w=800&q=80",
            affiliateLink = "https://shopee.vn",
            vendor        = "IRON_PROTOCOL",
            archetype     = Archetype.OPERATOR
        ),
        AffiliateArtifact(
            id            = "OP-002",
            title         = "URBAN PATROL BOOTS",
            category      = "FOOTWEAR // COMBAT",
            price         = "2.100.000 VND",
            imageUrl      = "https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=800&q=80",
            affiliateLink = "https://shopee.vn",
            vendor        = "STEEL_DIVISION",
            archetype     = Archetype.OPERATOR
        ),
        AffiliateArtifact(
            id            = "OP-003",
            title         = "COMMAND LAYER HOODIE",
            category      = "MID LAYER // STRUCTURED",
            price         = "850.000 VND",
            imageUrl      = "https://images.unsplash.com/photo-1556821840-3a63f15732ce?w=800&q=80",
            affiliateLink = "https://shopee.vn",
            vendor        = "IRON_PROTOCOL",
            archetype     = Archetype.OPERATOR
        ),

        // ── GLITCH ───────────────────────────────────────────────────
        AffiliateArtifact(
            id            = "GL-001",
            title         = "NOISE LAYER HOODIE",
            category      = "MID LAYER // DISTORTION",
            price         = "780.000 VND",
            imageUrl      = "https://images.unsplash.com/photo-1576566588028-4147f3842f27?w=800&q=80",
            affiliateLink = "https://shopee.vn",
            vendor        = "STATIC_WORKS",
            archetype     = Archetype.GLITCH
        ),
        AffiliateArtifact(
            id            = "GL-002",
            title         = "CORRUPTED DENIM JACKET",
            category      = "OUTER LAYER // DISTRESSED",
            price         = "1.100.000 VND",
            imageUrl      = "https://images.unsplash.com/photo-1551537482-f2075a1d41f2?w=800&q=80",
            affiliateLink = "https://shopee.vn",
            vendor        = "CORRUPT_FABRIC",
            archetype     = Archetype.GLITCH
        ),
        AffiliateArtifact(
            id            = "GL-003",
            title         = "SIGNAL BREAK SHORTS",
            category      = "LOWER UNIT // STREET",
            price         = "490.000 VND",
            imageUrl      = "https://images.unsplash.com/photo-1591195853828-11db59a44f43?w=800&q=80",
            affiliateLink = "https://shopee.vn",
            vendor        = "STATIC_WORKS",
            archetype     = Archetype.GLITCH
        ),

        // ── NOMAD ────────────────────────────────────────────────────
        AffiliateArtifact(
            id            = "NM-001",
            title         = "TRAVERSE MULTI-POCKET JACKET",
            category      = "OUTER LAYER // ALL-TERRAIN",
            price         = "1.650.000 VND",
            imageUrl      = "https://images.unsplash.com/photo-1559582798-678dfc71ccd8?w=800&q=80",
            affiliateLink = "https://shopee.vn",
            vendor        = "WAYPOINT_SUPPLY",
            archetype     = Archetype.NOMAD
        ),
        AffiliateArtifact(
            id            = "NM-002",
            title         = "GRID-WEAVE ROLL BAG",
            category      = "CARRY UNIT // EDC",
            price         = "540.000 VND",
            imageUrl      = "https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=800&q=80",
            affiliateLink = "https://shopee.vn",
            vendor        = "CARRY_PROTOCOL",
            archetype     = Archetype.NOMAD
        ),
        AffiliateArtifact(
            id            = "NM-003",
            title         = "TRANSIT TECH PANTS",
            category      = "LOWER UNIT // STRETCH",
            price         = "1.080.000 VND",
            imageUrl      = "https://images.unsplash.com/photo-1473966968600-fa801b869a1a?w=800&q=80",
            affiliateLink = "https://shopee.vn",
            vendor        = "WAYPOINT_SUPPLY",
            archetype     = Archetype.NOMAD
        )
    )

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // FILTER: Returns curated arsenal matched to identity archetype
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    fun getArsenalFor(archetype: Archetype): List<AffiliateArtifact> =
        allArtifacts.filter { it.archetype == archetype }
}