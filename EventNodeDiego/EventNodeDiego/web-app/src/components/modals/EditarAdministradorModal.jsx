import { useState, useEffect } from 'react'
import { toast } from 'react-toastify'
import { userService } from '../../services/userService'

function EditarAdministradorModal({ admin, onAdminUpdated }) {
  const [formData, setFormData] = useState({
    nombre: '',
    apellidoPaterno: '',
    apellidoMaterno: '',
    correo: '',
  })
  const [loading, setLoading] = useState(false)
  const [disableLoading, setDisableLoading] = useState(false)

  useEffect(() => {
    if (admin) {
      setFormData({
        nombre: admin.nombre || '',
        apellidoPaterno: admin.apellidoPaterno || '',
        apellidoMaterno: admin.apellidoMaterno || '',
        correo: admin.email || '',
      })
    }
  }, [admin])

  const handleChange = (e) => {
    const { name, value } = e.target
    setFormData(prev => ({ ...prev, [name]: value }))
  }

  const handleSubmit = async () => {
    setLoading(true)
    try {
      await userService.actualizarPerfil(admin.id, {
        nombre: formData.nombre,
        apellidoPaterno: formData.apellidoPaterno,
        apellidoMaterno: formData.apellidoMaterno,
        correo: formData.correo,
      })
      toast.success('Administrador actualizado exitosamente')
      const modalEl = document.getElementById('editarAdminModal')
      if (modalEl && window.bootstrap) {
        window.bootstrap.Modal.getOrCreateInstance(modalEl).hide()
      }
      if (onAdminUpdated) onAdminUpdated()
    } catch (err) {
      toast.error(err.message || 'Error al actualizar administrador')
    } finally {
      setLoading(false)
    }
  }

  const handleToggleState = async () => {
    setDisableLoading(true)
    try {
      await userService.cambiarEstado(admin.id)
      toast.success(admin?.active ? 'Administrador deshabilitado' : 'Administrador habilitado')
      const modalEl = document.getElementById('editarAdminModal')
      if (modalEl && window.bootstrap) {
        window.bootstrap.Modal.getOrCreateInstance(modalEl).hide()
      }
      if (onAdminUpdated) onAdminUpdated()
    } catch (err) {
      toast.error(err.message || 'Error al cambiar estado')
    } finally {
      setDisableLoading(false)
    }
  }

  return (
    <div className="modal fade" id="editarAdminModal" tabIndex="-1" aria-labelledby="editarAdminModalLabel" aria-hidden="true">
      <div className="modal-dialog modal-dialog-centered">
        <div className="modal-content border-0 rounded-4 shadow bg-white">
          <div className="modal-header border-0 px-4 pt-4 pb-0">
            <div>
              <h5 className="fw-bold mb-1 text-dark" id="editarAdminModalLabel">Editar administrador</h5>
              <p className="text-secondary small mb-0" style={{ fontSize: '13px' }}>
                Modifica los datos del administrador
              </p>
            </div>
            <button type="button" className="btn-close align-self-start mt-1" data-bs-dismiss="modal" aria-label="Cerrar"></button>
          </div>

          <div className="modal-body p-4 pt-4">
            <div className="row g-3">
              <div className="col-12">
                <label className="form-label text-dark fw-bold small mb-2" style={{ fontSize: '12px' }}>Nombre(s)</label>
                <input
                  type="text"
                  name="nombre"
                  value={formData.nombre}
                  onChange={handleChange}
                  className="form-control text-dark small"
                  style={{ fontSize: '13px' }}
                  disabled={loading || disableLoading}
                />
              </div>
              <div className="col-12 col-md-6">
                <label className="form-label text-dark fw-bold small mb-2" style={{ fontSize: '12px' }}>Apellido Paterno</label>
                <input
                  type="text"
                  name="apellidoPaterno"
                  value={formData.apellidoPaterno}
                  onChange={handleChange}
                  className="form-control text-dark small"
                  style={{ fontSize: '13px' }}
                  disabled={loading || disableLoading}
                />
              </div>
              <div className="col-12 col-md-6">
                <label className="form-label text-dark fw-bold small mb-2" style={{ fontSize: '12px' }}>Apellido Materno</label>
                <input
                  type="text"
                  name="apellidoMaterno"
                  value={formData.apellidoMaterno}
                  onChange={handleChange}
                  className="form-control text-dark small"
                  style={{ fontSize: '13px' }}
                  disabled={loading || disableLoading}
                />
              </div>
              <div className="col-12">
                <label className="form-label text-dark fw-bold small mb-2" style={{ fontSize: '12px' }}>Correo electrónico</label>
                <div className="input-group">
                  <span className="input-group-text bg-white border-end-0 text-secondary pe-2">
                    <i className="bi bi-envelope"></i>
                  </span>
                  <input
                    type="email"
                    name="correo"
                    value={formData.correo}
                    onChange={handleChange}
                    className="form-control border-start-0 ps-0 text-dark small"
                    style={{ fontSize: '13px' }}
                    disabled={loading || disableLoading}
                  />
                </div>
              </div>
            </div>
          </div>

          <div className="modal-footer border-top-0 px-4 py-3 bg-light bg-opacity-50 rounded-bottom-4 d-flex justify-content-between align-items-center mt-2">
            <div>
              {admin && !admin.esPrincipal && (
                <button
                  type="button"
                  className={`btn ${admin.active ? 'btn-outline-danger' : 'btn-outline-success'} fw-semibold px-3 py-2 rounded-3`}
                  style={{ fontSize: '13px' }}
                  onClick={handleToggleState}
                  disabled={disableLoading || loading}
                >
                  {disableLoading ? (
                    <span className="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
                  ) : (
                    <i className={`bi ${admin.active ? 'bi-person-dash' : 'bi-person-check'} me-2`}></i>
                  )}
                  {admin.active ? 'Deshabilitar' : 'Habilitar'}
                </button>
              )}
            </div>
            <div className="d-flex gap-2">
              <button
                type="button"
                className="btn btn-white border px-4 fw-semibold text-dark rounded-3"
                data-bs-dismiss="modal"
                style={{ fontSize: '13px' }}
                disabled={loading || disableLoading}
              >
                Cancelar
              </button>
              <button
                type="button"
                onClick={handleSubmit}
                className="btn btn-primary px-4 fw-semibold rounded-3"
                style={{ fontSize: '13px' }}
                disabled={loading || disableLoading}
              >
                {loading ? (
                  <span className="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span>
                ) : 'Guardar cambios'}
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}

export default EditarAdministradorModal
