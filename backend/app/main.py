import logging
from datetime import datetime
from fastapi import FastAPI, status
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse
from app.config.settings import settings
from app.routers import chatbot

#파일의 실행 중 어떤 곳에서 오류가 났는지 쉽게 확인하기 위한 장치
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s | %(levelname)s | %(name)s | %(message)s'
)
# 현재 파일 전용 로그 기록기
logger = logging.getLogger(__name__)

# === FastAPI 앱 생성 ===
app = FastAPI(
    title="도토리 챗봇 API",
    version="1.0.0", 
    description="친근한 다람이 AI 어시스턴트 🐿️",
    debug=settings.DEBUG,
    docs_url="/docs",   
    redoc_url="/redoc"
)

#보안을 이해 출발지 목록을 정해서 그것만 통과 가능하게 
app.add_middleware(
    CORSMiddleware,
    allow_origins=[
        "http://localhost:3000",    # React 기본 포트
        "http://localhost:5173",    # Vite 기본 포트
        "http://localhost:8081",    # Vue CLI 기본 포트
    ],
    allow_credentials=True,
    # 요청방식 허용 목록
    allow_methods=["GET", "POST", "PUT", "DELETE"],
    allow_headers=["*"],
)

app.include_router(
    chatbot.router
)

@app.get("/", tags=["기본"])
async def root():
    """
    API 루트 엔드포인트
    - 앱이 정상 작동하는지 확인용
    - 기본 정보 제공
    """
    return {
        "message": "🐿️ 다람이 챗봇 API가 실행 중입니다!",
        "version": "1.0.0",
        "docs": "/docs",
        "endpoints": {
            "pdf_chat": "/api/chat",
            "health": "/api/health"
        }
    }

# === 전역 헬스체크 ===
@app.get("/health", tags=["기본"])
async def health():
    """전체 서비스 상태 확인"""
    return {
        "status": "healthy",
        "app": "도토리 챗봇 API",
        "version": "1.0.0",
        "timestamp": datetime.now(),
        "debug_mode": settings.DEBUG
    }

# === 전역 예외 처리 ===
@app.exception_handler(Exception)
async def global_exception_handler(request, exc):
    """
    앱 전체에서 발생하는 예상치 못한 에러 처리
    - 사용자에게는 친절한 메시지
    - 개발자에게는 상세한 로그
    """
    logger.error(f"❌ 전역 예외 발생: {str(exc)}")
    logger.error(f"요청 URL: {request.url}")
    
    return JSONResponse(
        status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
        content={
            "error": "내부 서버 오류가 발생했습니다.",
            "message": "잠시 후 다시 시도해주세요.",
            "detail": str(exc) if settings.DEBUG else None  # 디버그 모드일 때만 상세 정보
        }
    )

# === 앱 시작 이벤트 ===
@app.on_event("startup")
async def startup_event():
    """
    앱이 시작될 때 실행되는 함수
    - 초기화 작업
    - 상태 확인
    - 시작 로그
    """
    logger.info("🚀 도토리 챗봇 API 시작 중...")
    logger.info(f"📋 API 문서: http://127.0.0.1:8000/docs")
    logger.info(f"🔍 디버그 모드: {settings.DEBUG}")
    
    # OpenAI 서비스 초기화 확인
    try:
        from app.services.openai_service import openai_service
        logger.info("✅ OpenAI 서비스 초기화 완료")
        
        # PDF 로딩 상태 확인
        if hasattr(openai_service, 'chain'):
            logger.info("📄 PDF RAG 시스템 준비 완료")
        else:
            logger.warning("⚠️ PDF RAG 시스템 초기화 실패")
            
    except Exception as e:
        logger.error(f"❌ OpenAI 서비스 초기화 실패: {e}")
    
    logger.info("🎉 도토리 챗봇 API 시작 완료!")

# === 앱 종료 이벤트 ===
@app.on_event("shutdown") 
async def shutdown_event():
    """
    앱이 종료될 때 실행되는 함수
    - 정리 작업
    - 연결 해제
    - 종료 로그
    """
    logger.info("🛑 도토리 챗봇 API 종료 중...")
    
    # 필요시 여기서 정리 작업 (DB 연결 해제, 파일 저장 등)
    
    logger.info("👋 도토리 챗봇 API 종료 완료!")

# === 개발용 정보 ===
if __name__ == "__main__":
    import uvicorn
    uvicorn.run(
        "app.main:app",
        host="127.0.0.1", 
        port=8000,
        reload=True  # 코드 변경시 자동 재시작
    )