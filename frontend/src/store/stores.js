import { create } from 'zustand'
import { persist } from 'zustand/middleware'

// ─── AUTH STORE ───────────────────────────────────────────────────────────────
export const useAuthStore = create(persist(
  (set) => ({
    user: null,
    accessToken: null,
    refreshToken: null,
    isAuthenticated: false,

    setAuth: (user, accessToken, refreshToken) => {
      localStorage.setItem('accessToken', accessToken)
      localStorage.setItem('refreshToken', refreshToken)
      set({ user, accessToken, refreshToken, isAuthenticated: true })
    },

    setUser: (user) => set({ user }),

    logout: () => {
      localStorage.removeItem('accessToken')
      localStorage.removeItem('refreshToken')
      set({ user: null, accessToken: null, refreshToken: null, isAuthenticated: false })
    },
  }),
  { name: 'fitdiary-auth', partialize: (s) => ({ user: s.user, isAuthenticated: s.isAuthenticated }) }
))

// ─── WORKOUT STORE (active session) ──────────────────────────────────────────
export const useWorkoutStore = create((set, get) => ({
  activeSession: null,
  sessionTimer: 0,
  timerInterval: null,
  restTimer: null,      // { seconds, total }

  startSession: (session) => {
    const interval = setInterval(() => {
      set(s => ({ sessionTimer: s.sessionTimer + 1 }))
    }, 1000)
    set({ activeSession: session, sessionTimer: 0, timerInterval: interval })
  },

  updateSession: (updater) => set(s => ({
    activeSession: typeof updater === 'function' ? updater(s.activeSession) : updater
  })),

  stopSession: () => {
    const { timerInterval } = get()
    if (timerInterval) clearInterval(timerInterval)
    set({ activeSession: null, sessionTimer: 0, timerInterval: null })
  },

  startRestTimer: (seconds) => {
    const { restTimer } = get()
    if (restTimer?.interval) clearInterval(restTimer.interval)
    const interval = setInterval(() => {
      set(s => {
        const left = (s.restTimer?.left ?? seconds) - 1
        if (left <= 0) {
          clearInterval(s.restTimer?.interval)
          return { restTimer: null }
        }
        return { restTimer: { ...s.restTimer, left } }
      })
    }, 1000)
    set({ restTimer: { seconds, left: seconds, total: seconds, interval } })
  },

  stopRestTimer: () => {
    const { restTimer } = get()
    if (restTimer?.interval) clearInterval(restTimer.interval)
    set({ restTimer: null })
  },
}))

// ─── PLANS STORE ─────────────────────────────────────────────────────────────
export const usePlansStore = create((set) => ({
  plans: [],
  loading: false,
  setPlans: (plans) => set({ plans }),
  addPlan: (plan) => set(s => ({ plans: [plan, ...s.plans] })),
  removePlan: (id) => set(s => ({ plans: s.plans.filter(p => p.id !== id) })),
  updatePlan: (plan) => set(s => ({ plans: s.plans.map(p => p.id === plan.id ? plan : p) })),
  setLoading: (loading) => set({ loading }),
}))
