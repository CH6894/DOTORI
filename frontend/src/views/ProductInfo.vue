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
      
      <!-- 상품 정보가 로드된 후 -->
      <div v-else-if="product.id">
        <!-- 상품 기본 정보 섹션 -->
        <ProductDetailInfo 
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
        
        <!-- 중고상품 섹션 -->
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
        <!-- <RecommendedProducts :products="recommendedProducts" /> -->
        
        <!-- 함께 본 상품 섹션 -->
        <!-- <RelatedProducts :products="relatedProducts" /> -->
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
import { useRoute, useRouter } from 'vue-router'
import ProductDetailInfo from '@/components/ProductInfo.vue'
import PriceChart from '@/components/PriceChart.vue'
import UsedProductsSection from '@/components/UsedProductsSection.vue'
import UsedItemDetailModal from '@/components/UsedItemDetailModal.vue'
import RecommendedProducts from '@/components/RecommendedProducts.vue'
import RelatedProducts from '@/components/RelatedProducts.vue'
import { fetchItemById, fetchApprovedUnpackedItemDetails, fetchApprovedOpenedItemDetails } from '@/api/items'
import type { ItemDTO } from '@/types/item'
import api from '@/api/axios'

const route = useRoute()
const router = useRouter()
const productId = String(route.params.id)

const loading = ref(true)
const error = ref<string | null>(null)
const product = ref<any>({})
const priceData = ref<any[]>([])
const usedItems = ref<any[]>([])
const unpackedItems = ref<any[]>([])
const recommendedProducts = ref<any[]>([])
const relatedProducts = ref<any[]>([])
const showUsedItemDetail = ref(false)
const selectedUsedItem = ref<any>(null)

const productType = computed(() => product.value.type || 'new')

function adaptProduct(dto: ItemDTO) {
  const base = import.meta.env.VITE_ASSET_BASE
  const codeImg = dto.itemCode ? `${base}${dto.itemCode}.jpg` : undefined
  const images = Array.isArray(dto.images) && dto.images.length
    ? dto.images
    : (codeImg ? [codeImg] : ['/img/placeholder.jpg'])
  console.log('adaptProduct - dto:', dto)
  console.log('adaptProduct - images:', images)
  return {
    id: dto.itemCode, // itemCode를 id로 사용 (URL 파라미터와 일치)
    itemCode: dto.itemCode, // itemCode도 별도로 저장
    name: dto.name,
    title: dto.title,
    brand: dto.manufacturer || '브랜드 미정',
    originalPrice: dto.cost || 0,  // 숫자로 전달
    currentPrice: dto.cost || 0,   // 숫자로 전달
    type: 'new', // 기본은 미개봉 상품
    images,
    description: dto.information || `${dto.name || dto.title} 상품입니다.`,
    manufacturer: dto.manufacturer,
    material: dto.material,
    releaseMonth: dto.releaseMonth,
    size: dto.size,
    genre: dto.genre,
    storageFees: dto.storageFees,
    cost: dto.cost
  }
}

const fetchProductDetail = async () => {
  try {
    const data: ItemDTO = await fetchItemById(productId)
    product.value = adaptProduct(data)
    
    // ItemDetails 정보도 가져와서 itemId 설정
    try {
      const unpackedDetails = await fetchApprovedUnpackedItemDetails(productId)
      if (unpackedDetails && unpackedDetails.length > 0) {
        // 첫 번째 미개봉 상품의 itemId를 사용
        product.value.itemId = unpackedDetails[0].itemId
      } else {
        // 미개봉 상품이 없으면 개봉 상품에서 가져오기
        const openedDetails = await fetchApprovedOpenedItemDetails(productId)
        if (openedDetails && openedDetails.length > 0) {
          product.value.itemId = openedDetails[0].itemId
        }
      }
    } catch (e) {
      console.warn('ItemDetails 정보 로드 실패:', e)
    }
  } catch (e: any) {
    error.value = e?.message ?? '상품 정보를 불러오지 못했습니다.'
  }
}

const fetchPriceData = async () => {
  if (productType.value !== 'new') return
  priceData.value = []
}

const fetchUsedItems = async () => {
  try {
    console.log('fetchUsedItems 시작 - productId:', productId)
    // 승인된 개봉 상품의 ItemDetails 조회 (unpacked = 1)
    const approvedOpenedDetails = await fetchApprovedOpenedItemDetails(productId)
    console.log('승인된 개봉 상품 데이터:', approvedOpenedDetails)
    console.log('첫 번째 상품의 productCondition:', approvedOpenedDetails[0]?.productCondition)
    
    // 백엔드에서 받은 데이터를 프론트엔드 형식으로 변환
    if (approvedOpenedDetails && approvedOpenedDetails.length > 0) {
      usedItems.value = approvedOpenedDetails.map((detail: any) => {
        const mappedItem = {
          id: detail.itemId,
          title: detail.itemName || '상품명 미정',
          description: detail.productCondition || '상품 설명 없음',
          price: detail.cost || 0,
          originalPrice: product.value.originalPrice || 0, // 해당 상품의 실제 발매가 사용
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
    console.log('fetchUnpackedItems 시작 - productId:', productId)
    // 승인된 미개봉 상품의 ItemDetails 조회 (unpacked = 0)
    const approvedUnpackedDetails = await fetchApprovedUnpackedItemDetails(productId)
    unpackedItems.value = approvedUnpackedDetails?.map((detail: any) => ({
      itemId: detail.itemId,
      cost: detail.cost || 0,
      status: detail.status,
      unpacked: detail.unpacked,
      productCondition: detail.productCondition,
      itemName: detail.itemName,
      itemImgUrl: detail.itemImgUrl
    })) || []
  } catch {
    unpackedItems.value = []
  }
}

const getConditionFromQuality = (quality: number) => {
  switch (quality) {
    case 1: return 'excellent'
    case 2: return 'good'
    case 3: return 'fair'
    case 4: return 'poor'
    default: return 'fair'
  }
}

// ✅ 바로구매: 미개봉 상품만, 수량 1 고정
const handlePurchase = () => {
  const available = unpackedItems.value.find(
    (d) => d.status === true && d.unpacked === false
  )

  if (!available) {
    alert("구매 가능한 미개봉 상품이 없습니다.")
    return
  }

  router.push({
    name: "checkout",
    query: {
      mode: "buynow",
      itemDetailsId: available.itemId,
      quantity: 1
    }
  })
}

// ✅ 장바구니 담기
const handleAddToCart = async () => {
  const available = unpackedItems.value.find(
    (d) => d.status === true && d.unpacked === false
  )

  if (!available) {
    alert("장바구니에 담을 수 있는 상품이 없습니다.")
    return
  }

  await api.post("/cart", {
    itemDetailsId: available.itemId,
    quantity: 1
  })
  alert("장바구니에 추가되었습니다!")
}

// ✅ 중고상품 구매
const handleUsedItemDetailDirect = (item: any) => {
  selectedUsedItem.value = item
  showUsedItemDetail.value = true
}

const handleUsedItemPurchase = (usedItem: any) => {
  if (!usedItem || !usedItem.id) {
    alert("선택한 중고상품이 없습니다.")
    return
  }

  router.push({
    name: "checkout",
    query: {
      mode: "used",
      itemDetailsId: usedItem.id,
      quantity: 1
    }
  })
}

const handleUsedItemAddToCart = async (usedItem: any) => {
  await api.post("/cart", {
    itemDetailsId: usedItem.id,
    quantity: 1
  })
  alert("중고상품이 장바구니에 추가되었습니다!")
}

const goBack = () => window.history.back()

const initializePage = async () => {
  console.log('=== 페이지 초기화 시작 ===')
  console.log('productId:', productId)
  loading.value = true
  error.value = null
  try {
    await Promise.all([
      fetchProductDetail(),
      fetchPriceData(),
      fetchUsedItems(),
      fetchUnpackedItems(),
    ])
    
    console.log('=== 페이지 초기화 완료 ===')
    console.log('product.value:', product.value)
    console.log('usedItems.value:', usedItems.value)
    console.log('unpackedItems.value:', unpackedItems.value)
  } catch (error) {
    console.error('페이지 초기화 실패:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => initializePage())
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