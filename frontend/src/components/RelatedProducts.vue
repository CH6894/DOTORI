<template>
  <section class="related-section">
    <div class="section-container">
      <h3 class="section-title">함께 본 상품</h3>
      
      <div v-if="products.length === 0" class="empty">
        <p>현재 함께 본 상품이 없습니다. 함께 본 상품이 추가되면 여기에 표시됩니다.</p>
      </div>
      
      <div v-else class="products-slider-container">
        <div class="products-slider">
          <div 
            v-for="product in products"
            :key="product.id"
            class="product-card"
            @click="goToProduct(product.id)"
          >
            <div class="product-image-container">
              <img 
                :src="product.image || '/img/test.jpg'"
                :alt="product.title"
                class="product-image"
              />
            </div>
            
            <div class="product-info">
              <p class="product-maker">{{ product.maker || '브랜드스토어' }}</p>
              <h4 class="product-title">{{ product.title }}</h4>
              <p class="product-price">{{ product.price }}</p>
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
  products: {
    type: Array,
    default: () => []
  }
})

const goToProduct = (productId) => {
  // 상품 상세 페이지로 이동
  // this.$router.push(`/products/${productId}`)
  console.log('상품 이동:', productId)
  console.log('전체 상품 목록:', props.products)
}
</script>

<style scoped>
@import '@/styles/ProductSlider.css';

.related-section {
  padding: 10px 0;
}
.empty {
  grid-column: 1 / -1;
  text-align: center;
  padding: 2.25rem 0;                                /* 36px */
  color: #9a9a9a;
  border: 0.0625rem dashed #e5e5e5;                  /* 1px */
  border-radius: 0.75rem;                            /* 12px */
}
</style>