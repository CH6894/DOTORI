import axios from "axios";

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
