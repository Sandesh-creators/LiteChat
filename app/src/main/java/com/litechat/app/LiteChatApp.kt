package com.litechat.app

import android.app.Application
import com.litechat.app.core.memory.BitmapRecycler
import com.litechat.app.core.memory.MemoryGuard
import com.litechat.app.core.memory.PoolManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class LiteChatApp : Application() {

    lateinit var memoryGuard: MemoryGuard
        private set

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        memoryGuard = MemoryGuard(this)
        memoryGuard.startMonitoring {
            handleMemoryCritical()
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        handleMemoryCritical()
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if (level >= TRIM_MEMORY_RUNNING_CRITICAL) {
            handleMemoryCritical()
        }
    }

    private fun handleMemoryCritical() {
        appScope.launch(Dispatchers.Default) {
            BitmapRecycler.recycleAll()
            PoolManager.releaseAll()
            memoryGuard.forceCleanup()
        }
    }
}
