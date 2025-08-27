from pydantic import BaseModel, Field  # 데이터 모델 만들기 위한 도구
from typing import List, Optional      # 타입 힌트 (리스트, 선택적 값)
from datetime import datetime          # 시간 데이터
from enum import Enum                  # 상수 정의

# === 1. 메시지 역할 정의 ===
class MessageRole(str, Enum):

    USER = "user"           # 사용자가 보낸 메시지
    ASSISTANT = "assistant" # AI가 보낸 메시지  
    SYSTEM = "system"       # 시스템 메시지 (설정용)

# === 2. 개별 메시지 구조 ===
class ChatMessage(BaseModel):

    role: MessageRole 

    content: str = Field(
        ...,  # ... = 필수 항목 (None 불가능)
        min_length=1,      # 빈 문자열 불가능
        max_length=4000,   # 너무 긴 메시지 방지
        description="메시지 내용"
    )
    
    # === API 문서에 나올 예시 ===
    class Config:
        json_schema_extra = {
            "example": {
                "role": "user",
                "content": "다람쥐가 좋아하는 음식은 뭐야?"
            }
        }

# === 3. 요청 데이터 구조 (프론트엔드 → 백엔드) ===
class ChatRequest(BaseModel):
    """
    프론트엔드에서 백엔드로 보내는 요청 구조
    
    예시:
    {
        "message": "안녕하세요!",
        "session_id": "user123",
        "conversation_history": [...]
    }
    """
    
    # 현재 사용자가 보낸 메시지 (필수)
    message: str = Field(
        ...,                    # 필수!
        min_length=1,           # 빈 메시지 안 됨
        max_length=4000,        # 길이 제한
        description="사용자 메시지"
    )
    
    # 세션 ID (선택사항)
    session_id: Optional[str] = Field(
        None,                   # 기본값 None = 없어도 됨
        description="세션 ID (없으면 새로 생성)"
    )
    
    # 대화 기록 (선택사항)
    conversation_history: Optional[List[ChatMessage]] = Field(
        default=[],             # 기본값 빈 리스트
        max_items=50,           # 최대 50개까지 (메모리 절약)
        description="이전 대화 기록"
    )
    
    class Config:
        json_schema_extra = {
            "example": {
                "message": "너가 갖고있는 피규어의 장르가 어떻게 돼?",
                "session_id": "user123",
                "conversation_history": [
                    {"role": "user", "content": "안녕하세요!"},
                    {"role": "assistant", "content": "안녕하세요! 🐿️"}
                ]
            }
        }

# === 4. 응답 데이터 구조 (백엔드 → 프론트엔드) ===
class ChatResponse(BaseModel):
    """
    백엔드에서 프론트엔드로 보내는 응답 구조
    
    예시:
    {
        "response": "도토리는 공원에서 주워오세요!",
        "session_id": "user123", 
        "timestamp": "2024-01-15T10:30:00",
        "tokens_used": 25
    }
    """
    
    # AI 응답 내용 (필수)
    response: str = Field(..., description="AI 응답")
    
    # 세션 ID (필수)
    session_id: str = Field(..., description="세션 ID")
    
    # 응답 시간 (자동 생성)
    timestamp: datetime = Field(
        default_factory=datetime.now,  # 현재 시간 자동 입력
        description="응답 생성 시간"
    )
    



class ErrorResponse(BaseModel):

    error: str = Field(..., description="에러 메시지")
    detail: Optional[str] = Field(None, description="상세 에러 정보")
    timestamp: datetime = Field(default_factory=datetime.now)

