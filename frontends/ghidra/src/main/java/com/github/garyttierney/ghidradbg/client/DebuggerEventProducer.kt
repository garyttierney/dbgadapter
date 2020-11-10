package com.github.garyttierney.ghidradbg.client

interface DebuggerEventProducer {

    fun addEventListener(listener: DebuggerEventListener)

    fun removeEventListener(listener: DebuggerEventListener)
}