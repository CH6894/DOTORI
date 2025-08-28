// src/router/index.ts
import { createRouter, createWebHistory, type RouteRecordRaw } from "vue-router";

/* ===== views (code-splitting) ===== */
const MainViews = () => import("@/views/MainViews.vue");
const SearchResult = () => import("@/views/SearchResult.vue");
const ProductInfo = () => import("@/views/ProductInfo.vue");
const LoginView = () => import("@/views/LoginView.vue");
const CheckoutPage = () => import("@/views/CheckoutPage.vue");
const OrderComplete = () => import("@/views/OrderComplete.vue");

/* ===== admin/verify & oauth ===== */
const VerifyUploadPage = () => import("@/views/VerifyUploadPage.vue");
const AdminPage = () => import("@/views/AdminPage.vue");
const OAuthCallback = () => import("@/pages/OAuthCallback.vue");

/* 새로 추가한 경로(페이지 스텁 가능) */
const CalendarView = () => import("@/views/CalendarPage.vue");
const CalendarManager = () => import("@/views/CalendarManager.vue");
const InspectionView = () => import("@/views/InspectionPage.vue");

/* ===== MyPage & children ===== */
const MyPage = () => import("@/views/MyPage.vue");
const MyPageShip = () => import("@/components/mypage/MyPageShip.vue");
const MyPageOrders = () => import("@/components/mypage/MyPageOrders.vue");
const MyPageSales = () => import("@/components/mypage/MyPageSales.vue");
const MyPageStorage = () => import("@/components/mypage/MyPageStorage.vue");
const MyPageWish = () => import("@/components/mypage/MyPageWish.vue");

/* ===== Cart ===== */
const ShoppingCart = () => import("@/views/ShoppingCart.vue");

/* ===== Dex & Category Views ===== */
const Dex = () => import("@/views/Dex.vue");
const BlackPink = () => import("@/components/BlackPink.vue");
const Kimetsu = () => import("@/components/Kimetsu.vue");
const ChimCollection = () => import("@/components/ChimCollection.vue");
const KiaCollection = () => import("@/components/KiaCollection.vue");

/* ===== auth helper (임시) ===== */
function isAuthenticated(): boolean {
  return !!localStorage.getItem("AuthToken");
}

const ItemList = () => import("@/components/ItemList.vue");

/* ===== routes ===== */
const routes: RouteRecordRaw[] = [
  { path: "/items", name: "items", component: ItemList },

  { path: "/CalendarManager", name: "CalendarManager", component: CalendarManager },

  // 홈
  { path: "/", name: "main", component: MainViews, meta: { header: "main", footer: true } },

  // 검색/상품
  { path: "/search", name: "search", component: SearchResult, meta: { header: "main", footer: true, utilbar: true } },
  { path: "/product/:id", name: "product", component: ProductInfo, meta: { header: "main", footer: true, utilbar: true } },

  // 로그인
  {
    path: "/login",
    name: "login",
    component: LoginView,
    meta: { header: "main", footer: true, utilbar: true, chatbot: true, topbtn: true },
  },

  // 마이페이지
  {
    path: "/mypage",
    name: "mypage",
    component: MyPage,
    meta: { requiresAuth: false, header: "main", footer: true, utilbar: true },
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
    meta: { requiresAuth: false, header: "main", footer: true, utilbar: true, chatbot: true, topbtn: true },
  },
  {
    path: "/ordercomplete",
    name: "ordercomplete",
    component: OrderComplete,
    meta: { requiresAuth: false, header: "main", footer: true, utilbar: true, chatbot: true, topbtn: true },
  },

  // 인증/관리 보조 페이지
  {
    path: "/verify-upload",
    name: "verify-upload",
    component: VerifyUploadPage,
    meta: { header: "main", footer: true },
  },
  {
    path: "/admin",
    name: "admin",
    component: AdminPage,
    meta: { header: "main", footer: true, chatbot: false },
  },
  {
    path: '/oauth2/callback',
    name: 'OAuth2Callback',
    component: () => import('@/pages/OAuthCallback.vue'),
    meta: { header: "main", footer: false, utilbar: true },
  },

  // 장바구니
  {
    path: "/cart",
    name: "cart",
    component: ShoppingCart,
    meta: {
      requiresAuth: true,
      header: "main", footer: true, utilbar: true
    },
  },

  /* ===== 도감/컬렉션 ===== */
  // 기본 도감: Dex.vue (포켓몬)
  { path: "/dex", name: "Dex", component: Dex, meta: { header: "main", footer: true } },

  {
    path: "/dex",
    name: "dex",
    component: () => import("@/views/Dex.vue"),
    meta: {
      requiresAuth: true,
      header: "main", footer: true
    },
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
  { path: "/:pathMatch(.*)*", name: "not-found", component: SearchResult, meta: { header: "main", footer: true } },
];

/* ===== router ===== */
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
  scrollBehavior(_to, _from, saved) {
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
