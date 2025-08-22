export const API_BASE = 'http://192.168.0.2:3000'; // 스프링 서버 주소/포트
export const OAUTH = {
  naver:  `${API_BASE}/oauth2/authorization/naver`,
  kakao:  `${API_BASE}/oauth2/authorization/kakao`,   // 추후 백엔드 붙일 때
  google: `${API_BASE}/oauth2/authorization/google`,  // 추후 백엔드 붙일 때
};
