use crate::dbgeng::WinDbgError;
use ghidradbg_backend::state::{StackFrame, StackTrace, Value};
use winapi::shared::winerror::FAILED;
use winapi::um::dbgeng::{
    IDebugControl7, DEBUG_STATUS_BREAK, DEBUG_STATUS_GO, DEBUG_STATUS_STEP_BRANCH,
    DEBUG_STATUS_STEP_INTO, DEBUG_STATUS_STEP_OVER,
};

#[repr(u32)]
#[derive(Eq, PartialEq)]
pub enum DebuggerExecutionStatus {
    Break = DEBUG_STATUS_BREAK,
    StepInto = DEBUG_STATUS_STEP_INTO,
    StepToBranch = DEBUG_STATUS_STEP_BRANCH,
    StepOver = DEBUG_STATUS_STEP_OVER,
    Continue = DEBUG_STATUS_GO,
}

pub struct DebugControl<'a> {
    inner: &'a mut IDebugControl7,
}

impl<'a> DebugControl<'a> {
    pub fn new(inner: &'a mut IDebugControl7) -> Self {
        Self { inner }
    }

    pub fn set_execution_status(&self, status: DebuggerExecutionStatus) -> Result<(), WinDbgError> {
        unsafe {
            let error = self.inner.SetExecutionStatus(status as _);

            if FAILED(error) {
                return Err(WinDbgError::FfiError(
                    "Unable to set execution status",
                    error,
                ));
            }
        }

        Ok(())
    }

    pub fn execution_status(&self) -> Result<DebuggerExecutionStatus, WinDbgError> {
        unsafe {
            let mut execution_status = 0u32;
            let res = self
                .inner
                .GetExecutionStatus(&mut execution_status as *mut _);

            if FAILED(res) {
                return Err(WinDbgError::FfiError(
                    "Unable to get current execution status",
                    res,
                ));
            }

            Ok(std::mem::transmute(execution_status))
        }
    }

    pub fn stack_trace(&self, num_frames: u32) -> Result<StackTrace, WinDbgError> {
        let frames: Vec<StackFrame> = unsafe {
            let mut frames = Vec::with_capacity(num_frames as usize);
            let mut frames_filled = 0u32;

            let res = self.inner.GetStackTrace(
                0,
                0,
                0,
                frames.as_mut_ptr(),
                num_frames,
                &mut frames_filled as *mut _,
            );

            log::info!("{} frames produced in stacktrace", frames_filled);

            if FAILED(res) {
                return Err(WinDbgError::FfiError("Unable to get stack frames", res));
            }

            frames.set_len(frames_filled as usize);
            frames
                .into_iter()
                .map(|frame| {
                    let params: Vec<Value> = frame.Params.iter().map(|p| Value::from(*p)).collect();

                    StackFrame::new(
                        frame.FrameNumber,
                        frame.InstructionOffset,
                        frame.ReturnOffset,
                        params,
                    )
                })
                .collect()
        };

        Ok(StackTrace::new(frames))
    }
}
