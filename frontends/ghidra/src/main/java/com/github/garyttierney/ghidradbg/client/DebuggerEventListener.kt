package com.github.garyttierney.ghidradbg.client

import com.github.garyttierney.ghidradbg.client.launcher.DebuggerEvent

interface DebuggerEventListener {
    suspend fun onDebuggerEvent(event: DebuggerEvent) = Unit
    suspend fun onLogMessage(message: String) = Unit
}