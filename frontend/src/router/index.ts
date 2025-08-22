// src/router/index.ts
import { createRouter, createWebHistory, type RouteRecordRaw } from "vue-router"

/* ===== views (code-splitting) ===== */
const MainViews     = () => import("@/views/MainViews.vue")
const SearchResult  = () => import("@/views/SearchResult.vue")
const ProductInfo   = () => import("@/views/ProductInfo.vue")
const LoginView     = () => import("@/views/LoginView.vue")
const CheckoutPage  = () => import("@/views/CheckoutPage.vue")
const OrderComplete = () => import("@/views/OrderComplete.vue")

/* ===== MyPage & children ===== */
const MyPage        = () => import("@/views/MyPage.vue")
const MyPageShip    = () => import("@/components/mypage/MyPageShip.vue")
const MyPageOrders  = () => import("@/components/mypage/MyPageOrders.vue")
const MyPageSales   = () => import("@/components/mypage/MyPageSales.vue")
const MyPageStorage = () => import("@/components/mypage/MyPageStorage.vue")
const MyPageWish    = () => import("@/components/mypage/MyPageWish.vue")

/* ===== Cart ===== */
const ShoppingCart  = () => import("@/views/ShoppingCart.vue")

/* ===== auth helper (임시) ===== */
function isAuthenticated(): boolean {
  return !!localStorage.getItem("authToken")
}

/* ===== routes ===== */
const routes: RouteRecordRaw[] = [
  // 홈
  { path: "/", name: "main", component: MainViews, meta: { header: "main", footer: true } },

  // 검색/상품
  { path: "/search",      name: "search",   component: SearchResult, meta: { header: "sub", footer: true, utilbar: false } },
  { path: "/product/:id", name: "product",  component: ProductInfo,  meta: { header: "sub", footer: true, utilbar: false } },

  // 로그인
  { path: "/login", name: "login", component: LoginView,
    meta: { header: "sub", footer: true, utilbar: false, chatbot: true, topbtn: true }
  },

  // 결제 플로우
  { path: "/checkout", name: "checkout", component: CheckoutPage,
    meta: { requiresAuth: true, header: "sub", footer: true, utilbar: false, chatbot: true, topbtn: true }
  },
  { path: "/ordercomplete", name: "ordercomplete", component: OrderComplete,
    meta: { requiresAuth: true, header: "sub", footer: true, utilbar: false, chatbot: true, topbtn: true }
  },

  // 장바구니
  { path: "/cart", name: "cart", component: ShoppingCart,
    meta: { header: "sub", footer: true, utilbar: false }
  },

  // 마이페이지 (부모 + 자식)
  {
    path: "/mypage",
      name: "mypage",             // ✅ 추가

    component: MyPage,
    meta: { requiresAuth: true, header: "sub", footer: true, utilbar: false },
    children: [
      { path: "", redirect: { name: "mypage-orders" } },     // 기본 진입 시 주문내역으로
      { path: "ship",    name: "mypage-ship",    component: MyPageShip },
      { path: "orders",  name: "mypage-orders",  component: MyPageOrders },
      { path: "sales",   name: "mypage-sales",   component: MyPageSales },
      { path: "storage", name: "mypage-storage", component: MyPageStorage },
      { path: "wish",    name: "mypage-wish",    component: MyPageWish },
    ],
  },

  // 404
  { path: "/:pathMatch(.*)*", name: "not-found", component: SearchResult, meta: { header: "sub", footer: true } },
]

/* ===== router ===== */
const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(_, __, saved) {
    return saved ?? { top: 0 }
  },
})

/* ===== global guard (필요 시 활성화) ===== */
// 개발 중에 막히면 아래를 주석 처리하거나 authToken을 세팅하세요.
// localStorage.setItem('authToken', 'dev')
router.beforeEach((to, _from, next) => {
  if (to.meta?.requiresAuth && !isAuthenticated()) {
    next({ name: "login", query: { redirect: to.fullPath } })
  } else {
    next()
  }
})

export default router
