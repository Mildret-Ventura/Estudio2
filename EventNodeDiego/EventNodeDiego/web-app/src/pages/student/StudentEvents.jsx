import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { useTranslation } from '../../i18n/I18nContext'
import { eventService } from '../../services/eventService'
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

function StudentEvents() {
  const { t } = useTranslation()
  const [eventos, setEventos] = useState([])
  const [categorias, setCategorias] = useState([])
  const [loading, setLoading] = useState(true)
  const [search, setSearch] = useState('')
  const [activeCategory, setActiveCategory] = useState('')
  const [dateFrom, setDateFrom] = useState('')
  const [dateTo, setDateTo] = useState('')

  const fetchEventos = async () => {
    try {
      const data = await eventService.getEventos(undefined, undefined, undefined, undefined)
      const visibles = data.filter(e => e.estado === 'ACTIVO' || e.estado === 'PRÓXIMO')
      const mapped = visibles.map((e, index) => ({
        id: e.idEvento,
        image: e.banner && e.banner.startsWith('data:image/') ? e.banner : fallbackImages[index % fallbackImages.length],
        title: e.nombre,
        fechaInicio: e.fechaInicio,
        date: e.fechaInicio ? new Date(e.fechaInicio).toLocaleDateString('es-MX', { day: '2-digit', month: 'short', year: 'numeric' }) + ' • ' + new Date(e.fechaInicio).toLocaleTimeString('es-MX', { hour: '2-digit', minute: '2-digit' }) : '',
        location: e.ubicacion,
        category: e.categoriaNombre || 'GENERAL',
        categoriaId: e.categoriaId,
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

  useEffect(() => {
    fetchEventos()

    const fetchCategorias = async () => {
      try {
        const data = await eventService.getCategorias()
        setCategorias(data)
      } catch {
        setCategorias([])
      }
    }
    fetchCategorias()
  }, [])

  const filtered = eventos.filter(e => {
    const matchesSearch = !search || e.title.toLowerCase().includes(search.toLowerCase())
    const matchesCategory = !activeCategory || e.category === activeCategory
    const key = localDateKey(e.fechaInicio)
    const matchesFrom = !dateFrom || (key && key >= dateFrom)
    const matchesTo = !dateTo || (key && key <= dateTo)
    return matchesSearch && matchesCategory && matchesFrom && matchesTo
  })

  const hasActiveFilters = Boolean(search || activeCategory || dateFrom || dateTo)

  const handleCategoryChange = (catName) => {
    setActiveCategory(catName === activeCategory ? '' : catName)
  }

  return (
    <div>
      <div className="d-flex flex-column flex-md-row justify-content-between align-items-stretch align-items-md-start gap-3 mb-3">
        <div className="flex-grow-1 min-w-0">
          <h2 className="fw-bold mb-1">{t('studentEvents.title')}</h2>
          <p className="text-secondary small mb-0">
            {t('studentEvents.subtitle')}
          </p>
        </div>
        <div
          className="flex-shrink-0 w-100 w-md-auto px-1"
          style={{ maxWidth: '36rem' }}
        >
        <div className="d-flex flex-wrap justify-content-center justify-content-md-end align-items-center gap-2 gap-sm-3 w-100 mb-2">
          <div className="d-flex align-items-center gap-1 flex-shrink-0">
            <label htmlFor="student-events-date-from" className="small text-secondary mb-0 text-nowrap">
              {t('studentEvents.dateFrom')}
            </label>
            <input
              id="student-events-date-from"
              type="date"
              className="form-control form-control-sm shadow-sm rounded-3"
              style={{ width: '9.5rem' }}
              value={dateFrom}
              onChange={(e) => setDateFrom(e.target.value)}
              max={dateTo || undefined}
            />
          </div>
          <div className="d-flex align-items-center gap-1 flex-shrink-0">
            <label htmlFor="student-events-date-to" className="small text-secondary mb-0 text-nowrap">
              {t('studentEvents.dateTo')}
            </label>
            <input
              id="student-events-date-to"
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
          <span className="input-group-text bg-white border-end-0 border-0">
            <i className="bi bi-search text-secondary"></i>
          </span>
          <input
            type="text"
            className="form-control border-start-0 border-0 shadow-none"
            placeholder={t('studentEvents.searchPlaceholder')}
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
        </div>
        </div>
      </div>

      <ul className="nav nav-tabs mb-3">
        <li className="nav-item">
          <Link to="/estudiante/eventos" className="nav-link active fw-semibold small">
            {t('studentEvents.exploreTab')}
          </Link>
        </li>
        <li className="nav-item">
          <Link to="/estudiante/mis-eventos" className="nav-link text-secondary small">
            {t('studentEvents.myEventsTab')}
          </Link>
        </li>
      </ul>

      {/* Category filter pills */}
      <div className="d-flex gap-2 mb-4 flex-wrap">
        <button
          className={`btn btn-sm rounded-pill px-3 fw-semibold ${!activeCategory ? 'btn-primary' : 'btn-outline-secondary'}`}
          onClick={() => setActiveCategory('')}
        >
          {t('studentEvents.allCategories')}
        </button>
        {categorias.map(cat => (
          <button
            key={cat.idCategoria}
            className={`btn btn-sm rounded-pill px-3 fw-semibold ${activeCategory === cat.nombre ? 'btn-primary' : 'btn-outline-secondary'}`}
            onClick={() => handleCategoryChange(cat.nombre)}
          >
            {cat.nombre}
          </button>
        ))}
      </div>

      {loading ? (
        <div className="text-center py-5">
          <div className="spinner-border text-primary" role="status">
            <span className="visually-hidden">Cargando...</span>
          </div>
        </div>
      ) : filtered.length > 0 ? (
        <div className="row g-3 mb-4">
          {filtered.map(event => (
            <div className="col-12 col-md-6 col-lg-4" key={event.id}>
              <EventCard
                image={event.image}
                title={event.title}
                date={event.date}
                location={event.location}
                category={event.category}
                status={event.status}
                capacityCurrent={event.capacityCurrent}
                capacityMax={event.capacityMax}
                detailUrl={`/estudiante/evento/${event.id}`}
              />
            </div>
          ))}
        </div>
      ) : (
        <div className="card border-0 shadow-sm rounded-4">
          <div className="card-body text-center py-5">
            <div className="rounded-circle bg-primary bg-opacity-10 d-inline-flex align-items-center justify-content-center mb-3" style={{ width: '64px', height: '64px' }}>
              <i className="bi bi-calendar-plus text-primary fs-3"></i>
            </div>
            <h6 className="fw-bold mb-1">{t('studentEvents.noEvents')}</h6>
            <p className="text-secondary small mb-0">
              {hasActiveFilters ? t('studentEvents.noEventsFiltered') : t('studentEvents.noEventsMsg')}
            </p>
          </div>
        </div>
      )}
    </div>
  )
}

export default StudentEvents
