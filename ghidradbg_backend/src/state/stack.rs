use serde::{Deserialize, Serialize};

#[derive(Serialize, Deserialize)]
#[serde(rename_all = "snake_case")]
pub struct StackTrace {
    frames: Vec<StackFrame>
}

impl StackTrace {
    pub fn new(frames: Vec<StackFrame>) -> Self {
        Self { frames }
    }
}

#[derive(Serialize, Deserialize)]
#[serde(rename_all = "snake_case")]
pub struct StackFrame {
    instruction_offset: u64,
    return_offset: u64,
    params: Vec<u64>,
}

impl StackFrame {
    pub fn new(instruction_offset: u64, return_offset: u64) -> Self {
        Self {
            instruction_offset,
            return_offset,
            params: vec![],
        }
    }
}