import axios from 'axios'

const API_URL = import.meta.env.VITE_API_URL || '/api'

const api = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
})

// Interceptador para adicionar token de autenticação
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('auth_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// Interceptador para tratar erros de autenticação
api.interceptors.response.use(
  (response) => response,
  (error) => {
    const requestUrl = error.config?.url ?? ''

    if (
      error.response?.status === 401 &&
      !requestUrl.includes('/auth/login') &&
      !requestUrl.includes('/auth/register')
    ) {
      localStorage.removeItem('auth_token')
      localStorage.removeItem('auth_user')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  },
)

export default api
