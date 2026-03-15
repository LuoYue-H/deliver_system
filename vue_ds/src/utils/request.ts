import axios from 'axios'
import { ElMessage } from 'element-plus'

const service = axios.create({
  baseURL: '/api', // 配合 vite proxy 使用
  timeout: 5000
})

// 请求拦截器
service.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = 'Bearer ' + token
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  (response) => {
    const res = response.data
    // 假设后端返回结构 { code: 1, msg: 'success', data: ... }
    if (res.code !== 1) {
      ElMessage.error(res.msg || 'Error')
      return Promise.reject(new Error(res.msg || 'Error'))
    }
    return res
  },
  (error) => {
    console.log('err' + error)
    if (error.response) {
      switch (error.response.status) {
        case 401:
          ElMessage.error('未登录或登录过期，请重新登录')
          // 这里可以触发登出逻辑，如清除 token 跳转登录页
          localStorage.removeItem('token')
          localStorage.removeItem('role')
          window.location.href = '/login'
          break
        case 500:
          ElMessage.error('服务器内部错误')
          break
        default:
          ElMessage.error(error.message)
      }
    } else {
      ElMessage.error('网络错误')
    }
    return Promise.reject(error)
  }
)

export default service
