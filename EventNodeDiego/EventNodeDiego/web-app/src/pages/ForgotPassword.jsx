import { useState, useRef, useEffect } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { toast } from 'react-toastify'
import { authService } from '../services/authService'
import ForgotPasswordHeader from '../components/ForgotPasswordHeader'

function ForgotPassword() {
  const navigate = useNavigate()

  // Flow steps: 'email' -> 'code' -> 'newPassword' -> 'success'
  const [step, setStep] = useState('email')
  const [correo, setCorreo] = useState('')
  /** Código de 6 dígitos (un solo string; la UI son casillas de solo lectura) */
  const [otp, setOtp] = useState('')
  const [loading, setLoading] = useState(false)

  // New password state
  const [newPassword, setNewPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [showPassword, setShowPassword] = useState(false)
  const [showConfirm, setShowConfirm] = useState(false)

  const otpInputRef = useRef(null)
  const verifyingOtpRef = useRef(false)

  // Password validations
  const hasMinLength = newPassword.length >= 8
  const hasUppercase = /[A-Z]/.test(newPassword)
  const hasSpecialChar = /[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/.test(newPassword)
  const passwordsMatch = newPassword === confirmPassword && newPassword.length > 0

  // --- Step 1: Send code ---
  const handleSendCode = async (e) => {
    e.preventDefault()
    if (!correo.trim()) {
      toast.error('Ingresa tu correo electrónico')
      return
    }
    if (!correo.trim().endsWith('@utez.edu.mx')) {
      toast.error('El correo debe terminar en @utez.edu.mx')
      return
    }
    setLoading(true)
    try {
      await authService.enviarCodigoRecuperacion(correo.trim())
      toast.success('Código enviado a tu correo')
      setStep('code')
    } catch (err) {
      toast.error(err.message)
    } finally {
      setLoading(false)
    }
  }

  // --- Step 2: Verify code (un solo campo; casillas solo muestran y permiten tocar para corregir) ---
  const verifyOtpString = async (codigoStr) => {
    if (codigoStr.length !== 6 || loading || verifyingOtpRef.current) return
    verifyingOtpRef.current = true
    setLoading(true)
    try {
      await authService.verificarCodigo(correo.trim(), codigoStr)
      toast.success('Código verificado correctamente')
      setStep('newPassword')
    } catch (err) {
      toast.error(err.message)
      setOtp('')
      requestAnimationFrame(() => otpInputRef.current?.focus())
    } finally {
      setLoading(false)
      verifyingOtpRef.current = false
    }
  }

  const handleOtpChange = (e) => {
    const d = e.target.value.replace(/\D/g, '').slice(0, 6)
    setOtp(d)
    if (d.length === 6) {
      verifyOtpString(d)
    }
  }

  const handleOtpBoxClick = (index) => {
    setOtp((prev) => prev.slice(0, index))
    otpInputRef.current?.focus()
    requestAnimationFrame(() => {
      const el = otpInputRef.current
      if (el) el.setSelectionRange(index, index)
    })
  }

  const handleVerifyCode = async () => {
    const codigoStr = otp
    if (codigoStr.length !== 6) {
      toast.error('Ingresa el código completo de 6 dígitos')
      return
    }
    await verifyOtpString(codigoStr)
  }

  const handleResendCode = async () => {
    setLoading(true)
    try {
      await authService.enviarCodigoRecuperacion(correo.trim())
      toast.success('Código reenviado a tu correo')
      setOtp('')
    } catch (err) {
      toast.error(err.message)
    } finally {
      setLoading(false)
    }
  }

  // --- Step 3: Reset password ---
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
      await authService.restablecerPassword(correo.trim(), otp, newPassword)
      toast.success('Contraseña restablecida exitosamente')
      setStep('success')
    } catch (err) {
      toast.error(err.message)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    if (step === 'code') {
      setOtp('')
      setTimeout(() => otpInputRef.current?.focus(), 100)
    }
  }, [step])

  return (
    <div className="bg-light min-vh-100 d-flex justify-content-center align-items-center p-3">
      <div className="col-11 col-sm-8 col-md-6 col-lg-5 col-xl-4">
        <div className="card shadow-sm border-0 rounded-4">
          <div className="card-body p-4 p-md-5">

            {/* Step 1: Email */}
            {step === 'email' && (
              <>
                <ForgotPasswordHeader />
                <form onSubmit={handleSendCode}>
                  <div className="mb-4">
                    <label className="form-label small fw-semibold" htmlFor="recoveryEmail">
                      Correo institucional
                    </label>
                    <div className="input-group">
                      <span className="input-group-text bg-white border-end-0">
                        <i className="bi bi-envelope text-secondary"></i>
                      </span>
                      <input
                        type="email"
                        className="form-control border-start-0"
                        id="recoveryEmail"
                        placeholder="matricula@utez.edu.mx"
                        value={correo}
                        onChange={(e) => setCorreo(e.target.value)}
                      />
                    </div>
                  </div>
                  <button
                    type="submit"
                    className="btn btn-primary w-100 py-2 rounded-pill fw-semibold"
                    disabled={loading}
                  >
                    {loading ? (
                      <>
                        <span className="spinner-border spinner-border-sm me-2" role="status"></span>
                        Enviando...
                      </>
                    ) : (
                      <>Enviar código <i className="bi bi-arrow-right ms-1"></i></>
                    )}
                  </button>
                  <p className="text-center mt-4 mb-0 small">
                    <Link to="/login" className="text-primary text-decoration-none fw-semibold">
                      <i className="bi bi-arrow-left me-1"></i>
                      Volver al inicio de sesión
                    </Link>
                  </p>
                </form>
              </>
            )}

            {/* Step 2: Verify Code */}
            {step === 'code' && (
              <div className="text-center">
                <div className="rounded-circle bg-primary bg-opacity-10 d-inline-flex align-items-center justify-content-center mb-3"
                  style={{ width: '48px', height: '48px' }}>
                  <i className="bi bi-envelope-check text-primary fs-4"></i>
                </div>
                <h5 className="fw-bold mb-2">Verificar Código</h5>
                <p className="text-secondary small mb-4">
                  Ingresa el código de 6 dígitos que enviamos a <strong>{correo}</strong>
                </p>

                <div className="position-relative mb-4" style={{ minHeight: '48px' }}>
                  <input
                    ref={otpInputRef}
                    type="text"
                    inputMode="numeric"
                    autoComplete="one-time-code"
                    value={otp}
                    onChange={handleOtpChange}
                    className="position-absolute border-0 p-0"
                    style={{
                      opacity: 0,
                      caretColor: 'transparent',
                      left: 0,
                      top: 0,
                      width: '100%',
                      height: '100%',
                      zIndex: 1,
                      cursor: 'text',
                    }}
                    aria-label="Código de verificación de 6 dígitos"
                  />
                  <div
                    className="d-flex justify-content-center gap-2 position-relative"
                    style={{ zIndex: 2 }}
                  >
                    {Array.from({ length: 6 }).map((_, i) => (
                      <div
                        key={i}
                        role="presentation"
                        className="form-control text-center fw-bold d-flex align-items-center justify-content-center user-select-none"
                        style={{ width: '44px', height: '48px', minWidth: '44px' }}
                        onMouseDown={(e) => e.preventDefault()}
                        onClick={() => handleOtpBoxClick(i)}
                      >
                        {otp[i] ?? ''}
                      </div>
                    ))}
                  </div>
                </div>

                <button
                  className="btn btn-primary rounded-pill w-100 fw-semibold mb-3"
                  onClick={handleVerifyCode}
                  disabled={loading}
                >
                  {loading ? (
                    <>
                      <span className="spinner-border spinner-border-sm me-2" role="status"></span>
                      Verificando...
                    </>
                  ) : 'Verificar'}
                </button>

                <p className="text-secondary small mb-0">
                  ¿No recibiste el código?{' '}
                  <button
                    className="btn btn-link text-primary text-decoration-underline small p-0 border-0"
                    onClick={handleResendCode}
                    disabled={loading}
                  >
                    Reenviar código
                  </button>
                </p>
              </div>
            )}

            {/* Step 3: New Password */}
            {step === 'newPassword' && (
              <div>
                <div className="d-flex align-items-center gap-2 mb-3">
                  <div className="rounded-circle bg-primary bg-opacity-10 d-flex align-items-center justify-content-center"
                    style={{ width: '32px', height: '32px' }}>
                    <i className="bi bi-shield-lock text-primary small"></i>
                  </div>
                  <h6 className="fw-bold mb-0">Establecer nueva contraseña</h6>
                </div>
                <p className="text-secondary small mb-4">
                  Crea una contraseña segura que no hayas utilizado antes para proteger tu cuenta.
                </p>

                <div className="mb-3">
                  <label className="form-label text-secondary small">Nueva contraseña</label>
                  <div className="input-group">
                    <input
                      type={showPassword ? 'text' : 'password'}
                      className="form-control"
                      placeholder="Ingresa tu nueva contraseña"
                      value={newPassword}
                      onChange={(e) => setNewPassword(e.target.value)}
                    />
                    <button
                      className="btn btn-outline-secondary border-start-0"
                      type="button"
                      onClick={() => setShowPassword(!showPassword)}
                    >
                      <i className={`bi ${showPassword ? 'bi-eye-slash' : 'bi-eye'} text-secondary`}></i>
                    </button>
                  </div>
                </div>

                <div className="mb-3">
                  <div className="text-uppercase text-secondary small fw-bold mb-2">
                    Requisitos de seguridad
                  </div>
                  <div className="d-flex flex-column gap-1">
                    <div className={`d-flex align-items-center gap-2 small ${hasMinLength ? 'text-success' : 'text-secondary'}`}>
                      <i className={`bi ${hasMinLength ? 'bi-check-circle-fill' : 'bi-circle'}`} style={{ fontSize: '10px' }}></i>
                      Mínimo 8 caracteres
                    </div>
                    <div className={`d-flex align-items-center gap-2 small ${hasUppercase ? 'text-success' : 'text-secondary'}`}>
                      <i className={`bi ${hasUppercase ? 'bi-check-circle-fill' : 'bi-circle'}`} style={{ fontSize: '10px' }}></i>
                      Al menos una letra mayúscula
                    </div>
                    <div className={`d-flex align-items-center gap-2 small ${hasSpecialChar ? 'text-success' : 'text-secondary'}`}>
                      <i className={`bi ${hasSpecialChar ? 'bi-check-circle-fill' : 'bi-circle'}`} style={{ fontSize: '10px' }}></i>
                      Un carácter especial (#, $, etc.)
                    </div>
                  </div>
                </div>

                <div className="mb-4">
                  <label className="form-label text-secondary small">Confirmar nueva contraseña</label>
                  <div className="input-group">
                    <input
                      type={showConfirm ? 'text' : 'password'}
                      className="form-control"
                      placeholder="Confirma tu nueva contraseña"
                      value={confirmPassword}
                      onChange={(e) => setConfirmPassword(e.target.value)}
                    />
                    <button
                      className="btn btn-outline-secondary border-start-0"
                      type="button"
                      onClick={() => setShowConfirm(!showConfirm)}
                    >
                      <i className={`bi ${showConfirm ? 'bi-eye-slash' : 'bi-eye'} text-secondary`}></i>
                    </button>
                  </div>
                  {confirmPassword && !passwordsMatch && (
                    <div className="text-danger small mt-1">Las contraseñas no coinciden</div>
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
                      Restableciendo...
                    </>
                  ) : 'Restablecer contraseña'}
                </button>
                <button
                  type="button"
                  className="btn btn-link text-secondary text-decoration-none small w-100"
                  onClick={() => setStep('email')}
                >
                  Cancelar
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
                <h5 className="fw-bold mb-2">¡Contraseña Actualizada!</h5>
                <p className="text-secondary small mb-4">
                  Tu contraseña ha sido cambiada con éxito.
                  Ahora puedes iniciar sesión con tu nueva clave.
                </p>
                <Link
                  to="/login"
                  className="btn btn-primary rounded-pill w-100 fw-semibold"
                >
                  Ir al Inicio de Sesión
                </Link>
              </div>
            )}

          </div>
        </div>
      </div>
    </div>
  )
}

export default ForgotPassword
