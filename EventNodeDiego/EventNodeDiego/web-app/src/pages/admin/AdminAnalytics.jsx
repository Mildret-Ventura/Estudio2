import { useState, useEffect } from 'react'
import { eventService } from '../../services/eventService'
import { asistenciaService } from '../../services/asistenciaService'
import { precheckinService } from '../../services/precheckinService'
import { diplomaService } from '../../services/diplomaService'
import { userService } from '../../services/userService'
import { useTranslation } from '../../i18n/I18nContext'

function AdminAnalytics() {
  const { t } = useTranslation()
  const [loading, setLoading] = useState(true)
  const [eventos, setEventos] = useState([])
  const [diplomas, setDiplomas] = useState([])
  const [usuarios, setUsuarios] = useState([])
  const [attendanceData, setAttendanceData] = useState([])
  const [categoryData, setCategoryData] = useState([])
  const [chartMode, setChartMode] = useState('precheckin') // 'precheckin' | 'checkin'

  // Filters
  const [filterType, setFilterType] = useState('all') // 'all' | 'month' | 'year' | 'quarter' | 'recent' | 'last'
  const [filterMonth, setFilterMonth] = useState('')
  const [filterYear, setFilterYear] = useState('')
  const [filterQuarter, setFilterQuarter] = useState('')
  const [filterRecentCount, setFilterRecentCount] = useState(5)
  const [filterLastCount, setFilterLastCount] = useState(1)

  useEffect(() => {
    fetchAllData()
  }, [])

  const fetchAllData = async () => {
    try {
      const [eventosRes, diplomasRes, usuariosRes] = await Promise.all([
        eventService.getEventos().catch(() => []),
        diplomaService.listarDiplomas().catch(() => []),
        userService.getUsuarios().catch(() => [])
      ])

      setEventos(eventosRes)
      setDiplomas(diplomasRes)
      setUsuarios(usuariosRes)

      // Fetch attendance per event
      const attData = await Promise.all(
        eventosRes.map(async (ev) => {
          const [asistencias, inscritos] = await Promise.all([
            asistenciaService.contarAsistencias(ev.idEvento).catch(() => 0),
            precheckinService.contarInscritos(ev.idEvento).catch(() => 0)
          ])
          return {
            id: ev.idEvento,
            nombre: ev.nombre,
            estado: ev.estado,
            categoria: ev.categoriaNombre || 'Sin categoría',
            fechaInicio: ev.fechaInicio,
            asistencias,
            inscritos,
            capacidad: ev.capacidadMaxima || 0,
            rate: inscritos > 0 ? Math.round((asistencias / inscritos) * 100) : 0
          }
        })
      )
      setAttendanceData(attData)

      // Group by category
      const catMap = {}
      eventosRes.forEach(ev => {
        const cat = ev.categoriaNombre || 'Sin categoría'
        if (!catMap[cat]) catMap[cat] = { total: 0, activos: 0 }
        catMap[cat].total++
        if (ev.estado === 'ACTIVO') catMap[cat].activos++
      })
      setCategoryData(Object.entries(catMap).map(([name, data]) => ({ name, ...data })))

    } catch (err) {
    } finally {
      setLoading(false)
    }
  }

  // --- Filter logic ---
  const getAvailableYears = () => {
    const years = new Set()
    eventos.forEach(ev => {
      if (ev.fechaInicio) years.add(new Date(ev.fechaInicio).getFullYear())
    })
    return [...years].sort((a, b) => b - a)
  }

  const monthNames = {
    es: ['Enero','Febrero','Marzo','Abril','Mayo','Junio','Julio','Agosto','Septiembre','Octubre','Noviembre','Diciembre'],
    en: ['January','February','March','April','May','June','July','August','September','October','November','December']
  }

  const quarterLabels = [
    { value: '1', labelEs: 'Ene - Abr', labelEn: 'Jan - Apr', months: [0,1,2,3] },
    { value: '2', labelEs: 'May - Ago', labelEn: 'May - Aug', months: [4,5,6,7] },
    { value: '3', labelEs: 'Sep - Dic', labelEn: 'Sep - Dec', months: [8,9,10,11] }
  ]

  const applyFilter = (list, dateField = 'fechaInicio') => {
    if (filterType === 'all') return list

    if (filterType === 'last') {
      const sorted = [...list].sort((a, b) => new Date(b[dateField]) - new Date(a[dateField]))
      return sorted.slice(0, filterLastCount)
    }

    if (filterType === 'recent') {
      const sorted = [...list].sort((a, b) => new Date(b[dateField]) - new Date(a[dateField]))
      return sorted.slice(0, filterRecentCount)
    }

    return list.filter(item => {
      if (!item[dateField]) return false
      const d = new Date(item[dateField])
      const month = d.getMonth()
      const year = d.getFullYear()

      if (filterType === 'month') {
        const matchMonth = filterMonth !== '' ? month === parseInt(filterMonth) : true
        const matchYear = filterYear !== '' ? year === parseInt(filterYear) : true
        return matchMonth && matchYear
      }

      if (filterType === 'year') {
        return filterYear !== '' ? year === parseInt(filterYear) : true
      }

      if (filterType === 'quarter') {
        const q = quarterLabels.find(ql => ql.value === filterQuarter)
        if (!q) return true
        const matchQuarter = q.months.includes(month)
        const matchYear = filterYear !== '' ? year === parseInt(filterYear) : true
        return matchQuarter && matchYear
      }

      return true
    })
  }

  const applyFilterToEventos = (list) => {
    if (filterType === 'all') return list

    if (filterType === 'last') {
      const sorted = [...list].sort((a, b) => new Date(b.fechaInicio) - new Date(a.fechaInicio))
      return sorted.slice(0, filterLastCount)
    }

    if (filterType === 'recent') {
      const sorted = [...list].sort((a, b) => new Date(b.fechaInicio) - new Date(a.fechaInicio))
      return sorted.slice(0, filterRecentCount)
    }

    return list.filter(ev => {
      if (!ev.fechaInicio) return false
      const d = new Date(ev.fechaInicio)
      const month = d.getMonth()
      const year = d.getFullYear()

      if (filterType === 'month') {
        const matchMonth = filterMonth !== '' ? month === parseInt(filterMonth) : true
        const matchYear = filterYear !== '' ? year === parseInt(filterYear) : true
        return matchMonth && matchYear
      }
      if (filterType === 'year') {
        return filterYear !== '' ? year === parseInt(filterYear) : true
      }
      if (filterType === 'quarter') {
        const q = quarterLabels.find(ql => ql.value === filterQuarter)
        if (!q) return true
        return q.months.includes(month) && (filterYear !== '' ? year === parseInt(filterYear) : true)
      }
      return true
    })
  }

  const resetFilters = () => {
    setFilterType('all')
    setFilterMonth('')
    setFilterYear('')
    setFilterQuarter('')
    setFilterRecentCount(5)
    setFilterLastCount(1)
  }

  // Apply filters to data
  const filteredEventos = applyFilterToEventos(eventos)
  const filteredAttendanceData = applyFilter(attendanceData)

  // Recompute category data from filtered events
  const filteredCategoryData = (() => {
    const catMap = {}
    filteredEventos.forEach(ev => {
      const cat = ev.categoriaNombre || 'Sin categoría'
      if (!catMap[cat]) catMap[cat] = { total: 0, activos: 0 }
      catMap[cat].total++
      if (ev.estado === 'ACTIVO') catMap[cat].activos++
    })
    return Object.entries(catMap).map(([name, data]) => ({ name, ...data }))
  })()

  // Summary stats (from filtered data)
  const totalEventos = filteredEventos.length
  const eventosActivos = filteredEventos.filter(e => e.estado === 'ACTIVO').length
  const eventosCancelados = filteredEventos.filter(e => e.estado === 'CANCELADO').length
  const eventosTerminados = filteredEventos.filter(e => e.estado === 'FINALIZADO').length

  const totalDiplomas = diplomas.length
  const diplomasEmitidos = diplomas.reduce((sum, d) => sum + (d.totalEmitidos || 0), 0)
  const diplomasPendientes = diplomas.reduce((sum, d) => sum + (d.totalPendientes || 0), 0)

  const totalUsuarios = usuarios.length
  const estudiantes = usuarios.filter(u => u.rol === 'ALUMNO' || u.rol === 'ESTUDIANTE' || u.rol === 'STUDENT')
  const admins = usuarios.filter(u => u.rol === 'ADMINISTRADOR' || u.rol === 'SUPERADMIN')

  const totalAsistencias = filteredAttendanceData.reduce((s, e) => s + e.asistencias, 0)
  const totalInscritos = filteredAttendanceData.reduce((s, e) => s + e.inscritos, 0)
  const overallRate = totalInscritos > 0 ? Math.round((totalAsistencias / totalInscritos) * 100) : 0

  // Top events — only active events (finished/cancelled excluded from chart)
  const activeAttendanceData = filteredAttendanceData.filter(e => e.estado === 'ACTIVO')
  const sortKey = chartMode === 'checkin' ? 'asistencias' : 'inscritos'
  const topEvents = [...activeAttendanceData].sort((a, b) => b[sortKey] - a[sortKey]).slice(0, 8)
  const maxChartValue = Math.max(...topEvents.map(e => e[sortKey]), 1)

  if (loading) {
    return (
      <div className="d-flex justify-content-center align-items-center" style={{ minHeight: '400px' }}>
        <div className="text-center">
          <div className="spinner-border text-primary mb-3" role="status">
            <span className="visually-hidden">{t('common.loading')}</span>
          </div>
          <p className="text-secondary">{t('analytics.loadingAnalytics')}</p>
        </div>
      </div>
    )
  }

  return (
    <div>
      <div className="d-flex justify-content-between align-items-center mb-4">
        <div>
          <h2 className="fw-bold mb-1">{t('analytics.title')}</h2>
          <p className="text-secondary mb-0">{t('analytics.subtitle')}</p>
        </div>
        <button className="btn btn-outline-primary btn-sm rounded-pill px-3" onClick={fetchAllData}>
          <i className="bi bi-arrow-clockwise me-1"></i>{t('analytics.refresh')}
        </button>
      </div>

      {/* Filter Bar */}
      <div className="card border-0 shadow-sm rounded-4 mb-4">
        <div className="card-body p-3">
          <div className="d-flex align-items-center gap-2 flex-wrap">
            <i className="bi bi-funnel-fill text-primary"></i>
            <span className="small fw-semibold text-secondary me-1">{t('analytics.filters')}:</span>

            {/* Quick filter buttons */}
            <button
              className={`btn btn-sm ${filterType === 'all' ? 'btn-primary' : 'btn-outline-secondary'} rounded-pill px-3`}
              onClick={resetFilters}
            >
              {t('analytics.filterAll')}
            </button>
            {/* Most Recent with count dropdown */}
            <div className="d-flex align-items-center gap-1">
              <button
                className={`btn btn-sm ${filterType === 'recent' ? 'btn-primary' : 'btn-outline-secondary'} rounded-start-pill px-3`}
                onClick={() => { setFilterType('recent'); setFilterMonth(''); setFilterQuarter('') }}
              >
                <i className="bi bi-clock-history me-1"></i>{t('analytics.filterRecent')}
              </button>
              <select
                className={`form-select form-select-sm rounded-end-pill ${filterType === 'recent' ? 'border-primary bg-primary bg-opacity-10' : ''}`}
                style={{ width: 'auto', minWidth: '55px', borderLeft: 'none' }}
                value={filterRecentCount}
                onChange={(e) => {
                  setFilterRecentCount(parseInt(e.target.value))
                  setFilterType('recent')
                  setFilterMonth('')
                  setFilterQuarter('')
                }}
              >
                {[3,5,10,15,20].map(n => (
                  <option key={n} value={n}>{n}</option>
                ))}
              </select>
            </div>

            {/* Last with count dropdown */}
            <div className="d-flex align-items-center gap-1">
              <button
                className={`btn btn-sm ${filterType === 'last' ? 'btn-primary' : 'btn-outline-secondary'} rounded-start-pill px-3`}
                onClick={() => { setFilterType('last'); setFilterMonth(''); setFilterQuarter('') }}
              >
                <i className="bi bi-1-circle me-1"></i>{t('analytics.filterLast')}
              </button>
              <select
                className={`form-select form-select-sm rounded-end-pill ${filterType === 'last' ? 'border-primary bg-primary bg-opacity-10' : ''}`}
                style={{ width: 'auto', minWidth: '55px', borderLeft: 'none' }}
                value={filterLastCount}
                onChange={(e) => {
                  setFilterLastCount(parseInt(e.target.value))
                  setFilterType('last')
                  setFilterMonth('')
                  setFilterQuarter('')
                }}
              >
                {[1,2,3,5,10].map(n => (
                  <option key={n} value={n}>{n}</option>
                ))}
              </select>
            </div>

            <div className="vr mx-1 d-none d-md-block"></div>

            {/* Month filter */}
            <select
              className={`form-select form-select-sm rounded-pill ${filterType === 'month' ? 'border-primary' : ''}`}
              style={{ width: 'auto', minWidth: '130px' }}
              value={filterType === 'month' ? filterMonth : ''}
              onChange={(e) => {
                setFilterType('month')
                setFilterMonth(e.target.value)
                setFilterQuarter('')
              }}
            >
              <option value="">{t('analytics.filterMonth')}</option>
              {[0,1,2,3,4,5,6,7,8,9,10,11].map(m => (
                <option key={m} value={m}>{monthNames.es[m]}</option>
              ))}
            </select>

            {/* Quarter filter */}
            <select
              className={`form-select form-select-sm rounded-pill ${filterType === 'quarter' ? 'border-primary' : ''}`}
              style={{ width: 'auto', minWidth: '150px' }}
              value={filterType === 'quarter' ? filterQuarter : ''}
              onChange={(e) => {
                setFilterType('quarter')
                setFilterQuarter(e.target.value)
                setFilterMonth('')
              }}
            >
              <option value="">{t('analytics.filterQuarter')}</option>
              {quarterLabels.map(q => (
                <option key={q.value} value={q.value}>
                  {t('analytics.quarterPrefix')} {q.value} ({q.labelEs})
                </option>
              ))}
            </select>

            {/* Year filter */}
            <select
              className={`form-select form-select-sm rounded-pill ${filterYear !== '' ? 'border-primary' : ''}`}
              style={{ width: 'auto', minWidth: '100px' }}
              value={filterYear}
              onChange={(e) => {
                setFilterYear(e.target.value)
                if (filterType === 'all' || filterType === 'recent' || filterType === 'last') {
                  setFilterType('year')
                }
              }}
            >
              <option value="">{t('analytics.filterYear')}</option>
              {getAvailableYears().map(y => (
                <option key={y} value={y}>{y}</option>
              ))}
            </select>

            {filterType !== 'all' && (
              <button className="btn btn-sm btn-outline-danger rounded-pill px-3 ms-auto" onClick={resetFilters}>
                <i className="bi bi-x-circle me-1"></i>{t('analytics.clearFilters')}
              </button>
            )}
          </div>
        </div>
      </div>

      {/* Summary Stat Cards */}
      <div className="row g-3 mb-4">
        <div className="col-6 col-lg-3">
          <div className="card border-0 shadow-sm rounded-4 h-100">
            <div className="card-body p-3">
              <div className="rounded-3 bg-primary bg-opacity-10 d-inline-flex align-items-center justify-content-center mb-2" style={{ width: '40px', height: '40px' }}>
                <i className="bi bi-calendar-event text-primary"></i>
              </div>
              <div className="text-secondary small mb-1">{t('analytics.totalEvents')}</div>
              <div className="fw-bold fs-2">{totalEventos}</div>
              <div className="d-flex gap-2 mt-1" style={{ fontSize: '12px' }}>
                <span className="text-success"><i className="bi bi-circle-fill me-1" style={{ fontSize: '6px' }}></i>{eventosActivos} {t('analytics.activeEvents')}</span>
                <span className="text-secondary"><i className="bi bi-circle-fill me-1" style={{ fontSize: '6px' }}></i>{eventosTerminados} {t('analytics.finishedEvents')}</span>
              </div>
            </div>
          </div>
        </div>

        <div className="col-6 col-lg-3">
          <div className="card border-0 shadow-sm rounded-4 h-100">
            <div className="card-body p-3">
              <div className="rounded-3 bg-success bg-opacity-10 d-inline-flex align-items-center justify-content-center mb-2" style={{ width: '40px', height: '40px' }}>
                <i className="bi bi-people-fill text-success"></i>
              </div>
              <div className="text-secondary small mb-1">{t('analytics.attendanceRate')}</div>
              <div className="fw-bold fs-2">{overallRate}%</div>
              <div style={{ fontSize: '12px' }} className="text-secondary">{t('analytics.attendanceFraction', { total: totalInscritos }).replace('asistencias', totalAsistencias)}</div>
            </div>
          </div>
        </div>

        <div className="col-6 col-lg-3">
          <div className="card border-0 shadow-sm rounded-4 h-100">
            <div className="card-body p-3">
              <div className="rounded-3 bg-warning bg-opacity-10 d-inline-flex align-items-center justify-content-center mb-2" style={{ width: '40px', height: '40px' }}>
                <i className="bi bi-award-fill text-warning"></i>
              </div>
              <div className="text-secondary small mb-1">{t('analytics.diplomasStat')}</div>
              <div className="fw-bold fs-2">{totalDiplomas}</div>
              <div className="d-flex gap-2 mt-1" style={{ fontSize: '12px' }}>
                <span className="text-success">{diplomasEmitidos} {t('analytics.emitted')}</span>
                <span className="text-warning">{diplomasPendientes} {t('analytics.pendingStat')}</span>
              </div>
            </div>
          </div>
        </div>

        <div className="col-6 col-lg-3">
          <div className="card border-0 shadow-sm rounded-4 h-100">
            <div className="card-body p-3">
              <div className="rounded-3 bg-info bg-opacity-10 d-inline-flex align-items-center justify-content-center mb-2" style={{ width: '40px', height: '40px' }}>
                <i className="bi bi-person-fill text-info"></i>
              </div>
              <div className="text-secondary small mb-1">{t('analytics.users')}</div>
              <div className="fw-bold fs-2">{totalUsuarios}</div>
              <div className="d-flex gap-2 mt-1" style={{ fontSize: '12px' }}>
                <span className="text-primary">{estudiantes.length} {t('analytics.studentsStat')}</span>
                <span className="text-secondary">{admins.length} {t('analytics.adminsStat')}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div className="row g-3 mb-4">
        {/* Attendance Chart */}
        <div className="col-12 col-lg-8">
          <div className="card border-0 shadow-sm rounded-4 h-100">
            <div className="card-body p-3">
              <div className="d-flex justify-content-between align-items-center mb-3">
                <h6 className="fw-bold mb-0">
                  <i className="bi bi-bar-chart-fill text-primary me-2"></i>
                  {chartMode === 'precheckin' ? (t('analytics.preCheckinByEvent') || 'Pre Check-In por Evento') : (t('analytics.checkinByEvent') || 'Check-In por Evento')}
                </h6>
                <div className="btn-group btn-group-sm" role="group">
                  <button
                    type="button"
                    className={`btn ${chartMode === 'precheckin' ? 'btn-primary' : 'btn-outline-primary'} rounded-start-pill px-3`}
                    onClick={() => setChartMode('precheckin')}
                  >
                    <i className="bi bi-clipboard-check me-1"></i>Pre Check-In
                  </button>
                  <button
                    type="button"
                    className={`btn ${chartMode === 'checkin' ? 'btn-primary' : 'btn-outline-primary'} rounded-end-pill px-3`}
                    onClick={() => setChartMode('checkin')}
                  >
                    <i className="bi bi-person-check me-1"></i>Check-In
                  </button>
                </div>
              </div>
              {topEvents.length > 0 ? (
                <div>
                  {topEvents.map((ev, i) => {
                    const value = chartMode === 'checkin' ? ev.asistencias : ev.inscritos
                    const max = ev.capacidad
                    const barColor = i === 0 ? 'bg-primary' : i === 1 ? 'bg-success' : i === 2 ? 'bg-info' : 'bg-secondary'
                    return (
                      <div key={ev.id} className="mb-3">
                        <div className="d-flex justify-content-between align-items-center mb-1">
                          <span className="small text-truncate me-2" style={{ maxWidth: '60%' }} title={ev.nombre}>{ev.nombre}</span>
                          <div className="d-flex align-items-center gap-1">
                            <span className="small fw-semibold">{value}</span>
                            <span className="text-secondary" style={{ fontSize: '11px' }}>/ {max}</span>
                          </div>
                        </div>
                        <div className="progress" style={{ height: '8px' }}>
                          <div
                            className={`progress-bar ${barColor}`}
                            style={{ width: `${max > 0 ? Math.min((value / max) * 100, 100) : 0}%`, transition: 'width 0.4s ease' }}
                          ></div>
                        </div>
                      </div>
                    )
                  })}
                </div>
              ) : (
                <div className="text-center py-4">
                  <i className="bi bi-bar-chart text-secondary fs-1 d-block mb-2"></i>
                  <p className="text-secondary small mb-0">{t('analytics.noAttendanceData')}</p>
                </div>
              )}
            </div>
          </div>
        </div>

        {/* Event Status Breakdown */}
        <div className="col-12 col-lg-4">
          <div className="card border-0 shadow-sm rounded-4 h-100">
            <div className="card-body p-3">
              <h6 className="fw-bold mb-3">
                <i className="bi bi-pie-chart-fill text-primary me-2"></i>
                {t('analytics.eventStatus')}
              </h6>
              {totalEventos > 0 ? (
                <div>
                  {/* Visual ring */}
                  <div className="d-flex justify-content-center mb-3">
                    <div className="position-relative" style={{ width: '140px', height: '140px' }}>
                      <svg viewBox="0 0 36 36" style={{ width: '100%', height: '100%', transform: 'rotate(-90deg)' }}>
                        <circle cx="18" cy="18" r="15.915" fill="none" stroke="#e9ecef" strokeWidth="3" />
                        {eventosActivos > 0 && (
                          <circle cx="18" cy="18" r="15.915" fill="none" stroke="#198754" strokeWidth="3"
                            strokeDasharray={`${(eventosActivos / totalEventos) * 100} ${100 - (eventosActivos / totalEventos) * 100}`}
                            strokeDashoffset="0" />
                        )}
                        {eventosTerminados > 0 && (
                          <circle cx="18" cy="18" r="15.915" fill="none" stroke="#6c757d" strokeWidth="3"
                            strokeDasharray={`${(eventosTerminados / totalEventos) * 100} ${100 - (eventosTerminados / totalEventos) * 100}`}
                            strokeDashoffset={`${-(eventosActivos / totalEventos) * 100}`} />
                        )}
                        {eventosCancelados > 0 && (
                          <circle cx="18" cy="18" r="15.915" fill="none" stroke="#dc3545" strokeWidth="3"
                            strokeDasharray={`${(eventosCancelados / totalEventos) * 100} ${100 - (eventosCancelados / totalEventos) * 100}`}
                            strokeDashoffset={`${-((eventosActivos + eventosTerminados) / totalEventos) * 100}`} />
                        )}
                      </svg>
                      <div className="position-absolute top-50 start-50 translate-middle text-center">
                        <div className="fw-bold fs-4">{totalEventos}</div>
                        <div className="text-secondary" style={{ fontSize: '10px' }}>{t('analytics.total')}</div>
                      </div>
                    </div>
                  </div>
                  {/* Legend */}
                  <div className="d-flex flex-column gap-2">
                    <div className="d-flex justify-content-between align-items-center">
                      <div className="d-flex align-items-center gap-2">
                        <span className="rounded-circle bg-success" style={{ width: '10px', height: '10px', display: 'inline-block' }}></span>
                        <span className="small">{t('analytics.activeLabel')}</span>
                      </div>
                      <span className="fw-semibold small">{eventosActivos}</span>
                    </div>
                    <div className="d-flex justify-content-between align-items-center">
                      <div className="d-flex align-items-center gap-2">
                        <span className="rounded-circle bg-secondary" style={{ width: '10px', height: '10px', display: 'inline-block' }}></span>
                        <span className="small">{t('analytics.finishedLabel')}</span>
                      </div>
                      <span className="fw-semibold small">{eventosTerminados}</span>
                    </div>
                    <div className="d-flex justify-content-between align-items-center">
                      <div className="d-flex align-items-center gap-2">
                        <span className="rounded-circle bg-danger" style={{ width: '10px', height: '10px', display: 'inline-block' }}></span>
                        <span className="small">{t('analytics.cancelledLabel')}</span>
                      </div>
                      <span className="fw-semibold small">{eventosCancelados}</span>
                    </div>
                  </div>
                </div>
              ) : (
                <div className="text-center py-4">
                  <i className="bi bi-pie-chart text-secondary fs-1 d-block mb-2"></i>
                  <p className="text-secondary small mb-0">{t('analytics.noEventsRegistered')}</p>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>

      <div className="row g-3 mb-4">
        {/* Categories */}
        <div className="col-12 col-lg-6">
          <div className="card border-0 shadow-sm rounded-4 h-100">
            <div className="card-body p-3">
              <h6 className="fw-bold mb-3">
                <i className="bi bi-tags-fill text-primary me-2"></i>
                {t('analytics.eventsByCategory')}
              </h6>
              {filteredCategoryData.length > 0 ? (
                <div>
                  {filteredCategoryData.map((cat, i) => (
                    <div key={i} className="mb-3">
                      <div className="d-flex justify-content-between align-items-center mb-1">
                        <span className="small fw-medium">{cat.name}</span>
                        <span className="badge bg-primary bg-opacity-10 text-primary small">{cat.total} {t('analytics.events')}</span>
                      </div>
                      <div className="progress" style={{ height: '6px' }}>
                        <div className="progress-bar bg-primary" style={{ width: `${totalEventos > 0 ? (cat.total / totalEventos) * 100 : 0}%` }}></div>
                      </div>
                      {cat.activos > 0 && (
                        <div className="text-success mt-1" style={{ fontSize: '11px' }}>{cat.activos} {t('analytics.activeCount')}</div>
                      )}
                    </div>
                  ))}
                </div>
              ) : (
                <div className="text-center py-4">
                  <i className="bi bi-tags text-secondary fs-1 d-block mb-2"></i>
                  <p className="text-secondary small mb-0">{t('analytics.noEventsToShow')}</p>
                </div>
              )}
            </div>
          </div>
        </div>

        {/* Demographics */}
        <div className="col-12 col-lg-6">
          <div className="card border-0 shadow-sm rounded-4 h-100">
            <div className="card-body p-3">
              <h6 className="fw-bold mb-3">
                <i className="bi bi-people-fill text-primary me-2"></i>
                {t('analytics.userDemographics')}
              </h6>
              {totalUsuarios > 0 ? (
                <div>
                  {/* Role distribution */}
                  <div>
                    <div className="small text-secondary mb-2 fw-semibold">{t('analytics.roleDistribution')}</div>
                    <div className="row g-2">
                      <div className="col-6">
                        <div className="border rounded-3 p-2 text-center">
                          <div className="fw-bold fs-4 text-primary">{estudiantes.length}</div>
                          <div className="text-secondary" style={{ fontSize: '11px' }}>{t('analytics.studentsLabel')}</div>
                        </div>
                      </div>
                      <div className="col-6">
                        <div className="border rounded-3 p-2 text-center">
                          <div className="fw-bold fs-4 text-warning">{admins.length}</div>
                          <div className="text-secondary" style={{ fontSize: '11px' }}>{t('analytics.administratorsLabel')}</div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              ) : (
                <div className="text-center py-4">
                  <i className="bi bi-person text-secondary fs-1 d-block mb-2"></i>
                  <p className="text-secondary small mb-0">{t('analytics.noEventsToShow')}</p>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>

      {/* Full event table */}
      <div className="card border-0 shadow-sm rounded-4">
        <div className="card-body p-0">
          <div className="p-3 pb-2">
            <h6 className="fw-bold mb-0">
              <i className="bi bi-table text-primary me-2"></i>
              {t('analytics.eventDetails')}
            </h6>
          </div>
          {filteredAttendanceData.length > 0 ? (
            <div className="table-responsive">
              <table className="table table-hover mb-0 align-middle">
                <thead className="border-top">
                  <tr>
                    <th className="text-uppercase text-secondary small fw-semibold ps-3 py-3" style={{ fontSize: '11px' }}>{t('analytics.eventCol')}</th>
                    <th className="text-uppercase text-secondary small fw-semibold py-3" style={{ fontSize: '11px' }}>{t('analytics.categoryCol')}</th>
                    <th className="text-uppercase text-secondary small fw-semibold py-3" style={{ fontSize: '11px' }}>{t('events.status')}</th>
                    <th className="text-uppercase text-secondary small fw-semibold py-3" style={{ fontSize: '11px' }}>{t('analytics.enrolledCol')}</th>
                    <th className="text-uppercase text-secondary small fw-semibold py-3" style={{ fontSize: '11px' }}>{t('analytics.attendanceCol')}</th>
                    <th className="text-uppercase text-secondary small fw-semibold pe-3 py-3" style={{ fontSize: '11px' }}>{t('analytics.rateCol')}</th>
                  </tr>
                </thead>
                <tbody>
                  {filteredAttendanceData.map(ev => (
                    <tr key={ev.id}>
                      <td className="ps-3 py-3 fw-semibold small">{ev.nombre}</td>
                      <td className="py-3">
                        <span className="badge bg-primary bg-opacity-10 text-primary rounded-pill px-2">{ev.categoria}</span>
                      </td>
                      <td className="py-3">
                        <span className={`badge bg-opacity-10 rounded-pill px-3 ${
                          ev.estado === 'ACTIVO' ? 'bg-success text-success' :
                          ev.estado === 'CANCELADO' ? 'bg-danger text-danger' : 'bg-secondary text-secondary'
                        }`}>{ev.estado}</span>
                      </td>
                      <td className="py-3 small">{ev.inscritos}</td>
                      <td className="py-3 small">{ev.asistencias}</td>
                      <td className="pe-3 py-3">
                        <div className="d-flex align-items-center gap-2">
                          <div className="progress flex-grow-1" style={{ height: '6px', minWidth: '50px' }}>
                            <div className={`progress-bar ${ev.rate >= 70 ? 'bg-success' : ev.rate >= 40 ? 'bg-warning' : 'bg-danger'}`}
                              style={{ width: `${ev.rate}%` }}></div>
                          </div>
                          <span className="small fw-semibold" style={{ minWidth: '35px' }}>{ev.rate}%</span>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : (
            <div className="text-center py-5">
              <i className="bi bi-inbox text-secondary fs-1 d-block mb-2"></i>
              <p className="text-secondary small mb-0">{t('analytics.noEventsToShow')}</p>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}

export default AdminAnalytics
