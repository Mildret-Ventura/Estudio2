import { useState, useRef } from 'react'
import { toast } from 'react-toastify'
import { userService } from '../../services/userService'

const INITIAL_FORM = {
  nombre: '',
  apellidoPaterno: '',
  apellidoMaterno: '',
  matricula: '',
  correo: '',
  password: '',
  sexo: 'Hombre',
  cuatrimestre: '',
  fechaNacimiento: '',
}

function CrearEstudianteModal({ onStudentCreated }) {
  const [formData, setFormData] = useState(INITIAL_FORM)
  const [confirmPassword, setConfirmPassword] = useState('')
  const [showPassword, setShowPassword] = useState(false)
  const [showConfirm, setShowConfirm] = useState(false)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const closeBtnRef = useRef(null)

  const handleChange = (e) => {
    const { name, value } = e.target
    setFormData(prev => ({ ...prev, [name]: value }))
    setError('')
  }

  const handleClose = () => {
    setFormData(INITIAL_FORM)
    setConfirmPassword('')
    setError('')
    setShowPassword(false)
    setShowConfirm(false)
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')

    if (formData.password !== confirmPassword) {
      setError('Las contraseñas no coinciden')
      return
    }

    setLoading(true)
    try {
      await userService.crearAlumno({
        ...formData,
        cuatrimestre: formData.cuatrimestre ? parseInt(formData.cuatrimestre, 10) : null,
      })
      toast.success('Estudiante creado correctamente')
      handleClose()
      closeBtnRef.current?.click()
      if (onStudentCreated) onStudentCreated()
    } catch (err) {
      setError(err.message)
      toast.error(err.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="modal fade" id="crearEstudianteModal" tabIndex="-1" aria-labelledby="crearEstudianteModalLabel" aria-hidden="true">
      <div className="modal-dialog modal-dialog-centered modal-lg">
        <div className="modal-content border-0 rounded-4 shadow bg-white">
          <div className="modal-header border-0 px-4 pt-4 pb-0">
            <div>
              <h5 className="fw-bold mb-1 text-dark" id="crearEstudianteModalLabel">Nuevo estudiante</h5>
              <p className="text-secondary small mb-0" style={{ fontSize: '13px' }}>
                Completa los datos para registrar un estudiante
              </p>
            </div>
            <button type="button" className="btn-close align-self-start mt-1" data-bs-dismiss="modal" aria-label="Cerrar" onClick={handleClose} ref={closeBtnRef}></button>
          </div>

          <form onSubmit={handleSubmit}>
            <div className="modal-body p-4 pt-4">
              {error && (
                <div className="alert alert-danger py-2 small" role="alert">{error}</div>
              )}
              <div className="row g-3">
                <div className="col-12 col-md-4">
                  <label className="form-label text-dark fw-bold small mb-2" style={{ fontSize: '12px' }}>Nombre(s)</label>
                  <input
                    type="text"
                    name="nombre"
                    className="form-control text-dark small"
                    placeholder="Nombre(s)"
                    value={formData.nombre}
                    onChange={handleChange}
                    required
                    style={{ fontSize: '13px' }}
                  />
                </div>
                <div className="col-12 col-md-4">
                  <label className="form-label text-dark fw-bold small mb-2" style={{ fontSize: '12px' }}>Apellido Paterno</label>
                  <input
                    type="text"
                    name="apellidoPaterno"
                    className="form-control text-dark small"
                    placeholder="Apellido paterno"
                    value={formData.apellidoPaterno}
                    onChange={handleChange}
                    required
                    style={{ fontSize: '13px' }}
                  />
                </div>
                <div className="col-12 col-md-4">
                  <label className="form-label text-dark fw-bold small mb-2" style={{ fontSize: '12px' }}>Apellido Materno</label>
                  <input
                    type="text"
                    name="apellidoMaterno"
                    className="form-control text-dark small"
                    placeholder="Apellido materno"
                    value={formData.apellidoMaterno}
                    onChange={handleChange}
                    style={{ fontSize: '13px' }}
                  />
                </div>
                <div className="col-12 col-md-6">
                  <label className="form-label text-dark fw-bold small mb-2" style={{ fontSize: '12px' }}>Matrícula</label>
                  <input
                    type="text"
                    name="matricula"
                    className="form-control text-dark small"
                    placeholder="Matrícula"
                    value={formData.matricula}
                    onChange={handleChange}
                    required
                    style={{ fontSize: '13px' }}
                  />
                </div>
                <div className="col-12 col-md-6">
                  <label className="form-label text-dark fw-bold small mb-2" style={{ fontSize: '12px' }}>Correo electrónico</label>
                  <div className="input-group">
                    <span className="input-group-text bg-white border-end-0 text-secondary pe-2">
                      <i className="bi bi-envelope"></i>
                    </span>
                    <input
                      type="email"
                      name="correo"
                      className="form-control border-start-0 ps-0 text-dark small"
                      placeholder="correo@ejemplo.com"
                      value={formData.correo}
                      onChange={handleChange}
                      required
                      style={{ fontSize: '13px' }}
                    />
                  </div>
                </div>
                <div className="col-12 col-md-4">
                  <label className="form-label text-dark fw-bold small mb-2" style={{ fontSize: '12px' }}>Fecha de nacimiento</label>
                  <input
                    type="date"
                    name="fechaNacimiento"
                    className="form-control text-dark small"
                    value={formData.fechaNacimiento}
                    onChange={handleChange}
                    style={{ fontSize: '13px' }}
                  />
                </div>
                <div className="col-12 col-md-4">
                  <label className="form-label text-dark fw-bold small mb-2" style={{ fontSize: '12px' }}>Sexo</label>
                  <select
                    name="sexo"
                    className="form-select text-dark small"
                    value={formData.sexo}
                    onChange={handleChange}
                    style={{ fontSize: '13px' }}
                  >
                    <option value="Hombre">Hombre</option>
                    <option value="Mujer">Mujer</option>
                    <option value="Otro">Otro</option>
                  </select>
                </div>
                <div className="col-12 col-md-4">
                  <label className="form-label text-dark fw-bold small mb-2" style={{ fontSize: '12px' }}>Cuatrimestre</label>
                  <input
                    type="number"
                    name="cuatrimestre"
                    className="form-control text-dark small"
                    placeholder="Cuatrimestre"
                    min="1"
                    max="12"
                    value={formData.cuatrimestre}
                    onChange={handleChange}
                    style={{ fontSize: '13px' }}
                  />
                </div>
                <div className="col-12 col-md-6">
                  <label className="form-label text-dark fw-bold small mb-2" style={{ fontSize: '12px' }}>Contraseña</label>
                  <div className="input-group">
                    <span className="input-group-text bg-white border-end-0 text-secondary pe-2">
                      <i className="bi bi-lock"></i>
                    </span>
                    <input
                      type={showPassword ? 'text' : 'password'}
                      name="password"
                      className="form-control border-start-0 border-end-0 ps-0 text-dark small"
                      placeholder="Mínimo 8 caracteres"
                      value={formData.password}
                      onChange={handleChange}
                      required
                      style={{ fontSize: '13px' }}
                    />
                    <button
                      type="button"
                      className="input-group-text bg-white border-start-0 text-secondary"
                      onClick={() => setShowPassword(v => !v)}
                      tabIndex="-1"
                    >
                      <i className={`bi ${showPassword ? 'bi-eye-slash' : 'bi-eye'}`}></i>
                    </button>
                  </div>
                </div>
                <div className="col-12 col-md-6">
                  <label className="form-label text-dark fw-bold small mb-2" style={{ fontSize: '12px' }}>Confirmar contraseña</label>
                  <div className="input-group">
                    <span className="input-group-text bg-white border-end-0 text-secondary pe-2">
                      <i className="bi bi-lock-fill"></i>
                    </span>
                    <input
                      type={showConfirm ? 'text' : 'password'}
                      className="form-control border-start-0 border-end-0 ps-0 text-dark small"
                      placeholder="Repite la contraseña"
                      value={confirmPassword}
                      onChange={(e) => { setConfirmPassword(e.target.value); setError('') }}
                      required
                      style={{ fontSize: '13px' }}
                    />
                    <button
                      type="button"
                      className="input-group-text bg-white border-start-0 text-secondary"
                      onClick={() => setShowConfirm(v => !v)}
                      tabIndex="-1"
                    >
                      <i className={`bi ${showConfirm ? 'bi-eye-slash' : 'bi-eye'}`}></i>
                    </button>
                  </div>
                </div>
              </div>
            </div>

            <div className="modal-footer border-top-0 px-4 py-3 bg-light bg-opacity-50 rounded-bottom-4 d-flex justify-content-end gap-2 mt-2">
              <button
                type="button"
                className="btn btn-white border px-4 fw-semibold text-dark rounded-3"
                data-bs-dismiss="modal"
                style={{ fontSize: '13px' }}
                onClick={handleClose}
              >
                Cancelar
              </button>
              <button
                type="submit"
                className="btn btn-primary px-4 fw-semibold rounded-3"
                disabled={loading}
                style={{ fontSize: '13px' }}
              >
                {loading ? (
                  <>
                    <span className="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
                    Creando...
                  </>
                ) : 'Crear estudiante'}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  )
}

export default CrearEstudianteModal
