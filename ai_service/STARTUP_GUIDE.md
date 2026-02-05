# 🤖 RescueBot AI Service 启动指南

## 📋 前置要求

✅ Python 3.8+ (已验证: Python 3.12.3)
✅ OpenAI API Key
✅ 虚拟环境（自动创建）

---

## 🚀 快速启动

### 方式1: 使用启动脚本（推荐）

```bash
cd /home/ubuntu/Food-Rescue-Hub-/ai_service

# 第一步: 配置OpenAI API Key
# 编辑 .env 文件，替换你的实际API Key
nano .env

# 第二步: 运行启动脚本
./start.sh
```

### 方式2: 手动启动

```bash
cd /home/ubuntu/Food-Rescue-Hub-/ai_service

# 创建虚拟环境
python3 -m venv venv
source venv/bin/activate

# 安装依赖
pip install -r requirements.txt

# 数据预处理（首次运行）
python3 ingest.py

# 启动API服务
python3 api.py
```

---

## 🔑 配置OpenAI API Key

### 获取API Key

1. 访问 https://platform.openai.com/api-keys
2. 登录OpenAI账户（或创建新账户）
3. 点击 "Create new secret key"
4. 复制生成的API Key

### 配置到项目

编辑 `/home/ubuntu/Food-Rescue-Hub-/ai_service/.env`：

```dotenv
# 替换为你的实际API Key
OPENAI_API_KEY=sk-proj-你的真实密钥
```

---

## 📱 服务地址

启动后，AI服务将在以下地址运行：

| 用途 | 地址 | 说明 |
|------|------|------|
| **API端点** | `http://0.0.0.0:8000` | 服务主地址 |
| **聊天接口** | `http://localhost:8000/chat` | 本地测试 |
| **API文档** | `http://localhost:8000/docs` | Swagger文档 |
| **Android模拟器** | `http://10.0.2.2:8000` | 模拟器访问PC |
| **Android真机** | `http://[你的PC IP]:8000` | 真机访问 |

---

## 🧪 测试API连接

### 测试1: 健康检查

```bash
# 使用curl
curl -v http://localhost:8000/

# 预期响应: 404 (正常，因为没有根路由)
```

### 测试2: 完整聊天流程

```bash
curl -X POST http://localhost:8000/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "What is a Surprise Bag?",
    "history": []
  }'
```

**预期响应:**
```json
{
  "reply": "A Surprise Bag is a collection of surplus food from a merchant..."
}
```

### 测试3: 使用Python脚本

创建 `test_rescuebot.py`：

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

运行测试：
```bash
pip install requests
python3 test_rescuebot.py
```

---

## 📊 服务组件说明

### 1. **api.py** - FastAPI服务器
- 提供 `/chat` 端点
- 管理OpenAI对话流程
- 负责工具调用和响应生成

### 2. **server.py** - MCP服务器
- 管理FAQ知识库查询
- 使用ChromaDB存储向量数据
- 提供 `search_faq_knowledge_base` 工具

### 3. **ingest.py** - 数据处理
- 解析 `data/faq.md` 文件
- 生成向量嵌入
- 存储到ChromaDB

### 4. **data/chroma_db/** - 向量数据库
- 存储FAQ的向量表示
- 支持相似度搜索
- 首次运行自动创建

---

## ⚙️ 依赖包

| 包名 | 用途 |
|------|------|
| `mcp` | Model Context Protocol (工具调用) |
| `chromadb` | 向量数据库 |
| `openai` | OpenAI API客户端 |
| `fastapi` | Web框架 |
| `uvicorn` | ASGI服务器 |
| `python-dotenv` | 环境变量管理 |
| `httpx` | HTTP客户端 |

---

## 🔍 常见问题

### Q1: "Incorrect API key" 错误

**解决方案:**
```bash
# 检查.env文件
cat .env

# 确保API Key格式正确
# 应该是: sk-proj-xxxxx...
```

### Q2: "ChromaDB not found"

**解决方案:**
```bash
# 手动运行数据摄取
python3 ingest.py

# 或使用启动脚本自动处理
./start.sh
```

### Q3: "MCP Server not ready"

**原因:** server.py启动失败
**解决方案:**
```bash
# 检查OpenAI API Key是否正确
# 检查依赖是否完整安装
pip install -r requirements.txt --upgrade
```

### Q4: Android连接超时

**原因:** 网络配置错误
**解决方案:**
```bash
# 检查服务是否运行
lsof -i :8000

# 检查防火墙
# 模拟器使用: http://10.0.2.2:8000
# 真机使用: http://[PC_IP]:8000 (如: http://192.168.1.100:8000)
```

---

## 📝 日志输出说明

启动时会看到：

```
============================================
RescueBot AI Service Startup
============================================

[1/5] Checking Python version...
✓ Python 3.12.3 found

[2/5] Setting up virtual environment...
✓ Virtual environment activated

[3/5] Installing dependencies...
✓ Dependencies installed

[4/5] Checking environment configuration...
✓ Environment configuration found

[5/5] Checking ChromaDB knowledge base...
✓ ChromaDB knowledge base found

============================================
Starting RescueBot AI Service...
============================================

Service Info:
  API Endpoint: http://0.0.0.0:8000
  Chat Endpoint: http://0.0.0.0:8000/chat
  Docs: http://localhost:8000/docs
```

---

## 🛑 停止服务

```bash
# 在运行的终端中按 Ctrl+C
# 或在另一个终端执行:
pkill -f "python3 api.py"
```

---

## ✅ 启动检查清单

- [ ] Python 3.8+ 已安装
- [ ] OpenAI API Key 已获取
- [ ] `.env` 文件已配置真实API Key
- [ ] `start.sh` 脚本有执行权限
- [ ] ChromaDB数据已生成
- [ ] 服务在 `http://localhost:8000` 运行
- [ ] API文档可访问 `http://localhost:8000/docs`
- [ ] 聊天接口测试成功

---

## 🔗 相关文档

- [OpenAI API文档](https://platform.openai.com/docs/api-reference)
- [FastAPI文档](https://fastapi.tiangolo.com/)
- [ChromaDB文档](https://docs.trychroma.com/)
- [MCP文档](https://modelcontextprotocol.io/)
