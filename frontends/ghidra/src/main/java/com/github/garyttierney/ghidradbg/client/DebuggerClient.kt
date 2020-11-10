package com.github.garyttierney.ghidradbg.client

import com.github.garyttierney.ghidradbg.client.message.DebuggerCommand
import com.github.garyttierney.ghidradbg.client.message.DebuggerCommandRequest
import com.github.garyttierney.ghidradbg.client.message.DebuggerCommandResponse
import com.github.garyttierney.ghidradbg.client.message.DebuggerNotification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.onReceiveOrNull
import kotlinx.coroutines.isActive
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.selects.select

class DebuggerClient(
    private val coroutineScope: CoroutineScope,
    private val commands: SendChannel<DebuggerCommandRequest>,
    private val notifications: ReceiveChannel<DebuggerNotification>,
    private val logs: ReceiveChannel<String>,
    private val listener: DebuggerEventListener
) : DebuggerCommandDispatcher {

    private val commandContinuations = mutableMapOf<Long, Continuation<DebuggerCommandResponse>>()
    private var commandSequence: Long = 0

    val isActive
        get() = coroutineScope.isActive

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun run() {
        while (isActive) {
            select<Unit> {
                notifications.onReceiveOrNull { notification ->
                    when (notification) {
                        is DebuggerNotification.Event -> listener.onDebuggerEvent(notification.event)
                        is DebuggerNotification.Command -> {
                            val response = notification.response
                            val id = response.requestId
                            val continuation = commandContinuations.remove(id)

                            continuation?.resume(notification.response)
                        }
                        null -> shutdown()
                    }
                }

                logs.onReceiveOrNull { logMessage ->
                    logMessage?.let { listener.onLogMessage(logMessage) } ?: shutdown()
                }
            }
        }
    }

    fun shutdown() {
        coroutineScope.cancel()
    }

    override suspend fun dispatch(command: DebuggerCommand<*>): DebuggerCommandResponse {
        val request = DebuggerCommandRequest(commandSequence++, Json.encodeToJsonElement(command))

        commands.send(request)

        return suspendCoroutine {
            commandContinuations[request.id] = it
        }
    }
}