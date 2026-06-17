package com.rinnsan.creavity.core.router

object Routes {
    // 8 Không gian số (Interface Breakdown) [cite: 32]
    const val GATEWAY = "gateway_screen"       // Splash
    const val ONBOARDING = "onboarding_screen" // Giới thiệu
    const val HOME = "home_screen"             // The Runway [cite: 43]
    const val SHOP = "shop_screen"             // The Data Bank [cite: 60]
    const val PRODUCT_DETAIL = "detail_screen"    // The Lab (3D) [cite: 71]
    const val SIGNAL = "signal_screen"             // The Signal [cite: 89]

    const val STYLIST = "uplink_screen"       // The Stylist [cite: 97]
    const val BRAND = "brand_screen"           // The Origin [cite: 98]
    const val CONTACT = "contact_screen"       // The Uplink [cite: 106]
             // Giỏ hàng

    const val IDENTITY_SCANNER = "identity_scanner"
    const val STYLIST_CHAT = "stylist_chat"
    const val ARTIFACT_ARCHIVE = "artifact_archive_screen" // The Vault

    // ── AUTH FLOW ─────────────────────────────────────────────────
    const val LOGIN = "login_screen"
    const val REGISTER = "register_screen"
    const val FORGOT_PASSWORD = "forgot_password_screen"

    // ── Profile & utility screens ────────────────────────────────────
    const val PROFILE   = "profile_screen"
    const val WISHLIST  = "wishlist_screen"
    const val BODY_DATA = "body_data_screen"

    // ── Admin ────────────────────────────────────────────────────────
    const val ADMIN_DASHBOARD = "admin_dashboard_screen"

    // ── Checkout flow ─────────────────────────────────────────────────
    const val ARTIFACT_DETAIL = "artifact_detail"
    const val CART          = "cart_screen_v2"
    const val CHECKOUT      = "checkout_screen"
    const val ORDER_SUCCESS = "order_success_screen"
    const val MY_ORDERS     = "my_orders_screen"
    const val LIVE_TRACKING = "live_tracking/{orderId}"
}