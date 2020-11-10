package com.github.garyttierney.ghidradbg.plugin

import com.github.garyttierney.ghidradbg.client.Debugger
import com.github.garyttierney.ghidradbg.client.DebuggerClient
import com.github.garyttierney.ghidradbg.client.DebuggerEventProducer
import com.github.garyttierney.ghidradbg.client.DelegateDebuggerEventListener
import com.github.garyttierney.ghidradbg.client.launcher.DebuggerClientLauncher
import com.github.garyttierney.ghidradbg.client.message.DebuggerCommand

class DebuggerImpl(private val eventListener: DelegateDebuggerEventListener) : Debugger, DebuggerEventProducer by eventListener {

    constructor() : this(DelegateDebuggerEventListener())

    var client: DebuggerClient? = null

    override val isAttached: Boolean
        get() = client?.isActive ?: false

    override fun attach(connection: DebuggerConnection) {
        if (isAttached) {
            error("Debugger must be detached before reconnecting")
        }

        client = DebuggerClientLauncher.launch(connection, eventListener)
    }

    override fun detach() {
        client?.shutdown()
    }

    override suspend fun dispatch(command: DebuggerCommand<*>) = client?.dispatch(command) ?: error("Debugger is not attached")
}