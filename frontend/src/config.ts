export const API_BASE = 'http://49.50.135.201';

export const OAUTH_NAVER_CLIENT_ID = '3J_9EIeTfTEup01jihZe';
export const OAUTH_NAVER_REDIRECT_URI = `${API_BASE}/login/oauth2/code/naver`;

export const OAUTH = {
  naver: `https://nid.naver.com/oauth2.0/authorize?response_type=code&client_id=${OAUTH_NAVER_CLIENT_ID}&redirect_uri=${OAUTH_NAVER_REDIRECT_URI}&state=YOUR_STATE`,
};
