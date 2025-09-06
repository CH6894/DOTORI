<!-- src/views/checkout/OrderComplete.vue -->
<template>
  <div class="order-complete">
    <!-- 상단 아이콘 + 완료 문구 -->
    <div class="hero">
      <img class="hero__icon" :src="iconSrc" alt="주문 완료 아이콘" />
      <h1 class="hero__title">주문이 완료되었습니다.</h1>
      <p class="hero__meta">
        주문번호 : <span class="order-no">{{ orderNo }}</span>
      </p>
    </div>

    <!-- 주문 상품 -->
    <section class="card wide" v-if="items.length">
      <h2 class="section-title">주문 상품</h2>
      <div class="thead row">
        <div>상품 이미지</div>
        <div>상품 정보</div>
        <div>수량</div>
        <div>결제 금액</div>
      </div>
      <div class="tbody">
        <div class="row" v-for="it in items" :key="it.orderId">
          <div class="thumb">
            <img :src="it.mainImageUrl || '/default-product.jpg'" alt="상품 이미지" />
          </div>
          <div class="info">
            <div class="name">{{ it.itemName }}</div>
          </div>
          <div>{{ it.quantity }}</div>
          <div class="right"><b>{{ fmt(it.price * (it.quantity || 1)) }}원</b></div>
        </div>
      </div>
    </section>

    <!-- 주문 없음 안내 -->
    <p v-else class="muted">주문 내역을 찾을 수 없습니다.</p>

    <!-- 배송/결제 정보 -->
    <section class="card wide" v-if="items.length">
      <div class="info-grid">
        <div class="panel">
          <h3 class="section-title">배송지 정보</h3>
          <div class="dl">
            <div class="dt">이름</div>
            <div class="dd">{{ ship.receiver || '정보 없음' }}</div>
            <div class="dt">휴대폰 번호</div>
            <div class="dd">{{ ship.phone || '정보 없음' }}</div>
            <div class="dt">주소</div>
            <div class="dd">
              <span v-if="ship.postcode">[{{ ship.postcode }}]</span>
              {{ ship.addr1 || ship.mainAddress || '주소 정보 없음' }}
              <br v-if="ship.addr2" />
              {{ ship.addr2 }}
            </div>
          </div>
        </div>

        <div class="panel">
          <h3 class="section-title">결제 정보</h3>
          <div class="dl">
            <div class="dt">상품 금액</div>
            <div class="dd">{{ fmt(subtotal) }}원</div>
            <div class="dt">배송비</div>
            <div class="dd">{{ shippingFee === 0 ? '무료' : fmt(shippingFee) + '원' }}</div>
            <div class="dt total">총 결제 금액</div>
            <div class="dd total"><b>{{ fmt(total) }}원</b></div>
          </div>
        </div>
      </div>
    </section>

    <!-- 하단 액션 -->
    <div class="actions" v-if="items.length">
      <router-link class="btn outline" :to="{ name: 'mypage-trade' }">주문 내역 보기</router-link>
      <router-link class="btn primary" :to="{ name: 'main' }">쇼핑 계속하기</router-link>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import api from '@/api/axios'
import icon from '@/assets/cart.svg'

const route = useRoute()
const iconSrc = icon

// ✅ Checkout에서 payTime query로 전달됨
const payTime = route.query.payTime as string

const orderNo = ref<string>('') 
const items = ref<any[]>([])
const ship = reactive({
  receiver: '',
  phone: '',
  postcode: '',
  addr1: '',
  addr2: '',
  mainAddress: '',
})
const shippingFee = ref(0)

// ✅ subtotal 수정 (quantity 반영)
const subtotal = computed(() =>
  items.value.reduce((s, it) => s + (it.price * (it.quantity || 1)), 0)
)
const total = computed(() => subtotal.value + shippingFee.value)
const fmt = (n: number) => n.toLocaleString('ko-KR')

// 주문 조회 - payTime 기준으로 해당 시간의 주문 그룹 조회
const fetchOrderDetail = async () => {
  try {
    console.log('payTime:', payTime)

    // ✅ 우선 전체 주문 내역을 가져와서 확인
    const allOrdersRes = await api.get('/orders/me')
    console.log('전체 주문 데이터:', allOrdersRes.data)

    // ✅ 새로운 API 엔드포인트 사용 - payTime 기준 주문 그룹 조회
    try {
      const res = await api.get(`/orders/paytime?payTime=${encodeURIComponent(payTime)}`)
      console.log('payTime 기준 주문 데이터:', res.data)

      if (Array.isArray(res.data) && res.data.length > 0) {
        // ✅ 주문번호는 해당 시간대 주문 중 가장 작은 ID로 설정
        const minOrderId = Math.min(...res.data.map((o: any) => o.orderId))
        orderNo.value = minOrderId.toString()
        
        // ✅ 중고상품이므로 모든 quantity는 1
        items.value = res.data.map((order: any) => ({
          orderId: order.orderId,
          itemName: order.itemName || '상품명 없음',
          mainImageUrl: order.mainImageUrl,
          price: order.price || 0,
          quantity: 1 // 중고상품은 항상 1개
        }))

        // 배송지 정보 불러오기
        await fetchShippingAddress()
        return
      }
    } catch (apiError) {
      console.error('payTime API 호출 실패:', apiError)
    }

    // ✅ API 실패 시 폴백: 전체 주문에서 시간 기준으로 필터링
    if (Array.isArray(allOrdersRes.data)) {
      console.log('폴백 모드: 전체 주문에서 필터링 시작')
      
      // payTime 기준으로 필터링 (여러 방식 시도)
      const targetTime = payTime.slice(0, 16) // "2025-09-05T02:37" 형식
      console.log('필터링 대상 시간:', targetTime)
      
      const filteredOrders = allOrdersRes.data.filter((order: any) => {
        if (!order.payTime) return false
        
        // 여러 형식으로 시간 비교 시도
        const orderTimeStr = order.payTime.toString()
        const orderTime16 = orderTimeStr.slice(0, 16)
        const orderTime19 = orderTimeStr.slice(0, 19)
        
        console.log(`주문 ${order.orderId} 시간 비교:`, {
          original: orderTimeStr,
          slice16: orderTime16,
          slice19: orderTime19,
          target: targetTime,
          match16: orderTime16 === targetTime,
          match19: orderTime19 === payTime
        })
        
        return orderTime16 === targetTime || orderTime19 === payTime
      })

      console.log('필터링된 주문:', filteredOrders)

      if (filteredOrders.length > 0) {
        const minOrderId = Math.min(...filteredOrders.map((o: any) => o.orderId || o.id))
        orderNo.value = minOrderId.toString()
        
        items.value = filteredOrders.map((order: any) => ({
          orderId: order.orderId || order.id,
          itemName: order.itemName || '상품명 없음',
          mainImageUrl: order.mainImageUrl,
          price: order.price || 0,
          quantity: 1
        }))

        await fetchShippingAddress()
        return
      }
    }

    // 모든 방법이 실패한 경우
    console.warn('해당 payTime에 맞는 주문이 없습니다:', payTime)
    alert('해당 주문 내역을 찾을 수 없습니다.')
    
  } catch (err) {
    console.error('주문 상세 조회 실패:', err)
    alert('주문 정보를 불러오는 중 오류가 발생했습니다.')
  }
}

// 배송지 정보 조회 함수 분리
const fetchShippingAddress = async () => {
  try {
    const addrRes = await api.get('/address')
    console.log('주소 응답:', addrRes.data)

    let addressData = null
    if (Array.isArray(addrRes.data) && addrRes.data.length > 0) {
      // 기본 배송지 또는 첫 번째 배송지 사용
      addressData = addrRes.data.find((addr: any) => addr.is_default) || addrRes.data[0]
    } else if (addrRes.data && !Array.isArray(addrRes.data)) {
      addressData = addrRes.data
    }

    if (addressData) {
      ship.receiver = addressData.receiver || ''
      ship.phone = addressData.phone || ''
      ship.postcode = addressData.postcode || ''
      ship.addr1 = addressData.addr1 || addressData.mainAddress || ''
      ship.addr2 = addressData.addr2 || ''
      ship.mainAddress = addressData.mainAddress || ''
    }
  } catch (e) {
    console.warn('주소 불러오기 실패:', e)
    // 배송지 정보는 선택사항이므로 에러를 alert로 표시하지 않음
  }
}

onMounted(() => {
  if (payTime) {
    fetchOrderDetail()
  } else {
    console.error('주문 payTime이 없습니다.')
    alert('주문번호 정보가 없습니다.')
  }
})
</script>

<style scoped>
/* Checkout 페이지와 톤/폭을 맞춤 */
.order-complete {
  max-width: 720px;
  margin: 24px auto 120px;
  padding: 0 16px;
}

/* 상단 히어로 */
.hero {
  text-align: center;
  margin: 8px 0 24px;
}

.hero__icon {
  width: 85px;
  height: 85px;
  object-fit: contain;
  display: block;
  margin: 16px auto 10px;
}

.hero__title {
  font-size: 18px;
  margin: 0;
}

.hero__meta {
  margin-top: 6px;
  color: #ff7a2e;
  font-size: 13px;
}

.order-no {
  font-weight: 700;
}

/* 카드 공통 */
.card {
  background: #fff;
  border: 1px solid #eee;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, .04);
  padding: 16px;
  margin-bottom: 16px;
  width: 100%;
}

.card.wide {
  width: 140%;
  position: relative;
  left: 50%;
  transform: translateX(-50%);
}

.section-title {
  font-size: 16px;
  margin: 0 0 12px;
}

/* 표 레이아웃 */
.thead,
.row {
  display: grid;
  grid-template-columns: 100px 1fr 80px 120px;
  align-items: center;
  gap: 12px;
}

.thead {
  font-weight: 700;
  color: #666;
  padding: 6px 2px;
  border-bottom: 1px solid #eee;
}

.tbody .row {
  padding: 10px 2px;
  border-bottom: 1px solid #f2f2f2;
}

.thumb img {
  width: 72px;
  height: 72px;
  object-fit: cover;
  border-radius: 8px;
  border: 1px solid #eee;
}

.info .name {
  font-weight: 700;
  line-height: 1.4;
}

.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.right {
  text-align: right;
}

.dl {
  display: grid;
  grid-template-columns: 110px 1fr;
  column-gap: 12px;
  row-gap: 10px;
  align-items: baseline;
}

.dt {
  color: #333;
  font-weight: 700;
}

.dd {
  color: #222;
  line-height: 1.6;
}

.total {
  border-top: 1px solid #eee;
  padding-top: 8px;
}

.actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 20px;
  width: 122%;
}

.btn {
  border-radius: 10px;
  padding: 10px 14px;
  cursor: pointer;
  border: 1px solid #ddd;
  text-decoration: none;
}

.btn.primary {
  background: #ff7a2e;
  color: #fff;
  border-color: #ff7a2e;
}

.btn.outline {
  background: #fff;
  color: #000;
}

.muted {
  text-align: center;
  color: #666;
  margin: 40px 0;
}

/* 반응형 */
@media (max-width: 720px) {
  .thead,
  .row {
    grid-template-columns: 80px 1fr 60px 100px;
  }

  .info-grid {
    grid-template-columns: 1fr;
  }
}
</style>