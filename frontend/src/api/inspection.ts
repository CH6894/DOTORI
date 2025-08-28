import axios from "axios";

// --- axios 기본 설정(프록시 안 쓰면 필요) ---
axios.defaults.baseURL = import.meta.env.VITE_API_BASE || "http://localhost:8081";
axios.defaults.withCredentials = true;

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
  // 실제 백엔드 붙이면 이 함수는 안 쓰거나, 아래 demoInspections()를 제거
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
// ⚠️ 백엔드가 itemId도 돌려줌 → 타입에 추가
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
  number: number; // 현재 페이지
  size: number;   // 페이지 크기
};

// 백엔드 목록 행(Projection) 예시: AdminListRow
type AdminListRow = {
  inspectionId: number;
  itemId: number;
  registrationDate: string;      // ISO
  unpacked: number;              // 0 미개봉 / 1 개봉
  admissionState: number;        // 0 대기 / 1 반려 / 2 승인
  quality: number | null;
  imageCount: number;
  firstFilmingTime: string | null;
  cost: number;
  // (상품명/판매자명이 백엔드 응답에 없다면 프론트에서 채우거나, 백엔드에 컬럼 추가 요청)
};

// 상태코드 → 뷰모델 Status
function mapState(s: number): Status {
  return s === 2 ? "APPROVED" : s === 1 ? "REJECTED" : "PENDING";
}

// AdminListRow → Inspection(뷰모델) 매핑
function rowToInspection(r: AdminListRow): Inspection {
  return {
    id: String(r.inspectionId),
    listingId: String(r.itemId),
    listingTitle: `상품 ${r.itemId}`,      // ⚠️ 백엔드에서 제목 주면 그 값으로 교체
    sellerName: "-",                       // ⚠️ 백엔드에서 판매자명 주면 교체
    sellPrice: r.cost,
    isOpened: r.unpacked === 0 ? "미개봉" : "개봉",
    submittedAt: r.registrationDate,
    status: mapState(r.admissionState),
    photos: [],                            // 목록에서는 빈 배열(상세에서 별도 요청)
    capturedAtInternal: r.firstFilmingTime ?? undefined,
  };
}

// 실제 관리자 목록 호출 (Page 그대로 돌려받고, 뷰모델 배열도 같이 제공)
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
