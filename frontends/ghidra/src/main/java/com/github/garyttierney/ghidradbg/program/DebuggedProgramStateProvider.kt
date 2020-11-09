package com.github.garyttierney.ghidradbg.program

import ghidra.program.model.listing.Program

interface DebuggedProgramStateProvider {
    fun provide(program: Program): DebuggedProgramState

    companion object {
        private val state = mutableMapOf<Program, DebuggedProgramState>()

        fun default(): DebuggedProgramStateProvider {
            return object : DebuggedProgramStateProvider {
                override fun provide(program: Program): DebuggedProgramState {
                    return state.computeIfAbsent(program) { DebuggedProgramState(program) }
                }
            }
        }
    }
}