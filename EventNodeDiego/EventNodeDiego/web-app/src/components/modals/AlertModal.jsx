import { useTranslation } from '../../i18n/I18nContext'

function AlertModal({ id = 'alertModal', title = 'Aviso', message, variant = 'warning' }) {
  const { t } = useTranslation()
  const iconClass = variant === 'danger' ? 'bi-x-circle text-danger' : variant === 'warning' ? 'bi-exclamation-triangle text-warning' : variant === 'success' ? 'bi-check-circle text-success' : 'bi-info-circle text-primary'

  return (
    <div className="modal fade" id={id} tabIndex="-1" aria-labelledby={`${id}Label`} aria-hidden="true">
      <div className="modal-dialog modal-dialog-centered modal-sm">
        <div className="modal-content border-0 rounded-4 shadow">
          <div className="modal-body text-center p-4">
            <div className={`bg-${variant} bg-opacity-10 rounded-circle d-inline-flex align-items-center justify-content-center mb-3`} style={{ width: '56px', height: '56px' }}>
              <i className={`bi ${iconClass} fs-3`}></i>
            </div>
            <h6 className="fw-bold mb-2" id={`${id}Label`}>{title}</h6>
            <p className="text-secondary mb-3" style={{ fontSize: '14px' }}>{message}</p>
            <button
              type="button"
              className="btn btn-primary px-4 fw-semibold rounded-pill"
              data-bs-dismiss="modal"
              style={{ fontSize: '13px' }}
            >
              {t('common.understood')}
            </button>
          </div>
        </div>
      </div>
    </div>
  )
}

export default AlertModal

// Helper function to show alert modal programmatically
export function showAlertModal(id = 'alertModal') {
  const modalEl = document.getElementById(id)
  if (modalEl && window.bootstrap) {
    const bsModal = new window.bootstrap.Modal(modalEl)
    bsModal.show()
  }
}
