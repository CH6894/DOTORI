from fastapi import APIRouter, HTTPException
from pydantic import BaseModel

router = APIRouter(prefix="/api", tags=["Chatbot"])

# 요청/응답 모델
class ChatRequest(BaseModel):
    query: str
    user_id: str = "default"

class ChatResponse(BaseModel):
    answer: str

@router.post("/chat")
def chat(request: ChatRequest):
    from main import chatbot_service
    
    try:
        # 입력 검증
        if not request.query or not request.query.strip():
            raise HTTPException(status_code=400, detail="질문을 입력해주세요.")
        
        # 답변 생성
        answer = chatbot_service.get_answer(request.query.strip(), request.user_id)
        return ChatResponse(answer=answer)
        
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"서버 오류: {str(e)}")

@router.get("/help")
def get_help():
    return {
        "message": "다람이 챗봇 사용법",
        "features": [
            "상품 검색: 키워드로 상품 찾기",
            "검수 기준: S/A/B/C 등급 설명",
            "FAQ: 자주 묻는 질문 답변"
        ],
        "examples": [
            "원피스 흰수염 찾아줘",
            "검수 A등급이 뭐야?",
            "할인율은 어떻게 정해지나요?"
        ]
    }