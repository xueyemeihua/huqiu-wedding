import axios from 'axios'
import {ElMessage} from 'element-plus'

const request = axios.create({
    baseURL: '/api',
    timeout: 15000
})

request.interceptors.request.use(config => {
    const token = localStorage.getItem('token')
    if (token) config.headers.Authorization = `Bearer ${token}`
    return config
})

request.interceptors.response.use(
    res => {
        // 如果请求配置了 responseType: 'blob'，直接返回原始响应
        if (res.config.responseType === 'blob') {
            return res
        }

        const data = res.data
        if (data.code === 200) return data.data
        if (data.code === 401) {
            localStorage.removeItem('token')
            localStorage.removeItem('user')
            window.location.href = '/login'
        } else {
            ElMessage.error(data.msg || '请求失败')
        }
        return Promise.reject(new Error(data.msg))
    },
    err => {
        ElMessage.error(err.message || '网络异常')
        return Promise.reject(err)
    }
)

export default request