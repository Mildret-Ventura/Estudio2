import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { authService } from '../services/authService'
import { useTranslation } from '../i18n/I18nContext'

function LoginForm({ onLogin }) {
  const { t } = useTranslation()
  const [correo, setCorreo] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState(null)
  const [isLoading, setIsLoading] = useState(false)
  const [keepSession, setKeepSession] = useState(false)
  const [showPassword, setShowPassword] = useState(false)
  const navigate = useNavigate()

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError(null)
    setIsLoading(true)

    try {
      const userData = await authService.login(correo, password, keepSession)
      onLogin(userData)

      if (userData.role === 'ADMIN') {
        navigate('/admin')
      } else {
        navigate('/estudiante')
      }
    } catch (err) {
      setError(err.message)
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <form onSubmit={handleSubmit}>
      {error && <div className="alert alert-danger mb-3 p-2 small text-center">{error}</div>}
      <div className="mb-3">
        <label className="form-label small fw-semibold" htmlFor="loginEmail">
          {t('auth.email')}
        </label>
        <div className="input-group">
          <span className="input-group-text bg-white border-end-0">
            <i className="bi bi-envelope text-secondary"></i>
          </span>
          <input
            type="email"
            className="form-control border-start-0"
            id="loginEmail"
            placeholder="20243ds01@utez.edu.mx"
            value={correo}
            onChange={(e) => setCorreo(e.target.value)}
            required
          />
        </div>
      </div>

      <div className="mb-3">
        <div className="d-flex justify-content-between align-items-center mb-1">
          <label className="form-label small fw-semibold mb-0" htmlFor="loginPassword">
            {t('auth.password')}
          </label>
          <Link
            to="/forgot-password"
            className="small text-primary text-decoration-none"
          >
            {t('auth.forgotPassword')}
          </Link>
        </div>
        <div className="input-group">
          <span className="input-group-text bg-white border-end-0">
            <i className="bi bi-lock text-secondary"></i>
          </span>
          <input
            type={showPassword ? 'text' : 'password'}
            className="form-control border-start-0 border-end-0"
            id="loginPassword"
            placeholder="••••••••"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />
          <span className="input-group-text bg-white border-start-0" role="button" onClick={() => setShowPassword(!showPassword)}>
            <i className={`bi ${showPassword ? 'bi-eye-slash' : 'bi-eye'} text-secondary`}></i>
          </span>
        </div>
      </div>

      <div className="form-check mb-4">
        <input
          type="checkbox"
          className="form-check-input"
          id="keepSession"
          checked={keepSession}
          onChange={(e) => setKeepSession(e.target.checked)}
        />
        <label className="form-check-label small" htmlFor="keepSession">
          {t('auth.keepSession')}
        </label>
      </div>

      <button
        type="submit"
        className="btn btn-primary w-100 py-2 rounded-pill fw-semibold"
        disabled={isLoading}
      >
        {isLoading ? t('auth.loggingIn') : (
          <>{t('auth.login')} <i className="bi bi-arrow-right ms-1"></i></>
        )}
      </button>

      <p className="text-center mt-4 mb-0 small text-secondary">
        {t('auth.noAccount')}{' '}
        <Link
          to="/register"
          className="text-primary text-decoration-none fw-semibold"
        >
          {t('auth.createAccount')}
        </Link>
      </p>
    </form>
  )
}

export default LoginForm
