import { useState, useRef, useEffect } from 'react'
import { eventService } from '../../services/eventService'
import { useTranslation } from '../../i18n/I18nContext'

function CrearEventoModal({ categorias = [], isLoading, onSubmit }) {
  const { t } = useTranslation()
  const fileInputRef = useRef(null)
  const formRef = useRef(null)
  const closeBtnRef = useRef(null)
  const [bannerPreview, setBannerPreview] = useState(null)
  const [showSuccess, setShowSuccess] = useState(false)
  const [errors, setErrors] = useState({})
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

  const resetForm = () => {
    setFormData({
      nombre: '', ubicacion: '', descripcion: '', fechaInicio: '', fechaFin: '',
      idCategoria: '', capacidadMaxima: '', tiempoCancelacionHoras: '',
      tiempoToleranciaMinutos: '', banner: null,
    })
    setBannerPreview(null)
    setSelectedOrgs([])
    setErrors({})
  }

  const handleSave = async () => {
    const validationErrors = validate()
    if (Object.keys(validationErrors).length > 0) {
      setErrors(validationErrors)
      return
    }
    try {
      const dataToSubmit = {
        ...formData,
        organizadores: selectedOrgs.map(o => o.idOrganizador),
      }
      await onSubmit(dataToSubmit)
      closeBtnRef.current?.click()
      resetForm()
      setShowSuccess(true)
    } catch {
      // error handled by parent via toast
    }
  }

  const ic = (field) => errors[field] ? 'form-control is-invalid' : 'form-control'
  const is = (field) => errors[field] ? 'form-select is-invalid' : 'form-select'

  return (
    <>
      <div className="modal fade" id="crearEventoModal" tabIndex="-1" aria-labelledby="crearEventoModalLabel" aria-hidden="true">
        <div className="modal-dialog modal-dialog-centered modal-lg modal-dialog-scrollable">
        <form ref={formRef} noValidate className="modal-content border-0 rounded-4 shadow">
          <div className="modal-header border-0 px-4 pt-4 pb-0">
            <h5 className="fw-bold">{t('createEvent.title')}</h5>
            <button type="button" className="btn-close" data-bs-dismiss="modal" aria-label={t('common.close')} ref={closeBtnRef}></button>
          </div>

          <div className="modal-body px-4 py-3">

            {/* Banner upload */}
            <div className="mb-4">
              <label className="form-label fw-semibold small">{t('createEvent.banner')}</label>
              <input type="file" ref={fileInputRef} accept="image/*" className="d-none" onChange={handleBannerChange} />
              <div
                className="d-flex flex-column align-items-center justify-content-center text-center p-4 rounded-3"
                style={{ border: `2px dashed ${bannerPreview ? '#198754' : '#dee2e6'}`, cursor: 'pointer', position: 'relative', overflow: 'hidden' }}
                onClick={handleBannerClick}
              >
                {bannerPreview ? (
                  <img src={bannerPreview} alt="Preview" style={{ position: 'absolute', top: 0, left: 0, width: '100%', height: '100%', objectFit: 'cover' }} />
                ) : (
                  <>
                    <i className="bi bi-image text-secondary fs-3 mb-2"></i>
                    <div className="text-secondary small">{t('createEvent.bannerInstruction')}</div>
                    <div className="text-secondary small" style={{ fontSize: '11px' }}>{t('createEvent.bannerRecommended')}</div>
                  </>
                )}
              </div>
            </div>

            {/* Nombre + Ubicación */}
            <div className="row g-3 mb-3">
              <div className="col-12 col-md-6">
                <label className="form-label fw-semibold small">{t('createEvent.eventName')}</label>
                <input
                  type="text"
                  name="nombre"
                  className={ic('nombre')}
                  placeholder={t('createEvent.eventNamePlaceholder')}
                  value={formData.nombre}
                  onChange={handleChange}
                  maxLength={100}
                />
                {errors.nombre && <div className="invalid-feedback">{errors.nombre}</div>}
              </div>
              <div className="col-12 col-md-6">
                <label className="form-label fw-semibold small">{t('createEvent.location')}</label>
                <input
                  type="text"
                  name="ubicacion"
                  className={ic('ubicacion')}
                  placeholder={t('createEvent.locationPlaceholder')}
                  value={formData.ubicacion}
                  onChange={handleChange}
                  maxLength={150}
                />
                {errors.ubicacion && <div className="invalid-feedback">{errors.ubicacion}</div>}
              </div>
            </div>

            {/* Descripción */}
            <div className="mb-3">
              <label className="form-label fw-semibold small">{t('createEvent.description')}</label>
              <textarea
                name="descripcion"
                className={errors.descripcion ? 'form-control is-invalid' : 'form-control'}
                rows="3"
                placeholder={t('createEvent.descriptionPlaceholder')}
                value={formData.descripcion}
                onChange={handleChange}
                maxLength={300}
              ></textarea>
              {errors.descripcion && <div className="invalid-feedback">{errors.descripcion}</div>}
            </div>

            {/* Fechas */}
            <div className="row g-3 mb-3">
              <div className="col-12 col-md-6">
                <label className="form-label fw-semibold small">{t('createEvent.startDateTime')}</label>
                <input
                  type="datetime-local"
                  name="fechaInicio"
                  className={ic('fechaInicio')}
                  value={formData.fechaInicio}
                  onChange={handleChange}
                />
                {errors.fechaInicio
                  ? <div className="invalid-feedback">{errors.fechaInicio}</div>
                  : <div className="text-secondary small mt-1" style={{ fontSize: '11px' }}>{t('createEvent.futureDate')}</div>
                }
              </div>
              <div className="col-12 col-md-6">
                <label className="form-label fw-semibold small">{t('createEvent.endDateTime')}</label>
                <input
                  type="datetime-local"
                  name="fechaFin"
                  className={ic('fechaFin')}
                  value={formData.fechaFin}
                  onChange={handleChange}
                />
                {errors.fechaFin
                  ? <div className="invalid-feedback">{errors.fechaFin}</div>
                  : <div className="text-secondary small mt-1" style={{ fontSize: '11px' }}>{t('createEvent.afterStart')}</div>
                }
              </div>
            </div>

            {/* Categoría + Capacidad */}
            <div className="row g-3 mb-3">
              <div className="col-12 col-md-6">
                <label className="form-label fw-semibold small">{t('createEvent.category')}</label>
                <select
                  name="idCategoria"
                  className={is('idCategoria')}
                  value={formData.idCategoria}
                  onChange={handleChange}
                >
                  <option value="">{t('createEvent.selectCategory')}</option>
                  {categorias.map(cat => (
                    <option key={cat.idCategoria} value={cat.idCategoria}>
                      {cat.nombre}
                    </option>
                  ))}
                </select>
                {errors.idCategoria && <div className="invalid-feedback">{errors.idCategoria}</div>}
              </div>
              <div className="col-12 col-md-6">
                <label className="form-label fw-semibold small">{t('createEvent.maxCapacity')}</label>
                <input
                  type="number"
                  name="capacidadMaxima"
                  className={ic('capacidadMaxima')}
                  placeholder={t('createEvent.capacityPlaceholder')}
                  min="1"
                  max="9999"
                  value={formData.capacidadMaxima}
                  onChange={handleChange}
                />
                {errors.capacidadMaxima && <div className="invalid-feedback">{errors.capacidadMaxima}</div>}
              </div>
            </div>

            {/* Cancelación + Tolerancia */}
            <div className="row g-3 mb-3">
              <div className="col-12 col-md-6">
                <label className="form-label fw-semibold small">{t('createEvent.cancellationTime')}</label>
                <input
                  type="number"
                  name="tiempoCancelacionHoras"
                  className={ic('tiempoCancelacionHoras')}
                  placeholder={t('createEvent.cancellationPlaceholder')}
                  min="1"
                  max="720"
                  value={formData.tiempoCancelacionHoras}
                  onChange={handleChange}
                />
                {errors.tiempoCancelacionHoras
                  ? <div className="invalid-feedback">{errors.tiempoCancelacionHoras}</div>
                  : <div className="text-secondary small mt-1" style={{ fontSize: '11px' }}>{t('createEvent.cancellationHelp')}</div>
                }
              </div>
              <div className="col-12 col-md-6">
                <label className="form-label fw-semibold small">{t('createEvent.toleranceTime')}</label>
                <input
                  type="number"
                  name="tiempoToleranciaMinutos"
                  className={ic('tiempoToleranciaMinutos')}
                  placeholder={t('createEvent.tolerancePlaceholder')}
                  min="0"
                  max="120"
                  value={formData.tiempoToleranciaMinutos}
                  onChange={handleChange}
                />
                {errors.tiempoToleranciaMinutos
                  ? <div className="invalid-feedback">{errors.tiempoToleranciaMinutos}</div>
                  : <div className="text-secondary small mt-1" style={{ fontSize: '11px' }}>{t('createEvent.toleranceHelp')}</div>
                }
              </div>
            </div>

            {/* Organizadores */}
            <div className="mb-2">
              <label className="form-label fw-semibold small">{t('createEvent.organizer')}</label>
              <select className="form-select mb-2" onChange={handleSelectOrg} defaultValue="">
                <option value="">Seleccionar organizador...</option>
                {allOrgs
                  .filter(o => !selectedOrgs.some(s => s.idOrganizador === o.idOrganizador))
                  .map(org => (
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
              className="btn btn-primary rounded-pill px-4"
              onClick={handleSave}
              disabled={isLoading}
            >
              {isLoading ? (
                <>
                  <span className="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
                  {t('createEvent.creating')}
                </>
              ) : (
                t('createEvent.saveEvent')
              )}
            </button>
          </div>
        </form>
      </div>
    </div>

    {showSuccess && (
      <div className="position-fixed top-0 start-0 w-100 h-100 d-flex align-items-center justify-content-center" style={{ backgroundColor: 'rgba(0,0,0,0.5)', zIndex: 1060 }}>
        <div className="bg-white border-0 rounded-4 shadow text-center p-4" style={{ maxWidth: '400px', width: '90%' }}>
          <div className="mb-3">
            <i className="bi bi-check-circle-fill text-success" style={{ fontSize: '3rem' }}></i>
          </div>
          <h5>{t('createEvent.eventCreated')}</h5>
          <p className="text-secondary small">{t('createEvent.eventSaved')}</p>
          <button className="btn btn-primary rounded-pill px-4 mt-2 mx-auto" onClick={() => setShowSuccess(false)}>
            {t('createEvent.accept')}
          </button>
        </div>
      </div>
    )}
    </>
  )
}

export default CrearEventoModal
