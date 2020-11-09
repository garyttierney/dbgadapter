package com.github.garyttierney.ghidradbg.plugin

enum class DebuggerType(val id: String) {
    WinDbg("windbg"),
}

data class DebuggerConnection(val type: DebuggerType, val options: String)
