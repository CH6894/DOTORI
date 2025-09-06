<!-- frontend/src/views/MyPageWish.vue -->
<template>
  <main class="mypage">
    <section class="container">
      <div class="section-header">
        <button @click="goToMyPage" class="btn-back">←돌아가기</button>
        <h1 class="section__title section__title--center">위시리스트</h1>
        <div class="wish-header__meta">
          <span class="count">총 {{ wishlist.length }}개</span>
          <button
            v-if="wishlist.length"
            class="btn btn--ghost btn--thin"
            @click="clearWishlist"
          >
            전체 비우기
          </button>
        </div>
      </div>

      <div class="panel">
        <template v-if="wishlist.length">
          <div class="wish-grid">
            <div
              v-for="w in wishlist"
              :key="w.wishListId"
              class="wish-card"
              @click="goProduct(w)"
              role="button"
              tabindex="0"
            >
              <div class="wish-image-container">
                <img
                  :src="w.image || '/img/placeholder.jpg'"
                  :alt="w.title"
                  class="wish-image"
                />
              </div>

              <div class="wish-info">
                <p class="wish-title">{{ w.name }}</p>
                <p class="wish-price">{{ (w.price || 0).toLocaleString() }}원</p>
              </div>

              <div class="wish-actions">
                <!-- 하트: 장바구니 왼쪽, 위시 제거 -->
                <button
                  class="icon-heart-inline"
                  aria-label="위시에서 제거"
                  title="위시에서 제거"
                  @click.stop="removeFromWishlist(w.itemId)"
                >
                  <svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor" aria-hidden="true">
                    <path d="M12 21s-6.716-4.146-9.193-7.142C.61 11.41 1.077 8.5 3.2 6.9c1.86-1.42 4.46-1.17 6.11.44L12 10l2.69-2.66c1.65-1.61 4.25-1.86 6.11-.44 2.12 1.6 2.59 4.51.393 6.958C18.716 16.854 12 21 12 21z"/>
                  </svg>
                </button>

                <button class="btn btn--thin" @click.stop="addToCart(w.itemId)">
                  장바구니
                </button>
              </div>
            </div>
          </div>
        </template>

        <template v-else>
          <div class="empty">
            <p>위시리스트가 비어 있어요.</p>
            <p class="muted">상품 상세 페이지에서 하트를 눌러 담아보세요.</p>
          </div>
        </template>
      </div>
    </section>

    <!-- 토스트 -->
    <div v-if="toast.open" class="toast">{{ toast.message }}</div>
  </main>
</template>

<script>
/* eslint-disable */
import { useWishlistStore } from '@/stores/wishlist'

/* 더미 이미지 (처음 진입 시 비어 있으면 시딩용) */
import imgRengokuKeyring from '@/assets/list/렌고쿠 코쥬로 키링.svg'
import imgAkazaDoll from '@/assets/list/아카자 인형.svg'
import imgKozumeFigure from '@/assets/list/코즈메 켄마 피규어.svg'
import imgMikuFigure from '@/assets/list/하츠네 미쿠 피규어.svg'
import imgHinataSoyoFigure from '@/assets/list/히나타 소요 피규어.svg'

/* 장바구니는 이 페이지에서만 로컬스토리지 사용 */
const LS_CART_KEY = 'dotori_cart_v1'

export default {
  name: 'MyPageWish',

  data() {
    return {
      wish: useWishlistStore(),
      toast: { open: false, message: '', _t: null },
    }
  },

  async created() {
    // API에서 위시리스트 데이터 로드
    await this.wish.load()
  },

  computed: {
    // 템플릿 호환: v-for="w in wishlist"
    wishlist() { return this.wish.items },
  },

  methods: {
    goToMyPage() {
      this.$router.push({ name: 'mypage' })
    },
    
    /* ===== 라우팅 ===== */
    goProduct(w) {
      // 위시리스트 아이템의 itemCode를 사용하여 상품 상세 페이지로 이동
      this.$router.push({ 
        name: 'product', 
        params: { id: String(w.itemCode) } 
      }).catch(err => {
        console.error('라우터 이동 실패:', err)
        // 대안: 쿼리 파라미터 사용
        this.$router.push({ 
          path: `/product/${w.itemCode}` 
        }).catch(() => {
          console.error('대안 라우터도 실패')
        })
      })
    },

    /* ===== 위시리스트 (Pinia 스토어 사용) ===== */
    async removeFromWishlist(itemId) {
      try {
        await this.wish.remove(itemId)
        this.showToast('위시에서 제거되었습니다')
      } catch (error) {
        console.error('위시리스트 제거 실패:', error)
        this.showToast('위시리스트 제거에 실패했습니다')
      }
    },
    clearWishlist() {
      if (!this.wish.count) return
      if (!confirm('위시리스트를 모두 비울까요?')) return
      this.wish.clear()
      this.showToast('위시리스트가 비워졌습니다')
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
    addToCart(itemId) {
      const w = this.wish.byItemId(itemId)
      if (!w) { this.showToast('상품을 찾을 수 없습니다'); return }
      const item = { id: w.itemId, title: w.title, price: w.price, qty: 1, shipping: 0, thumb: w.image }
      const next = this.upsert(this.getCart(), item)
      this.saveCart(next)
      this.showToast('장바구니에 담겼습니다')
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
/* ===== 섹션 헤더 ===== */
.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 2rem 0 1rem 0;
  position: relative;
  width: 100%;
}

.section-header .section__title {
  text-align: center;
  margin: 0;
  font-size: 22px;
  font-weight: 800;
  color: #2d251c;
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
}

.section-header .btn-back {
  flex-shrink: 0;
}

.section-header .wish-header__meta {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  flex-shrink: 0;
}

.btn-back {
  padding: 0.5rem 0.75rem;
  background: #f8f9fa;
  color: #6c757d;
  border: 1px solid #dee2e6;
  border-radius: 4px;
  font-size: 0.875rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.15s ease;
  flex-shrink: 0;
}

.btn-back:hover {
  background: #e9ecef;
  color: #495057;
  border-color: #adb5bd;
}

.mypage {
  color: #2d251c;
  letter-spacing: -0.00625rem;
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
}

/* 컨테이너 */
.container {
  width: clamp(20rem, 96vw, 82.5rem);
  /* 320px ~ 1320px */
  margin: 0 auto;
  padding-bottom: 120px;
}

/* 헤더 */
.count {
  font-size: .875rem;
  color: #7b6d5d;
}

/* 패널 */
.panel {
  background: #fff;
  border: .09375rem solid #e9e4db;
  border-radius: .75rem;
  padding: 1rem 1.125rem;
}

/* 버튼 */
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
.btn--thin {
  padding: 0.5rem 0.75rem;
  background: #f8f9fa;
  color: #6c757d;
  border: 1px solid #dee2e6;
  border-radius: 4px;
  font-size: 0.875rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.15s ease;
  white-space: nowrap;
}

.btn--thin:hover {
  background: #e9ecef;
  color: #495057;
  border-color: #adb5bd;
}

/* 카드 그리드 */
.wish-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(11.25rem, 1fr));
  gap: 1rem; /* 16px */
  justify-content: start;
}
.wish-card {
  background: #fff;
  border-radius: .5rem;
  border: 1px solid #f0f0f0;
  width: 100%;
  max-width: 11.25rem;                  /* 180px */
  overflow: hidden;
  cursor: pointer;
  transition: all .3s ease;
  display: flex;
  flex-direction: column;
}
.wish-card:hover {
  transform: translateY(-.375rem);
  box-shadow: 0 .5rem 1.875rem rgba(0,0,0,.12);
  border-color: #FC703C;
}

/* 이미지 */
.wish-image-container {
  width: 100%;
  aspect-ratio: 1/1;
  background: #f8f9fa;
  overflow: hidden;
}
.wish-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform .3s ease;
}
.wish-card:hover .wish-image {
  transform: scale(1.05);
}

/* 정보 */
.wish-info {
  padding: .625rem;
}
.wish-title {
  font-size: .875rem;
  font-weight: 600;
  color: #333;
  margin: 0 0 .25rem 0;
  line-height: 1.3;
}
.wish-price {
  font-size: .8125rem;
  color: #17a2b8;
  font-weight: 700;
  margin: 0;
}

/* 액션 */
.wish-actions {
  display: flex;
  justify-content: space-between;
  gap: .375rem;
  padding: .625rem;
}

/* 하트(인라인) */
.icon-heart-inline {
  display: inline-grid;
  place-items: center;
  width: 2rem; height: 2rem;
  border: 1px solid #e5e7eb;
  border-radius: 999px;
  background: #fff;
  color: #dc2626;
  cursor: pointer;
  transition: transform .15s ease, background .15s ease, border-color .15s ease;
}
.icon-heart-inline:hover {
  transform: scale(1.04);
  background: #fff5f5;
  border-color: #fecaca;
}

/* 빈 상태 */
.empty {
  text-align: center;
  padding: 2.5rem 1rem;
  color: #5f5346;
}
.muted { color: #7b6d5d; }

/* 토스트 */
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
  0% { opacity: 0; transform: translate(-50%, -60%); }
  15% { opacity: 1; transform: translate(-50%, -50%); }
  85% { opacity: 1; transform: translate(-50%, -50%); }
  100% { opacity: 0; transform: translate(-50%, -40%); }
}

/* 반응형 */
@media (max-width: 64rem) { /* <= 1024px */
  .wish-grid { 
    gap: .75rem;
    grid-template-columns: repeat(auto-fill, minmax(10rem, 1fr));
  }
}
@media (max-width: 40rem) { /* <= 640px */
  .wish-grid {
    grid-template-columns: repeat(auto-fill, minmax(8rem, 1fr));
    gap: .5rem;
  }
}
</style>
