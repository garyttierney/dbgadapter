package com.github.garyttierney.ghidradbg.client

import com.github.garyttierney.ghidradbg.client.launcher.DebuggerEvent

class DelegateDebuggerEventListener : DebuggerEventListener, DebuggerEventProducer {
    private val listeners = mutableListOf<DebuggerEventListener>()

    override fun addEventListener(listener: DebuggerEventListener) {
        listeners.add(listener)
    }

    override fun removeEventListener(listener: DebuggerEventListener) {
        listeners.remove(listener)
    }

    override suspend fun onDebuggerEvent(event: DebuggerEvent) {
       for (listener in listeners) {
           listener.onDebuggerEvent(event)
       }
    }

    override suspend fun onLogMessage(message: String) {
        for (listener in listeners) {
            listener.onLogMessage(message)
        }
    }
}