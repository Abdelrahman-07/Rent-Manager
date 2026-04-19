package com.rentmanager.app.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tenants")
data class Tenant(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val firstName: String,
    val lastName: String,
    val phone: String = "",
    val email: String = "",
    val nationalId: String = "",
    val nationality: String = "",
    // Photo URIs stored as strings (file:// or content:// URI)
    val photoUri: String = "",
    val idPhotoFrontUri: String = "",
    val idPhotoBackUri: String = "",
    // Apartment assignment
    val apartmentId: Long? = null,
    // Contract details
    val contractStartDate: Long = 0L,     // epoch millis
    val contractEndDate: Long = 0L,       // epoch millis
    val rentDueDay: Int = 1,              // day of month (1–28)
    val monthlyRent: Double = 0.0,
    val securityDeposit: Double = 0.0,
    val depositPaid: Boolean = false,
    // Emergency contact
    val emergencyContactName: String = "",
    val emergencyContactPhone: String = "",
    // Meta
    val notes: String = "",
    val isActive: Boolean = true,
    val moveInDate: Long = 0L             // epoch millis
)
