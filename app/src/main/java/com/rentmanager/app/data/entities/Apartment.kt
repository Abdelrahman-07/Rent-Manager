package com.rentmanager.app.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "apartments")
data class Apartment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,                    // e.g. "Flat 1A", "Unit 301"
    val floor: String = "",
    val address: String = "",
    val description: String = "",
    val bedrooms: Int = 1,
    val bathrooms: Int = 1,
    val areaSqm: Double = 0.0,
    val monthlyRent: Double = 0.0,
    val isOccupied: Boolean = false,
    val amenities: String = "",          // comma-separated
    val notes: String = ""
)
