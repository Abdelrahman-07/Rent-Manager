package com.rentmanager.app.data.dao

import androidx.room.*
import com.rentmanager.app.data.entities.Payment
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentDao {

    @Query("SELECT * FROM payments ORDER BY dueDate DESC")
    fun getAllPayments(): Flow<List<Payment>>

    @Query("SELECT * FROM payments WHERE tenantId = :tenantId ORDER BY dueDate DESC")
    fun getPaymentsByTenant(tenantId: Long): Flow<List<Payment>>

    @Query("SELECT * FROM payments WHERE month = :month AND year = :year ORDER BY dueDate ASC")
    fun getPaymentsByMonthYear(month: Int, year: Int): Flow<List<Payment>>

    @Query("""
        SELECT * FROM payments 
        WHERE status = 'PENDING' OR status = 'OVERDUE' 
        ORDER BY dueDate ASC
    """)
    fun getPendingAndOverduePayments(): Flow<List<Payment>>

    @Query("SELECT * FROM payments WHERE id = :id")
    suspend fun getPaymentById(id: Long): Payment?

    @Query("""
        SELECT COUNT(*) FROM payments 
        WHERE (status = 'PENDING' OR status = 'OVERDUE') 
        AND month = :month AND year = :year
    """)
    fun getUnpaidCountForMonth(month: Int, year: Int): Flow<Int>

    @Query("SELECT SUM(paidAmount) FROM payments WHERE tenantId = :tenantId AND status = 'PAID'")
    fun getTotalPaidByTenant(tenantId: Long): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: Payment): Long

    @Update
    suspend fun updatePayment(payment: Payment)

    @Delete
    suspend fun deletePayment(payment: Payment)
}
