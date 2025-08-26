import axios from 'axios'
const api = axios.create({ baseURL: 'http://localhost:8081', withCredentials: false })

api.interceptors.request.use(config => {
  const t = localStorage.getItem('accessToken')
  if (t) config.headers.Authorization = `Bearer ${t}`
  return config
})

api.interceptors.response.use(
  res => res,
  err => {
    if (err.response?.status === 401) window.location.href = '/login'
    return Promise.reject(err)
  }
)
export default api
