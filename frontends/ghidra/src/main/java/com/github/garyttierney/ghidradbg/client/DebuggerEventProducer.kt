package com.github.garyttierney.ghidradbg.client

import com.github.garyttierney.ghidradbg.plugin.DebuggerEventListener

interface DebuggerEventProducer {

    fun addEventListener(listener: DebuggerEventListener)

    fun removeEventListener(listener: DebuggerEventListener)
}