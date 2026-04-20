import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { toast } from 'react-toastify'
import { useTranslation } from '../../i18n/I18nContext'
import { diplomaService } from '../../services/diplomaService'
import { eventService } from '../../services/eventService'
import { closeModal } from '../../services/apiHelper'
import CrearDiplomaModal from '../../components/modals/CrearDiplomaModal'
import EditarDiplomaModal from '../../components/modals/EditarDiplomaModal'
import ConfirmModal from '../../components/modals/ConfirmModal'

function AdminDiplomas() {
  const { t } = useTranslation()
  const [diplomas, setDiplomas] = useState([])
  const [loading, setLoading] = useState(true)
  const [eventos, setEventos] = useState([])
  const [creating, setCreating] = useState(false)
  const [emitting, setEmitting] = useState(null)
  const [editing, setEditing] = useState(false)
  const [deleting, setDeleting] = useState(null)
  const [selectedDiploma, setSelectedDiploma] = useState(null)
  const [diplomaToDelete, setDiplomaToDelete] = useState(null)
  const [formData, setFormData] = useState({
    idEvento: '',
    firma: '',
    diseno: 'Personalizado',
    plantillaPdf: '',
    firmaImagen: ''
  })

  const fetchDiplomas = async () => {
    try {
      const data = await diplomaService.listarDiplomas()
      setDiplomas(data)
    } catch {
      setDiplomas([])
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchDiplomas()
    const fetchEventos = async () => {
      try {
        const data = await eventService.getEventos()
        setEventos(data)
      } catch {
        setEventos([])
      }
    }
    fetchEventos()
  }, [])

  const handleCreateDiploma = async () => {
    if (!formData.idEvento) {
      toast.error(t('diplomas.selectEvent'))
      return
    }
    if (!formData.plantillaPdf) {
      toast.error(t('diplomas.uploadPdfTemplate'))
      return
    }
    if (!formData.firmaImagen) {
      toast.error(t('diplomas.uploadSignatureImage'))
      return
    }
    setCreating(true)
    try {
      await diplomaService.crearDiploma({
        idEvento: parseInt(formData.idEvento),
        firma: formData.firma || 'Administrador',
        diseno: 'Personalizado',
        plantillaPdf: formData.plantillaPdf,
        firmaImagen: formData.firmaImagen
      })
      toast.success(t('diplomas.diplomaCreated'))
      setFormData({ idEvento: '', firma: '', diseno: 'Personalizado', plantillaPdf: '', firmaImagen: '' })
      setLoading(true)
      fetchDiplomas()
    } catch (err) {
      toast.error(err.message)
      throw err
    } finally {
      setCreating(false)
    }
  }

  const handleEmitir = async (idDiploma) => {
    setEmitting(idDiploma)
    try {
      const result = await diplomaService.emitirDiplomas(idDiploma)
      if (result.totalErrores > 0 && result.totalEmitidos === 0) {
        toast.error(`Error al generar diplomas: ${result.primerError || 'Error desconocido'}`)
      } else if (result.totalErrores > 0) {
        toast.warning(`${result.totalEmitidos} enviados, ${result.totalErrores} con error: ${result.primerError}`)
      } else {
        toast.success(`${result.totalEmitidos} ${t('diplomas.diplomasEmittedAndSent')}`)
      }
      setLoading(true)
      fetchDiplomas()
    } catch (err) {
      toast.error(err.message)
    } finally {
      setEmitting(null)
    }
  }

  const handleUpdateDiploma = async (idDiploma, datos) => {
    setEditing(true)
    try {
      const result = await diplomaService.actualizarDiploma(idDiploma, datos)
      toast.success(result.mensaje || t('diplomas.diplomaUpdatedSuccess'))
      setSelectedDiploma(null)
      setLoading(true)
      fetchDiplomas()
    } catch (err) {
      toast.error(err.message)
      throw err
    } finally {
      setEditing(false)
    }
  }

  const handleConfirmDelete = async () => {
    if (!diplomaToDelete) return
    setDeleting(diplomaToDelete.idDiploma)
    try {
      await diplomaService.eliminarDiploma(diplomaToDelete.idDiploma)
      toast.success(t('diplomas.diplomaDeletedSuccess'))
      setDiplomaToDelete(null)
      setLoading(true)
      fetchDiplomas()
    } catch (err) {
      toast.error(err.message)
      throw err
    } finally {
      setDeleting(null)
    }
  }

  const totalCertificaciones = diplomas.length
  const totalEntregados = diplomas.reduce((sum, d) => sum + (d.totalEmitidos || 0), 0)
  const totalPendientes = diplomas.reduce((sum, d) => sum + (d.totalPendientes || 0), 0)

  return (
    <div>
      <div className="d-flex flex-column flex-md-row justify-content-between align-items-start align-items-md-center mb-4 gap-3">
        <div>
          <h2 className="fw-bold mb-1">{t('diplomas.title')}</h2>
        </div>
        <button
          className="btn btn-primary rounded-pill d-flex align-items-center gap-2 flex-shrink-0 px-4"
          data-bs-toggle="modal"
          data-bs-target="#crearDiplomaModal"
        >
          <i className="bi bi-plus-lg"></i>
          {t('diplomas.createNew')}
        </button>
      </div>

      <div className="row g-3 mb-4">
        <div className="col-12 col-md-4">
          <div className="card border-0 shadow-sm rounded-4 h-100 p-3 card-stat">
            <div className="card-body">
              <div className="d-flex justify-content-between align-items-start mb-3">
                <div className="bg-primary bg-opacity-10 text-primary rounded pt-1 px-2 pb-1 d-flex align-items-center justify-content-center">
                  <i className="bi bi-file-earmark-check"></i>
                </div>
              </div>
              <div className="text-secondary small mb-1" style={{ fontSize: '13px' }}>{t('diplomas.totalCertifications')}</div>
              <h3 className="fw-bold mb-0">{totalCertificaciones}</h3>
            </div>
          </div>
        </div>
        <div className="col-12 col-md-4">
          <div className="card border-0 shadow-sm rounded-4 h-100 p-3 card-stat">
            <div className="card-body">
              <div className="d-flex justify-content-between align-items-start mb-3">
                <div className="bg-success bg-opacity-10 text-success rounded pt-1 px-2 pb-1 d-flex align-items-center justify-content-center">
                  <i className="bi bi-check-circle"></i>
                </div>
              </div>
              <div className="text-secondary small mb-1" style={{ fontSize: '13px' }}>{t('diplomas.delivered')}</div>
              <h3 className="fw-bold mb-0">{totalEntregados}</h3>
            </div>
          </div>
        </div>
        <div className="col-12 col-md-4">
          <div className="card border-0 shadow-sm rounded-4 h-100 p-3 card-stat">
            <div className="card-body">
              <div className="d-flex justify-content-between align-items-start mb-3">
                <div className="bg-danger bg-opacity-10 text-danger rounded pt-1 px-2 pb-1 d-flex align-items-center justify-content-center">
                  <i className="bi bi-clock-history"></i>
                </div>
              </div>
              <div className="text-secondary small mb-1" style={{ fontSize: '13px' }}>{t('diplomas.pendingCerts')}</div>
              <h3 className="fw-bold mb-0">{totalPendientes}</h3>
            </div>
          </div>
        </div>
      </div>

      {loading ? (
        <div className="text-center py-5">
          <div className="spinner-border text-primary" role="status">
            <span className="visually-hidden">{t('common.loading')}</span>
          </div>
        </div>
      ) : diplomas.length > 0 ? (
        <div className="row g-3">
          {diplomas.map(d => (
            <div className="col-12 col-md-6 col-lg-4" key={d.idDiploma}>
              <div className="card border-0 shadow-sm rounded-4 h-100 card-hover">
                <div className="card-body p-4">
                  <div className="d-flex align-items-center gap-3 mb-3">
                    <div className="bg-primary bg-opacity-10 text-primary rounded-circle d-flex align-items-center justify-content-center" style={{ width: '44px', height: '44px' }}>
                      <i className="bi bi-award fs-5"></i>
                    </div>
                    <div className="flex-grow-1">
                      <h6 className="fw-bold mb-0">{d.nombreEvento}</h6>
                      <div className="d-flex gap-2 mt-1">
                        {d.tienePlantilla && (
                          <span className="badge bg-success bg-opacity-10 text-success" style={{ fontSize: '10px' }}>
                            <i className="bi bi-file-earmark-code me-1"></i>{t('diplomas.pdf')}
                          </span>
                        )}
                        {d.tieneFirma && (
                          <span className="badge bg-primary bg-opacity-10 text-primary" style={{ fontSize: '10px' }}>
                            <i className="bi bi-pen me-1"></i>{t('diplomas.signature')}
                          </span>
                        )}
                      </div>
                    </div>
                  </div>
                  <div className="d-flex justify-content-between mb-2">
                    <span className="text-secondary small">{t('diplomas.emitted')}</span>
                    <span className="fw-bold small">{d.totalEmitidos || 0}</span>
                  </div>
                  <div className="d-flex justify-content-between mb-3">
                    <span className="text-secondary small">{t('diplomas.pendingStat')}</span>
                    <span className="fw-bold small">{d.totalPendientes || 0}</span>
                  </div>
                  {(d.totalPendientes || 0) > 0 && (
                    <button
                      className="btn btn-primary btn-sm w-100 rounded-pill fw-semibold mb-2"
                      onClick={() => handleEmitir(d.idDiploma)}
                      disabled={emitting === d.idDiploma}
                    >
                      {emitting === d.idDiploma ? (
                        <><span className="spinner-border spinner-border-sm me-1" role="status"></span>{t('diplomas.sending')}</>
                      ) : (
                        <><i className="bi bi-send me-1"></i>{t('diplomas.emit')}</>
                      )}
                    </button>
                  )}
                  <div className="d-flex gap-2">
                    <button
                      className="btn btn-outline-primary btn-sm flex-grow-1 rounded-pill fw-semibold"
                      data-bs-toggle="modal"
                      data-bs-target="#editarDiplomaModal"
                      onClick={() => setSelectedDiploma(d)}
                    >
                      <i className="bi bi-pencil me-1"></i>{t('diplomas.edit')}
                    </button>
                    <button
                      className="btn btn-outline-danger btn-sm flex-grow-1 rounded-pill fw-semibold"
                      data-bs-toggle="modal"
                      data-bs-target="#confirmarEliminarDiplomaModal"
                      onClick={() => setDiplomaToDelete(d)}
                      disabled={deleting === d.idDiploma}
                    >
                      {deleting === d.idDiploma ? (
                        <><span className="spinner-border spinner-border-sm me-1" role="status"></span>...</>
                      ) : (
                        <><i className="bi bi-trash me-1"></i>{t('diplomas.delete')}</>
                      )}
                    </button>
                  </div>
                </div>
              </div>
            </div>
          ))}
        </div>
      ) : (
        <div className="card border-0 shadow-sm rounded-4 mb-4">
          <div className="card-body text-center py-5">
            <div className="rounded-circle bg-primary bg-opacity-10 d-inline-flex align-items-center justify-content-center mb-3" style={{ width: '64px', height: '64px' }}>
              <i className="bi bi-award text-primary fs-3"></i>
            </div>
            <h6 className="fw-bold mb-1">{t('diplomas.noDiplomas')}</h6>
            <p className="text-secondary small mb-2">
              {t('diplomas.createMsg')}
            </p>
            <button className="btn btn-primary btn-sm rounded-pill px-4" data-bs-toggle="modal" data-bs-target="#crearDiplomaModal">
              {t('diplomas.createFirst')}
            </button>
          </div>
        </div>
      )}

      <CrearDiplomaModal
        eventos={eventos}
        formData={formData}
        onChange={(e) => setFormData(prev => ({ ...prev, [e.target.name]: e.target.value }))}
        onSubmit={handleCreateDiploma}
        isLoading={creating}
      />

      <EditarDiplomaModal
        diploma={selectedDiploma}
        onSubmit={handleUpdateDiploma}
        isLoading={editing}
      />

      <ConfirmModal
        id="confirmarEliminarDiplomaModal"
        title={t('diplomas.deleteTitle')}
        message={diplomaToDelete ? t('diplomas.deleteConfirm', { name: diplomaToDelete.nombreEvento }) : ''}
        confirmText={t('diplomas.delete')}
        cancelText={t('common.cancel')}
        onConfirm={handleConfirmDelete}
        isLoading={!!deleting}
        variant="danger"
      />
    </div>
  )
}

export default AdminDiplomas
