function CodigoVerificadoModal() {
  return (
    <div className="modal fade" id="codeVerifiedModal" tabIndex="-1" aria-labelledby="codeVerifiedModalLabel" aria-hidden="true">
      <div className="modal-dialog modal-dialog-centered modal-sm">
        <div className="modal-content border-0 rounded-4 shadow">
          <div className="modal-header border-0 pb-0 pt-3 pe-3">
            <button type="button" className="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button>
          </div>
          <div className="modal-body px-4 pb-4 pt-0 text-center">
            <div className="rounded-circle bg-success bg-opacity-10 d-inline-flex align-items-center justify-content-center mb-3"
              style={{ width: '64px', height: '64px' }}>
              <i className="bi bi-check-lg text-success fs-2"></i>
            </div>
            <h5 className="fw-bold mb-2">¡Código Verificado!</h5>
            <p className="text-secondary small mb-4">
              Tu código ha sido validado correctamente.
              Ahora puedes proceder a cambiar tu contraseña.
            </p>

            <button
              className="btn btn-primary rounded-pill w-100 fw-semibold"
              data-bs-dismiss="modal"
              data-bs-toggle="modal"
              data-bs-target="#newPasswordModal"
            >
              Continuar
            </button>
          </div>
        </div>
      </div>
    </div>
  )
}

export default CodigoVerificadoModal
