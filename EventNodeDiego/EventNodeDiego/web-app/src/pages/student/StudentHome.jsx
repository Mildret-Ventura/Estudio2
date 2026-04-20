import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { useTranslation } from '../../i18n/I18nContext'
import { eventService } from '../../services/eventService'
import { diplomaService } from '../../services/diplomaService'
import { authService } from '../../services/authService'
import EventCard from '../../components/EventCard'
import eventAi from '../../assets/events/event_ai.png'
import eventMarketing from '../../assets/events/event_marketing.png'
import eventUiux from '../../assets/events/event_uiux.png'

const fallbackImages = [eventAi, eventMarketing, eventUiux]

/** YYYY-MM-DD en calendario local (alineado con input type="date") */
function localDateKey(iso) {
  if (!iso) return ''
  const d = new Date(iso)
  if (Number.isNaN(d.getTime())) return ''
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

function StudentHome() {
  const { t } = useTranslation()
  const [eventos, setEventos] = useState([])
  const [loading, setLoading] = useState(true)
  const [searchTerm, setSearchTerm] = useState('')
  const [dateFrom, setDateFrom] = useState('')
  const [dateTo, setDateTo] = useState('')
  const [diplomas, setDiplomas] = useState([])
  const [diplomasLoading, setDiplomasLoading] = useState(true)
  const user = authService.getCurrentUser()

  useEffect(() => {
    const fetchEventos = async () => {
      try {
        const data = await eventService.getEventos(undefined, undefined, undefined, undefined)
        const visibles = data.filter(e => e.estado === 'ACTIVO' || e.estado === 'PRÓXIMO')
        const mapped = visibles.map((e, index) => ({
          id: e.idEvento,
          image: e.banner && e.banner.startsWith('data:image/') ? e.banner : (e.banner || fallbackImages[index % fallbackImages.length]),
          title: e.nombre,
          fechaInicio: e.fechaInicio,
          date: e.fechaInicio ? new Date(e.fechaInicio).toLocaleDateString('es-MX', { day: '2-digit', month: 'short', year: 'numeric' }) + ' | ' + new Date(e.fechaInicio).toLocaleTimeString('es-MX', { hour: '2-digit', minute: '2-digit' }) : '',
          location: e.ubicacion,
          detailUrl: `/estudiante/evento/${e.idEvento}`,
          status: e.estado,
          capacityCurrent: e.inscritos || 0,
          capacityMax: e.capacidadMaxima || 0,
        }))
        setEventos(mapped)
      } catch {
        setEventos([])
      } finally {
        setLoading(false)
      }
    }
    fetchEventos()
  }, [])

  useEffect(() => {
    const fetchDiplomas = async () => {
      try {
        if (user?.id) {
          const data = await diplomaService.listarDiplomasEstudiante(user.id)
          setDiplomas(data || [])
        }
      } catch {
        setDiplomas([])
      } finally {
        setDiplomasLoading(false)
      }
    }
    fetchDiplomas()
  }, [])

  const filteredEventos = eventos
    .filter(event => {
      const q = searchTerm.toLowerCase()
      const matchesSearch = !searchTerm || event.title.toLowerCase().includes(q)
      const key = localDateKey(event.fechaInicio)
      const matchesFrom = !dateFrom || (key && key >= dateFrom)
      const matchesTo = !dateTo || (key && key <= dateTo)
      return matchesSearch && matchesFrom && matchesTo
    })
    .slice(0, 3)

  const hasActiveFilters = Boolean(searchTerm || dateFrom || dateTo)

  return (
    <div>
      <div className="d-flex flex-column flex-md-row justify-content-between align-items-stretch align-items-md-start gap-3 mb-4">
        <div className="flex-grow-1 min-w-0">
          <h2 className="fw-bold mb-1">{t('studentHome.title')}</h2>
          <p className="text-secondary small mb-0">
            {t('studentHome.subtitle')}
          </p>
        </div>
        <div
          className="flex-shrink-0 w-100 w-md-auto px-1"
          style={{ maxWidth: '36rem' }}
        >
        <div className="d-flex flex-wrap justify-content-center justify-content-md-end align-items-center gap-2 gap-sm-3 w-100 mb-2">
          <div className="d-flex align-items-center gap-1 flex-shrink-0">
            <label htmlFor="student-home-date-from" className="small text-secondary mb-0 text-nowrap">
              {t('studentEvents.dateFrom')}
            </label>
            <input
              id="student-home-date-from"
              type="date"
              className="form-control form-control-sm shadow-sm rounded-3"
              style={{ width: '9.5rem' }}
              value={dateFrom}
              onChange={(e) => setDateFrom(e.target.value)}
              max={dateTo || undefined}
            />
          </div>
          <div className="d-flex align-items-center gap-1 flex-shrink-0">
            <label htmlFor="student-home-date-to" className="small text-secondary mb-0 text-nowrap">
              {t('studentEvents.dateTo')}
            </label>
            <input
              id="student-home-date-to"
              type="date"
              className="form-control form-control-sm shadow-sm rounded-3"
              style={{ width: '9.5rem' }}
              value={dateTo}
              onChange={(e) => setDateTo(e.target.value)}
              min={dateFrom || undefined}
            />
          </div>
          <div
            className="d-flex align-items-center flex-shrink-0"
            style={{ width: '8.75rem', minHeight: '31px' }}
          >
            <button
              type="button"
              className="btn btn-sm btn-outline-secondary rounded-pill w-100 text-truncate"
              style={{ visibility: dateFrom || dateTo ? 'visible' : 'hidden' }}
              tabIndex={dateFrom || dateTo ? 0 : -1}
              aria-hidden={!(dateFrom || dateTo)}
              onClick={() => { setDateFrom(''); setDateTo('') }}
            >
              {t('studentEvents.clearDates')}
            </button>
          </div>
        </div>
        <div className="input-group shadow-sm rounded-3 overflow-hidden w-100">
          <span className="input-group-text bg-white border-end-0">
            <i className="bi bi-search text-secondary"></i>
          </span>
          <input
            type="text"
            className="form-control border-start-0"
            placeholder={t('studentHome.searchPlaceholder')}
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
        </div>
        </div>
      </div>

      <h5 className="fw-bold mb-3">
        <i className="bi bi-calendar-event me-2 text-primary"></i>
        {t('studentHome.upcomingEvents')}
      </h5>
      {loading ? (
        <div className="text-center py-3">
          <div className="spinner-border text-primary spinner-border-sm" role="status">
            <span className="visually-hidden">Cargando...</span>
          </div>
        </div>
      ) : filteredEventos.length > 0 ? (
        <div className="row g-3 mb-5">
          {filteredEventos.map(event => (
            <div className="col-12 col-md-6 col-lg-4" key={event.id}>
              <EventCard
                image={event.image}
                title={event.title}
                date={event.date}
                location={event.location}
                status={event.status}
                capacityCurrent={event.capacityCurrent}
                capacityMax={event.capacityMax}
                detailUrl={event.detailUrl}
              />
            </div>
          ))}
        </div>
      ) : hasActiveFilters ? (
        <div className="card border-0 shadow-sm rounded-4 mb-5">
          <div className="card-body text-center py-5">
            <div className="rounded-circle bg-primary bg-opacity-10 d-inline-flex align-items-center justify-content-center mb-3" style={{ width: '64px', height: '64px' }}>
              <i className="bi bi-search text-primary fs-3"></i>
            </div>
            <h6 className="fw-bold mb-1">{t('studentHome.noEvents')}</h6>
            <p className="text-secondary small mb-0">
              {t('studentHome.filteredEmpty')}
            </p>
          </div>
        </div>
      ) : (
        <div className="card border-0 shadow-sm rounded-4 mb-5">
          <div className="card-body text-center py-5">
            <div className="rounded-circle bg-primary bg-opacity-10 d-inline-flex align-items-center justify-content-center mb-3" style={{ width: '64px', height: '64px' }}>
              <i className="bi bi-calendar-x text-primary fs-3"></i>
            </div>
            <h6 className="fw-bold mb-1">{t('studentHome.noUpcoming')}</h6>
            <p className="text-secondary small mb-0">
              {t('studentHome.upcomingSoon')}
            </p>
          </div>
        </div>
      )}

      <h5 className="fw-bold mb-3">
        <i className="bi bi-award me-2 text-primary"></i>
        {t('studentHome.diplomas')}
      </h5>
      {diplomasLoading ? (
        <div className="text-center py-3">
          <div className="spinner-border text-primary spinner-border-sm" role="status">
            <span className="visually-hidden">Cargando...</span>
          </div>
        </div>
      ) : diplomas.length > 0 ? (
        <div className="row g-3">
          {diplomas.map((diploma, idx) => (
            <div className="col-12 col-md-6 col-lg-4" key={idx}>
              <div className="card border-0 shadow-sm rounded-4 h-100 overflow-hidden card-hover" style={{ transition: 'all 0.2s ease' }}>
                <div className="card-body p-4 d-flex flex-column">
                  <div className="rounded-circle bg-success bg-opacity-10 d-inline-flex align-items-center justify-content-center mb-3 align-self-start" style={{ width: '48px', height: '48px' }}>
                    <i className="bi bi-award text-success fs-5"></i>
                  </div>
                  <h6 className="fw-bold mb-2">{diploma.nombre || 'Diploma'}</h6>
                  <p className="text-secondary small mb-3 flex-grow-1">
                    {diploma.descripcion || 'Certificado de participación'}
                  </p>
                  <Link
                    to="/estudiante/diplomas"
                    className="btn btn-primary btn-sm rounded-pill w-100"
                  >
                    {t('studentDiplomas.view')}
                  </Link>
                </div>
              </div>
            </div>
          ))}
        </div>
      ) : (
        <div className="card border-0 shadow-sm rounded-4">
          <div className="card-body text-center py-5">
            <div className="rounded-circle bg-primary bg-opacity-10 d-inline-flex align-items-center justify-content-center mb-3" style={{ width: '64px', height: '64px' }}>
              <i className="bi bi-award text-primary fs-3"></i>
            </div>
            <h6 className="fw-bold mb-1">{t('studentHome.noDiplomas')}</h6>
            <p className="text-secondary small mb-0">
              {t('studentHome.getDiplomas')}
            </p>
          </div>
        </div>
      )}
    </div>
  )
}

export default StudentHome
