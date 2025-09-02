import { http as axios } from '@/lib/http'

export type Status = "PENDING" | "APPROVED" | "REJECTED";

export type Photo = {
  id: string;
  url: string;
  isCover: boolean;
  width: number;
  height: number;
};

export type Inspection = {
  id: string;
  listingId: string;
  listingTitle: string;
  sellerName: string;
  sellPrice: number;
  isOpened: string;
  submittedAt: string;
  status: Status;
  photos: Photo[];
  capturedAtInternal?: string;
  warnings?: string[];
  grade?: "S" | "A" | "B" | "C";
};

// 👉 API 호출 함수
export async function fetchInspections(params: any): Promise<Inspection[]> {
  // 나중에 백엔드 나오면 이 부분만 교체
  // const { data } = await axios.get("/api/inspections", { params })
  // return data

  // 현재는 더미 데이터
  return demoInspections();
}

// 👉 더미 데이터 생성기
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

// --- [추가] 판매 신청 생성(약관 동의 후 최종 전송) -----------------
export async function createInspection(fd: FormData): Promise<{ inspectionId: string; status: string }> {
  const { data } = await axios.post('/api/inspections', fd, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
  return data
}

// --- [추가] 관리자용 조회(백엔드 붙일 때 fetchInspections 대체용) -----
export type OpenState = 'UNOPENED' | 'OPENED' | 'PARTIAL'

// 백엔드에서 내려줄 DTO 예시 타입(필드명이 다르면 여기만 맞춰주면 됨)
type AdminInspectionDto = {
  inspectionId: string
  productId?: string
  productTitle: string
  sellerName: string
  price: number
  openState: OpenState
  uploadedAt: string
  capturedAtInternal?: string
  status: Status
  photos?: Array<{ id?: string; url: string; isCover?: boolean; width?: number; height?: number }>
  imageUrls?: string[] // 사진을 URL 배열로만 줄 수도 있으니 대비
}

// DTO -> 프론트 `Inspection`으로 변환
function adaptAdmin(dto: AdminInspectionDto): Inspection {
  const mkId = () => Math.random().toString(36).slice(2)

  const photos: Photo[] =
    (dto.photos
      ? dto.photos.map((p, i) => ({
          id: p.id ?? `ph_${dto.inspectionId}_${i}`,
          url: p.url,
          isCover: p.isCover ?? i === 0,
          width: p.width ?? 640,
          height: p.height ?? 640,
        }))
      : (dto.imageUrls ?? []).map((u, i) => ({
          id: `ph_${dto.inspectionId}_${i}`,
          url: u,
          isCover: i === 0,
          width: 640,
          height: 640,
        }))) || []

  return {
    id: dto.inspectionId,
    listingId: dto.inspectionId,                     // 관리자 테이블에서 ID로도 쓰고 있어서 동일 매핑
    listingTitle: dto.productTitle,
    sellerName: dto.sellerName,
    sellPrice: dto.price,
    isOpened: dto.openState === 'UNOPENED' ? '미개봉' : '개봉',
    submittedAt: dto.uploadedAt,
    status: dto.status,
    photos,
    capturedAtInternal: dto.capturedAtInternal,
  }
}

// 실제 관리자 목록 호출 (백엔드 붙이면 이걸 사용)
export async function fetchInspectionsFromAdmin(params?: any): Promise<Inspection[]> {
  const { data } = await axios.get<AdminInspectionDto[]>('/api/inspections/admin', { params })
  return data.map(adaptAdmin)
}
