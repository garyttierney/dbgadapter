use std::env;

use bytes::{BufMut, BytesMut};
use env_logger::Env;
use futures::{SinkExt, TryStreamExt};
use log::{error, info};
use serde::{Deserialize, Serialize};
use tokio::io::AsyncBufReadExt;
use tokio::prelude::{AsyncRead, AsyncWrite};
use tokio::stream::StreamExt;
use tokio_util::codec::{Encoder, FramedWrite};

use ghidradbg_backend::command::{DebuggerCommandRequest, DebuggerCommandResponse};
use ghidradbg_backend::{Debugger, DebuggerError, DebuggerEvent, Result};
use ghidradbg_backend_windbg::WinDbg;
use tokio::time::Duration;

pub struct WireResponseEncoder;

#[derive(Serialize, Deserialize)]
#[serde(tag = "type")]
#[serde(rename_all = "snake_case")]
pub enum WireResponse {
    Command { response: DebuggerCommandResponse },

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
            event = debugger.next_event() => match event {
                Ok(event) => output.send(WireResponse::Notification { event }).await?,
                Err(e) => {
                    return Err(e)
                }
            },
            input = input.next() => match input {
                Some(Ok(cmd)) =>  {
                    let response = debugger.handle_command(cmd).await?;

                    output.send(WireResponse::Command { response }).await?;
                },
                _ => break
            }
        }
    }

    Ok(())
}

fn main() -> Result<()> {
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

    let mut runtime = tokio::runtime::Builder::new()
        .basic_scheduler()
        .enable_all()
        .build()
        .unwrap();

    let result = runtime.block_on(async {
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

        info!("Using {}, args={}", debugger, debugger_args);

        debugger_task.await
    });

    if let Err(e) = result {
        error!("Debugger exited with error: {}", e);
    }

    runtime.shutdown_timeout(Duration::from_secs(5));

    Ok(())
}
