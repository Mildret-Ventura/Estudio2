import avatarNatalia from '../../assets/avatar_natalia.png'

function AsistenciaExitosaModal() {
  return (
    <div className="modal fade" id="asistenciaExitosaModal" tabIndex="-1" aria-labelledby="asistenciaExitosaModalLabel" aria-hidden="true">
      <div className="modal-dialog modal-dialog-centered modal-sm">
        <div className="modal-content border-0 rounded-4 shadow p-3">
          <div className="d-flex justify-content-end mb-2">
            <button type="button" className="btn-close bg-light rounded-circle p-2" data-bs-dismiss="modal" aria-label="Cerrar" style={{ width: '10px', height: '10px', backgroundSize: '10px' }}></button>
          </div>
          <div className="modal-body text-center px-4 pt-0 pb-2">
            
            <div className="d-flex justify-content-center mb-4">
              <div className="bg-success bg-opacity-10 rounded-circle d-flex align-items-center justify-content-center" style={{ width: '80px', height: '80px' }}>
                <div className="bg-white rounded-circle d-flex align-items-center justify-content-center border border-success border-2 shadow-sm" style={{ width: '48px', height: '48px' }}>
                  <i className="bi bi-check-lg text-success fs-1"></i>
                </div>
              </div>
            </div>
            
            <h4 className="fw-bold text-dark mb-3 px-3 lh-sm">
              ¡Asistencia<br/>Exitoso!
            </h4>
            
            <p className="text-secondary small mb-4 px-2" style={{ fontSize: '12px', lineHeight: '1.5' }}>
              El estudiante ha sido registrado correctamente para el evento. Sus credenciales han sido validadas.
            </p>

            <div className="bg-light rounded-4 p-3 d-flex align-items-center gap-3 mb-4 text-start">
              <img 
                src={avatarNatalia} 
                alt="Ia Natalia" 
                className="rounded-circle object-fit-cover"
                style={{ width: '40px', height: '40px' }}
              />
              <div>
                <div className="fw-bold text-dark" style={{ fontSize: '13px' }}>Ia Natalia</div>
                <div className="text-secondary" style={{ fontSize: '11px' }}>ID: 2024300046</div>
              </div>
            </div>

            <button 
              type="button" 
              className="btn btn-primary w-100 py-2 fw-semibold rounded-3 mb-2" 
              data-bs-dismiss="modal"
            >
              Continuar
            </button>
          </div>
        </div>
      </div>
    </div>
  )
}

export default AsistenciaExitosaModal
