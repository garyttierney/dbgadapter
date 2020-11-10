package com.github.garyttierney.ghidradbg.plugin.ui.action

import com.github.garyttierney.ghidradbg.client.Debugger
import docking.ActionContext
import docking.action.DockingAction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

abstract class DebuggerCommandAction(protected val debugger: Debugger, name: String, owner: String) : DockingAction(name, owner, true) {
    init {
        enabledWhen { debugger.isAttached }
    }

    override fun actionPerformed(context: ActionContext) {
        GlobalScope.launch(Dispatchers.IO) {
            actionPerformedAsync(context)
        }
    }

    abstract suspend fun actionPerformedAsync(context: ActionContext)

}