<template>
  <section class="used-products-section">
    <div class="section-container">
      <h3 class="section-title">중고상품</h3>
      
      <!-- 디버깅 정보 -->
      <div v-if="usedItems.length === 0" class="debug-info">
        <p>중고상품이 없습니다. usedItems: {{ usedItems }}</p>
      </div>
      
      <div class="products-slider-container">
        <div class="products-slider">
          <!-- 각각의 개별 중고상품 카드 -->
          <div 
            v-for="item in usedItems" 
            :key="item.id"
            class="used-product-card"
            @click="openUsedItemDetail(item)"
          >
            <div class="product-image-container">
              <img 
                :src="item.images?.[0] || '/img/placeholder.jpg'" 
                :alt="`중고상품 - #${item.id}`"
                class="product-image"
              />
              <!-- 등급 배지 -->
              <div class="grade-badge" :class="getGradeClass(item.quality)">
                {{ getGradeText(item.quality) }}
              </div>
            </div>
            
            <div class="product-info">
              <p class="item-price">{{ formatPrice(item.price) }}원</p>
              <p class="condition-desc">{{ item.conditionDetails || getConditionDescription(item.condition) }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup>
/* eslint-disable no-undef */

const props = defineProps({
  usedItems: {
    type: Array,
    default: () => []
  },
  productInfo: {
    type: Object,
    default: () => ({})
  }
})

const emit = defineEmits(['openUsedItemDetail'])

// 유틸리티 함수들
const getGradeText = (quality) => {
  const map = { 1: 'S', 2: 'A', 3: 'B', 4: 'C' }
  return map[quality] || '미정'
}

const getGradeClass = (quality) => {
  const map = { 1: 'badge-grade--s', 2: 'badge-grade--a', 3: 'badge-grade--b', 4: 'badge-grade--c' }
  return map[quality] || 'badge-grade--none'
}

const getConditionText = (condition) => {
  const conditionMap = {
    'excellent': '최상',
    'good': '상', 
    'fair': '중',
    'poor': '하'
  }
  return conditionMap[condition] || '미정'
}

const getConditionDescription = (condition) => {
  const descriptions = {
    excellent: '거의 새상품 수준',
    good: '사용감 적음',
    fair: '일반적인 사용감',
    poor: '사용감 많음'
  }
  return descriptions[condition] || ''
}

const formatPrice = (price) => {
  return price.toLocaleString()
}

const openUsedItemDetail = (item) => {
  // 개별 중고상품 상세 모달로 바로 연결
  emit('openUsedItemDetail', item)
  console.log('상품 정보:', props.productInfo) // props 사용
  console.log('중고상품 아이템:', item) // 디버깅용
  console.log('아이템 quality:', item.quality) // quality 값 확인
}
</script>

<style scoped>
.used-products-section {
  padding: 10px 0;
  background: white;
}

.section-container {
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 20px;
}

.section-title {
  font-size: 28px;
  font-weight: bold;
  color: #333;
  margin-bottom: 40px;
  text-align: left;
}

.products-slider-container {
  position: relative;
  overflow: hidden;
}

.products-slider {
  display: flex;
  gap: 16px;
  overflow-x: auto;
  scroll-behavior: smooth;
  padding: 10px 0 20px 0;
  scrollbar-width: thin;
  scrollbar-color: #ddd transparent;
}

.products-slider::-webkit-scrollbar {
  height: 6px;
}

.products-slider::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.products-slider::-webkit-scrollbar-thumb {
  background: #ddd;
  border-radius: 3px;
}

.products-slider::-webkit-scrollbar-thumb:hover {
  background: #bbb;
}

.used-product-card {
  background: white;
  border-radius: 2px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid #f0f0f0;
  flex-shrink: 0;
  width: 180px;
}

.used-product-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
  border-color: #FC703C;
}

.product-image-container {
  position: relative;
  width: 100%;
  aspect-ratio: 1;
  overflow: hidden;
  background: #f8f9fa;
}

.product-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.used-product-card:hover .product-image {
  transform: scale(1.05);
}

.grade-badge {
  position: absolute;
  top: 8px;
  left: 8px;
  padding: 4px 8px;
  border-radius: 6px;
  font-size: 11px;
  font-weight: bold;
  color: white;
}

/* 등급 배지 스타일 - 모달과 동일 */
.badge-grade--s {
  background: linear-gradient(45deg, #9333ea, #f43f5e);
}

.badge-grade--a {
  background: #2563eb;
}

.badge-grade--b {
  background: #16a34a;
}

.badge-grade--c {
  background: #f59e0b;
}

.badge-grade--none {
  background: #9ca3af;
}

.product-info {
  padding: 12px;
}

.item-price {
  font-size: 16px;
  font-weight: bold;
  color: #17a2b8;
  margin: 0 0 6px 0;
}

.condition-desc {
  font-size: 12px; 
  color: #666;
  margin: 0;
  line-height: 1.3;
}
</style>