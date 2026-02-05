import os
import chromadb
from chromadb.utils import embedding_functions
from dotenv import load_dotenv

# 1. Load environment variables (API Keys)
load_dotenv()
OPENAI_API_KEY = os.getenv("OPENAI_API_KEY")

if not OPENAI_API_KEY:
    raise ValueError("Error: OPENAI_API_KEY not found in .env file")

# 2. Setup ChromaDB with OpenAI Embeddings
# This effectively replaces "langchain-openai" and "langchain-chroma"
openai_ef = embedding_functions.OpenAIEmbeddingFunction(
    api_key=OPENAI_API_KEY,
    model_name="text-embedding-3-small" # Cheap & Fast model
)

# PersistentClient saves data to your hard drive so you don't lose it on restart
client = chromadb.PersistentClient(path="./data/chroma_db")
collection = client.get_or_create_collection(
    name="faq_knowledge_base",
    embedding_function=openai_ef
)

def ingest_faqs():
    file_path = "./data/faq.md"
    
    if not os.path.exists(file_path):
        print(f"Error: File not found at {file_path}")
        return

    with open(file_path, "r", encoding="utf-8") as f:
        content = f.read()

    # 3. Custom Splitting Logic (The "Text Splitter")
    # We split by '###' because that is how you formatted your file.
    # this is chunking
    raw_sections = content.split("###")
    
    documents = []
    ids = []

    print(f"Found {len(raw_sections)} raw sections. Processing...")

    for i, section in enumerate(raw_sections):
        text = section.strip()
        if not text:
            continue # Skip empty sections
        
        # Add to lists
        documents.append(text)
        ids.append(f"faq_{i}")

    # 4. Save to Database
    if documents:
        collection.upsert(
            documents=documents,
            ids=ids
        )
        print(f"Successfully loaded {len(documents)} FAQs into ChromaDB!")
    else:
        print("No valid FAQs found to ingest.")

if __name__ == "__main__":
    ingest_faqs()