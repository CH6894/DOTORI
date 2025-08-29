<template>
  <div>
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
  </div>
</template>

<script setup>
import api from '@/api/axios'         // 로그인 필요한 API (장바구니 담기)
import openApi from '@/api/axiosPublic' // 로그인 불필요 API (상품 조회)
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

// components
import ProductInfo from '@/components/ProductInfo.vue'
import PriceChart from '@/components/PriceChart.vue'
import UsedProductsSection from '@/components/UsedProductsSection.vue'
import UsedItemDetailModal from '@/components/UsedItemDetailModal.vue'
import RecommendedProducts from '@/components/RecommendedProducts.vue'
import RelatedProducts from '@/components/RelatedProducts.vue'

const route = useRoute()
const router = useRouter()
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

  // 상품 상세 조회 (Spring Boot API 연결)
const fetchProductDetail = async () => {
  try {
    const res = await openApi.get(`/items/${productId}`)
    product.value = res.data
  } catch (error) {
    console.error('상품 정보 로드 실패:', error)
  }
}

// 가격 차트 조회
const fetchPriceData = async () => {
  if (productType.value !== 'new') return
  try {
    const res = await openApi.get(`/items/${productId}/price-chart`)
    priceData.value = res.data
  } catch (error) {
    console.error('가격 차트 로드 실패:', error)
  }
}

// 중고상품 리스트 조회
const fetchUsedItems = async () => {
  try {
    const res = await openApi.get(`/items/${productId}/used`)
    usedItems.value = res.data
  } catch (error) {
    console.error('중고상품 목록 로드 실패:', error)
  }
}

// 추천상품
const fetchRecommendedProducts = async () => {
  try {
    const res = await openApi.get(`/items/${productId}/recommended`)
    recommendedProducts.value = res.data
  } catch (error) {
    console.error('추천상품 로드 실패:', error)
  }
}

// 연관상품
const fetchRelatedProducts = async () => {
  try {
    const res = await openApi.get(`/items/${productId}/related`)
    relatedProducts.value = res.data
  } catch (error) {
    console.error('관련상품 로드 실패:', error)
  }
}

// 이벤트

// 미개봉 상품 즉시 구매
const handlePurchase = async () => {
  try {
    const res = await api.post('/orders', {
      cartIds: [product.value.id], // 단일상품 바로 주문
      depositerName: '구매자명',   // TODO: 사용자 입력값과 연결
      payMessage: '빠른 배송 부탁드립니다.'
    })
    router.push({ name: 'OrderComplete', state: { orders: res.data } })
  } catch (error) {
    console.error('구매 실패:', error)
    alert('구매 처리 실패')
  }
}

// 장바구니 담기
const handleAddToCart = async () => {
  try {
    await api.post('/cart', null, { params: { itemDetailsId: product.value.id } })
    alert('장바구니에 담겼습니다!')
  } catch (error) {
    console.error('장바구니 담기 실패:', error)
    alert('장바구니 담기 실패')
  }
}

// 중고상품 상세
const handleUsedItemDetailDirect = (item) => {
  selectedUsedItem.value = item
  showUsedItemDetail.value = true
}

// 중고상품 구매
const handleUsedItemPurchase = async (usedItem) => {
  try {
    const res = await api.post('/orders', {
      cartIds: [usedItem.id],
      depositerName: '구매자명',
      payMessage: '중고상품 구매'
    })
    router.push({ name: 'OrderComplete', state: { orders: res.data } })
  } catch (error) {
    console.error('중고상품 구매 실패:', error)
    alert('구매 처리 실패')
  }
}

// 중고상품 장바구니 추가
const handleUsedItemAddToCart = async (usedItem) => {
  try {
    await api.post('/cart', null, { params: { itemDetailsId: usedItem.id } })
    alert('장바구니에 추가되었습니다.')
  } catch (error) {
    console.error('장바구니 추가 실패:', error)
  }
}

const goBack = () => window.history.back()

// 초기화
const initializePage = async () => {
  loading.value = true
  try {
    await Promise.all([
      fetchProductDetail(),
      fetchPriceData(),
      fetchUsedItems(),
      fetchRecommendedProducts(),
      fetchRelatedProducts()
    ])
  } finally {
    loading.value = false
  }
}

onMounted(initializePage)
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