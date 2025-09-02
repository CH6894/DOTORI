// src/api/axiosPublic.js  (공개)
// rest api와 연결하기 위함
//  => axios.js와 차이 : 로그인 없어도 가져올 수 있도록
import axios from 'axios'
const openApi = axios.create({ baseURL: '' }) // 프록시 쓰니 상대경로 사용해도 됨
export default openApi
