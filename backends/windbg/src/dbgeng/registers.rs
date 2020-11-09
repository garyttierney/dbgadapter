use winapi::shared::winerror::S_OK;
use winapi::{
    shared::winerror::FAILED, um::dbgeng::IDebugRegisters2, um::dbgeng::DEBUG_REGISTER_DESCRIPTION,
    um::dbgeng::DEBUG_VALUE,
};

use ghidradbg_backend::state::{Register, Value};

use super::WinDbgError;

pub struct Registers<'a> {
    pub inner: &'a mut IDebugRegisters2,
}

impl<'a> Registers<'a> {
    pub fn new(inner: &'a mut IDebugRegisters2) -> Self {
        Self { inner }
    }

    pub fn instruction_offset(&self) -> Result<u64, WinDbgError> {
        let mut ip = 0u64;

        unsafe {
            let res = self.inner.GetInstructionOffset(&mut ip as *mut _);

            if FAILED(res) {
                return Err(WinDbgError::FfiError(
                    "Unable to get instruction offset",
                    res,
                ));
            }
        }

        Ok(ip)
    }

    pub fn all(&self) -> Result<Vec<Register>, WinDbgError> {
        Ok(unsafe {
            let mut register_count = 0u32;

            let res = self.inner.GetNumberRegisters(&mut register_count as *mut _);
            if FAILED(res) {
                return Err(WinDbgError::FfiError(
                    "Unable to determine nmber of registers",
                    res,
                ));
            }

            let mut registers = Vec::with_capacity(register_count as usize);

            for register in 0..register_count {
                let mut name_buf = vec![0u8; 64];
                let mut name_len = 0u32;
                let mut desc: DEBUG_REGISTER_DESCRIPTION = std::mem::zeroed();

                let res = self.inner.GetDescription(
                    register as u32,
                    name_buf.as_mut_ptr() as *mut _,
                    name_buf.len() as u32,
                    &mut name_len as *mut _,
                    &mut desc as *mut _,
                );

                if res != S_OK {
                    return Err(WinDbgError::FfiError(
                        "Unable to get register information",
                        res,
                    ));
                }

                if desc.SubregMaster != 0 {
                    continue;
                }

                let mut value: DEBUG_VALUE = std::mem::zeroed();
                let res = self.inner.GetValue(register, &mut value as *mut _);

                if FAILED(res) {
                    return Err(WinDbgError::FfiError("Unable to get register value", res));
                }

                let raw_value = value.u.RawBytes();

                registers.push(Register {
                    name: std::ffi::CStr::from_bytes_with_nul(&name_buf[..name_len as usize])
                        .map(|cstr| cstr.to_str().unwrap())
                        .map_err(|_| WinDbgError::UnknownError)?
                        .to_string(),
                    index: register,
                    value: Value::new(raw_value),
                })
            }

            registers
        })
    }
}
