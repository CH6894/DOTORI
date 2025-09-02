<!-- src/components/UsedItemDetailModal.vue (예상 경로/이름) -->
<template>
  <teleport to="body">
    <!-- 오버레이 -->
    <div class="modal-overlay" role="presentation" @click="closeModal">
      <!-- 컨테이너 -->
      <div class="modal-container" role="dialog" aria-modal="true" :aria-label="item.title || '상세 선택'" @click.stop>
        <!-- 헤더 -->
        <div class="modal-header">
          <h2 class="modal-title">{{ item.title }}</h2>
          <button class="close-btn" @click="closeModal" aria-label="닫기">&times;</button>
        </div>

        <!-- 본문 -->
        <div class="modal-content">
          <div class="item-detail-container">
            <!-- 왼쪽: 이미지 갤러리 -->
            <div class="image-gallery">
              <div class="main-image-container">
                <img :src="currentImage || '/img/placeholder.jpg'" :alt="item.title" class="main-image" />

                <!-- 상태 배지 -->
                <div class="condition-badge" :class="item.condition">
                  {{ getConditionText(item.condition) }}
                </div>

                <!-- 좌/우 화살표 -->
                <button v-if="images.length > 1" class="nav-btn prev-btn" @click="previousImage"
                  :disabled="currentImageIndex === 0">‹</button>
                <button v-if="images.length > 1" class="nav-btn next-btn" @click="nextImage"
                  :disabled="currentImageIndex === images.length - 1">›</button>

                <!-- 인디케이터 -->
                <div v-if="images.length > 1" class="image-indicators">
                  <span v-for="(image, index) in images" :key="index" class="indicator"
                    :class="{ active: index === currentImageIndex }" @click="setCurrentImage(index)" />
                </div>
              </div>

              <!-- 썸네일 -->
              <div v-if="images.length > 1" class="thumbnail-container">
                <img v-for="(image, index) in images" :key="index" :src="image || '/img/placeholder.jpg'"
                  :alt="`${item.title} ${index + 1}`" class="thumbnail-image"
                  :class="{ active: currentImage === image }" @click="setCurrentImage(index)" />
              </div>
            </div>

            <!-- 오른쪽: 상품 정보 -->
            <div class="item-info-section">
              <!-- 가격 -->
              <div class="price-section">
                <div class="current-price">{{ formatPrice(item.price) }}원</div>
                <div v-if="item.originalPrice" class="original-price">
                  정가: {{ formatPrice(item.originalPrice) }}원
                </div>
              </div>

              <!-- 세부 -->
              <div class="item-details">
                <div class="detail-row">
                  <span class="label">상품 상태</span>
                  <span class="value condition-text" :class="getGradeClass(item.quality)">
                    {{ getGradeText(item.quality) }}
                  </span>
                </div>
                <div class="detail-row">
                  <span class="label">상태 상세</span>
                  <span class="value">{{ getConditionDetails(item.conditionDetails) }}</span>
                </div>
                <div class="detail-row">
                  <span class="label">등록일</span>
                  <span class="value">{{ formatDate(item.registrationDate) }}</span>
                </div>
              </div>

              <!-- 설명 -->
              <div class="description-section">
                <h4 class="section-title">상품 설명</h4>
                <p class="description-text">{{ getItemExplanation(item.itemExplanation) }}</p>
              </div>

              <!-- 액션 -->
              <div class="action-buttons">
                <button class="heart-btn" @click="toggleWishlist" :aria-pressed="isWishlisted">
                  <span :class="['heart-icon', { active: isWishlisted }]">♡</span>
                </button>

                <!-- ✅ 장바구니: alert 없이 저장 + 토스트 -->
                <button class="cart-btn" @click="addToCart">
                  장바구니
                </button>

                <button class="purchase-btn" @click="purchaseNow">
                  구매하기
                </button>
              </div>
            </div>
          </div>
        </div>

      </div>
    </div>

    <!-- ✅ 토스트 -->
    <div v-if="showToast" class="toast" role="status" aria-live="polite">
      <div class="toast__content">장바구니에 담았습니다.</div>
    </div>
  </teleport>
</template>

<script setup>
/* eslint-disable no-undef, no-unused-vars */
import { ref, computed } from 'vue'

const props = defineProps({
  item: { type: Object, required: true },
  productInfo: { type: Object, default: () => ({}) },
})
const emit = defineEmits(['close', 'purchase', 'addToCart'])

// ---------- 상태 ----------
const currentImageIndex = ref(0)
const isWishlisted = ref(false)
const showToast = ref(false)
const TOAST_MS = 1200

// ---------- 안전 이미지 배열 ----------
const images = computed(() => {
  const arr = props.item?.images
  return Array.isArray(arr) ? arr.filter(Boolean) : []
})

// 현재 이미지
const currentImage = computed(() =>
  images.value[currentImageIndex.value] || '/img/placeholder.jpg'
)

// 이미지 네비게이션
const previousImage = () => { if (currentImageIndex.value > 0) currentImageIndex.value-- }
const nextImage = () => {
  if (currentImageIndex.value < images.value.length - 1) currentImageIndex.value++
}
const setCurrentImage = (index) => {
  if (index >= 0 && index < images.value.length) currentImageIndex.value = index
}

// 닫기
const closeModal = () => { emit('close') }

// 구매(부모가 처리)
const purchaseNow = () => { emit('purchase', props.item) }

// ---------- 장바구니 저장 (alert 없이) ----------
const LS_CART = 'dotori_cart_v1'
const getCart = () => {
  try { return JSON.parse(localStorage.getItem(LS_CART) || '[]') } catch { return [] }
}
const saveCart = (list) => localStorage.setItem(LS_CART, JSON.stringify(list))
const upsert = (cart, item) => {
  const i = cart.findIndex(x =>
    String(x.id) === String(item.id) &&
    (x.condition ?? null) === (item.condition ?? null)
  )
  if (i >= 0) cart[i].qty += item.qty
  else cart.push(item)
  return cart
}
// 현재 모달의 item으로 카트 아이템 구성
const buildCartItem = () => {
  const p = props.item || {}
  const firstImage = Array.isArray(p.images) ? p.images.find(Boolean) : null
  return {
    id: p.id ?? String(Date.now()),
    title: p.title ?? '',
    price: Number(p.currentPrice ?? p.price ?? 0),
    qty: 1,
    shipping: Number(p.shipping ?? 0),
    thumb: firstImage || '/img/placeholder.jpg',
    condition: p.condition ?? null,
    note: null,
    variant: null,
  }
}
const addToCart = () => {
  const next = upsert(getCart(), buildCartItem())
  saveCart(next)
  emit('addToCart', props.item) // 부모가 별도 처리할 게 있으면 사용

  // 토스트만 잠깐 보여주고 끝 (alert 없음, 페이지 이동 없음)
  showToast.value = true
  window.setTimeout(() => { showToast.value = false }, TOAST_MS)
}

// 찜 토글
const toggleWishlist = () => { isWishlisted.value = !isWishlisted.value }

// ---------- 유틸 ----------
const getGradeText = (quality) => {
  const map = { 1: 'S', 2: 'A', 3: 'B', 4: 'C' }
  return map[quality] || '미정'
}

const getGradeClass = (quality) => {
  const map = { 1: 'badge-grade--s', 2: 'badge-grade--a', 3: 'badge-grade--b', 4: 'badge-grade--c' }
  return map[quality] || 'badge-grade--none'
}

const getConditionText = (condition) => {
  const map = { excellent: '최상', good: '상', fair: '중', poor: '하' }
  return map[condition] || '미정'
}

const getConditionDetails = (details) => details || '상태 양호'

const getItemExplanation = (explanation) => explanation || '판매자 코멘트가 없습니다.'

const formatPrice = (price) =>
  new Intl.NumberFormat('ko-KR', { maximumFractionDigits: 0 }).format(Number(price || 0))

const formatDate = (dateString) => {
  if (!dateString) return '-'
  
  try {
    const date = new Date(dateString)
    
    // 유효한 날짜인지 확인
    if (isNaN(date.getTime())) {
      console.warn('Invalid date:', dateString)
      return '-'
    }
    
    // 한국 시간대로 변환하여 YYYY-MM-DD 형식으로 표시
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    
    return `${year}-${month}-${day}`
  } catch (error) {
    console.error('Date formatting error:', error)
    return '-'
  }
}
</script>

<style scoped>
@import '@/styles/InfoCommon.css';

/* 레이아웃 */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, .45);
  display: grid;
  place-items: center;
  z-index: 1000;
}

.modal-container {
  width: min(960px, 92vw);
  max-height: 88vh;
  overflow: auto;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 20px 80px rgba(0, 0, 0, .25);
  padding: 20px;
}

/* 헤더 */
.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.modal-title {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
}

.close-btn {
  border: none;
  background: transparent;
  font-size: 28px;
  line-height: 1;
  cursor: pointer;
}

/* 본문 */
.item-detail-container {
  display: flex;
  gap: 40px;
}

.image-gallery {
  flex: 1;
  max-width: 400px;
}

.item-info-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 300px;
}

/* 이미지 */
.main-image-container {
  position: relative;
}

.main-image {
  width: 100%;
  border-radius: 12px;
  object-fit: cover;
}

.condition-badge {
  position: absolute;
  top: 10px;
  left: 10px;
  padding: 4px 8px;
  border-radius: 999px;
  font-size: 12px;
  background: #111;
  color: #fff;
}

.nav-btn {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 32px;
  height: 32px;
  border-radius: 999px;
  border: none;
  cursor: pointer;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, .2);
}

.prev-btn {
  left: 8px;
}

.next-btn {
  right: 8px;
}

.image-indicators {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 10px;
  display: flex;
  gap: 6px;
  justify-content: center;
}

.indicator {
  width: 8px;
  height: 8px;
  border-radius: 999px;
  background: #ddd;
  cursor: pointer;
}

.indicator.active {
  background: #111;
}

.thumbnail-container {
  display: flex;
  gap: 8px;
  margin-top: 10px;
  overflow-x: auto;
}

.thumbnail-image {
  width: 64px;
  height: 64px;
  object-fit: cover;
  border-radius: 8px;
  border: 2px solid transparent;
  cursor: pointer;
}

.thumbnail-image.active {
  border-color: #111;
}

/* 가격/세부/설명 */
.price-section {
  margin-bottom: 30px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 12px;
}

.current-price {
  font-size: 28px;
  font-weight: 800;
  color: #ff6b35;
  margin-bottom: 6px;
}

.original-price {
  font-size: 14px;
  color: #666;
}

.item-details {
  margin-bottom: 30px;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.detail-row:last-child {
  border-bottom: none;
}

.label {
  font-weight: 500;
  color: #666;
}

.value {
  font-weight: 600;
  color: #333;
}

.description-section {
  margin-bottom: 30px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  margin: 0 0 10px;
}

.description-text {
  color: #666;
  line-height: 1.5;
  margin: 0;
}

/* 등급 배지 스타일 */
.badge-grade--s {
  background: linear-gradient(45deg, #9333ea, #f43f5e);
  color: white;
  padding: 4px 8px;
  border-radius: 4px;
  font-weight: bold;
  font-size: 12px;
}

.badge-grade--a {
  background: #2563eb;
  color: white;
  padding: 4px 8px;
  border-radius: 4px;
  font-weight: bold;
  font-size: 12px;
}

.badge-grade--b {
  background: #16a34a;
  color: white;
  padding: 4px 8px;
  border-radius: 4px;
  font-weight: bold;
  font-size: 12px;
}

.badge-grade--c {
  background: #f59e0b;
  color: white;
  padding: 4px 8px;
  border-radius: 4px;
  font-weight: bold;
  font-size: 12px;
}

.badge-grade--none {
  background: #9ca3af;
  color: white;
  padding: 4px 8px;
  border-radius: 4px;
  font-weight: bold;
  font-size: 12px;
}

/* 액션 */
.action-buttons {
  margin-top: auto;
  display: flex;
  gap: 8px;
}

.heart-btn,
.cart-btn,
.purchase-btn {
  height: 44px;
  padding: 0 14px;
  border-radius: 10px;
  cursor: pointer;
  border: 1px solid #e5e5e5;
  background: #fff;
}

.cart-btn {
  background: #111;
  color: #fff;
  border-color: #111;
}

.purchase-btn {
  background: #ff6b35;
  color: #fff;
  border-color: #ff6b35;
}

/* 토스트 */
.toast {
  position: fixed;
  left: 50%;
  bottom: 28px;
  transform: translateX(-50%);
  z-index: 1100;
  pointer-events: none;
}

.toast__content {
  min-width: 220px;
  max-width: 70vw;
  padding: 12px 16px;
  border-radius: 12px;
  background: rgba(25, 25, 25, .92);
  color: #fff;
  font-size: 14px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, .25);
  animation: toast-in .18s ease-out forwards;
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
