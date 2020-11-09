use ghidradbg_backend::event::DebuggerEvent;
use ghidradbg_backend::Result;

use crate::dbgeng::control::DebuggerExecutionStatus;
use crate::dbgeng::event_callbacks::DebugeeStateChange;
use crate::dbgeng::DebugClient;

pub fn map_state_change(
    debugger: &mut DebugClient,
    _: DebugeeStateChange,
) -> Result<DebuggerEvent> {
    let control = debugger.control()?;
    let status = control.execution_status()?;

    if status == DebuggerExecutionStatus::Continue {
        return Ok(DebuggerEvent::DebuggeeContinued);
    }

    let symbols = debugger.symbols()?;
    let registers = debugger.registers()?;

    let abs_instruction_offset = registers.instruction_offset()?;
    let instruction_offset = symbols.relativize(abs_instruction_offset)?;

    let values = registers
        .all()?
        .into_iter()
        .filter(|reg| !reg.value.is_zeroed())
        .collect();

    let stack_trace = control.stack_trace(10)?;

    Ok(DebuggerEvent::DebuggeeStateChange {
        instruction_offset,
        registers: values,
        stack_trace,
    })
}
