package com.github.garyttierney.ghidradbg.plugin

import com.github.garyttierney.ghidradbg.client.launcher.DebuggeeStateChange
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.swing.Swing

interface DebuggerEventListener {
    suspend fun onDebuggeeStateChange(change: DebuggeeStateChange) = runBlocking(Dispatchers.Swing) {
        onDebuggeeStateChangedSync(change)
    }

    fun onDebuggeeStateChangedSync(change: DebuggeeStateChange) {
    }

    suspend fun onLogMessage(message: String) {

    }
}