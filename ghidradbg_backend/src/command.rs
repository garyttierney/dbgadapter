use serde::{Deserialize, Serialize};
use serde::de::DeserializeOwned;
use serde_json::{from_value, to_value, Value};


use crate::Result;

pub trait DebuggerCommand: Sized {
    const TYPE: &'static str;

    type Response;
}

#[derive(Serialize, Deserialize)]
pub struct DebuggerCommandRequest {
    id: u64,
    ty: String,
    command: Value,
}

impl DebuggerCommandRequest {
    pub fn ty(&self) -> &str {
        &self.ty
    }
}

#[derive(Serialize, Deserialize)]
#[serde(rename_all = "snake_case")]
pub struct DebuggerCommandResponse {
    request_id: u64,
    request_ty: String,
    response: Value,
}

pub struct DebuggerCommandDispatcher<'a, Cx> {
    pub context: &'a mut Cx,
    pub request: DebuggerCommandRequest,
    pub result: Option<DebuggerCommandResponse>,
}

impl<'a, Cx> DebuggerCommandDispatcher<'a, Cx> {
    pub fn on<R>(&mut self, handler: fn(&mut Cx, R) -> Result<R::Response>) -> Result<&mut Self>
        where
            R: DebuggerCommand + DeserializeOwned + 'static,
            R::Response: Serialize + 'static,
    {
        if self.result.is_none() && self.request.ty == R::TYPE {
            let cmd = from_value(self.request.command.clone()).unwrap();
            let result = handler(self.context, cmd)?;

            self.result = Some(DebuggerCommandResponse {
                request_id: self.request.id,
                request_ty: self.request.ty.clone(),
                response: to_value(result).unwrap(),
            })
        }

        Ok(self)
    }
}

pub fn dispatch_with<Cx>(context: &mut Cx, request: DebuggerCommandRequest, handler_chain: fn(&mut DebuggerCommandDispatcher<Cx>) -> Result<()>) -> Result<Option<DebuggerCommandResponse>> {
    let mut dispatcher = DebuggerCommandDispatcher {
        context,
        request,
        result: None
    };

    handler_chain(&mut dispatcher)?;

    Ok(dispatcher.result)
}

#[derive(Serialize, Deserialize)]
pub struct SingleStep {}

impl DebuggerCommand for SingleStep {
    const TYPE: &'static str = "single_step";

    type Response = ();
}
