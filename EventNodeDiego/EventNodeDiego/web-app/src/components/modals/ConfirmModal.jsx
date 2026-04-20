import { useEffect, useRef } from 'react'
import { useTranslation } from '../../i18n/I18nContext'

function ConfirmModal({ id = 'confirmModal', title, message, confirmText = 'Confirmar', cancelText = 'Cancelar', onConfirm, isLoading = false, variant = 'danger' }) {
  const { t } = useTranslation()
  const closeBtnRef = useRef(null)
  const btnClass = variant === 'danger' ? 'btn-danger' : variant === 'warning' ? 'btn-warning' : 'btn-primary'
  const iconClass = variant === 'danger' ? 'bi-exclamation-triangle text-danger' : variant === 'warning' ? 'bi-exclamation-circle text-warning' : 'bi-question-circle text-primary'

  // Fix aria-hidden focus issue: blur active element when modal hides
  useEffect(() => {
    const modalEl = document.getElementById(id)
    if (!modalEl) return
    const handleHide = () => {
      if (document.activeElement && modalEl.contains(document.activeElement)) {
        document.activeElement.blur()
      }
    }
    modalEl.addEventListener('hide.bs.modal', handleHide)
    return () => modalEl.removeEventListener('hide.bs.modal', handleHide)
  }, [id])

  const handleConfirm = async () => {
    try {
      await onConfirm()
      closeBtnRef.current?.click()
    } catch {
      // El error ya fue mostrado por el padre
    }
  }

  return (
    <div className="modal fade" id={id} tabIndex="-1" aria-labelledby={`${id}Label`} aria-hidden="true">
      <div className="modal-dialog modal-dialog-centered">
        <div className="modal-content border-0 rounded-4 shadow">
          <div className="modal-header border-0 px-4 pt-4 pb-2">
            <div className="d-flex align-items-center gap-3">
              <div className={`bg-${variant} bg-opacity-10 rounded-circle d-flex align-items-center justify-content-center`} style={{ width: '44px', height: '44px' }}>
                <i className={`bi ${iconClass} fs-5`}></i>
              </div>
              <h5 className="fw-bold mb-0" id={`${id}Label`}>{title}</h5>
            </div>
            <button type="button" className="btn-close" data-bs-dismiss="modal" aria-label="Cerrar" ref={closeBtnRef}></button>
          </div>
          <div className="modal-body px-4 py-3">
            <p className="text-secondary mb-0" style={{ fontSize: '14px' }}>{message}</p>
          </div>
          <div className="modal-footer border-0 px-4 pb-4 pt-2 d-flex justify-content-end gap-2">
            <button
              type="button"
              className="btn btn-outline-secondary px-4 fw-semibold rounded-pill"
              data-bs-dismiss="modal"
              style={{ fontSize: '13px' }}
            >
              {cancelText}
            </button>
            <button
              type="button"
              className={`btn ${btnClass} px-4 fw-semibold rounded-pill`}
              onClick={handleConfirm}
              disabled={isLoading}
              style={{ fontSize: '13px' }}
            >
              {isLoading ? (
                <><span className="spinner-border spinner-border-sm me-1" role="status"></span>{t('common.processing')}</>
              ) : confirmText}
            </button>
          </div>
        </div>
      </div>
    </div>
  )
}

export default ConfirmModal
