package com.github.garyttierney.ghidradbg.plugin.ui

import com.github.garyttierney.ghidradbg.plugin.*
import docking.ComponentProvider
import getService
import ghidra.framework.plugintool.Plugin
import resources.ResourceManager
import javax.swing.Icon
import javax.swing.JComponent

class DebuggerComponentProvider(private val plugin: DebuggerPlugin) : ComponentProvider(plugin.tool, "Debugger", plugin.name) {

    private val debugger: Debugger
    private val component: DebuggerComponent

    init {
        debugger = plugin.tool.getService<Debugger>()
        component = DebuggerComponent(this::onAttach)
    }

    fun onAttach() {
        val attachConfig = component.attachConfiguration.text
        if (attachConfig.isNotBlank()) {
            debugger.attach(DebuggerConnection(DebuggerType.WinDbg, attachConfig))
        }
    }

    override fun getIcon() = DEBUGGER_ICON

    override fun getComponent() = component

    companion object {
        private val DEBUGGER_ICON = ResourceManager.loadImage("images/icons/startDebugger.png")
    }
}