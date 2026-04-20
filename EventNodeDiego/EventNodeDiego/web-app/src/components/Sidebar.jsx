import { NavLink, Link, useLocation } from 'react-router-dom'
import { useTranslation } from '../i18n/I18nContext'

function Sidebar({ menuItems, user }) {
  const { t } = useTranslation()
  const location = useLocation()

  const userName = user?.name || 'Usuario'
  const userInitials = userName.split(' ').filter(Boolean).map(n => n[0]).join('').toUpperCase().slice(0, 2)
  const rolLabel = user?.role === 'ADMIN'
    ? (user?.originalRole === 'SUPERADMIN' ? 'Super Administrador' : 'Administrador')
    : 'Estudiante'

  return (
    <nav className="d-flex flex-column h-100 bg-white border-end py-3">
      <div className="d-flex align-items-center gap-2 px-3 mb-4">
        <div className="rounded-circle border border-2 border-primary bg-primary bg-opacity-10 d-flex align-items-center justify-content-center flex-shrink-0 fw-bold text-primary avatar-ring"
          style={{ width: '42px', height: '42px', fontSize: '14px' }}>
          {userInitials}
        </div>
        <div className="text-truncate">
          <div className="fw-semibold small text-truncate text-dark">{userName}</div>
          <div className="text-secondary text-truncate" style={{ fontSize: '11px' }}>{rolLabel}</div>
        </div>
      </div>

      <div className="px-2">
        {menuItems.map((item, index) => {
          const isEventsLink = item.path.includes('/eventos')
          const isEventsActive = isEventsLink && (
            location.pathname.startsWith(`${item.path.replace('/eventos', '')}/evento`) ||
            location.pathname.startsWith(`${item.path.replace('/eventos', '')}/mis-eventos`)
          )

          return (
            <NavLink
              key={index}
              to={item.path}
              end={item.exact}
              className={({ isActive }) =>
                `sidebar-link d-flex align-items-center gap-2 px-3 py-2 text-decoration-none mb-1 ${(isActive || isEventsActive) ? 'active-link' : 'text-secondary'}`
              }
            >
              <i className={item.icon}></i>
              {item.label}
            </NavLink>
          )
        })}
      </div>

      <div className="mt-auto px-3 py-2">
        <button
          type="button"
          className="sidebar-link d-flex align-items-center gap-2 text-decoration-none text-secondary small btn btn-link p-0 border-0 px-2 py-1"
          data-bs-toggle="modal"
          data-bs-target="#logoutModal"
          style={{ fontSize: '13px' }}
        >
          <i className="bi bi-box-arrow-left"></i>
          {t('nav.logout')}
        </button>
      </div>
    </nav>
  )
}

export default Sidebar
