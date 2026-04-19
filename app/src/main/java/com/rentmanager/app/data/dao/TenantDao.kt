package com.rentmanager.app.data.dao

import androidx.room.*
import com.rentmanager.app.data.entities.Tenant
import kotlinx.coroutines.flow.Flow

@Dao
interface TenantDao {

    @Query("SELECT * FROM tenants WHERE isActive = 1 ORDER BY firstName ASC, lastName ASC")
    fun getAllActiveTenants(): Flow<List<Tenant>>

    @Query("SELECT * FROM tenants ORDER BY firstName ASC, lastName ASC")
    fun getAllTenants(): Flow<List<Tenant>>

    @Query("SELECT * FROM tenants WHERE id = :id")
    suspend fun getTenantById(id: Long): Tenant?

    @Query("SELECT * FROM tenants WHERE apartmentId = :apartmentId AND isActive = 1")
    fun getTenantsByApartment(apartmentId: Long): Flow<List<Tenant>>

    @Query("""
        SELECT * FROM tenants 
        WHERE contractEndDate BETWEEN :startDate AND :endDate 
        AND isActive = 1
        ORDER BY contractEndDate ASC
    """)
    suspend fun getTenantsWithContractEndingSoon(startDate: Long, endDate: Long): List<Tenant>

    @Query("SELECT * FROM tenants WHERE rentDueDay = :day AND isActive = 1")
    suspend fun getTenantsWithRentDueOn(day: Int): List<Tenant>

    @Query("SELECT COUNT(*) FROM tenants WHERE isActive = 1")
    fun getActiveTenantCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTenant(tenant: Tenant): Long

    @Update
    suspend fun updateTenant(tenant: Tenant)

    @Delete
    suspend fun deleteTenant(tenant: Tenant)

    @Query("UPDATE tenants SET isActive = 0, apartmentId = NULL WHERE id = :tenantId")
    suspend fun deactivateTenant(tenantId: Long)
}
