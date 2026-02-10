import importlib.util
import json
import runpy
import sys
from pathlib import Path
from types import ModuleType, SimpleNamespace
from unittest.mock import mock_open

import pytest


AI_SERVICE_DIR = Path(__file__).resolve().parents[1]


def _load_module(module_name: str, module_path: Path):
    spec = importlib.util.spec_from_file_location(module_name, str(module_path))
    module = importlib.util.module_from_spec(spec)
    sys.modules[module_name] = module
    assert spec.loader is not None
    spec.loader.exec_module(module)
    return module


def _stub_common_ai_modules(monkeypatch, state):
    dotenv_mod = ModuleType("dotenv")
    dotenv_mod.load_dotenv = lambda: None
    monkeypatch.setitem(sys.modules, "dotenv", dotenv_mod)

    uvicorn_mod = ModuleType("uvicorn")
    uvicorn_mod.run = lambda *args, **kwargs: state.setdefault("uvicorn_calls", []).append((args, kwargs))
    monkeypatch.setitem(sys.modules, "uvicorn", uvicorn_mod)

    fastapi_mod = ModuleType("fastapi")

    class HTTPException(Exception):
        def __init__(self, status_code: int, detail: str):
            super().__init__(detail)
            self.status_code = status_code
            self.detail = detail

    class FastAPI:
        def __init__(self, lifespan=None):
            self.lifespan = lifespan
            self.routes = {}

        def post(self, path, response_model=None):
            def _decorator(func):
                self.routes[path] = {"func": func, "response_model": response_model}
                return func

            return _decorator

    fastapi_mod.FastAPI = FastAPI
    fastapi_mod.HTTPException = HTTPException
    monkeypatch.setitem(sys.modules, "fastapi", fastapi_mod)

    pydantic_mod = ModuleType("pydantic")

    class BaseModel:
        def __init__(self, **kwargs):
            for key, value in self.__class__.__dict__.items():
                if key.startswith("_"):
                    continue
                if callable(value):
                    continue
                setattr(self, key, value)
            for key, value in kwargs.items():
                setattr(self, key, value)

    pydantic_mod.BaseModel = BaseModel
    monkeypatch.setitem(sys.modules, "pydantic", pydantic_mod)

    openai_mod = ModuleType("openai")

    class OpenAI:
        def __init__(self, **kwargs):
            state["openai_init"] = kwargs
            self.chat = SimpleNamespace(
                completions=SimpleNamespace(create=lambda **inner_kwargs: state.setdefault("default_openai_calls", []).append(inner_kwargs))
            )

    openai_mod.OpenAI = OpenAI
    monkeypatch.setitem(sys.modules, "openai", openai_mod)

    mcp_mod = ModuleType("mcp")
    mcp_client_mod = ModuleType("mcp.client")
    mcp_client_stdio_mod = ModuleType("mcp.client.stdio")

    class StdioServerParameters:
        def __init__(self, **kwargs):
            self.kwargs = kwargs
            state.setdefault("server_params", []).append(kwargs)

    class ClientSession:
        def __init__(self, read, write):
            self.read = read
            self.write = write
            self.initialized = False
            self.tool_calls = []
            state["client_session"] = self

        async def __aenter__(self):
            return self

        async def __aexit__(self, exc_type, exc, tb):
            return False

        async def initialize(self):
            self.initialized = True

        async def list_tools(self):
            return SimpleNamespace(tools=state.get("tools", []))

        async def call_tool(self, name, args):
            self.tool_calls.append((name, args))
            return SimpleNamespace(content=state.get("tool_content", "tool-content"))

    class _StdioClientContext:
        async def __aenter__(self):
            return ("reader", "writer")

        async def __aexit__(self, exc_type, exc, tb):
            return False

    def stdio_client(_params):
        return _StdioClientContext()

    mcp_mod.StdioServerParameters = StdioServerParameters
    mcp_mod.ClientSession = ClientSession
    mcp_client_stdio_mod.stdio_client = stdio_client

    monkeypatch.setitem(sys.modules, "mcp", mcp_mod)
    monkeypatch.setitem(sys.modules, "mcp.client", mcp_client_mod)
    monkeypatch.setitem(sys.modules, "mcp.client.stdio", mcp_client_stdio_mod)


def _stub_ingest_server_modules(monkeypatch, state):
    dotenv_mod = ModuleType("dotenv")
    dotenv_mod.load_dotenv = lambda: None
    monkeypatch.setitem(sys.modules, "dotenv", dotenv_mod)

    chromadb_mod = ModuleType("chromadb")
    chromadb_utils_mod = ModuleType("chromadb.utils")
    embedding_functions_mod = ModuleType("chromadb.utils.embedding_functions")

    class OpenAIEmbeddingFunction:
        def __init__(self, **kwargs):
            self.kwargs = kwargs
            state.setdefault("embedding_inits", []).append(kwargs)

    class Collection:
        def __init__(self):
            self.upsert_calls = []
            self.query_calls = []

        def upsert(self, **kwargs):
            self.upsert_calls.append(kwargs)

        def query(self, **kwargs):
            self.query_calls.append(kwargs)
            return state.get("query_result", {"documents": [[]], "ids": [[]]})

    class PersistentClient:
        def __init__(self, path):
            self.path = path
            self.collection = Collection()
            state.setdefault("clients", []).append(self)

        def get_or_create_collection(self, **kwargs):
            state["get_or_create_collection_kwargs"] = kwargs
            return self.collection

        def get_collection(self, **kwargs):
            state["get_collection_kwargs"] = kwargs
            return self.collection

    chromadb_mod.PersistentClient = PersistentClient
    embedding_functions_mod.OpenAIEmbeddingFunction = OpenAIEmbeddingFunction
    chromadb_utils_mod.embedding_functions = embedding_functions_mod

    monkeypatch.setitem(sys.modules, "chromadb", chromadb_mod)
    monkeypatch.setitem(sys.modules, "chromadb.utils", chromadb_utils_mod)
    monkeypatch.setitem(sys.modules, "chromadb.utils.embedding_functions", embedding_functions_mod)

    mcp_mod = ModuleType("mcp")
    mcp_server_mod = ModuleType("mcp.server")
    mcp_server_fastmcp_mod = ModuleType("mcp.server.fastmcp")

    class FastMCP:
        def __init__(self, name):
            self.name = name
            self.tools = []
            self.run_called = False
            state["fastmcp_instance"] = self

        def tool(self):
            def _decorator(func):
                self.tools.append(func)
                return func

            return _decorator

        def run(self):
            self.run_called = True

    mcp_server_fastmcp_mod.FastMCP = FastMCP
    monkeypatch.setitem(sys.modules, "mcp", mcp_mod)
    monkeypatch.setitem(sys.modules, "mcp.server", mcp_server_mod)
    monkeypatch.setitem(sys.modules, "mcp.server.fastmcp", mcp_server_fastmcp_mod)


def test_api_print_messages_handles_dict_and_object(capsys, monkeypatch):
    state = {}
    _stub_common_ai_modules(monkeypatch, state)
    api_module = _load_module("api_for_print_test", AI_SERVICE_DIR / "api.py")

    class AssistantMessage:
        role = "assistant"
        content = None

    api_module._print_messages("debug", [{"role": "user", "content": "hello\nworld"}, AssistantMessage()])
    output = capsys.readouterr().out
    assert "--- debug (count=2) ---" in output
    assert "role=user content='hello world'" in output
    assert "role=assistant content=''" in output


@pytest.mark.asyncio
async def test_api_chat_endpoint_requires_mcp_session(monkeypatch):
    state = {}
    _stub_common_ai_modules(monkeypatch, state)
    api_module = _load_module("api_for_503_test", AI_SERVICE_DIR / "api.py")
    api_module.mcp_session = None

    request = SimpleNamespace(message="hello", history=[])
    with pytest.raises(api_module.HTTPException) as exc_info:
        await api_module.chat_endpoint(request)

    assert exc_info.value.status_code == 503
    assert exc_info.value.detail == "MCP Server not ready"


@pytest.mark.asyncio
async def test_api_chat_endpoint_without_tool_call(monkeypatch):
    state = {
        "tools": [SimpleNamespace(name="search_faq_knowledge_base", description="desc", inputSchema={"type": "object"})]
    }
    _stub_common_ai_modules(monkeypatch, state)
    api_module = _load_module("api_no_tool_test", AI_SERVICE_DIR / "api.py")

    openai_calls = []
    assistant_message = SimpleNamespace(role="assistant", tool_calls=None, content="Plain answer")
    completion_response = SimpleNamespace(choices=[SimpleNamespace(message=assistant_message)])

    def fake_create(**kwargs):
        openai_calls.append(kwargs)
        return completion_response

    api_module.ai_client.chat.completions.create = fake_create

    class Session:
        async def list_tools(self):
            return SimpleNamespace(tools=state["tools"])

    api_module.mcp_session = Session()
    history = [{"role": "user", "content": f"m{i}"} for i in range(30)]
    response = await api_module.chat_endpoint(SimpleNamespace(message="latest", history=history))

    assert response == {"reply": "Plain answer"}
    assert len(openai_calls) == 1
    sent_messages = openai_calls[0]["messages"]
    assert sent_messages[1]["content"] == "m10"
    assert sent_messages[-1] == {"role": "user", "content": "latest"}
    assert len(openai_calls[0]["tools"]) == 1


@pytest.mark.asyncio
async def test_api_chat_endpoint_with_tool_call(monkeypatch):
    state = {
        "tools": [SimpleNamespace(name="search_faq_knowledge_base", description="desc", inputSchema={"type": "object"})]
    }
    _stub_common_ai_modules(monkeypatch, state)
    api_module = _load_module("api_tool_test", AI_SERVICE_DIR / "api.py")

    tool_call = SimpleNamespace(
        id="tool-1",
        function=SimpleNamespace(name="search_faq_knowledge_base", arguments=json.dumps({"query": "refund"})),
    )
    first_message = SimpleNamespace(role="assistant", tool_calls=[tool_call], content=None)
    final_message = SimpleNamespace(content="Refund details")
    responses = [
        SimpleNamespace(choices=[SimpleNamespace(message=first_message)]),
        SimpleNamespace(choices=[SimpleNamespace(message=final_message)]),
    ]
    openai_calls = []

    def fake_create(**kwargs):
        openai_calls.append(kwargs)
        return responses.pop(0)

    class Session:
        def __init__(self):
            self.called = []

        async def list_tools(self):
            return SimpleNamespace(tools=state["tools"])

        async def call_tool(self, name, args):
            self.called.append((name, args))
            return SimpleNamespace(content=["faq chunk"])

    api_module.ai_client.chat.completions.create = fake_create
    api_module.mcp_session = Session()

    response = await api_module.chat_endpoint(SimpleNamespace(message="Can I cancel?", history=[]))

    assert response == {"reply": "Refund details"}
    assert api_module.mcp_session.called == [("search_faq_knowledge_base", {"query": "refund"})]
    assert len(openai_calls) == 2
    assert "tools" not in openai_calls[1]


@pytest.mark.asyncio
async def test_api_lifespan_initializes_mcp_session(monkeypatch):
    state = {}
    _stub_common_ai_modules(monkeypatch, state)
    api_module = _load_module("api_lifespan_test", AI_SERVICE_DIR / "api.py")

    async with api_module.lifespan(api_module.app):
        assert api_module.mcp_session is state["client_session"]
        assert state["client_session"].initialized is True


def test_api_main_block_calls_uvicorn(monkeypatch):
    state = {}
    _stub_common_ai_modules(monkeypatch, state)
    runpy.run_path(str(AI_SERVICE_DIR / "api.py"), run_name="__main__")

    assert len(state["uvicorn_calls"]) == 1
    _, kwargs = state["uvicorn_calls"][0]
    assert kwargs["host"] == "0.0.0.0"
    assert kwargs["port"] == 8000
    assert kwargs["workers"] == 1


@pytest.mark.asyncio
async def test_client_run_chat_loop_tool_and_non_tool_paths(monkeypatch):
    state = {
        "tools": [SimpleNamespace(name="search_faq_knowledge_base", description="desc", inputSchema={"type": "object"})],
        "tool_content": ["policy text"],
    }
    _stub_common_ai_modules(monkeypatch, state)
    client_module = _load_module("client_chat_loop_test", AI_SERVICE_DIR / "client.py")

    tool_call = SimpleNamespace(
        id="call-1",
        function=SimpleNamespace(name="search_faq_knowledge_base", arguments=json.dumps({"query": "refund"})),
    )
    assistant_with_tool = SimpleNamespace(role="assistant", tool_calls=[tool_call], content=None)
    final_assistant = SimpleNamespace(role="assistant", content="Tool answer")
    assistant_without_tool = SimpleNamespace(role="assistant", tool_calls=None, content="Direct answer")

    responses = [
        SimpleNamespace(choices=[SimpleNamespace(message=assistant_with_tool)]),
        SimpleNamespace(choices=[SimpleNamespace(message=final_assistant)]),
        SimpleNamespace(choices=[SimpleNamespace(message=assistant_without_tool)]),
    ]
    openai_calls = []

    def fake_create(**kwargs):
        openai_calls.append(kwargs)
        return responses.pop(0)

    client_module.ai_client.chat.completions.create = fake_create
    user_inputs = iter(["Need refund help", "hello", "quit"])
    monkeypatch.setattr("builtins.input", lambda _prompt: next(user_inputs))

    await client_module.run_chat_loop()

    assert len(openai_calls) == 3
    assert state["client_session"].tool_calls == [("search_faq_knowledge_base", {"query": "refund"})]


def test_client_main_block_calls_asyncio_run(monkeypatch):
    state = {}
    _stub_common_ai_modules(monkeypatch, state)
    asyncio_calls = []

    def fake_asyncio_run(coro):
        asyncio_calls.append(coro)
        coro.close()

    monkeypatch.setattr("asyncio.run", fake_asyncio_run)
    runpy.run_path(str(AI_SERVICE_DIR / "client.py"), run_name="__main__")
    assert len(asyncio_calls) == 1


def test_ingest_module_requires_openai_key(monkeypatch):
    state = {}
    _stub_ingest_server_modules(monkeypatch, state)
    monkeypatch.delenv("OPENAI_API_KEY", raising=False)
    with pytest.raises(ValueError):
        _load_module("ingest_missing_key_test", AI_SERVICE_DIR / "ingest.py")


def test_ingest_faqs_handles_missing_file(capsys, monkeypatch):
    state = {}
    _stub_ingest_server_modules(monkeypatch, state)
    monkeypatch.setenv("OPENAI_API_KEY", "test-key")
    ingest_module = _load_module("ingest_missing_file_test", AI_SERVICE_DIR / "ingest.py")
    monkeypatch.setattr(ingest_module.os.path, "exists", lambda _path: False)

    ingest_module.ingest_faqs()
    output = capsys.readouterr().out

    assert "Error: File not found" in output
    assert state["clients"][0].collection.upsert_calls == []


def test_ingest_faqs_upserts_valid_sections(capsys, monkeypatch):
    state = {}
    _stub_ingest_server_modules(monkeypatch, state)
    monkeypatch.setenv("OPENAI_API_KEY", "test-key")
    ingest_module = _load_module("ingest_success_test", AI_SERVICE_DIR / "ingest.py")
    monkeypatch.setattr(ingest_module.os.path, "exists", lambda _path: True)
    monkeypatch.setattr(
        "builtins.open",
        mock_open(read_data="### Refund\nPolicy details\n###\n### Pickup\nPickup details"),
    )

    ingest_module.ingest_faqs()
    output = capsys.readouterr().out

    assert "Successfully loaded 2 FAQs into ChromaDB!" in output
    upsert_payload = state["clients"][0].collection.upsert_calls[0]
    assert upsert_payload["documents"] == ["Refund\nPolicy details", "Pickup\nPickup details"]
    assert upsert_payload["ids"] == ["faq_1", "faq_3"]


def test_ingest_faqs_handles_no_valid_sections(capsys, monkeypatch):
    state = {}
    _stub_ingest_server_modules(monkeypatch, state)
    monkeypatch.setenv("OPENAI_API_KEY", "test-key")
    ingest_module = _load_module("ingest_empty_sections_test", AI_SERVICE_DIR / "ingest.py")
    monkeypatch.setattr(ingest_module.os.path, "exists", lambda _path: True)
    monkeypatch.setattr("builtins.open", mock_open(read_data="###   ###"))

    ingest_module.ingest_faqs()
    output = capsys.readouterr().out
    assert "No valid FAQs found to ingest." in output


def test_ingest_main_block_invokes_ingest(capsys, monkeypatch):
    state = {}
    _stub_ingest_server_modules(monkeypatch, state)
    monkeypatch.setenv("OPENAI_API_KEY", "test-key")
    monkeypatch.setattr("os.path.exists", lambda _path: False)
    runpy.run_path(str(AI_SERVICE_DIR / "ingest.py"), run_name="__main__")
    assert "Error: File not found" in capsys.readouterr().out


def test_server_search_returns_joined_documents(monkeypatch):
    state = {"query_result": {"documents": [["A", "B"]], "ids": [["faq_1", "faq_2"]]}}
    _stub_ingest_server_modules(monkeypatch, state)
    monkeypatch.setenv("OPENAI_API_KEY", "test-key")
    server_module = _load_module("server_query_join_test", AI_SERVICE_DIR / "server.py")

    result = server_module.search_faq_knowledge_base("refund")

    assert result == "A\n\nB"
    query_payload = state["clients"][0].collection.query_calls[0]
    assert query_payload == {"query_texts": ["refund"], "n_results": 3}


def test_server_search_returns_fallback_when_empty(monkeypatch):
    state = {"query_result": {"documents": [[]], "ids": [[]]}}
    _stub_ingest_server_modules(monkeypatch, state)
    monkeypatch.setenv("OPENAI_API_KEY", "test-key")
    server_module = _load_module("server_query_empty_test", AI_SERVICE_DIR / "server.py")

    result = server_module.search_faq_knowledge_base("missing")
    assert result == "No relevant FAQ information found."


def test_server_main_block_runs_mcp(monkeypatch):
    state = {}
    _stub_ingest_server_modules(monkeypatch, state)
    monkeypatch.setenv("OPENAI_API_KEY", "test-key")
    globals_dict = runpy.run_path(str(AI_SERVICE_DIR / "server.py"), run_name="__main__")
    assert globals_dict["mcp"].run_called is True
