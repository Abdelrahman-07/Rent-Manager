package com.rentmanager.app.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class PaymentStatus { PENDING, PAID, OVERDUE, PARTIAL }

@Entity(tableName = "payments")
data class Payment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tenantId: Long,
    val apartmentId: Long,
    val amount: Double,
    val dueDate: Long,                              // epoch millis
    val paidDate: Long? = null,                     // epoch millis, null if not paid
    val paidAmount: Double = 0.0,
    val month: Int,                                 // 1–12
    val year: Int,
    val status: String = PaymentStatus.PENDING.name,
    val notes: String = ""
)
