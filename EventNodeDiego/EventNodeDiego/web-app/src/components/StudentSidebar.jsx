import { NavLink, Link, useLocation } from 'react-router-dom'
import { useTranslation } from '../i18n/I18nContext'

function StudentSidebar() {
  const location = useLocation()
  const { t } = useTranslation()
  const isEventosActive = location.pathname.startsWith('/estudiante/eventos') || location.pathname.startsWith('/estudiante/mis-eventos') || location.pathname.startsWith('/estudiante/evento')

  return (
    <nav className="d-flex flex-column h-100 bg-white border-end py-3">
      <div className="d-flex align-items-center gap-2 px-3 mb-4">
        <div className="rounded-circle bg-light d-flex align-items-center justify-content-center flex-shrink-0"
          style={{ width: '40px', height: '40px' }}>
          <i className="bi bi-person-fill text-secondary"></i>
        </div>
        <div className="text-truncate">
          <div className="fw-semibold small text-truncate text-dark">{t('nav.student')}</div>
          <div className="text-secondary small text-truncate">estudiante@utez.edu.mx</div>
        </div>
      </div>

      <NavLink
        to="/estudiante"
        end
        className={({ isActive }) =>
          `d-flex align-items-center gap-2 px-3 py-2 text-decoration-none ${isActive ? 'bg-primary bg-opacity-10 text-primary fw-semibold border-start border-3 border-primary' : 'text-secondary'}`
        }
      >
        <i className="bi bi-house"></i>
        {t('nav.home')}
      </NavLink>

      <Link
        to="/estudiante/eventos"
        className={`d-flex align-items-center gap-2 px-3 py-2 text-decoration-none ${isEventosActive ? 'bg-primary bg-opacity-10 text-primary fw-semibold border-start border-3 border-primary' : 'text-secondary'}`}
      >
        <i className="bi bi-calendar-event"></i>
        {t('nav.events')}
      </Link>

      <NavLink
        to="/estudiante/diplomas"
        className={({ isActive }) =>
          `d-flex align-items-center gap-2 px-3 py-2 text-decoration-none ${isActive ? 'bg-primary bg-opacity-10 text-primary fw-semibold border-start border-3 border-primary' : 'text-secondary'}`
        }
      >
        <i className="bi bi-award"></i>
        {t('nav.diplomas')}
      </NavLink>

      <NavLink
        to="/estudiante/perfil"
        className={({ isActive }) =>
          `d-flex align-items-center gap-2 px-3 py-2 text-decoration-none ${isActive ? 'bg-primary bg-opacity-10 text-primary fw-semibold border-start border-3 border-primary' : 'text-secondary'}`
        }
      >
        <i className="bi bi-person"></i>
        {t('nav.profile')}
      </NavLink>

      <div className="mt-auto px-3 py-2">
        <button
          type="button"
          className="d-flex align-items-center gap-2 text-decoration-none text-secondary small btn btn-link p-0 border-0"
          data-bs-toggle="modal"
          data-bs-target="#logoutModal"
        >
          <i className="bi bi-box-arrow-left"></i>
          {t('nav.logout')}
        </button>
      </div>
    </nav>
  )
}

export default StudentSidebar
