package com.github.garyttierney.ghidradbg.program

import com.github.garyttierney.ghidradbg.client.DebuggerEventListener
import com.github.garyttierney.ghidradbg.client.launcher.DebuggeeStateChange
import com.github.garyttierney.ghidradbg.client.launcher.DebuggerEvent
import ghidra.app.cmd.register.SetRegisterCmd
import ghidra.app.plugin.core.analysis.AutoAnalysisManager
import ghidra.app.services.GoToService
import ghidra.framework.cmd.CompoundCmd
import ghidra.framework.plugintool.PluginTool
import ghidra.program.model.listing.Program
import java.math.BigInteger

class DebuggedProgramStateChangeListener(
    private val tool: PluginTool,
    private val program: Program,
    private val goTo: GoToService
) :
    DebuggerEventListener {

    override suspend fun onDebuggerEvent(event: DebuggerEvent) = when (event) {
        is DebuggeeStateChange -> stateChanged(event)
        else -> Unit
    }

    private fun stateChanged(change: DebuggeeStateChange) {
        val startAddress = program.imageBase.addNoWrap(change.instructionOffset.displacement)
        val function = program.functionManager.getFunctionAt(startAddress)

        goTo.goTo(startAddress)

        if (function == null) {
            return
        }

        val cmd = CompoundCmd("Set Register Values")

        for (register in change.registers) {
            val programRegister = program.getRegister(register.name) ?: continue

            val data = register.value.data
            data.reverse()

            // TODO: this assumes the data from the target is in little endian, as the JVM wants it in big endian
            //       we should check the endianness of the target to do this correctly.
            val value = BigInteger(data)

            cmd.add(
                SetRegisterCmd(
                    programRegister,
                    function.entryPoint,
                    function.body.maxAddress,
                    value
                )
            )
        }

        if (tool.execute(cmd, program)) {
            val analysisManger = AutoAnalysisManager.getAnalysisManager(program)
            analysisManger.reAnalyzeAll(function.body)
        }
    }
}