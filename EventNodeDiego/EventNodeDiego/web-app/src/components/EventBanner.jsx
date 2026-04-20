import bannerImg from '../assets/events/event_banner.png'

function EventBanner() {
  return (
    <div
      className="rounded-4 overflow-hidden position-relative mb-4"
      style={{
        backgroundImage: `linear-gradient(rgba(0,0,0,0.45), rgba(0,0,0,0.55)), url(${bannerImg})`,
        backgroundSize: 'cover',
        backgroundPosition: 'center',
        minHeight: '260px'
      }}
    >
      <div className="p-4 text-white d-flex flex-column justify-content-end h-100"
        style={{ minHeight: '260px' }}>
        <h3 className="fw-bold mb-2">
          Artificial en el Aula: El Futuro hoy
        </h3>
        <p className="small mb-3 opacity-75">
          <i className="bi bi-lightbulb me-1"></i>
          Aprende a integrar herramientas de IA generativa para personalizar el aprendizaje.
        </p>
        <div className="d-flex align-items-center gap-3 mb-3 small">
          <span>
            <i className="bi bi-calendar3 me-1"></i>
            15 Octubre, 2024
          </span>
          <span>
            <i className="bi bi-clock me-1"></i>
            18:00 - 20:00 PM
          </span>
        </div>
        <div className="d-flex gap-2">
          <button className="btn btn-primary btn-sm rounded-pill px-3">
            Inscribirme ahora
          </button>
          <button className="btn btn-outline-light btn-sm rounded-pill px-3">
            Más detalles
          </button>
        </div>
      </div>
    </div>
  )
}

export default EventBanner
