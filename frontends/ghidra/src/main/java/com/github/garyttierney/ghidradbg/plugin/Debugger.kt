package com.github.garyttierney.ghidradbg.plugin

import com.github.garyttierney.ghidradbg.client.DebuggerCommandDispatcher
import com.github.garyttierney.ghidradbg.client.DebuggerEventProducer

interface Debugger : DebuggerEventProducer, DebuggerCommandDispatcher {
    val isAttached: Boolean

    fun attach(debugger: DebuggerConnection)
}