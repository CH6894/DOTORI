<template>
  <section class="product-info-section">
    <div class="product-info-container">
      <!-- 왼쪽: 상품 이미지 -->
      <div class="product-image-area">
        <div class="main-image-container">
          <img :src="currentMainImage || '/img/placeholder.jpg'" :alt="product.title" class="main-image" />

          <button v-if="images.length > 1" class="nav-btn prev-btn" @click="previousImage" :disabled="currentImageIndex === 0">‹</button>
          <button v-if="images.length > 1" class="nav-btn next-btn" @click="nextImage" :disabled="currentImageIndex === images.length - 1">›</button>

          <div v-if="images.length > 1" class="image-indicators">
            <span v-for="(image, index) in images" :key="index" class="indicator"
              :class="{ active: index === currentImageIndex }" @click="setCurrentImage(index)" />
          </div>
        </div>

        <div v-if="images.length > 1" class="thumbnail-gallery">
          <img v-for="(image, index) in images" :key="index" :src="image || '/img/placeholder.jpg'"
            :alt="`${product.title} ${index + 1}`" class="thumbnail-image"
            :class="{ active: index === currentImageIndex }" @click="setCurrentImage(index)" />
        </div>
      </div>

      <!-- 오른쪽: 상품 정보 -->
      <div class="product-details-area">
        <h1 class="product-title">{{ product.name }}</h1>

        <div class="product-meta">
          <div class="brand-info"><span class="label">제조사</span><span class="value">{{ product.brand }}</span></div>
          <div class="series-info"><span class="label">타이틀</span><span class="value">{{ product.title }}</span></div>
          <div class="price-info">
            <div class="original-price">
              <span class="label">발매가</span>
              <span class="value">{{ computedOriginalPrice }}</span>
            </div>
            <div class="current-price">
              <span class="label">현재가</span>
              <span class="price">{{ computedCurrentPrice }}</span>
            </div>
          </div>
        </div>

        <!-- 옵션 -->
        <div class="product-options">
          <div class="option-item"><span class="option-label">배송비</span><span class="option-value">무료배송</span></div>
          <div class="option-item"><span class="option-label">배송예정</span><span class="option-value">2-3일 내 배송</span></div>
        </div>

        <!-- 버튼 -->
        <div class="action-buttons">
          <button :class="['wish-heart', { active: isLiked }]" @click="toggleLike" aria-label="위시 토글" title="위시 토글">
            <svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor" aria-hidden="true">
              <path
                d="M12 21s-6.716-4.146-9.193-7.142C.61 11.41 1.077 8.5 3.2 6.9c1.86-1.42 4.46-1.17 6.11.44L12 10l2.69-2.66c1.65-1.61 4.25-1.86 6.11-.44 2.12 1.6 2.59 4.51.393 6.958C18.716 16.854 12 21 12 21z" />
            </svg>
          </button>
          <button class="sell-btn" @click="handleSell">판매</button>
          <button class="purchase-btn" @click="buyNowDirect">미개봉 상품 구매</button>
        </div>

        <div class="cart-section">
          <button class="cart-btn" @click="addToCartAndNotify">장바구니 담기</button>
        </div>
      </div>
    </div>

    <!-- 판매 확인 모달 -->
    <ModalSellConfirm v-if="showSellModal" :item="sellItem" :rows="priceRows" @close="closeSellModal" @submit="onSellSubmit" />

    <!-- 중고 상세 모달 -->
    <Teleport to="body">
      <UsedItemDetailModal v-if="showUsedModal" :item="product" :mode="usedMode" v-model:open="showUsedModal"
        @close="closeUsedModal" @confirm="onUsedConfirm" />
    </Teleport>

    <!-- ✅ 장바구니 토스트 -->
    <Teleport to="body">
      <div v-if="showCartToast" class="cart-toast">
        <div class="cart-toast__content">🛒 상품이 장바구니에 추가되었습니다.</div>
      </div>
    </Teleport>
  </section>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import ModalSellConfirm from '@/components/ModalSellConfirm.vue'
import UsedItemDetailModal from '@/components/UsedItemDetailModal.vue'
import { useWishlistStore } from '@/stores/wishlist'
import api from '@/api/axios'

const wish = useWishlistStore()
wish.load()

/* ===== 타입 정의 ===== */
interface Product {
  id?: string | number
  itemId?: number
  itemCode?: string
  name?: string,
  title?: string
  brand?: string
  series?: string
  originalPrice?: number | string
  currentPrice?: number | string
  price?: number | string
  condition?: string
  shipping?: number | string
  images?: string[]
  genre?: string
  size?: string
  manufacturer?: string
  material?: string
  description?: string
  releaseMonth?: string
  storageFees?: number
}

interface UsedConfirmPayload {
  qty?: number
  price?: number
  condition?: string | null
  note?: string | null
  variant?: string | null
}

interface CartItem {
  id: string | number
  title: string
  price: number
  qty: number
  shipping: number
  thumb: string
  condition: string | null
  note: string | null
  variant: string | null
}

/* ===== props / emits ===== */
const props = defineProps<{ 
  product: any
  approvedUnpackedDetails?: Array<any>
  usedItems?: Array<any>
}>()
const emit = defineEmits(['purchase','addToCart'])
const router = useRouter()

/* ===== 상태 ===== */
const isLiked = computed(() => {
  const itemId = Number(props.product.itemId || props.product.id)
  return itemId ? wish.has(itemId) : false
})
const currentImageIndex = ref<number>(0)

// ✅ 토스트 상태
const showCartToast = ref(false)
const TOAST_MS = 1500

// 이미지 관련
const images = computed(() => Array.isArray(props.product?.images) ? props.product.images.filter(Boolean) : [])
const currentMainImage = computed(() => images.value[currentImageIndex.value] ?? '/img/placeholder.jpg')

// 모달 상태
const showSellModal = ref(false)
const showUsedModal = ref(false)
const usedMode = ref<'buy' | 'cart'>('buy')

/* ===== 발매가 계산 ===== */
const computedOriginalPrice = computed(() => {
  const originalPrice = props.product.originalPrice || 0
  console.log('ProductInfo - originalPrice:', originalPrice)
  
  if (typeof originalPrice === 'number') {
    return `${originalPrice.toLocaleString()}원`
  } else if (typeof originalPrice === 'string') {
    return originalPrice.includes('원') ? originalPrice : `${originalPrice}원`
  } else {
    return '0원'
  }
})

/* ===== 현재가 계산 ===== */
const computedCurrentPrice = computed(() => {
  if (props.approvedUnpackedDetails && props.approvedUnpackedDetails.length > 0) {
    const valid = props.approvedUnpackedDetails.filter(d => d.status && d.unpacked === false && d.cost > 0)
    if (valid.length > 0) {
      const minPrice = Math.min(...valid.map(d => Number(d.cost)))
      return `${minPrice.toLocaleString()}원`
    }
  }
  
  // 승인된 미개봉 상품이 없으면 기존 현재가 또는 발매가 표시
  const fallbackPrice = props.product.currentPrice || props.product.originalPrice || 0
  console.log('ProductInfo - fallback 가격:', fallbackPrice)
  
  // 숫자인 경우 포맷팅, 이미 문자열인 경우 그대로 반환
  if (typeof fallbackPrice === 'number') {
    return `${fallbackPrice.toLocaleString()}원`
  } else if (typeof fallbackPrice === 'string') {
    // 이미 '원'이 포함되어 있는지 확인
    return fallbackPrice.includes('원') ? fallbackPrice : `${fallbackPrice}원`
  } else {
    return '0원'
  }
})

// 판매 모달용 아이템
const sellItem = computed(() => ({
  id: props.product?.id ?? 0,
  itemCode: props.product?.itemCode ?? String(props.product?.id ?? ''),
  name: props.product?.name ?? '',
  title: props.product?.title ?? '',
  images: props.product?.images ?? ['/img/placeholder.jpg'],
  price: Number(props.product?.currentPrice ?? 0)
}))

const priceRows = ref([{ option: '미개봉', price: Number(props.product?.currentPrice ?? 0), date: '2025-09-01' }])

// 이미지 전환
const previousImage = (): void => {
  if (currentImageIndex.value > 0) currentImageIndex.value--
}

const nextImage = (): void => {
  if (currentImageIndex.value < images.value.length - 1) currentImageIndex.value++
}

const setCurrentImage = (index: number): void => {
  currentImageIndex.value = index
}

/* ===== 좋아요 토글 + 키보드 접근성 ===== */
const toggleLike = async (): Promise<void> => {
  try {
    // itemId가 있으면 itemId 사용, 없으면 id 사용
    const itemId = Number(props.product.itemId || props.product.id)
    if (!itemId) {
      console.error('상품 ID가 없습니다. product:', props.product)
      return
    }
    
    await wish.toggle(itemId)
  } catch (error) {
    console.error('위시리스트 토글 실패:', error)
    if (error instanceof Error && error.message === '로그인이 필요합니다') {
      alert('위시리스트를 사용하려면 로그인이 필요합니다.')
    }
  }
}

function onLikeKey(e: KeyboardEvent): void {
  if (e.key === ' ' || e.key === 'Spacebar') {
    e.preventDefault()
    toggleLike()
  }
  if (e.key === 'Enter') {
    e.preventDefault()
    toggleLike()
  }
}

/* ===== 판매 모달 ===== */
const handleSell = (): void => {
  showSellModal.value = true
}
const closeSellModal = (): void => {
  showSellModal.value = false
}
const onSellSubmit = (payload: unknown): void => {
  console.log('Modal submit payload:', payload)
  showSellModal.value = false
}

/* ===== 중고 상세 모달 열기 ===== */
const openUsedModal = (mode: 'buy' | 'cart'): void => {
  usedMode.value = mode
  showUsedModal.value = true
}
const closeUsedModal = (): void => {
  showUsedModal.value = false
}
const onUsedConfirm = (payload: UsedConfirmPayload): void => {
  console.log('Used item confirm payload:', payload)
  showUsedModal.value = false
}

/* ===== 장바구니/구매 유틸 ===== */
const LS_CART = 'dotori_cart_v1'
const SS_BUY_ONE = 'dotori_checkout_one'

const getCart = (): CartItem[] => {
  try {
    const raw = localStorage.getItem(LS_CART)
    return raw ? (JSON.parse(raw) as CartItem[]) : []
  } catch {
    return []
  }
}
const saveCart = (list: CartItem[]): void => {
  localStorage.setItem(LS_CART, JSON.stringify(list))
}

const upsert = (cart: CartItem[], item: CartItem): CartItem[] => {
  const i = cart.findIndex(
    (x) =>
      String(x.id) === String(item.id) &&
      (x.condition ?? null) === (item.condition ?? null),
  )
  if (i >= 0) cart[i].qty += item.qty
  else cart.push(item)
  return cart
}

const buildCartItem = (payload: UsedConfirmPayload = {}): CartItem => {
  const p = props.product ?? {}
  const firstImage = Array.isArray(p.images) ? p.images.find(Boolean) ?? null : null

  return {
    id: p.id ?? String(Date.now()),
    title: p.title ?? '',
    price: Number(payload.price ?? p.currentPrice ?? p.price ?? 0),
    qty: 1,
    shipping: Number(p.shipping ?? 0),
    thumb: firstImage ?? '/img/placeholder.jpg',

    condition: (payload.condition ?? p.condition ?? null) ?? null,
    note: payload.note ?? null,
    variant: payload.variant ?? null,
  }
}

/* ===== 구매 플로우 ===== */
// 구매 버튼 → 모달 없이 즉시 결제 페이지
const buyNowDirect = (): void => {
  const firstDetail = props.approvedUnpackedDetails?.find(d => d.status && d.unpacked === false)
  if (!firstDetail) {
    alert("구매 가능한 미개봉 상품이 없습니다.")
    return
  }
  emit('purchase', props.product)
  router.push({ name: "checkout", query: { mode: "buynow", itemDetailsId: firstDetail.itemId, quantity: 1 } })
}

// ✅ 장바구니 버튼
const addToCartAndNotify = async () => {
  try {
    const availableUnpacked = props.approvedUnpackedDetails?.find(d => d.status && d.unpacked === false)
    if (availableUnpacked) {
      await api.post("/cart", { itemDetailsId: availableUnpacked.itemId, quantity: 1 })
      triggerCartToast()
      return
    }
    const availableUsed = props.usedItems?.find(item => item.status === true)
    if (availableUsed) {
      await api.post("/cart", { itemDetailsId: availableUsed.id, quantity: 1 })
      triggerCartToast()
      return
    }
    alert("구매 가능한 미개봉 상품이 없습니다.")
  } catch {
    alert("장바구니 담기 중 오류가 발생했습니다.")
  }
}

const triggerCartToast = () => {
  showCartToast.value = true
  setTimeout(() => { showCartToast.value = false }, TOAST_MS)
}
</script>


<style scoped>
@import '@/styles/InfoCommon.css';

/* ProductInfo 컴포넌트 특화 스타일 */
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
  margin: 0 0 30px;
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

/* 버튼들 */
.like-btn {
  height: 56px;
  border-radius: 10px;
  border: 1px solid #e5e5e5;
  background: #fff;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.sell-btn {
  flex: 1;
  border: none;
  height: 56px;
  background: #28a745;
  color: #fff;
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

/* 동그란 하트 버튼 */
.wish-heart {
  display: inline-grid;
  place-items: center;
  width: 3.5rem;
  height: 3.5rem;
  border: 1px solid #e5e7eb;
  border-radius: 999px;
  background: #fff;
  color: #bebebe;
  cursor: pointer;
  transition: transform .15s ease, background .15s ease, border-color .15s ease;
}

.wish-heart:hover {
  transform: scale(1.04);
  background: #fff5f5;
  border-color: #fecaca;
}

.wish-heart.active {
  background: #fee2e2;
  color: #dc2626;
  border-color: #fecaca;
}

.wish-heart svg {
  pointer-events: none;
}

/* 장바구니 성공 모달 스타일 */
.cart-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: flex-end;
  justify-content: center;
  z-index: 1000;
}

.cart-modal {
  background: white;
  border-radius: 16px 16px 0 0;
  width: 100%;
  max-width: 480px;
  max-height: 80vh;
  overflow-y: auto;
}

.cart-modal-content {
  padding: 24px;
  text-align: center;
}

.cart-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.cart-modal-content h3 {
  font-size: 18px;
  font-weight: 700;
  margin-bottom: 8px;
  color: #333;
}

.cart-modal-content p {
  color: #666;
  margin-bottom: 24px;
  line-height: 1.5;
}

.cart-modal-actions {
  display: flex;
  gap: 12px;
}

.btn-continue {
  flex: 1;
  padding: 12px 16px;
  border: 1px solid #ddd;
  border-radius: 8px;
  background: white;
  color: #333;
  font-weight: 600;
  cursor: pointer;
}

.btn-go-cart {
  flex: 1;
  padding: 12px 16px;
  border: none;
  border-radius: 8px;
  background: #ff7a2e;
  color: white;
  font-weight: 700;
  cursor: pointer;
}

.btn-continue:hover {
  background: #f5f5f5;
}

.btn-go-cart:hover {
  background: #e6651a;
}

.modal-fade-enter-active,
.modal-fade-leave-active {
  transition: opacity 0.3s ease;
}

.modal-fade-enter-from,
.modal-fade-leave-to {
  opacity: 0;
}

.modal-fade-enter-active .cart-modal,
.modal-fade-leave-active .cart-modal {
  transition: transform 0.3s ease;
}

.modal-fade-enter-from .cart-modal,
.modal-fade-leave-to .cart-modal {
  transform: translateY(100%);
}

/* 장바구니 토스트 */
.cart-toast {
  position: fixed;
  left: 50%;
  bottom: 28px;
  transform: translateX(-50%);
  z-index: 9999;
  pointer-events: none;
}

.cart-toast__content {
  min-width: 180px;
  max-width: 60vw;
  padding: 12px 16px;
  border-radius: 12px;
  background: #670600;
  color: #fff;
  font-size: 14px;
  font-weight: 700;
  box-shadow: 0 10px 30px rgba(0, 0, 0, .25);
  animation: toast-in .18s ease-out forwards;
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
  gap: 8px;
}

.cart-toast__text {
  display: inline;
}

@keyframes toast-in {
  from {
    opacity: 0;
    transform: translateY(8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>