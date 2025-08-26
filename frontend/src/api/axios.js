import axios from 'axios'
<<<<<<< HEAD
const api = axios.create({ baseURL: '', withCredentials: false })
=======
<<<<<<< HEAD
const api = axios.create({ baseURL: '', withCredentials: false })
=======
const api = axios.create({ baseURL: 'http://localhost:8081', withCredentials: false })
>>>>>>> d130171b33f98802945670901c692036adeb031d
>>>>>>> 4dffb8a51ee82bd57cc85fe79a3cd8d0bf29f731

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
