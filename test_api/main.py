from fastapi import FastAPI, Response
from pydantic import BaseModel

app = FastAPI()

class SyncInput(BaseModel):
    message_text: str


@app.post("/sync_message_text")
async def sync_message_text(input: SyncInput):
    print(input)
    return input

@app.get("/index.html")
async def index():
    return Response()