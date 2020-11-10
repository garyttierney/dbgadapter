package com.github.garyttierney.ghidradbg.client

import com.github.garyttierney.ghidradbg.plugin.DebuggerConnection

interface Debugger : DebuggerEventProducer, DebuggerCommandDispatcher {
    val isAttached: Boolean

    fun attach(connection: DebuggerConnection)

    fun detach()
}