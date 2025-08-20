<template>
  <div>
    <Header />
    <main class="product-detail-container">
      <!-- 로딩 상태 -->
      <div v-if="loading" class="loading-container">
        <div class="loading-spinner">로딩 중...</div>
      </div>
      
      <!-- 상품 정보가 로드된 후 -->
      <div v-else-if="product.id">
        <!-- 상품 기본 정보 섹션 -->
        <ProductInfo 
          :product="product" 
          :productType="productType"
          @purchase="handlePurchase"
          @addToCart="handleAddToCart"
        />
        
        <!-- 가격 차트 섹션 (미개봉 상품만) -->
        <PriceChart 
          v-if="productType === 'new'" 
          :priceData="priceData" 
          :productId="product.id"
        />
        
        <!-- 중고상품 섹션 (카드 형태로 표시) -->
        <UsedProductsSection 
          :usedItems="usedItems"
          :productInfo="product"
          @openUsedItemDetail="handleUsedItemDetailDirect"
        />
        
        <!-- 개별 중고상품 상세 모달 -->
        <UsedItemDetailModal
          v-if="showUsedItemDetail"
          :item="selectedUsedItem"
          :productInfo="product"
          @close="showUsedItemDetail = false"
          @purchase="handleUsedItemPurchase"
          @addToCart="handleUsedItemAddToCart"
        />
        
        <!-- 추천상품 섹션 -->
        <RecommendedProducts :products="recommendedProducts" />
        
        <!-- 함께 본 상품 섹션 -->
        <RelatedProducts :products="relatedProducts" />
      </div>
      
      <!-- 상품을 찾을 수 없는 경우 -->
      <div v-else class="error-container">
        <h2>상품을 찾을 수 없습니다.</h2>
        <button @click="goBack" class="back-btn">이전 페이지로</button>
      </div>
    </main>
    <Footer />
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import Header from '@/components/AppHeader.vue'
import Footer from '@/components/AppFooter.vue'
import ProductInfo from '@/components/ProductInfo.vue'
import PriceChart from '@/components/PriceChart.vue'
import UsedProductsSection from '@/components/UsedProductsSection.vue'
import UsedItemDetailModal from '@/components/UsedItemDetailModal.vue'
import RecommendedProducts from '@/components/RecommendedProducts.vue'
import RelatedProducts from '@/components/RelatedProducts.vue'

const route = useRoute()
const productId = route.params.id

// 반응형 데이터
const loading = ref(true)
const product = ref({})
const priceData = ref([])
const usedItems = ref([])
const recommendedProducts = ref([])
const relatedProducts = ref([])
const showUsedItemDetail = ref(false)
const selectedUsedItem = ref(null)

// 상품 타입 계산 (URL 파라미터나 상품 데이터 기반)
const productType = computed(() => {
  return product.value.type || 'new' // 기본은 'new' (미개봉)
})

// API 호출 함수들 (DB 연동 준비)
const fetchProductDetail = async () => {
  try {
    // const response = await fetch(`/api/products/${productId}`)
    // const data = await response.json()
    
    // 임시 데이터 (DB 연동 시 삭제)
    product.value = {
      id: productId,
      title: '세가 (SEGA) 귀멸의 칼날 Xross Link 피규어 렌고쿠 쿄쥬로 이카자',
      brand: '세가',
      originalPrice: '발매가 미정',
      currentPrice: '20,000원',
      type: 'new', // 기본은 미개봉 상품
      images: [
        '/img/예시1.jpg',
         '/img/예시1.jpg',
         '/img/예시1.jpg',
      ],
      description: '세가에서 출시한 귀멸의 칼날 피규어입니다.'
    }
  } catch (error) {
    console.error('상품 정보 로드 실패:', error)
  }
}

const fetchPriceData = async () => {
  if (productType.value !== 'new') return
  
  try {
    // const response = await fetch(`/api/products/${productId}/price-chart`)
    // const data = await response.json()
    
    // 임시 데이터 (DB 연동 시 삭제)
    priceData.value = [
      { date: '24/07/28', price: 20000 },
      { date: '24/08/04', price: 22000 },
      { date: '24/08/11', price: 25000 },
      { date: '24/08/18', price: 20000 },
    ]
  } catch (error) {
    console.error('가격 차트 데이터 로드 실패:', error)
  }
}

const fetchUsedItems = async () => {
  try {
    // const response = await fetch(`/api/products/${productId}/used-items`)
    // const data = await response.json()
    
    // 임시 데이터 (DB 연동 시 삭제) - 각각 고유한 개별 중고상품들
    usedItems.value = [
      {
        id: 1,
        title: '세가 귀멸의 칼날 피규어',
        description: 'dmdkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkk',
        price: 22000,
        originalPrice: 25000,
        condition: 'excellent',
        createdAt: new Date(),
        images: ['/img/예시1.jpg', '/img/test.jpg'],
        conditionDetails: '박스 외관 미세한 눌림, 내용물 완벽'
      },
      {
        id: 2,
        title: '세가 귀멸의 칼날 피규어',
        description: '한번 사용 후 보관된 최상급 상품입니다.',
        price: 24000,
        originalPrice: 25000,
        condition: 'excellent',
        createdAt: new Date(Date.now() - 86400000),
        images: ['/img/used002.jpg'],
        conditionDetails: '흠집 전무'
      },
      {
        id: 3,
        title: '세가 귀멸의 칼날 피규어',
        description: '박스 개봉했으나 피규어 자체는 새상품 수준입니다.',
        price: 20000,
        originalPrice: 25000,
        condition: 'poor',
        createdAt: new Date(Date.now() - 172800000),
        images: ['/img/used003.jpg'],
        conditionDetails: '더러움 아주 더러움 세상에서 이것보다 더 더러운 것은 없을거임'
      },
      {
        id: 4,
        title: '세가 귀멸의 칼날 피규어',
        description: '약간의 사용감이 있지만 전체적으로 양호한 상태입니다.',
        price: 18000,
        originalPrice: 25000,
        condition: 'good',
        createdAt: new Date(Date.now() - 259200000),
        images: ['/img/used004.jpg'],
        conditionDetails: '미세한 스크래치, 기능상 문제없음'
      },
      {
        id: 5,
        title: '세가 귀멸의 칼날 피규어',
        description: '일반적인 사용감이 있는 중고상품입니다.',
        price: 15000,
        originalPrice: 25000,
        condition: 'fair',
        createdAt: new Date(Date.now() - 345600000),
        images: ['/img/used005.jpg'],
        conditionDetails: '사용감 있으나 정상 작동'
      },
      {
        id: 6,
        title: '세가 귀멸의 칼날 피규어',
        description: '약간의 기스와 사용감이 있지만 저렴한 가격입니다.',
        price: 17000,
        originalPrice: 25000,
        condition: 'good',
        createdAt: new Date(Date.now() - 432000000),
        images: ['/img/used006.jpg'],
        conditionDetails: '약간의 기스, 전체적으로 양호'
      }
    ]
  } catch (error) {
    console.error('중고상품 목록 로드 실패:', error)
  }
}

const fetchRecommendedProducts = async () => {
  try {
    // const response = await fetch(`/api/products/${productId}/recommended`)
    // const data = await response.json()
    
    // 임시 데이터 (DB 연동 시 삭제)
    recommendedProducts.value = [
      { id: 1, title: '추천상품1', image: '/img/rec1.jpg', price: '15,000원' },
      { id: 2, title: '추천상품2', image: '/img/rec2.jpg', price: '18,000원' },
      { id: 3, title: '추천상품3', image: '/img/rec3.jpg', price: '22,000원' },
      { id: 4, title: '추천상품4', image: '/img/rec4.jpg', price: '25,000원' },
      { id: 4, title: '추천상품5', image: '/img/rec4.jpg', price: '25,000원' },
     { id: 4, title: '추천상품', image: '/img/rec4.jpg', price: '25,000원' },
    ]
  } catch (error) {
    console.error('추천 상품 로드 실패:', error)
  }
}

const fetchRelatedProducts = async () => {
  try {
    // const response = await fetch(`/api/products/${productId}/related`)
    // const data = await response.json()
    
    // 임시 데이터 (DB 연동 시 삭제)
    relatedProducts.value = [
      { id: 1, title: '함께본상품1', image: '/img/test.jpg', price: '20,000원' },
      { id: 2, title: '함께본상품2', image: '/img/rel2.jpg', price: '25,000원' },
      { id: 3, title: '함께본상품3', image: '/img/rel3.jpg', price: '18,000원' },
      { id: 4, title: '함께본상품4', image: '/img/rel4.jpg', price: '35,000원' },
      { id: 5, title: '함께본상품1', image: '/img/test.jpg', price: '20,000원' },
      { id: 6, title: '함께본상품1', image: '/img/test.jpg', price: '20,000원' },
    ]
  } catch (error) {
    console.error('관련 상품 로드 실패:', error)
  }
}

// 이벤트 핸들러들
const handlePurchase = async (productData) => {
  try {
    // 미개봉 상품 즉시 구매 API 호출
    console.log('구매 처리:', productData)
    // const response = await fetch('/api/orders/immediate-purchase', {...})
  } catch (error) {
    console.error('구매 처리 실패:', error)
    alert('구매 처리 중 오류가 발생했습니다.')
  }
}

const handleAddToCart = async (productData) => {
  try {
    // 장바구니 추가 API 호출
    console.log('장바구니 추가:', productData)
    // const response = await fetch('/api/cart/add', {...})
    alert('장바구니에 추가되었습니다.')
  } catch (error) {
    console.error('장바구니 추가 실패:', error)
    alert('장바구니 추가 중 오류가 발생했습니다.')
  }
}

// 중고상품 관련 핸들러들
const handleUsedItemDetailDirect = (item) => {
  selectedUsedItem.value = item
  showUsedItemDetail.value = true
}

const handleUsedItemPurchase = async (usedItem) => {
  try {
    // 중고상품 구매 API 호출
    console.log('중고상품 구매:', usedItem)
    // const response = await fetch('/api/orders/immediate-purchase', {...})
  } catch (error) {
    console.error('중고상품 구매 처리 실패:', error)
    alert('구매 처리 중 오류가 발생했습니다.')
  }
}

const handleUsedItemAddToCart = async (usedItem) => {
  try {
    // 중고상품 장바구니 추가 API 호출
    console.log('중고상품 장바구니 추가:', usedItem)
    // const response = await fetch('/api/cart/add', {...})
    alert('장바구니에 추가되었습니다.')
  } catch (error) {
    console.error('장바구니 추가 실패:', error)
    alert('장바구니 추가 중 오류가 발생했습니다.')
  }
}

const goBack = () => {
  // 이전 페이지로 이동
  window.history.back()
}

// 페이지 초기화
const initializePage = async () => {
  loading.value = true
  
  try {
    // 병렬로 데이터 로드
    await Promise.all([
      fetchProductDetail(),
      fetchPriceData(),
      fetchUsedItems(),
      fetchRecommendedProducts(),
      fetchRelatedProducts()
    ])
  } catch (error) {
    console.error('페이지 초기화 실패:', error)
  } finally {
    loading.value = false
  }
}

// 컴포넌트 마운트 시 데이터 로드
onMounted(() => {
  initializePage()
})
</script>

<style scoped>
@import url('https://cdn.jsdelivr.net/gh/orioncactus/pretendard/dist/web/static/pretendard.css');

* {
  font-family: "Pretendard", -apple-system, BlinkMacSystemFont, system-ui, sans-serif;
}
.product-detail-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  min-height: 70vh;
}

.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 50vh;
}

.loading-spinner {
  font-size: 18px;
  color: #666;
  padding: 20px;
  border: 2px solid #f3f3f3;
  border-top: 2px solid #ff6b35;
  border-radius: 50%;
  width: 40px;
  height: 40px;
  animation: spin 1s linear infinite;
  margin: 0 auto;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.error-container {
  text-align: center;
  padding: 60px 20px;
}

.error-container h2 {
  color: #666;
  margin-bottom: 30px;
  font-size: 24px;
}

.back-btn {
  padding: 12px 24px;
  background: #ff6b35;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 16px;
  font-weight: 600;
  transition: background-color 0.2s;
}

.back-btn:hover {
  background: #e55a2b;
}

</style>