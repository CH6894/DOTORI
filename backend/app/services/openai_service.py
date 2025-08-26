from typing import List, Tuple
import uuid
import logging
from langchain_openai import ChatOpenAI, OpenAIEmbeddings
from langchain_community.document_loaders import PyPDFLoader
from langchain.memory import ConversationSummaryMemory
from langchain.text_splitter import RecursiveCharacterTextSplitter
from langchain_community.vectorstores import FAISS
from langchain.chains import RetrievalQA
from langchain.prompts import PromptTemplate

from app.config.settings import settings
from app.models.chat import ChatMessage, MessageRole

logger = logging.getLogger(__name__)

class OpenAIService:
    def __init__(self):
        """
        RAG 서비스 초기화
        """
        try:
          
            self.llm = ChatOpenAI(
                model="gpt-3.5-turbo",
                openai_api_key=settings.OPENAI_API_KEY,
                temperature=0.2,
            )
            
            # 임베딩 모델 초기화 (pdf를 보기 위해서 꼭 필요한 코드)
            # 문자를 숫자로(백터화) 돌리는 작업 (=임베딩)
            self.embeddings = OpenAIEmbeddings(
                openai_api_key=settings.OPENAI_API_KEY
            )
            
            # 세션별 메모리 저장소
            self.session_memories = {}
            
            # PDF 문서 로딩 및 벡터스토어 생성
            self._setup_document_retrieval()
            
            logger.info("✅ RAG 서비스 초기화 완료")
            
        except Exception as e:
            logger.error(f"❌ RAG 서비스 초기화 실패: {e}")
            raise
    
    def _setup_document_retrieval(self):
        """문서 로딩 및 벡터스토어 설정"""
        try:
            # PDF 경로
            pdf_path = "C:\\Users\\smhrd\\Documents\\figure_dummy_200.pdf"
            
            # PDF 로딩
            logger.info(f"📄 PDF 문서 로딩 중: {pdf_path}")
            loader = PyPDFLoader(pdf_path)
            documents = loader.load()
            
            # 텍스트 분할
            text_splitter = RecursiveCharacterTextSplitter(
                chunk_size=1000,
                chunk_overlap=50,
                separators=["\n\n", "\n", ".", " ", ""]
            )
            texts = text_splitter.split_documents(documents)
            
            # 벡터스토어 생성
            logger.info("🔍 벡터스토어 생성 중...")
            self.vectorstore = FAISS.from_documents(
                documents=texts,
                embedding=self.embeddings
            )
            self.retriever = self.vectorstore.as_retriever(search_kwargs={"k": 5})
            
            # 피규어 전문 프롬프트 템플릿 생성
            prompt_template = """당신은 피규어 전문 쇼핑몰의 AI 어시스턴트입니다. 
사용자의 질문에 대해 제공된 피규어 데이터베이스 정보를 바탕으로 정확하고 도움이 되는 답변을 해주세요.

피규어 데이터베이스 정보:
{context}

사용자 질문: {question}

답변 지침:
- 피규어 관련 질문이면 데이터베이스 정보를 참조해서 구체적으로 답변해주세요
- 가격, 재고, 희귀도, 상태 등의 정보를 포함해주세요  
- 일반적인 대화도 피규어 쇼핑몰 어시스턴트로서 친근하게 답변해주세요
- 데이터에 없는 정보는 "죄송하지만 해당 정보는 현재 데이터베이스에 없습니다"라고 안내해주세요

답변:"""

            PROMPT = PromptTemplate(
                template=prompt_template, 
                input_variables=["context", "question"]
            )
            
            # RetrievalQA 체인 생성 (모든 질문에 대해 RAG 사용)
            self.chain = RetrievalQA.from_chain_type(
                llm=self.llm,
                chain_type="stuff",
                retriever=self.retriever,
                return_source_documents=True,
                chain_type_kwargs={"prompt": PROMPT}
            )
            
            logger.info(f"✅ 문서 처리 완료: {len(texts)}개 청크 생성")
            
        except Exception as e:
            logger.error(f"❌ 문서 처리 실패: {e}")
            raise
    
    def _get_or_create_memory(self, session_id: str):
        """세션별 메모리 관리 기억하기"""
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
        """RAG 기반 응답 생성 (모든 질문에 대해 PDF 참조)"""
        try:
            # 세션 ID 처리
            if not session_id:
                session_id = str(uuid.uuid4())
            
            # 세션별 메모리 가져오기
            memory = self._get_or_create_memory(session_id)
            
            logger.info(f"🤖 RAG 질문 처리 시작: [{session_id}] {user_message[:50]}...")
            
            # RetrievalQA 체인으로 답변 생성 (항상 PDF 데이터 참조)
            result = self.chain.invoke({"query": user_message})
            ai_response = result["result"]
            
            # 메모리에 대화 저장
            memory.save_context(
                {"input": user_message},
                {"output": ai_response}
            )
            
            logger.info(f"✅ RAG 응답 생성 완료: [{session_id}]")
            logger.info(f"📚 참조된 문서 수: {len(result.get('source_documents', []))}")
            
            return ai_response, session_id
            
        except Exception as e:
            logger.error(f"❌ RAG 응답 생성 실패: {str(e)}")
            raise Exception(f"질답 처리 중 오류가 발생했습니다: {str(e)}")
    
    # generate_general_response 메서드 제거 - 모든 응답이 RAG를 사용하도록 함
    
    async def health_check(self) -> bool:
        """서비스 상태 확인"""
        try:
            self.chain.invoke({"query": "테스트"})
            return True
        except Exception as e:
            logger.error(f"❌ 헬스체크 실패: {e}")
            return False

# 전역 서비스 인스턴스
openai_service = OpenAIService()