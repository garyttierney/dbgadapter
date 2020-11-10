package com.github.garyttierney.ghidradbg.client

import com.github.garyttierney.ghidradbg.client.message.DebuggerCommand
import com.github.garyttierney.ghidradbg.client.message.DebuggerCommandResponse
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement

interface DebuggerCommandDispatcher {
    suspend fun dispatch(command: DebuggerCommand<*>): DebuggerCommandResponse
}

suspend inline fun <reified Request : DebuggerCommand<Response>, reified Response> DebuggerCommandDispatcher.runCommand(command: Request): Response {
    return Json.decodeFromJsonElement(dispatch(command).response)
}
