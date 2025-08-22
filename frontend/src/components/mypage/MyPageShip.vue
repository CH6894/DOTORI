<template>
  <main class="mypage">
    <section class="container">
      <h1 class="section__title section__title--center">주문 배송 현황</h1>

      <!-- 주문 탭 -->
      <div class="panel panel--center">
        <div class="order-tabs">
          <div class="tabs-header">
            <button
              v-for="order in userOrders"
              :key="order.id"
              :class="['tab-button', { 'is-active': selectedOrderId === order.id }]"
              @click="selectOrder(order.id)"
            >
              <div class="tab-info">
                <span class="tab-order-no">{{ order.no || order.orderNo }}</span>
                <span class="tab-date">{{ order.date }}</span>
                <span class="tab-amount">{{ calculateOrderTotal(order).toLocaleString() }}원</span>
              </div>
              <div class="tab-status" :class="'status-' + order.progress">
                {{ getStatusLabel(order.progress) }}
              </div>
            </button>
          </div>
        </div>
      </div>

      <!-- 주문 기본 정보 요약 -->
      <div class="panel panel--center">
        <div class="order-summary">
          <div class="summary-header">
            <h2 class="order-number">{{ currentOrder.orderNo }}</h2>
            <span class="order-date">{{ currentOrder.orderDate }}</span>
          </div>
          <div class="summary-details">
            <div class="detail-item">
              <span class="label">총 상품</span>
              <span class="value">{{ totalItems }}개</span>
            </div>
            <div class="detail-item">
              <span class="label">총 금액</span>
              <span class="value total-amount">{{ total.toLocaleString() }}원</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 통합 배송 진행 현황 -->
      <div class="panel panel--center">
        <h3 class="panel-title">배송 진행 현황</h3>
        <div class="delivery-progress">
          <!-- 진행 스텝 바 -->
          <ol class="steps steps--flat">
            <li v-for="(s, i) in steps" :key="s.key" class="step" :class="{ 'is-done': i <= currentOrder.progress }">
              <img :src="s.icon" :alt="s.label" class="step__icon" />
              <div v-if="i < steps.length - 1" class="step__arrow" :class="{ 'is-on': i < currentOrder.progress }" aria-hidden="true">›</div>
              <p class="step__label">{{ s.label }}</p>
            </li>
          </ol>
          
          <!-- 상세 추적 정보 -->
          <div class="tracking-timeline" v-if="currentOrder.trackingInfo && currentOrder.trackingInfo.length">
            <h4 class="tracking-title">상세 추적</h4>
            <div v-for="track in currentOrder.trackingInfo" :key="track.id" class="timeline-item" :class="{ 'is-current': track.isCurrent }">
              <div class="timeline-marker"></div>
              <div class="timeline-content">
                <div class="timeline-time">
                  <span class="date">{{ track.date }}</span>
                  <span class="time">{{ track.time }}</span>
                </div>
                <div class="timeline-status">
                  <h5 class="status-title">{{ track.status }}</h5>
                  <p class="status-detail">{{ track.detail }}</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 배송 정보 -->
      <div class="panel panel--center">
        <h3 class="panel-title">배송 정보</h3>
        <div class="shipping-info">
          <div class="info-grid">
            <div class="info-item">
              <span class="info-label">받는 사람</span>
              <span class="info-value">{{ currentOrder.recipient }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">연락처</span>
              <span class="info-value">{{ currentOrder.phone }}</span>
            </div>
            <div class="info-item address">
              <span class="info-label">배송지</span>
              <span class="info-value">{{ currentOrder.address }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 주문 상품 목록 (테이블 형태로 개선) -->
      <div class="panel panel--center">
        <h3 class="panel-title">주문 상품 목록</h3>
        <div class="products-table">
          <div class="table-header">
            <div class="col-product">상품</div>
            <div class="col-price">단가</div>
            <div class="col-qty">수량</div>
            <div class="col-total">합계</div>
          </div>
          <div class="table-body">
            <div v-for="item in currentOrder.items" :key="item.id" class="table-row">
              <div class="col-product">
                <img :src="item.image" :alt="item.title" class="product-thumb" />
                <span class="product-name">{{ item.title }}</span>
              </div>
              <div class="col-price">{{ item.price.toLocaleString() }}원</div>
              <div class="col-qty">{{ item.qty }}개</div>
              <div class="col-total">{{ (item.price * item.qty).toLocaleString() }}원</div>
            </div>
          </div>
          
          <!-- 결제 요약 -->
          <div class="payment-summary">
            <div class="summary-row">
              <span>상품 금액</span>
              <span>{{ subtotal.toLocaleString() }}원</span>
            </div>
            <div class="summary-row">
              <span>배송비</span>
              <span>{{ currentOrder.shippingFee === 0 ? '무료' : currentOrder.shippingFee.toLocaleString() + '원' }}</span>
            </div>
            <div class="summary-row total">
              <span>총 결제 금액</span>
              <span>{{ total.toLocaleString() }}원</span>
            </div>
          </div>
        </div>
      </div>



    </section>
  </main>
</template>

<script>
import { useOrderStore } from '@/stores/orders'
import iconOrder from '@/assets/주문.png'
import iconPay   from '@/assets/결제.png'
import iconPrep  from '@/assets/준비.png'
import iconShip  from '@/assets/배송.png'
import iconDone  from '@/assets/완료.png'

export default {
  name: 'MyPageShip',
  data() {
    return {
      selectedOrderId: 'order1', // 기본 선택된 주문
      orderStore: null,
      
      steps: [
        { key: 'order',   label: '주문접수',  icon: iconOrder },
        { key: 'payment', label: '결제 완료',  icon: iconPay },
        { key: 'prep',    label: '상품 준비',  icon: iconPrep },
        { key: 'ship',    label: '배송중',    icon: iconShip },
        { key: 'done',    label: '배송 완료',  icon: iconDone },
      ]
    };
  },
  
  computed: {
    userOrders() {
      return this.orderStore?.userOrders || [];
    },
    loading() {
      return this.orderStore?.loading || false;
    },
    currentOrder() {
      if (!this.orderStore) return {};
      const order = this.orderStore.getOrderById(this.selectedOrderId);
      return order || this.userOrders[0] || {};
    },
    subtotal() {
      if (!this.currentOrder.items) return 0;
      return this.currentOrder.items.reduce((sum, item) => sum + item.price * item.qty, 0);
    },
    total() {
      return this.subtotal + (this.currentOrder.shippingFee || 0);
    },
    totalItems() {
      if (!this.currentOrder.items) return 0;
      return this.currentOrder.items.reduce((sum, item) => sum + item.qty, 0);
    }
  },
  
  async created() {
    // Pinia store 초기화
    this.orderStore = useOrderStore();
    
    // 컴포넌트 생성 시 주문 목록 로드
    await this.orderStore.fetchUserOrders();
    
    // URL 파라미터에서 주문 ID 가져오기 (있다면)
    if (this.$route && this.$route.params && this.$route.params.orderId) {
      this.selectedOrderId = this.$route.params.orderId;
    } else if (this.userOrders.length > 0) {
      this.selectedOrderId = this.userOrders[0].id;
    }
  },
  
  methods: {
    async selectOrder(orderId) {
      this.selectedOrderId = orderId;
      
      // 선택된 주문의 상세 정보 로드 (필요한 경우)
      if (this.orderStore) {
        await this.orderStore.fetchOrderDetail(orderId);
      }
      
      console.log('주문 변경:', orderId);
    },
    
    getStatusLabel(progress) {
      const labels = ['주문접수', '결제완료', '상품준비', '배송중', '배송완료'];
      return labels[progress] || '주문접수';
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
.container { width:min(1160px,92%); margin:0 auto; padding-bottom:40px; }
.section__title { margin:22px 4px 12px; font-size:22px; font-weight:800; color:#2d251c; }
.section__title--center { text-align:center; }

.panel { 
  background:#fff; 
  border:1.5px solid #e9e4db; 
  border-radius:12px; 
  padding:24px; 
  margin-bottom:20px;
}
.panel--center { max-width:980px; margin:0 auto 20px; }
.panel-title { 
  margin:0 0 20px; 
  font-size:16px; 
  font-weight:700; 
  color:#2d251c; 
  border-bottom:2px solid #f0f0f0; 
  padding-bottom:8px;
}

/* 주문 탭 */
.order-tabs {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 8px;
}

.tabs-header {
  display: flex;
  gap: 8px;
  overflow-x: auto;
  scrollbar-width: thin;
}

.tab-button {
  flex: 1;
  min-width: 200px;
  padding: 16px 20px;
  background: white;
  border: 2px solid #e9e4db;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;
  text-align: left;
}

.tab-button:hover {
  border-color: #fc703c;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.tab-button.is-active {
  border-color: #fc703c;
  background: #fff5f0;
  box-shadow: 0 2px 12px rgba(252, 112, 60, 0.15);
}

.tab-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-bottom: 8px;
}

.tab-order-no {
  font-weight: 700;
  font-size: 14px;
  color: #2d251c;
}

.tab-date {
  font-size: 12px;
  color: #7b6d5d;
}

.tab-amount {
  font-weight: 600;
  font-size: 14px;
  color: #fc703c;
}

.tab-status {
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 600;
  text-align: center;
  display: inline-block;
  min-width: 60px;
}

.tab-status.status-0 { background: #f0f0f0; color: #666; }
.tab-status.status-1 { background: #e3f2fd; color: #1976d2; }
.tab-status.status-2 { background: #fff3e0; color: #f57c00; }
.tab-status.status-3 { background: #e8f5e8; color: #388e3c; }
.tab-status.status-4 { background: #e8f5e8; color: #2e7d32; }

/* 주문 요약 */
.order-summary {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 20px;
}
.summary-header .order-number {
  font-size: 18px;
  font-weight: 800;
  color: #2d251c;
  margin: 0 0 4px 0;
}
.summary-header .order-date {
  font-size: 14px;
  color: #7b6d5d;
}
.summary-details {
  display: flex;
  gap: 24px;
}
.detail-item {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;
}
.detail-item .label {
  font-size: 12px;
  color: #7b6d5d;
}
.detail-item .value {
  font-size: 16px;
  font-weight: 600;
  color: #2d251c;
}
.total-amount {
  color: #fc703c !important;
  font-size: 18px !important;
  font-weight: 800 !important;
}

/* 통합 배송 진행 현황 */
.delivery-progress {
  display: flex;
  flex-direction: column;
  gap: 32px;
}

.tracking-title {
  margin: 0 0 16px 0;
  font-size: 14px;
  font-weight: 600;
  color: #666;
  padding-left: 20px;
  border-left: 3px solid #fc703c;
}

/* 진행 스텝 */
:root { --line:#e9e4db; --green:#38b36f; }
.steps--flat {
  margin:0; 
  padding:20px 10px; 
  border:1px solid var(--line);
  border-radius:8px; 
  background:#f9f9f9; 
  display:grid; 
  grid-template-columns:repeat(5,1fr); 
  align-items:center;
}
.step { 
  position:relative; 
  display:grid; 
  grid-template-rows:auto auto; 
  justify-items:center; 
  padding:10px 0; 
}
.step__arrow { 
  position:absolute; 
  top:20px; 
  right:-8px; 
  color:#d9d4cc; 
  font-size:22px; 
  line-height:22px; 
  transition:color .2s; 
}
.step__arrow.is-on { color:var(--green); }
.step__label { 
  margin-top:12px; 
  font-size:13px; 
  color:#8f8577; 
  text-align:center; 
}
.step.is-done .step__label { color:#5c5246; font-weight:700; }
.step__icon {
  width: 40px;
  height: 40px;
  object-fit: contain;
  filter: grayscale(100%) opacity(0.6); 
  transition: filter .2s ease;
}
.step.is-done .step__icon {
  filter: none;
}

.products-table {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
}
.table-header {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr 1fr;
  gap: 16px;
  padding: 16px;
  background: #f8f9fa;
  border-bottom: 2px solid #e9e4db;
  font-weight: 700;
  font-size: 14px;
  color: #2d251c;
}
.table-body {
  border-bottom: 1px solid #f0f0f0;
}
.table-row {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr 1fr;
  gap: 16px;
  padding: 16px;
  border-bottom: 1px solid #f5f5f5;
  align-items: center;
}
.table-row:last-child {
  border-bottom: none;
}

.col-product {
  display: flex;
  align-items: center;
  gap: 12px;
}
.product-thumb {
  width: 50px;
  height: 50px;
  object-fit: cover;
  border-radius: 6px;
  border: 1px solid #e9e4db;
  flex-shrink: 0;
}
.product-name {
  font-weight: 600;
  color: #2d251c;
  line-height: 1.3;
}
.col-price, .col-qty, .col-total {
  text-align: center;
  font-weight: 600;
  color: #333;
}

/* 결제 요약 */
.payment-summary {
  padding: 20px;
  background: #f8f9fa;
  border-top: 2px solid #e9e4db;
}
.summary-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  font-size: 14px;
}
.summary-row.total {
  font-weight: 800;
  font-size: 16px;
  color: #2d251c;
  border-top: 1px solid #ddd;
  padding-top: 12px;
  margin-top: 8px;
}

/* 배송 정보 */
.shipping-info {
  background: #f8f9fa;
  padding: 20px;
  border-radius: 8px;
}
.info-grid {
  display: grid;
  gap: 16px;
}
.info-item {
  display: flex;
  align-items: flex-start;
  gap: 20px;
}
.info-item.address {
  align-items: flex-start;
}
.info-label {
  font-weight: 600;
  color: #666;
  min-width: 80px;
  flex-shrink: 0;
}
.info-value {
  color: #333;
  font-weight: 500;
  line-height: 1.4;
}

/* 배송 추적 타임라인 */
.tracking-timeline {
  position: relative;
  padding-left: 20px;
}
.tracking-timeline::before {
  content: '';
  position: absolute;
  left: 6px;
  top: 0;
  bottom: 0;
  width: 2px;
  background: #e9e4db;
}

.timeline-item {
  position: relative;
  padding-bottom: 24px;
  margin-left: 20px;
}
.timeline-item:last-child {
  padding-bottom: 0;
}

.timeline-marker {
  position: absolute;
  left: -26px;
  top: 4px;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: #ddd;
  border: 2px solid #fff;
}
.timeline-item.is-current .timeline-marker {
  background: var(--green);
}

.timeline-content {
  display: flex;
  gap: 16px;
  align-items: flex-start;
}
.timeline-time {
  display: flex;
  flex-direction: column;
  min-width: 80px;
  text-align: right;
  flex-shrink: 0;
}
.date {
  font-size: 13px;
  color: #666;
  font-weight: 600;
}
.time {
  font-size: 12px;
  color: #999;
}
.timeline-status {
  flex: 1;
}
.status-title {
  font-weight: 600;
  margin: 0 0 4px 0;
  color: #2d251c;
}
.status-detail {
  font-size: 14px;
  color: #666;
  margin: 0;
  line-height: 1.4;
}

@media (max-width: 1024px) {
  .tabs-header {
    overflow-x: auto;
    padding-bottom: 4px;
  }
  .tab-button {
    min-width: 180px;
  }
}

@media (max-width:768px) {
  .tabs-header {
    flex-direction: column;
  }
  .tab-button {
    min-width: auto;
  }
  .delivery-progress {
    gap: 24px;
  }
  .order-summary {
    flex-direction: column;
    gap: 16px;
  }
  .summary-details {
    align-self: stretch;
    justify-content: space-between;
  }
  .table-header, .table-row {
    grid-template-columns: 1fr;
    gap: 8px;
  }
  .col-product {
    justify-content: flex-start;
  }
  .col-price, .col-qty, .col-total {
    text-align: left;
  }
  .steps--flat { grid-template-columns:repeat(5, minmax(44px,1fr)); }
  .timeline-content {
    flex-direction: column;
    gap: 8px;
  }
  .timeline-time {
    text-align: left;
  }
}
</style>