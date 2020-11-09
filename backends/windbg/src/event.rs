use ghidradbg_backend::{DebuggerEvent, Result};
pub use state_change::*;

use crate::dbgeng::event_callbacks::WinDbgEvent;
use crate::dbgeng::DebugClient;

mod state_change;

pub fn translate(client: &mut DebugClient, event: WinDbgEvent) -> Result<Option<DebuggerEvent>> {
    Ok(match event {
        WinDbgEvent::DebugeeStateChange(change) if change.is_complete_change() => {
            Some(state_change::map_state_change(client, change)?)
        }
        _ => None,
    })
}
