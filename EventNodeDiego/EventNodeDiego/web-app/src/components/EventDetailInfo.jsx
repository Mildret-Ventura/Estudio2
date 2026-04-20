import { Link } from 'react-router-dom'

function EventDetailInfo({ enrolled }) {
  return (
    <div className="card border-0 shadow-sm rounded-3">
      <div className="card-body p-4">
        <h6 className="text-uppercase text-secondary small fw-bold mb-3 ls-wide">
          Detalles del Horario
        </h6>

        <div className="d-flex align-items-start gap-3 mb-3">
          <div className="rounded-circle bg-primary bg-opacity-10 d-flex align-items-center justify-content-center flex-shrink-0"
            style={{ width: '36px', height: '36px' }}>
            <i className="bi bi-calendar3 text-primary small"></i>
          </div>
          <div>
            <div className="text-secondary small">Fecha</div>
            <div className="fw-semibold small">15 de Octubre, 2024</div>
          </div>
        </div>

        <div className="d-flex align-items-start gap-3 mb-3">
          <div className="rounded-circle bg-primary bg-opacity-10 d-flex align-items-center justify-content-center flex-shrink-0"
            style={{ width: '36px', height: '36px' }}>
            <i className="bi bi-clock text-primary small"></i>
          </div>
          <div>
            <div className="text-secondary small">Tiempo</div>
            <div className="fw-semibold small">18:00 - 20:00 (GMT-5)</div>
          </div>
        </div>

        <div className="d-flex align-items-start gap-3 mb-4">
          <div className="rounded-circle bg-primary bg-opacity-10 d-flex align-items-center justify-content-center flex-shrink-0"
            style={{ width: '36px', height: '36px' }}>
            <i className="bi bi-geo-alt text-primary small"></i>
          </div>
          <div>
            <div className="text-secondary small">Ubicación</div>
            <div className="fw-semibold small">Auditorio</div>
          </div>
        </div>

        {enrolled ? (
          <Link
            to="/estudiante/eventos"
            className="btn btn-outline-danger w-100 rounded-pill fw-semibold"
          >
            Cancelar Inscripción
          </Link>
        ) : (
          <Link
            to="/estudiante/evento/1/inscrito"
            className="btn btn-primary w-100 rounded-pill fw-semibold"
          >
            Inscribirme
          </Link>
        )}
      </div>
    </div>
  )
}

export default EventDetailInfo
