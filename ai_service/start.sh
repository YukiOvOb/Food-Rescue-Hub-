#!/bin/bash

# ============================================
# RescueBot AI Service Startup Script
# ============================================

set -e  # Exit on error

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Get the directory where this script is located
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

echo -e "${BLUE}============================================${NC}"
echo -e "${BLUE}RescueBot AI Service Startup${NC}"
echo -e "${BLUE}============================================${NC}"
echo ""

# 1. Check Python version
echo -e "${YELLOW}[1/5]${NC} Checking Python version..."
if ! command -v python3 &> /dev/null; then
    echo -e "${RED}✗ Python3 not found. Please install Python 3.8+${NC}"
    exit 1
fi
PYTHON_VERSION=$(python3 --version 2>&1 | awk '{print $2}')
echo -e "${GREEN}✓ Python ${PYTHON_VERSION} found${NC}"
echo ""

# 2. Check if virtual environment exists, if not create it
echo -e "${YELLOW}[2/5]${NC} Setting up virtual environment..."
if [ ! -d "$SCRIPT_DIR/venv" ]; then
    echo -e "${YELLOW}Creating virtual environment...${NC}"
    python3 -m venv "$SCRIPT_DIR/venv"
fi
source "$SCRIPT_DIR/venv/bin/activate"
echo -e "${GREEN}✓ Virtual environment activated${NC}"
echo ""

# 3. Install dependencies
echo -e "${YELLOW}[3/5]${NC} Installing dependencies..."
if [ -f "$SCRIPT_DIR/requirements.txt" ]; then
    pip install --upgrade pip > /dev/null 2>&1
    pip install -r "$SCRIPT_DIR/requirements.txt" > /dev/null 2>&1
    echo -e "${GREEN}✓ Dependencies installed${NC}"
else
    echo -e "${RED}✗ requirements.txt not found${NC}"
    exit 1
fi
echo ""

# 4. Check for .env file
echo -e "${YELLOW}[4/5]${NC} Checking environment configuration..."
if [ ! -f "$SCRIPT_DIR/.env" ]; then
    echo -e "${YELLOW}⚠ .env file not found. Creating template...${NC}"
    cat > "$SCRIPT_DIR/.env" << 'EOF'
# RescueBot AI Service Configuration
# Replace with your actual OpenAI API key
OPENAI_API_KEY=your-api-key-here
EOF
    echo -e "${YELLOW}✗ Please configure .env file with your OpenAI API key${NC}"
    echo -e "${YELLOW}   Location: $SCRIPT_DIR/.env${NC}"
    exit 1
else
    # Check if OPENAI_API_KEY is set
    if grep -q "OPENAI_API_KEY=your-api-key-here" "$SCRIPT_DIR/.env"; then
        echo -e "${RED}✗ Please set OPENAI_API_KEY in .env file${NC}"
        exit 1
    fi
    echo -e "${GREEN}✓ Environment configuration found${NC}"
fi
echo ""

# 5. Check if ChromaDB data exists
echo -e "${YELLOW}[5/5]${NC} Checking ChromaDB knowledge base..."
if [ ! -d "$SCRIPT_DIR/data/chroma_db" ]; then
    echo -e "${YELLOW}⚠ ChromaDB not found. Ingesting FAQ data...${NC}"
    python3 "$SCRIPT_DIR/ingest.py"
    echo -e "${GREEN}✓ FAQ data ingested${NC}"
else
    echo -e "${GREEN}✓ ChromaDB knowledge base found${NC}"
fi
echo ""

# 6. Start the API service
echo -e "${BLUE}============================================${NC}"
echo -e "${GREEN}Starting RescueBot AI Service...${NC}"
echo -e "${BLUE}============================================${NC}"
echo ""
echo -e "${YELLOW}Service Info:${NC}"
echo -e "  API Endpoint: ${GREEN}http://0.0.0.0:8000${NC}"
echo -e "  Chat Endpoint: ${GREEN}http://0.0.0.0:8000/chat${NC}"
echo -e "  Docs: ${GREEN}http://localhost:8000/docs${NC}"
echo ""
echo -e "${YELLOW}For Android Emulator, use:${NC}"
echo -e "  ${GREEN}http://10.0.2.2:8000${NC}"
echo ""
echo -e "${YELLOW}Press Ctrl+C to stop the service${NC}"
echo ""

# Run the API
python3 "$SCRIPT_DIR/api.py"
