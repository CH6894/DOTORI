// item 반환 타입 지정용
// item entity 컬럼 자료형과 동일하게 작성.

export interface Item {
  id?: string;        // 상품 ID (fallback용)
  itemCode: string;
  name: string;
  title: string;
  cost?: number;
  price?: number;
  genre?: string;
  size?: string;
  manufacturer?: string;
  material?: string;
  information?: string;
  releaseMonth?: string;
  imgUrl?: string;
  images?: string[];
  storageFees?: number;
}
