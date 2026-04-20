import { useState, useEffect } from 'react'
import { Link, useParams } from 'react-router-dom'
import { toast } from 'react-toastify'
import { useTranslation } from '../../i18n/I18nContext'
import { diplomaService } from '../../services/diplomaService'

function StudentDiplomaDetail({ user }) {
  const { t } = useTranslation()
  const { id } = useParams()
  const [diploma, setDiploma] = useState(null)
  const [loading, setLoading] = useState(true)
  const [pdfUrl, setPdfUrl] = useState(null)
  const [pdfLoading, setPdfLoading] = useState(false)
  const [downloading, setDownloading] = useState(false)

  useEffect(() => {
    const fetchDiploma = async () => {
      try {
        const data = await diplomaService.obtenerDiploma(id)
        setDiploma(data)
      } catch {
        setDiploma(null)
      } finally {
        setLoading(false)
      }
    }
    fetchDiploma()
  }, [id])

  // Load PDF preview
  useEffect(() => {
    if (!diploma || !user?.id) return
    const loadPreview = async () => {
      setPdfLoading(true)
      try {
        const blob = await diplomaService.descargarDiploma(diploma.idDiploma, user.id)
        const url = window.URL.createObjectURL(blob)
        setPdfUrl(url)
      } catch {
        setPdfUrl(null)
      } finally {
        setPdfLoading(false)
      }
    }
    loadPreview()

    return () => {
      if (pdfUrl) window.URL.revokeObjectURL(pdfUrl)
    }
  }, [diploma, user])

  const handleDownload = async () => {
    if (!diploma || !user?.id) return
    setDownloading(true)
    try {
      const blob = await diplomaService.descargarDiploma(diploma.idDiploma, user.id)
      const url = window.URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = `Diploma_${(diploma.nombreEvento || 'evento').replace(/[^a-zA-Z0-9]/g, '_')}.pdf`
      document.body.appendChild(a)
      a.click()
      document.body.removeChild(a)
      window.URL.revokeObjectURL(url)
    } catch (err) {
      toast.error(err.message || 'Error al descargar el diploma')
    } finally {
      setDownloading(false)
    }
  }

  if (loading) {
    return (
      <div className="text-center py-5">
        <div className="spinner-border text-primary" role="status">
          <span className="visually-hidden">{t('eventDetail.loading')}</span>
        </div>
      </div>
    )
  }

  if (!diploma) {
    return (
      <div className="text-center py-5">
        <h5>Diploma no encontrado</h5>
        <Link to="/estudiante/diplomas" className="btn btn-primary btn-sm rounded-pill px-4 mt-2">{t('eventDetail.back')}</Link>
      </div>
    )
  }

  // Find user's emitted diploma info
  const myEmitido = diploma.emitidos?.find(e => e.idUsuario === user?.id)

  return (
    <div>
      <Link
        to="/estudiante/diplomas"
        className="text-secondary text-decoration-none small d-flex align-items-center gap-1 mb-3"
      >
        <i className="bi bi-arrow-left"></i>
        {t('studentDiplomaDetail.backToDiplomas')}
      </Link>

      <h4 className="fw-bold mb-4">{t('studentDiplomaDetail.title')}</h4>

      <div className="row g-4">
        {/* PDF Preview */}
        <div className="col-12 col-lg-8">
          <div className="card border-0 shadow-sm rounded-4 mb-3">
            <div className="card-body p-3">
              {pdfLoading ? (
                <div className="text-center py-5">
                  <div className="spinner-border text-primary mb-3" role="status">
                    <span className="visually-hidden">{t('eventDetail.loading')}</span>
                  </div>
                  <p className="text-secondary small">{t('eventDetail.loading')}</p>
                </div>
              ) : pdfUrl ? (
                <div style={{ width: '100%', minHeight: '500px' }}>
                  <iframe
                    src={pdfUrl}
                    title="Vista previa del diploma"
                    style={{
                      width: '100%',
                      height: '550px',
                      border: 'none',
                      borderRadius: '12px'
                    }}
                  />
                </div>
              ) : (
                <div className="text-center py-5"
                  style={{
                    border: '3px dashed #e0e7ff',
                    borderRadius: '12px',
                    background: 'linear-gradient(135deg, #f8faff 0%, #ffffff 100%)'
                  }}>
                  <div className="mb-3">
                    <i className="bi bi-file-earmark-pdf text-primary" style={{ fontSize: '3rem' }}></i>
                  </div>
                  <h5 className="fw-bold mb-1">{t('studentDiplomaDetail.attendanceDiploma')}</h5>
                  <p className="text-uppercase text-secondary small mb-3">
                    {t('studentDiplomaDetail.awardedTo')}
                  </p>
                  <h3 className="fw-bold mb-3" style={{ fontSize: '1.8rem', color: '#1a56db' }}>
                    {myEmitido?.nombre || user?.nombre || 'Estudiante'}
                  </h3>
                  <p className="text-secondary small mb-2">
                    {t('studentDiplomaDetail.forAttending')}
                  </p>
                  <p className="text-primary fw-semibold">{diploma.nombreEvento}</p>
                  <p className="text-secondary small mt-3">
                    <i className="bi bi-exclamation-triangle me-1"></i>
                    No se pudo cargar la vista previa del PDF
                  </p>
                </div>
              )}
            </div>
          </div>

          <div className="d-flex align-items-center gap-2 p-3 bg-white rounded-3 shadow-sm">
            <div className="rounded-circle bg-primary d-flex align-items-center justify-content-center flex-shrink-0"
              style={{ width: '32px', height: '32px' }}>
              <i className="bi bi-check-lg text-white small"></i>
            </div>
            <div>
              <div className="fw-semibold small">{t('studentDiplomaDetail.verifiedCert')}</div>
              <div className="text-secondary small">
                {t('studentDiplomaDetail.verifiedMsg')}
              </div>
            </div>
          </div>
        </div>

        {/* Sidebar info */}
        <div className="col-12 col-lg-4">
          <div className="card border-0 shadow-sm rounded-4 mb-3">
            <div className="card-body p-4">
              <h6 className="fw-bold mb-3">{diploma.nombreEvento}</h6>

              <div className="d-flex align-items-start gap-2 mb-3">
                <i className="bi bi-person-check text-primary small mt-1"></i>
                <div>
                  <div className="text-secondary small text-uppercase">{t('studentDiplomaDetail.signedBy')}</div>
                  <div className="fw-semibold small">{diploma.firma || 'Administrador'}</div>
                </div>
              </div>

              <div className="d-flex align-items-start gap-2 mb-3">
                <i className="bi bi-calendar-check text-primary small mt-1"></i>
                <div>
                  <div className="text-secondary small text-uppercase">{t('studentDiplomaDetail.issueDate')}</div>
                  <div className="fw-semibold small">
                    {myEmitido?.fechaEnvio
                      ? new Date(myEmitido.fechaEnvio).toLocaleDateString('es-MX', { day: 'numeric', month: 'long', year: 'numeric' })
                      : diploma.fechaCreacion
                        ? new Date(diploma.fechaCreacion).toLocaleDateString('es-MX', { day: 'numeric', month: 'long', year: 'numeric' })
                        : '—'}
                  </div>
                </div>
              </div>

              <div className="d-flex align-items-start gap-2 mb-3">
                <i className="bi bi-patch-check text-primary small mt-1"></i>
                <div>
                  <div className="text-secondary small text-uppercase">{t('eventDetail.scheduleDetails')}</div>
                  <div className="fw-semibold small">
                    <span className={`badge rounded-pill px-2 py-1 ${myEmitido?.estadoEnvio === 'ENVIADO' ? 'bg-success bg-opacity-10 text-success' : 'bg-warning bg-opacity-10 text-warning'}`}>
                      {myEmitido?.estadoEnvio === 'ENVIADO' ? t('studentDiplomaDetail.delivered') : t('studentDiplomaDetail.pending')}
                    </span>
                  </div>
                </div>
              </div>

              {diploma.diseno && (
                <div className="d-flex align-items-start gap-2 mb-4">
                  <i className="bi bi-palette text-primary small mt-1"></i>
                  <div>
                    <div className="text-secondary small text-uppercase">{t('studentDiplomaDetail.design')}</div>
                    <div className="fw-semibold small">{diploma.diseno}</div>
                  </div>
                </div>
              )}

              <button
                className="btn btn-primary w-100 rounded-pill fw-semibold d-flex align-items-center justify-content-center gap-2"
                onClick={handleDownload}
                disabled={downloading}
              >
                <i className="bi bi-download"></i>
                {downloading ? 'Descargando...' : t('studentDiplomaDetail.downloadPdf')}
              </button>
            </div>
          </div>

          <div className="card border-0 rounded-4 text-white"
            style={{ background: 'linear-gradient(135deg, #1a1a2e 0%, #16213e 100%)' }}>
            <div className="card-body p-4">
              <h6 className="fw-bold mb-2">{t('studentDiplomaDetail.nextStep')}</h6>
              <p className="small opacity-75 mb-1">{t('studentDiplomaDetail.eventCompleted')}</p>
              <p className="small opacity-75 mb-3">{t('studentDiplomaDetail.anotherEvent')}</p>
              <Link
                to="/estudiante/eventos"
                className="btn btn-light btn-sm rounded-pill fw-semibold w-100"
              >
                {t('studentDiplomaDetail.exploreEvents')}
              </Link>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}

export default StudentDiplomaDetail
