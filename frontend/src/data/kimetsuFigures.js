const imageModules = import.meta.glob("/src/assets/ani/*.{png,jpg,jpeg,svg}", {
  eager: true,
  import: "default",
});

// 파일명 속 숫자 기준으로 정렬 (1,2,3,...,10,11)
const images = Object.keys(imageModules)
  .sort((a, b) => {
    const na = parseInt(a.match(/(\d+)/)?.[0] ?? "0", 10);
    const nb = parseInt(b.match(/(\d+)/)?.[0] ?? "0", 10);
    return na - nb;
  })
  .map((p) => imageModules[p]);

// 혹시 이미지 개수가 메타데이터보다 적어도 에러 안 나게 안전 접근
const imgAt = (idx) => images[idx] ?? images[0] ?? "";

const kimetsuFigures = [
  {
    name: "아가츠마 젠이츠",
    character: "아가츠마 젠이츠",
    series: "제일복권",
    image: imgAt(0),
    verified: false,
  },
  {
    name: "카마도 네즈코",
    character: "카마도 네즈코",
    series: "제일복권",
    image: imgAt(1),
    verified: true,
  },
  {
    name: "칸로지 미츠리",
    character: "칸로지 미츠리",
    series: "제일복권",
    image: imgAt(2),
    verified: false,
  },
  {
    name: "하시비라 이노스케",
    character: "하시비라 이노스케",
    series: "제일복권",
    image: imgAt(3),
    verified: false,
  },
  {
    name: "토미오카 기유",
    character: "토미오카 기유",
    series: "제일복권",
    image: imgAt(4),
    verified: true,
  },
  {
    name: "우즈이 텐겐",
    character: "우즈이 텐겐",
    series: "제일복권",
    image: imgAt(5),
    verified: true,
  },
  {
    name: "아카자",
    character: "아카자",
    series: "제일복권",
    image: imgAt(6),
    verified: true,
  },
  {
    name: "코쵸우 시노부",
    character: "코쵸우 시노부",
    series: "제일복권",
    image: imgAt(7),
    verified: true,
  },
  {
    name: "카마도 탄지로",
    character: "카마도 탄지로",
    series: "제일복권",
    image: imgAt(8),
    verified: true,
  },
  {
    name: "츠유리 카나오",
    character: "츠유리 카나오",
    series: "제일복권",
    image: imgAt(9),
    verified: true,
  },
  {
    name: "하시비라 이노스케",
    character: "하시비라 이노스케",
    series: "제일복권",
    image: imgAt(10),
    verified: false,
  },
];

export default kimetsuFigures;
