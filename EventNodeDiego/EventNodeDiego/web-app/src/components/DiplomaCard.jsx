import { Link } from 'react-router-dom'

function DiplomaCard({ image, title, date, category, detailUrl }) {
  return (
    <div className="card border-0 shadow-sm h-100 rounded-3 overflow-hidden">
      <div className="position-relative">
        <img
          src={image}
          className="card-img-top"
          alt={title}
          style={{ height: '140px', objectFit: 'cover' }}
        />
        {category && (
          <span className="badge bg-primary bg-opacity-75 position-absolute top-0 start-0 m-2 rounded-pill small">
            {category}
          </span>
        )}
      </div>
      <div className="card-body p-3">
        <h6 className="card-title fw-semibold mb-2">{title}</h6>
        <div className="d-flex align-items-center gap-1 text-secondary small">
          <i className="bi bi-calendar-check"></i>
          <span>Emitido el {date}</span>
        </div>
      </div>
      <div className="card-footer bg-white border-0 p-3 pt-0 d-flex flex-column gap-2">
        <button className="btn btn-primary btn-sm rounded-pill w-100 d-flex align-items-center justify-content-center gap-1">
          <i className="bi bi-download"></i>
          Descargar Diploma
        </button>
        <Link
          to={detailUrl || '/estudiante/diplomas/1'}
          className="btn btn-link btn-sm text-secondary text-decoration-none w-100"
        >
          Ver Diploma
        </Link>
      </div>
    </div>
  )
}

export default DiplomaCard
