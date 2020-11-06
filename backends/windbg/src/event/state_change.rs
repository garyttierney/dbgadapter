use ghidradbg_backend::event::DebuggerEvent;
use ghidradbg_backend::Result;

use crate::dbgeng::event::DebugeeStateChange;
use crate::dbgeng::control::DebuggerExecutionStatus;
use crate::dbgeng::DebugClient;

pub fn map_state_change(debugger: &mut DebugClient, _: DebugeeStateChange) -> Result<DebuggerEvent> {
    let control = debugger.control()?;
    let status = control.execution_status()?;

    if status == DebuggerExecutionStatus::Continue {
        return Ok(DebuggerEvent::DebuggeeContinued);
    }

    let registers = debugger.registers()?;
    let instruction_offset = registers.instruction_offset()?;
    let values = registers
        .all()?
        .into_iter()
        .filter(|reg| reg.data.iter().any(|v| *v != 0))
        .collect();

    let stack_trace = control.stack_trace(10)?;

    Ok(DebuggerEvent::DebuggeeStateChange {
        instruction_offset,
        registers: values,
        stack_trace,
    })
}