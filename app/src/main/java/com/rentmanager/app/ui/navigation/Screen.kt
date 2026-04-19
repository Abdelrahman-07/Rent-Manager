package com.rentmanager.app.ui.navigation

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object Apartments : Screen("apartments")
    object ApartmentDetail : Screen("apartment_detail/{apartmentId}") {
        fun createRoute(id: Long) = "apartment_detail/$id"
    }
    object AddEditApartment : Screen("add_edit_apartment?apartmentId={apartmentId}") {
        fun createRoute(id: Long? = null) =
            if (id != null) "add_edit_apartment?apartmentId=$id" else "add_edit_apartment"
    }
    object Tenants : Screen("tenants")
    object TenantDetail : Screen("tenant_detail/{tenantId}") {
        fun createRoute(id: Long) = "tenant_detail/$id"
    }
    object AddEditTenant : Screen("add_edit_tenant?tenantId={tenantId}") {
        fun createRoute(id: Long? = null) =
            if (id != null) "add_edit_tenant?tenantId=$id" else "add_edit_tenant"
    }
    object Payments : Screen("payments")
    object AddEditPayment : Screen("add_edit_payment?paymentId={paymentId}&tenantId={tenantId}") {
        fun createRoute(paymentId: Long? = null, tenantId: Long? = null): String {
            var route = "add_edit_payment"
            val params = mutableListOf<String>()
            if (paymentId != null) params.add("paymentId=$paymentId")
            if (tenantId != null) params.add("tenantId=$tenantId")
            if (params.isNotEmpty()) route += "?" + params.joinToString("&")
            return route
        }
    }
}
