import axios from 'axios'

// 백엔드 서버 주소
const api = axios.create({
  baseURL: 'http://49.50.135.201/api',
  withCredentials: false
})

// 요청 인터셉터 → 토큰 자동 첨부
api.interceptors.request.use(config => {
  const t = localStorage.getItem('accessToken')
  if (t) {
    config.headers.Authorization = `Bearer ${t}`
  }
  return config
})

// 응답 인터셉터 → 토큰 만료 시 로그인 화면으로
api.interceptors.response.use(
  res => res,
  err => {
    if (err.response?.status === 401) {
      window.location.href = '/login'
    }
    return Promise.reject(err)
  }
)

export default api