package com.example.nammasantheledger

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.nammasantheledger.feature.reminders.WeeklyReminderWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Application class for Namma Santhe Ledger.
 * Annotated with @HiltAndroidApp to trigger Hilt's code generation
 * and serve as the application-level dependency container.
 *
 * Also configures WorkManager with Hilt for dependency injection
 * in Workers, and schedules the weekly reminder job.
 */
@HiltAndroidApp
class NammaSantheApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        scheduleWeeklyReminder()
    }

    /**
     * Schedules a periodic worker that runs once every 7 days
     * to notify the user about customers with outstanding dues.
     *
     * Uses KEEP policy so it doesn't reset the schedule on every app launch.
     */
    private fun scheduleWeeklyReminder() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED) // Works offline
            .build()

        val weeklyWork = PeriodicWorkRequestBuilder<WeeklyReminderWorker>(
            7, TimeUnit.DAYS
        )
            .setConstraints(constraints)
            .setInitialDelay(1, TimeUnit.DAYS) // First run after 1 day
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            WeeklyReminderWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP, // Don't reset timer on every launch
            weeklyWork
        )
    }
}
