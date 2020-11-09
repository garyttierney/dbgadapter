use serde::{Deserialize, Serialize};

use crate::state::Value;

#[derive(Debug, Serialize, Deserialize)]
pub struct Register {
    pub name: String,
    pub index: u32,
    pub value: Value,
}
