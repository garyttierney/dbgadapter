use std::env;

use bytes::{BufMut, BytesMut};
use env_logger::Env;
use futures::{SinkExt, TryStreamExt};
use log::{info, error};
use serde::{Deserialize, Serialize};
use tokio::io::AsyncBufReadExt;
use tokio::prelude::{AsyncRead, AsyncWrite};
use tokio::stream::StreamExt;
use tokio_util::codec::{Encoder, FramedWrite};

use ghidradbg_backend::{Debugger, DebuggerError, DebuggerEvent, Result};
use ghidradbg_backend::command::{
    DebuggerCommandRequest, DebuggerCommandResponse,
};
use ghidradbg_backend_windbg::WinDbg;

pub struct WireResponseEncoder;

#[derive(Serialize, Deserialize)]
#[serde(tag = "type")]
#[serde(rename_all = "snake_case")]
pub enum WireResponse {
    Command(DebuggerCommandResponse),

    Notification { event: DebuggerEvent },
}

impl Encoder<WireResponse> for WireResponseEncoder {
    type Error = DebuggerError;

    fn encode(&mut self, item: WireResponse, dst: &mut BytesMut) -> Result<()> {
        let mut msg = serde_json::to_string(&item)?;
        msg.push('\n');

        dst.put_slice(msg.as_bytes());
        Ok(())
    }
}

async fn debug_loop<Dbg, In, Out>(
    command_line: &str,
    input_reader: In,
    output_writer: Out,
) -> Result<()>
    where
        Dbg: Debugger,
        In: AsyncRead + Unpin,
        Out: AsyncWrite + Unpin,
{
    let mut debugger = Dbg::launch(command_line.to_string()).await?;
    let mut input = tokio::io::BufReader::new(input_reader)
        .lines()
        .and_then(|line| {
            futures::future::ready(Ok(
                serde_json::from_str::<DebuggerCommandRequest>(&line).unwrap()
            ))
        });

    let mut output = FramedWrite::new(output_writer, WireResponseEncoder);

    loop {
        tokio::select! {
            event = debugger.next_event() => {
                output.send(WireResponse::Notification{event: event?}).await?;
            }
            input = input.next() => match input {
                Some(Ok(cmd)) =>  {
                    let response = debugger.handle_command(cmd).await?;

                    output.send(WireResponse::Command(response)).await?;
                },
                _ => break
            }
        }
    }

    Ok(())
}

#[tokio::main]
async fn main() -> Result<()> {
    env_logger::Builder::from_env(Env::default().default_filter_or("info")).init();

    let mut args = env::args();
    let (debugger, debugger_args) = match (args.nth(1), args.next()) {
        (Some(debugger), Some(args)) => (debugger, args),
        _ => {
            error!("Usage: ghidra-dbg <backend> <backend-args>");
            error!("       ghidra-dbg dbgeng tcp:port=3333");
            panic!()
        }
    };


    let input = tokio::io::stdin();
    let output = tokio::io::stdout();

    let debugger_task = match debugger.to_lowercase().as_str() {
        #[cfg(feature = "windbg")]
        "windbg" | "win_dbg" => debug_loop::<WinDbg, _, _>(&debugger_args, input, output),
        _ => {
            error!("{} is not a supported debugger backend", debugger);
            panic!()
        }
    };

    info!("Connecting to {} on {}", debugger, debugger_args);

    Ok(debugger_task.await?)
}
