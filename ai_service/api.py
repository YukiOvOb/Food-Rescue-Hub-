import os
import asyncio
import json
import uvicorn
from contextlib import asynccontextmanager
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from mcp import ClientSession, StdioServerParameters
from mcp.client.stdio import stdio_client
from openai import OpenAI
from dotenv import load_dotenv

load_dotenv()

# Debug helper for message flow
def _print_messages(label: str, messages: list[dict]) -> None:
    print(f"\n--- {label} (count={len(messages)}) ---")
    for i, msg in enumerate(messages):
        if hasattr(msg, "role"):
            role = getattr(msg, "role", "unknown")
            content = getattr(msg, "content", "")
        else:
            role = msg.get("role", "unknown")
            content = msg.get("content", "")
        if content is None:
            content = ""
        preview = str(content).replace("\n", " ")[:120]
        print(f"{i:02d} role={role} content='{preview}'")

# 1. Setup Models (The JSON Android sends)
class ChatRequest(BaseModel):
    message: str
    history: list[dict] = [] # List of {"role": "user", "content": "..."}

class ChatResponse(BaseModel):
    reply: str

# 2. Global Services
ai_client = OpenAI(api_key=os.getenv("OPENAI_API_KEY"))

# We will start the MCP Server subprocess ONCE when the API starts
mcp_session = None

@asynccontextmanager
async def lifespan(app: FastAPI):
    # Startup: Connect to server.py
    print("Starting MCP Server connection...")
    server_params = StdioServerParameters(
        command="python",
        args=["server.py"], # Must be in same folder
        env=os.environ.copy()
    )
    
    async with stdio_client(server_params) as (read, write):
        async with ClientSession(read, write) as session:
            await session.initialize()
            global mcp_session
            mcp_session = session
            yield # API is running now
            
    print("Shutting down MCP connection...")

app = FastAPI(lifespan=lifespan)

# 3. The Endpoint Android calls (POST http://localhost:8000/chat)
@app.post("/chat", response_model=ChatResponse)
async def chat_endpoint(request: ChatRequest):
    if not mcp_session:
        raise HTTPException(status_code=503, detail="MCP Server not ready")

    # Prepare messages
    messages = [
                {
                    "role": "system",
                    "content": (
                        "You are RescueBot, the dedicated AI assistant for a Food Rescue App. "
                        "Follow these rules strictly:\n"
                        "1. IDENTITY: If asked who you are or if you are a bot, reply: "
                        "'I am RescueBot, here to help you rescue surplus food!'\n"
                        "2. SCOPE: You ONLY answer questions about food rescue, app policies, pickups, and refunds. "
                        "Do NOT answer general questions like math, history, or coding.\n"
                        "3. DEFAULT REPLY: If a user asks a question unrelated to food rescue, reply: "
                        "'I am sorry, I can only assist with questions related to the Food Rescue App.'\n"
                        "4. KNOWLEDGE: Always use the 'search_faq_knowledge_base' tool to find answers. "
                        "If the tool returns no results, admit you do not know."
                    )
                }
            ]
    _print_messages("After system prompt", messages)
    # Add history from Android
    # extend appends multiple objects into the list
    # eg. x = [1,2]; x.extend([3,4]) -> x = [1,2,3,4]
    messages.extend(request.history)
    _print_messages("After history", messages)
    # Add current message
    # append adds a single object into the list
    # eg. x = [1,2]; x.append([3,4]) -> x = [1,2,[3,4]]
    messages.append({"role": "user", "content": request.message})
    _print_messages("After user message", messages)

    # Get available tools
    tools = await mcp_session.list_tools()
    
    # Call OpenAI
    response = ai_client.chat.completions.create(
        model="gpt-4o",
        messages=messages,
        tools=[{
            "type": "function",
            "function": {
                "name": t.name,
                "description": t.description,
                "parameters": t.inputSchema
            }
        } for t in tools.tools]
    )

    assistant_msg = response.choices[0].message
    print(f"\n--- OpenAI assistant_msg ---\nrole={assistant_msg.role} tool_calls={bool(assistant_msg.tool_calls)}")

    # Check for Tool Use
    if assistant_msg.tool_calls:
        tool_call = assistant_msg.tool_calls[0]
        tool_name = tool_call.function.name
        tool_args = json.loads(tool_call.function.arguments)

        # Execute Tool
        result = await mcp_session.call_tool(tool_name, tool_args)

        # Feed back to AI
        messages.append(assistant_msg)
        messages.append({
            "role": "tool",
            "tool_call_id": tool_call.id,
            "content": str(result.content)
        })
        _print_messages("After tool result", messages)

        final_response = ai_client.chat.completions.create(
            model="gpt-4o",
            messages=messages
        )
        return {"reply": final_response.choices[0].message.content}

    return {"reply": assistant_msg.content}

if __name__ == "__main__":
    # Run on 0.0.0.0 so Android Emulator can see it
    uvicorn.run(app, host="0.0.0.0", port=8000)
