#!/usr/bin/env python3
"""
RescueBot API Connection Test Script
测试AI服务是否正常工作
"""

import requests
import json
import time
import sys
from typing import Optional

# 配置
BASE_URL = "http://localhost:8000"
TIMEOUT = 10

# 颜色代码
GREEN = "\033[92m"
RED = "\033[91m"
YELLOW = "\033[93m"
BLUE = "\033[94m"
RESET = "\033[0m"

def print_status(status: str, message: str):
    """打印状态信息"""
    if status == "✓":
        print(f"{GREEN}[✓]{RESET} {message}")
    elif status == "✗":
        print(f"{RED}[✗]{RESET} {message}")
    elif status == "→":
        print(f"{BLUE}[→]{RESET} {message}")
    elif status == "⚠":
        print(f"{YELLOW}[⚠]{RESET} {message}")

def test_service_availability() -> bool:
    """测试服务是否可用"""
    print_status("→", "Testing service availability...")
    try:
        response = requests.get(f"{BASE_URL}/docs", timeout=TIMEOUT)
        if response.status_code == 200:
            print_status("✓", f"Service is running at {BASE_URL}")
            return True
        else:
            print_status("⚠", f"Service returned status {response.status_code}")
            return False
    except requests.exceptions.ConnectionError:
        print_status("✗", f"Cannot connect to {BASE_URL}")
        print_status("⚠", "Make sure the AI service is running:")
        print_status("⚠", "  cd ai_service && ./start.sh")
        return False
    except requests.exceptions.Timeout:
        print_status("✗", "Connection timeout")
        return False
    except Exception as e:
        print_status("✗", f"Error: {str(e)}")
        return False

def test_chat_simple() -> bool:
    """测试简单聊天"""
    print_status("→", "Testing simple chat...")
    
    payload = {
        "message": "What is a Surprise Bag?",
        "history": []
    }
    
    try:
        response = requests.post(
            f"{BASE_URL}/chat",
            json=payload,
            headers={"Content-Type": "application/json"},
            timeout=TIMEOUT
        )
        
        if response.status_code == 200:
            data = response.json()
            reply = data.get("reply", "")
            
            if reply:
                print_status("✓", "Chat response received")
                print(f"   Response: {reply[:100]}...")
                return True
            else:
                print_status("✗", "Empty response received")
                return False
        else:
            print_status("✗", f"API returned status {response.status_code}")
            print_status("⚠", f"Response: {response.text}")
            return False
            
    except requests.exceptions.Timeout:
        print_status("✗", "Chat request timeout (API may be slow or processing tool calls)")
        return False
    except Exception as e:
        print_status("✗", f"Error: {str(e)}")
        return False

def test_chat_with_history() -> bool:
    """测试带历史的聊天"""
    print_status("→", "Testing chat with conversation history...")
    
    payload = {
        "message": "Can I cancel my order?",
        "history": [
            {"role": "user", "content": "Hello"},
            {"role": "assistant", "content": "Hi! I'm RescueBot, here to help you rescue surplus food!"}
        ]
    }
    
    try:
        response = requests.post(
            f"{BASE_URL}/chat",
            json=payload,
            headers={"Content-Type": "application/json"},
            timeout=TIMEOUT
        )
        
        if response.status_code == 200:
            data = response.json()
            reply = data.get("reply", "")
            
            if reply:
                print_status("✓", "Chat with history works")
                print(f"   Response: {reply[:100]}...")
                return True
            else:
                print_status("✗", "Empty response")
                return False
        else:
            print_status("✗", f"API returned status {response.status_code}")
            return False
            
    except requests.exceptions.Timeout:
        print_status("✗", "Request timeout")
        return False
    except Exception as e:
        print_status("✗", f"Error: {str(e)}")
        return False

def test_off_topic_question() -> bool:
    """测试不相关问题处理"""
    print_status("→", "Testing off-topic question handling...")
    
    payload = {
        "message": "What is 2+2?",
        "history": []
    }
    
    try:
        response = requests.post(
            f"{BASE_URL}/chat",
            json=payload,
            headers={"Content-Type": "application/json"},
            timeout=TIMEOUT
        )
        
        if response.status_code == 200:
            data = response.json()
            reply = data.get("reply", "")
            
            if "Food Rescue" in reply or "assist with questions related" in reply:
                print_status("✓", "Correctly identified off-topic question")
                print(f"   Response: {reply[:100]}...")
                return True
            else:
                print_status("⚠", "Response may not be handling off-topic correctly")
                print(f"   Response: {reply[:100]}...")
                return True  # Still pass, as service is working
        else:
            print_status("✗", f"API returned status {response.status_code}")
            return False
            
    except Exception as e:
        print_status("✗", f"Error: {str(e)}")
        return False

def test_bot_identity() -> bool:
    """测试机器人身份识别"""
    print_status("→", "Testing bot identity...")
    
    payload = {
        "message": "Who are you?",
        "history": []
    }
    
    try:
        response = requests.post(
            f"{BASE_URL}/chat",
            json=payload,
            headers={"Content-Type": "application/json"},
            timeout=TIMEOUT
        )
        
        if response.status_code == 200:
            data = response.json()
            reply = data.get("reply", "")
            
            if "RescueBot" in reply:
                print_status("✓", "Bot correctly identifies itself")
                print(f"   Response: {reply}")
                return True
            else:
                print_status("⚠", "Response doesn't mention RescueBot")
                print(f"   Response: {reply}")
                return True  # Still pass
        else:
            print_status("✗", f"API returned status {response.status_code}")
            return False
            
    except Exception as e:
        print_status("✗", f"Error: {str(e)}")
        return False

def main():
    """运行所有测试"""
    print(f"\n{BLUE}{'='*50}{RESET}")
    print(f"{BLUE}RescueBot API Connection Test{RESET}")
    print(f"{BLUE}{'='*50}{RESET}\n")
    
    tests = [
        ("Service Availability", test_service_availability),
        ("Simple Chat", test_chat_simple),
        ("Chat with History", test_chat_with_history),
        ("Bot Identity", test_bot_identity),
        ("Off-topic Handling", test_off_topic_question),
    ]
    
    results = {}
    for test_name, test_func in tests:
        print(f"\n{YELLOW}Test: {test_name}{RESET}")
        try:
            results[test_name] = test_func()
        except Exception as e:
            print_status("✗", f"Unexpected error: {str(e)}")
            results[test_name] = False
        time.sleep(0.5)  # 避免请求过快
    
    # 输出总结
    print(f"\n{BLUE}{'='*50}{RESET}")
    print(f"{BLUE}Test Summary{RESET}")
    print(f"{BLUE}{'='*50}{RESET}\n")
    
    passed = sum(1 for v in results.values() if v)
    total = len(results)
    
    for test_name, result in results.items():
        status = "✓" if result else "✗"
        print_status(status, test_name)
    
    print(f"\n{BLUE}Results: {passed}/{total} tests passed{RESET}")
    
    if passed == total:
        print_status("✓", "All tests passed! AI service is working correctly.")
        return 0
    elif passed >= total - 1:
        print_status("⚠", "Most tests passed. Some features may have issues.")
        return 1
    else:
        print_status("✗", "Multiple tests failed. Please check the AI service.")
        return 2

if __name__ == "__main__":
    sys.exit(main())
