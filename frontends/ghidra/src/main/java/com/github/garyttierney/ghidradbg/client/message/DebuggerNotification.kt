package com.github.garyttierney.ghidradbg.client.message

import com.github.garyttierney.ghidradbg.client.launcher.DebuggerEvent
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class DebuggerNotification {
    @Serializable
    @SerialName("notification")
    data class Event(val event: DebuggerEvent) : DebuggerNotification()

    @Serializable
    @SerialName("command")
    data class Command(val response: DebuggerCommandResponse) : DebuggerNotification()
}