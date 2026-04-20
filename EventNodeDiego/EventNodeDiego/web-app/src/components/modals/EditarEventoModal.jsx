import { useState, useRef, useEffect } from 'react'
import { eventService } from '../../services/eventService'
import { useTranslation } from '../../i18n/I18nContext'

function EditarEventoModal({ evento, categorias = [], onSubmit }) {
  const { t } = useTranslation()
  const fileInputRef = useRef(null)
  const formRef = useRef(null)
  const closeBtnRef = useRef(null)
  const [bannerPreview, setBannerPreview] = useState(null)
  const [errors, setErrors] = useState({})
  const [isLoading, setIsLoading] = useState(false)

  const [formData, setFormData] = useState({
    nombre: '',
    ubicacion: '',
    descripcion: '',
    fechaInicio: '',
    fechaFin: '',
    idCategoria: '',
    capacidadMaxima: '',
    tiempoCancelacionHoras: '',
    tiempoToleranciaMinutos: '',
    banner: null,
  })

  const [allOrgs, setAllOrgs] = useState([])
  const [selectedOrgs, setSelectedOrgs] = useState([])

  useEffect(() => {
    eventService.buscarOrganizadores('').then(setAllOrgs).catch(() => setAllOrgs([]))
  }, [])

  useEffect(() => {
    if (evento) {
      const fmtDate = (dt) => {
        if (!dt) return ''
        const str = typeof dt === 'string' ? dt : dt.toString()
        return str.substring(0, 16)
      }

      setFormData({
        nombre: evento.nombre || '',
        ubicacion: evento.ubicacion || '',
        descripcion: evento.descripcion || '',
        fechaInicio: fmtDate(evento.fechaInicio),
        fechaFin: fmtDate(evento.fechaFin),
        idCategoria: evento.categoriaId ? String(evento.categoriaId) : '',
        capacidadMaxima: evento.capacidadMaxima ? String(evento.capacidadMaxima) : '',
        tiempoCancelacionHoras: evento.tiempoCancelacionHoras ? String(evento.tiempoCancelacionHoras) : '',
        tiempoToleranciaMinutos: evento.tiempoToleranciaMinutos != null ? String(evento.tiempoToleranciaMinutos) : '',
        banner: null,
      })
      setBannerPreview(
        evento.banner && evento.banner.startsWith('data:image/') ? evento.banner : null
      )
      setSelectedOrgs([])
      setErrors({})
    }
  }, [evento])

  const handleSelectOrg = (e) => {
    const id = parseInt(e.target.value)
    if (!id) return
    const org = allOrgs.find(o => o.idOrganizador === id)
    if (org && !selectedOrgs.some(s => s.idOrganizador === id)) {
      setSelectedOrgs(prev => [...prev, org])
    }
    e.target.value = ''
  }

  const handleRemoveOrg = (idOrganizador) => {
    setSelectedOrgs(prev => prev.filter(o => o.idOrganizador !== idOrganizador))
  }

  const handleChange = (e) => {
    const { name, value } = e.target
    setFormData(prev => ({ ...prev, [name]: value }))
    if (errors[name]) setErrors(prev => ({ ...prev, [name]: '' }))
  }

  const handleBannerClick = () => fileInputRef.current?.click()
  const handleBannerChange = (e) => {
    const file = e.target.files[0]
    if (file) {
      setBannerPreview(URL.createObjectURL(file))
      setFormData(prev => ({ ...prev, banner: file }))
    }
  }

  const validate = () => {
    const { nombre, ubicacion, descripcion, fechaInicio, fechaFin, idCategoria, capacidadMaxima, tiempoCancelacionHoras, tiempoToleranciaMinutos } = formData
    const e = {}
    if (!nombre.trim()) e.nombre = 'El nombre es obligatorio'
    if (!ubicacion.trim()) e.ubicacion = 'La ubicación es obligatoria'
    if (!descripcion.trim()) e.descripcion = 'La descripción es obligatoria'
    if (!fechaInicio) e.fechaInicio = 'La fecha de inicio es obligatoria'
    if (!fechaFin) e.fechaFin = 'La fecha de fin es obligatoria'
    if (!idCategoria) e.idCategoria = 'Selecciona una categoría'
    if (!capacidadMaxima) e.capacidadMaxima = 'La capacidad es obligatoria'
    if (!tiempoCancelacionHoras) e.tiempoCancelacionHoras = 'El tiempo de cancelación es obligatorio'
    if (tiempoToleranciaMinutos === '') e.tiempoToleranciaMinutos = 'La tolerancia es obligatoria'
    return e
  }

  const handleSave = async () => {
    const validationErrors = validate()
    if (Object.keys(validationErrors).length > 0) {
      setErrors(validationErrors)
      return
    }
    setIsLoading(true)
    try {
      await onSubmit({
        ...formData,
        organizadores: selectedOrgs.map(o => o.idOrganizador),
      })
      closeBtnRef.current?.click()
      setErrors({})
    } catch {
      // error handled by parent via toast
    } finally {
      setIsLoading(false)
    }
  }

  const ic = (field) => errors[field] ? 'form-control is-invalid' : 'form-control'
  const is = (field) => errors[field] ? 'form-select is-invalid' : 'form-select'
  const availableOrgs = allOrgs.filter(o => !selectedOrgs.some(s => s.idOrganizador === o.idOrganizador))

  return (
    <div className="modal fade" id="editarEventoModal" tabIndex="-1" aria-labelledby="editarEventoModalLabel" aria-hidden="true">
      <div className="modal-dialog modal-dialog-centered modal-lg modal-dialog-scrollable">
      <form ref={formRef} noValidate className="modal-content border-0 rounded-4 shadow">
        <div className="modal-header border-0 px-4 pt-4 pb-0">
          <div>
            <h5 className="fw-bold mb-1">{t('editEvent.title')}</h5>
            <p className="text-secondary small mb-0">
              {t('editEvent.subtitle')}
            </p>
          </div>
          <button type="button" className="btn-close" data-bs-dismiss="modal" aria-label={t('common.close')} ref={closeBtnRef}></button>
        </div>
        <div className="modal-body px-4 py-3">
          {/* Banner */}
          <div className="mb-4">
            <label className="form-label fw-semibold small">{t('editEvent.changeBanner')}</label>
            <input type="file" ref={fileInputRef} accept="image/*" className="d-none" onChange={handleBannerChange} />
            <div className="rounded-3 overflow-hidden position-relative" style={{ cursor: 'pointer' }} onClick={handleBannerClick}>
              {bannerPreview ? (
                <img
                  src={bannerPreview}
                  alt={t('editEvent.changeBanner')}
                  className="w-100"
                  style={{ height: '180px', objectFit: 'cover' }}
                />
              ) : (
                <div
                  className="d-flex flex-column align-items-center justify-content-center text-center p-4"
                  style={{ border: '2px dashed #dee2e6', height: '180px' }}
                >
                  <i className="bi bi-image text-secondary fs-3 mb-2"></i>
                  <div className="text-secondary small">{t('editEvent.selectBanner')}</div>
                </div>
              )}
              {bannerPreview && (
                <div className="position-absolute top-50 start-50 translate-middle bg-dark bg-opacity-50 text-white px-3 py-2 rounded-pill d-flex align-items-center gap-2" style={{ pointerEvents: 'none' }}>
                  <i className="bi bi-camera"></i> {t('editEvent.changeBanner')}
                </div>
              )}
            </div>
          </div>

          {/* Nombre + Ubicación */}
          <div className="row g-3 mb-3">
            <div className="col-12 col-md-6">
              <label className="form-label fw-semibold small">{t('editEvent.eventName')}</label>
              <input type="text" name="nombre" className={ic('nombre')} placeholder={t('editEvent.eventName')} value={formData.nombre} onChange={handleChange} />
              {errors.nombre && <div className="invalid-feedback">{errors.nombre}</div>}
            </div>
            <div className="col-12 col-md-6">
              <label className="form-label fw-semibold small">{t('editEvent.location')}</label>
              <input type="text" name="ubicacion" className={ic('ubicacion')} placeholder={t('editEvent.location')} value={formData.ubicacion} onChange={handleChange} />
              {errors.ubicacion && <div className="invalid-feedback">{errors.ubicacion}</div>}
            </div>
          </div>

          {/* Descripción */}
          <div className="mb-3">
            <label className="form-label fw-semibold small">{t('editEvent.description')}</label>
            <textarea name="descripcion" className={errors.descripcion ? 'form-control is-invalid' : 'form-control'} rows="3" placeholder={t('editEvent.description')} value={formData.descripcion} onChange={handleChange}></textarea>
            {errors.descripcion && <div className="invalid-feedback">{errors.descripcion}</div>}
          </div>

          {/* Fechas */}
          <div className="row g-3 mb-3">
            <div className="col-12 col-md-6">
              <label className="form-label fw-semibold small">Fecha y Hora Inicio *</label>
              <input type="datetime-local" name="fechaInicio" className={ic('fechaInicio')} value={formData.fechaInicio} onChange={handleChange} />
              {errors.fechaInicio && <div className="invalid-feedback">{errors.fechaInicio}</div>}
            </div>
            <div className="col-12 col-md-6">
              <label className="form-label fw-semibold small">Fecha y Hora Fin *</label>
              <input type="datetime-local" name="fechaFin" className={ic('fechaFin')} value={formData.fechaFin} onChange={handleChange} />
              {errors.fechaFin && <div className="invalid-feedback">{errors.fechaFin}</div>}
            </div>
          </div>

          {/* Categoría + Capacidad */}
          <div className="row g-3 mb-3">
            <div className="col-12 col-md-6">
              <label className="form-label fw-semibold small">Categoría</label>
              <select name="idCategoria" className={is('idCategoria')} value={formData.idCategoria} onChange={handleChange}>
                <option value="">{t('editEvent.selectCategory')}</option>
                {categorias.map(cat => (
                  <option key={cat.idCategoria} value={String(cat.idCategoria)}>
                    {cat.nombre}
                  </option>
                ))}
              </select>
              {errors.idCategoria && <div className="invalid-feedback">{errors.idCategoria}</div>}
            </div>
            <div className="col-12 col-md-6">
              <label className="form-label fw-semibold small">Capacidad Máxima *</label>
              <input type="number" name="capacidadMaxima" className={ic('capacidadMaxima')} placeholder="Ej: 100" min="1" value={formData.capacidadMaxima} onChange={handleChange} />
              {errors.capacidadMaxima && <div className="invalid-feedback">{errors.capacidadMaxima}</div>}
            </div>
          </div>

          {/* Cancelación + Tolerancia */}
          <div className="row g-3 mb-3">
            <div className="col-12 col-md-6">
              <label className="form-label fw-semibold small">Tiempo Cancelación (hrs) *</label>
              <input type="number" name="tiempoCancelacionHoras" className={ic('tiempoCancelacionHoras')} placeholder="Ej: 24" min="1" value={formData.tiempoCancelacionHoras} onChange={handleChange} />
              {errors.tiempoCancelacionHoras && <div className="invalid-feedback">{errors.tiempoCancelacionHoras}</div>}
            </div>
            <div className="col-12 col-md-6">
              <label className="form-label fw-semibold small">Tolerancia de entrada (min) *</label>
              <input type="number" name="tiempoToleranciaMinutos" className={ic('tiempoToleranciaMinutos')} placeholder="Ej: 15" min="0" value={formData.tiempoToleranciaMinutos} onChange={handleChange} />
              {errors.tiempoToleranciaMinutos && <div className="invalid-feedback">{errors.tiempoToleranciaMinutos}</div>}
            </div>
          </div>

          {/* Organizadores */}
          <div className="mb-2">
            <label className="form-label fw-semibold small">Organizadores</label>
            <select className="form-select mb-2" onChange={handleSelectOrg} defaultValue="">
              <option value="">Seleccionar organizador...</option>
              {availableOrgs.map(org => (
                <option key={org.idOrganizador} value={org.idOrganizador}>
                  {org.nombre}{org.correo ? ` — ${org.correo}` : ''}
                </option>
              ))}
            </select>
            {selectedOrgs.length > 0 && (
              <div className="d-flex flex-wrap gap-2 mt-1">
                {selectedOrgs.map(org => (
                  <span
                    key={org.idOrganizador}
                    className="badge bg-primary bg-opacity-10 text-primary d-flex align-items-center gap-1 px-2 py-1 rounded-pill"
                    style={{ fontSize: '12px' }}
                  >
                    {org.nombre}
                    <button
                      type="button"
                      className="btn-close btn-close-sm"
                      style={{ fontSize: '7px', filter: 'none', opacity: 0.7 }}
                      onClick={() => handleRemoveOrg(org.idOrganizador)}
                    ></button>
                  </span>
                ))}
              </div>
            )}
          </div>
        </div>

        <div className="modal-footer border-top px-4 py-3">
          <button type="button" className="btn btn-link text-secondary text-decoration-none" data-bs-dismiss="modal">
            {t('common.cancel')}
          </button>
          <button
            type="button"
            className="btn btn-primary rounded-pill px-4 d-flex align-items-center gap-2"
            onClick={handleSave}
            disabled={isLoading}
          >
            {isLoading ? (
              <>
                <span className="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span>
                {t('editEvent.updating')}
              </>
            ) : (
              <>
                <i className="bi bi-check-circle"></i>
                {t('editEvent.updateEvent')}
              </>
            )}
          </button>
        </div>
      </form>
    </div>
  </div>
  )
}

export default EditarEventoModal
