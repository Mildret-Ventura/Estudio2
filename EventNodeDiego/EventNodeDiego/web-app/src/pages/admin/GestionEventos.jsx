import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { toast } from 'react-toastify'
import { eventService } from '../../services/eventService'
import { useTranslation } from '../../i18n/I18nContext'
import eventConcert from '../../assets/events/event_concert.png'
import eventTechSummit from '../../assets/events/event_tech_summit.png'
import eventGala from '../../assets/events/event_gala.png'
import eventFestival from '../../assets/events/event_festival.png'
import eventWorkshop from '../../assets/events/event_workshop.png'
import CrearEventoModal from '../../components/modals/CrearEventoModal'
import EditarEventoModal from '../../components/modals/EditarEventoModal'

function AdminEventCard({ id, image, title, location, date, status, capacityCurrent, capacityMax, isFull, isFinished, onEdit, onDelete }) {
  const isActive = status === 'ACTIVO'
  const isCancelled = status === 'CANCELADO'
  const isTerminado = status === 'TERMINADO' || status === 'FINALIZADO'
  const isProximo = status === 'PRÓXIMO'
  const percent = capacityMax > 0 ? Math.round((capacityCurrent / capacityMax) * 100) : 0

  return (
    <Link
      to={`/admin/evento/${id}`}
      className="text-decoration-none text-dark"
      style={{ display: 'block' }}
    >
      <div
        className="card border-0 shadow-sm rounded-4 h-100 overflow-hidden card-hover"
        style={isFull ? { border: '2px solid #dc3545' } : {}}
      >
        <div className="position-relative">
          <img
            src={image}
            className="card-img-top"
            alt={title}
            style={{
              height: '160px',
              objectFit: 'cover',
              filter: isTerminado || isCancelled ? 'grayscale(100%)' : 'none'
            }}
          />
          <span className={`badge position-absolute top-0 start-0 m-2 rounded-pill px-3 ${
            isActive ? 'bg-primary text-white' :
            isTerminado ? 'bg-secondary text-white' :
            isProximo ? 'text-white' :
            'bg-danger text-white'
          }`} style={isProximo ? { backgroundColor: '#fd7e14' } : {}}>
            {status}
          </span>
        </div>
        <div className="card-body p-3">
          <h6 className="fw-bold mb-2">{title}</h6>
        </div>
        <div className="card-body p-3 pt-0">
          <div className="d-flex align-items-center gap-1 text-secondary small mb-3">
            <i className="bi bi-geo-alt"></i>
            <span>{location}</span>
            <span className="mx-1">•</span>
            <span>{date}</span>
          </div>

          <div className="d-flex justify-content-between align-items-center mb-2">
            <span className={`text-uppercase small fw-bold ${isFull ? 'text-danger' : isTerminado ? 'text-secondary' : 'text-dark'}`}>
              {isTerminado ? 'Finalizado' : 'Capacidad'}
            </span>
            <span className={`small fw-semibold ${isFull ? 'text-danger' : ''}`}>
              {capacityCurrent} / {capacityMax}
            </span>
          </div>
          <div className="progress" style={{ height: '4px' }}>
            <div
              className={`progress-bar ${isFull ? 'bg-danger' : isTerminado ? 'bg-secondary' : 'bg-primary'}`}
              style={{ width: `${percent}%` }}
            ></div>
          </div>
        </div>
        <div className="card-footer bg-white border-top d-flex justify-content-start gap-3 px-3 py-2">
          {(isActive || isProximo) && (
            <>
              <button
                className="btn btn-link text-secondary p-0"
                title="Editar"
                data-bs-toggle="modal"
                data-bs-target="#editarEventoModal"
                onClick={(e) => { e.preventDefault(); e.stopPropagation(); onEdit && onEdit() }}
              >
                <i className="bi bi-pencil"></i>
              </button>
            </>
          )}
          <button
            className="btn btn-link text-secondary p-0"
            title="Eliminar"
            onClick={(e) => { e.preventDefault(); e.stopPropagation(); onDelete && onDelete() }}
          >
            <i className="bi bi-trash"></i>
          </button>
        </div>
      </div>
    </Link>
  )
}

// Imágenes de fallback para cuando la API no proporciona banner
const fallbackImages = [eventConcert, eventTechSummit, eventGala, eventFestival, eventWorkshop]

// Utilidad: convierte un File a Base64 data URI
function fileToBase64(file) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = () => resolve(reader.result)
    reader.onerror = reject
    reader.readAsDataURL(file)
  })
}

const STATUS_FILTERS = [
  { label: 'Todos', value: '' },
  { label: 'PRÓXIMO', value: 'PRÓXIMO' },
  { label: 'ACTIVO', value: 'ACTIVO' },
  { label: 'CANCELADO', value: 'CANCELADO' },
  { label: 'FINALIZADO', value: 'FINALIZADO' },
]

function GestionEventos({ user }) {
  const { t } = useTranslation()
  const [eventos, setEventos] = useState([])
  const [loading, setLoading] = useState(true)
  const [errorMsg, setErrorMsg] = useState(null)

  const [categorias, setCategorias] = useState([])
  const [eventLoading, setEventLoading] = useState(false)

  // Search & Filter state
  const [searchTerm, setSearchTerm] = useState('')
  const [activeFilter, setActiveFilter] = useState('')

  // Delete modal state
  const [deleteTarget, setDeleteTarget] = useState(null)
  const [deleteLoading, setDeleteLoading] = useState(false)

  // Edit modal state
  const [editTarget, setEditTarget] = useState(null)

  const fetchEventos = async (nombre, estado) => {
    try {
      const data = await eventService.getEventos(nombre || undefined, undefined, undefined, estado || undefined)
      const mapped = data.map((e, index) => ({
        id: e.idEvento,
        image: e.banner && e.banner.startsWith('data:image/') ? e.banner : fallbackImages[index % fallbackImages.length],
        title: e.nombre,
        location: e.ubicacion,
        date: e.fechaInicio ? new Date(e.fechaInicio).toLocaleDateString('es-MX', { day: 'numeric', month: 'short' }) : '',
        status: e.estado,
        capacityCurrent: e.inscritos || 0,
        capacityMax: e.capacidadMaxima,
        isFull: e.inscritos >= e.capacidadMaxima,
        isFinished: e.estado === 'FINALIZADO',
        // Raw data for edit modal
        raw: e,
      }))
      setEventos(mapped)
    } catch (err) {
      setErrorMsg(err.message)
      setEventos([])
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchEventos(searchTerm, activeFilter)

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

  // Re-fetch when filter changes
  const handleFilterChange = (value) => {
    setActiveFilter(value)
    setLoading(true)
    fetchEventos(searchTerm, value)
  }

  // Debounced search
  useEffect(() => {
    const timer = setTimeout(() => {
      setLoading(true)
      fetchEventos(searchTerm, activeFilter)
    }, 400)
    return () => clearTimeout(timer)
  }, [searchTerm])

  const handleEventSubmit = async (formData) => {
    setEventLoading(true)
    try {
      // Convertir banner File a Base64 si existe
      let bannerBase64 = null
      if (formData.banner && formData.banner instanceof File) {
        bannerBase64 = await fileToBase64(formData.banner)
      }

      // Formatear fechas para el backend: yyyy-MM-ddTHH:mm:ss
      const fechaInicioStr = formData.fechaInicio.length === 16
        ? formData.fechaInicio + ':00'
        : formData.fechaInicio

      const fechaFinStr = formData.fechaFin.length === 16
        ? formData.fechaFin + ':00'
        : formData.fechaFin

      const payload = {
        nombre: formData.nombre.trim(),
        ubicacion: formData.ubicacion.trim(),
        descripcion: formData.descripcion.trim(),
        fechaInicio: fechaInicioStr,
        fechaFin: fechaFinStr,
        idCategoria: parseInt(formData.idCategoria),
        capacidadMaxima: parseInt(formData.capacidadMaxima),
        tiempoCancelacionHoras: parseInt(formData.tiempoCancelacionHoras),
        tiempoToleranciaMinutos: parseInt(formData.tiempoToleranciaMinutos),
        banner: bannerBase64,
        idCreador: user?.id,
        organizadores: formData.organizadores || [],
      }

      await eventService.crearEvento(payload)
      toast.success('Evento creado exitosamente')

      // Refrescar lista
      setLoading(true)
      fetchEventos(searchTerm, activeFilter)
    } catch (err) {
      toast.error(err.message)
      throw err
    } finally {
      setEventLoading(false)
    }
  }

  const handleDeleteEvent = async () => {
    if (!deleteTarget) return
    setDeleteLoading(true)
    try {
      await eventService.eliminarEvento(deleteTarget.id)
      toast.success('Evento eliminado exitosamente')
      setDeleteTarget(null)
      setLoading(true)
      fetchEventos(searchTerm, activeFilter)
    } catch (err) {
      toast.error(err.message)
    } finally {
      setDeleteLoading(false)
    }
  }

  const handleEditOpen = (evento) => {
    // Spread to create a new object reference so React always detects the change
    setEditTarget({ ...(evento.raw || evento), _ts: Date.now() })
  }

  const handleEventUpdate = async (formData) => {
    if (!editTarget) return
    const idEvento = editTarget.idEvento
    try {
      let bannerBase64 = formData.banner
      if (formData.banner && formData.banner instanceof File) {
        bannerBase64 = await fileToBase64(formData.banner)
      }

      const fechaInicioStr = formData.fechaInicio && formData.fechaInicio.length === 16
        ? formData.fechaInicio + ':00'
        : formData.fechaInicio

      const fechaFinStr = formData.fechaFin && formData.fechaFin.length === 16
        ? formData.fechaFin + ':00'
        : formData.fechaFin

      const payload = {
        nombre: formData.nombre?.trim(),
        ubicacion: formData.ubicacion?.trim(),
        descripcion: formData.descripcion?.trim(),
        fechaInicio: fechaInicioStr,
        fechaFin: fechaFinStr,
        idCategoria: formData.idCategoria ? parseInt(formData.idCategoria) : undefined,
        capacidadMaxima: formData.capacidadMaxima ? parseInt(formData.capacidadMaxima) : undefined,
        tiempoCancelacionHoras: formData.tiempoCancelacionHoras ? parseInt(formData.tiempoCancelacionHoras) : undefined,
        tiempoToleranciaMinutos: formData.tiempoToleranciaMinutos != null ? parseInt(formData.tiempoToleranciaMinutos) : undefined,
        banner: bannerBase64,
      }

      await eventService.actualizarEvento(idEvento, payload)
      toast.success('Evento actualizado exitosamente')
      setLoading(true)
      fetchEventos(searchTerm, activeFilter)
    } catch (err) {
      toast.error(err.message)
      throw err
    }
  }

  return (
    <>
      <div className="fade-in">
        <div className="d-flex flex-column flex-md-row justify-content-between align-items-start align-items-md-center mb-4 gap-2">
        <div>
          <h2 className="fw-bold mb-1">{t('events.title')}</h2>
          <p className="text-secondary small mb-0">
            {t('events.subtitle')}
          </p>
        </div>
        <button
          className="btn btn-primary rounded-pill d-flex align-items-center gap-2 flex-shrink-0"
          data-bs-toggle="modal"
          data-bs-target="#crearEventoModal"
        >
          <i className="bi bi-plus-circle"></i>
          {t('events.newEvent')}
        </button>
      </div>

      <div className="input-group mb-3 shadow-sm rounded-3 overflow-hidden" style={{ maxWidth: '400px' }}>
        <span className="input-group-text bg-white border-end-0 border-0">
          <i className="bi bi-search text-secondary"></i>
        </span>
        <input
          type="text"
          className="form-control border-start-0 border-0 shadow-none"
          placeholder={t('events.searchPlaceholder')}
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
        />
      </div>

      <div className="d-flex gap-2 mb-4 flex-wrap">
        {STATUS_FILTERS.map(f => (
          <button
            key={f.value}
            className={`btn btn-sm rounded-pill px-3 fw-semibold ${activeFilter === f.value ? 'btn-primary' : 'btn-outline-secondary'}`}
            onClick={() => handleFilterChange(f.value)}
          >
            {f.label}
          </button>
        ))}
      </div>

      <div className="row g-3">
        {loading ? (
          <div className="col-12 text-center py-5">
            <div className="spinner-border text-primary" role="status">
              <span className="visually-hidden">Cargando...</span>
            </div>
          </div>
        ) : eventos.length > 0 ? eventos.map(evento => (
          <div className="col-12 col-md-6 col-lg-4" key={evento.id}>
            <AdminEventCard
              id={evento.id}
              image={evento.image}
              title={evento.title}
              location={evento.location}
              date={evento.date}
              status={evento.status}
              capacityCurrent={evento.capacityCurrent}
              capacityMax={evento.capacityMax}
              isFull={evento.isFull}
              isFinished={evento.isFinished}
              onEdit={() => handleEditOpen(evento)}
              onDelete={() => setDeleteTarget(evento)}
            />
          </div>
        )) : (
          <div className="col-12 text-center py-5">
            <div className="rounded-circle bg-primary bg-opacity-10 d-inline-flex align-items-center justify-content-center mb-3" style={{ width: '56px', height: '56px' }}>
              <i className="bi bi-calendar-x text-primary fs-4"></i>
            </div>
            <h6 className="fw-bold mb-1">{t('events.noEvents')}</h6>
            <p className="text-secondary small mb-0">
              {searchTerm || activeFilter ? t('events.tryOtherFilters') : t('events.createFirst')}
            </p>
          </div>
        )}
      </div>
      </div>

      <CrearEventoModal
        categorias={categorias}
        isLoading={eventLoading}
        onSubmit={handleEventSubmit}
      />
      <EditarEventoModal
        evento={editTarget}
        categorias={categorias}
        onSubmit={handleEventUpdate}
      />

      {/* Delete Confirmation Modal */}
      {deleteTarget && (
        <div className="position-fixed top-0 start-0 w-100 h-100 d-flex align-items-center justify-content-center" style={{ backgroundColor: 'rgba(0,0,0,0.5)', zIndex: 1055 }}>
          <div className="bg-white border-0 rounded-4 shadow text-center p-4" style={{ maxWidth: '350px', width: '90%' }}>
              <div className="mb-3">
                <i className="bi bi-exclamation-triangle-fill text-danger" style={{ fontSize: '3rem' }}></i>
              </div>
              <h6 className="fw-bold mb-2">{t('events.deleteEvent')}</h6>
              <p className="text-secondary small mb-3">
                {t('events.deleteConfirm', { name: deleteTarget.title })}
              </p>
              <div className="d-flex justify-content-center gap-2">
                <button
                  className="btn btn-link text-secondary text-decoration-none"
                  onClick={() => setDeleteTarget(null)}
                  disabled={deleteLoading}
                >
                  {t('common.cancel')}
                </button>
                <button
                  className="btn btn-danger rounded-pill px-4"
                  onClick={handleDeleteEvent}
                  disabled={deleteLoading}
                >
                  {deleteLoading ? t('events.deleting') : t('events.delete')}
                </button>
              </div>
          </div>
        </div>
      )}
    </>
  )
}

export default GestionEventos
