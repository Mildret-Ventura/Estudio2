import { Link } from 'react-router-dom'

function Header({ user }) {
  const homePath = user?.role === 'ADMIN' ? '/admin' : '/estudiante'
  
  return (
    <header className="bg-white border-bottom shadow-sm d-flex justify-content-between align-items-center px-3 py-2 d-md-none">
      <button
        className="btn btn-outline-dark border-0"
        type="button"
        data-bs-toggle="offcanvas"
        data-bs-target="#mobileSidebar"
        aria-controls="mobileSidebar"
      >
        <i className="bi bi-list fs-4"></i>
      </button>
      <Link to={homePath} className="text-decoration-none text-dark d-flex align-items-center gap-2">
        <span className="fw-bold font-monospace">{`{ EN }`}</span>
        <span className="fw-semibold">EventNode</span>
      </Link>
      <div className="invisible">
        <i className="bi bi-list fs-4"></i>
      </div>
    </header>
  )
}

export default Header
