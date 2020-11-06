use serde::{Serialize, Deserialize};

#[derive(Debug, Serialize, Deserialize)]
pub struct Register {
    pub name: String,
    pub index: u32,
    pub data: Vec<u8>,
}