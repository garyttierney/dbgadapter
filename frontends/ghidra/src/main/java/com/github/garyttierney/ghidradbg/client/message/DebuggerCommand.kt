package com.github.garyttierney.ghidradbg.client.message

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
sealed class DebuggerCommand<Response>

@Serializable
@SerialName("single_step")
object SingleStep : DebuggerCommand<Unit>()

@Serializable
data class DebuggerCommandRequest(
    val id: Long,
    val command: JsonElement
)

@Serializable
data class DebuggerCommandResponse(
    @property:SerialName("request_id") val requestId: Long,
    val response: JsonElement
)