package com.github.garyttierney.ghidradbg.client.state

import kotlinx.serialization.Serializable

@Serializable
data class StackFrame(val index: Int, val instruction_offset: Long, val return_offset: Long, val params: List<Value>)

@Serializable
data class StackTrace(val frames: List<StackFrame>)
