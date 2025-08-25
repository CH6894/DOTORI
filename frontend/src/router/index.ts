// src/router/index.ts
import {
  createRouter,
  createWebHistory,
  type RouteRecordRaw,
} from "vue-router";

/* ===== views (code-splitting) ===== */
const MainViews = () => import("@/views/MainViews.vue");
const SearchResult = () => import("@/views/SearchResult.vue");
const ProductInfo = () => import("@/views/ProductInfo.vue");
const LoginView = () => import("@/views/LoginView.vue");
const CheckoutPage = () => import("@/views/CheckoutPage.vue");
const OrderComplete = () => import("@/views/OrderComplete.vue");

/* 새로 추가한 경로(페이지 스텁 가능) */
const DexView = () => import("@/views/DexView.vue");
const CalendarView = () => import("@/views/CalendarView.vue");
const InspectionView = () => import("@/views/InspectionView.vue");

/* ===== MyPage & children ===== */
const MyPage = () => import("@/views/MyPage.vue");
const MyPageShip = () => import("@/components/mypage/MyPageShip.vue");
const MyPageOrders = () => import("@/components/mypage/MyPageOrders.vue");
const MyPageSales = () => import("@/components/mypage/MyPageSales.vue");
const MyPageStorage = () => import("@/components/mypage/MyPageStorage.vue");
const MyPageWish = () => import("@/components/mypage/MyPageWish.vue");

/* ===== Cart ===== */
const ShoppingCart = () => import("@/views/ShoppingCart.vue");

/* ===== auth helper (임시) ===== */
function isAuthenticated(): boolean {
  return !!localStorage.getItem("authToken");
}

/* ===== routes ===== */
const routes: RouteRecordRaw[] = [
  // 홈
  {
    path: "/",
    name: "main",
    component: MainViews,
    meta: { header: "main", footer: true },
  },

  // 검색/상품
  {
    path: "/search",
    name: "search",
    component: SearchResult,
    meta: { header: "main", footer: true, utilbar: false },
  },
  {
    path: "/product/:id",
    name: "product",
    component: ProductInfo,
    meta: { header: "main", footer: true, utilbar: false },
  },

  // 로그인
  {
    path: "/login",
    name: "login",
    component: LoginView,
    meta: {
      header: "main",
      footer: true,
      utilbar: false,
      chatbot: true,
      topbtn: true,
    },
  },
  {
    path: "/mypage",
    name: "mypage",
    component: MyPage,
    meta: { requiresAuth: true, header: "main", footer: true, utilbar: false },
    children: [
      { path: "", name: "mypage-index", redirect: { name: "mypage-orders" } },
      { path: "orders", name: "mypage-orders", component: MyPageOrders },
      { path: "sales", name: "mypage-sales", component: MyPageSales },
      { path: "ship", name: "mypage-ship", component: MyPageShip },
      { path: "storage", name: "mypage-storage", component: MyPageStorage },
      { path: "wish", name: "mypage-wish", component: MyPageWish }, 
    ],
  },

  // 결제 플로우
  {
    path: "/checkout",
    name: "checkout",
    component: CheckoutPage,
    meta: {
      requiresAuth: true,
      header: "main",
      footer: true,
      utilbar: false,
      chatbot: true,
      topbtn: true,
    },
  },
  {
    path: "/ordercomplete",
    name: "ordercomplete",
    component: OrderComplete,
    meta: {
      requiresAuth: true,
      header: "main",
      footer: true,
      utilbar: false,
      chatbot: true,
      topbtn: true,
    },
  },

  // 장바구니
  {
    path: "/cart",
    name: "cart",
    component: ShoppingCart,
    meta: { header: "main", footer: true, utilbar: false },
  },

  // 도감/캘린더/검수기준 (페이지 스텁 가능)
  {
    path: "/dex",
    name: "dex",
    component: DexView,
    meta: { header: "main", footer: true },
  },
  {
    path: "/calendar",
    name: "calendar",
    component: CalendarView,
    meta: { header: "main", footer: true },
  },
  {
    path: "/inspection",
    name: "inspection",
    component: InspectionView,
    meta: { header: "main", footer: true },
  },

  // 404 -> 임시로 검색 페이지로 라우팅
  {
    path: "/:pathMatch(.*)*",
    name: "not-found",
    component: SearchResult,
    meta: { header: "main", footer: true },
  },
];

/* ===== router ===== */
const router = createRouter({
  // ✅ BASE_URL 반영: 배포 서브경로에서도 안전
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
  scrollBehavior(_to, _from, saved) {
    // 저장된 스크롤 위치가 있으면 복원, 없으면 최상단
    return saved ?? { top: 0 };
  },
});

/* ===== global guard ===== */
router.beforeEach((to, _from, next) => {
  if (to.meta?.requiresAuth && !isAuthenticated()) {
    next({ name: "login", query: { redirect: to.fullPath } });
  } else {
    next();
  }
});

export default router;
