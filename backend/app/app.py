from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from routers.chatbot import router

# FastAPI 앱 생성
app = FastAPI(
    title="PDF Chat API",
    description="PDF 문서와 대화할 수 있는 API",
    version="1.0.0"
)

# CORS 설정
app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:5173", "http://127.0.0.1:5173"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# 라우터 등록
app.include_router(router)

@app.get("/")
def read_root():
    return {"message": "PDF Chat API Server is running!"}

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="127.0.0.1", port=8000)