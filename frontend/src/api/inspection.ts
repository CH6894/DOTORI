import axios from "axios";

// --- axios 기본 설정 ---
// 백엔드 API 서버 주소
axios.defaults.baseURL = "http://localhost:8081";
axios.defaults.withCredentials = true;

// ✅ AuthToken 자동 주입 (로그인된 사용자만 호출 가능하도록)
axios.interceptors.request.use((config) => {
  const token = localStorage.getItem("AuthToken");
  if (token) {
    config.headers = config.headers || {};
    config.headers["Authorization"] = `Bearer ${token}`;
  }
  return config;
});

// ===== 뷰 모델 =====
export type Status = "PENDING" | "APPROVED" | "REJECTED";

export type Photo = {
  id: string;
  url: string;
  isCover: boolean;
  width: number;
  height: number;
};

export type Inspection = {
  id: string;                // 검수ID
  listingId: string;         // 아이템ID
  listingTitle: string;      // (백엔드 목록에 없으면 프론트에서 임시 생성)
  sellerName: string;        // (백엔드 목록에 없으면 임시 '-')
  sellPrice: number;
  isOpened: string;          // '미개봉' | '개봉'
  submittedAt: string;       // 등록일
  status: Status;            // 'PENDING' | 'APPROVED' | 'REJECTED'
  photos: Photo[];           // 목록에선 빈 배열(상세에서 불러오기)
  capturedAtInternal?: string;
  warnings?: string[];
  grade?: "S" | "A" | "B" | "C";
};

// ===== 더미 목록(로컬 개발용) =====
export async function fetchInspections(params: any): Promise<Inspection[]> {
  return demoInspections();
}

function demoInspections(): Inspection[] {
  const now = Date.now();
  const make = (i: number, st: Status): Inspection => ({
    id: `ins_${1000 + i}`,
    listingId: `list_${2000 + i}`,
    listingTitle: `굿즈 상품 ${i}`,
    sellerName: i % 2 ? "mango" : "peach",
    sellPrice: 10000 + i * 500,
    isOpened: i % 2 === 0 ? "개봉" : "미개봉",
    submittedAt: new Date(now - i * 86400000).toISOString(),
    status: st,
    photos: Array.from({ length: 4 + (i % 3) }, (_, k) => ({
      id: `ph_${i}_${k}`,
      url: `https://picsum.photos/seed/${i}-${k}/640/640`,
      isCover: k === 0,
      width: 640,
      height: 640,
    })),
    capturedAtInternal: new Date(now - (i + 1) * 3600000).toISOString(),
    warnings:
      i % 3 === 0
        ? ["정면 샷 부족", "라벨 근접샷 없음"]
        : i % 5 === 0
        ? ["해상도 낮음"]
        : [],
  });

  return [
    make(1, "PENDING"),
    make(2, "PENDING"),
    make(3, "PENDING"),
    make(4, "APPROVED"),
    make(5, "REJECTED"),
    make(6, "PENDING"),
    make(7, "PENDING"),
    make(8, "APPROVED"),
    make(9, "PENDING"),
    make(10, "PENDING"),
    make(11, "PENDING"),
    make(12, "PENDING"),
    make(13, "APPROVED"),
    make(14, "PENDING"),
    make(15, "REJECTED"),
  ];
}

// ===== 판매 신청 생성 =====
export async function createInspection(
  fd: FormData
): Promise<{ inspectionId: number; itemId: number; status: string }> {
  const { data } = await axios.post("/api/inspections", fd, {
    headers: { "Content-Type": "multipart/form-data" },
  });
  return data;
}

// ===== 관리자 목록(API 응답 타입 & 어댑터) =====

// 백엔드 Page 래퍼
type Page<T> = {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
};

// 백엔드 목록 행(Projection) 예시
type AdminListRow = {
  inspectionId: number;
  itemId: number;
  registrationDate: string;
  unpacked: number;
  admissionState: number;
  quality: number | null;
  imageCount: number;
  firstFilmingTime: string | null;
  cost: number;
};

function mapState(s: number): Status {
  return s === 2 ? "APPROVED" : s === 1 ? "REJECTED" : "PENDING";
}

function rowToInspection(r: AdminListRow): Inspection {
  return {
    id: String(r.inspectionId),
    listingId: String(r.itemId),
    listingTitle: `상품 ${r.itemId}`,
    sellerName: "-",
    sellPrice: r.cost,
    isOpened: r.unpacked === 0 ? "미개봉" : "개봉",
    submittedAt: r.registrationDate,
    status: mapState(r.admissionState),
    photos: [],
    capturedAtInternal: r.firstFilmingTime ?? undefined,
  };
}

export async function fetchInspectionsFromAdmin(params?: {
  state?: number;
  from?: string;
  to?: string;
  page?: number;
  size?: number;
}): Promise<{ page: Page<AdminListRow>; items: Inspection[] }> {
  const { data } = await axios.get<Page<AdminListRow>>("/api/inspections/admin", { params });
  const items = data.content.map(rowToInspection);
  return { page: data, items };
}
