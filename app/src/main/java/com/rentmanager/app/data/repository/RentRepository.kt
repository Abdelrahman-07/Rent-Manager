package com.rentmanager.app.data.repository

import com.rentmanager.app.data.dao.ApartmentDao
import com.rentmanager.app.data.dao.PaymentDao
import com.rentmanager.app.data.dao.TenantDao
import com.rentmanager.app.data.entities.Apartment
import com.rentmanager.app.data.entities.Payment
import com.rentmanager.app.data.entities.Tenant
import kotlinx.coroutines.flow.Flow

class RentRepository(
    private val apartmentDao: ApartmentDao,
    private val tenantDao: TenantDao,
    private val paymentDao: PaymentDao
) {

    // ── Apartments ────────────────────────────────────────────────────────────

    val allApartments: Flow<List<Apartment>> = apartmentDao.getAllApartments()
    val vacantApartments: Flow<List<Apartment>> = apartmentDao.getVacantApartments()
    val totalApartmentCount: Flow<Int> = apartmentDao.getTotalCount()
    val occupiedApartmentCount: Flow<Int> = apartmentDao.getOccupiedCount()

    suspend fun getApartmentById(id: Long): Apartment? = apartmentDao.getApartmentById(id)
    suspend fun insertApartment(apartment: Apartment): Long = apartmentDao.insertApartment(apartment)
    suspend fun updateApartment(apartment: Apartment) = apartmentDao.updateApartment(apartment)
    suspend fun deleteApartment(apartment: Apartment) = apartmentDao.deleteApartment(apartment)

    // ── Tenants ───────────────────────────────────────────────────────────────

    val allActiveTenants: Flow<List<Tenant>> = tenantDao.getAllActiveTenants()
    val allTenants: Flow<List<Tenant>> = tenantDao.getAllTenants()
    val activeTenantCount: Flow<Int> = tenantDao.getActiveTenantCount()

    fun getTenantsByApartment(apartmentId: Long): Flow<List<Tenant>> =
        tenantDao.getTenantsByApartment(apartmentId)

    suspend fun getTenantById(id: Long): Tenant? = tenantDao.getTenantById(id)

    suspend fun insertTenant(tenant: Tenant): Long = tenantDao.insertTenant(tenant)
    suspend fun updateTenant(tenant: Tenant) = tenantDao.updateTenant(tenant)
    suspend fun deleteTenant(tenant: Tenant) = tenantDao.deleteTenant(tenant)
    suspend fun deactivateTenant(tenantId: Long) = tenantDao.deactivateTenant(tenantId)

    suspend fun getTenantsWithContractEndingSoon(
        startDate: Long,
        endDate: Long
    ): List<Tenant> = tenantDao.getTenantsWithContractEndingSoon(startDate, endDate)

    suspend fun getTenantsWithRentDueOn(day: Int): List<Tenant> =
        tenantDao.getTenantsWithRentDueOn(day)

    // ── Payments ──────────────────────────────────────────────────────────────

    val allPayments: Flow<List<Payment>> = paymentDao.getAllPayments()
    val pendingAndOverduePayments: Flow<List<Payment>> = paymentDao.getPendingAndOverduePayments()

    fun getPaymentsByTenant(tenantId: Long): Flow<List<Payment>> =
        paymentDao.getPaymentsByTenant(tenantId)

    fun getPaymentsByMonthYear(month: Int, year: Int): Flow<List<Payment>> =
        paymentDao.getPaymentsByMonthYear(month, year)

    fun getUnpaidCountForMonth(month: Int, year: Int): Flow<Int> =
        paymentDao.getUnpaidCountForMonth(month, year)

    fun getTotalPaidByTenant(tenantId: Long): Flow<Double?> =
        paymentDao.getTotalPaidByTenant(tenantId)

    suspend fun getPaymentById(id: Long): Payment? = paymentDao.getPaymentById(id)
    suspend fun insertPayment(payment: Payment): Long = paymentDao.insertPayment(payment)
    suspend fun updatePayment(payment: Payment) = paymentDao.updatePayment(payment)
    suspend fun deletePayment(payment: Payment) = paymentDao.deletePayment(payment)
}
