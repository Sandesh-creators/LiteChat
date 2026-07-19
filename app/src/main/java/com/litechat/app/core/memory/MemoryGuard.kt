package com.litechat.app.core.memory

import android.app.ActivityManager
import android.content.Context
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MemoryGuard(context: Context) {

    companion object {
        const val MAX_MEMORY_MB = 300
        const val MEMORY_CHECK_INTERVAL_MS = 5000L
        const val CRITICAL_THRESHOLD_PERCENT = 85
        const val WARNING_THRESHOLD_PERCENT = 70
    }

    enum class MemoryLevel {
        NORMAL, WARNING, CRITICAL, EMERGENCY
    }

    private val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    private val handler = Handler(Looper.getMainLooper())

    private val _memoryLevel = MutableStateFlow(MemoryLevel.NORMAL)
    val memoryLevel: StateFlow<MemoryLevel> = _memoryLevel.asStateFlow()

    private val _usedMemoryMB = MutableStateFlow(0)
    val usedMemoryMB: StateFlow<Int> = _usedMemoryMB.asStateFlow()

    private var onMemoryCritical: (() -> Unit)? = null

    private val checkRunnable = object : Runnable {
        override fun run() {
            checkMemory()
            handler.postDelayed(this, MEMORY_CHECK_INTERVAL_MS)
        }
    }

    fun startMonitoring(onCritical: () -> Unit = {}) {
        onMemoryCritical = onCritical
        handler.post(checkRunnable)
    }

    fun stopMonitoring() {
        handler.removeCallbacks(checkRunnable)
    }

    private fun checkMemory() {
        val runtime = Runtime.getRuntime()
        val usedMB = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024)
        _usedMemoryMB.value = usedMB.toInt()

        val memInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memInfo)
        val totalSystemMB = memInfo.totalMem / (1024 * 1024)
        val usedPercent = ((usedMB.toFloat() / totalSystemMB) * 100).toInt()

        val level = when {
            usedPercent >= CRITICAL_THRESHOLD_PERCENT -> MemoryLevel.CRITICAL
            usedMB >= MAX_MEMORY_MB -> MemoryLevel.EMERGENCY
            usedPercent >= WARNING_THRESHOLD_PERCENT -> MemoryLevel.WARNING
            else -> MemoryLevel.NORMAL
        }

        val previousLevel = _memoryLevel.value
        _memoryLevel.value = level

        if (level == MemoryLevel.CRITICAL || level == MemoryLevel.EMERGENCY) {
            if (previousLevel != level) {
                onMemoryCritical?.invoke()
                System.gc()
            }
        }
    }

    fun forceCleanup() {
        System.gc()
        System.runFinalization()
    }
}
