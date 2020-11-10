package com.github.garyttierney.ghidradbg.plugin.ui

import com.github.garyttierney.ghidradbg.client.Debugger
import com.github.garyttierney.ghidradbg.plugin.DebuggerConnection
import com.github.garyttierney.ghidradbg.plugin.DebuggerPlugin
import com.github.garyttierney.ghidradbg.plugin.DebuggerType
import docking.ComponentProvider
import getService
import resources.ResourceManager

class DebuggerComponentProvider(private val plugin: DebuggerPlugin) : ComponentProvider(plugin.tool, "Debugger", plugin.name) {

    private val debugger: Debugger
    private val component: DebuggerComponent

    init {
        debugger = plugin.tool.getService()
        component = DebuggerComponent(this::onAttach)
    }

    fun onAttach() {
        val attachConfig = component.attachConfiguration.text
        if (attachConfig.isNotBlank()) {
            debugger.attach(DebuggerConnection(DebuggerType.WinDbg, attachConfig))
        }
    }

    override fun getIcon() = DebuggerIcons.DEBUGGER_ICON

    override fun getComponent() = component
}