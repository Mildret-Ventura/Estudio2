function IngresoManualModal() {
  return (
    <div className="modal fade" id="ingresoManualModal" tabIndex="-1" aria-labelledby="ingresoManualModalLabel" aria-hidden="true">
      <div className="modal-dialog modal-dialog-centered modal-sm">
        <div className="modal-content border-0 rounded-4 shadow">
          <div className="modal-body p-4 pb-0">
            <div className="d-flex justify-content-between align-items-start mb-3">
              <div className="bg-primary bg-opacity-10 text-primary rounded d-flex align-items-center justify-content-center" style={{ width: '32px', height: '32px' }}>
                <i className="bi bi-person-plus-fill"></i>
              </div>
              <button type="button" className="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button>
            </div>
            
            <h5 className="fw-bold text-dark mb-2">Ingreso Manual de Matrícula</h5>
            <p className="text-secondary small mb-4">
              Ingrese la matrícula del estudiante para registrar su asistencia al evento. El sistema validará los datos automáticamente.
            </p>

            <div className="mb-3">
              <label className="form-label text-uppercase text-secondary fw-bold" style={{ fontSize: '11px', letterSpacing: '0.5px' }}>
                MATRÍCULA
              </label>
              <div className="input-group">
                <span className="input-group-text bg-transparent border-end-0 text-secondary pe-2">
                  <i className="bi bi-person-badge"></i>
                </span>
                <input
                  type="text"
                  className="form-control border-start-0 ps-0 text-secondary"
                  placeholder="Ej: 12345678"
                  style={{ boxShadow: 'none' }}
                />
              </div>
            </div>

            <div className="alert alert-light bg-light border-0 d-flex gap-2 p-3 rounded-3" role="alert">
              <i className="bi bi-info-circle text-primary mt-1" style={{ fontSize: '14px' }}></i>
              <div className="text-secondary" style={{ fontSize: '11px', lineHeight: '1.4' }}>
                Asegúrese de que el número coincida exactamente con la matrícula del alumno.
              </div>
            </div>
          </div>
          <div className="modal-footer border-0 p-4 pt-2">
            <button type="button" className="btn btn-white border flex-grow-1 py-2 fw-semibold" data-bs-dismiss="modal">
              Cancelar
            </button>
            <button
              type="button"
              className="btn btn-primary flex-grow-1 py-2 fw-semibold"
              data-bs-toggle="modal"
              data-bs-target="#asistenciaExitosaModal"
            >
              Confirmar
            </button>
          </div>
        </div>
      </div>
    </div>
  )
}

export default IngresoManualModal
