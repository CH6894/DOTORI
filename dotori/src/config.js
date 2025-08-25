export const API_BASE = 'http://192.168.219.189:8085'; // 스프링 서버 주소/포트
export const OAUTH_NAVER_CLIENT_ID = '3J_9EIeTfTEup01jihZe';
export const OAUTH_NAVER_REDIRECT_URI = `${API_BASE}/login/oauth2/code/naver`;

export const OAUTH = {
    naver: `https://nid.naver.com/oauth2.0/authorize?response_type=code&client_id=${OAUTH_NAVER_CLIENT_ID}&redirect_uri=${OAUTH_NAVER_REDIRECT_URI}&state=YOUR_STATE`
};
