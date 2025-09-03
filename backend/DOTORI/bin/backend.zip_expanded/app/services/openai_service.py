from typing import List, Tuple
import uuid
import logging
from langchain_openai import ChatOpenAI, OpenAIEmbeddings
from openai import OpenAI
import json
from langchain.docstore.document import Document
from langchain.memory import ConversationSummaryMemory
from langchain_community.vectorstores import FAISS
from langchain.chains import RetrievalQA

from app.config.settings import settings
from app.models.chat import ChatMessage

logger = logging.getLogger(__name__)

class OpenAIService:
    def __init__(self):
        """기본 서비스만 초기화 (PDF 로딩은 별도로)"""
        try:
            self.llm = ChatOpenAI(
                model="gpt-4o",
                openai_api_key=settings.OPENAI_API_KEY,
                temperature=0.2,
            )
            
            # OpenAI 클라이언트 추가
            self.client = OpenAI(api_key=settings.OPENAI_API_KEY)
            
            self.embeddings = OpenAIEmbeddings(
                openai_api_key=settings.OPENAI_API_KEY
            )
            
            self.session_memories = {}
            
            # ✅ 전역 변수들 초기화만 (실제 로딩은 initialize()에서)
            self.vectorstore = None
            self.retriever = None
            self.chain = None
            self.is_initialized = False
            
            # 프롬프트 캐싱
            self.system_prompt = None
            
            logger.info("✅ 기본 서비스 초기화 완료")
            
        except Exception as e:
            logger.error(f"❌ 기본 서비스 초기화 실패: {e}")
            raise
    
    def get_system_prompt(self):
        """파일에서 시스템 프롬프트 읽어오기"""
        if self.system_prompt is None:
            try:
                with open("prompts/system_prompt.txt", "r", encoding="utf-8") as f:
                    self.system_prompt = f.read()
                logger.info("✅ 시스템 프롬프트 로드 완료")
            except Exception as e:
                logger.error(f"❌ 프롬프트 로드 실패: {e}")
                self.system_prompt = ""
        return self.system_prompt

    async def initialize(self):
        if self.is_initialized:
            return
        try:
            # JSON 데이터가 이미 로드되었는지 확인
            if self.vectorstore is None:
               json_path = "../data/figure_dummy_200.json"
                
            logger.info(f"📄 JSON 문서 로딩 중: {json_path}")
            with open(json_path, 'r', encoding='utf-8') as f:
                data = json.load(f)
                
                # 각 항목을 Document 객체로 변환
                documents = []
                for item in data:
                    content = f"ID: {item['id']}, 이름: {item['name']}, 시리즈: {item['series']}, 스타일: {item['style']}, 가격: {item['price']}원, 출시년도: {item['release_year']}, 상태: {item['condition']}, 재고: {item['stock']}개"
                    doc = Document(
                        page_content=content, 
                        metadata=item
                    )
                    documents.append(doc)
                
                # 벡터스토어 생성
                logger.info("🔍 벡터스토어 생성 중...")
                self.vectorstore = FAISS.from_documents(
                    documents=documents,
                    embedding=self.embeddings
                )
                logger.info(f"✅ JSON 데이터 처리 완료: {len(documents)}개 상품")
            
            # 벡터스토어가 준비되면 나머지 초기화
            if self.retriever is None:
                self.retriever = self.vectorstore.as_retriever(
                    search_kwargs={"k": 10}  # 최대 10개 결과
                )
            
            if self.chain is None:
                # RetrievalQA 체인 생성
                self.chain = RetrievalQA.from_chain_type(
                    llm=self.llm,
                    chain_type="stuff",
                    retriever=self.retriever,
                    return_source_documents=True
                )
            
            self.is_initialized = True
            logger.info("✅ RAG 시스템 초기화 완료")
            
        except Exception as e:
            logger.error(f"❌ JSON 처리 실패: {e}")
            raise
            
    def _get_or_create_memory(self, session_id: str):
        """세션별 메모리 관리"""
        if session_id not in self.session_memories:
            self.session_memories[session_id] = ConversationSummaryMemory(
                llm=self.llm,
                max_token_limit=80,
                memory_key="chat_history",
                return_messages=True,
            )
            logger.info(f"🧠 새 메모리 생성: {session_id}")
        
        return self.session_memories[session_id]
    
    async def generate_response(
        self, 
        user_message: str, 
        session_id: str = None,
        conversation_history: List[ChatMessage] = None
    ) -> Tuple[str, str]:
        """RAG 기반 응답 생성"""
        
        # ✅ 자동 초기화 확인
        if not self.is_initialized:
            await self.initialize()
        
        try:
            if not session_id:
                session_id = str(uuid.uuid4())
            
            memory = self._get_or_create_memory(session_id)
            
            logger.info(f"🤖 RAG 질문 처리 시작: [{session_id}] {user_message[:50]}...")
            
            # 저장된 프롬프트 가져오기
            system_prompt = self.get_system_prompt()
            
            # 프롬프트와 질문 결합
            enhanced_query = f"{system_prompt}\n\n사용자 질문: {user_message}"
            
            # RetrievalQA 체인으로 답변 생성
            result = self.chain.invoke({"query": enhanced_query})
            ai_response = result["result"]
            
            # 메모리에 대화 저장
            memory.save_context(
                {"input": user_message},
                {"output": ai_response}
            )
            
            logger.info(f"✅ RAG 응답 생성 완료: [{session_id}]")
            
            return ai_response, session_id
            
        except Exception as e:
            logger.error(f"❌ RAG 응답 생성 실패: {str(e)}")
            raise Exception(f"질답 처리 중 오류가 발생했습니다: {str(e)}")

openai_service = OpenAIService()