function CuentaVinculadaModal() {
  return (
    <div className="modal fade" id="verifyModal" tabIndex="-1" aria-labelledby="verifyModalLabel" aria-hidden="true">
      <div className="modal-dialog modal-dialog-centered">
        <div className="modal-content border-0 rounded-4 shadow">
          <div className="modal-body p-4 text-center">
            <div className="rounded-circle bg-primary bg-opacity-10 d-inline-flex align-items-center justify-content-center mb-3"
              style={{ width: '48px', height: '48px' }}>
              <i className="bi bi-link-45deg text-primary fs-4"></i>
            </div>
            <h5 className="fw-bold mb-2">Cuenta Vinculada</h5>
            <p className="text-secondary small mb-4">
              Tu Matrícula única ha sido vinculada con éxito. Tu historial
              académico y registros se encuentran sincronizados y actualizados.
            </p>

            <div className="text-start mb-3">
              <div className="d-flex align-items-center justify-content-between mb-2">
                <span className="text-secondary small">Matrícula Única</span>
                <span className="badge bg-success bg-opacity-10 text-success small d-flex align-items-center gap-1">
                  <i className="bi bi-check-circle-fill"></i>
                  VERIFICADO
                </span>
              </div>
              <div className="input-group">
                <span className="input-group-text bg-light border-end-0">
                  <i className="bi bi-lock text-secondary"></i>
                </span>
                <input
                  type="text"
                  className="form-control bg-light border-start-0"
                  value="20243ds01"
                  readOnly
                />
              </div>
            </div>

            <div className="d-flex align-items-center gap-1 text-success small mb-4">
              <i className="bi bi-check-circle"></i>
              Sincronización activa
            </div>

            <button
              type="button"
              className="btn btn-outline-primary rounded-pill px-5 w-100"
              data-bs-dismiss="modal"
            >
              Cerrar
            </button>
          </div>
        </div>
      </div>
    </div>
  )
}

export default CuentaVinculadaModal
