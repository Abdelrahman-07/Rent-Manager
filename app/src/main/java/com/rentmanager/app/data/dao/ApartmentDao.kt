package com.rentmanager.app.data.dao

import androidx.room.*
import com.rentmanager.app.data.entities.Apartment
import kotlinx.coroutines.flow.Flow

@Dao
interface ApartmentDao {

    @Query("SELECT * FROM apartments ORDER BY name ASC")
    fun getAllApartments(): Flow<List<Apartment>>

    @Query("SELECT * FROM apartments WHERE id = :id")
    suspend fun getApartmentById(id: Long): Apartment?

    @Query("SELECT * FROM apartments WHERE isOccupied = 0 ORDER BY name ASC")
    fun getVacantApartments(): Flow<List<Apartment>>

    @Query("SELECT COUNT(*) FROM apartments")
    fun getTotalCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM apartments WHERE isOccupied = 1")
    fun getOccupiedCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApartment(apartment: Apartment): Long

    @Update
    suspend fun updateApartment(apartment: Apartment)

    @Delete
    suspend fun deleteApartment(apartment: Apartment)
}
