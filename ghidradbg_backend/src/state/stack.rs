use crate::state::Value;
use serde::{Deserialize, Serialize};

#[derive(Serialize, Deserialize)]
#[serde(rename_all = "snake_case")]
pub struct StackTrace {
    frames: Vec<StackFrame>,
}

impl StackTrace {
    pub fn new(frames: Vec<StackFrame>) -> Self {
        Self { frames }
    }
}

#[derive(Serialize, Deserialize)]
#[serde(rename_all = "snake_case")]
pub struct StackFrame {
    index: u32,
    instruction_offset: u64,
    return_offset: u64,
    params: Vec<Value>,
}

impl StackFrame {
    pub fn new(
        index: u32,
        instruction_offset: u64,
        return_offset: u64,
        params: Vec<Value>,
    ) -> Self {
        Self {
            index,
            instruction_offset,
            return_offset,
            params,
        }
    }
}
