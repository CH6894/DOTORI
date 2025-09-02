import axios from "axios"

// --- axios 기본 설정 ---
const api = axios.create({
  baseURL: "http://localhost:8081/api/inspections",
  withCredentials: true,
})

api.interceptors.request.use((config) => {
  const token = localStorage.getItem("accessToken")
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
  photos: any[]        
  grade?: string     
  memo?: string
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
  title: string
  sellerName: string
  registrationDate: string
  unpacked: boolean
  admissionState: number
  quality: number | null
  itemExplanation: string | null
  imageCount: number
  filmingTime: string | null
  cost: number
  imageUrls: string[]
}

// 상태 매핑
function mapState(s: number): Status {
  return s === 1 ? "APPROVED" : s === 2 ? "REJECTED" : "PENDING"
}

// AdminListRow -> Inspection 변환
function rowToInspection(r: AdminListRow): Inspection {
  return {
    id: String(r.inspectionId),
    listingId: String(r.itemId),
    listingTitle: r.title || "상품명 없음",
    sellerName: r.sellerName || "판매자명 없음",
    sellPrice: r.cost ? Number(r.cost) : 0,
    isOpened: r.unpacked ? "개봉" : "미개봉",
    submittedAt: r.registrationDate || new Date().toISOString(),
    status: mapState(r.admissionState || 0),
    memo: r.itemExplanation || "",
    photos: r.imageUrls?.map((url, idx) => ({ 
      id: idx, 
      url: url.startsWith('http') ? url : `http://localhost:8081/uploads/items/${url}` 
    })) || [],
    grade: r.quality !== null ? mapGrade(r.quality) : undefined,
    capturedAtInternal: r.filmingTime || undefined,
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
  } catch (error: any) {
    console.error("검수 신청 생성 오류:", error)
    console.error("오류 응답:", error.response?.data)
    console.error("오류 상태:", error.response?.status)
    console.error("오류 메시지:", error.response?.data?.message)
    throw error
  }
}

// 관리자 승인
export async function approveInspection(inspectionId: string, grade?: number, reason?: string) {
  try {
    const params = new URLSearchParams()
    if (grade !== undefined) params.append('grade', grade.toString())
    if (reason) params.append('reason', reason)
    
    const { data } = await api.post(`/${inspectionId}/approve?${params.toString()}`)
    return data
  } catch (error: any) {
    console.error("승인 처리 오류:", error)
    throw error
  }
}

// 관리자 반려
export async function rejectInspection(inspectionId: string, grade?: number, reason?: string) {
  try {
    const params = new URLSearchParams()
    if (grade !== undefined) params.append('grade', grade.toString())
    if (reason) params.append('reason', reason)
    
    const { data } = await api.post(`/${inspectionId}/reject?${params.toString()}`)
    return data
  } catch (error: any) {
    console.error("반려 처리 오류:", error)
    throw error
  }
}
