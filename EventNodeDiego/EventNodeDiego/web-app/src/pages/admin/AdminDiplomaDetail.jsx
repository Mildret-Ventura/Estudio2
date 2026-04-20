import { useState, useEffect } from 'react'
import { useParams, Link } from 'react-router-dom'
import { useTranslation } from '../../i18n/I18nContext'
import { diplomaService } from '../../services/diplomaService'

function AdminDiplomaDetail() {
  const { t } = useTranslation()
  const { id } = useParams()
  const [diploma, setDiploma] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [downloading, setDownloading] = useState({})

  useEffect(() => {
    const fetchDiploma = async () => {
      try {
        const data = await diplomaService.obtenerDiploma(id)
        setDiploma(data)
      } catch (err) {
        setError(t('eventDetail.eventNotFound'))
      } finally {
        setLoading(false)
      }
    }
    fetchDiploma()
  }, [id])

  const handleDownload = async (idUsuario) => {
    setDownloading(prev => ({ ...prev, [idUsuario]: true }))
    try {
      const blob = await diplomaService.descargarDiploma(id, idUsuario)
      const url = window.URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = `diploma_${idUsuario}.pdf`
      document.body.appendChild(a)
      a.click()
      window.URL.revokeObjectURL(url)
      document.body.removeChild(a)
    } catch (err) {
    } finally {
      setDownloading(prev => ({ ...prev, [idUsuario]: false }))
    }
  }

  if (loading) {
    return (
      <div className="text-center py-5">
        <div className="spinner-border text-primary spinner-border-sm" role="status">
          <span className="visually-hidden">{t('common.loading')}</span>
        </div>
      </div>
    )
  }

  if (error || !diploma) {
    return (
      <div>
        <div className="d-flex align-items-center gap-3 mb-4">
          <Link to="/admin/diplomas" className="btn btn-light rounded-circle d-flex align-items-center justify-content-center p-0" style={{ width: '40px', height: '40px' }}>
            <i className="bi bi-arrow-left text-secondary fs-5"></i>
          </Link>
        </div>
        <div className="alert alert-danger" role="alert">
          {error || t('diplomas.noDiplomas')}
        </div>
      </div>
    )
  }

  const formattedDate = diploma.fechaCreacion
    ? new Date(diploma.fechaCreacion).toLocaleDateString('es-MX')
    : 'N/A'

  return (
    <div>
      <div className="d-flex align-items-center gap-3 mb-4">
        <Link to="/admin/diplomas" className="btn btn-light rounded-circle d-flex align-items-center justify-content-center p-0" style={{ width: '40px', height: '40px' }}>
          <i className="bi bi-arrow-left text-secondary fs-5"></i>
        </Link>
        <span className="fw-semibold small">{diploma.nombreEvento}</span>
      </div>

      <div className="mb-4">
        <h2 className="fw-bold mb-1">{t('diplomas.diplomaTitle')}</h2>
        <p className="text-secondary small mb-0">
          {t('diplomas.manageSubtitle')}
        </p>
      </div>

      {/* Información del Diploma */}
      <div className="card border-0 shadow-sm rounded-4 mb-4">
        <div className="card-body p-4">
          <div className="row mb-3">
            <div className="col-md-6">
              <div className="text-secondary small mb-1">{t('events.title')}</div>
              <div className="fw-semibold">{diploma.nombreEvento}</div>
            </div>
            <div className="col-md-6">
              <div className="text-secondary small mb-1">{t('diplomas.creationDate')}</div>
              <div className="fw-semibold">{formattedDate}</div>
            </div>
          </div>
          <div className="row">
            <div className="col-md-6">
              <div className="text-secondary small mb-1">{t('analytics.emitted')}</div>
              <div className="fw-semibold">{diploma.totalEmitidos || 0}</div>
            </div>
            <div className="col-md-6">
              <div className="text-secondary small mb-1">{t('analytics.pendingStat')}</div>
              <div className="fw-semibold">{diploma.totalPendientes || 0}</div>
            </div>
          </div>
        </div>
      </div>

      {/* Tabla de Receptores */}
      {diploma.emitidos && diploma.emitidos.length > 0 ? (
        <div className="card border-0 shadow-sm rounded-4">
          <div className="card-body p-0">
            <div className="p-3 pb-2 border-bottom">
              <h5 className="fw-bold mb-0">{t('diplomas.recipients')}</h5>
            </div>
            <div className="table-responsive">
              <table className="table table-hover mb-0 align-middle">
                <thead className="border-top">
                  <tr>
                    <th className="text-uppercase text-secondary small fw-semibold ps-3 py-3" style={{ fontSize: '11px' }}>{t('diplomas.fullName')}</th>
                    <th className="text-uppercase text-secondary small fw-semibold py-3" style={{ fontSize: '11px' }}>{t('diplomas.emailCol')}</th>
                    <th className="text-uppercase text-secondary small fw-semibold py-3" style={{ fontSize: '11px' }}>{t('checkin.status')}</th>
                    <th className="text-uppercase text-secondary small fw-semibold py-3" style={{ fontSize: '11px' }}>{t('diplomas.sendDate')}</th>
                    <th className="text-uppercase text-secondary small fw-semibold pe-3 py-3" style={{ fontSize: '11px' }}>{t('diplomas.action')}</th>
                  </tr>
                </thead>
                <tbody>
                  {diploma.emitidos.map((receptor) => (
                    <tr key={receptor.idUsuario}>
                      <td className="ps-3 py-3 fw-semibold small">{receptor.nombreCompleto}</td>
                      <td className="py-3 text-secondary small">{receptor.correo}</td>
                      <td className="py-3">
                        <span className={`badge bg-opacity-10 rounded-pill px-3 ${
                          receptor.estado === 'ENVIADO' ? 'bg-success text-success' : 'bg-warning text-warning'
                        }`}>
                          {receptor.estado}
                        </span>
                      </td>
                      <td className="py-3 text-secondary small">
                        {receptor.fechaEnvio
                          ? new Date(receptor.fechaEnvio).toLocaleDateString('es-MX')
                          : 'N/A'
                        }
                      </td>
                      <td className="pe-3 py-3">
                        <button
                          className="btn btn-sm btn-primary rounded-3 d-flex align-items-center gap-2"
                          onClick={() => handleDownload(receptor.idUsuario)}
                          disabled={downloading[receptor.idUsuario]}
                        >
                          <i className="bi bi-download"></i>
                          {downloading[receptor.idUsuario] ? t('diplomas.downloading') : t('diplomas.download')}
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      ) : (
        <div className="card border-0 shadow-sm rounded-4">
          <div className="card-body text-center py-5 px-3">
            <div className="rounded-circle bg-primary bg-opacity-10 d-inline-flex align-items-center justify-content-center mb-3" style={{ width: '56px', height: '56px' }}>
              <i className="bi bi-document-text text-primary fs-4"></i>
            </div>
            <h6 className="fw-bold mb-1">{t('diplomas.noRecipients')}</h6>
            <p className="text-secondary small mb-0">
              {t('diplomas.noRecipientsMsg')}
            </p>
          </div>
        </div>
      )}
    </div>
  )
}

export default AdminDiplomaDetail
