import requests
import sys

def test_server():
    """백엔드 서버 연결 테스트"""
    try:
        # 서버 상태 확인
        response = requests.get('http://localhost:8000/', timeout=5)
        print(f"✅ 서버 연결 성공 (상태 코드: {response.status_code})")
        print(f"응답: {response.json()}")
        
        # 챗봇 API 테스트
        chat_response = requests.post('http://localhost:8000/api/chat', 
                                     json={'query': '안녕하세요', 'user_id': 'test'}, 
                                     timeout=10)
        print(f"✅ 챗봇 API 연결 성공 (상태 코드: {chat_response.status_code})")
        print(f"응답: {chat_response.json()}")
        
        return True
        
    except requests.exceptions.ConnectionError:
        print("❌ 서버에 연결할 수 없습니다. 서버가 실행 중인지 확인해주세요.")
        return False
    except requests.exceptions.Timeout:
        print("❌ 서버 응답 시간 초과")
        return False
    except Exception as e:
        print(f"❌ 오류 발생: {e}")
        return False

if __name__ == "__main__":
    print("백엔드 서버 연결 테스트 시작...")
    success = test_server()
    sys.exit(0 if success else 1)
