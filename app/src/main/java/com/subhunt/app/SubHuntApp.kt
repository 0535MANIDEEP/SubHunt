package com.subhunt.app

// Copyright (c) 2026 Manideep Daram. All rights reserved.

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.work.Configuration
import com.subhunt.app.billing.BillingManager
import com.subhunt.app.BuildConfig
import com.subhunt.app.notification.BillingReminderWorker
import com.subhunt.app.notification.NotificationHelper
import com.subhunt.app.security.AppLockManager
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class SubHuntApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var billingManager: BillingManager

    @Inject
    lateinit var appLockManager: AppLockManager

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()

        // Initialize notifications
        try {
            NotificationHelper.createChannels(this)
            BillingReminderWorker.schedule(this)
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) Log.e("SubHuntApp", "Failed to init notifications", e)
        }

        // BillingManager restores activation from SharedPreferences on init

        // Auto-lock when app goes to background
        ProcessLifecycleOwner.get().lifecycle.addObserver(
            object : DefaultLifecycleObserver {
                override fun onStop(owner: LifecycleOwner) {
                    appScope.launch {
                        appLockManager.setBackgroundedAt(System.currentTimeMillis())
                    }
                }

                override fun onStart(owner: LifecycleOwner) {
                    appScope.launch {
                        val bgAt = appLockManager.backgroundedAt.first()
                        val away = System.currentTimeMillis() - bgAt
                        if (bgAt > 0 && away > AUTO_LOCK_TIMEOUT_MS) {
                            appLockManager.markLocked()
                        }
                    }
                }
            }
        )
    }

    companion object {
        const val AUTO_LOCK_TIMEOUT_MS = 60_000L
    }
}
