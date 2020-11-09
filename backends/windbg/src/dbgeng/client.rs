use std::ffi::OsString;
use std::os::windows::ffi::OsStrExt;

use tokio::sync::mpsc::{channel, Receiver};
use winapi::ctypes::c_void;
use winapi::shared::guiddef::IID;
use winapi::shared::winerror::{S_FALSE, S_OK};
use winapi::um::dbgeng::{
    DebugConnectWide, IDebugClient, IDebugControl7, IDebugEventCallbacks, IID_IDebugClient,
    IID_IDebugControl7, IID_IDebugRegisters2, IID_IDebugSymbols5,
    DEBUG_EVENT_CHANGE_DEBUGGEE_STATE,
};

use crate::dbgeng::control::DebugControl;
use crate::dbgeng::event_callbacks::DebugEventInterestFlags;
use crate::dbgeng::{DebugEventCallbacks, WinDbgError};

use super::{event_callbacks::WinDbgEvent, registers::Registers};
use crate::dbgeng::symbols::DebugSymbols;

#[derive(Clone)]
pub struct DebugClient<'a> {
    inner: &'a IDebugClient,
}

unsafe impl<'a> Send for DebugClient<'a> {}

impl<'a> DebugClient<'a> {
    /// Connect to a remote debug server and join an existing debugging session. The debugger
    /// **must** already be attached to a process before connecting.
    ///
    /// Expects a WinDbg connection string in `connection_string`, taking the same format as
    /// the arguments passed to the `.server` command. Examples:
    ///
    ///     tcp:Port=3333,Server=localhost
    ///     npipe:Pipe=DbgX_35d0e593f3bd46c0900717235f6fd973
    pub fn connect<S>(connection_string: S) -> Result<DebugClient<'a>, WinDbgError>
    where
        S: AsRef<str>,
    {
        let host: Vec<u16> = OsString::from(connection_string.as_ref())
            .encode_wide()
            .collect();

        let client = unsafe {
            let mut debug_client: *mut IDebugClient = std::ptr::null_mut();

            let res = DebugConnectWide(
                host.as_ptr(),
                &IID_IDebugClient,
                &mut debug_client as *mut _ as *mut *mut c_void,
            );

            if res != S_OK {
                return Err(WinDbgError::FfiError(
                    "Unable to connect to debug server",
                    res,
                ));
            };

            let debug_client = &mut *debug_client;
            let res = debug_client.ConnectSession(0, 1024);

            if res != S_OK {
                return Err(WinDbgError::FfiError("Unable to join debug session", res));
            }

            debug_client
        };

        Ok(DebugClient { inner: client })
    }

    /// Create a channel to receive debug events from WinDbg and set the event callback implementation
    /// to send to this channel. Only event types in `DebugEventInterestFlags` will be sent.
    pub fn events(&'a self, interest_mask: DebugEventInterestFlags) -> Receiver<WinDbgEvent> {
        let (tx, rx) = channel(10);
        let callbacks = DebugEventCallbacks::new(interest_mask.bits(), tx);

        unsafe {
            self.inner
                .SetEventCallbacks(Box::into_raw(Box::new(callbacks)) as *mut _);
        }

        rx
    }

    pub fn symbols(&'a self) -> Result<DebugSymbols<'a>, WinDbgError> {
        Ok(DebugSymbols::new(self.query_interface(IID_IDebugSymbols5)?))
    }

    pub fn control(&'a self) -> Result<DebugControl<'a>, WinDbgError> {
        Ok(DebugControl::new(self.query_interface(IID_IDebugControl7)?))
    }

    pub fn registers(&'a self) -> Result<Registers<'a>, WinDbgError> {
        Ok(Registers::new(self.query_interface(IID_IDebugRegisters2)?))
    }

    pub fn dispatch_callbacks(&self) -> Result<(), WinDbgError> {
        unsafe {
            let result = self.inner.DispatchCallbacks(50);

            if result != S_OK && result != S_FALSE {
                return Err(WinDbgError::FfiError(
                    "Error occurred while dispatching callbacks",
                    result,
                ));
            }
        }

        Ok(())
    }

    fn query_interface<I: Sized>(&self, iid: IID) -> Result<&'a mut I, WinDbgError> {
        unsafe {
            let mut interface: *mut I = std::ptr::null_mut();

            let err = self
                .inner
                .QueryInterface(&iid, &mut interface as *mut _ as *mut *mut c_void);

            if err != S_OK {
                return Err(WinDbgError::FfiError(
                    "Unable to query for COM interface",
                    err,
                ));
            }

            Ok(&mut *interface)
        }
    }
}
