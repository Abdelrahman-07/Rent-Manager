package com.rentmanager.app

import android.app.Application
import com.rentmanager.app.data.database.AppDatabase
import com.rentmanager.app.data.repository.RentRepository
import com.rentmanager.app.notifications.NotificationHelper
import com.rentmanager.app.notifications.RentCheckWorker

class RentManagerApplication : Application() {

    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }

    val repository: RentRepository by lazy {
        RentRepository(
            database.apartmentDao(),
            database.tenantDao(),
            database.paymentDao()
        )
    }

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannels(this)
        RentCheckWorker.schedule(this)
    }
}
