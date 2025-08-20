<template>
  <section class="product-info-section">
    <div class="product-info-container">
      <!-- 왼쪽: 상품 이미지 갤러리 -->
      <div class="product-image-area">
        <!-- 메인 이미지 -->
        <div class="main-image-container">
          <img 
            :src="currentMainImage || '/img/placeholder.jpg'" 
            :alt="product.title" 
            class="main-image"
          />
          
          <!-- 이미지 네비게이션 화살표 -->
          <button 
            v-if="product.images?.length > 1"
            class="nav-btn prev-btn" 
            @click="previousImage"
            :disabled="currentImageIndex === 0"
          >
            ‹
          </button>
          <button 
            v-if="product.images?.length > 1"
            class="nav-btn next-btn" 
            @click="nextImage"
            :disabled="currentImageIndex === product.images.length - 1"
          >
            ›
          </button>
          
          <!-- 이미지 인디케이터 -->
          <div v-if="product.images?.length > 1" class="image-indicators">
            <span 
              v-for="(image, index) in product.images" 
              :key="index"
              class="indicator"
              :class="{ active: index === currentImageIndex }"
              @click="setCurrentImage(index)"
            ></span>
          </div>
        </div>
        
        <!-- 썸네일 이미지들 -->
        <div v-if="product.images?.length > 1" class="thumbnail-gallery">
          <img 
            v-for="(image, index) in product.images" 
            :key="index"
            :src="image || '/img/placeholder.jpg'"
            :alt="`${product.title} ${index + 1}`"
            class="thumbnail-image"
            :class="{ active: index === currentImageIndex }"
            @click="setCurrentImage(index)"
          />
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
          <button class="purchase-btn" @click="handlePurchase">구매</button>
        </div>
        
        <div class="cart-section">
          <button class="cart-btn" @click="handleAddToCart">
            장바구니
          </button>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup>
/* eslint-disable no-undef */
import { ref, computed } from 'vue'

const props = defineProps({
  product: {
    type: Object,
    default: () => ({})
  }
})

const emit = defineEmits(['purchase', 'addToCart'])

const isLiked = ref(false)
const currentImageIndex = ref(0)

// 현재 메인 이미지 계산
const currentMainImage = computed(() => {
  if (props.product.images?.length > 0) {
    return props.product.images[currentImageIndex.value]
  }
  return '/img/placeholder.jpg'
})

// 이미지 네비게이션
const previousImage = () => {
  if (currentImageIndex.value > 0) {
    currentImageIndex.value--
  }
}

const nextImage = () => {
  if (currentImageIndex.value < props.product.images.length - 1) {
    currentImageIndex.value++
  }
}

const setCurrentImage = (index) => {
  currentImageIndex.value = index
}

const toggleLike = () => {
  isLiked.value = !isLiked.value
}

const handleSell = () => {
  console.log('판매 버튼 클릭')
}

const handlePurchase = () => {
  emit('purchase', props.product)
}

const handleAddToCart = () => {
  emit('addToCart', props.product)
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
  margin-bottom: 30px; /* 35px → 30px로 축소 */
}

.brand-info,
.series-info {
  display: flex;
  margin-bottom: 12px; /* 15px → 12px로 축소 */
}

.brand-info .label,
.series-info .label {
  width: 70px; /* 80px → 70px로 축소 */
  color: #666;
  font-weight: 500;
  font-size: 13px; /* 14px → 13px로 축소 */
}

.brand-info .value,
.series-info .value {
  color: #333;
  font-weight: 600;
  font-size: 13px; /* 14px → 13px로 축소 */
}

.price-info {
  background: #f8f9fa;
  padding: 20px; /* 25px → 20px로 축소 */
  border-radius: 12px;
}

.original-price,
.current-price {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.original-price {
  margin-bottom: 12px; /* 15px → 12px로 축소 */
  color: #666;
  font-size: 14px; /* 16px → 14px로 축소 */
}

.current-price {
  font-size: 18px; /* 20px → 18px로 축소 */
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
  padding: 12px 0; /* 15px → 12px로 축소 */
  border-bottom: 1px solid #f0f0f0;
}

.option-label {
  color: #666;
  font-weight: 500;
  font-size: 14px; /* 16px → 14px로 축소 */
}

.option-value {
  color: #333;
  font-weight: 600;
  font-size: 14px; /* 16px → 14px로 축소 */
}

.action-buttons {
  margin-bottom: 10px;
  gap: 8px;
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