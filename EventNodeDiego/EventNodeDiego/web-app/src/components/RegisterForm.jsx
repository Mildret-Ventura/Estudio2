import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useTranslation } from '../i18n/I18nContext'

function RegisterForm({ formData, confirmPassword, onConfirmPasswordChange, age, error, success, isLoading, onChange, onBirthDateChange, onSubmit }) {
  const { t } = useTranslation()
  const [showPassword, setShowPassword] = useState(false)
  const [showConfirmPassword, setShowConfirmPassword] = useState(false)

  const v = (value) => value && value.trim() ? 'is-valid' : ''
  const vEmail = (email) => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email) ? 'is-valid' : ''
  const vPassword = (pwd) => pwd && pwd.length >= 8 ? 'is-valid' : ''
  const vConfirm = () => confirmPassword && confirmPassword === formData.password ? 'is-valid' : ''

  return (
    <form onSubmit={onSubmit}>
      {error && <div className="alert alert-danger mb-3 p-2 small text-center">{error}</div>}
      {success && <div className="alert alert-success mb-3 p-2 small text-center">{t('auth.accountCreated')}</div>}

      <div className="row mb-3">
        <div className="col-12 col-md-4 mb-3 mb-md-0">
          <label className="form-label small fw-semibold" htmlFor="regName">
            {t('auth.firstName')}*
          </label>
          <input
            type="text"
            className={`form-control ${v(formData.nombre)}`}
            id="regName"
            name="nombre"
            value={formData.nombre}
            onChange={onChange}
            placeholder={t('auth.firstNamePlaceholder')}
            required
          />
        </div>
        <div className="col-12 col-md-4 mb-3 mb-md-0">
          <label className="form-label small fw-semibold" htmlFor="regLastNameP">
            {t('auth.lastNameP')}*
          </label>
          <input
            type="text"
            className={`form-control ${v(formData.apellidoPaterno)}`}
            id="regLastNameP"
            name="apellidoPaterno"
            value={formData.apellidoPaterno}
            onChange={onChange}
            placeholder={t('auth.lastNamePPlaceholder')}
            required
          />
        </div>
        <div className="col-12 col-md-4">
          <label className="form-label small fw-semibold" htmlFor="regLastNameM">
            {t('auth.lastNameM')}*
          </label>
          <input
            type="text"
            className={`form-control ${v(formData.apellidoMaterno)}`}
            id="regLastNameM"
            name="apellidoMaterno"
            value={formData.apellidoMaterno}
            onChange={onChange}
            placeholder={t('auth.lastNameMPlaceholder')}
            required
          />
        </div>
      </div>

      <div className="row mb-3">
        <div className="col-12 col-md-6 mb-3 mb-md-0">
          <label className="form-label small fw-semibold" htmlFor="regMatricula">
            {t('auth.matricula')}*
          </label>
          <input
            type="text"
            className={`form-control ${v(formData.matricula)}`}
            id="regMatricula"
            name="matricula"
            value={formData.matricula}
            onChange={onChange}
            placeholder={t('auth.matriculaPlaceholder')}
            required
          />
        </div>
        <div className="col-12 col-md-6">
          <label className="form-label small fw-semibold" htmlFor="regEmail">
            {t('auth.institutionalEmail')}*
          </label>
          <input
            type="email"
            className={`form-control ${vEmail(formData.correo)}`}
            id="regEmail"
            name="correo"
            value={formData.correo}
            onChange={onChange}
            placeholder={t('auth.emailPlaceholder')}
            required
          />
        </div>
      </div>

      <div className="row mb-3">
        <div className="col-12 col-md-6 mb-3 mb-md-0">
          <label className="form-label small fw-semibold" htmlFor="regPassword">
            {t('auth.passwordLabel')}*
          </label>
          <div className="input-group">
            <input
              type={showPassword ? 'text' : 'password'}
              className={`form-control border-end-0 ${vPassword(formData.password)}`}
              id="regPassword"
              name="password"
              value={formData.password}
              onChange={onChange}
              placeholder="••••••••"
              required
            />
            <span
              className={`input-group-text bg-white border-start-0 ${vPassword(formData.password) ? 'border-success' : ''}`}
              role="button"
              onClick={() => setShowPassword(!showPassword)}
            >
              <i className={`bi ${showPassword ? 'bi-eye-slash' : 'bi-eye'} text-secondary`}></i>
            </span>
          </div>
        </div>
        <div className="col-12 col-md-6">
          <label className="form-label small fw-semibold" htmlFor="regConfirmPassword">
            {t('auth.confirmPasswordLabel')}*
          </label>
          <div className="input-group">
            <input
              type={showConfirmPassword ? 'text' : 'password'}
              className={`form-control border-end-0 ${vConfirm()}`}
              id="regConfirmPassword"
              value={confirmPassword}
              onChange={onConfirmPasswordChange}
              placeholder="••••••••"
              required
            />
            <span
              className={`input-group-text bg-white border-start-0 ${vConfirm() ? 'border-success' : ''}`}
              role="button"
              onClick={() => setShowConfirmPassword(!showConfirmPassword)}
            >
              <i className={`bi ${showConfirmPassword ? 'bi-eye-slash' : 'bi-eye'} text-secondary`}></i>
            </span>
          </div>
        </div>
      </div>
      <small className="text-primary d-block mb-3 mt-1">
        {t('auth.passwordRequirements')}
      </small>

      <div className="row mb-3">
        <div className="col-12 col-md-4 mb-3 mb-md-0">
          <label className="form-label small fw-semibold" htmlFor="regBirthDate">
            {t('auth.birthDate')}*
          </label>
          <input
            type="date"
            className={`form-control ${v(formData.fechaNacimiento)}`}
            id="regBirthDate"
            name="fechaNacimiento"
            value={formData.fechaNacimiento}
            onChange={onBirthDateChange}
            min={`${new Date().getFullYear() - 99}-${String(new Date().getMonth() + 1).padStart(2, '0')}-${String(new Date().getDate()).padStart(2, '0')}`}
            required
          />
        </div>
        <div className="col-12 col-md-2 mb-3 mb-md-0">
          <label className="form-label small fw-semibold" htmlFor="regAge">
            {t('auth.age')}
          </label>
          <input
            type="text"
            className="form-control bg-light"
            id="regAge"
            value={age}
            readOnly
            disabled
          />
        </div>
        <div className="col-12 col-md-3 mb-3 mb-md-0">
          <label className="form-label small fw-semibold" htmlFor="regSex">
            {t('auth.gender')}*
          </label>
          <select
            className={`form-select ${v(formData.sexo)}`}
            id="regSex"
            name="sexo"
            value={formData.sexo}
            onChange={onChange}
            required
          >
            <option value="">{t('auth.select')}</option>
            <option value="M">{t('auth.male')}</option>
            <option value="F">{t('auth.female')}</option>
          </select>
        </div>
        <div className="col-12 col-md-3">
          <label className="form-label small fw-semibold" htmlFor="regQuarter">
            {t('auth.quarter')}*
          </label>
          <select
            className={`form-select ${v(formData.cuatrimestre)}`}
            id="regQuarter"
            name="cuatrimestre"
            value={formData.cuatrimestre}
            onChange={onChange}
            required
          >
            <option value="">{t('auth.select')}</option>
            <option value="1">1</option>
            <option value="2">2</option>
            <option value="3">3</option>
            <option value="4">4</option>
            <option value="5">5</option>
            <option value="7">7</option>
            <option value="8">8</option>
            <option value="9">9</option>
          </select>
        </div>
      </div>

      <button
        type="submit"
        className="btn btn-primary w-100 py-2 rounded-pill fw-semibold mt-2"
        disabled={isLoading || success}
      >
        {isLoading ? t('auth.creatingAccount') : t('auth.register')}
      </button>

      <p className="text-center mt-4 mb-0 small text-secondary">
        {t('auth.hasAccount')}{' '}
        <Link
          to="/login"
          className="text-primary text-decoration-none fw-semibold"
        >
          {t('auth.signIn')}
        </Link>
      </p>
    </form>
  )
}

export default RegisterForm
