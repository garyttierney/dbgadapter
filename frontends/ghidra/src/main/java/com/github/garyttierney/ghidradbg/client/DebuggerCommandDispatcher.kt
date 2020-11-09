package com.github.garyttierney.ghidradbg.client

import com.github.garyttierney.ghidradbg.client.message.DebuggerCommand
import com.github.garyttierney.ghidradbg.client.message.DebuggerCommandRequest
import com.github.garyttierney.ghidradbg.client.message.DebuggerCommandResponse
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement

interface DebuggerCommandDispatcher {
    fun nextCommandSequence(): Long

    suspend fun dispatch(request: DebuggerCommandRequest): DebuggerCommandResponse
}

suspend inline fun <reified Request : DebuggerCommand<Response>, reified Response> DebuggerCommandDispatcher.runCommand(command: Request): Response {
    val request = DebuggerCommandRequest(nextCommandSequence(), Json.encodeToJsonElement(command))
    val response = dispatch(request)

    return Json.decodeFromJsonElement(response.response)
}
