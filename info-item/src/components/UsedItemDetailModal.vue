<template>
  <div class="modal-overlay" @click="closeModal">
    <div class="modal-container" @click.stop>
      <!-- 모달 헤더 -->
      <div class="modal-header">
        <h2 class="modal-title">{{ item.title }}</h2>
        <button class="close-btn" @click="closeModal">&times;</button>
      </div>
      
      <!-- 모달 콘텐츠 -->
      <div class="modal-content">
        <div class="item-detail-container">
          <!-- 왼쪽: 이미지 갤러리 -->
          <div class="image-gallery">
            <div class="main-image-container">
              <img 
                :src="currentImage || '/images/placeholder.jpg'" 
                :alt="item.title"
                class="main-image"
              />
              <!-- 상태 배지 -->
              <div class="condition-badge" :class="item.condition">
                {{ getConditionText(item.condition) }}
              </div>
              
              <!-- 이미지 네비게이션 화살표 -->
              <button 
                v-if="item.images?.length > 1"
                class="nav-btn prev-btn" 
                @click="previousImage"
                :disabled="currentImageIndex === 0"
              >
                ‹
              </button>
              <button 
                v-if="item.images?.length > 1"
                class="nav-btn next-btn" 
                @click="nextImage"
                :disabled="currentImageIndex === item.images.length - 1"
              >
                ›
              </button>
              
              <!-- 이미지 인디케이터 -->
              <div v-if="item.images?.length > 1" class="image-indicators">
                <span 
                  v-for="(image, index) in item.images" 
                  :key="index"
                  class="indicator"
                  :class="{ active: index === currentImageIndex }"
                  @click="setCurrentImage(index)"
                ></span>
              </div>
            </div>
            
            <!-- 썸네일 이미지들 -->
            <div v-if="item.images?.length > 1" class="thumbnail-container">
              <img 
                v-for="(image, index) in item.images" 
                :key="index"
                :src="image || '/images/placeholder.jpg'"
                :alt="`${item.title} ${index + 1}`"
                class="thumbnail-image"
                :class="{ active: currentImage === image }"
                @click="setCurrentImage(index)"
              />
            </div>
          </div>
          
          <!-- 오른쪽: 상품 정보 -->
          <div class="item-info-section">
            <!-- 가격 정보 -->
            <div class="price-section">
              <div class="current-price">{{ formatPrice(item.price) }}원</div>
              <div v-if="item.originalPrice" class="original-price">
                정가: {{ formatPrice(item.originalPrice) }}원
              </div>
            </div>
            
            <!-- 상품 상태 및 정보 -->
            <div class="item-details">
              <div class="detail-row">
                <span class="label">상품 상태</span>
                <span class="value condition-text" :class="item.condition">
                  {{ getConditionText(item.condition) }}
                </span>
              </div>
              <div class="detail-row">
                <span class="label">상태 상세</span>
                <span class="value">{{ getConditionDetails(item.conditionDetails) }}</span>
              </div>
              
              <div class="detail-row">
                <span class="label">등록일</span>
                <span class="value">{{ formatDate(item.createdAt) }}</span>
              </div>
            </div>
            
            <!-- 상품 설명 -->
            <div class="description-section">
              <h4 class="section-title">상품 설명</h4>
              <p class="description-text">{{ item.description }}</p>
            </div>
            
            <!-- 액션 버튼들 -->
            <div class="action-buttons">
              <button class="heart-btn" @click="toggleWishlist">
                <span :class="['heart-icon', { active: isWishlisted }]">♡</span>
              </button>
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
</template>

<script setup>
/* eslint-disable no-undef, no-unused-vars */
import { ref, computed } from 'vue'

const props = defineProps({
  item: {
    type: Object,
    required: true
  },
  productInfo: {
    type: Object,
    default: () => ({})
  }
})

const emit = defineEmits(['close', 'purchase', 'addToCart'])

const currentImageIndex = ref(0)
const isWishlisted = ref(false)

// 현재 이미지 계산
const currentImage = computed(() => {
  if (props.item.images?.length > 0) {
    return props.item.images[currentImageIndex.value]
  }
  return '/images/placeholder.jpg'
})

// 이미지 네비게이션 함수들
const previousImage = () => {
  if (currentImageIndex.value > 0) {
    currentImageIndex.value--
  }
}

const nextImage = () => {
  if (currentImageIndex.value < props.item.images.length - 1) {
    currentImageIndex.value++
  }
}

const setCurrentImage = (index) => {
  if (index >= 0 && index < props.item.images.length) {
    currentImageIndex.value = index
  }
}

const closeModal = () => {
  emit('close')
}

const purchaseNow = () => {
  emit('purchase', props.item)
}

const addToCart = () => {
  emit('addToCart', props.item)
}

const toggleWishlist = () => {
  isWishlisted.value = !isWishlisted.value
  console.log('찜하기 토글:', props.item.id)
}

// 유틸리티 함수들
const getConditionText = (condition) => {
  const conditionMap = {
    'excellent': '최상',
    'good': '상',
    'fair': '중',
    'poor': '하'
  }
  return conditionMap[condition] || '미정'
}

const getConditionDetails = (details) => {
  return details || '상태 양호'
}

const formatPrice = (price) => {
  return price.toLocaleString()
}

const formatDate = (dateString) => {
  const date = new Date(dateString)
  const now = new Date()
  const diffTime = Math.abs(now - date)
  const diffDays = Math.floor(diffTime / (1000 * 60 * 60 * 24))
  
  if (diffDays === 0) return '오늘'
  if (diffDays === 1) return '어제'
  if (diffDays < 7) return `${diffDays}일 전`
  if (diffDays < 30) return `${Math.floor(diffDays / 7)}주 전`
  return `${Math.floor(diffDays / 30)}개월 전`
}
</script>

<style scoped>
@import '@/styles/InfoCommon.css';

.item-detail-container {
  display: flex;
  gap: 40px;
}

.image-gallery {
  flex: 1;
  max-width: 400px;
}

.item-info-section {
  flex:1;
  display: flex;
  flex-direction: column;
  min-width: 300px;
}

.price-section {
  margin-bottom: 30px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 12px;
}

.current-price {
  font-size: 28px;
  font-weight: bold;
  color: #ff6b35;
  margin-bottom: 8px;
}

.original-price {
  font-size: 16px;
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
  color: #333;
  margin: 0 0 10px 0;
  word-wrap: break-word;
}

.description-text {
  color: #666;
  line-height: 1.5;
  margin: 0;
}

.action-buttons {
 margin-top: auto;
}
</style>