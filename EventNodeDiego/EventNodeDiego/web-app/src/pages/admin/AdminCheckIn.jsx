import { useState, useEffect } from 'react'
import { Link, useParams } from 'react-router-dom'
import { toast } from 'react-toastify'
import { eventService } from '../../services/eventService'
import { asistenciaService } from '../../services/asistenciaService'
import { useTranslation } from '../../i18n/I18nContext'

function AdminCheckIn() {
  const { id } = useParams()
  const { t } = useTranslation()
  const [evento, setEvento] = useState(null)
  const [students, setStudents] = useState([])
  const [loading, setLoading] = useState(true)
  const [search, setSearch] = useState('')
  const [matricula, setMatricula] = useState('')
  const [manualLoading, setManualLoading] = useState(false)

  const fetchData = async () => {
    try {
      const [eventoData, asistenciasData] = await Promise.all([
        eventService.getEvento(id),
        asistenciaService.listarAsistencias(id),
      ])
      setEvento(eventoData)
      setStudents(asistenciasData)
    } catch {
      setStudents([])
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { fetchData() }, [id])

  const handleManualCheckin = async () => {
    if (!matricula.trim()) return toast.error(t('checkin.studentId'))
    setManualLoading(true)
    try {
      await asistenciaService.registrarManual(matricula.trim(), parseInt(id))
      toast.success(t('checkin.successMsg'))
      setMatricula('')
      fetchData()
    } catch (err) {
      toast.error(err.message)
    } finally {
      setManualLoading(false)
    }
  }

  const filtered = search
    ? students.filter(s =>
        (s.nombre || '').toLowerCase().includes(search.toLowerCase()) ||
        (s.correo || '').toLowerCase().includes(search.toLowerCase())
      )
    : students

  const qrCount = students.filter(s => s.metodo === 'QR').length
  const manualCount = students.filter(s => s.metodo === 'MANUAL').length

  return (
    <div>
      <div className="d-flex align-items-center gap-3 mb-4">
        <Link to={`/admin/evento/${id}`} className="btn btn-light rounded-circle shadow-sm d-flex align-items-center justify-content-center p-0" style={{ width: '40px', height: '40px' }}>
          <i className="bi bi-arrow-left text-secondary fs-5"></i>
        </Link>
        <span className="fw-semibold small">{evento?.nombre || 'Cargando...'}</span>
      </div>

      <div className="mb-4">
        <h2 className="fw-bold mb-1">{t('checkin.title')}</h2>
        <div className="text-secondary small">
          {t('checkin.subtitle')}
        </div>
      </div>

      {/* Stats */}
      <div className="row g-3 mb-4">
        <div className="col-6 col-md-3">
          <div className="card border-0 shadow-sm rounded-3">
            <div className="card-body p-3 text-center">
              <div className="text-secondary small text-uppercase fw-bold mb-1">{t('checkin.total')}</div>
              <div className="fw-bold fs-4">{students.length}</div>
            </div>
          </div>
        </div>
        <div className="col-6 col-md-3">
          <div className="card border-0 shadow-sm rounded-3">
            <div className="card-body p-3 text-center">
              <div className="text-primary small text-uppercase fw-bold mb-1">QR</div>
              <div className="fw-bold fs-4 text-primary">{qrCount}</div>
            </div>
          </div>
        </div>
        <div className="col-6 col-md-3">
          <div className="card border-0 shadow-sm rounded-3">
            <div className="card-body p-3 text-center">
              <div className="text-warning small text-uppercase fw-bold mb-1">Manual</div>
              <div className="fw-bold fs-4 text-warning">{manualCount}</div>
            </div>
          </div>
        </div>
      </div>

      {/* Manual check-in */}
      <div className="card border-0 shadow-sm rounded-4 mb-4">
        <div className="card-body p-4">
          <h6 className="fw-bold mb-2">{t('checkin.manualEntry')}</h6>
          <div className="d-flex gap-2">
            <input
              type="text"
              className="form-control"
              placeholder={t('checkin.studentId')}
              value={matricula}
              onChange={(e) => setMatricula(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && handleManualCheckin()}
              style={{ maxWidth: '300px' }}
            />
            <button className="btn btn-primary rounded-pill px-4" onClick={handleManualCheckin} disabled={manualLoading}>
              {manualLoading ? t('checkin.registering') : t('checkin.register')}
            </button>
          </div>
        </div>
      </div>

      <div className="card border-0 shadow-sm rounded-4 mb-4">
        <div className="card-body p-4">
          <div className="d-flex flex-column flex-md-row gap-3 mb-4">
            <div className="input-group bg-light rounded-3 overflow-hidden flex-grow-1" style={{ border: 'none' }}>
              <span className="input-group-text bg-transparent border-0 pe-1">
                <i className="bi bi-search text-secondary"></i>
              </span>
              <input
                type="text"
                className="form-control bg-transparent border-0 shadow-none small"
                placeholder={t('checkin.searchPlaceholder')}
                style={{ fontSize: '13px' }}
                value={search}
                onChange={(e) => setSearch(e.target.value)}
              />
            </div>
          </div>

          {loading ? (
            <div className="text-center py-4">
              <div className="spinner-border text-primary" role="status">
                <span className="visually-hidden">Cargando...</span>
              </div>
            </div>
          ) : filtered.length > 0 ? (
            <div className="table-responsive">
              <table className="table table-hover align-middle mb-0">
                <thead className="border-bottom">
                  <tr>
                    <th className="text-uppercase text-secondary small fw-bold pb-3 border-0 ps-3" style={{ fontSize: '11px', letterSpacing: '0.5px' }}>Nombre Completo</th>
                    <th className="text-uppercase text-secondary small fw-bold pb-3 border-0" style={{ fontSize: '11px', letterSpacing: '0.5px' }}>{t('checkin.quarter')}</th>
                    <th className="text-uppercase text-secondary small fw-bold pb-3 border-0" style={{ fontSize: '11px', letterSpacing: '0.5px' }}>Correo</th>
                    <th className="text-uppercase text-secondary small fw-bold pb-3 border-0 pe-3" style={{ fontSize: '11px', letterSpacing: '0.5px' }}>{t('checkin.method')}</th>
                  </tr>
                </thead>
                <tbody className="border-top-0">
                  {filtered.map((student) => (
                    <tr key={student.idAsistencia}>
                      <td className="fw-bold small py-3 border-light ps-3">{student.nombre}</td>
                      <td className="small py-3 border-light text-secondary">{student.cuatrimestre ? `${student.cuatrimestre}°` : '—'}</td>
                      <td className="small py-3 border-light text-secondary">{student.correo}</td>
                      <td className="small py-3 border-light pe-3">
                        <span className={`badge rounded-pill px-2 py-1 ${student.metodo === 'QR' ? 'bg-primary bg-opacity-10 text-primary' : 'bg-warning bg-opacity-10 text-warning'}`} style={{ fontSize: '10px' }}>
                          {student.metodo === 'QR' ? 'QR' : 'Manual'}
                        </span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : (
            <div className="text-center py-4">
              <p className="text-secondary small mb-0">{t('checkin.noAttendance')}</p>
            </div>
          )}
        </div>
        <div className="card-footer bg-transparent border-top p-3 d-flex justify-content-between align-items-center">
          <span className="text-secondary small">
            {students.length} registros totales
          </span>
          <span className="text-secondary small">
            <span className="text-primary fw-semibold">{qrCount} QR</span>
            {' · '}
            <span className="text-warning fw-semibold">{manualCount} Manual</span>
          </span>
        </div>
      </div>
    </div>
  )
}

export default AdminCheckIn
