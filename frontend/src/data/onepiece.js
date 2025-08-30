// src/data/onepiece.js
// ✅ webp 포함 + default export 이름/배열명 일치
const imageModules = import.meta.glob('/src/assets/onepiece/*.{png,jpg,jpeg,svg,webp}', 
    { eager: true, import: "default" }
);

// 파일명 속 숫자 기준 정렬 (1,2,3,...)
const images = Object.keys(imageModules)
  .sort((a, b) => {
    const na = parseInt(a.match(/(\d+)/)?.[0] ?? "0", 10);
    const nb = parseInt(b.match(/(\d+)/)?.[0] ?? "0", 10);
    return na - nb;
  })
  .map((p) => imageModules[p]);

const imgAt = (i) => images[i] ?? images[0] ?? ''

// ✅ 배열 이름을 onepieceFigures로, export도 동일하게
const onepieceFigures = [
  { name: "너를 향한 마음 피규어 B상 볼사리노", character: "볼사리노", series: "제일복권", image: imgAt(0), verified: false },
  { name: "메모리얼 로그 20주년 D상 에드워드 뉴 게이트", character: "에드워드 뉴게이트", series: "제일복권", image: imgAt(1), verified: true },
  { name: "EX 용과 쟁쟁한 강자들 A상 킹", character: "알베르", series: "제일복권", image: imgAt(2), verified: false },
  { name: "베스트 오브 옴니버스 A상 카이도", character: "카이도", series: "제일복권", image: imgAt(3), verified: false },
  { name: "EX 악마를 품고 있는 자들 Vol.2 B상 마샬.D.티치", character: "마샬.D.티치", series: "제일복권", image: imgAt(4), verified: true },
  { name: "엑설런트모델 P.O.P SA-MAXIMUM 롤로노아 조로", character: "롤로노아 조로", series: "P.O.P", image: imgAt(5), verified: true },
  { name: "SCultures 조형왕 정상결전 WORLD 2018 상디", character: "상디", series: "제일복권", image: imgAt(6), verified: true },
  { name: "BEYOND THE LEVEL 라스트원상 몽키.D.루피 기어5 라스트원 Ver.", character: "몽키.D.루피", series: "제일복권", image: imgAt(7), verified: true },
  { name: "극장판 필름 레드 -MORE BEAT- C상 토니토니 쵸파", character: "토니토니 쵸파", series: "제일복권", image: imgAt(8), verified: true },
  { name: "브라더후드 2탄 루피 & 에이스 & 사보", character: "몽키.D.루피, 포트거스.D.에이스, 사보", series: "브라더후드", image: imgAt(9), verified: true },
];

export default onepieceFigures;
