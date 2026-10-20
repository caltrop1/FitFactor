package com.sami.setra.debug

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Explicitly invoked by adb. This receiver exists only in the debug APK. */
class DemoDataSeedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_SEED_DEMO_DATA) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val message = DemoDataSeeder.seed(context.applicationContext)
                Log.i(TAG, message)
            } catch (error: Exception) {
                Log.e(TAG, "Demo database seeding failed", error)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val ACTION_SEED_DEMO_DATA = "com.sami.fitfactor.DEBUG_SEED_DEMO_DATA"
        private const val TAG = "FitFactorDemoSeeder"
    }
}
