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

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
load_dotenv(os.path.join(BASE_DIR, ".env"))

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
ai_client = OpenAI(
    api_key=os.getenv("OPENAI_API_KEY"),
    timeout=30.0,  # 防止请求挂起
    max_retries=2
)

# We will start the MCP Server subprocess ONCE when the API starts
mcp_session = None

@asynccontextmanager
async def lifespan(app: FastAPI):
    # Startup: Connect to server.py
    print("Starting MCP Server connection...")
    if not os.getenv("OPENAI_API_KEY"):
        raise RuntimeError("OPENAI_API_KEY is missing. Set it in ai_service/.env")
    server_params = StdioServerParameters(
        command="python",
        args=[os.path.join(BASE_DIR, "server.py")], # Must be in same folder
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

    # 限制历史消息数量，防止内存溢出
    MAX_HISTORY = 20
    limited_history = request.history[-MAX_HISTORY:] if len(request.history) > MAX_HISTORY else request.history
    
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
    # Add history from Android (限制数量)
    # extend appends multiple objects into the list
    # eg. x = [1,2]; x.extend([3,4]) -> x = [1,2,3,4]
    messages.extend(limited_history)
    _print_messages("After history", messages)
    # Add current message
    # append adds a single object into the list
    # eg. x = [1,2]; x.append([3,4]) -> x = [1,2,[3,4]]
    messages.append({"role": "user", "content": request.message})
    _print_messages("After user message", messages)

    # Get available tools
    tools = await mcp_session.list_tools()
    
    # Call OpenAI
    try:
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
    except Exception as e:
        print(f"OpenAI step1 error: {type(e).__name__}: {e}")
        raise HTTPException(status_code=500, detail=f"OpenAI step1 failed: {type(e).__name__}")

    assistant_msg = response.choices[0].message
    print(f"\n--- OpenAI assistant_msg ---\nrole={assistant_msg.role} tool_calls={bool(assistant_msg.tool_calls)}")

    # Check for Tool Use
    if assistant_msg.tool_calls:
        tool_call = assistant_msg.tool_calls[0]
        tool_name = tool_call.function.name
        tool_args = json.loads(tool_call.function.arguments)

        # Execute Tool
        try:
            result = await mcp_session.call_tool(tool_name, tool_args)
        except Exception as e:
            print(f"Tool call error: {type(e).__name__}: {e}")
            raise HTTPException(status_code=500, detail=f"Tool call failed: {type(e).__name__}")

        # Feed back to AI
        messages.append(assistant_msg)
        messages.append({
            "role": "tool",
            "tool_call_id": tool_call.id,
            "content": str(result.content)
        })
        _print_messages("After tool result", messages)

        try:
            final_response = ai_client.chat.completions.create(
                model="gpt-4o",
                messages=messages
            )
        except Exception as e:
            print(f"OpenAI step2 error: {type(e).__name__}: {e}")
            raise HTTPException(status_code=500, detail=f"OpenAI step2 failed: {type(e).__name__}")
        return {"reply": final_response.choices[0].message.content}

    return {"reply": assistant_msg.content}

if __name__ == "__main__":
    # Run on all interfaces, use reverse proxy for security
    uvicorn.run(
        app, 
        host="0.0.0.0",  # 监听所有接口，由nginx做反向代理
        port=8000,
        workers=1,  # 单worker避免多进程内存问题
        timeout_keep_alive=30,
        limit_concurrency=50,  # 限制并发连接数
        limit_max_requests=1000  # 重启worker防止内存泄漏
    )
