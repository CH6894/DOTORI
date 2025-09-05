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

                <!-- 이미지 타입 배지 -->
                <div class="condition-badge" :class="currentImageType === '관리자' ? 'admin' : 'seller'">
                  {{ currentImageType }}
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
                  발매가: {{ formatPrice(item.originalPrice) }}원
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

              <!-- 관리자 이미지 섹션 -->
              <div v-if="adminImages.length > 0" class="admin-images-section">
                <h4 class="section-title">관리자 검수 이미지</h4>
                <div class="admin-images-grid">
                  <img v-for="(image, index) in adminImages" :key="`admin-${index}`" :src="image"
                    :alt="`관리자 검수 이미지 ${index + 1}`" class="admin-image" @click="setCurrentImage(index)" />
                </div>
              </div>

              <!-- 액션 -->
              <div class="action-buttons">
                <button :class="['wish-heart', { active: isWishlisted }]" @click="toggleWishlist" aria-label="위시 토글"
                  title="위시 토글">
                  <span>♡</span>
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
import { ref, computed, onMounted, watch } from 'vue'
import { useWishlistStore } from '@/stores/wishlist'
import { fetchAllImagesByItemDetailsId, fetchAdminImagesByItemDetailsId, fetchSellerImagesByItemDetailsId, fetchAllImageInfosByItemDetailsId } from '@/api/items'

const wish = useWishlistStore()

const props = defineProps({
  item: { type: Object, required: true },
  productInfo: { type: Object, default: () => ({}) },
})
const emit = defineEmits(['close', 'purchase', 'addToCart'])

// ---------- 상태 ----------
const currentImageIndex = ref(0)
const isWishlisted = computed(() => {
  const itemId = Number(props.item.id)
  return itemId ? wish.has(itemId) : false
})
const showToast = ref(false)
const TOAST_MS = 1200

// ---------- 이미지 상태 ----------
const adminImages = ref([])
const sellerImages = ref([])
const imageInfos = ref([]) // 이미지 정보 (URL + 타입)
const isLoadingImages = ref(false)

// ---------- 안전 이미지 배열 ----------
const images = computed(() => {
  // imageInfos가 있으면 사용 (관리자 이미지 우선, 판매자 이미지 후순위)
  if (imageInfos.value.length > 0) {
    return imageInfos.value.map(info => info.url)
  }
  // 기존 방식 (fallback)
  const allImages = [...adminImages.value, ...sellerImages.value]
  if (allImages.length > 0) {
    return allImages
  }
  const arr = props.item?.images
  return Array.isArray(arr) ? arr.filter(Boolean) : []
})

// 현재 이미지
const currentImage = computed(() =>
  images.value[currentImageIndex.value] || '/img/placeholder.jpg'
)

// 현재 이미지의 타입 정보
const currentImageType = computed(() => {
  if (imageInfos.value.length > 0 && currentImageIndex.value < imageInfos.value.length) {
    return imageInfos.value[currentImageIndex.value].typeLabel
  }
  return '판매자' // 기본값
})

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

// 위시리스트 클릭 핸들러
// 찜 토글
const toggleWishlist = async () => {
  try {
    const itemId = Number(props.item.id)
    if (!itemId) {
      console.error('상품 ID가 없습니다')
      return
    }

    await wish.toggle(itemId)
  } catch (error) {
    console.error('위시리스트 토글 실패:', error)
  }
}

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

// ---------- 이미지 로딩 ----------
const loadAllImages = async () => {
  if (!props.item?.id) return

  try {
    isLoadingImages.value = true
    console.log('이미지 로딩 시작:', props.item.id)

    // 새로운 API를 사용하여 이미지 정보를 로드 (관리자 이미지 우선, 판매자 이미지 후순위)
    try {
      const imageInfosData = await fetchAllImageInfosByItemDetailsId(props.item.id)
      console.log('이미지 정보들:', imageInfosData)

      if (imageInfosData && imageInfosData.length > 0) {
        imageInfos.value = imageInfosData
        console.log('imageInfos 설정 완료:', imageInfosData.length, '개')
      } else {
        console.log('imageInfos가 비어있음, 기존 방식 사용')
      }
    } catch (error) {
      console.error('imageInfos 로드 실패:', error)
    }

    // 기존 방식도 유지 (fallback용)
    const [adminImgs, sellerImgs] = await Promise.all([
      fetchAdminImagesByItemDetailsId(props.item.id),
      fetchSellerImagesByItemDetailsId(props.item.id)
    ])

    console.log('관리자 이미지들:', adminImgs)
    console.log('판매자 이미지들:', sellerImgs)

    adminImages.value = adminImgs
    sellerImages.value = sellerImgs
    currentImageIndex.value = 0 // 첫 번째 이미지로 리셋
  } catch (error) {
    console.error('이미지 로딩 실패:', error)
    // 실패해도 기존 이미지는 유지
  } finally {
    isLoadingImages.value = false
  }
}

// ---------- 생명주기 ----------
onMounted(() => {
  loadAllImages()
})

// item이 변경될 때마다 이미지 다시 로드
watch(() => props.item?.id, (newId) => {
  if (newId) {
    loadAllImages()
  }
})
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
  height: 470px;
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

.condition-badge.admin {
  background: #2563eb;
  /* 파란색 - 관리자 */
}

.condition-badge.seller {
  background: #059669;
  /* 초록색 - 판매자 */
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
  left: 50%;
  right: 0;
  bottom: 10px;
  transform: translateX(-50%);
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

/* 관리자 이미지 섹션 */
.admin-images-section {
  margin-bottom: 30px;
}

.admin-images-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(80px, 1fr));
  gap: 8px;
  margin-top: 12px;
}

.admin-image {
  width: 100%;
  height: 80px;
  object-fit: cover;
  border-radius: 8px;
  border: 2px solid #e5e7eb;
  cursor: pointer;
  transition: all 0.2s ease;
}

.admin-image:hover {
  border-color: #3b82f6;
  transform: scale(1.05);
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

/* ProductInfo와 동일한 위시리스트 하트 버튼 스타일 */
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
</style>
