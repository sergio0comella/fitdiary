import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 10000,
})

// Request interceptor – attach token
api.interceptors.request.use(config => {
  const token = localStorage.getItem('accessToken')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

// Response interceptor – auto-refresh on 401
api.interceptors.response.use(
  res => res,
  async err => {
    const original = err.config
    if (err.response?.status === 401 && !original._retry) {
      original._retry = true
      const refreshToken = localStorage.getItem('refreshToken')
      if (refreshToken) {
        try {
          const { data } = await axios.post('/api/auth/refresh', { refreshToken })
          const { accessToken, refreshToken: newRefresh } = data.data
          localStorage.setItem('accessToken', accessToken)
          localStorage.setItem('refreshToken', newRefresh)
          original.headers.Authorization = `Bearer ${accessToken}`
          return api(original)
        } catch {
          localStorage.clear()
          window.location.href = '/login'
        }
      }
    }
    return Promise.reject(err)
  }
)

export default api

// ─── API METHODS ─────────────────────────────────────────────────────────────

export const authAPI = {
  register: (data) => api.post('/auth/register', data),
  login: (data) => api.post('/auth/login', data),
  logout: () => api.post('/auth/logout'),
}

export const userAPI = {
  getMe: () => api.get('/users/me'),
  updateMe: (data) => api.put('/users/me', data),
}

export const plansAPI = {
  getAll: () => api.get('/plans'),
  get: (id) => api.get(`/plans/${id}`),
  create: (data) => api.post('/plans', data),
  delete: (id) => api.delete(`/plans/${id}`),
  activate: (id) => api.post(`/plans/${id}/activate`),
  addExercise: (planId, dayId, data) => api.post(`/plans/${planId}/days/${dayId}/exercises`, data),
  updateExercise: (planId, dayId, exerciseId, data) => api.put(`/plans/${planId}/days/${dayId}/exercises/${exerciseId}`, data),
  deleteExercise: (planId, dayId, exerciseId) => api.delete(`/plans/${planId}/days/${dayId}/exercises/${exerciseId}`),
}

export const exercisesAPI = {
  getAll: (search) => api.get('/exercises', { params: search ? { search } : {} }),
  create: (data) => api.post('/exercises', data),
}

export const workoutsAPI = {
  start: (data) => api.post('/workouts/start', data),
  finish: (data) => api.post('/workouts/finish', data),
  addSet: (data) => api.post('/workouts/sets', data),
  getSessions: (limit = 20) => api.get('/workouts', { params: { limit } }),
  getSession: (id) => api.get(`/workouts/${id}`),
}

export const statsAPI = {
  getStats: () => api.get('/stats'),
  getVolume: (weeks = 12) => api.get('/stats/volume', { params: { weeks } }),
  getExerciseProgress: (exerciseId) => api.get(`/stats/progress/exercise/${exerciseId}`),
  getPRs: () => api.get('/stats/prs'),
  getInsights: () => api.get('/stats/insights'),
  getSuggestedLoad: (exerciseId) => api.get(`/stats/suggest/${exerciseId}`),
}
