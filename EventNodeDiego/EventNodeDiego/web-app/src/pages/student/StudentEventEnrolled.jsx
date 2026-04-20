import { useState, useEffect } from 'react'
import { useParams, Link, useNavigate } from 'react-router-dom'
import { toast } from 'react-toastify'
import { useTranslation } from '../../i18n/I18nContext'
import { eventService } from '../../services/eventService'
import { precheckinService } from '../../services/precheckinService'
import { authService } from '../../services/authService'
import eventDetailImg from '../../assets/events/event_detail.png'

function StudentEventEnrolled() {
  const { t } = useTranslation()
  const { id } = useParams()
  const [evento, setEvento] = useState(null)
  const [loading, setLoading] = useState(true)
  const [canceling, setCanceling] = useState(false)
  const user = authService.getCurrentUser()
  const navigate = useNavigate()

  useEffect(() => {
    const fetchData = async () => {
      try {
        const data = await eventService.getEvento(id)
        setEvento(data)
      } catch (err) {
        toast.error('Error cargando el evento')
        setEvento(null)
      } finally {
        setLoading(false)
      }
    }
    fetchData()
  }, [id])

  const handleCancelarInscripcion = async () => {
    if (!user?.id) return toast.error('Debes iniciar sesión')

    setCanceling(true)
    try {
      await precheckinService.cancelarInscripcion(user.id, parseInt(id))
      toast.info('Inscripción cancelada exitosamente')
      navigate('/estudiante/eventos')
    } catch (err) {
      toast.error(err.message)
    } finally {
      setCanceling(false)
    }
  }

  if (loading) {
    return (
      <div className="text-center py-5">
        <div className="spinner-border text-primary" role="status">
          <span className="visually-hidden">{t('eventDetail.loading')}</span>
        </div>
      </div>
    )
  }

  if (!evento) {
    return (
      <div className="text-center py-5">
        <h5>{t('eventDetail.eventNotFound')}</h5>
        <Link to="/estudiante/eventos" className="btn btn-primary btn-sm rounded-pill px-4 mt-2">
          {t('eventDetail.backToEvents')}
        </Link>
      </div>
    )
  }

  const bannerSrc = evento.banner && evento.banner.startsWith('data:image/') ? evento.banner : eventDetailImg

  return (
    <div>
      <div className="d-flex align-items-center gap-2 mb-4">
        <Link
          to="/estudiante/eventos"
          className="text-secondary text-decoration-none small d-flex align-items-center gap-1"
        >
          <i className="bi bi-arrow-left"></i>
          {t('eventDetail.back')}
        </Link>
        <span className="text-secondary small">|</span>
        <h5 className="fw-bold mb-0">{t('eventDetail.scheduleDetails')}</h5>
      </div>

      <div className="rounded-4 overflow-hidden mb-4 position-relative"
        style={{ maxHeight: '300px' }}>
        <img
          src={bannerSrc}
          alt={evento.nombre}
          className="w-100"
          style={{ objectFit: 'cover', height: '300px' }}
        />
        <div className="position-absolute bottom-0 start-0 p-4 w-100"
          style={{ background: 'linear-gradient(transparent, rgba(0,0,0,0.7))' }}>
          <div className="d-flex align-items-center gap-2 mb-2">
            <span className="badge bg-primary rounded-pill">{evento.categoriaNombre || 'GENERAL'}</span>
            <span className="badge bg-success rounded-pill">
              <i className="bi bi-check-circle me-1"></i>
              {t('eventDetail.alreadyEnrolled')}
            </span>
          </div>
          <h2 className="fw-bold text-white mb-0">{evento.nombre}</h2>
        </div>
      </div>

      <div className="row g-4">
        <div className="col-12 col-lg-8">
          <div className="card border-0 shadow-sm rounded-4 mb-4">
            <div className="card-body p-4">
              <h6 className="fw-bold mb-3">
                <i className="bi bi-info-circle text-primary me-2"></i>
                {t('eventDetail.about')}
              </h6>
              <p className="text-secondary small mb-0">
                {evento.descripcion}
              </p>
            </div>
          </div>

          {evento.organizadores && evento.organizadores.length > 0 && (
            <div className="card border-0 shadow-sm rounded-4">
              <div className="card-body p-4">
                <h6 className="fw-bold mb-3">
                  <i className="bi bi-people text-primary me-2"></i>
                  {t('eventDetail.organizers')}
                </h6>
                <div className="row g-3">
                  {evento.organizadores.map((org, idx) => (
                    <div key={idx} className="col-12">
                      <div className="d-flex align-items-start gap-3 p-3 border border-light-subtle rounded-3">
                        <div className="rounded-circle bg-primary bg-opacity-10 d-flex align-items-center justify-content-center flex-shrink-0" style={{ width: '40px', height: '40px' }}>
                          <i className="bi bi-person-fill text-primary"></i>
                        </div>
                        <div className="flex-grow-1">
                          <div className="fw-semibold text-dark small">{org.nombre}</div>
                          {org.correo && <div className="text-secondary" style={{ fontSize: '12px' }}>{org.correo}</div>}
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            </div>
          )}
        </div>

        <div className="col-12 col-lg-4">
          <div className="card border-0 shadow-sm rounded-4">
            <div className="card-body p-4">
              <h6 className="text-uppercase text-secondary small fw-bold mb-3">{t('eventDetail.scheduleDetails')}</h6>

              <div className="d-flex align-items-start gap-3 mb-3">
                <div className="rounded-circle bg-primary bg-opacity-10 d-flex align-items-center justify-content-center flex-shrink-0" style={{ width: '36px', height: '36px' }}>
                  <i className="bi bi-calendar3 text-primary small"></i>
                </div>
                <div>
                  <div className="text-secondary small">{t('eventDetail.date')}</div>
                  <div className="fw-semibold small">
                    {evento.fechaInicio ? new Date(evento.fechaInicio).toLocaleDateString('es-MX', { day: 'numeric', month: 'long', year: 'numeric' }) : '—'}
                  </div>
                </div>
              </div>

              <div className="d-flex align-items-start gap-3 mb-3">
                <div className="rounded-circle bg-primary bg-opacity-10 d-flex align-items-center justify-content-center flex-shrink-0" style={{ width: '36px', height: '36px' }}>
                  <i className="bi bi-clock text-primary small"></i>
                </div>
                <div>
                  <div className="text-secondary small">{t('eventDetail.time')}</div>
                  <div className="fw-semibold small">
                    {evento.fechaInicio && evento.fechaFin
                      ? `${new Date(evento.fechaInicio).toLocaleTimeString('es-MX', { hour: '2-digit', minute: '2-digit' })} - ${new Date(evento.fechaFin).toLocaleTimeString('es-MX', { hour: '2-digit', minute: '2-digit' })}`
                      : '—'}
                  </div>
                </div>
              </div>

              <div className="d-flex align-items-start gap-3 mb-4">
                <div className="rounded-circle bg-primary bg-opacity-10 d-flex align-items-center justify-content-center flex-shrink-0" style={{ width: '36px', height: '36px' }}>
                  <i className="bi bi-geo-alt text-primary small"></i>
                </div>
                <div>
                  <div className="text-secondary small">{t('eventDetail.location')}</div>
                  <div className="fw-semibold small">{evento.ubicacion || '—'}</div>
                </div>
              </div>

              <button
                className="btn btn-outline-danger w-100 rounded-pill fw-semibold"
                onClick={handleCancelarInscripcion}
                disabled={canceling}
              >
                {canceling ? 'Cancelando...' : t('eventDetail.cancelEnrollment')}
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}

export default StudentEventEnrolled
