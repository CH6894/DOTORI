<template>
  <main class="mypage">
    <section class="container">
      <!-- 타이틀: 가운데 정렬, 박스 밖 -->
      <h1 class="section__title section__title--center">주문 내역</h1>

      <!-- 요약 박스 -->
      <div class="panel panel--center">
        <ul class="order-summary">
          <li class="order-summary__item">
            <span class="order-summary__label">전체</span>
            <span class="order-summary__num">{{ summary.total }}</span>
          </li>
          <li class="order-summary__item">
            <span class="order-summary__label">입금중</span>
            <span class="order-summary__num">{{ summary.deposit }}</span>
          </li>
          <li class="order-summary__item">
            <span class="order-summary__label">진행중</span>
            <span class="order-summary__num">{{ summary.progress }}</span>
          </li>
          <li class="order-summary__item">
            <span class="order-summary__label">종료</span>
            <span class="order-summary__num">{{ summary.done }}</span>
          </li>
        </ul>
      </div>

      <!-- 필터 및 검색 -->
      <div class="panel panel--center">
        <div class="filter-section">
          <div class="filter-group">
            <label class="filter-label">기간</label>
            <select v-model="selectedPeriod" class="filter-select">
              <option value="all">전체</option>
              <option value="3months">최근 3개월</option>
              <option value="6months">최근 6개월</option>
              <option value="1year">최근 1년</option>
            </select>
          </div>
          <div class="filter-group">
            <label class="filter-label">상태</label>
            <select v-model="selectedStatus" class="filter-select">
              <option value="all">전체</option>
              <option value="deposit">입금중</option>
              <option value="progress">진행중</option>
              <option value="done">종료</option>
            </select>
          </div>
          <div class="filter-group">
            <input 
              v-model="searchKeyword" 
              type="text" 
              placeholder="주문번호 또는 상품명 검색"
              class="search-input"
            />
          </div>
        </div>
      </div>

      <!-- 주문 목록 -->
      <div class="panel panel--center" v-if="filteredOrders.length">
        <div class="order-list">
          <div v-for="order in filteredOrders" :key="order.no" class="order-card">
            <div class="order-header">
              <div class="order-info">
                <span class="order-no">{{ order.no }}</span>
                <span class="order-date">{{ order.date }}</span>
              </div>
              <span class="badge" :data-type="order.state.type">{{ order.state.text }}</span>
            </div>
            
            <div class="order-items">
              <div v-for="item in order.items" :key="item.id" class="order-item">
                <img :src="item.image" :alt="item.name" class="item-image" />
                <div class="item-details">
                  <h4 class="item-name">{{ item.name }}</h4>
                  <p class="item-price">{{ item.price.toLocaleString() }}원 × {{ item.qty }}개</p>
                </div>
              </div>
            </div>
            
            <div class="order-footer">
              <div class="order-total">
                <span class="total-label">총 결제금액</span>
                <span class="total-amount">{{ calculateOrderTotal(order).toLocaleString() }}원</span>
              </div>
              <div class="order-actions">
                <button v-if="order.state.type === 'progress'" class="btn btn--sm btn--detail" @click="trackShipping(order.no)">배송조회</button>
                <button v-if="order.state.type === 'done'" class="btn btn--sm btn--cart" @click="addToCart(order)">재구매</button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 빈 상태 -->
      <div v-else class="panel panel--center">
        <div class="empty-state">
          <p class="empty-message">주문 내역이 없습니다.</p>
          <button class="btn btn--primary" @click="goShopping">쇼핑하러 가기</button>
        </div>
      </div>

    </section>
  </main>
</template>

<script>
import { useOrderStore } from '@/stores/orders'

export default {
  name: 'MyPageOrders',
  setup() {
    // Pinia store 사용
    const orderStore = useOrderStore()
    
    return {
      summary: orderStore.orderSummary,  // store에서 데이터 가져오기
      orders: orderStore.orders          // store에서 주문 목록도 가져오기
    }
  },
  data() {
    return {
      selectedPeriod: 'all',
      selectedStatus: 'all',
      searchKeyword: '',
      
      // summary, orders 제거 (setup에서 store로 가져옴)
    };
  },
  
  computed: {
    filteredOrders() {
      let filtered = this.orders;
      
      // 상태 필터링
      if (this.selectedStatus !== 'all') {
        filtered = filtered.filter(order => order.state.type === this.selectedStatus);
      }
      
      // 검색 키워드 필터링
      if (this.searchKeyword.trim()) {
        const keyword = this.searchKeyword.toLowerCase();
        filtered = filtered.filter(order => 
          order.no.toLowerCase().includes(keyword) ||
          order.items.some(item => item.name.toLowerCase().includes(keyword))
        );
      }
      
      return filtered;
    }
  },
  
  methods: {
    trackShipping() {
      this.$router.push('/mypage/ship');
    },
    
    async addToCart(order) {
      try {
        // 장바구니 스토어나 API 호출
        // const cartStore = useCartStore();
        
        // 주문의 모든 상품을 장바구니에 추가
        for (const item of order.items) {
          // cartStore.addItem({
          //   id: item.id,
          //   name: item.name,
          //   price: item.price,
          //   qty: item.qty,
          //   image: item.image
          // });
          
          console.log('장바구니에 추가:', item.name, '×', item.qty);
        }
        
        // 성공 메시지
        alert(`${order.items.length}개 상품이 장바구니에 담겼습니다.`);
        
        // 장바구니 페이지로 이동할지 묻기
        if (confirm('장바구니로 이동하시겠습니까?')) {
          this.$router.push('/cart');
        }
        
      } catch (error) {
        console.error('장바구니 추가 실패:', error);
        alert('장바구니 추가에 실패했습니다.');
      }
    },
    
    goShopping() {
      this.$router.push('/');
    },
    
    calculateOrderTotal(order) {
      if (!order.items) return 0;
      const subtotal = order.items.reduce((sum, item) => sum + item.price * item.qty, 0);
      return subtotal + (order.shippingFee || 0);
    }
  }
};
</script>

<style scoped>
/* 컨테이너는 마이페이지와 동일하게 가운데 정렬 */
.container { width: min(1160px, 92%); margin: 0 auto; padding-bottom: 120px; }

/* 타이틀 */
.section__title{ margin:22px 4px 12px; font-size:22px; font-weight:800; color:#2d251c; }
.section__title--center{ text-align:center; }

/* 패널(마이페이지 박스와 동일) */
.panel{
  background:#fff; border:1.5px solid #e9e4db; border-radius:12px; padding:20px;
  margin-bottom: 20px;
}
.panel--center{ max-width: 980px; margin: 0 auto 20px; }
:root { --line:#e9e4db; --primary:#fc703c; }

/* 요약 박스 */
.order-summary{
  margin:0; padding:20px 10px; border:1px solid var(--line);
  border-radius:8px; list-style:none; display:grid; grid-template-columns:repeat(4,1fr); background:#fff;
}
.order-summary__item{ text-align:center; padding:10px 0; }
.order-summary__label{ display:block; font-weight:800; color:#2d251c; margin-bottom:8px; font-size:14px; }
.order-summary__num{ display:inline-block; font-weight:800; font-size:18px; color:#000; }

/* 필터 섹션 */
.filter-section {
  display: flex;
  gap: 16px;
  align-items: flex-end;
  flex-wrap: wrap;
  margin-bottom: 8px;
}
.filter-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-width: 120px;
}
.filter-label {
  font-size: 13px;
  font-weight: 600;
  color: #666;
  white-space: nowrap;
}

.filter-select {
  padding: 10px 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  min-height: 38px;
}
.search-input {
  padding: 8px 16px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  height: 22px;
  width: 200px;
}





/* 주문 카드 */
.order-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.order-card {
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  overflow: hidden;
}
.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background: #fafafa;
  border-bottom: 1px solid #f0f0f0;
}
.order-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.order-no {
  font-weight: 700;
  color: #333;
}
.order-date {
  font-size: 13px;
  color: #666;
}

/* 주문 상품 */
.order-items {
  padding: 16px;
}
.order-item {
  display: flex;
  gap: 12px;
  align-items: center;
  padding: 8px 0;
}
.item-image {
  width: 50px;
  height: 50px;
  object-fit: cover;
  border-radius: 6px;
  border: 1px solid #eee;
}
.item-details {
  flex: 1;
}
.item-name {
  font-weight: 600;
  margin: 0 0 4px 0;
  color: #333;
}
.item-price {
  font-size: 13px;
  color: #666;
  margin: 0;
}

/* 주문 푸터 */
.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background: #fafafa;
  border-top: 1px solid #f0f0f0;
}
.order-total {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.total-label {
  font-size: 13px;
  color: #666;
}
.total-amount {
  font-weight: 700;
  font-size: 16px;
  color: #333;
}
.order-actions {
  display: flex;
  gap: 8px;
}

/* 상태 뱃지 */
.badge{ 
  padding:6px 12px; 
  border-radius:20px; 
  font-size:12px; 
  font-weight: 600;
  border:1px solid #e4d8c3; 
}
.badge[data-type="deposit"]{ background:#fff3cd; border-color:#ffeaa7; color:#856404; }
.badge[data-type="progress"]{ background:#d1ecf1; border-color:#bee5eb; color:#0c5460; }
.badge[data-type="done"]{ background:#d4edda; border-color:#c3e6cb; color:#155724; }

/* 버튼 */
.btn {
  padding: 8px 16px;
  border: 1px solid #ddd;
  background: #fff;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 600;
  font-size: 13px;
  white-space: nowrap;
  transition: all 0.2s ease;
}
.btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}
.btn--sm {
  padding: 6px 12px;
  font-size: 12px;
}
.btn--ghost {
  background: transparent;
  color: #666;
}
.btn--primary {
  background: var(--primary);
  color: #fff;
  border-color: var(--primary);
}
.btn--primary:hover {
  background: #e55a2b;
  border-color: #e55a2b;
}
.btn--detail {
  background: #f4f3e6;
  color: #7b6d5d;
  border-color: #e8e1d4;
}
.btn--detail:hover {
  background: #ede9d8;
  border-color: #d9d2c1;
  color: #6b5d4d;
}
.btn--cart {
  background: #f4f4f4;
  color: #7b6d5d;
  border-color: #e8e1d4;
}
.btn--cart:hover {
  background: #ebe9e4;
  border-color: #d9d2c1;
  color: #6b5d4d;
}

/* 빈 상태 */
.empty-state {
  text-align: center;
  padding: 40px 20px;
}
.empty-message {
  margin: 0 0 20px 0;
  color: #666;
  font-size: 16px;
}

/* 모바일 대응 */
@media (max-width: 640px){
  .order-summary{ grid-template-columns: repeat(2, 1fr); row-gap: 8px; }
  .panel--center{ max-width: 100%; }
  .filter-section { flex-direction: column; align-items: stretch; }
  .search-input { min-width: auto; }
  .order-header, .order-footer { flex-direction: column; gap: 12px; align-items: stretch; }
  .order-actions { justify-content: center; }
}
</style>