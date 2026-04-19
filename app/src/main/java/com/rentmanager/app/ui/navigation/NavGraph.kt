package com.rentmanager.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.rentmanager.app.data.repository.RentRepository
import com.rentmanager.app.ui.screens.apartments.AddEditApartmentScreen
import com.rentmanager.app.ui.screens.apartments.ApartmentDetailScreen
import com.rentmanager.app.ui.screens.apartments.ApartmentsScreen
import com.rentmanager.app.ui.screens.dashboard.DashboardScreen
import com.rentmanager.app.ui.screens.payments.AddEditPaymentScreen
import com.rentmanager.app.ui.screens.payments.PaymentsScreen
import com.rentmanager.app.ui.screens.tenants.AddEditTenantScreen
import com.rentmanager.app.ui.screens.tenants.TenantDetailScreen
import com.rentmanager.app.ui.screens.tenants.TenantsScreen

@Composable
fun NavGraph(navController: NavHostController, repository: RentRepository) {
    NavHost(navController = navController, startDestination = Screen.Dashboard.route) {

        composable(Screen.Dashboard.route) {
            DashboardScreen(repository = repository, navController = navController)
        }

        // ── Apartments ──────────────────────────────────────────────────────

        composable(Screen.Apartments.route) {
            ApartmentsScreen(repository = repository, navController = navController)
        }

        composable(
            route = Screen.ApartmentDetail.route,
            arguments = listOf(navArgument("apartmentId") { type = NavType.LongType })
        ) { backStack ->
            val id = backStack.arguments?.getLong("apartmentId") ?: return@composable
            ApartmentDetailScreen(
                apartmentId = id,
                repository = repository,
                navController = navController
            )
        }

        composable(
            route = Screen.AddEditApartment.route,
            arguments = listOf(
                navArgument("apartmentId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { backStack ->
            val id = backStack.arguments?.getLong("apartmentId").takeIf { it != -1L }
            AddEditApartmentScreen(
                apartmentId = id,
                repository = repository,
                navController = navController
            )
        }

        // ── Tenants ─────────────────────────────────────────────────────────

        composable(Screen.Tenants.route) {
            TenantsScreen(repository = repository, navController = navController)
        }

        composable(
            route = Screen.TenantDetail.route,
            arguments = listOf(navArgument("tenantId") { type = NavType.LongType })
        ) { backStack ->
            val id = backStack.arguments?.getLong("tenantId") ?: return@composable
            TenantDetailScreen(
                tenantId = id,
                repository = repository,
                navController = navController
            )
        }

        composable(
            route = Screen.AddEditTenant.route,
            arguments = listOf(
                navArgument("tenantId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { backStack ->
            val id = backStack.arguments?.getLong("tenantId").takeIf { it != -1L }
            AddEditTenantScreen(
                tenantId = id,
                repository = repository,
                navController = navController
            )
        }

        // ── Payments ─────────────────────────────────────────────────────────

        composable(Screen.Payments.route) {
            PaymentsScreen(repository = repository, navController = navController)
        }

        composable(
            route = Screen.AddEditPayment.route,
            arguments = listOf(
                navArgument("paymentId") {
                    type = NavType.LongType
                    defaultValue = -1L
                },
                navArgument("tenantId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { backStack ->
            val paymentId = backStack.arguments?.getLong("paymentId").takeIf { it != -1L }
            val tenantId = backStack.arguments?.getLong("tenantId").takeIf { it != -1L }
            AddEditPaymentScreen(
                paymentId = paymentId,
                preselectedTenantId = tenantId,
                repository = repository,
                navController = navController
            )
        }
    }
}
