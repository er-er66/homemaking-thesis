import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: '/admin',
  timeout: 10000
})

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
// 约定：凡是拦截器已经弹过 ElMessage 的错误，都会在该 Error 上打 __handled = true，
// 业务侧 catch 里用 `if (!e?.__handled)` 判断是否还要自己提示，避免双 toast。
const handledError = (err) => {
  if (err && typeof err === 'object') err.__handled = true
  return err
}

request.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code && res.code !== 200) {
      if (res.message && (res.message.includes('成功') || res.message.includes('success'))) {
        return res
      }
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(
        handledError(Object.assign(new Error(res.message || '请求失败'), { code: res.code, data: res }))
      )
    }
    return res
  },
  (error) => {
    if (error.response) {
      const { status } = error.response
      switch (status) {
        case 401:
          const msg = error.response.data?.message || '登录已过期，请重新登录'
          ElMessage.error(msg)
          localStorage.removeItem('token')
          localStorage.removeItem('userInfo')
          window.location.href = '/login'
          break
        case 403:
          ElMessage.error('没有权限访问')
          break
        case 404:
          ElMessage.error('请求的资源不存在')
          break
        case 500:
          ElMessage.error('服务器内部错误')
          break
        default:
          ElMessage.error(error.response.data?.message || '网络错误')
      }
    } else if (error.code === 'ECONNABORTED') {
      ElMessage.error('请求超时，请稍后重试')
    } else {
      ElMessage.error('网络连接失败，请检查网络')
    }
    return Promise.reject(handledError(error))
  }
)

export default request