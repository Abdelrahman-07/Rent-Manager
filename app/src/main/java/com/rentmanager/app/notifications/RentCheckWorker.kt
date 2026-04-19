package com.rentmanager.app.notifications

import android.content.Context
import androidx.work.*
import com.rentmanager.app.data.database.AppDatabase
import com.rentmanager.app.data.repository.RentRepository
import java.util.Calendar
import java.util.concurrent.TimeUnit

class RentCheckWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val db = AppDatabase.getDatabase(applicationContext)
        val repository = RentRepository(db.apartmentDao(), db.tenantDao(), db.paymentDao())

        val calendar = Calendar.getInstance()
        val today = calendar.get(Calendar.DAY_OF_MONTH)
        val currentTime = calendar.timeInMillis

        // ── Check rent due in the next 3 days ────────────────────────────────
        for (offset in 0..2) {
            val checkCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, offset) }
            val dueDay = checkCal.get(Calendar.DAY_OF_MONTH)

            repository.getTenantsWithRentDueOn(dueDay).forEach { tenant ->
                if (tenant.isActive && tenant.apartmentId != null) {
                    val apartment = db.apartmentDao().getApartmentById(tenant.apartmentId)
                    NotificationHelper.sendRentDueNotification(
                        applicationContext,
                        "${tenant.firstName} ${tenant.lastName}",
                        apartment?.name ?: "Apartment",
                        daysUntilDue = offset,
                        notificationId = (tenant.id * 10 + offset).toInt()
                    )
                }
            }
        }

        // ── Check contracts expiring in next 30 days ──────────────────────────
        val thirtyDaysFromNow = currentTime + (30L * 24 * 60 * 60 * 1000)
        repository.getTenantsWithContractEndingSoon(currentTime, thirtyDaysFromNow).forEach { tenant ->
            if (tenant.apartmentId != null) {
                val apartment = db.apartmentDao().getApartmentById(tenant.apartmentId)
                val daysLeft = ((tenant.contractEndDate - currentTime) / (24 * 60 * 60 * 1000)).toInt()
                NotificationHelper.sendContractExpiryNotification(
                    applicationContext,
                    "${tenant.firstName} ${tenant.lastName}",
                    apartment?.name ?: "Apartment",
                    daysLeft,
                    notificationId = (tenant.id * 10 + 5000).toInt()
                )
            }
        }

        return Result.success()
    }

    companion object {
        private const val WORK_TAG = "rent_check_work"

        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .build()

            val request = PeriodicWorkRequestBuilder<RentCheckWorker>(1, TimeUnit.DAYS)
                .setConstraints(constraints)
                .setInitialDelay(1, TimeUnit.HOURS)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_TAG,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
