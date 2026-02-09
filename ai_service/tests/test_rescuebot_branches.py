import runpy
import sys
from pathlib import Path
from types import SimpleNamespace

import pytest

import test_rescuebot as rescuebot


AI_SERVICE_DIR = Path(__file__).resolve().parents[1]


class _FakeResponse:
    def __init__(self, status_code=200, payload=None, text=""):
        self.status_code = status_code
        self._payload = payload if payload is not None else {}
        self.text = text

    def json(self):
        return self._payload


def test_print_status_all_paths(capsys):
    rescuebot.print_status("✓", "ok")
    rescuebot.print_status("✗", "bad")
    rescuebot.print_status("→", "next")
    rescuebot.print_status("⚠", "warn")
    output = capsys.readouterr().out
    assert "[✓]" in output
    assert "[✗]" in output
    assert "[→]" in output
    assert "[⚠]" in output


def test_service_availability_branches(monkeypatch):
    monkeypatch.setattr(rescuebot.requests, "get", lambda *_args, **_kwargs: _FakeResponse(status_code=200))
    assert rescuebot.test_service_availability() is True

    monkeypatch.setattr(rescuebot.requests, "get", lambda *_args, **_kwargs: _FakeResponse(status_code=503))
    assert rescuebot.test_service_availability() is False

    monkeypatch.setattr(
        rescuebot.requests,
        "get",
        lambda *_args, **_kwargs: (_ for _ in ()).throw(rescuebot.requests.exceptions.ConnectionError()),
    )
    assert rescuebot.test_service_availability() is False

    monkeypatch.setattr(
        rescuebot.requests,
        "get",
        lambda *_args, **_kwargs: (_ for _ in ()).throw(rescuebot.requests.exceptions.Timeout()),
    )
    assert rescuebot.test_service_availability() is False

    monkeypatch.setattr(
        rescuebot.requests,
        "get",
        lambda *_args, **_kwargs: (_ for _ in ()).throw(RuntimeError("boom")),
    )
    assert rescuebot.test_service_availability() is False


def test_chat_simple_branches(monkeypatch):
    monkeypatch.setattr(
        rescuebot.requests,
        "post",
        lambda *_args, **_kwargs: _FakeResponse(status_code=200, payload={"reply": "answer"}),
    )
    assert rescuebot.test_chat_simple() is True

    monkeypatch.setattr(
        rescuebot.requests,
        "post",
        lambda *_args, **_kwargs: _FakeResponse(status_code=200, payload={"reply": ""}),
    )
    assert rescuebot.test_chat_simple() is False

    monkeypatch.setattr(
        rescuebot.requests,
        "post",
        lambda *_args, **_kwargs: _FakeResponse(status_code=500, payload={}, text="error"),
    )
    assert rescuebot.test_chat_simple() is False

    monkeypatch.setattr(
        rescuebot.requests,
        "post",
        lambda *_args, **_kwargs: (_ for _ in ()).throw(rescuebot.requests.exceptions.Timeout()),
    )
    assert rescuebot.test_chat_simple() is False

    monkeypatch.setattr(
        rescuebot.requests,
        "post",
        lambda *_args, **_kwargs: (_ for _ in ()).throw(RuntimeError("unexpected")),
    )
    assert rescuebot.test_chat_simple() is False


def test_chat_with_history_branches(monkeypatch):
    monkeypatch.setattr(
        rescuebot.requests,
        "post",
        lambda *_args, **_kwargs: _FakeResponse(status_code=200, payload={"reply": "history ok"}),
    )
    assert rescuebot.test_chat_with_history() is True

    monkeypatch.setattr(
        rescuebot.requests,
        "post",
        lambda *_args, **_kwargs: _FakeResponse(status_code=200, payload={"reply": ""}),
    )
    assert rescuebot.test_chat_with_history() is False

    monkeypatch.setattr(
        rescuebot.requests,
        "post",
        lambda *_args, **_kwargs: _FakeResponse(status_code=500, payload={}),
    )
    assert rescuebot.test_chat_with_history() is False

    monkeypatch.setattr(
        rescuebot.requests,
        "post",
        lambda *_args, **_kwargs: (_ for _ in ()).throw(rescuebot.requests.exceptions.Timeout()),
    )
    assert rescuebot.test_chat_with_history() is False

    monkeypatch.setattr(
        rescuebot.requests,
        "post",
        lambda *_args, **_kwargs: (_ for _ in ()).throw(RuntimeError("unexpected")),
    )
    assert rescuebot.test_chat_with_history() is False


def test_off_topic_question_branches(monkeypatch):
    monkeypatch.setattr(
        rescuebot.requests,
        "post",
        lambda *_args, **_kwargs: _FakeResponse(status_code=200, payload={"reply": "Food Rescue only"}),
    )
    assert rescuebot.test_off_topic_question() is True

    monkeypatch.setattr(
        rescuebot.requests,
        "post",
        lambda *_args, **_kwargs: _FakeResponse(status_code=200, payload={"reply": "Random reply"}),
    )
    assert rescuebot.test_off_topic_question() is True

    monkeypatch.setattr(
        rescuebot.requests,
        "post",
        lambda *_args, **_kwargs: _FakeResponse(status_code=500, payload={}),
    )
    assert rescuebot.test_off_topic_question() is False

    monkeypatch.setattr(
        rescuebot.requests,
        "post",
        lambda *_args, **_kwargs: (_ for _ in ()).throw(RuntimeError("unexpected")),
    )
    assert rescuebot.test_off_topic_question() is False


def test_bot_identity_branches(monkeypatch):
    monkeypatch.setattr(
        rescuebot.requests,
        "post",
        lambda *_args, **_kwargs: _FakeResponse(status_code=200, payload={"reply": "I am RescueBot"}),
    )
    assert rescuebot.test_bot_identity() is True

    monkeypatch.setattr(
        rescuebot.requests,
        "post",
        lambda *_args, **_kwargs: _FakeResponse(status_code=200, payload={"reply": "Unknown bot"}),
    )
    assert rescuebot.test_bot_identity() is True

    monkeypatch.setattr(
        rescuebot.requests,
        "post",
        lambda *_args, **_kwargs: _FakeResponse(status_code=500, payload={}),
    )
    assert rescuebot.test_bot_identity() is False

    monkeypatch.setattr(
        rescuebot.requests,
        "post",
        lambda *_args, **_kwargs: (_ for _ in ()).throw(RuntimeError("unexpected")),
    )
    assert rescuebot.test_bot_identity() is False


def test_main_exit_code_paths(monkeypatch):
    monkeypatch.setattr(rescuebot.time, "sleep", lambda *_args, **_kwargs: None)
    monkeypatch.setattr(rescuebot, "print_status", lambda *_args, **_kwargs: None)

    monkeypatch.setattr(rescuebot, "test_service_availability", lambda: True)
    monkeypatch.setattr(rescuebot, "test_chat_simple", lambda: True)
    monkeypatch.setattr(rescuebot, "test_chat_with_history", lambda: True)
    monkeypatch.setattr(rescuebot, "test_bot_identity", lambda: True)
    monkeypatch.setattr(rescuebot, "test_off_topic_question", lambda: True)
    assert rescuebot.main() == 0

    monkeypatch.setattr(rescuebot, "test_chat_simple", lambda: False)
    assert rescuebot.main() == 1

    def _raise_error():
        raise RuntimeError("failure")

    monkeypatch.setattr(rescuebot, "test_chat_simple", _raise_error)
    monkeypatch.setattr(rescuebot, "test_chat_with_history", lambda: False)
    monkeypatch.setattr(rescuebot, "test_bot_identity", lambda: False)
    assert rescuebot.main() == 2


def test_script_main_calls_sys_exit(monkeypatch):
    class _ExitCalled(Exception):
        pass

    monkeypatch.setattr(rescuebot.time, "sleep", lambda *_args, **_kwargs: None)

    def fake_get(*_args, **_kwargs):
        return _FakeResponse(status_code=200)

    def fake_post(*_args, **_kwargs):
        return _FakeResponse(status_code=200, payload={"reply": "RescueBot Food Rescue"})

    monkeypatch.setattr("requests.get", fake_get)
    monkeypatch.setattr("requests.post", fake_post)

    exit_codes = []

    def fake_exit(code=0):
        exit_codes.append(code)
        raise _ExitCalled()

    monkeypatch.setattr(sys, "exit", fake_exit)

    with pytest.raises(_ExitCalled):
        runpy.run_path(str(AI_SERVICE_DIR / "test_rescuebot.py"), run_name="__main__")

    assert exit_codes == [0]
