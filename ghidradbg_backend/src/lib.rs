use std::io;

use async_trait::async_trait;

pub use event::DebuggerEvent;

use crate::command::{DebuggerCommandRequest, DebuggerCommandResponse};

pub type Result<T> = std::result::Result<T, DebuggerError>;

pub mod command;
pub mod event;
pub mod state;

#[derive(thiserror::Error, Debug)]
pub enum DebuggerError {
    #[error("debuggee exited while waiting on response")]
    TargetExited,

    #[error(transparent)]
    Other(#[from] anyhow::Error),

    #[error(transparent)]
    Io(#[from] io::Error),

    #[error(transparent)]
    Serialization(#[from] serde_json::Error),
}

#[async_trait]
pub trait Debugger: Sized {
    async fn launch(cli: String) -> Result<Self>;

    async fn handle_command(&mut self, command: DebuggerCommandRequest) -> Result<DebuggerCommandResponse>;

    async fn next_event(&mut self) -> Result<DebuggerEvent>;
}

