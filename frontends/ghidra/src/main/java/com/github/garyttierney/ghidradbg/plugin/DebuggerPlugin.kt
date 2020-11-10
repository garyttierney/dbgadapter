package com.github.garyttierney.ghidradbg.plugin

import com.github.garyttierney.ghidradbg.client.Debugger
import com.github.garyttierney.ghidradbg.client.DebuggerClient
import com.github.garyttierney.ghidradbg.client.DebuggerEventListener
import com.github.garyttierney.ghidradbg.plugin.ui.DebuggerComponentProvider
import com.github.garyttierney.ghidradbg.plugin.ui.action.StopDebuggerAction
import com.github.garyttierney.ghidradbg.plugin.ui.action.step.SingleStepAction
import com.github.garyttierney.ghidradbg.program.DebuggedProgramStateChangeListener
import com.github.garyttierney.ghidradbg.program.DebuggedProgramStateProvider
import docking.action.DockingAction
import getService
import ghidra.app.plugin.PluginCategoryNames
import ghidra.app.plugin.ProgramPlugin
import ghidra.app.services.ConsoleService
import ghidra.app.services.GoToService
import ghidra.framework.plugintool.PluginInfo
import ghidra.framework.plugintool.PluginTool
import ghidra.framework.plugintool.util.PluginStatus
import ghidra.program.model.listing.Program

@PluginInfo(
    status = PluginStatus.UNSTABLE,
    packageName = "Debugger",
    category = PluginCategoryNames.DEBUGGER,
    shortDescription = "Debugger integration for Ghidra",
    description = "",
    servicesProvided = [Debugger::class, DebuggedProgramStateProvider::class],
    servicesRequired = [ConsoleService::class, GoToService::class]
)
class DebuggerPlugin(tool: PluginTool) : ProgramPlugin(tool, false, false) {

    private val programListeners = mutableMapOf<Program, DebuggerEventListener>()
    private val debugger = DebuggerImpl()

    private lateinit var console: ConsoleService
    private lateinit var goTo: GoToService

    init {
        registerServiceProvided(Debugger::class.java, debugger)
        registerServiceProvided(DebuggedProgramStateProvider::class.java, DebuggedProgramStateProvider.default())
    }

    override fun init() {
        console = tool.getService()
        goTo = tool.getService()

        val component = DebuggerComponentProvider(this)
        tool.addComponentProvider(component, true)

        actions.forEach { actionFactory ->
            // Ghidra doesn't allow the same action registered globally and locally,
            // so we just duplicate it.
            tool.addAction(this.actionFactory())
            tool.addLocalAction(component, this.actionFactory())
        }

        debugger.addEventListener(object : DebuggerEventListener {
            override suspend fun onLogMessage(message: String) {
                console.println(message)
            }
        })
    }

    override fun programClosed(program: Program) {
        programListeners[program]?.let { debugger.removeEventListener(it) }
    }

    override fun programOpened(program: Program) {
        debugger.addEventListener(programListeners.computeIfAbsent(program) {
            DebuggedProgramStateChangeListener(
                tool,
                program,
                goTo
            )
        })
    }

    companion object {
        private val actions : List<DebuggerPlugin.() -> DockingAction> = listOf(
            { SingleStepAction(debugger, name ) },
            { StopDebuggerAction(debugger, name) }
        )
    }
}