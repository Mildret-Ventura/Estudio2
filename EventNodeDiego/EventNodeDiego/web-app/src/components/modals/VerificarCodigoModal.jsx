function VerificarCodigoModal() {
  return (
    <div className="modal fade" id="verifyCodeModal" tabIndex="-1" aria-labelledby="verifyCodeModalLabel" aria-hidden="true">
      <div className="modal-dialog modal-dialog-centered modal-sm">
        <div className="modal-content border-0 rounded-4 shadow">
          <div className="modal-header border-0 pb-0 pt-3 pe-3">
            <button type="button" className="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button>
          </div>
          <div className="modal-body px-4 pb-4 pt-0 text-center">
            <div className="rounded-circle bg-primary bg-opacity-10 d-inline-flex align-items-center justify-content-center mb-3"
              style={{ width: '48px', height: '48px' }}>
              <i className="bi bi-envelope-check text-primary fs-4"></i>
            </div>
            <h5 className="fw-bold mb-2">Verificar Código</h5>
            <p className="text-secondary small mb-4">
              Ingresa el código de 6 dígitos que enviamos a tu correo electrónico
            </p>

            <div className="d-flex justify-content-center gap-2 mb-4">
              <input type="text" className="form-control text-center fw-bold" maxLength="1" style={{ width: '44px', height: '48px' }} />
              <input type="text" className="form-control text-center fw-bold" maxLength="1" style={{ width: '44px', height: '48px' }} />
              <input type="text" className="form-control text-center fw-bold" maxLength="1" style={{ width: '44px', height: '48px' }} />
              <input type="text" className="form-control text-center fw-bold" maxLength="1" style={{ width: '44px', height: '48px' }} />
              <input type="text" className="form-control text-center fw-bold" maxLength="1" style={{ width: '44px', height: '48px' }} />
              <input type="text" className="form-control text-center fw-bold" maxLength="1" style={{ width: '44px', height: '48px' }} />
            </div>

            <button
              className="btn btn-primary rounded-pill w-100 fw-semibold mb-3"
              data-bs-dismiss="modal"
              data-bs-toggle="modal"
              data-bs-target="#codeVerifiedModal"
            >
              Verificar
            </button>

            <p className="text-secondary small mb-0">
              ¿No recibiste el código?{' '}
              <button className="btn btn-link text-primary text-decoration-underline small p-0 border-0">
                Reenviar código
              </button>
            </p>
          </div>
        </div>
      </div>
    </div>
  )
}

export default VerificarCodigoModal
