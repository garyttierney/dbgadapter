use serde::{Deserialize, Serialize};

use crate::state::{Register, RelativeAddress, StackTrace};

#[derive(Serialize, Deserialize)]
#[serde(tag = "type")]
#[serde(rename_all = "snake_case")]
pub enum DebuggerEvent {
    DebuggeeStateChange {
        instruction_offset: RelativeAddress,
        registers: Vec<Register>,
        stack_trace: StackTrace,
    },
    DebuggeeContinued,
}
