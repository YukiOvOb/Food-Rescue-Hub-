import os
import chromadb
from chromadb.utils import embedding_functions
from mcp.server.fastmcp import FastMCP
from dotenv import load_dotenv

# 1. Load Secrets
load_dotenv()
OPENAI_API_KEY = os.getenv("OPENAI_API_KEY")

# 2. Setup the MCP Server
# This name "FoodRescue" is what the AI sees as the "System Name"
mcp = FastMCP("FoodRescue")

# 3. Connect to the ChromaDB
# this is creating the embedding function again similar to ingest.py
openai_ef = embedding_functions.OpenAIEmbeddingFunction(
    api_key=OPENAI_API_KEY,
    model_name="text-embedding-3-small"
)
# Connect to the existing ChromaDB collection
client = chromadb.PersistentClient(path="./data/chroma_db")
collection = client.get_collection(
    name="faq_knowledge_base",
    embedding_function=openai_ef
)

# 4. Define the Tool
@mcp.tool()
def search_faq_knowledge_base(query: str) -> str:
    """
    Search the FAQ database for policies, safety guidelines, and general info.
    Use this tool whenever the user asks about rules, refunds, or how the app works.
    """
    print(f"--- Tool Triggered: Searching FAQs for '{query}' ---")
    
    # Query ChromaDB (Get top 3 matches)
    results = collection.query(
        query_texts=[query],
        n_results=3
    )
    
    # Format the results into a string the AI can read
    documents = results["documents"][0]
    if not documents:
        return "No relevant FAQ information found."
        
    return "\n\n".join(documents)

if __name__ == "__main__":
    mcp.run()