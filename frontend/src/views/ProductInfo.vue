<template>
  <div>
    <main class="product-detail-container">
      <!-- 로딩 상태 -->
      <div v-if="loading" class="loading-container">
        <div class="loading-spinner"></div>
      </div>
      
      <!-- 에러 상태 -->
      <div v-else-if="error" class="error-container">
        <h2>{{ error }}</h2>
        <button @click="goBack" class="back-btn">이전 페이지로</button>
      </div>
      
      <!-- 에러 상태 -->
      <div v-else-if="error" class="error-container">
        <h2>{{ error }}</h2>
        <button @click="goBack" class="back-btn">이전 페이지로</button>
      </div>
      
      <!-- 상품 정보가 로드된 후 -->
      <div v-else-if="product.id">
        <!-- 상품 기본 정보 섹션 -->
        <ProductInfo 
          :product="product" 
          :productType="productType"
          :approvedUnpackedDetails="unpackedItems"
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

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import ProductInfo from '@/components/ProductInfo.vue'
import PriceChart from '@/components/PriceChart.vue'
import UsedProductsSection from '@/components/UsedProductsSection.vue'
import UsedItemDetailModal from '@/components/UsedItemDetailModal.vue'
import RecommendedProducts from '@/components/RecommendedProducts.vue'
import RelatedProducts from '@/components/RelatedProducts.vue'
import { fetchItemById } from '@/api/items'
import { fetchApprovedUnpackedItemDetails, fetchApprovedOpenedItemDetails } from '@/api/items'
import type { ItemDTO } from '@/types/item'

const route = useRoute()
const productId = String(route.params.id)

// 반응형 데이터
const loading = ref(true)
const error = ref<string | null>(null)
const product = ref<any>({})
const priceData = ref<any[]>([])
const usedItems = ref<any[]>([])
const unpackedItems = ref<any[]>([]) // 미개봉 상품용
const recommendedProducts = ref<any[]>([])
const relatedProducts = ref<any[]>([])
const showUsedItemDetail = ref(false)
const selectedUsedItem = ref<any>(null)

// 상품 타입 계산 (URL 파라미터나 상품 데이터 기반)
const productType = computed(() => {
  return product.value.type || 'new' // 기본은 'new' (미개봉)
})

// ItemDTO를 화면용 product 객체로 변환
function adaptProduct(dto: ItemDTO) {
  const base = import.meta.env.VITE_ASSET_BASE
  const codeImg = dto.itemCode ? `${base}${dto.itemCode}.jpg` : undefined
  const images = Array.isArray(dto.images) && dto.images.length
    ? dto.images
    : (codeImg ? [codeImg] : ['/img/placeholder.jpg'])
  console.log(images)
  return {
    id: dto.itemCode,
    name: dto.name,
    title: dto.title,
    brand: dto.manufacturer || '브랜드 미정',
    originalPrice: dto.cost ? `${dto.cost.toLocaleString()}원` : '발매가 미정',
    currentPrice: `${(dto.cost ?? 0).toLocaleString()}원`,
    type: 'new', // 기본은 미개봉 상품
    images,
    description: dto.information || `${dto.name || dto.title} 상품입니다.`,
    // 추가 필드들
    manufacturer: dto.manufacturer,
    material: dto.material,
    releaseMonth: dto.releaseMonth,
    size: dto.size,
    genre: dto.genre,
    storageFees: dto.storageFees,
    cost: dto.cost
  }
}

// API 호출 함수들
const fetchProductDetail = async () => {
  try {
    const data: ItemDTO = await fetchItemById(productId)
    product.value = adaptProduct(data)
  } catch (e: any) {
    console.error('상품 정보 로드 실패:', e)
    error.value = e?.message ?? '상품 정보를 불러오지 못했습니다.'
  }
}

const fetchPriceData = async () => {
  if (productType.value !== 'new') return
  
  try {
    // TODO: 실제 가격 차트 API 연동
    // const response = await fetch(`/api/products/${productId}/price-chart`)
    // priceData.value = await response.json()
    
    // 임시 빈 데이터
    priceData.value = []
  } catch (error) {
    console.error('가격 차트 데이터 로드 실패:', error)
  }
}

const fetchUsedItems = async () => {
  try {
    // 승인된 개봉 상품의 ItemDetails 조회 (unpacked = 1)
    const approvedOpenedDetails = await fetchApprovedOpenedItemDetails(productId)
    console.log('승인된 개봉 상품 데이터:', approvedOpenedDetails)
    
    // 백엔드에서 받은 데이터를 프론트엔드 형식으로 변환
    if (approvedOpenedDetails && approvedOpenedDetails.length > 0) {
      usedItems.value = approvedOpenedDetails.map((detail: any) => {
        const mappedItem = {
          id: detail.itemId,
          title: detail.itemName || '상품명 미정',
          description: detail.productCondition || '상품 설명 없음',
          price: detail.cost || 0,
          originalPrice: detail.cost || 0,
          condition: getConditionFromQuality(detail.quality), // Admin의 quality로 등급 결정
          createdAt: detail.registrationDate ? new Date(detail.registrationDate) : new Date(),
          images: detail.itemImgUrl ? [detail.itemImgUrl] : ['/img/placeholder.jpg'],
          conditionDetails: detail.productCondition || '상품 상태 상세 정보 없음',
          // 추가 정보
          quality: detail.quality,
          registrationDate: detail.registrationDate,
          itemExplanation: detail.itemExplanation
        }
        console.log('매핑된 중고상품 아이템:', mappedItem) // 디버깅용
        return mappedItem
      })
      console.log('최종 usedItems:', usedItems.value) // 디버깅용
    } else {
      // 데이터가 없으면 빈 배열
      usedItems.value = []
      console.log('승인된 개봉 상품이 없습니다.') // 디버깅용
    }
  } catch (error) {
    console.error('중고상품 목록 로드 실패:', error)
    usedItems.value = []
  }
}

const fetchUnpackedItems = async () => {
  try {
    // 승인된 미개봉 상품의 ItemDetails 조회 (unpacked = 0)
    const approvedUnpackedDetails = await fetchApprovedUnpackedItemDetails(productId)
    console.log('승인된 미개봉 상품 데이터:', approvedUnpackedDetails)
    
    // 백엔드에서 받은 데이터를 프론트엔드 형식으로 변환
    if (approvedUnpackedDetails && approvedUnpackedDetails.length > 0) {
      unpackedItems.value = approvedUnpackedDetails.map((detail: any) => ({
        itemId: detail.itemId,
        cost: detail.cost || 0,
        status: detail.status,
        unpacked: detail.unpacked,
        productCondition: detail.productCondition,
        itemName: detail.itemName,
        itemImgUrl: detail.itemImgUrl
      }))
    } else {
      // 데이터가 없으면 빈 배열
      unpackedItems.value = []
    }
  } catch (error) {
    console.error('미개봉 상품 목록 로드 실패:', error)
    unpackedItems.value = []
  }
}

// Admin의 quality 값으로 등급 결정
const getConditionFromQuality = (quality: number) => {
  switch (quality) {
    case 1: return 'excellent' // S등급
    case 2: return 'good'      // A등급
    case 3: return 'fair'      // B등급
    case 4: return 'poor'      // C등급
    default: return 'fair'
  }
}


// 이벤트 핸들러들
const handlePurchase = async (productData: any) => {
  try {
    // 미개봉 상품 즉시 구매 API 호출
    console.log('구매 처리:', productData)
    // const response = await fetch('/api/orders/immediate-purchase', {...})
  } catch (error) {
    console.error('구매 처리 실패:', error)
    alert('구매 처리 중 오류가 발생했습니다.')
  }
}

const handleAddToCart = async (productData: any) => {
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
const handleUsedItemDetailDirect = (item: any) => {
  selectedUsedItem.value = item
  showUsedItemDetail.value = true
}

const handleUsedItemPurchase = async (usedItem: any) => {
  try {
    // 중고상품 구매 API 호출
    console.log('중고상품 구매:', usedItem)
    // const response = await fetch('/api/orders/immediate-purchase', {...})
  } catch (error) {
    console.error('중고상품 구매 처리 실패:', error)
    alert('구매 처리 중 오류가 발생했습니다.')
  }
}

const handleUsedItemAddToCart = async (usedItem: any) => {
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
  error.value = null // 에러 상태 초기화
  
  try {
    // 병렬로 데이터 로드
    await Promise.all([
      fetchProductDetail(),
      fetchPriceData(),
      fetchUsedItems(),
      fetchUnpackedItems(), // 미개봉 상품 데이터도 로드
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