package com.github.garyttierney.ghidradbg.plugin

import com.github.garyttierney.ghidradbg.client.DebuggerClient
import com.github.garyttierney.ghidradbg.client.DebuggerCommandDispatcher
import com.github.garyttierney.ghidradbg.client.DebuggerEventProducer
import kotlinx.coroutines.GlobalScope

class DebuggerImpl(val client: DebuggerClient) :
    Debugger,
    DebuggerEventProducer by client,
    DebuggerCommandDispatcher by client {

    override val isAttached: Boolean
        get() = client.isRunning

    override fun attach(connection: DebuggerConnection) {
        client.runWithScope("ghidra-dbg", connection, scope = GlobalScope)
    }
}