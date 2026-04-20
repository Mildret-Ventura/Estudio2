import { useState, useRef, useEffect } from 'react'
import { toast } from 'react-toastify'
import { authService } from '../../services/authService'
import { useTranslation } from '../../i18n/I18nContext'

function CambiarContrasenaModal({ correo }) {
  const { t } = useTranslation()
  // Flow: 'send' -> 'code' -> 'newPassword' -> 'success'
  const [step, setStep] = useState('send')
  const [loading, setLoading] = useState(false)
  const [codigo, setCodigo] = useState(['', '', '', '', '', ''])
  const [newPassword, setNewPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [showPassword, setShowPassword] = useState(false)
  const [showConfirm, setShowConfirm] = useState(false)

  const codeInputRefs = useRef([])

  const hasMinLength = newPassword.length >= 8
  const hasUppercase = /[A-Z]/.test(newPassword)
  const hasSpecialChar = /[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/.test(newPassword)
  const passwordsMatch = newPassword === confirmPassword && newPassword.length > 0

  const resetAll = () => {
    setStep('send')
    setLoading(false)
    setCodigo(['', '', '', '', '', ''])
    setNewPassword('')
    setConfirmPassword('')
    setShowPassword(false)
    setShowConfirm(false)
  }

  // Reset when modal opens
  useEffect(() => {
    const modalEl = document.getElementById('cambiarContrasenaModal')
    if (!modalEl) return
    const handleShow = () => resetAll()
    modalEl.addEventListener('show.bs.modal', handleShow)
    return () => modalEl.removeEventListener('show.bs.modal', handleShow)
  }, [])

  // Focus first code input when entering code step
  useEffect(() => {
    if (step === 'code') {
      setTimeout(() => codeInputRefs.current[0]?.focus(), 100)
    }
  }, [step])

  const handleSendCode = async () => {
    if (!correo) {
      toast.error('No se encontró el correo de tu cuenta')
      return
    }
    setLoading(true)
    try {
      await authService.enviarCodigoRecuperacion(correo)
      toast.success('Código enviado a tu correo')
      setStep('code')
    } catch (err) {
      toast.error(err.message)
    } finally {
      setLoading(false)
    }
  }

  const handleCodeChange = (index, value) => {
    if (!/^\d*$/.test(value)) return
    const newCode = [...codigo]
    newCode[index] = value.slice(-1)
    setCodigo(newCode)
    if (value && index < 5) {
      codeInputRefs.current[index + 1]?.focus()
    }
  }

  const handleCodeKeyDown = (index, e) => {
    if (e.key === 'Backspace' && !codigo[index] && index > 0) {
      codeInputRefs.current[index - 1]?.focus()
    }
  }

  const handleCodePaste = (e) => {
    e.preventDefault()
    const pasted = e.clipboardData.getData('text').replace(/\D/g, '').slice(0, 6)
    if (pasted.length > 0) {
      const newCode = [...codigo]
      for (let i = 0; i < 6; i++) {
        newCode[i] = pasted[i] || ''
      }
      setCodigo(newCode)
      const focusIndex = Math.min(pasted.length, 5)
      codeInputRefs.current[focusIndex]?.focus()
    }
  }

  const handleVerifyCode = async () => {
    const codigoStr = codigo.join('')
    if (codigoStr.length !== 6) {
      toast.error('Ingresa el código completo de 6 dígitos')
      return
    }
    setLoading(true)
    try {
      await authService.verificarCodigo(correo, codigoStr)
      toast.success('Código verificado correctamente')
      setStep('newPassword')
    } catch (err) {
      toast.error(err.message)
    } finally {
      setLoading(false)
    }
  }

  const handleResendCode = async () => {
    setLoading(true)
    try {
      await authService.enviarCodigoRecuperacion(correo)
      toast.success('Código reenviado a tu correo')
      setCodigo(['', '', '', '', '', ''])
    } catch (err) {
      toast.error(err.message)
    } finally {
      setLoading(false)
    }
  }

  const handleResetPassword = async () => {
    if (!hasMinLength || !hasUppercase || !hasSpecialChar) {
      toast.error('La contraseña no cumple con los requisitos de seguridad')
      return
    }
    if (!passwordsMatch) {
      toast.error('Las contraseñas no coinciden')
      return
    }
    setLoading(true)
    try {
      await authService.restablecerPassword(correo, codigo.join(''), newPassword)
      toast.success('Contraseña actualizada exitosamente')
      setStep('success')
    } catch (err) {
      toast.error(err.message)
    } finally {
      setLoading(false)
    }
  }

  const closeModal = () => {
    const modalEl = document.getElementById('cambiarContrasenaModal')
    if (modalEl && window.bootstrap) {
      const bsModal = window.bootstrap.Modal.getInstance(modalEl)
      if (bsModal) bsModal.hide()
    }
  }

  return (
    <div className="modal fade" id="cambiarContrasenaModal" tabIndex="-1" aria-hidden="true">
      <div className="modal-dialog modal-dialog-centered">
        <div className="modal-content border-0 rounded-4 shadow">
          <div className="modal-header border-0 pb-0 pt-3 pe-3">
            <button type="button" className="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button>
          </div>
          <div className="modal-body px-4 pb-4 pt-0">

            {/* Step 1: Confirm send code */}
            {step === 'send' && (
              <div className="text-center">
                <div className="rounded-circle bg-primary bg-opacity-10 d-inline-flex align-items-center justify-content-center mb-3"
                  style={{ width: '48px', height: '48px' }}>
                  <i className="bi bi-arrow-repeat text-primary fs-4"></i>
                </div>
                <h5 className="fw-bold mb-2">{t('changePassword.title')}</h5>
                <p className="text-secondary small mb-4">
                  {t('changePassword.sendCodeMsg').replace('{{email}}', correo)}
                </p>
                <button
                  className="btn btn-primary rounded-pill w-100 fw-semibold mb-2"
                  onClick={handleSendCode}
                  disabled={loading}
                >
                  {loading ? (
                    <>
                      <span className="spinner-border spinner-border-sm me-2" role="status"></span>
                      {t('changePassword.sending')}
                    </>
                  ) : t('changePassword.sendCode')}
                </button>
                <button
                  type="button"
                  className="btn btn-link text-secondary text-decoration-none small w-100"
                  data-bs-dismiss="modal"
                >
                  {t('common.cancel')}
                </button>
              </div>
            )}

            {/* Step 2: Verify code */}
            {step === 'code' && (
              <div className="text-center">
                <div className="rounded-circle bg-primary bg-opacity-10 d-inline-flex align-items-center justify-content-center mb-3"
                  style={{ width: '48px', height: '48px' }}>
                  <i className="bi bi-envelope-check text-primary fs-4"></i>
                </div>
                <h5 className="fw-bold mb-2">{t('passwordRecovery.verifyCode')}</h5>
                <p className="text-secondary small mb-4">
                  {t('changePassword.codeMsg').replace('{{email}}', correo)}
                </p>
                <div className="d-flex justify-content-center gap-2 mb-4" onPaste={handleCodePaste}>
                  {codigo.map((digit, i) => (
                    <input
                      key={i}
                      ref={(el) => (codeInputRefs.current[i] = el)}
                      type="text"
                      inputMode="numeric"
                      className="form-control text-center fw-bold"
                      maxLength="1"
                      style={{ width: '44px', height: '48px' }}
                      value={digit}
                      onChange={(e) => handleCodeChange(i, e.target.value)}
                      onKeyDown={(e) => handleCodeKeyDown(i, e)}
                    />
                  ))}
                </div>
                <button
                  className="btn btn-primary rounded-pill w-100 fw-semibold mb-3"
                  onClick={handleVerifyCode}
                  disabled={loading}
                >
                  {loading ? (
                    <>
                      <span className="spinner-border spinner-border-sm me-2" role="status"></span>
                      {t('changePassword.verifying')}
                    </>
                  ) : t('passwordRecovery.verify')}
                </button>
                <p className="text-secondary small mb-0">
                  {t('passwordRecovery.noCode')}{' '}
                  <button
                    className="btn btn-link text-primary text-decoration-underline small p-0 border-0"
                    onClick={handleResendCode}
                    disabled={loading}
                  >
                    {t('passwordRecovery.resendCode')}
                  </button>
                </p>
              </div>
            )}

            {/* Step 3: New password */}
            {step === 'newPassword' && (
              <div>
                <div className="d-flex align-items-center gap-2 mb-3">
                  <div className="rounded-circle bg-primary bg-opacity-10 d-flex align-items-center justify-content-center"
                    style={{ width: '32px', height: '32px' }}>
                    <i className="bi bi-shield-lock text-primary small"></i>
                  </div>
                  <h6 className="fw-bold mb-0">{t('passwordRecovery.setNewPassword')}</h6>
                </div>
                <p className="text-secondary small mb-4">
                  {t('passwordRecovery.securePasswordMsg')}
                </p>
                <div className="mb-3">
                  <label className="form-label text-secondary small">{t('passwordRecovery.newPassword')}</label>
                  <div className="input-group">
                    <input
                      type={showPassword ? 'text' : 'password'}
                      className="form-control"
                      placeholder="Ingresa tu nueva contraseña"
                      value={newPassword}
                      onChange={(e) => setNewPassword(e.target.value)}
                    />
                    <button className="btn btn-outline-secondary border-start-0" type="button" onClick={() => setShowPassword(!showPassword)}>
                      <i className={`bi ${showPassword ? 'bi-eye-slash' : 'bi-eye'} text-secondary`}></i>
                    </button>
                  </div>
                </div>
                <div className="mb-3">
                  <div className="text-uppercase text-secondary small fw-bold mb-2">{t('passwordRecovery.securityRequirements')}</div>
                  <div className="d-flex flex-column gap-1">
                    <div className={`d-flex align-items-center gap-2 small ${hasMinLength ? 'text-success' : 'text-secondary'}`}>
                      <i className={`bi ${hasMinLength ? 'bi-check-circle-fill' : 'bi-circle'}`} style={{ fontSize: '10px' }}></i>
                      {t('passwordRecovery.min8Chars')}
                    </div>
                    <div className={`d-flex align-items-center gap-2 small ${hasUppercase ? 'text-success' : 'text-secondary'}`}>
                      <i className={`bi ${hasUppercase ? 'bi-check-circle-fill' : 'bi-circle'}`} style={{ fontSize: '10px' }}></i>
                      {t('passwordRecovery.oneUppercase')}
                    </div>
                    <div className={`d-flex align-items-center gap-2 small ${hasSpecialChar ? 'text-success' : 'text-secondary'}`}>
                      <i className={`bi ${hasSpecialChar ? 'bi-check-circle-fill' : 'bi-circle'}`} style={{ fontSize: '10px' }}></i>
                      {t('passwordRecovery.oneSpecial')}
                    </div>
                  </div>
                </div>
                <div className="mb-4">
                  <label className="form-label text-secondary small">{t('passwordRecovery.confirmPassword')}</label>
                  <div className="input-group">
                    <input
                      type={showConfirm ? 'text' : 'password'}
                      className="form-control"
                      placeholder="Confirma tu nueva contraseña"
                      value={confirmPassword}
                      onChange={(e) => setConfirmPassword(e.target.value)}
                    />
                    <button className="btn btn-outline-secondary border-start-0" type="button" onClick={() => setShowConfirm(!showConfirm)}>
                      <i className={`bi ${showConfirm ? 'bi-eye-slash' : 'bi-eye'} text-secondary`}></i>
                    </button>
                  </div>
                  {confirmPassword && !passwordsMatch && (
                    <div className="text-danger small mt-1">{t('passwordRecovery.passwordMismatch')}</div>
                  )}
                </div>
                <button
                  className="btn btn-primary rounded-pill w-100 fw-semibold mb-2"
                  onClick={handleResetPassword}
                  disabled={loading || !hasMinLength || !hasUppercase || !hasSpecialChar || !passwordsMatch}
                >
                  {loading ? (
                    <>
                      <span className="spinner-border spinner-border-sm me-2" role="status"></span>
                      {t('passwordRecovery.resetting')}
                    </>
                  ) : t('passwordRecovery.resetPassword')}
                </button>
                <button type="button" className="btn btn-link text-secondary text-decoration-none small w-100" data-bs-dismiss="modal">
                  {t('common.cancel')}
                </button>
              </div>
            )}

            {/* Step 4: Success */}
            {step === 'success' && (
              <div className="text-center">
                <div className="rounded-circle bg-success bg-opacity-10 d-inline-flex align-items-center justify-content-center mb-3"
                  style={{ width: '64px', height: '64px' }}>
                  <i className="bi bi-check-circle-fill text-success fs-2"></i>
                </div>
                <h5 className="fw-bold mb-2">{t('passwordRecovery.passwordUpdated')}</h5>
                <p className="text-secondary small mb-4">
                  {t('passwordRecovery.passwordUpdatedMsg')}
                </p>
                <button
                  className="btn btn-primary rounded-pill w-100 fw-semibold"
                  onClick={closeModal}
                >
                  {t('passwordRecovery.accept')}
                </button>
              </div>
            )}

          </div>
        </div>
      </div>
    </div>
  )
}

export default CambiarContrasenaModal
