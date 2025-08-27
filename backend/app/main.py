import os
from pydantic import BaseModel
from langchain_openai import ChatOpenAI, OpenAIEmbeddings
from langchain_community.document_loaders import PyPDFLoader
from langchain.text_splitter import RecursiveCharacterTextSplitter
from langchain_community.vectorstores import FAISS
from langchain.prompts import ChatPromptTemplate
from langchain_core.output_parsers import StrOutputParser
from langchain_core.runnables import RunnablePassthrough
from dotenv import load_dotenv

# .env 파일에 저장된 환경변수 불러오기 (OPENAI_API_KEY 등)
load_dotenv() 

# === 요청/응답 데이터 모델 (FastAPI와 연결됨) ===
class ChatRequest(BaseModel):
    query: str             # 사용자가 입력한 질문
    user_id: str = "default"  # 사용자 ID (옵션)

class ChatResponse(BaseModel):
    answer: str            # AI가 반환할 답변


# === RAG 서비스 클래스 ===
class RAGService:
    def __init__(self, pdf_path: str = "./data/mbti.pdf"):
        # OpenAI API 키 불러오기
        self.openai_api_key = os.getenv("OPENAI_API_KEY")
        
        # 1. LLM 모델 초기화 (GPT-4o 사용)
        self.llm = ChatOpenAI(
            model="gpt-4o",
            api_key=self.openai_api_key,
            temperature=0.2,   # 낮을수록 일관된 답변
        )
        
        # 2. PDF 문서 로드
        loader = PyPDFLoader(pdf_path)
        documents = loader.load()

        # 3. 텍스트 분할 (chunk 단위로 나눔)
        text_splitter = RecursiveCharacterTextSplitter(
            chunk_size=300,       # 한 조각의 크기
            chunk_overlap=30,     # 겹치는 길이
            separators=["\n\n", "\n", ".", " ", ""]
        )
        texts = text_splitter.split_documents(documents)
        
        # 4. 임베딩 모델 초기화 (텍스트를 벡터로 변환)
        embeddings = OpenAIEmbeddings(
            api_key=self.openai_api_key,
            model="text-embedding-3-small"
        )
        
        # 5. FAISS 벡터스토어 생성 (문서 검색 인덱스)
        self.vectorstore = FAISS.from_documents(
            documents=texts[:10],   # 문서 일부만 인덱싱 (테스트용)
            embedding=embeddings
        )
        self.retriever = self.vectorstore.as_retriever(
            search_kwargs={"k": 2} # 검색 시 가져올 문서 수
        )
        
        # 6. 프롬프트 템플릿 정의 (AI에게 줄 지침)
        self.prompt = ChatPromptTemplate.from_messages([
            ("system", "너는 챗봇이야. 이름은 다람이야. 다음 문서를 참고해서 한국어로 답변하세요.\n문서: {context}"),
            ("human", "{query}")
        ])
        # ↑ system = AI에게 기본 규칙 전달
        # ↑ human  = 사용자의 실제 질문 자리

        # 검색된 문서들을 문자열로 변환하는 함수
        def format_docs(docs):
            return "\n\n".join(doc.page_content for doc in docs)
        
        # 7. 체인 구성 (검색 → 프롬프트 → LLM → 결과 파싱)
        self.chain = (
            {"context": self.retriever | format_docs, "query": RunnablePassthrough()}
            | self.prompt
            | self.llm
            | StrOutputParser()
        )
        
    # === 외부에서 호출하는 메서드 ===
    def get_answer(self, query: str, user_id: str = "default") -> str:
        # 사용자의 질문(query)을 받아서 체인 실행 → 답변 반환
        return self.chain.invoke(query)


# === 전역 서비스 객체 ===
rag_service = RAGService()
