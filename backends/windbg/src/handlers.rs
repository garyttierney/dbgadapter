use ghidradbg_backend::Result;
use ghidradbg_backend::command::SingleStep;

use crate::dbgeng::DebugClient;

pub fn single_step(_client: &mut DebugClient, _cmd: SingleStep) -> Result<()> {
    Ok(())
}