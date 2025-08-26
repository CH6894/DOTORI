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
# pdf 문서 기반의 질의 응답을 처리하는 주문 창고
@router.post(
    "/chat",
    response_model=ChatResponse,
    summary="챗봇 대화",
    description="사용자 메시지를 받아 AI 응답을 생성합니다."
)
async def gen_answer(req: ChatRequest):
    """
    - PDF 로딩은 서비스 초기화 시 한 번만
    - 매 요청마다 빠른 검색만 실행
    - 에러 처리 강화
    """
    try:
        logger.info(f'입력: {req.message}')

        ai_response, session_id = await openai_service.generate_response(
            user_message=req.message,
            session_id=req.session_id,  # 세션 관리
            conversation_history=req.conversation_history
        )
        
        # 응답 객체 생성 (기존 코드 참조하여 구조 맞춤)
        response = ChatResponse(
            response=ai_response,  # 기존: result["result"] → 개선: ai_response
            session_id=session_id,
            timestamp=datetime.now(),
            model_used="gpt-3.5-turbo"
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

@router.post(
    "/chat/general", 
    response_model=ChatResponse,
    summary="일반 대화",
    description="문서 검색 없이 일반적인 대화를 합니다."
)
async def general_chat(req: ChatRequest):
    """
    일반 대화 모드 (PDF 검색 없이)
    - 다람쥐 캐릭터로 자유로운 대화
    - 빠른 응답 속도
    """
    try:
        logger.info(f'일반 대화 입력: {req.message}')
        
        # 일반 대화형 응답 생성 (PDF 검색 없음)
        ai_response, session_id = await openai_service.generate_general_response(
            user_message=req.message,
            conversation_history=req.conversation_history
        )
        
        response = ChatResponse(
            response=ai_response,
            session_id=req.session_id or session_id,
            timestamp=datetime.now(),
            model_used="gpt-3.5-turbo"
        )
        
        return response
        
    except Exception as e:
        logger.error(f"❌ 일반 대화 실패: {str(e)}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"일반 대화 처리 실패: {str(e)}"
        )