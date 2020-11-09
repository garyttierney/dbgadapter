package com.github.garyttierney.ghidradbg.client

import com.github.garyttierney.ghidradbg.client.launcher.*
import com.github.garyttierney.ghidradbg.client.message.DebuggerCommandRequest
import com.github.garyttierney.ghidradbg.client.message.DebuggerCommandResponse
import com.github.garyttierney.ghidradbg.client.message.DebuggerNotification
import com.github.garyttierney.ghidradbg.plugin.DebuggerConnection
import com.github.garyttierney.ghidradbg.plugin.DebuggerEventListener
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.concurrent.Executors
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class DebuggerClient : DebuggerEventProducer, DebuggerCommandDispatcher {

    private val commandContinuations = mutableMapOf<Long, Continuation<DebuggerCommandResponse>>()
    private var commandSequence: Long = 0
    private val commands = Channel<DebuggerCommandRequest>(10)
    private val notifications = Channel<DebuggerNotification>(1)

    val isRunning: Boolean
        get() = activeJob?.isActive ?: false

    private val workExecutor = CoroutineScope(Executors.newFixedThreadPool(3).asCoroutineDispatcher())
    private val listeners = mutableListOf<DebuggerEventListener>()
    private var activeJob: Job? = null

    private suspend fun withListeners(callback: suspend DebuggerEventListener.() -> Unit) {
        for (listener in listeners) {
            listener.callback()
        }
    }


    private fun whileAliveRunInPool(process: Process, runnable: suspend CoroutineScope.() -> Boolean) {
        workExecutor.launch {
            while (process.isAlive) {
                if (!runnable()) {
                    break
                }
            }
        }
    }

    fun runWithScope(executable: String, connection: DebuggerConnection, scope: CoroutineScope) {
        activeJob?.cancel()
        activeJob = scope.launch {
            run(executable, connection)
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun run(executable: String, connection: DebuggerConnection) {
        val proc = DebuggerClientLauncher.launch("ghidra-dbg", connection.type.id, connection.options)
        val errors = proc.errorStream.bufferedReader()
        val input = proc.inputStream.bufferedReader()
        val output = proc.outputStream.bufferedWriter()

        whileAliveRunInPool(proc) {
            val next = errors.readLine() ?: return@whileAliveRunInPool false
            withListeners { onLogMessage(next) }

            true
        }

        whileAliveRunInPool(proc) {
            val next = input.readLine() ?: return@whileAliveRunInPool false
            notifications.send(Json.decodeFromString(next))

            true
        }

        whileAliveRunInPool(proc) {
            for (cmd in commands) {
                output.write(Json.encodeToString(cmd))
            }

            true
        }

        for (notification in notifications) {
            if (notification is DebuggerNotification.Event) {
                when (val event = notification.event) {
                    is DebuggeeStateChange -> withListeners { onDebuggeeStateChange(event) }
                }
            } else if (notification is DebuggerNotification.Command) {
                val response = notification.response
                val id = response.requestId
                val continuation = commandContinuations.remove(id)

                continuation?.resume(notification.response)
            }
        }
    }

    override fun nextCommandSequence() = commandSequence++

    override suspend fun dispatch(request: DebuggerCommandRequest): DebuggerCommandResponse {
        commands.send(request)

        return suspendCoroutine {
            commandContinuations[request.id] = it
        }
    }

    override fun addEventListener(listener: DebuggerEventListener) {
        listeners.add(listener)
    }

    override fun removeEventListener(listener: DebuggerEventListener) {
        listeners.remove(listener)
    }
}