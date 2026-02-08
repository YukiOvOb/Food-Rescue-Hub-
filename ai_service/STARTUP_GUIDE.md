# RescueBot AI Service Startup Guide

## Prerequisites

1. Python 3.8+ (verified: Python 3.12.3)
2. OpenAI API key
3. Virtual environment (created automatically by `start.sh`)

---

## Quick Start

### Option 1: Use the startup script (recommended)

```bash
cd /home/ubuntu/Food-Rescue-Hub-/ai_service

# Step 1: Configure your OpenAI API key
# Edit .env and replace with your real key
nano .env

# Step 2: Run the startup script
./start.sh
```

### Option 2: Start manually

```bash
cd /home/ubuntu/Food-Rescue-Hub-/ai_service

# Create virtual environment
python3 -m venv venv
source venv/bin/activate

# Install dependencies
pip install -r requirements.txt

# Preprocess data (first run only)
python3 ingest.py

# Start API service
python3 api.py
```

---

## Configure OpenAI API Key

### Get an API key

1. Go to https://platform.openai.com/api-keys
2. Sign in to your OpenAI account (or create one)
3. Click `Create new secret key`
4. Copy the generated key

### Add it to the project

Edit `/home/ubuntu/Food-Rescue-Hub-/ai_service/.env`:

```dotenv
# Replace with your real API key
OPENAI_API_KEY=sk-proj-your-real-key
```

---

## Service URLs

After startup, the AI service runs at:

| Purpose | URL | Notes |
|------|------|------|
| API endpoint | `http://0.0.0.0:8000` | Main service address |
| Chat endpoint | `http://localhost:8000/chat` | Local testing |
| API docs | `http://localhost:8000/docs` | Swagger UI |
| Android emulator | `http://10.0.2.2:8000` | Emulator -> host machine |
| Android device | `http://[YOUR_PC_IP]:8000` | Real device -> host machine |

---

## Test API Connectivity

### Test 1: Health check

```bash
# Using curl
curl -v http://localhost:8000/

# Expected response: 404 (normal, there is no root route)
```

### Test 2: Full chat flow

```bash
curl -X POST http://localhost:8000/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "What is a Surprise Bag?",
    "history": []
  }'
```

Expected response:
```json
{
  "reply": "A Surprise Bag is a collection of surplus food from a merchant..."
}
```

### Test 3: Python script

Create `test_rescuebot.py`:

```python
import requests
import json

BASE_URL = "http://localhost:8000"

def test_chat():
    payload = {
        "message": "Can I cancel my order?",
        "history": []
    }

    response = requests.post(
        f"{BASE_URL}/chat",
        json=payload,
        headers={"Content-Type": "application/json"}
    )

    print("Status:", response.status_code)
    print("Response:", json.dumps(response.json(), indent=2, ensure_ascii=False))

if __name__ == "__main__":
    test_chat()
```

Run test:
```bash
pip install requests
python3 test_rescuebot.py
```

---

## Service Components

### 1. `api.py` - FastAPI server
- Exposes `/chat` endpoint
- Manages OpenAI conversation flow
- Handles tool calls and response generation

### 2. `server.py` - MCP server
- Handles FAQ knowledge-base search
- Uses ChromaDB for vector storage
- Exposes `search_faq_knowledge_base` tool

### 3. `ingest.py` - Data ingestion
- Parses `data/faq.md`
- Creates embeddings
- Stores vectors in ChromaDB

### 4. `data/chroma_db/` - Vector database
- Stores vector representations of FAQs
- Supports similarity search
- Auto-created on first run

---

## Dependencies

| Package | Purpose |
|------|------|
| `mcp` | Model Context Protocol (tool calling) |
| `chromadb` | Vector database |
| `openai` | OpenAI API client |
| `fastapi` | Web framework |
| `uvicorn` | ASGI server |
| `python-dotenv` | Environment variable management |
| `httpx` | HTTP client |

---

## Troubleshooting

### Q1: "Incorrect API key" error

Solution:
```bash
# Check .env file
cat .env

# Ensure key format is correct
# Should be: sk-proj-xxxxx...
```

### Q2: "ChromaDB not found"

Solution:
```bash
# Run ingestion manually
python3 ingest.py

# Or let startup script handle it
./start.sh
```

### Q3: "MCP Server not ready"

Cause: `server.py` failed to start.

Solution:
```bash
# Verify OpenAI API key is correct
# Verify dependencies are fully installed
pip install -r requirements.txt --upgrade
```

### Q4: Android connection timeout

Cause: Network configuration issue.

Solution:
```bash
# Check if service is running
lsof -i :8000

# Check firewall/network rules
# Emulator: http://10.0.2.2:8000
# Real device: http://[PC_IP]:8000 (example: http://192.168.1.100:8000)
```

---

## Startup Logs

You should see output like:

```
============================================
RescueBot AI Service Startup
============================================

[1/5] Checking Python version...
+ Python 3.12.3 found

[2/5] Setting up virtual environment...
+ Virtual environment activated

[3/5] Installing dependencies...
+ Dependencies installed

[4/5] Checking environment configuration...
+ Environment configuration found

[5/5] Checking ChromaDB knowledge base...
+ ChromaDB knowledge base found

============================================
Starting RescueBot AI Service...
============================================

Service Info:
  API Endpoint: http://0.0.0.0:8000
  Chat Endpoint: http://0.0.0.0:8000/chat
  Docs: http://localhost:8000/docs
```

---

## Stop Service

```bash
# Press Ctrl+C in the running terminal
# Or run in another terminal:
pkill -f "python3 api.py"
```

---

## Startup Checklist

- [ ] Python 3.8+ installed
- [ ] OpenAI API key created
- [ ] `.env` configured with real API key
- [ ] `start.sh` has execute permission
- [ ] ChromaDB data generated
- [ ] Service running at `http://localhost:8000`
- [ ] API docs available at `http://localhost:8000/docs`
- [ ] Chat endpoint test successful

---

## Related Docs

- [OpenAI API Docs](https://platform.openai.com/docs/api-reference)
- [FastAPI Docs](https://fastapi.tiangolo.com/)
- [ChromaDB Docs](https://docs.trychroma.com/)
- [MCP Docs](https://modelcontextprotocol.io/)
