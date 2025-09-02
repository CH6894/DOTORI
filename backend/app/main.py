import os
from langchain_openai import ChatOpenAI, OpenAIEmbeddings
from langchain_community.document_loaders import PyPDFLoader
from langchain.text_splitter import RecursiveCharacterTextSplitter
from langchain_community.vectorstores import FAISS
from langchain.prompts import ChatPromptTemplate
from langchain_core.output_parsers import StrOutputParser
from dotenv import load_dotenv
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel

load_dotenv()


class ChatbotService:
    def __init__(self, pdf_path: str = "./data/상품목록.pdf"):
        """다람이 챗봇 서비스 초기화"""
        self.openai_api_key = os.getenv("OPENAI_API_KEY")
        
        # LLM 초기화
        self.llm = ChatOpenAI(
            model="gpt-4o",
            api_key=self.openai_api_key,
            temperature=0.3,
            timeout=30,
            max_retries=1,
        )
        
        # PDF 텍스트 로드 (주요 검색 방식)
        self._load_pdf_text(pdf_path)
        
        # 벡터스토어는 보조용으로만 설정
        self._setup_vectorstore(pdf_path)
        
        # 프롬프트 템플릿 설정
        self._setup_prompts()

    def _setup_vectorstore(self, pdf_path: str):
        """벡터스토어 설정"""
        # PDF 문서 로드
        loader = PyPDFLoader(pdf_path)
        documents = loader.load()
        
        # 텍스트 분할 - 별칭 인식을 위해 작은 청크 사용
        text_splitter = RecursiveCharacterTextSplitter(
            chunk_size=500,
            chunk_overlap=50,
            separators=["\n제품명", "\n별칭", "\n---", "\n", " "]
        )
        texts = text_splitter.split_documents(documents)
        
        # 임베딩 및 벡터스토어 생성
        embeddings = OpenAIEmbeddings(
            api_key=self.openai_api_key,
            model="text-embedding-3-small"
        )
        
        self.vectorstore = FAISS.from_documents(documents=texts, embedding=embeddings)
        self.retriever = self.vectorstore.as_retriever(
            search_type="similarity",
            search_kwargs={"k": 10}
        )

    def _setup_prompts(self):
        """프롬프트 템플릿 설정"""
        
        # 상품 검색용
        self.product_search_prompt = ChatPromptTemplate.from_messages([
            ("system", """다람이 챗봇입니다. 상품 검색 결과에 따라 다르게 답변합니다.

검색 결과 판단:
- 매칭된 상품이 없습니다: "죄송해요, [검색어] 관련 상품을 찾지 못했어요. 다른 상품명으로 검색해보시겠어요?"
- 5개 이하 상품: 앞머리에 [사용자 검색어명] 검색 결과입니다. 하고 찾은 상품들 자연스럽게 나열
- 5개 초과: [사용자 검색어명].. 검색 실패. 더 구체적 검색 유도

매칭된 상품이 없을 때:
"죄송해요, [검색어] 관련 상품을 찾지 못했어요. 다른 상품명으로 검색해보시겠어요?"

5개 초과일 때 답변 형식:
"더 자세히 말씀해주시면 제가 더 잘 찾아 줄 수 있어요!

예시:
- [그룹/작품명] [상품이름] (예: 블핑 응원봉)
- [그룹/작품명] [멤버/캐릭터명] (예: 블핑 제니)
- [그룹/작품명] [의류/키링/굿즈] (예: 블핑 티셔츠)"

상품 답변 형식 (매칭 점수 2점 이상):
각 상품마다 개별 메시지로 분리해서:
"[상품명]
재고는 S등급 O개, A등급 O개, B등급 O개, C등급 O개 있어요!
[여기서 상품 보기](링크주소)"

주의사항:
- 매칭 점수 2점 이상만 수량과 링크 제공
- 1점 제품은 상품명만 간단히
- 각 상품을 개별 말풍선으로 분리
- 마지막에 "다른 상품도 궁금하신가요?" 추가
- 사용자가 고맙다는 등 긍정적인 답변을 하면 "아닙니다! 추가로 궁금하신 점이 있다면 언제든 말해주세요!" 라고 해

상품 정보:
{context}"""),
            ("human", "{query}")
        ])
        
        # 검수 기준용
        self.inspection_prompt = ChatPromptTemplate.from_messages([
            ("system", """검수 기준에 대해 친근하게 설명해드립니다.

답변 스타일:
- 친근하고 이해하기 쉽게
- S/A/B/C 등급 차이를 실생활 예시로
- 딱딱한 나열보다는 자연스러운 설명
- 사용자가 물어본 질문의 요약된 정보로 제공하고 사용자가 물어본 것 이상을 말하지 말 것 
- 말의 끝에 사용자가 더 물어볼 수 있도록 유도해
- 사용자가 고맙다던지 답변에 만족하는 긍정적인 말을 하면 "감사합니다! 더 자세한 내용은 검수 내역란에서 확인하실 수 있습니다." 라고 해

검수 정보:
{context}"""),
            ("human", "{query}")
        ])
        
        # FAQ용
        self.faq_prompt = ChatPromptTemplate.from_messages([
            ("system", """자주 묻는 질문에 대해 도움이 되는 답변을 드립니다.

답변 스타일:
- 구체적 질문: 바로 답변
- 모호한 질문: "어떤 부분이 궁금하신가요?"로 되묻기
- 친근하고 도움이 되는 톤
- A의 답만 말하기로 해

FAQ 정보:
{context}"""),
            ("human", "{query}")
        ])

    def _classify_query(self, query: str) -> str:
        """쿼리 타입 분류"""
        query_lower = query.lower()
        
        # 검수 관련
        if any(word in query_lower for word in ['검수', '등급', 's급', 'a급', 'b급', 'c급']):
            return "inspection"
        
        # FAQ 관련
        if any(word in query_lower for word in ['faq', '자주', '질문', '할인', '안전']):
            return "faq"
        
        # 기본은 상품 검색
        return "product_search"

    def _load_pdf_text(self, pdf_path: str):
        """PDF 전체 텍스트를 메모리에 로드"""
        loader = PyPDFLoader(pdf_path)
        documents = loader.load()
        
        # 전체 텍스트 저장
        self.full_text = "\n".join([doc.page_content for doc in documents])
        
        # 제품별로 분리해서 저장
        self.products = []
        current_product = ""
        
        for line in self.full_text.split('\n'):
            line = line.strip()
            if line.startswith('제품명'):
                if current_product:
                    self.products.append(current_product)
                current_product = line + '\n'
            elif line.startswith('별칭'):
                current_product += line + '\n'
            elif current_product and line:
                current_product += line + '\n'
        
        if current_product:
            self.products.append(current_product)

    def _search_products(self, query: str) -> dict:
        """직접 텍스트 매칭으로 상품 검색"""
        query_words = query.lower().split()
        
        # 너무 일반적인 키워드 필터링
        filtered_words = []
        ignore_words = ['피규어', '굿즈', '상품', '있어', '있나', '있어?', '있나?', '물건']
        
        for word in query_words:
            if word not in ignore_words:
                filtered_words.append(word)
        
        if not filtered_words:
            return {"answer": "더 구체적인 상품명이나 캐릭터 이름을 알려주시면 찾아드릴게요!"}
        
        matched_products = []
        
        # 각 제품에서 별칭과 제품명 검색
        for product in self.products:
            product_lower = product.lower()
            
            # 전체 상품 텍스트에서 키워드 매칭 점수 계산
            match_count = 0
            for word in filtered_words:
                if word in product_lower:
                    match_count += 1
            
            # 매칭된 키워드가 있으면 추가
            if match_count > 0:
                matched_products.append((product, match_count))
        
        # 매칭 점수순으로 정렬 (높은 점수부터)
        matched_products.sort(key=lambda x: x[1], reverse=True)
        
        # 2점 이상 제품과 1점 제품 분리
        high_score_products = [(product, score) for product, score in matched_products if score >= 2]
        low_score_products = [(product, score) for product, score in matched_products if score == 1]
        
        # 2점 이상 제품이 있으면 수량과 링크 포함해서 보여줌
        if high_score_products:
            final_products = [product for product, score in high_score_products[:8]]
            context = "\n\n".join(final_products)
            context += f"\n\n[지시: 검색 결과가 {len(high_score_products)}개입니다. 찾은 상품들을 자연스럽게 소개하고, 각 상품의 수량과 링크 정보도 함께 제공하세요.]"
        
        # 2점 이상이 없고 1점만 5개 이상이면 유도
        elif len(low_score_products) >= 5:
            context = "\n\n".join([product for product, score in low_score_products[:8]])
            context += f"\n\n[지시: 검색 결과가 {len(low_score_products)}개로 많습니다. 사용자에게 더 구체적인 검색을 요청하세요.]"
        
        # 1점 제품이 5개 미만이면 보여줌
        elif low_score_products:
            final_products = [product for product, score in low_score_products]
            context = "\n\n".join(final_products)
            context += f"\n\n[지시: 검색 결과가 {len(low_score_products)}개입니다. 찾은 상품들을 자연스럽게 소개하세요.]"
        
        # 매칭된 제품이 없음
        else:
            return {"answer": "아직 해당 상품이 존재하지 않아요! 다른 키워드로 검색해보시겠어요?"}
        
        # LLM으로 답변 생성
        chain = self.product_search_prompt | self.llm | StrOutputParser()
        answer = chain.invoke({"context": context, "query": query})
        return {"answer": answer}

    def _search_inspection(self, query: str) -> dict:
        """검수 기준 처리 - 벡터 검색 사용"""
        docs = self.retriever.invoke(f"검수 기준 {query}")
        context = "\n\n".join([doc.page_content for doc in docs[:3]])
        
        chain = self.inspection_prompt | self.llm | StrOutputParser()
        answer = chain.invoke({"context": context, "query": query})
        return {"answer": answer}

    def _search_faq(self, query: str) -> dict:
        """FAQ 처리 - 벡터 검색 사용"""
        docs = self.retriever.invoke(f"FAQ {query}")
        context = "\n\n".join([doc.page_content for doc in docs[:3]])
        
        chain = self.faq_prompt | self.llm | StrOutputParser()
        answer = chain.invoke({"context": context, "query": query})
        return {"answer": answer}

    def get_answer(self, query: str, user_id: str = "default") -> dict:
        """메인 답변 생성 함수"""
        # 쿼리 타입 분류
        query_type = self._classify_query(query)
        
        # 타입별 처리
        if query_type == "inspection":
            return self._search_inspection(query)
        elif query_type == "faq":
            return self._search_faq(query)
        else:  # product_search
            return self._search_products(query)


# 전역 서비스 인스턴스
chatbot_service = ChatbotService()


class ChatRequest(BaseModel):
    query: str
    user_id: str = "default"


class ChatResponse(BaseModel):
    answer: str
    is_multiple: bool = False
    messages: list[str] = []


app = FastAPI(title="다람이 챗봇", version="2.0.0")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:5173", "http://127.0.0.1:5173"],
    allow_credentials=True,
    allow_methods=["GET", "POST", "PUT", "DELETE", "OPTIONS"],
    allow_headers=["*"],
)

# 라우터 등록
from routers.chatbot import router
app.include_router(router)

@app.get("/")
def root():
    return {"message": "다람이 챗봇 서버 실행 중"}