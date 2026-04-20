function NuevaContrasenaModal() {
  return (
    <div className="modal fade" id="newPasswordModal" tabIndex="-1" aria-labelledby="newPasswordModalLabel" aria-hidden="true">
      <div className="modal-dialog modal-dialog-centered">
        <div className="modal-content border-0 rounded-4 shadow">
          <div className="modal-header border-0 pb-0 pt-3 px-4">
            <div className="d-flex align-items-center gap-2">
              <div className="rounded-circle bg-primary bg-opacity-10 d-flex align-items-center justify-content-center"
                style={{ width: '32px', height: '32px' }}>
                <i className="bi bi-shield-lock text-primary small"></i>
              </div>
              <h6 className="fw-bold mb-0">Establecer nueva contraseña</h6>
            </div>
            <button type="button" className="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button>
          </div>
          <div className="modal-body px-4 pb-4 pt-2">
            <p className="text-secondary small mb-4">
              Crea una contraseña segura que no hayas utilizado antes para proteger tu cuenta.
            </p>

            <div className="mb-3">
              <label className="form-label text-secondary small">Nueva contraseña</label>
              <div className="input-group">
                <input type="password" className="form-control" defaultValue="12345678" />
                <button className="btn btn-outline-secondary border-start-0" type="button">
                  <i className="bi bi-eye text-secondary"></i>
                </button>
              </div>
            </div>

            <div className="mb-3">
              <div className="text-uppercase text-secondary small fw-bold mb-2">
                Requisitos de seguridad
              </div>
              <div className="d-flex flex-column gap-1">
                <div className="d-flex align-items-center gap-2 text-secondary small">
                  <i className="bi bi-circle text-secondary" style={{ fontSize: '8px' }}></i>
                  Mínimo 8 caracteres
                </div>
                <div className="d-flex align-items-center gap-2 text-secondary small">
                  <i className="bi bi-circle text-secondary" style={{ fontSize: '8px' }}></i>
                  Al menos un número
                </div>
                <div className="d-flex align-items-center gap-2 text-secondary small">
                  <i className="bi bi-circle text-secondary" style={{ fontSize: '8px' }}></i>
                  Un carácter especial (¡#, $, etc.)
                </div>
              </div>
            </div>

            <div className="mb-4">
              <label className="form-label text-secondary small">Confirmar nueva contraseña</label>
              <div className="input-group">
                <input type="password" className="form-control" defaultValue="12345678" />
                <button className="btn btn-outline-secondary border-start-0" type="button">
                  <i className="bi bi-eye text-secondary"></i>
                </button>
              </div>
            </div>

            <button
              className="btn btn-primary rounded-pill w-100 fw-semibold mb-2"
              data-bs-dismiss="modal"
              data-bs-toggle="modal"
              data-bs-target="#passwordUpdatedModal"
            >
              Restablecer contraseña
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

export default NuevaContrasenaModal
