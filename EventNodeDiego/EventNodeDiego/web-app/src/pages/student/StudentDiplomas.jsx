import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { toast } from 'react-toastify'
import { useTranslation } from '../../i18n/I18nContext'
import { diplomaService } from '../../services/diplomaService'

function StudentDiplomas({ user }) {
  const { t } = useTranslation()
  const [diplomas, setDiplomas] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    if (!user?.id) return
    const fetchDiplomas = async () => {
      try {
        const data = await diplomaService.listarDiplomasEstudiante(user.id)
        setDiplomas(data)
      } catch {
        setDiplomas([])
      } finally {
        setLoading(false)
      }
    }
    fetchDiplomas()
  }, [user])

  const handleDownload = async (diploma) => {
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
    }
  }

  return (
    <div>
      <div className="d-flex flex-column flex-md-row justify-content-between align-items-start align-items-md-center mb-4 gap-2">
        <div>
          <h2 className="fw-bold mb-1">{t('studentDiplomas.title')}</h2>
          <p className="text-secondary small mb-0">
            {t('studentDiplomas.subtitle')}
          </p>
        </div>
      </div>

      {loading ? (
        <div className="text-center py-5">
          <div className="spinner-border text-primary" role="status">
            <span className="visually-hidden">Cargando...</span>
          </div>
        </div>
      ) : diplomas.length > 0 ? (
        <div className="row g-3">
          {diplomas.map(d => (
            <div className="col-12 col-md-6 col-lg-4" key={d.idEmitido}>
              <div className="card border-0 shadow-sm rounded-4 h-100 card-hover">
                <div className="card-body p-4 text-center">
                  <div className="rounded-circle bg-primary bg-opacity-10 d-inline-flex align-items-center justify-content-center mb-3" style={{ width: '56px', height: '56px' }}>
                    <i className="bi bi-award text-primary fs-4"></i>
                  </div>
                  <h6 className="fw-bold mb-1">{d.nombreEvento}</h6>
                  <p className="text-secondary small mb-2">
                    {d.fechaEnvio ? new Date(d.fechaEnvio).toLocaleDateString('es-MX', { day: 'numeric', month: 'long', year: 'numeric' }) : ''}
                  </p>
                  <span className={`badge rounded-pill px-3 py-1 mb-3 ${d.estadoEnvio === 'ENVIADO' ? 'bg-success bg-opacity-10 text-success' : 'bg-danger bg-opacity-10 text-danger'}`}>
                    {d.estadoEnvio === 'ENVIADO' ? t('studentDiplomas.received') : t('studentDiplomas.error')}
                  </span>

                  <div className="d-flex gap-2 justify-content-center mt-2">
                    <Link
                      to={`/estudiante/diplomas/${d.idDiploma}`}
                      className="btn btn-outline-primary btn-sm rounded-pill px-3 d-flex align-items-center gap-1"
                    >
                      <i className="bi bi-eye"></i>
                      {t('studentDiplomas.view')}
                    </Link>
                    <button
                      className="btn btn-primary btn-sm rounded-pill px-3 d-flex align-items-center gap-1"
                      onClick={() => handleDownload(d)}
                    >
                      <i className="bi bi-download"></i>
                      {t('studentDiplomas.download')}
                    </button>
                  </div>
                </div>
              </div>
            </div>
          ))}
        </div>
      ) : (
        <div className="card border-0 shadow-sm rounded-4">
          <div className="card-body text-center py-5">
            <div className="rounded-circle bg-primary bg-opacity-10 d-inline-flex align-items-center justify-content-center mb-3" style={{ width: '64px', height: '64px' }}>
              <i className="bi bi-award text-primary fs-3"></i>
            </div>
            <h6 className="fw-bold mb-1">{t('studentDiplomas.noDiplomas')}</h6>
            <p className="text-secondary small mb-0">
              {t('studentDiplomas.noDiplomasMsg')}
            </p>
          </div>
        </div>
      )}
    </div>
  )
}

export default StudentDiplomas
