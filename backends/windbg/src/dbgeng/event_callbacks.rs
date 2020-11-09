use bitflags::bitflags;
use tokio::sync::mpsc::Sender;
use winapi::shared::basetsd::*;
use winapi::shared::guiddef::REFIID;
use winapi::shared::ntdef::*;
use winapi::shared::winerror::{HRESULT, S_OK};
use winapi::um::dbgeng::*;
use winapi::um::unknwnbase::{IUnknown, IUnknownVtbl};
use winapi::um::winnt::PEXCEPTION_RECORD64;

#[derive(Debug)]
pub enum WinDbgEvent {
    DebugeeStateChange(DebugeeStateChange),
}

bitflags! {
    pub struct DebugeeStateChange: u32 {
        const REGISTERS = DEBUG_CDS_REGISTERS;
        const REFRESH = DEBUG_CDS_REFRESH;
        const DATA = DEBUG_CDS_DATA;
        const ALL = DEBUG_CDS_ALL;
    }
}

impl DebugeeStateChange {
    pub fn is_complete_change(&self) -> bool {
        self.contains(DebugeeStateChange::ALL)
    }
}

bitflags! {
    pub struct DebugEventInterestFlags: u32 {
        const BREAKPOINT = DEBUG_EVENT_BREAKPOINT;
        const EXCEPTION = DEBUG_EVENT_EXCEPTION;
        const CREATE_THREAD = DEBUG_EVENT_CREATE_THREAD;
        const EXIT_THREAD = DEBUG_EVENT_EXIT_THREAD;
        const CREATE_PROCESS = DEBUG_EVENT_CREATE_PROCESS;
        const EXIT_PROCESS = DEBUG_EVENT_EXIT_PROCESS;
        const LOAD_MODULE = DEBUG_EVENT_LOAD_MODULE;
        const UNLOAD_MODULE = DEBUG_EVENT_UNLOAD_MODULE;
        const SYSTEM_ERROR = DEBUG_EVENT_SYSTEM_ERROR;
        const SESSION_STATUS = DEBUG_EVENT_SESSION_STATUS;
        const CHANGE_DEBUGGEE_STATE = DEBUG_EVENT_CHANGE_DEBUGGEE_STATE;
        const CHANGE_ENGINE_STATE = DEBUG_EVENT_CHANGE_ENGINE_STATE;
        const CHANGE_SYMBOL_STATE = DEBUG_EVENT_CHANGE_SYMBOL_STATE;
    }
}

pub struct DebugEventCallbacks {
    _vtable: Box<IDebugEventCallbacksVtbl>,
    pub(crate) interest_mask: u32,
    pub(crate) tx: Sender<WinDbgEvent>,
}

impl DebugEventCallbacks {
    pub fn new(interest_mask: u32, tx: Sender<WinDbgEvent>) -> Self {
        let vtable = Box::new(IDebugEventCallbacksVtbl {
            parent: IUnknownVtbl {
                QueryInterface: DebugEventCallbacks::QueryInterface,
                AddRef: DebugEventCallbacks::AddRef,
                Release: DebugEventCallbacks::Release,
            },
            GetInterestMask: DebugEventCallbacks::GetInterestMask,
            Breakpoint: DebugEventCallbacks::Breakpoint,
            Exception: DebugEventCallbacks::Exception,
            CreateThread: DebugEventCallbacks::CreateThread,
            ExitThread: DebugEventCallbacks::ExitThread,
            CreateProcess: DebugEventCallbacks::CreateProcess,
            ExitProcess: DebugEventCallbacks::ExitProcess,
            LoadModule: DebugEventCallbacks::LoadModule,
            UnloadModule: DebugEventCallbacks::UnloadModule,
            SystemError: DebugEventCallbacks::SystemError,
            SessionStatus: DebugEventCallbacks::SessionStatus,
            ChangeDebuggeeState: DebugEventCallbacks::ChangeDebuggeeState,
            ChangeEngineState: DebugEventCallbacks::ChangeEngineState,
            ChangeSymbolState: DebugEventCallbacks::ChangeSymbolState,
        });

        Self {
            _vtable: vtable,
            interest_mask,
            tx,
        }
    }
}

#[allow(non_snake_case)]
impl DebugEventCallbacks {
    unsafe extern "system" fn QueryInterface(
        _this: *mut IUnknown,
        _riid: REFIID,
        _ppvObject: *mut *mut winapi::ctypes::c_void,
    ) -> HRESULT {
        0
    }

    unsafe extern "system" fn AddRef(_this: *mut IUnknown) -> ULONG {
        0
    }
    unsafe extern "system" fn Release(_this: *mut IUnknown) -> ULONG {
        0
    }

    unsafe extern "system" fn GetInterestMask(
        this: *mut IDebugEventCallbacks,
        Mask: PULONG,
    ) -> HRESULT {
        let this: &mut DebugEventCallbacks = &mut *(this as *mut _);

        *Mask = this.interest_mask;
        S_OK
    }

    unsafe extern "system" fn Breakpoint(
        _this: *mut IDebugEventCallbacks,
        _Bp: PDEBUG_BREAKPOINT,
    ) -> HRESULT {
        0
    }

    unsafe extern "system" fn Exception(
        _this: *mut IDebugEventCallbacks,
        _Exception: PEXCEPTION_RECORD64,
        _FirstChance: ULONG,
    ) -> HRESULT {
        0
    }

    unsafe extern "system" fn CreateThread(
        _this: *mut IDebugEventCallbacks,
        _Handle: ULONG64,
        _DataOffset: ULONG64,
        _StartOffset: ULONG64,
    ) -> HRESULT {
        0
    }

    unsafe extern "system" fn ExitThread(
        _this: *mut IDebugEventCallbacks,
        _ExitCode: ULONG,
    ) -> HRESULT {
        0
    }

    unsafe extern "system" fn CreateProcess(
        _this: *mut IDebugEventCallbacks,
        _ImageFileHandle: ULONG64,
        _Handle: ULONG64,
        _BaseOffset: ULONG64,
        _ModuleSize: ULONG,
        _ModuleName: PCSTR,
        _ImageName: PCSTR,
        _CheckSum: ULONG,
        _TimeDateStamp: ULONG,
        _InitialThreadHandle: ULONG64,
        _ThreadDataOffset: ULONG64,
        _StartOffset: ULONG64,
    ) -> HRESULT {
        0
    }

    unsafe extern "system" fn ExitProcess(
        _this: *mut IDebugEventCallbacks,
        _ExitCode: ULONG,
    ) -> HRESULT {
        0
    }

    unsafe extern "system" fn LoadModule(
        _this: *mut IDebugEventCallbacks,
        _ImageFileHandle: ULONG64,
        _BaseOffset: ULONG64,
        _ModuleSize: ULONG,
        _ModuleName: PCSTR,
        _ImageName: PCSTR,
        _CheckSum: ULONG,
        _TimeDateStamp: ULONG,
    ) -> HRESULT {
        0
    }

    unsafe extern "system" fn UnloadModule(
        _this: *mut IDebugEventCallbacks,
        _ImageBaseName: PCSTR,
        _BaseOffset: ULONG64,
    ) -> HRESULT {
        0
    }

    unsafe extern "system" fn SystemError(
        _this: *mut IDebugEventCallbacks,
        _Error: ULONG,
        _Level: ULONG,
    ) -> HRESULT {
        0
    }

    unsafe extern "system" fn SessionStatus(
        _this: *mut IDebugEventCallbacks,
        _Status: ULONG,
    ) -> HRESULT {
        0
    }

    unsafe extern "system" fn ChangeDebuggeeState(
        this: *mut IDebugEventCallbacks,
        Flags: u32,
        _Argument: ULONG64,
    ) -> HRESULT {
        let this: &mut DebugEventCallbacks = &mut *(this as *mut _);
        let flags = DebugeeStateChange::from_bits(Flags).unwrap();
        let _ = this.tx.try_send(WinDbgEvent::DebugeeStateChange(flags));

        S_OK
    }

    unsafe extern "system" fn ChangeEngineState(
        _this: *mut IDebugEventCallbacks,
        _Flags: ULONG,
        _Argument: ULONG64,
    ) -> HRESULT {
        0
    }

    unsafe extern "system" fn ChangeSymbolState(
        _this: *mut IDebugEventCallbacks,
        _Flags: ULONG,
        _Argument: ULONG64,
    ) -> HRESULT {
        0
    }
}
