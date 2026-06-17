package com.rinnsan.creavity.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.rinnsan.creavity.core.router.Routes
import com.rinnsan.creavity.data.IDENTITY_QUESTIONS
import com.rinnsan.creavity.domain.engine.IdentityEngine
import com.rinnsan.creavity.presentation.chat.StylistChatScreen
import com.rinnsan.creavity.presentation.contact.ContactScreen
import com.rinnsan.creavity.presentation.gateway.GatewayScreen
import com.rinnsan.creavity.presentation.home.HomeScreen
import com.rinnsan.creavity.presentation.signal.TheSignalScreen
import com.rinnsan.creavity.presentation.uplink.UplinkScreen
import com.rinnsan.creavity.presentation.uplink.UplinkViewModel
import com.rinnsan.creavity.presentation.uplink.identity.IdentityScanner
import com.rinnsan.creavity.presentation.uplink.identity.IdentityScannerViewModel
import com.rinnsan.creavity.presentation.archive.ArtifactArchiveScreen
import com.rinnsan.creavity.presentation.archive.VaultViewModel
import com.rinnsan.creavity.presentation.archive.ArtifactDetailScreen
import com.rinnsan.creavity.presentation.auth.AuthViewModel
import com.rinnsan.creavity.presentation.auth.LoginScreen
import com.rinnsan.creavity.presentation.auth.RegisterScreen
import com.rinnsan.creavity.presentation.auth.ForgotPasswordScreen
import com.rinnsan.creavity.presentation.bodydata.BodyDataScreen
import com.rinnsan.creavity.presentation.brand.BrandScreen
import com.rinnsan.creavity.presentation.profile.ProfileScreen
import com.rinnsan.creavity.presentation.wishlist.WishlistScreen
import com.rinnsan.creavity.presentation.signal.create.CreatePostScreen
import com.rinnsan.creavity.presentation.signal.viewmodel.SignalViewModel
import com.google.firebase.auth.FirebaseAuth
import com.rinnsan.creavity.presentation.admin.AdminDashboardScreen
import com.rinnsan.creavity.presentation.cart.CartScreen
import com.rinnsan.creavity.presentation.cart.CheckoutScreen
import com.rinnsan.creavity.presentation.cart.MyOrdersScreen
import com.rinnsan.creavity.presentation.cart.OrderSuccessScreen
import com.rinnsan.creavity.presentation.cart.CartViewModel
import com.rinnsan.creavity.presentation.cart.LiveTrackingScreen



@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // Shared ViewModels
    val uplinkViewModel: UplinkViewModel  = hiltViewModel()
    val authViewModel: AuthViewModel      = hiltViewModel()
    val signalViewModel: SignalViewModel  = hiltViewModel() // shared giữa Signal & CreatePost
    val cartViewModel: CartViewModel      = hiltViewModel() // shared giữa Cart & Checkout
    val vaultViewModel: VaultViewModel    = hiltViewModel() // shared giữa Archive & Detail

    NavHost(
        navController = navController,
        startDestination = Routes.GATEWAY
    ) {
        // ═══════════════════════════════════════════════════════════════
        // 1. GATEWAY SCREEN — Check Firebase session, route accordingly
        // ═══════════════════════════════════════════════════════════════
        composable(Routes.GATEWAY) {
            GatewayScreen(
                onNavigateToHome = {
                    // Check Firebase session — nếu đã login → HOME, chưa → LOGIN
                    val currentUser = FirebaseAuth.getInstance().currentUser
                    val destination = if (currentUser != null) Routes.HOME else Routes.LOGIN
                    navController.navigate(destination) {
                        popUpTo(Routes.GATEWAY) { inclusive = true }
                    }
                }
            )
        }

        // ═══════════════════════════════════════════════════════════════
        // 2. HOME SCREEN (Main Scrollytelling Experience)
        // ═══════════════════════════════════════════════════════════════
        composable(Routes.HOME) {
            HomeScreen(
                navController  = navController,
                vaultViewModel = vaultViewModel
            )
        }

        // ═══════════════════════════════════════════════════════════════
        // 3. SHOP SCREEN (Placeholder - implement later)
        // ═══════════════════════════════════════════════════════════════
        composable(Routes.SHOP) {
            // TODO: Implement ShopScreen
            // ShopScreen(navController = navController)
        }

        // ═══════════════════════════════════════════════════════════════
        // 4. NEWS SCREEN (Placeholder - implement later)
        // ═══════════════════════════════════════════════════════════════
        composable(Routes.SIGNAL) {
            TheSignalScreen(
                navController = navController,
                viewModel = signalViewModel
            )
        }

        // ═══════════════════════════════════════════════════════════════
        // 6. CONTACT SCREEN (Placeholder - implement later)
        // ═══════════════════════════════════════════════════════════════
        composable(Routes.CONTACT) {
            // TODO: Implement ContactScreen
            ContactScreen(navController = navController)
        }

        composable(Routes.STYLIST) {
            val identityProfile by uplinkViewModel.identityProfile.collectAsState()
            val stylistState by uplinkViewModel.stylistState.collectAsState()

            UplinkScreen(
                navController = navController,
                identityProfile = identityProfile,
                stylistState = stylistState,
                onReset = {
                    uplinkViewModel.clearIdentity()
                    navController.navigate(Routes.IDENTITY_SCANNER) {
                        popUpTo(Routes.STYLIST)
                    }
                }
            )
        }

        composable(Routes.IDENTITY_SCANNER) {
            val scannerViewModel: IdentityScannerViewModel = hiltViewModel()

            IdentityScanner(
                questions = IDENTITY_QUESTIONS,
                engine = IdentityEngine,
                navController = navController,
                onComplete = { profile ->
                    // Lưu profile vào UplinkViewModel
                    uplinkViewModel.setIdentityProfile(profile)

                    // Navigate back to Uplink
                    navController.navigate(Routes.STYLIST) {
                        popUpTo(Routes.STYLIST) { inclusive = false }
                    }
                }
            )
        }

        // ═══════════════════════════════════════════════════════════════
        // STYLIST CHAT SCREEN (NEW)
        // ═══════════════════════════════════════════════════════════════
        composable(Routes.STYLIST_CHAT) {
            val profile by uplinkViewModel.identityProfile.collectAsState()

            if (profile != null) {
                StylistChatScreen(
                    profile = profile,
                    navController = navController,
                    viewModel = uplinkViewModel  // ← FIX: Pass shared ViewModel
                )
            } else {
                // No profile - redirect to scanner
                LaunchedEffect(Unit) {
                    navController.navigate(Routes.IDENTITY_SCANNER) {
                        popUpTo(Routes.STYLIST) { inclusive = false }
                    }
                }
            }
        }

        composable(Routes.ARTIFACT_ARCHIVE) {
            ArtifactArchiveScreen(
                navController = navController,
                viewModel     = vaultViewModel,
                cartViewModel = cartViewModel
            )
        }

        // ── ARTIFACT DETAIL ────────────────────────────────────────────
        composable("${Routes.ARTIFACT_DETAIL}/{artifactId}") { backStackEntry ->
            val artifactId = backStackEntry.arguments?.getString("artifactId") ?: ""
            ArtifactDetailScreen(
                artifactId     = artifactId,
                navController  = navController,
                vaultViewModel = vaultViewModel,
                cartViewModel  = cartViewModel
            )
        }

        // ═══════════════════════════════════════════════════════════════
        // BRAND SCREEN
        // ═══════════════════════════════════════════════════════════════
        composable(Routes.BRAND) {
            BrandScreen(navController = navController)
        }

        // ═══════════════════════════════════════════════════════════════
        // AUTH SCREENS
        // ═══════════════════════════════════════════════════════════════
        composable(Routes.LOGIN) {
            LoginScreen(navController = navController)
        }

        composable(Routes.REGISTER) {
            RegisterScreen(navController = navController)
        }

        composable(Routes.FORGOT_PASSWORD) {
            ForgotPasswordScreen(navController = navController)
        }



        composable(Routes.PROFILE) {
            ProfileScreen(navController = navController)
        }
        composable(Routes.WISHLIST) {
            WishlistScreen(navController = navController)
        }
        composable(Routes.BODY_DATA) {
            BodyDataScreen(navController = navController)
        }
        // ── CHECKOUT FLOW ───────────────────────────────────────────────
        composable(Routes.CART) {
            CartScreen(navController = navController, viewModel = cartViewModel)
        }

        composable(Routes.CHECKOUT) {
            CheckoutScreen(navController = navController, viewModel = cartViewModel)
        }

        composable("${Routes.ORDER_SUCCESS}/{orderId}") { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
            OrderSuccessScreen(orderId = orderId, navController = navController)
        }

        composable(Routes.MY_ORDERS) {
            MyOrdersScreen(navController = navController)
        }
        composable(Routes.ADMIN_DASHBOARD) {
            AdminDashboardScreen(navController = navController)
        }

        composable(Routes.LIVE_TRACKING) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
            LiveTrackingScreen(navController = navController, orderId = orderId)
        }

        composable("create_post") {
            CreatePostScreen(
                navController = navController,
                signalViewModel = signalViewModel
            )
        }
    }
}