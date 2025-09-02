import axios from "axios";

const api = axios.create({
  baseURL: "http://localhost:8081/api", // Spring Boot API 서버
  withCredentials: true, // 세션 쿠키(JSESSIONID) 같이 보낼 경우 필요
});

// 요청 인터셉터: localStorage 에 저장된 JWT 붙이기
api.interceptors.request.use((config) => {
  const token = localStorage.getItem("accessToken");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export default api;