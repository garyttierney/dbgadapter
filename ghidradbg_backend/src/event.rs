use serde::{Serialize, Deserialize};

use crate::state::{Register, StackTrace};

#[derive(Serialize, Deserialize)]
#[serde(tag = "type")]
#[serde(rename_all = "snake_case")]
pub enum DebuggerEvent {
    DebuggeeStateChange {
        instruction_offset: u64,
        registers: Vec<Register>,
        stack_trace: StackTrace
    },
    DebuggeeContinued
}
