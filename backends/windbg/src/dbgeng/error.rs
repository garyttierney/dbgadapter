use std::os::raw::{c_long, c_ulong};
use std::ptr;

use ghidradbg_backend::DebuggerError;
use winapi::shared::winerror::HRESULT;
use winapi::um::winbase::*;

impl From<WinDbgError> for DebuggerError {
    fn from(error: WinDbgError) -> Self {
        DebuggerError::Other(error.into())
    }
}

#[derive(thiserror::Error, Debug)]
pub enum WinDbgError {
    #[error("Unknown error")]
    UnknownError,

    #[error("{}. {}", .0, error_string(*.1))]
    FfiError(&'static str, HRESULT),
}

fn error_string(errnum: c_long) -> String {
    let mut buf = [0 as u16; 2048];
    unsafe {
        let res = FormatMessageW(
            FORMAT_MESSAGE_FROM_SYSTEM | FORMAT_MESSAGE_IGNORE_INSERTS,
            ptr::null_mut(),
            errnum as c_ulong,
            0,
            buf.as_mut_ptr(),
            buf.len() as c_ulong,
            ptr::null_mut(),
        );
        if res == 0 {
            // Sometimes FormatMessageW can fail e.g. system doesn't like langId,
            // let fm_err = errno();
            return format!("OS Error {} (FormatMessageW() returned error)", errnum);
        }

        let b = buf.iter().position(|&b| b == 0).unwrap_or(buf.len());
        let msg = String::from_utf16(&buf[..b]);
        match msg {
            Ok(msg) => msg.trim_end().to_string(),
            Err(..) => format!(
                "OS Error {} (FormatMessageW() returned \
                                invalid UTF-16)",
                errnum
            ),
        }
    }
}
