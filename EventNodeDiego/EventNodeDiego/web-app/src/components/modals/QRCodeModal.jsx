import { Link } from 'react-router-dom'
import qrCodeImg from '../../assets/events/qr_code_mock.png'

function QRCodeModal() {
  return (
    <div className="modal fade" id="qrCodeModal" tabIndex="-1" aria-labelledby="qrCodeModalLabel" aria-hidden="true">
      <div className="modal-dialog modal-dialog-centered modal-sm">
        <div className="modal-content border-0 rounded-4 shadow">
          <div className="modal-header border-bottom py-3 px-4 d-flex justify-content-between align-items-center">
            <div className="d-flex align-items-center gap-2">
              {/* EventNode Logo - Simplified for modal header */}
              <div className="d-flex align-items-end" style={{ height: '20px' }}>
                <div className="bg-primary rounded-1" style={{ width: '6px', height: '8px', marginRight: '2px' }}></div>
                <div className="bg-primary rounded-1" style={{ width: '6px', height: '14px', marginRight: '2px' }}></div>
                <div className="bg-primary rounded-1" style={{ width: '6px', height: '20px', marginRight: '6px' }}></div>
                <span className="fw-bold text-dark fs-6 lh-1">EventNode</span>
              </div>
            </div>
            <button type="button" className="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button>
          </div>
          <div className="modal-body p-4 text-center pb-5">
            <div className="text-uppercase text-secondary fw-bold mb-1" style={{ fontSize: '10px', letterSpacing: '1px' }}>
              CHECK-IN
            </div>
            <h5 className="fw-bold mb-4 text-dark">Tech Summit 2023</h5>
            
            <div className="bg-light rounded-4 p-4 mx-auto mb-2 d-flex justify-content-center align-items-center" style={{ width: '220px', height: '220px' }}>
              <img 
                src={qrCodeImg} 
                alt="QR Code Tech Summit 2023" 
                className="img-fluid rounded shadow-sm"
                style={{ width: '100%', height: '100%', objectFit: 'cover' }}
              />
            </div>
          </div>
          <div className="modal-footer border-top p-3 bg-light bg-opacity-50 justify-content-between rounded-bottom-4">
            <button
              type="button"
              className="btn btn-primary d-flex align-items-center justify-content-center gap-2 px-3 fw-semibold flex-grow-1 py-2"
              data-bs-toggle="modal"
              data-bs-target="#ingresoManualModal"
            >
              <i className="bi bi-keyboard-fill"></i>
              <div className="text-start lh-sm" style={{ fontSize: '13px' }}>
                <div>Entrada</div>
                <div>manual</div>
              </div>
            </button>
            <button type="button" className="btn btn-white border bg-white text-dark fw-semibold px-4 flex-grow-1 py-3 h-100" data-bs-dismiss="modal">
              Cerrar
            </button>
          </div>
        </div>
      </div>
    </div>
  )
}

export default QRCodeModal
