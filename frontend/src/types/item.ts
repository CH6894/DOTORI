// item 반환 타입 지정용
// item entity 컬럼 자료형과 동일하게 작성.

// 백엔드 JSON 예시와 DB 스키마를 기준으로 안전하게 매핑
export type ItemDTO = {
    itemCode: string;
    name: string;
    title: string;

    // nullable 가능성이 있는 값들은 string | null 로
    manufacturer?: string | null;
    material?: string | null;
    releaseMonth?: string | null;
    size: string;
    information?: string | null;

    imgUrl?: string | null;

    // 숫자형 컬럼은 number
    storageFees: number;
    genre: string;
    cost?: number | null;
};
