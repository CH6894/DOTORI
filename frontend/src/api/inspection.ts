import axios from "axios"

// --- axios 기본 설정 ---
const api = axios.create({
  baseURL: "http://localhost:8081/api/inspections",
  withCredentials: true,
})

api.interceptors.request.use((config) => {
  const token = localStorage.getItem("AuthToken")
  if (token) {
    config.headers = config.headers || {}
    config.headers["Authorization"] = `Bearer ${token}`
  }
  return config
})

export type Status = "PENDING" | "APPROVED" | "REJECTED"

export type Inspection = {
  id: string
  listingId: string
  listingTitle: string
  sellerName: string
  sellPrice: number
  isOpened: string
  submittedAt: string
  status: Status
  photos: any[]        // ✅ 항상 배열
  grade?: string       // ✅ 등급 필드 추가
  capturedAtInternal?: string
}

// Page 타입
export type Page<T> = {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
  size: number
}

// AdminListRow -> Inspection 변환용 타입
export type AdminListRow = {
  inspectionId: number
  itemId: number
  registrationDate: string
  unpacked: number
  admissionState: number
  quality: number | null
  imageCount: number
  firstFilmingTime: string | null
  cost: number
}

// 상태 매핑
function mapState(s: number): Status {
  return s === 3 ? "APPROVED" : s === 2 ? "REJECTED" : "PENDING"
}

// AdminListRow -> Inspection 변환
function rowToInspection(r: AdminListRow): Inspection {
  return {
    id: String(r.inspectionId),
    listingId: String(r.itemId),
    listingTitle: `상품 ${r.itemId}`,
    sellerName: "-",                       // 판매자명은 백엔드 연결 시 교체
    sellPrice: r.cost,
    isOpened: r.unpacked === 0 ? "미개봉" : "개봉",
    submittedAt: r.registrationDate,
    status: mapState(r.admissionState),
    photos: [],                            // ✅ 항상 빈 배열로 초기화
    grade: r.quality !== null ? mapGrade(r.quality) : undefined,
    capturedAtInternal: r.firstFilmingTime ?? undefined,
  }
}

// 등급 숫자 -> 문자 변환 (예시)
function mapGrade(q: number): string {
  switch (q) {
    case 1: return "S"
    case 2: return "A"
    case 3: return "B"
    case 4: return "C"
    default: return "-"
  }
}

// ✅ 판매 신청 목록 가져오기
export async function fetchInspectionsFromAdmin(params?: {
  state?: number
  from?: string
  to?: string
  page?: number
  size?: number
}): Promise<{ page: Page<AdminListRow>; items: Inspection[] }> {
  try {
    const { data } = await api.get<Page<AdminListRow>>("", { params })
    const items = data.content.map(rowToInspection)
    return { page: data, items }
  } catch (error) {
    console.error("API 호출 오류:", error)
    throw error
  }
  
}

export type Photo = {
  id: number
  url: string
  isCover?: boolean
  width?: number
  height?: number
}

export async function createInspection(fd: FormData) {
  try {
    const { data } = await api.post("", fd, {
      headers: { "Content-Type": "multipart/form-data" },
    })
    return data
  } catch (error) {
    console.error("검수 신청 생성 오류:", error)
    throw error
  }
}
