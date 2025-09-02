from fastapi import APIRouter, HTTPException, status
from datetime import datetime
import logging
from app.models.chat import ChatRequest, ChatResponse, ErrorResponse
from app.services.openai_service import openai_service

logger = logging.getLogger(__name__)

router = APIRouter(
    prefix="/api", 
    tags=["챗봇"],
    responses={
        404: {"description": "Not found"},
        500: {"model": ErrorResponse}
    }
)

_is_initialized = False

async def initialize_openai_service():
    """앱 시작시 OpenAI 서비스 초기화 (PDF 로딩 포함)"""
    global _is_initialized
    if not _is_initialized:
        logger.info("🔄 OpenAI 서비스 초기화 중...")
        await openai_service.initialize()  # PDF 로딩 + 벡터스토어 생성
        _is_initialized = True
        logger.info("✅ OpenAI 서비스 초기화 완료")

@router.post(
    "/chat",
    response_model=ChatResponse,
    summary="챗봇 대화",
    description="사용자 메시지를 받아 AI 응답을 생성합니다."
)
async def gen_answer(req: ChatRequest):

    try:
        # 초기화 확인
        await initialize_openai_service()
        
        logger.info(f'입력: {req.message}')

        ai_response, session_id = await openai_service.generate_response(
            user_message=req.message,
            session_id=req.session_id,
            conversation_history=req.conversation_history
        )
        
        response = ChatResponse(
            response=ai_response,
            session_id=session_id,
            timestamp=datetime.now(),
            model_used="gpt-4o"
        )
        
        logger.info(f'응답 생성 완료: {session_id}')
        return response
         
    except Exception as e:
        error_msg = f"챗봇 응답 생성 실패: {str(e)}"
        logger.error(f"❌ {error_msg}")
        
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=error_msg
        )
    