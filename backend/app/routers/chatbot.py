from fastapi import APIRouter
from main import rag_service, ChatRequest, ChatResponse

router = APIRouter(prefix="/api")

@router.post("/chat", response_model=ChatResponse)
def gen_answer(req: ChatRequest):
    print(f'사용자 {req.user_id}의 질문: {req.query}')
    result = rag_service.get_answer(req.query, req.user_id)
    return ChatResponse(answer=result)