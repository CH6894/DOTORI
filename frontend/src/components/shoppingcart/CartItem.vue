<template>
  <tr class="cart-row">
    <!-- 1) 체크박스 -->
    <td class="w-40 checkbox">
      <input
        type="checkbox"
        :checked="modelValue"
        @change="$emit('update:modelValue', $event.target.checked)"
      />
    </td>

    <!-- 2) 상품 정보 -->
    <td class="product">
      <img 
        class="thumb" 
        :src="item.mainImageUrl || item.thumb || item.imageUrl || '/default-product.jpg'" 
        :alt="item.itemName || item.title || '상품'" 
        @error="handleImageError"
      />
      <div class="meta">
        <!-- 상품명과 배지 -->
        <div class="title-container">
          <!-- 배지 시스템: unpacked 상태에 따라 구분 -->
          <span v-if="item.unpacked === true" class="used-badge">중고</span>
          <span v-else-if="item.unpacked === false" class="new-badge">미개봉</span>
          <span class="product-name">{{ item.itemName || item.title || '상품명 없음' }}</span>
        </div>
        <p class="unit-price">{{ currency(item.price) }}</p>
      </div>
    </td>

    <!-- 3) 수량 (항상 1 고정) -->
    <td class="qty">
      <span class="fixed-qty">1</span>
    </td>

    <!-- 4) 상품 금액 -->
    <td class="amount">{{ currency(item.price) }}</td>

    <!-- 5) 할인/적립 -->
    <td class="discount">—</td>

    <!-- 6) 배송비 -->
    <td class="shipping">
      {{ item.shipping ? currency(item.shipping) : '무료' }}
    </td>

    <!-- 7) 관리 -->
    <td class="actions">
      <button 
        type="button" 
        class="btn danger" 
        @click.stop="handleRemove"
      >
        삭제
      </button>
    </td>
  </tr>
</template>

<script>
export default {
  name: 'CartItem',
  props: {
    item: { type: Object, required: true },
    modelValue: { type: Boolean, default: true },
  },
  emits: ['update:modelValue', 'remove'],
  
  methods: {
    currency(n) {
      return new Intl.NumberFormat('ko-KR', {
        style: 'currency',
        currency: 'KRW',
        maximumFractionDigits: 0,
      }).format(n || 0);
    },
    handleRemove() {
      const itemId = this.item.cartId || this.item.id;
      this.$emit('remove', itemId);
    },
    handleImageError(event) {
      event.target.src = '/default-product.jpg';
    },
  },
};
</script>

<style scoped>
.cart-row { 
  border-bottom: 1px solid #eee; 
  transition: background-color 0.2s ease;
}
.cart-row:hover { background-color: #f9f9f9; }

.checkbox { text-align: center; }

/* 상품 정보 */
.product { 
  display: flex; 
  align-items: center; 
  gap: 14px; 
  min-width: 0; 
}
.thumb { 
  width: 100px; height: 100px; 
  object-fit: cover; 
  border-radius: 8px; 
  border: 1px solid #eee; 
  flex-shrink: 0;
}
.meta { flex: 1; min-width: 0; }

.title-container {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.product-name {
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  flex: 1;
}

.unit-price { color: #777; font-size: 13px; margin: 0; }

/* 배지 스타일 */
.used-badge {
  display: inline-block;
  background: #ff7a2e;
  color: #fff;
  font-size: 11px;
  font-weight: 700;
  padding: 2px 6px;
  border-radius: 4px;
  vertical-align: middle;
  flex-shrink: 0;
}

.new-badge {
  display: inline-block;
  background: #22c55e;
  color: #fff;
  font-size: 11px;
  font-weight: 700;
  padding: 2px 6px;
  border-radius: 4px;
  vertical-align: middle;
  flex-shrink: 0;
}

/* 수량 (고정 1) */
.qty { text-align: center; }
.fixed-qty {
  font-weight: 600;
  color: #333;
}

/* 금액/배송 정렬 */
.amount, .discount, .shipping { 
  text-align: center; 
  font-weight: 600; 
}
.amount { font-size: 15px; }

/* 삭제 버튼 */
.actions { text-align: center; }
.btn { padding: 6px 12px; border: 1px solid #ddd; border-radius: 6px; cursor: pointer; font-weight: 600; font-size: 13px; }
.btn.danger { border-color: #ff6b6b; color: #ff6b6b; }
.btn.danger:hover { background: #ff6b6b; color: #fff; }
</style>