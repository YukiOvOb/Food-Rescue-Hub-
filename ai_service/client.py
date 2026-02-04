import asyncio
import os
import json
# We use OpenAI directly (Clean & Fast)
from openai import OpenAI 
from mcp import ClientSession, StdioServerParameters
from mcp.client.stdio import stdio_client
from dotenv import load_dotenv

load_dotenv()

# Setup OpenAI Client
ai_client = OpenAI(api_key=os.getenv("OPENAI_API_KEY"))

async def run_chat_loop():
    # 1. Configure the connection to your server.py
    # creates the MCP connection
    server_params = StdioServerParameters(
        command="python",
        args=["server.py"], # This runs the server script automatically
        env=os.environ.copy() # Pass API keys to the subprocess
    )

    async with stdio_client(server_params) as (read, write):
        async with ClientSession(read, write) as session:
            await session.initialize()
            
            # 2. Get the tools (Discovery)
            tools = await session.list_tools()
            print(f"Connected to MCP! Available Tools: {[t.name for t in tools.tools]}")
            for tool in tools.tools:
                print(f"\nTool: {tool.name}")
                print(f"Description: {tool.description}")
                print(f"Input Schema: {tool.inputSchema}")
            
            # 3. Simple Chat History with Enhanced System Prompt with Guardrails
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

            print("\n--- RescueBot is Ready! (Type 'quit' to exit) ---")

            while True:
                user_input = input("\nYou: ")
                if user_input.lower() in ["quit", "exit"]:
                    break
                
                messages.append({"role": "user", "content": user_input})

                # 4. The "Agent" Loop
                # We send the tools definition to GPT-4o
                # tool.name refers to the function name defined in server.py
                # tool.description is the docstring from the function
                # tool.inputSchema is the JSON Schema of the function inputs
                response = ai_client.chat.completions.create(
                    model="gpt-4o",
                    messages=messages,
                    tools=[{
                        "type": "function",
                        "function": {
                            "name": tool.name,
                            "description": tool.description,
                            "parameters": tool.inputSchema
                        }
                    } for tool in tools.tools]
                )
                print(f"OpenAI response choices: {len(response.choices)}")

                # 5. Check if AI wants to use a tool
                assistant_msg = response.choices[0].message
                
                # if AI responded with a tool request instead of a final answer
                print(f"Tool calls present? {bool(assistant_msg.tool_calls)}")
                if assistant_msg.tool_calls:
                    # AI decided to check the database!
                    tool_call = assistant_msg.tool_calls[0]
                    tool_name = tool_call.function.name
                    tool_args = json.loads(tool_call.function.arguments) # Safe JSON parse

                    # Execute the tool via MCP server
                    result = await session.call_tool(tool_name, tool_args)
                    
                    # Feed the result back to the AI
                    messages.append(assistant_msg) # Add the AI's "thought"
                    messages.append({
                        "role": "tool",
                        "tool_call_id": tool_call.id,
                        "content": str(result.content)
                    })
                    
                    # Get final answer
                    final_response = ai_client.chat.completions.create(
                        model="gpt-4o",
                        messages=messages
                    )
                    print(f"RescueBot: {final_response.choices[0].message.content}")
                    # this saves the conversation history
                    messages.append(final_response.choices[0].message)
                
                else:
                    # No tool needed (Just chatting)
                    print(f"RescueBot: {assistant_msg.content}")
                    messages.append(assistant_msg)

if __name__ == "__main__":
    asyncio.run(run_chat_loop())
