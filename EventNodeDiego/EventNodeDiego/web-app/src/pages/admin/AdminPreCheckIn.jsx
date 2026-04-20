import { useState, useEffect } from 'react'
import { Link, useParams } from 'react-router-dom'
import { eventService } from '../../services/eventService'
import { precheckinService } from '../../services/precheckinService'
import { useTranslation } from '../../i18n/I18nContext'

function AdminPreCheckIn() {
  const { id } = useParams()
  const { t } = useTranslation()
  const [evento, setEvento] = useState(null)
  const [students, setStudents] = useState([])
  const [loading, setLoading] = useState(true)
  const [search, setSearch] = useState('')

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [eventoData, inscritosData] = await Promise.all([
          eventService.getEvento(id),
          precheckinService.listarInscritos(id),
        ])
        setEvento(eventoData)
        setStudents(inscritosData)
      } catch {
        setStudents([])
      } finally {
        setLoading(false)
      }
    }
    fetchData()
  }, [id])

  const filtered = search
    ? students.filter(s =>
        (s.nombre || '').toLowerCase().includes(search.toLowerCase()) ||
        (s.matricula || '').toLowerCase().includes(search.toLowerCase()) ||
        (s.correo || '').toLowerCase().includes(search.toLowerCase())
      )
    : students

  return (
    <div>
      <div className="d-flex align-items-center gap-3 mb-4">
        <Link to={`/admin/evento/${id}`} className="btn btn-light rounded-circle shadow-sm d-flex align-items-center justify-content-center p-0" style={{ width: '40px', height: '40px' }}>
          <i className="bi bi-arrow-left text-secondary fs-5"></i>
        </Link>
        <span className="fw-semibold small">{evento?.nombre || 'Cargando...'}</span>
      </div>

      <div className="mb-4">
        <h2 className="fw-bold mb-1">{t('precheckin.title')}</h2>
        <div className="text-secondary small d-flex align-items-center gap-2">
          <i className="bi bi-calendar-event"></i>
          {evento?.nombre}
        </div>
      </div>

      <div className="card border-0 shadow-sm rounded-4 mb-4">
        <div className="card-body p-4">
          <div className="d-flex flex-column flex-md-row gap-3 mb-4 justify-content-between">
            <div className="input-group bg-light rounded-3 overflow-hidden flex-grow-1" style={{ maxWidth: '400px', border: 'none' }}>
              <span className="input-group-text bg-transparent border-0 pe-1">
                <i className="bi bi-search text-secondary"></i>
              </span>
              <input
                type="text"
                className="form-control bg-transparent border-0 shadow-none small"
                placeholder={t('precheckin.searchPlaceholder')}
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
                    <th className="text-uppercase text-secondary small fw-bold pb-3 border-0 ps-3" style={{ fontSize: '11px', letterSpacing: '0.5px' }}>Matrícula</th>
                    <th className="text-uppercase text-secondary small fw-bold pb-3 border-0" style={{ fontSize: '11px', letterSpacing: '0.5px' }}>Nombre Completo</th>
                    <th className="text-uppercase text-secondary small fw-bold pb-3 border-0" style={{ fontSize: '11px', letterSpacing: '0.5px' }}>Correo</th>
                    <th className="text-uppercase text-secondary small fw-bold pb-3 border-0 text-end pe-4" style={{ fontSize: '11px', letterSpacing: '0.5px' }}>Estado</th>
                  </tr>
                </thead>
                <tbody className="border-top-0">
                  {filtered.map((student) => (
                    <tr key={student.idPrecheckin}>
                      <td className="fw-bold small py-3 border-light ps-3">{student.matricula || '—'}</td>
                      <td className="small py-3 border-light text-secondary">{student.nombre}</td>
                      <td className="small py-3 border-light text-secondary">{student.correo}</td>
                      <td className="py-3 border-light text-end pe-4">
                        <span className={`badge rounded-pill px-3 py-2 fw-semibold ${student.estado === 'ACTIVO' ? 'bg-success bg-opacity-10 text-success' : 'bg-danger bg-opacity-10 text-danger'}`} style={{ fontSize: '11px' }}>
                          {student.estado === 'ACTIVO' ? 'Confirmado' : 'Cancelado'}
                        </span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : (
            <div className="text-center py-4">
              <p className="text-secondary small mb-0">{t('precheckin.noStudents')}</p>
            </div>
          )}
        </div>
        <div className="card-footer bg-transparent border-top p-3">
          <span className="text-secondary small">
            {t('precheckin.showing', { current: filtered.length, total: students.length })}
          </span>
        </div>
      </div>
    </div>
  )
}

export default AdminPreCheckIn
