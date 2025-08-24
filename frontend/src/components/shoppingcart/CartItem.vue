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
      <img class="thumb" :src="item.thumb" :alt="item.title" />
      <div class="meta">
        <p class="title">{{ item.title }}</p>
        <p class="unit-price">{{ currency(item.price) }}</p>
      </div>
    </td>

    <!-- 3) 수량 (± / 입력만, 수량변경 버튼 완전 제거) -->
    <td class="qty">
      <div class="qty-controls">
        <button class="qty-btn" @click="step(-1)" :disabled="tempQty <= 1">−</button>
        <input
          class="qty-input"
          type="text"
          v-model.trim="tempQtyStr"
          @input="sanitize"
          @keydown.enter.prevent="apply"
          @blur="apply"
          placeholder="1"
        />
        <button class="qty-btn" @click="step(1)">＋</button>
      </div>
    </td>

    <!-- 4) 상품 금액(= 단가 × 실제 수량) -->
    <td class="amount">{{ currency(item.price * item.qty) }}</td>

    <!-- 5) 할인/적립 -->
    <td class="discount">—</td>

    <!-- 6) 배송비 -->
    <td class="shipping">{{ item.shipping ? currency(item.shipping) : '무료' }}</td>

    <!-- 7) 관리 -->
    <td class="actions">
      <button type="button" class="btn danger" @click.stop="$emit('remove', item.id)">삭제</button>
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
  emits: ['update:modelValue', 'remove', 'apply-qty'],
  data() {
    return {
      tempQty: this.item.qty,
      tempQtyStr: String(this.item.qty),
    };
  },
  watch: {
    'item.qty'(n) {
      this.tempQty = n;
      this.tempQtyStr = String(n);
    },
  },
  methods: {
    currency(n) {
      return new Intl.NumberFormat('ko-KR', {
        style: 'currency',
        currency: 'KRW',
        maximumFractionDigits: 0,
      }).format(n);
    },
    sanitize() {
      // 입력 중에는 0도 허용하고, 최종 적용 시에만 1 이상으로 제한
      const cleaned = String(this.tempQtyStr).replace(/[^\d]/g, '');
      const n = parseInt(cleaned, 10);
      
      if (cleaned === '' || isNaN(n)) {
        this.tempQty = 1;
        this.tempQtyStr = '';
      } else {
        this.tempQty = n;
        this.tempQtyStr = cleaned;
      }
    },
    step(delta) {
      const next = Math.max(1, (parseInt(this.tempQty, 10) || 1) + delta);
      this.tempQty = next;
      this.tempQtyStr = String(next);
      this.apply(); // 즉시 적용
    },
    apply() {
      // 최종 적용 시에만 1 이상으로 제한
      const finalQty = Math.max(1, parseInt(this.tempQty, 10) || 1);
      if (finalQty !== this.item.qty) {
        this.tempQty = finalQty;
        this.tempQtyStr = String(finalQty);
        this.$emit('apply-qty', { id: this.item.id, qty: finalQty });
      }
    },
  },
};
</script>

<style scoped>
.cart-row { border-bottom: 1px solid #eee; }
.checkbox { text-align: center; }

/* 상품 정보 */
.product { display:flex; align-items:center; gap:14px; min-width:0; }
.thumb   { width:100px; height:100px; object-fit:cover; border-radius:8px; border:1px solid #eee; }
.meta .title { font-weight:600; white-space:nowrap; overflow:hidden; text-overflow:ellipsis; }
.unit-price  { color:#777; font-size:13px; margin-top:4px; }

/* 수량 - 컴팩트하게 정리 */
.qty { text-align: center; }
.qty-controls { 
  display: inline-flex; 
  align-items: center; 
  gap: 2px; 
  border: 1px solid #ddd; 
  border-radius: 8px; 
  overflow: hidden;
}

.qty-btn {
  width: 32px;
  height: 32px;
  padding: 0;
  border: none;
  background: #f8f9fa;
  cursor: pointer;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
}

.qty-btn:hover:not(:disabled) { background: #e9ecef; }
.qty-btn:disabled { opacity: 0.4; cursor: not-allowed; }

.qty-input {
  width: 50px; 
  height: 32px;
  text-align: center; 
  border: none; 
  outline: none;
  font-weight: 600;
  background: #fff;
}

/* 스핀 제거 */
.qty-input::-webkit-outer-spin-button,
.qty-input::-webkit-inner-spin-button { -webkit-appearance:none; margin:0; }
.qty-input[type="number"] { -moz-appearance:textfield; }

/* 숫자/금액/배송 정렬 */
.amount, .discount, .shipping { text-align:center; font-weight: 600; }

/* 관리(삭제) */
.actions { text-align:center; }

/* 버튼들 */
.btn { 
  padding: 6px 12px; 
  border: 1px solid #ddd; 
  background: #fff; 
  border-radius: 6px; 
  cursor: pointer; 
  font-weight: 600;
  font-size: 13px;
}

.btn.danger { 
  border-color: #ff6b6b; 
  color: #ff6b6b; 
  background: #fff;
}

.btn.danger:hover {
  background: #ff6b6b;
  color: #fff;
}
</style>