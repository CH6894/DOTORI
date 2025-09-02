<!-- frontend/src/views/MyPage.vue -->
<template>
  <main class="mypage">
    <!-- 헤더: 제목 + 탭 (상세 진입 시 숨김) -->
    <section class="mypage__head container" v-if="!$route.query.detail">
      <h1 class="mypage__title">마이페이지</h1>

      <nav class="tabs" role="tablist" aria-label="마이페이지 탭" ref="tabsWrap">
        <!-- 움직이는 밑줄 인디케이터 -->
        <span class="tabs__indicator"
          :style="{ width: indicator.width + 'px', transform: `translateX(${indicator.left}px)` }" />
        <button v-for="t in tabs" :key="t.key" ref="tabBtns" class="tab" :class="{ 'is-active': currentTab === t.key }"
          role="tab" :aria-selected="currentTab === t.key" @click="setTab(t.key)">
          {{ t.label }}
        </button>
      </nav>
    </section>

    <!-- ✅ 자식 라우트가 들어올 자리 (예: /mypage/trade 등) -->
    <section class="container" v-if="isChildRoute">
      <router-view />
    </section>

    <!-- 본문 (자식 라우트가 아닐 때만 보임) -->
    <section class="container" v-else>
      <!-- 내 정보 탭 -->
      <div v-show="currentTab === 'profile'">
        <!-- 프로필 카드 -->
        <div class="panel profile-card">
          <div class="card__tools">
            <button v-if="!isEditing" class="link-edit" @click="startEdit">회원정보 수정</button>
            <div v-else class="edit-actions">
              <button class="btn btn--ghost btn--sm" @click="cancelEdit">취소</button>
              <button class="btn btn--sm" @click="saveProfile">저장</button>
            </div>
          </div>

          <div class="profile">
            <div class="profile__avatar">
              <img :src="user.userImg || defaultPhoto" alt="프로필" />
            </div>

            <!-- 보기 / 편집 -->
            <div v-if="!isEditing" class="profile__info">
              <p>
                <span class="profile__label">닉네임</span>
                <span class="profile__nickName">{{ user.nickName || '닉네임 없음' }}</span>
              </p>
              <p>
                <span class="profile__label">이메일</span>
                <span class="profile__userId">{{ user.email || '이메일 없음' }}</span>
              </p>
            </div>

            <form v-else class="profile__form" @submit.prevent="saveProfile">
              <label class="profile__row">
                <span class="profile__label">닉네임</span>
                <input v-model.trim="form.nickname" placeholder="닉네임" required />
              </label>
              <label class="profile__row">
                <span class="profile__label">이메일</span>
                <input :value="user.email" placeholder="이메일" readonly />
              </label>
            </form>
          </div>
        </div>

        <!-- 주문 배송 현황 -->
        <h2 class="section__title link" @click="go('ShipPage')">주문 배송 현황</h2>
        <div class="panel">
          <ol class="steps steps--flat">
            <li v-for="(s, i) in orderSteps" :key="s.key" class="step" :class="{ 'is-done': i <= orderProgressIndex }">
              <img :src="s.icon" :alt="s.label" class="step__icon" />
              <div v-if="i < orderSteps.length - 1" class="step__arrow" :class="{ 'is-on': i < orderProgressIndex }"
                aria-hidden="true">›</div>
              <p class="step__label">{{ s.label }}</p>
            </li>
          </ol>
        </div>

        <!-- 거래 내역(최근 5개) -->
        <h2 class="section__title link" @click="go('TradePage')">거래 내역</h2>
        <div class="card">
          <template v-if="recent5 && recent5.length">
            <table class="tbl tbl--fixed">
              <colgroup>
                <col style="width: 14ch" />
                <col />
                <col style="width: 11ch" />
                <col style="width: 10ch" />
              </colgroup>
              <thead>
                <tr>
                  <th>거래번호</th>
                  <th>상품</th>
                  <th>금액</th>
                  <th>상태</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="t in recent5" :key="t.no">
                  <td>{{ t.no }}</td>
                  <td class="td-product">
                    <span class="kind-badge" :data-kind="t.kind">
                      {{ t.kind === 'sell' ? '판매' : '구매' }}
                    </span>
                    <span class="product-title" :title="t.item">{{ truncate(t.item, 30) }}</span>
                  </td>
                  <td>{{ t.price.toLocaleString() }}원</td>
                  <td>
                    <span class="badge" :data-type="t.state.type">{{ t.state.text }}</span>
                  </td>
                </tr>
              </tbody>
            </table>

            <div class="center" style="margin-top:.75rem">
              <button class="btn btn--wide" @click="go('TradePage')">거래내역 전체 보기</button>
            </div>
          </template>

          <template v-else>
            <p class="muted center">최근 거래내역이 없습니다.</p>
          </template>
        </div>

        <!-- 위시리스트 -->
        <h2 class="section__title link" @click="go('WishPage')">위시리스트</h2>
        <div class="panel">
          <div class="wish-grid">
            <div v-for="w in wishlist" :key="w.id" class="wish-card" @click="goProduct(w)" role="button" tabindex="0">
              <div class="wish-image-container">
                <img :src="w.image || '/img/placeholder.jpg'" :alt="w.title" class="wish-image" />
              </div>

              <div class="wish-info">
                <p class="wish-title">{{ w.title }}</p>
                <p class="wish-price">{{ w.price.toLocaleString() }}원</p>
              </div>

              <div class="wish-actions">
                <!-- 하트 버튼을 장바구니 버튼 왼쪽으로 -->
                <button class="icon-heart-inline" aria-label="위시에서 제거" title="위시에서 제거"
                  @click.stop="removeFromWishlist(w.id)">
                  <svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor" aria-hidden="true">
                    <path
                      d="M12 21s-6.716-4.146-9.193-7.142C.61 11.41 1.077 8.5 3.2 6.9c1.86-1.42 4.46-1.17 6.11.44L12 10l2.69-2.66c1.65-1.61 4.25-1.86 6.11-.44 2.12 1.6 2.59 4.51.393 6.958C18.716 16.854 12 21 12 21z" />
                  </svg>
                </button>
                <button class="btn btn--thin" @click.stop="addToCart(w.id)">장바구니</button>
              </div>
            </div>
          </div>

        </div>

      </div>

      <!-- 문의 탭 -->
      <div v-show="currentTab === 'qna'" class="card">
        <h2 class="card__title">문의 내역</h2>
        <p class="muted">문의 리스트/상세가 이 영역에 표시됩니다.</p>
      </div>
    </section>

    <!-- 토스트 -->
    <div v-if="toast.open" class="toast">
      {{ toast.message }}
    </div>
  </main>
</template>

<script>
import { useOrderStore } from '@/stores/orders'
import api from '@/api/axios.js'
import { useAuthStore } from '@/stores/auth'
import { useWishlistStore } from '@/stores/wishlist'

import defaultPhoto from '@/assets/profile_phto.svg'
import iconOrder from '@/assets/주문.png'
import iconPay from '@/assets/결제.png'
import iconPrep from '@/assets/준비.png'
import iconShip from '@/assets/배송.png'
import iconDone from '@/assets/완료.png'

/* 위시 더미 이미지 (처음 진입 시 비어 있으면 시딩용) */
import imgRengokuKeyring from '@/assets/list/렌고쿠 코쥬로 키링.svg'
import imgAkazaDoll from '@/assets/list/아카자 인형.svg'
import imgKozumeFigure from '@/assets/list/코즈메 켄마 피규어.svg'
import imgMikuFigure from '@/assets/list/하츠네 미쿠 피규어.svg'
import imgHinataSoyoFigure from '@/assets/list/히나타 소요 피규어.svg'

const ROUTE_NAME_MAP = {
  ShipPage: 'mypage-ship',
  WishPage: 'mypage-wish',
  TradePage: 'mypage-trade',
}

const LS_CART_KEY = 'dotori_cart_v1'

export default {
  name: 'MyPage',

  data() {
    return {
      /* 스토어 */
      wish: useWishlistStore(),
      store: useOrderStore(),
      authStore: useAuthStore(),

      /* UI 상태 */
      currentTab: 'profile',
      tabs: [
        { key: 'profile', label: '내 정보' },
        { key: 'qna', label: '문의내역' },
      ],
      indicator: { width: 0, left: 0 },

      /* 프로필 */
      isEditing: false,
      user: {},
      form: { nickname: '' },
      defaultPhoto,

      /* 진행 상태 */
      orderSteps: [
        { key: 'order', label: '주문접수', icon: iconOrder },
        { key: 'payment', label: '결제 완료', icon: iconPay },
        { key: 'prep', label: '상품 준비', icon: iconPrep },
        { key: 'ship', label: '배송중', icon: iconShip },
        { key: 'done', label: '배송 완료', icon: iconDone },
      ],
      orderProgressIndex: 2,

      /* 샘플 거래내역 */
      trades: [
        { no: 'O202508201', kind: 'buy', item: '루피 피규어', price: 53000, state: { type: 'buy-done', text: '구매 완료' }, date: '2025-08-20T10:12:00' },
        { no: 'O202508199', kind: 'buy', item: '미쿠 스페셜', price: 67000, state: { type: 'buy-done', text: '구매 완료' }, date: '2025-08-19T09:01:00' },
        { no: 'S202508121', kind: 'sell', item: '아카자 인형', price: 48000, state: { type: 'sell-done', text: '판매 완료' }, date: '2025-08-18T14:30:00' },
        { no: 'S202508118', kind: 'sell', item: '렌고쿠 키링', price: 10000, state: { type: 'selling', text: '판매중' }, date: '2025-08-17T18:25:00' },
        { no: 'O202508115', kind: 'buy', item: '원피스 세트', price: 82000, state: { type: 'buy-done', text: '구매 완료' }, date: '2025-08-15T11:05:00' },
      ],

      /* 토스트 */
      toast: { open: false, message: '', _t: null },
    }
  },

  computed: {
    isChildRoute() {
      const n = this.$route.name || ''
      return n.startsWith('mypage-') && n !== 'mypage'
    },
    sortedTrades() {
      return [...(this.trades || [])].sort(
        (a, b) => new Date(b.date).getTime() - new Date(a.date).getTime()
      )
    },
    recent5() {
      return this.sortedTrades.slice(0, 5)
    },
    orderSummary() {
      return this.store?.orderSummary ?? { total: 0, deposit: 0, progress: 0, done: 0 }
    },
    /* 템플릿 호환: v-for="w in wishlist" 그대로 쓰려면 아래 사용 */
    wishlist() {
      return this.wish.items
    },
  },

  created() {
    /* 위시리스트 로드 + 비어있으면 더미 시딩 */
    this.wish.load()
    if (!this.wish.count) {
      this.wish.replace([
        { id: 'w1', title: '렌고쿠 코쥬로 키링', image: imgRengokuKeyring, price: 10000 },
        { id: 'w2', title: '아카자 인형', image: imgAkazaDoll, price: 48000 },
        { id: 'w3', title: '코즈메 켄마 피규어', image: imgKozumeFigure, price: 59000 },
        { id: 'w4', title: '하츠네 미쿠 피규어', image: imgMikuFigure, price: 53000 },
        { id: 'w5', title: '히나타 소요 피규어', image: imgHinataSoyoFigure, price: 67000 },
      ])
    }
  },

  mounted() {
    this.$nextTick(this.updateIndicator)
    window.addEventListener('resize', this.updateIndicator, { passive: true })
  },
  beforeUnmount() {
    window.removeEventListener('resize', this.updateIndicator)
  },

  methods: {
    /* ===== 탭 & 인디케이터 ===== */
    setTab(key) {
      if (this.currentTab === key) return
      this.currentTab = key
      this.$nextTick(this.updateIndicator)
    },
    updateIndicator() {
      const wrap = this.$refs.tabsWrap
      const btns = this.$refs.tabBtns || []
      const idx = this.tabs.findIndex(t => t.key === this.currentTab)
      const active = Array.isArray(btns) ? btns[idx] : btns
      if (!wrap || !active) return
      const wrapRect = wrap.getBoundingClientRect()
      const btnRect = active.getBoundingClientRect()
      this.indicator.width = btnRect.width
      this.indicator.left = btnRect.left - wrapRect.left
    },

    /* ===== 프로필 ===== */
    startEdit() {
      this.form.nickname = this.user.nickName || ''
      this.isEditing = true
    },
    cancelEdit() {
      this.isEditing = false
    },
    saveProfile() {
      if (!this.form.nickname.trim()) {
        alert('닉네임을 입력해주세요')
        return
      }
      this.updateProfile()
      this.isEditing = false
    },
    async updateProfile() {
      try {
        // await api.post('/profile', { nickname: this.form.nickname })
        this.user.nickName = this.form.nickname
        this.showToast('프로필이 저장되었습니다')
      } catch {
        this.showToast('저장 중 오류가 발생했습니다')
      }
    },

    /* ===== 라우팅 ===== */
    go(name) {
      const real = ROUTE_NAME_MAP[name]
      if (real) {
        this.$router.push({ name: real }).catch(() => {
          if (name === 'TradePage') return this.$router.push('/mypage/trade')
          if (name === 'WishPage')  return this.$router.push('/mypage/wish')
          if (name === 'ShipPage')  return this.$router.push('/mypage/ship')
        })
        return
      }
      this.$router.push('/mypage')
    },
    goProduct(w) {
      this.$router.push({ name: 'ProductInfo', params: { id: w.id } })
    },

    /* ===== 유틸 ===== */
    truncate(text, len = 30) {
      if (!text) return ''
      return text.length > len ? text.slice(0, len) + '…' : text
    },

    /* ===== 장바구니 ===== */
    getCart() {
      try {
        const raw = localStorage.getItem(LS_CART_KEY)
        return raw ? JSON.parse(raw) : []
      } catch { return [] }
    },
    saveCart(list) {
      localStorage.setItem(LS_CART_KEY, JSON.stringify(list))
    },
    upsert(cart, item) {
      const i = cart.findIndex(x => x.id === item.id)
      if (i >= 0) cart[i].qty += item.qty
      else cart.push(item)
      return cart
    },
    addToCart(id) {
      const w = this.wish.byId(id)
      if (!w) { this.showToast('상품을 찾을 수 없습니다'); return }
      const item = { id: w.id, title: w.title, price: w.price, qty: 1, shipping: 0, thumb: w.image }
      const next = this.upsert(this.getCart(), item)
      this.saveCart(next)
      this.showToast('장바구니에 담겼습니다')
    },

    /* ===== 위시리스트 버튼(하트) ===== */
    removeFromWishlist(id) {
      this.wish.remove(id)
      this.showToast('위시리스트에서 제거되었습니다')
    },

    /* ===== Toast ===== */
    showToast(message = '완료되었습니다') {
      this.toast.message = message
      this.toast.open = true
      clearTimeout(this.toast._t)
      this.toast._t = setTimeout(() => { this.toast.open = false }, 2000)
    },
  },
}
</script>



<style scoped>
/* 컨테이너: 유연한 최대폭 */
.container {
  width: clamp(20rem, 96vw, 82.5rem);
  /* 320px ~ 1320px */
  margin: 0 auto;
}

.mypage {
  color: #2d251c;
  letter-spacing: -0.00625rem;
}

/* ===== 헤더 & 탭 ===== */
.mypage__head {
  padding-top: 1.125rem;
  text-align: center;
}

.mypage__title {
  margin: 1rem 0 .75rem;
  font-size: 1.75rem;
  /* 28px */
  font-weight: 800;
}

.tabs {
  position: relative;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(7.5rem, 1fr));
  /* 120px */
  width: min(26.25rem, 100%);
  /* 420px */
  margin: 0 auto;
  padding-bottom: .375rem;
  border-bottom: .125rem solid #e5dcc9;
  gap: 0;
}

.tabs__indicator {
  position: absolute;
  left: 0;
  bottom: -.125rem;
  height: .125rem;
  background: #2d251c;
  border-radius: .125rem;
  transition: width .25s ease, transform .25s ease;
  will-change: transform, width;
  pointer-events: none;
}

.tab {
  appearance: none;
  background: transparent;
  border: 0;
  padding: .75rem .5rem;
  font-weight: 700;
  color: #7b6d5d;
  position: relative;
  cursor: pointer;
  text-align: center;
}

.tab.is-active {
  color: #2d251c;
}

/* ===== 카드/패널 ===== */
.card {
  margin-top: 1.5rem;
  padding: 1.375rem;
  background: #fff;
  border: .0625rem solid #e8e1d4;
  border-radius: .625rem;
}

.card__title {
  margin: 0 0 .875rem;
  font-size: 1rem;
  font-weight: 800;
}

.panel {
  background: #fff;
  border: .09375rem solid #e9e4db;
  border-radius: .75rem;
  padding: 1rem 1.125rem;
  margin-top: 1.5rem;
}

.section__title {
  margin: 1.375rem .25rem .625rem;
  font-size: 1rem;
  font-weight: 800;
  color: #2d251c;
}

.section__title.link {
  cursor: pointer;
}

.section__title.link:hover {
  text-decoration: underline;
}

.muted {
  color: #7b6d5d;
}

.center {
  text-align: center;
}

/* ===== Profile ===== */
.profile-card {
  margin-top: 1.5rem;
  position: relative;
}

.card__tools {
  position: absolute;
  top: .625rem;
  right: .75rem;
  display: flex;
  gap: .5rem;
  align-items: center;
}

.link-edit {
  background: transparent;
  border: 0;
  font-size: .75rem;
  color: #7b6d5d;
  cursor: pointer;
}

.edit-actions .btn--sm {
  padding: .375rem .625rem;
}

.profile {
  display: flex;
  align-items: center;
  gap: 1.25rem;
}

.profile__avatar {
  inline-size: 5rem;
  block-size: 5rem;
  /* 80x80 */
  border: .0625rem solid #e5dcc9;
  border-radius: 50%;
  overflow: hidden;
  display: grid;
  place-items: center;
  background: #fff;
}

.profile__avatar img {
  inline-size: 100%;
  block-size: 100%;
  object-fit: cover;
}

.profile__label {
  font-size: .875rem;
  color: #8f8577;
  margin-right: .375rem;
}

.profile__nickname {
  font-size: 1.125rem;
  font-weight: 800;
  color: #2d251c;
}

.profile__userid {
  font-size: .875rem;
  font-weight: 600;
  color: #5c5346;
}

.profile__form {
  max-width: 22.5rem;
  display: flex;
  flex-direction: column;
  gap: .625rem;
}

.profile__row {
  display: flex;
  flex-direction: column;
}

.profile__form .profile__label {
  font-size: .75rem;
  color: #8f8577;
  margin-bottom: .25rem;
}

.profile__form input {
  inline-size: 100%;
  padding: .625rem .75rem;
  border: .0625rem solid #e5dcc9;
  border-radius: .625rem;
}

/* ===== 공통 버튼 ===== */
.btn {
  appearance: none;
  border: 0;
  background: #f4f4f4;
  color: #000;
  padding: .625rem .875rem;
  border-radius: .5rem;
  font-weight: 700;
  cursor: pointer;
}

.btn--ghost {
  background: #f4f3e6;
  color: #5f5346;
  border: .0625rem solid #d9d9d9;
}

.btn--wide {
  inline-size: 100%;
  padding: .75rem 1rem;
  border: .0625rem solid #d9d9d9;
}

.btn--sm {
  padding: .375rem .625rem;
  border-radius: .5rem;
  background: #fff7ea;
  color: #332b22;
  border: .0625rem solid #e3d7c2;
  margin-left: .3125rem;
}

.btn--thin {
  padding: .375rem .625rem;
  border-radius: .4375rem;
  border: .0625rem solid #d9d9d9;
}

/* ===== Steps ===== */
:root {
  --line: #e9e4db;
  --green: #f4f4f4;
}

.steps--flat {
  margin: .5rem 0 0;
  padding: .75rem .625rem;
  border: .0625rem solid var(--line);
  border-radius: .5rem;
  background: #fff;
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  align-items: center;
}

.step {
  position: relative;
  display: grid;
  grid-template-rows: 1.75rem auto;
  justify-items: center;
  padding: .375rem 0 .125rem;
}

.step__arrow {
  position: absolute;
  top: .1875rem;
  right: -.125rem;
  color: #d9d4cc;
  font-size: 1.375rem;
  line-height: 1.375rem;
  transition: color .2s;
}

.step__arrow.is-on {
  color: var(--green);
}

.step__label {
  margin-top: 1rem;
  font-size: .75rem;
  color: #8f8577;
}

.step.is-done .step__label {
  color: #5c5246;
  font-weight: 700;
}

.step__icon {
  inline-size: 2.5rem;
  block-size: 2.5rem;
  object-fit: contain;
  filter: grayscale(100%) opacity(.6);
  transition: filter .2s;
}

.step.is-done .step__icon {
  filter: none;
}

/* ===== 거래내역 테이블 (요약 4열) ===== */
.tbl {
  inline-size: 100%;
  border-collapse: collapse;
}

.tbl--fixed {
  table-layout: fixed;
}

.tbl th,
.tbl td {
  border-bottom: .0625rem solid #eee6d7;
  padding: .75rem .625rem;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  vertical-align: middle;
  text-align: center;
}

/* 상품 셀 내부: 배지 + 제목 말줄임 */
.td-product {
  display: flex;
  align-items: center;
  gap: .5rem;
  min-width: 0;
}

.product-title {
  flex: 1 1 auto;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-weight: 600;
}

/* 구매/판매 배지 */
.kind-badge {
  padding: .25rem .5rem;
  border-radius: 999px;
  font-size: .75rem;
  border: .0625rem solid #e4d8c3;
  background: #f7f5ef;
  color: #5c5346;
  white-space: nowrap;
}

.kind-badge[data-kind="sell"] {
  background: #fff7ea;
  border-color: #f0dfbd;
}

.kind-badge[data-kind="buy"] {
  background: #eef6ff;
  border-color: #cfe1ff;
}

/* 상태 뱃지 */
.badge {
  display: inline-block;
  padding: .375rem .625rem;
  border-radius: 20rem;
  font-size: .75rem;
  font-weight: 700;
  color: #fff;
}

.badge[data-type="sell-done"] {
  background: #f97316;
}

.badge[data-type="buy-done"] {
  background: #22c55e;
}

.badge[data-type="selling"] {
  background: #3b82f6;
}

/* ===== Wish (반응형 카드 그리드) ===== */
.wish-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
  /* 16px */
}

.wish-card {
  background: #fff;
  border-radius: 0.5rem;
  border: 1px solid #f0f0f0;
  flex: 1 1 clamp(8rem, 20%, 11.25rem);
  /* 128px ~ 180px */
  max-width: 11.25rem;
  /* 180px */
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
}

.wish-card:hover {
  transform: translateY(-0.375rem);
  /* -6px */
  box-shadow: 0 0.5rem 1.875rem rgba(0, 0, 0, 0.12);
  border-color: #FC703C;
}

.wish-image-container {
  width: 100%;
  aspect-ratio: 1 / 1;
  background: #f8f9fa;
  overflow: hidden;
}

.wish-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.wish-card:hover .wish-image {
  transform: scale(1.05);
}

.wish-info {
  padding: 0.625rem;
}

.wish-title {
  font-size: 0.875rem;
  font-weight: 600;
  color: #333;
  margin: 0 0 0.25rem 0;
  line-height: 1.3;
}

.wish-price {
  font-size: 0.8125rem;
  color: #17a2b8;
  font-weight: 700;
  margin: 0;
}

.wish-actions {
  display: flex;
  justify-content: space-between;
  gap: 0.375rem;
  padding: 0.625rem;
}

/* 하트 아이콘 버튼 */
.icon-heart-inline {
  display: inline-grid;
  place-items: center;
  width: 2rem;
  height: 2rem;
  border: 1px solid #e5e7eb;
  border-radius: 999px;
  background: #fff;
  color: #dc2626;
  cursor: pointer;
  transition: transform 0.15s ease, background 0.15s ease, border-color 0.15s ease;
}

.icon-heart-inline:hover {
  transform: scale(1.04);
  background: #fff5f5;
  border-color: #fecaca;
}

/* ===== Toast ===== */
.toast {
  position: fixed;
  inset: 50% auto auto 50%;
  transform: translate(-50%, -50%);
  background: #f4f4f4;
  color: #7b6d5d;
  padding: .875rem 1.25rem;
  border-radius: .75rem;
  font-weight: 600;
  text-align: center;
  z-index: 9999;
  animation: fadeInOut 2.2s ease forwards;
}

@keyframes fadeInOut {
  0% {
    opacity: 0;
    transform: translate(-50%, -60%);
  }

  15% {
    opacity: 1;
    transform: translate(-50%, -50%);
  }

  85% {
    opacity: 1;
    transform: translate(-50%, -50%);
  }

  100% {
    opacity: 0;
    transform: translate(-50%, -40%);
  }
}

/* ===== 반응형 ===== */
@media (max-width: 64rem) {

  /* <= 1024px */
  .wish-grid {
    gap: 0.75rem;
    /* 12px */
  }

  .wish-card {
    flex: 1 1 calc(33.333% - 0.75rem);
    /* 3열 */
    max-width: none;
  }
}

@media (max-width: 40rem) {

  /* <= 640px */
  .tabs {
    width: 100%;
  }

  .profile {
    flex-direction: column;
    align-items: flex-start;
  }

  .wish-card {
    flex: 1 1 calc(50% - 0.5rem);
    /* 2열 */
  }
}
</style>