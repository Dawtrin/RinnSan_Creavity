package com.rinnsan.creavity.presentation.uplink

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.rinnsan.creavity.core.router.Routes

@Composable
fun UplinkScreenWrapper(
    navController: NavController,
    viewModel: UplinkViewModel = hiltViewModel()
) {
    val identityProfile by viewModel.identityProfile.collectAsState()

    UplinkScreen(
        navController = navController,
        identityProfile = identityProfile,
        onReset = {
            viewModel.clearIdentity()
            navController.navigate(Routes.IDENTITY_SCANNER) {
                popUpTo(Routes.STYLIST)
            }
        }
    )
}