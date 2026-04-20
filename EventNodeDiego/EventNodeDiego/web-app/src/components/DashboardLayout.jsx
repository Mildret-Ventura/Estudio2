import Header from './Header'
import Footer from './Footer'

function DashboardLayout({ sidebar, user, children }) {
  return (
    <div className="d-flex flex-column vh-100 w-100 overflow-hidden">
      <Header user={user} />

      <div className="d-flex flex-grow-1 w-100 overflow-hidden">
        <aside className="d-none d-md-block flex-shrink-0 overflow-auto" style={{ width: '240px' }}>
          {sidebar}
        </aside>

        <div
          className="offcanvas offcanvas-start bg-white"
          tabIndex="-1"
          id="mobileSidebar"
          aria-labelledby="mobileSidebarLabel"
          style={{ width: '240px' }}
        >
          <div className="offcanvas-header border-bottom pb-2">
            <span className="offcanvas-title fw-bold text-dark font-monospace" id="mobileSidebarLabel">
              {`{ EN }`} EventNode
            </span>
            <button
              type="button"
              className="btn-close"
              data-bs-dismiss="offcanvas"
              aria-label="Cerrar"
            ></button>
          </div>
          <div className="offcanvas-body p-0">
            {sidebar}
          </div>
        </div>

        <main className="flex-grow-1 p-3 p-md-4 overflow-auto" style={{ backgroundColor: '#f8f9fc' }}>
          {children}
          <Footer />
        </main>
      </div>
    </div>
  )
}

export default DashboardLayout
