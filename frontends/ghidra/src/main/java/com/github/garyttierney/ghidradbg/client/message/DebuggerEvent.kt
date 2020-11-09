package com.github.garyttierney.ghidradbg.client.launcher

import com.github.garyttierney.ghidradbg.client.state.Register
import com.github.garyttierney.ghidradbg.client.state.RelativeAddress
import com.github.garyttierney.ghidradbg.client.state.StackTrace
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("event")
sealed class DebuggerEvent

@Serializable
@SerialName("debuggee_state_change")
data class DebuggeeStateChange(
    @SerialName("instruction_offset") val instructionOffset: RelativeAddress,
    @SerialName("registers") val registers: List<Register>,
    @SerialName("stack_trace") val stacktrace: StackTrace,
) : DebuggerEvent()

@Serializable
@SerialName("debuggee_continued")
object DebuggeeContinued : DebuggerEvent()