use ghidradbg_backend::command::SingleStep;
use ghidradbg_backend::Result;

use crate::dbgeng::control::DebuggerExecutionStatus;
use crate::dbgeng::DebugClient;

pub fn single_step(client: &mut DebugClient, _cmd: SingleStep) -> Result<()> {
    let ctrl = client.control()?;
    ctrl.set_execution_status(DebuggerExecutionStatus::StepInto)?;

    Ok(())
}
