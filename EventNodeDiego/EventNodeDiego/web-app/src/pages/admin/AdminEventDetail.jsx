import { useState, useEffect } from 'react'
import { Link, useParams } from 'react-router-dom'
import { toast } from 'react-toastify'
import { eventService } from '../../services/eventService'
import { precheckinService } from '../../services/precheckinService'
import { asistenciaService } from '../../services/asistenciaService'
import { diplomaService } from '../../services/diplomaService'
import { useTranslation } from '../../i18n/I18nContext'
import eventTechSummit from '../../assets/events/event_tech_summit.png'

function AdminEventDetail() {
  const { id } = useParams()
  const { t } = useTranslation()
  const [evento, setEvento] = useState(null)
  const [loading, setLoading] = useState(true)
  const [inscritos, setInscritos] = useState(0)
  const [asistencias, setAsistencias] = useState(0)
  const [cancelling, setCancelling] = useState(false)
  const [emitting, setEmitting] = useState(false)
  const [reactivating, setReactivating] = useState(false)

  useEffect(() => {
    const fetchData = async () => {
      try {
        const data = await eventService.getEvento(id)
        setEvento(data)
        setInscritos(data.inscritos || 0)
        setAsistencias(data.asistencias || 0)
      } catch {
        setEvento(null)
      } finally {
        setLoading(false)
      }
    }
    fetchData()
  }, [id])

  const handleCancelEvent = async () => {
    setCancelling(true)
    try {
      await eventService.cancelarEvento(id)
      toast.success('Evento cancelado')
      setEvento(prev => ({ ...prev, estado: 'CANCELADO' }))
    } catch (err) {
      toast.error(err.message)
    } finally {
      setCancelling(false)
    }
  }

  const handleReactivateEvent = async () => {
    setReactivating(true)
    try {
      await eventService.reactivarEvento(id)
      toast.success(t('eventDetail.eventReactivated') || 'Evento reactivado')
      setEvento(prev => ({ ...prev, estado: 'ACTIVO' }))
    } catch (err) {
      toast.error(err.message)
    } finally {
      setReactivating(false)
    }
  }

  const handleEmitirDiplomas = async () => {
    setEmitting(true)
    try {
      // Check if a diploma exists for this event
      const diplomas = await diplomaService.listarDiplomas()
      const diploma = diplomas.find(d => d.idEvento === parseInt(id))

      if (!diploma) {
        toast.warning('Este evento no tiene un diploma asociado. Ve a Gestión de Diplomas para crear uno primero.')
        setEmitting(false)
        return
      }

      if (!diploma.tienePlantilla) {
        toast.warning('El diploma no tiene una plantilla PDF configurada. Edítalo en Gestión de Diplomas.')
        setEmitting(false)
        return
      }

      const result = await diplomaService.emitirDiplomas(diploma.idDiploma)
      if (result.totalErrores > 0 && result.totalEmitidos === 0) {
        toast.error(`Error al generar diplomas: ${result.primerError || 'Error desconocido'}`)
      } else if (result.totalErrores > 0) {
        toast.warning(`${result.totalEmitidos} enviados, ${result.totalErrores} con error: ${result.primerError}`)
      } else {
        toast.success(`${result.totalEmitidos} diploma(s) emitido(s) y enviado(s) por correo`)
      }
    } catch (err) {
      toast.error(err.message)
    } finally {
      setEmitting(false)
    }
  }

  if (loading) {
    return (
      <div className="text-center py-5 fade-in">
        <div className="spinner-border text-primary" role="status">
          <span className="visually-hidden">Cargando...</span>
        </div>
      </div>
    )
  }

  if (!evento) {
    return (
      <div className="text-center py-5">
        <h5>{t('eventDetail.eventNotFound')}</h5>
        <Link to="/admin/eventos" className="btn btn-primary btn-sm rounded-pill px-4 mt-2">{t('eventDetail.backToEvents')}</Link>
      </div>
    )
  }

  const bannerSrc = evento.banner && evento.banner.startsWith('data:image/') ? evento.banner : eventTechSummit
  const capacityPercent = evento.capacidadMaxima > 0 ? Math.round((inscritos / evento.capacidadMaxima) * 100) : 0
  const isActive = evento.estado === 'ACTIVO'

  return (
    <div className="fade-in">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <div className="d-flex align-items-center gap-2">
          <Link to="/admin/eventos" className="text-dark text-decoration-none">
            <i className="bi bi-arrow-left fs-5"></i>
          </Link>
          <h5 className="fw-bold mb-0">{evento.nombre}</h5>
        </div>
      </div>

      <div className="rounded-4 overflow-hidden mb-4 position-relative shadow" style={{ height: '240px' }}>
        <img src={bannerSrc} alt={evento.nombre} className="w-100 h-100" style={{ objectFit: 'cover', filter: !isActive ? 'grayscale(100%) brightness(0.4)' : 'brightness(0.4)' }} />
        <div className="position-absolute bottom-0 start-0 p-4 w-100" style={{ background: 'linear-gradient(transparent 0%, rgba(0,0,0,0.75) 100%)' }}>
          <div className="d-flex gap-2 mb-2">
            <span className={`badge rounded-pill px-3 small ${isActive ? 'bg-success text-white' : 'bg-danger text-white'}`}>{evento.estado.charAt(0).toUpperCase() + evento.estado.slice(1).toLowerCase()}</span>
            <span className="badge rounded-pill px-3 small text-white" style={{ backgroundColor: '#7c3aed' }}>{evento.categoriaNombre || 'GENERAL'}</span>
          </div>
          <h2 className="text-white fw-bold mb-2">{evento.nombre}</h2>
          <p className="text-white text-opacity-75 small mb-0" style={{ maxWidth: '500px' }}>{evento.descripcion}</p>
        </div>
      </div>

      <div className="row g-3 mb-4">
        <div className="col-12 col-md-4">
          <div className="card border-0 shadow-sm rounded-4 h-100 card-stat">
            <div className="card-body p-3">
              <div className="d-flex align-items-center gap-2 mb-2">
                <i className="bi bi-people-fill text-primary"></i>
                <span className="text-uppercase text-secondary small fw-bold">{t('eventDetail.capacity')}</span>
              </div>
              <div className="d-flex align-items-center gap-3">
                <span className="fw-bold fs-2">{capacityPercent}%</span>
                <div className="flex-grow-1">
                  <div className="progress" style={{ height: '6px' }}>
                    <div className={`progress-bar ${capacityPercent >= 100 ? 'bg-danger' : 'bg-primary'}`} style={{ width: `${Math.min(capacityPercent, 100)}%` }}></div>
                  </div>
                  <div className="text-secondary small mt-1">{inscritos} / {evento.capacidadMaxima}</div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div className="col-12 col-md-4">
          <div className="card border-0 shadow-sm rounded-4 h-100 card-stat">
            <div className="card-body p-3">
              <div className="d-flex align-items-center gap-2 mb-2">
                <i className="bi bi-award-fill text-primary"></i>
                <span className="text-uppercase text-secondary small fw-bold">{t('events.title')}</span>
              </div>
              <div className="fw-bold fs-2">{asistencias}</div>
            </div>
          </div>
        </div>

        <div className="col-12 col-md-4">
          {isActive ? (
            <div className="card border-0 rounded-3 h-100 bg-danger bg-opacity-10 border border-danger border-opacity-25">
              <div className="card-body p-3">
                <div className="text-uppercase text-danger small fw-bold mb-1">{t('eventDetail.cancelEvent')}</div>
                <p className="text-secondary small mb-2" style={{ fontSize: '11px' }}>
                  {t('eventDetail.cancelNotice')}
                </p>
                <button className="btn btn-danger rounded-pill w-100 btn-sm" onClick={handleCancelEvent} disabled={cancelling}>
                  {cancelling ? t('common.processing') : t('eventDetail.cancelEvent')}
                </button>
              </div>
            </div>
          ) : evento.estado === 'CANCELADO' ? (
            <div className="card border-0 rounded-3 h-100 bg-success bg-opacity-10 border border-success border-opacity-25">
              <div className="card-body p-3">
                <div className="text-uppercase text-success small fw-bold mb-1">{t('eventDetail.reactivateEvent') || 'Reactivar evento'}</div>
                <p className="text-secondary small mb-2" style={{ fontSize: '11px' }}>
                  {t('eventDetail.reactivateNotice') || 'El evento será visible nuevamente para los estudiantes.'}
                </p>
                <button className="btn btn-success rounded-pill w-100 btn-sm" onClick={handleReactivateEvent} disabled={reactivating}>
                  {reactivating ? (t('common.processing') || 'Procesando...') : (t('eventDetail.reactivateEvent') || 'Reactivar evento')}
                </button>
              </div>
            </div>
          ) : (
            <div className="card border-0 rounded-3 h-100 bg-secondary bg-opacity-10">
              <div className="card-body p-3">
                <div className="text-uppercase text-secondary small fw-bold mb-1">{t('events.status')}</div>
                <div className="fw-bold fs-4">{evento.estado.charAt(0).toUpperCase() + evento.estado.slice(1).toLowerCase()}</div>
              </div>
            </div>
          )}
        </div>
      </div>

      {/* Check-in links */}
      <div className="card border-0 shadow-sm rounded-4 mb-4">
        <div className="card-body p-4">
          <h6 className="fw-bold mb-3">
            <i className="bi bi-person-check me-2 text-primary"></i>
            {t('eventDetail.preCheckin')} / {t('eventDetail.checkin')}
          </h6>
          <div className="d-flex gap-3">
            <Link to={`/admin/evento/${id}/pre-check-in`} className="btn btn-outline-secondary btn-sm d-flex align-items-center gap-1 rounded-pill px-3 text-decoration-none">
              <i className="bi bi-person-check"></i>
              {t('eventDetail.preCheckin')} ({inscritos})
            </Link>
            <Link to={`/admin/evento/${id}/check-in`} className="btn btn-outline-secondary btn-sm d-flex align-items-center gap-1 rounded-pill px-3 text-decoration-none">
              <i className="bi bi-person-check-fill"></i>
              {t('eventDetail.checkin')} ({asistencias})
            </Link>
          </div>
        </div>
      </div>

      <div className="row g-3">
        <div className="col-12 col-md-8">
          <div className="card border-0 rounded-3 h-100 text-white" style={{ background: 'linear-gradient(135deg, #2563eb 0%, #1e40af 100%)' }}>
            <div className="card-body p-3">
              <div className="text-uppercase small fw-bold mb-1">{t('eventDetail.sendDiplomas')}</div>
              <p className="small opacity-75 mb-2" style={{ fontSize: '11px' }}>
                {t('eventDetail.diplomaInstruction')}
              </p>
              <button className="btn btn-light rounded-pill w-100 btn-sm fw-semibold text-primary" onClick={handleEmitirDiplomas} disabled={emitting}>
                {emitting ? t('eventDetail.emitting') : t('eventDetail.sendDiplomas')}
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}

export default AdminEventDetail
