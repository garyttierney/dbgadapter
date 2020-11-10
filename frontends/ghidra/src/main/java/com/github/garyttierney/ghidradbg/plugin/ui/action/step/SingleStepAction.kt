package com.github.garyttierney.ghidradbg.plugin.ui.action.step

import com.github.garyttierney.ghidradbg.client.Debugger
import com.github.garyttierney.ghidradbg.client.message.SingleStep
import com.github.garyttierney.ghidradbg.client.runCommand
import com.github.garyttierney.ghidradbg.plugin.ui.DebuggerIcons
import com.github.garyttierney.ghidradbg.plugin.ui.action.DebuggerCommandAction
import docking.ActionContext
import docking.action.MenuData
import docking.action.ToolBarData
import resources.ResourceManager

class SingleStepAction(debugger: Debugger, owner: String) : DebuggerCommandAction(debugger, "SingleStep", owner) {
    init {
        toolBarData = ToolBarData(DebuggerIcons.STEP_ICON, "Debug")
        menuBarData = MenuData(arrayOf("Step into"), DebuggerIcons.STEP_ICON, "2")
        description = "Step to the next instruction"
    }

    override suspend fun actionPerformedAsync(context: ActionContext) {
        debugger.runCommand(SingleStep)
    }
}