import { Link } from 'react-router-dom'
import { useTranslation } from '../i18n/I18nContext'

function EventCard({ image, title, date, location, category, detailUrl, status, capacityCurrent, capacityMax }) {
  const { t } = useTranslation()
  const isActive = status === 'ACTIVO'
  const isCancelled = status === 'CANCELADO'
  const isFinished = status === 'FINALIZADO'
  const isProximo = status === 'PRÓXIMO'
  const percent = capacityMax > 0 ? Math.round((capacityCurrent / capacityMax) * 100) : 0
  const isFull = capacityCurrent >= capacityMax && capacityMax > 0

  return (
    <Link to={detailUrl || '#'} className="text-decoration-none text-dark">
      <div
        className="card border-0 shadow-sm h-100 rounded-4 overflow-hidden card-hover"
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
              filter: isFinished || isCancelled ? 'grayscale(100%)' : 'none'
            }}
          />
          {status && (
            <span className={`badge position-absolute top-0 start-0 m-2 rounded-pill px-3 ${
              isActive ? 'bg-primary text-white' :
              isFinished ? 'bg-secondary text-white' :
              isProximo ? 'text-white' :
              'bg-danger text-white'
            }`} style={isProximo ? { backgroundColor: '#fd7e14' } : {}}>
              {status}
            </span>
          )}
          {category && !status && (
            <span className="badge bg-primary bg-opacity-75 position-absolute top-0 start-0 m-2 rounded-pill px-3 small"
              style={{ backdropFilter: 'blur(4px)' }}>
              {category}
            </span>
          )}
        </div>
        <div className="card-body p-3">
          <h6 className="fw-bold mb-2">{title}</h6>
          <div className="d-flex align-items-center gap-1 text-secondary small mb-3">
            <i className="bi bi-geo-alt"></i>
            <span>{location}</span>
            <span className="mx-1">•</span>
            <span>{date}</span>
          </div>

          {capacityMax > 0 && (
            <>
              <div className="d-flex justify-content-between align-items-center mb-2">
                <span className={`text-uppercase small fw-bold ${isFull ? 'text-danger' : isFinished ? 'text-secondary' : 'text-dark'}`}>
                  {isFinished ? t('events.finished') : isCancelled ? t('events.cancelled') : t('events.capacity')}
                </span>
                <span className={`small fw-semibold ${isFull ? 'text-danger' : ''}`}>
                  {capacityCurrent} / {capacityMax}
                </span>
              </div>
              <div className="progress" style={{ height: '4px' }}>
                <div
                  className={`progress-bar ${isFull ? 'bg-danger' : isFinished ? 'bg-secondary' : 'bg-primary'}`}
                  style={{ width: `${percent}%` }}
                ></div>
              </div>
            </>
          )}
        </div>
      </div>
    </Link>
  )
}

export default EventCard
