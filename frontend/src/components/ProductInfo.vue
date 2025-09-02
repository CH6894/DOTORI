<!-- src/components/ProductInfo.vue -->
<template>
  <section class="product-info-section">
    <div class="product-info-container">
      <!-- 왼쪽: 상품 이미지 갤러리 -->
      <div class="product-image-area">
        <!-- 메인 이미지 -->
        <div class="main-image-container">
          <img :src="currentMainImage || '/img/placeholder.jpg'" :alt="product.title" class="main-image" />

          <!-- 이미지 네비게이션 화살표 -->
          <button v-if="images.length > 1" class="nav-btn prev-btn" @click="previousImage"
            :disabled="currentImageIndex === 0">
            ‹
          </button>
          <button v-if="images.length > 1" class="nav-btn next-btn" @click="nextImage"
            :disabled="currentImageIndex === images.length - 1">
            ›
          </button>

          <!-- 이미지 인디케이터 -->
          <div v-if="images.length > 1" class="image-indicators">
            <span v-for="(image, index) in images" :key="index" class="indicator"
              :class="{ active: index === currentImageIndex }" @click="setCurrentImage(index)" />
          </div>
        </div>

        <!-- 썸네일 이미지들 -->
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
          <div class="brand-info">
            <span class="label">제조사</span>
            <span class="value">{{ product.brand }}</span>
          </div>
          <div class="series-info">
            <span class="label">타이틀</span>
            <span class="value">{{ product.title }}</span>
          </div>
          <div class="price-info">
            <div class="original-price">
              <span class="label">발매가</span>
              <span class="value">{{ product.originalPrice || 0 }}</span>
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
          <button :class="['wish-heart', { active: wish.has(product.id) }]"
            @click="wish.toggle({ id: product.id, title: product.title, price: product.price, image: product.images?.[0] })"
            aria-label="위시 토글" title="위시 토글">
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
    <ModalSellConfirm v-if="showSellModal" :item="sellItem" :rows="priceRows" @close="closeSellModal"
      @submit="onSellSubmit" />

    <!-- 중고 상세 선택 모달 -->
    <Teleport to="body">
      <UsedItemDetailModal v-if="showUsedModal" :item="product" :mode="usedMode" v-model:open="showUsedModal"
        @close="closeUsedModal" @confirm="onUsedConfirm" />
    </Teleport>

    <!-- 장바구니 토스트 -->
    <Teleport to="body">
      <div v-if="showCartToast" class="cart-toast" role="status" aria-live="polite">
        <div class="cart-toast__content">
          <span class="cart-toast__text">장바구니에 담았습니다.</span>
        </div>
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
const wish = useWishlistStore()
wish.load()

/* ===== 타입 정의 ===== */
interface Product {
  id?: string | number
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
const props = defineProps<{ product: Product }>()
const emit = defineEmits<{
  (e: 'purchase', p: Product): void
  (e: 'addToCart', p: Product): void
}>()

const router = useRouter()

/* ===== 상태 ===== */
const isLiked = ref<boolean>(false)
const currentImageIndex = ref<number>(0)

/* 판매 모달 */
const showSellModal = ref<boolean>(false)

/* 중고 상세 모달 */
const showUsedModal = ref<boolean>(false)
const usedMode = ref<'buy' | 'cart'>('buy')

/* 토스트 상태 */
const showCartToast = ref<boolean>(false)
const TOAST_DURATION = 1200 // ms

/* ===== 안전 이미지 배열 ===== */
const images = computed<string[]>(() => {
  const arr = props.product?.images
  return Array.isArray(arr) ? (arr.filter(Boolean) as string[]) : []
})

/* ===== 판매 모달용 데이터 ===== */
const sellItem = computed(() => ({
  id: props.product?.id?.toString() ?? '0',
  itemCode: props.product?.id?.toString() ?? '0',
  name: props.product?.name ?? '',
  title: props.product?.title ?? '',
  images: images.value.length ? images.value : ['/img/placeholder.jpg'],
  condition: undefined as unknown as undefined,
  price: Number(props.product?.currentPrice ?? props.product?.price ?? 0),
  cost: Number(props.product?.currentPrice ?? props.product?.price ?? 0),
  genre: props.product?.genre,
  size: props.product?.size,
  manufacturer: props.product?.manufacturer,
  material: props.product?.material,
  information: props.product?.description,
  releaseMonth: props.product?.releaseMonth,
  imgUrl: images.value[0] || '/img/placeholder.jpg',
  storageFees: props.product?.storageFees
}))

const priceRows = ref<Array<{ option: string; price: number; date: string }>>([
  {
    option: '미개봉',
    price: Number(props.product?.currentPrice ?? props.product?.price ?? 0),
    date: '2025-08-01',
  },
  {
    option: '사용감 없음',
    price: Math.max(
      0,
      Number(props.product?.currentPrice ?? props.product?.price ?? 0) - 2000,
    ),
    date: '2025-08-15',
  },
])

/* ===== 이미지 갤러리 ===== */
const currentMainImage = computed<string>(() => {
  return images.value[currentImageIndex.value] ?? '/img/placeholder.jpg'
})

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
const toggleLike = (): void => {
  isLiked.value = !isLiked.value
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
    qty: Math.max(1, Number(payload.qty ?? 1)),
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
  const item = buildCartItem({})
  sessionStorage.setItem(SS_BUY_ONE, JSON.stringify([item]))
  emit('purchase', props.product)
  router.push({ name: 'checkout', query: { mode: 'buynow' } })
}

/* ===== 장바구니 버튼 플로우  ===== */
const addToCartAndNotify = (): void => {
  // 1) 실제 장바구니 저장 (upsert)
  const next = upsert(getCart(), buildCartItem({}))
  saveCart(next)
  emit('addToCart', props.product)

  // 2) 토스트 보여주고 자동 닫기
  showCartToast.value = true
  window.setTimeout(() => {
    showCartToast.value = false
  }, TOAST_DURATION)
}

/* ===== 모달에서 확정 시 (cart 분기도 토스트만) ===== */
const onUsedConfirm = (payload: UsedConfirmPayload): void => {
  const item = buildCartItem(payload)

  if (usedMode.value === 'cart') {
    const next = upsert(getCart(), item)
    saveCart(next)
    emit('addToCart', props.product)
    showCartToast.value = true
    window.setTimeout(() => { showCartToast.value = false }, TOAST_DURATION)

    closeUsedModal()
    return
  }

  // buy 분기
  sessionStorage.setItem(SS_BUY_ONE, JSON.stringify([item]))
  emit('purchase', props.product)
  closeUsedModal()
  router.push({ name: 'checkout', query: { mode: 'buynow' } })
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

/* SVG 하트 (빈 → 채움) */
/* 동그란 하트 버튼 */
.wish-heart {
  display: inline-grid;
  place-items: center;
  width: 3.5rem;
  /* 32px */
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
  /* 활성화 시 살짝 강조 */
  color: #dc2626;
  border-color: #fecaca;
}

.wish-heart svg {
  pointer-events: none;
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

  /* 👇 중앙 정렬 핵심 */
  display: flex;
  align-items: center;
  /* 수직 중앙 */
  justify-content: center;
  /* 수평 중앙 */
  text-align: center;
  /* 여러 줄일 때 텍스트 중앙 */
  gap: 8px;
  /* 아이콘이 있다면 간격 */
}

.cart-toast__text {
  /* 굳이 inline-block 필요 없음 */
  display: inline;
  /* or simply remove this rule */
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
