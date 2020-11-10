package com.github.garyttierney.ghidradbg.plugin.ui.action

import com.github.garyttierney.ghidradbg.client.Debugger
import com.github.garyttierney.ghidradbg.plugin.ui.DebuggerIcons
import docking.ActionContext
import docking.action.DockingAction
import docking.action.ToolBarData
import ghidra.app.plugin.core.functionwindow.FunctionWindowProvider.icon

class StopDebuggerAction(val debugger: Debugger, owner: String) : DockingAction("Stop Debugger", owner) {
    init {
        enabledWhen { debugger.isAttached }
        description = "Detach from debugger session"
        toolBarData = ToolBarData(DebuggerIcons.DETACH_ICON, "Debug")
    }

    override fun actionPerformed(p0: ActionContext?) {
        debugger.detach()
    }

}