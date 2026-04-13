import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { authAPI } from '../services/api'
import { useAuthStore } from '../store/stores'
import toast from 'react-hot-toast'

export default function LoginPage() {
  const [form, setForm] = useState({ email: '', password: '' })
  const [loading, setLoading] = useState(false)
  const setAuth = useAuthStore(s => s.setAuth)
  const navigate = useNavigate()

  const handleSubmit = async (e) => {
    e.preventDefault()
    setLoading(true)
    try {
      const { data } = await authAPI.login(form)
      const { user, accessToken, refreshToken } = data.data
      setAuth(user, accessToken, refreshToken)
      navigate('/')
    } catch (err) {
      toast.error(err.response?.data?.message || 'Credenziali non valide')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="auth-page">
      <div className="auth-card">
        <div className="auth-logo">💪 FitDiary</div>
        <div className="auth-sub">Accedi al tuo diario di allenamento</div>

        <form onSubmit={handleSubmit}>
          <div className="field">
            <label>Email</label>
            <input
              type="email" required
              value={form.email}
              onChange={e => setForm({ ...form, email: e.target.value })}
              placeholder="mario@example.com"
            />
          </div>
          <div className="field">
            <label>Password</label>
            <input
              type="password" required
              value={form.password}
              onChange={e => setForm({ ...form, password: e.target.value })}
              placeholder="••••••••"
            />
          </div>
          <button type="submit" className="btn btn-block" disabled={loading}>
            {loading ? <span className="spinner" /> : 'Accedi'}
          </button>
        </form>

        <p style={{ textAlign: 'center', marginTop: 20, color: 'var(--text2)', fontSize: 13 }}>
          Non hai un account?{' '}
          <Link to="/register" style={{ color: 'var(--accent2)' }}>Registrati</Link>
        </p>

        {/* Demo hint */}
        <div style={{
          marginTop: 20, padding: '10px 14px',
          background: 'var(--bg3)', borderRadius: 'var(--radius-sm)',
          fontSize: 12, color: 'var(--text2)'
        }}>
          <strong style={{ color: 'var(--amber)' }}>Demo:</strong> registra un account o usa
          demo@fitdiary.app / demo1234
        </div>
      </div>
    </div>
  )
}
