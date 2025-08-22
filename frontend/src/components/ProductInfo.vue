<template>
  <section class="product-info-section">
    <div class="product-info-container">
      <!-- 왼쪽: 상품 이미지 갤러리 -->
      <div class="product-image-area">
        <!-- 메인 이미지 -->
        <div class="main-image-container">
          <img :src="currentMainImage || '/img/placeholder.jpg'" :alt="product.title" class="main-image" />

          <!-- 이미지 네비게이션 화살표 -->
          <button v-if="product.images?.length > 1" class="nav-btn prev-btn" @click="previousImage"
            :disabled="currentImageIndex === 0">
            ‹
          </button>
          <button v-if="product.images?.length > 1" class="nav-btn next-btn" @click="nextImage"
            :disabled="currentImageIndex === product.images.length - 1">
            ›
          </button>

          <!-- 이미지 인디케이터 -->
          <div v-if="product.images?.length > 1" class="image-indicators">
            <span v-for="(image, index) in product.images" :key="index" class="indicator"
              :class="{ active: index === currentImageIndex }" @click="setCurrentImage(index)"></span>
          </div>
        </div>

        <!-- 썸네일 이미지들 -->
        <div v-if="product.images?.length > 1" class="thumbnail-gallery">
          <img v-for="(image, index) in product.images" :key="index" :src="image || '/img/placeholder.jpg'"
            :alt="`${product.title} ${index + 1}`" class="thumbnail-image"
            :class="{ active: index === currentImageIndex }" @click="setCurrentImage(index)" />
        </div>
      </div>

      <!-- 오른쪽: 상품 정보 -->
      <div class="product-details-area">
        <h1 class="product-title">{{ product.title }}</h1>

        <div class="product-meta">
          <div class="brand-info">
            <span class="label">제조사</span>
            <span class="value">{{ product.brand }}</span>
          </div>
          <div class="series-info">
            <span class="label">작품명</span>
            <span class="value">{{ product.series || '귀멸의 칼날' }}</span>
          </div>
          <div class="price-info">
            <div class="original-price">
              <span class="label">발매가</span>
              <span class="value">{{ product.originalPrice }}</span>
            </div>
            <div class="current-price">
              <span class="label">현재가</span>
              <span class="price">{{ product.currentPrice }}</span>
            </div>
          </div>
        </div>

        <!-- 상품 옵션/정보 -->
        <div class="product-options">
          <div class="option-item">
            <span class="option-label">배송비</span>
            <span class="option-value">무료배송</span>
          </div>
          <div class="option-item">
            <span class="option-label">배송예정</span>
            <span class="option-value">2-3일 내 배송</span>
          </div>
        </div>

        <!-- 액션 버튼들 -->
        <div class="action-buttons">
          <button class="like-btn" @click="toggleLike">
            <span :class="['like-icon', { active: isLiked }]">{{ isLiked ? '♥' : '♡' }}</span>
          </button>
          <button class="sell-btn" @click="handleSell">판매</button>
          <button class="purchase-btn" @click="openUsedModal('buy')">구매</button>
        </div>

        <div class="cart-section">
          <button class="cart-btn" @click="openUsedModal('cart')">
            장바구니
          </button>
        </div>
      </div>
    </div>

    <!-- 판매 확인 모달 (기존) -->
    <ModalSellConfirm v-if="showSellModal" :item="sellItem" :rows="priceRows" @close="closeSellModal"
      @submit="onSellSubmit" />

    <!-- ✅ 중고 상세 선택 모달: 버튼 클릭 시 /checkout으로 즉시 이동하지 않고 이 모달을 먼저 띄움 -->
    <Teleport to="body">
      <UsedItemDetailModal v-if="showUsedModal" :item="product" :mode="usedMode" v-model:open="showUsedModal"
        @close="closeUsedModal" @confirm="onUsedConfirm" />
    </Teleport>

  </section>
</template>

<script setup>
/* eslint-disable no-undef */
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import ModalSellConfirm from '@/components/ModalSellConfirm.vue'
import UsedItemDetailModal from '@/components/UsedItemDetailModal.vue'

const props = defineProps({
  product: {
    type: Object,
    default: () => ({})
  }
})

// (부모에서 이미 듣고 있으면 유지)
const emit = defineEmits(['purchase', 'addToCart'])

const router = useRouter()

/* ---------- 상태 ---------- */
const isLiked = ref(false)
const currentImageIndex = ref(0)

/* 판매 모달 */
const showSellModal = ref(false)

/* ✅ 중고 상세 모달 */
const showUsedModal = ref(false)
const usedMode = ref('buy') // 'buy' | 'cart'

/* ---------- 판매 모달용 ---------- */
const sellItem = computed(() => ({
  id: props.product.id ?? 0,
  title: props.product.title ?? '',
  images: props.product.images?.length ? props.product.images : ['/img/placeholder.jpg'],
  condition: props.product.condition ?? 'excellent',
  price: Number(props.product.currentPrice ?? props.product.price ?? 0),
}))
const priceRows = ref([
  { option: '미개봉', price: Number(props.product.currentPrice ?? props.product.price ?? 0), date: '2025-08-01' },
  { option: '사용감 없음', price: Math.max(0, Number(props.product.currentPrice ?? props.product.price ?? 0) - 2000), date: '2025-08-15' },
])

/* ---------- 이미지 갤러리 ---------- */
const currentMainImage = computed(() => {
  if (props.product.images?.length > 0) {
    return props.product.images[currentImageIndex.value]
  }
  return '/img/placeholder.jpg'
})
const previousImage = () => { if (currentImageIndex.value > 0) currentImageIndex.value-- }
const nextImage = () => {
  if (props.product.images && currentImageIndex.value < props.product.images.length - 1) currentImageIndex.value++
}
const setCurrentImage = (index) => { currentImageIndex.value = index }

/* ---------- 좋아요 ---------- */
const toggleLike = () => { isLiked.value = !isLiked.value }

/* ---------- 판매 버튼(모달) ---------- */
const handleSell = () => { showSellModal.value = true }
const closeSellModal = () => { showSellModal.value = false }
const onSellSubmit = (payload) => {
  // TODO: 서버 제출 등 필요한 처리
  console.log('Modal submit payload:', payload)
  showSellModal.value = false
}

/* ---------- ✅ UsedItemDetailModal 열기 ---------- */
const openUsedModal = (mode /* 'buy' | 'cart' */) => {
  usedMode.value = mode
  showUsedModal.value = true
}
const closeUsedModal = () => { showUsedModal.value = false }

/* ---------- 장바구니/구매 공통 유틸 (모달에서 확정 후 실행) ---------- */
const LS_CART = 'dotori_cart_v1'          // 장바구니 저장소
const SS_BUY_ONE = 'dotori_checkout_one'  // 바로구매 단건

const getCart = () => {
  try { return JSON.parse(localStorage.getItem(LS_CART) || '[]') } catch { return [] }
}
const saveCart = (list) => localStorage.setItem(LS_CART, JSON.stringify(list))

// 같은 id + (선택) 상태/옵션이 같으면 수량 합치기
const upsert = (cart, item) => {
  const i = cart.findIndex(x =>
    String(x.id) === String(item.id) &&
    (x.condition ?? null) === (item.condition ?? null)
  )
  if (i >= 0) cart[i].qty += item.qty
  else cart.push(item)
  return cart
}

// 모달에서 전달한 선택값(payload)에 맞춰 카트 아이템 생성
const buildCartItem = (payload = {}) => {
  const p = props.product || {}
  const firstImage = Array.isArray(p.images) ? p.images.find(Boolean) : null

  return {
    id: p.id ?? String(Date.now()),
    title: p.title ?? '',
    price: Number(payload.price ?? p.currentPrice ?? p.price ?? 0),
    qty: Math.max(1, Number(payload.qty ?? 1)),
    shipping: Number(p.shipping ?? 0),
    thumb: firstImage || '/img/placeholder.jpg',

    // 중고/선택 정보들(있으면 저장 → upsert 키로도 사용)
    condition: payload.condition ?? p.condition ?? null,
    note: payload.note ?? null,
    variant: payload.variant ?? null,
  }
}

/* ---------- ✅ 모달 확정 핸들러 ---------- */
const onUsedConfirm = (payload /* { qty, price, condition, ... } */) => {
  const item = buildCartItem(payload)

  if (usedMode.value === 'cart') {
    const next = upsert(getCart(), item)
    saveCart(next)
    emit('addToCart', props.product)
    alert('장바구니에 담았습니다.')
    closeUsedModal()
    return
  }

  // usedMode === 'buy' → 즉시구매: 모달에서 확정 후에만 체크아웃으로 진행
  sessionStorage.setItem(SS_BUY_ONE, JSON.stringify([item]))
  emit('purchase', props.product)
  closeUsedModal()
  // 필요 시 결제 페이지로 이동 (모달 확인 후)
  // 주: 버튼 클릭 시에는 이동하지 않고, 모달에서 확정한 뒤 이동합니다.
  router.push({ name: 'checkout', query: { mode: 'buynow' } })
}
</script>

<style scoped>
/* 공통 스타일 import - 파일명 수정 */
@import '@/styles/InfoCommon.css';

/* ProductInfo 컴포넌트 특화 스타일만 */
.product-info-section {
  padding: 20px 0;
  border-bottom: 1px solid #eee;
}

.product-info-container {
  display: flex;
  gap: 50px;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.product-image-area {
  flex: 1.3;
  max-width: 500px;
}

.product-details-area {
  flex: 0.8;
  min-width: 450px;
}

.product-title {
  font-size: 30px;
  font-weight: bold;
  color: #333;
  margin-bottom: 30px;
  margin-top: 0;
  line-height: 1.3;
}

.product-meta {
  margin-bottom: 30px;
}

.brand-info,
.series-info {
  display: flex;
  margin-bottom: 12px;
}

.brand-info .label,
.series-info .label {
  width: 70px;
  color: #666;
  font-weight: 500;
  font-size: 13px;
}

.brand-info .value,
.series-info .value {
  color: #333;
  font-weight: 600;
  font-size: 13px;
}

.price-info {
  background: #f8f9fa;
  padding: 20px;
  border-radius: 12px;
}

.original-price,
.current-price {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.original-price {
  margin-bottom: 12px;
  color: #666;
  font-size: 14px;
}

.current-price {
  font-size: 18px;
  font-weight: bold;
}

.current-price .price {
  color: #FC703C;
  font-size: 24px;
}

.product-options {
  margin-bottom: 60px;
}

.option-item {
  display: flex;
  justify-content: space-between;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.option-label {
  color: #666;
  font-weight: 500;
  font-size: 14px;
}

.option-value {
  color: #333;
  font-weight: 600;
  font-size: 14px;
}

.action-buttons {
  margin-bottom: 10px;
  gap: 8px;
  display: grid;
  grid-template-columns: 56px 1fr 1fr;
}

.like-btn {
  height: 56px;
  border-radius: 10px;
  border: 1px solid #e5e5e5;
  background: #fff;
}

.like-icon {
  font-size: 20px;
}

.like-icon.active {
  color: #FC703C;
}

.sell-btn {
  flex: 1;
  border: none;
  height: 56px;
  background: #28a745;
  color: white;
  font-size: 18px;
  border-radius: 10px;
}

.sell-btn:hover {
  background: #124e20;
}

.purchase-btn {
  flex: 1;
  height: 56px;
  font-size: 18px;
  border-radius: 10px;
}

.cart-section {
  display: flex;
}

.cart-btn {
  flex: 1;
  border-radius: 10px;
}
</style>
