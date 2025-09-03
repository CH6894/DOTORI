from pydantic_settings import BaseSettings

class Settings(BaseSettings):
    
    OPENAI_API_KEY: str     # 없으면 API 호출 불가능
    DEBUG: bool = True      # 개발할 때 에러 정보 보려면 필요
    
    class Config:
        env_file = ".env"  
# 전역 인스턴스
settings = Settings()

# 간단한 검증
def validate_settings():
    if not settings.OPENAI_API_KEY:
        raise ValueError("❌ OPENAI_API_KEY가 없습니다!")
    print("✅ 설정 완료!")

validate_settings()