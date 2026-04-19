package com.rentmanager.app.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.rentmanager.app.data.dao.ApartmentDao
import com.rentmanager.app.data.dao.PaymentDao
import com.rentmanager.app.data.dao.TenantDao
import com.rentmanager.app.data.entities.Apartment
import com.rentmanager.app.data.entities.Payment
import com.rentmanager.app.data.entities.Tenant

@Database(
    entities = [Apartment::class, Tenant::class, Payment::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun apartmentDao(): ApartmentDao
    abstract fun tenantDao(): TenantDao
    abstract fun paymentDao(): PaymentDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "rent_manager_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
