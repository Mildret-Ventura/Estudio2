function RestablecerContrasenaModal() {
  return (
    <div className="modal fade" id="resetPasswordModal" tabIndex="-1" aria-labelledby="resetPasswordModalLabel" aria-hidden="true">
      <div className="modal-dialog modal-dialog-centered modal-sm">
        <div className="modal-content border-0 rounded-4 shadow">
          <div className="modal-header border-0 pb-0 pt-3 pe-3">
            <button type="button" className="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button>
          </div>
          <div className="modal-body px-4 pb-4 pt-0 text-center">
            <div className="rounded-circle bg-primary bg-opacity-10 d-inline-flex align-items-center justify-content-center mb-3"
              style={{ width: '48px', height: '48px' }}>
              <i className="bi bi-arrow-repeat text-primary fs-4"></i>
            </div>
            <h5 className="fw-bold mb-2">Restablecer Contraseña</h5>
            <p className="text-secondary small mb-4">
              Ingresa tu correo electrónico y te enviaremos un enlace seguro para recuperar el acceso a tu cuenta.
            </p>

            <div className="text-start mb-3">
              <label className="form-label text-secondary small">Correo electrónico</label>
              <div className="input-group">
                <span className="input-group-text bg-light border-end-0">
                  <i className="bi bi-envelope text-secondary"></i>
                </span>
                <input
                  type="email"
                  className="form-control bg-light border-start-0"
                  placeholder="ejemplo@correo.com"
                />
              </div>
            </div>

            <button
              className="btn btn-primary rounded-pill w-100 fw-semibold mb-2"
              data-bs-dismiss="modal"
              data-bs-toggle="modal"
              data-bs-target="#verifyCodeModal"
            >
              Enviar enlace
            </button>
            <button
              type="button"
              className="btn btn-link text-secondary text-decoration-none small w-100"
              data-bs-dismiss="modal"
            >
              Cancelar
            </button>
          </div>
        </div>
      </div>
    </div>
  )
}

export default RestablecerContrasenaModal
