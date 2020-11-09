use crate::dbgeng::WinDbgError;
use ghidradbg_backend::state::RelativeAddress;
use winapi::shared::winerror::FAILED;
use winapi::um::dbgeng::{IDebugSymbols5, DEBUG_GETMOD_NO_UNLOADED_MODULES};

pub struct DebugSymbols<'a> {
    inner: &'a mut IDebugSymbols5,
}

impl<'a> DebugSymbols<'a> {
    pub fn new(inner: &'a mut IDebugSymbols5) -> Self {
        Self { inner }
    }

    pub fn relativize(&self, addr: u64) -> Result<RelativeAddress, WinDbgError> {
        unsafe {
            let mut base = 0u64;
            let error = self.inner.GetModuleByOffset2(
                addr,
                0,
                DEBUG_GETMOD_NO_UNLOADED_MODULES,
                std::ptr::null_mut(),
                &mut base as *mut _,
            );

            if FAILED(error) {
                return Err(WinDbgError::FfiError(
                    "Unable to get module by offset",
                    error,
                ));
            }

            Ok(RelativeAddress {
                base,
                displacement: (addr - base) as u32,
            })
        }
    }
}
