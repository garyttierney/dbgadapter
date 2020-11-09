use serde::{Deserialize, Serialize};

#[derive(Debug, Serialize, Deserialize)]
pub struct RelativeAddress {
    pub base: u64,
    pub displacement: u32,
}

#[derive(Debug, Serialize, Deserialize)]
pub struct Value {
    data: Vec<u8>,
}

impl Value {
    pub fn new(data: &[u8]) -> Self {
        Self {
            data: data.to_vec(),
        }
    }

    pub fn is_zeroed(&self) -> bool {
        !self.data.iter().any(|v| *v != 0)
    }
}

impl From<u64> for Value {
    fn from(wrapped: u64) -> Self {
        let bytes: [u8; 8] = unsafe { std::mem::transmute(wrapped) };

        Self {
            data: bytes.to_vec(),
        }
    }
}
