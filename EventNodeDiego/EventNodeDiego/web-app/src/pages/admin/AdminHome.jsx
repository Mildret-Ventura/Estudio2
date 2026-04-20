import { useState, useEffect } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { eventService } from '../../services/eventService'
import { asistenciaService } from '../../services/asistenciaService'
import { precheckinService } from '../../services/precheckinService'
import { diplomaService } from '../../services/diplomaService'
import { useTranslation } from '../../i18n/I18nContext'

function AdminHome() {
  const { t } = useTranslation()
  const navigate = useNavigate()
  const [eventos, setEventos] = useState([])
  const [loading, setLoading] = useState(true)
  const [totalEmitidos, setTotalEmitidos] = useState(0)
  const [totalPendientes, setTotalPendientes] = useState(0)
  const [overallAttendanceRate, setOverallAttendanceRate] = useState(0)

  useEffect(() => {
    const fetchData = async () => {
      try {
        // Fetch events
        const eventosData = await eventService.getEventos()

        // Fetch diploma data
        const diplomasData = await diplomaService.listarDiplomas()
        const sumEmitidos = diplomasData.reduce((sum, d) => sum + (d.totalEmitidos || 0), 0)
        const sumPendientes = diplomasData.reduce((sum, d) => sum + (d.totalPendientes || 0), 0)
        setTotalEmitidos(sumEmitidos)
        setTotalPendientes(sumPendientes)

        // Map events and fetch capacity data
        const mapped = await Promise.all(
          eventosData.slice(0, 5).map(async (e) => {
            const inscritos = await precheckinService.contarInscritos(e.idEvento)
            const asistencias = await asistenciaService.contarAsistencias(e.idEvento)
            const capacidadMaxima = e.capacidadMaxima || 1
            const capacityPercent = Math.round((inscritos / capacidadMaxima) * 100)

            return {
              id: e.idEvento,
              name: e.nombre,
              date: e.fechaInicio ? new Date(e.fechaInicio).toLocaleDateString('es-MX', { month: 'short', day: '2-digit' }).toUpperCase() + ', ' + new Date(e.fechaInicio).toLocaleTimeString('es-MX', { hour: '2-digit', minute: '2-digit' }) : '',
              status: e.estado,
              capacityPercent: capacityPercent,
              capacityText: `${capacityPercent}%`,
              inscritos: inscritos,
              asistencias: asistencias,
              statusClass: e.estado === 'ACTIVO' ? 'bg-success text-success' : e.estado === 'CANCELADO' ? 'bg-danger text-danger' : 'bg-secondary text-secondary',
              isCancelled: e.estado === 'CANCELADO',
            }
          })
        )
        setEventos(mapped)

        // Calculate overall attendance rate
        if (mapped.length > 0) {
          const totalAsistencias = mapped.reduce((sum, e) => sum + e.asistencias, 0)
          const totalInscritos = mapped.reduce((sum, e) => sum + e.inscritos, 0)
          const attendanceRate = totalInscritos > 0 ? Math.round((totalAsistencias / totalInscritos) * 100) : 0
          setOverallAttendanceRate(attendanceRate)
        }
      } catch {
        setEventos([])
        setTotalEmitidos(0)
        setTotalPendientes(0)
        setOverallAttendanceRate(0)
      } finally {
        setLoading(false)
      }
    }
    fetchData()
  }, [])

  const totalEventos = eventos.length
  const eventosActivos = eventos.filter(e => e.status === 'ACTIVO').length

  return (
    <div>
      <h2 className="fw-bold mb-4">{t('adminHome.dashboard')}</h2>

      <div className="row g-3 mb-4">
        <div className="col-12 col-md-4">
          <div className="card border-0 shadow-sm rounded-4 h-100 card-stat">
            <div className="card-body p-3">
              <div className="d-flex justify-content-between align-items-start mb-2">
                <div className="rounded-3 bg-primary bg-opacity-10 d-flex align-items-center justify-content-center"
                  style={{ width: '40px', height: '40px' }}>
                  <i className="bi bi-people-fill text-primary"></i>
                </div>
              </div>
              <div className="text-secondary small mb-1">{t('adminHome.attendance')}</div>
              <div className="fw-bold fs-2">{overallAttendanceRate}%</div>
              <div className="text-secondary" style={{ fontSize: '12px' }}>{eventos.length > 0 ? t('adminHome.attendanceRate') : t('adminHome.noAttendanceData')}</div>
            </div>
          </div>
        </div>

        <div className="col-12 col-md-4">
          <div className="card border-0 shadow-sm rounded-4 h-100 card-stat">
            <div className="card-body p-3">
              <div className="d-flex justify-content-between align-items-start mb-2">
                <div className="rounded-3 bg-success bg-opacity-10 d-flex align-items-center justify-content-center"
                  style={{ width: '40px', height: '40px' }}>
                  <i className="bi bi-calendar-check text-success"></i>
                </div>
                {eventosActivos > 0 && (
                  <span className="badge bg-danger bg-opacity-10 text-danger small fw-semibold rounded-pill px-2">{t('adminHome.live')}</span>
                )}
              </div>
              <div className="text-secondary small mb-1">{t('adminHome.activeEvents')}</div>
              <div className="fw-bold fs-2">{eventosActivos}</div>
            </div>
          </div>
        </div>

        <div className="col-12 col-md-4">
          <div className="card border-0 shadow-sm rounded-4 h-100 card-stat" style={{ borderTop: '3px solid var(--bs-primary)' }}>
            <div className="card-body p-3">
              <div className="text-uppercase text-secondary small fw-bold mb-2" style={{ letterSpacing: '0.5px' }}>{t('adminHome.diplomas')}</div>
              <div className="d-flex justify-content-between align-items-end">
                <div>
                  <div className="fw-bold fs-3 mb-0">{totalEmitidos}</div>
                  <div className="text-secondary" style={{ fontSize: '12px' }}>{t('adminHome.emitted')}</div>
                </div>
                <div className="text-end">
                  <div className="fw-bold fs-3 mb-0">{totalPendientes}</div>
                  <div className="text-secondary" style={{ fontSize: '12px' }}>{t('adminHome.pending')}</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div className="card border-0 shadow-sm rounded-4">
        <div className="card-body p-0">
          <div className="d-flex justify-content-between align-items-center p-3 pb-2">
            <h5 className="fw-bold mb-0">{t('adminHome.eventDetails')}</h5>
            <Link to="/admin/eventos" className="text-primary text-decoration-none small fw-semibold">
              {t('adminHome.viewAll')}
            </Link>
          </div>

          {loading ? (
            <div className="text-center py-4">
              <div className="spinner-border text-primary spinner-border-sm" role="status">
                <span className="visually-hidden">Cargando...</span>
              </div>
            </div>
          ) : eventos.length > 0 ? (
            <div className="table-responsive">
              <table className="table table-hover mb-0 align-middle">
                <thead className="border-top">
                  <tr>
                    <th className="text-uppercase text-secondary small fw-semibold ps-3 py-3" style={{ fontSize: '11px' }}>{t('adminHome.eventName')}</th>
                    <th className="text-uppercase text-secondary small fw-semibold py-3" style={{ fontSize: '11px' }}>{t('adminHome.dateTime')}</th>
                    <th className="text-uppercase text-secondary small fw-semibold py-3" style={{ fontSize: '11px' }}>{t('adminHome.status')}</th>
                    <th className="text-uppercase text-secondary small fw-semibold pe-3 py-3" style={{ fontSize: '11px' }}>{t('adminHome.capacity')}</th>
                  </tr>
                </thead>
                <tbody>
                  {eventos.map(event => (
                    <tr key={event.id} style={{ cursor: 'pointer' }} onClick={() => navigate(`/admin/evento/${event.id}`)}>
                      <td className={`ps-3 py-3 fw-semibold small ${event.isCancelled ? 'text-primary' : ''}`}>{event.name}</td>
                      <td className="py-3 text-secondary small">{event.date}</td>
                      <td className="py-3">
                        <span className={`badge bg-opacity-10 rounded-pill px-3 ${event.statusClass}`}>{event.status}</span>
                      </td>
                      <td className="pe-3 py-3">
                        {event.isCancelled ? (
                          <span className="text-secondary small">{event.capacityText}</span>
                        ) : (
                          <div className="d-flex align-items-center gap-2">
                            <div className="progress flex-grow-1" style={{ height: '6px' }}>
                              <div className="progress-bar bg-primary" style={{ width: `${event.capacityPercent}%` }}></div>
                            </div>
                            <span className="text-secondary small">{event.capacityText}</span>
                          </div>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : (
            <div className="text-center py-5 px-3">
              <div className="rounded-circle bg-primary bg-opacity-10 d-inline-flex align-items-center justify-content-center mb-3" style={{ width: '56px', height: '56px' }}>
                <i className="bi bi-calendar-plus text-primary fs-4"></i>
              </div>
              <h6 className="fw-bold mb-1">{t('adminHome.noEvents')}</h6>
              <p className="text-secondary small mb-2">
                {t('adminHome.createFirstMsg')}
              </p>
              <Link to="/admin/eventos" className="btn btn-primary btn-sm rounded-pill px-4">
                {t('adminHome.createEvent')}
              </Link>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}

export default AdminHome
