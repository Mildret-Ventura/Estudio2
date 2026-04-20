import { useState, useEffect, useRef } from 'react'
import { toast } from 'react-toastify'
import { useTranslation } from '../../i18n/I18nContext'
import { eventService } from '../../services/eventService'
import { closeModal, apiGet } from '../../services/apiHelper'

const INITIAL_FORM = { nombre: '', correo: '', descripcion: '' }
const DESC_MAX_LENGTH = 60

function AdminOrganizadores() {
  const { t } = useTranslation()
  const [organizadores, setOrganizadores] = useState([])
  const [loading, setLoading] = useState(true)
  const [search, setSearch] = useState('')

  // Create/Edit state
  const [formData, setFormData] = useState(INITIAL_FORM)
  const [editingId, setEditingId] = useState(null)
  const [formLoading, setFormLoading] = useState(false)
  const [formError, setFormError] = useState('')

  // Delete state
  const [deleteTarget, setDeleteTarget] = useState(null)
  const [deleteLoading, setDeleteLoading] = useState(false)
  const orgModalCloseBtnRef = useRef(null)
  const deleteOrgCloseBtnRef = useRef(null)

  // Detail view state
  const [detailTarget, setDetailTarget] = useState(null)

  const fetchOrganizadores = async () => {
    try {
      const data = await apiGet('/eventos/organizadores')
      setOrganizadores(data)
    } catch {
      setOrganizadores([])
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { fetchOrganizadores() }, [])

  const filtered = search
    ? organizadores.filter(o =>
        (o.nombre || '').toLowerCase().includes(search.toLowerCase()) ||
        (o.correo || '').toLowerCase().includes(search.toLowerCase())
      )
    : organizadores

  const handleChange = (e) => {
    setFormData(prev => ({ ...prev, [e.target.name]: e.target.value }))
    setFormError('')
  }

  const openCreate = () => {
    setFormData(INITIAL_FORM)
    setEditingId(null)
    setFormError('')
  }

  const openEdit = (org) => {
    setFormData({ nombre: org.nombre, correo: org.correo || '', descripcion: org.descripcion || '' })
    setEditingId(org.idOrganizador)
    setFormError('')
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (!formData.nombre.trim()) {
      setFormError(t('organizers.nameRequired'))
      return
    }
    setFormLoading(true)
    try {
      if (editingId) {
        await eventService.actualizarOrganizador(editingId, formData)
        toast.success(t('organizers.organizerUpdated'))
      } else {
        await eventService.crearOrganizador(formData)
        toast.success(t('organizers.organizerCreated'))
      }
      setFormData(INITIAL_FORM)
      setEditingId(null)
      orgModalCloseBtnRef.current?.click()
      setLoading(true)
      fetchOrganizadores()
    } catch (err) {
      setFormError(err.message)
      toast.error(err.message)
    } finally {
      setFormLoading(false)
    }
  }

  const handleDelete = async () => {
    if (!deleteTarget) return
    setDeleteLoading(true)
    try {
      await eventService.eliminarOrganizador(deleteTarget.idOrganizador)
      toast.success(t('organizers.organizerDeleted'))
      setDeleteTarget(null)
      deleteOrgCloseBtnRef.current?.click()
      setLoading(true)
      fetchOrganizadores()
    } catch (err) {
      toast.error(err.message)
    } finally {
      setDeleteLoading(false)
    }
  }

  return (
    <div>
      <div className="d-flex flex-column flex-md-row justify-content-between align-items-start align-items-md-center mb-4 gap-3">
        <div>
          <h2 className="fw-bold mb-1">{t('organizers.title')}</h2>
          <p className="text-secondary small mb-0">
            {t('organizers.subtitle')}
          </p>
        </div>
        <button
          className="btn btn-primary rounded-pill d-flex align-items-center gap-2 flex-shrink-0"
          data-bs-toggle="modal"
          data-bs-target="#orgFormModal"
          onClick={openCreate}
        >
          <i className="bi bi-plus-circle"></i>
          {t('organizers.newOrganizer')}
        </button>
      </div>

      <div className="card border-0 shadow-sm rounded-4 mb-4">
        <div className="card-header bg-white border-bottom-0 p-4">
          <div className="input-group bg-light rounded-3 overflow-hidden" style={{ maxWidth: '400px', border: 'none' }}>
            <span className="input-group-text bg-transparent border-0 pe-1">
              <i className="bi bi-search text-secondary"></i>
            </span>
            <input
              type="text"
              className="form-control bg-transparent border-0 shadow-none small"
              placeholder={t('organizers.searchPlaceholder')}
              style={{ fontSize: '13px' }}
              value={search}
              onChange={(e) => setSearch(e.target.value)}
            />
          </div>
        </div>

        <div className="card-body p-0">
          {loading ? (
            <div className="text-center py-5">
              <div className="spinner-border text-primary" role="status">
                <span className="visually-hidden">{t('common.loading')}</span>
              </div>
            </div>
          ) : filtered.length > 0 ? (
            <div className="table-responsive">
              <table className="table table-hover align-middle mb-0">
                <thead>
                  <tr>
                    <th className="text-uppercase text-secondary small fw-bold pb-3 border-0 border-bottom ps-4" style={{ fontSize: '10px', letterSpacing: '1px' }}>{t('organizers.name')}</th>
                    <th className="text-uppercase text-secondary small fw-bold pb-3 border-0 border-bottom" style={{ fontSize: '10px', letterSpacing: '1px' }}>{t('organizers.email')}</th>
                    <th className="text-uppercase text-secondary small fw-bold pb-3 border-0 border-bottom" style={{ fontSize: '10px', letterSpacing: '1px' }}>{t('organizers.description')}</th>
                    <th className="text-uppercase text-secondary small fw-bold pb-3 border-0 border-bottom text-end pe-4" style={{ fontSize: '10px', letterSpacing: '1px' }}>{t('organizers.actions')}</th>
                  </tr>
                </thead>
                <tbody className="border-top-0">
                  {filtered.map(org => (
                    <tr key={org.idOrganizador}>
                      <td className="py-3 border-light ps-4">
                        <div className="d-flex align-items-center gap-3">
                          <div className="bg-primary bg-opacity-10 text-primary rounded-circle d-flex align-items-center justify-content-center fw-bold" style={{ width: '32px', height: '32px', fontSize: '12px' }}>
                            {(org.nombre || '').slice(0, 2).toUpperCase()}
                          </div>
                          <span className="fw-bold text-dark small">{org.nombre}</span>
                        </div>
                      </td>
                      <td className="small py-3 border-light text-secondary">{org.correo || '—'}</td>
                      <td className="small py-3 border-light text-secondary">
                        {org.descripcion
                          ? org.descripcion.length > DESC_MAX_LENGTH
                            ? `${org.descripcion.substring(0, DESC_MAX_LENGTH)}...`
                            : org.descripcion
                          : '—'}
                      </td>
                      <td className="py-3 border-light text-end pe-4">
                        <div className="d-flex justify-content-end gap-2">
                          <button
                            className="btn btn-link text-primary p-0"
                            title="Ver detalle"
                            onClick={() => setDetailTarget(org)}
                          >
                            <i className="bi bi-eye" style={{ fontSize: '13px' }}></i>
                          </button>
                          <button
                            className="btn btn-link text-secondary p-0"
                            title="Editar"
                            data-bs-toggle="modal"
                            data-bs-target="#orgFormModal"
                            onClick={() => openEdit(org)}
                          >
                            <i className="bi bi-pencil" style={{ fontSize: '13px' }}></i>
                          </button>
                          <button
                            className="btn btn-link text-danger p-0"
                            title="Eliminar"
                            data-bs-toggle="modal"
                            data-bs-target="#deleteOrgModal"
                            onClick={() => setDeleteTarget(org)}
                          >
                            <i className="bi bi-trash" style={{ fontSize: '13px' }}></i>
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : (
            <div className="text-center py-5">
              <div className="rounded-circle bg-primary bg-opacity-10 d-inline-flex align-items-center justify-content-center mb-3" style={{ width: '56px', height: '56px' }}>
                <i className="bi bi-building text-primary fs-4"></i>
              </div>
              <h6 className="fw-bold mb-1">{t('organizers.noOrganizers')}</h6>
              <p className="text-secondary small mb-0">
                {t('organizers.createMsg')}
              </p>
            </div>
          )}
        </div>
        <div className="card-footer bg-transparent border-top p-3">
          <span className="text-secondary small">
            {t('organizers.showing', { current: filtered.length, total: organizadores.length })}
          </span>
        </div>
      </div>

      {/* Create/Edit Modal */}
      <div className="modal fade" id="orgFormModal" tabIndex="-1" aria-hidden="true">
        <div className="modal-dialog modal-dialog-centered">
          <form onSubmit={handleSubmit} noValidate className="modal-content border-0 rounded-4 shadow">
            <div className="modal-header border-0 px-4 pt-4 pb-0">
              <h5 className="fw-bold">{editingId ? t('organizers.editOrganizer') : t('organizers.newOrganizer')}</h5>
              <button type="button" className="btn-close" data-bs-dismiss="modal" aria-label={t('common.close')} ref={orgModalCloseBtnRef}></button>
            </div>
            <div className="modal-body px-4 py-3">
              {formError && (
                <div className="alert alert-danger small p-2 mb-3">{formError}</div>
              )}
              <div className="mb-3">
                <label className="form-label fw-semibold small">{t('organizers.name')} *</label>
                <input
                  type="text"
                  name="nombre"
                  className="form-control"
                  placeholder={t('organizers.name')}
                  value={formData.nombre}
                  onChange={handleChange}
                />
              </div>
              <div className="mb-3">
                <label className="form-label fw-semibold small">{t('organizers.email')}</label>
                <input
                  type="email"
                  name="correo"
                  className="form-control"
                  placeholder="correo@ejemplo.com"
                  value={formData.correo}
                  onChange={handleChange}
                />
              </div>
              <div className="mb-3">
                <label className="form-label fw-semibold small">{t('organizers.description')}</label>
                <textarea
                  name="descripcion"
                  className="form-control"
                  rows="2"
                  placeholder={t('organizers.descriptionPlaceholder')}
                  value={formData.descripcion}
                  onChange={handleChange}
                ></textarea>
              </div>
            </div>
            <div className="modal-footer border-top px-4 py-3">
              <button type="button" className="btn btn-link text-secondary text-decoration-none" data-bs-dismiss="modal">{t('common.cancel')}</button>
              <button type="submit" className="btn btn-primary rounded-pill px-4" disabled={formLoading}>
                {formLoading ? t('common.processing') : editingId ? t('organizers.update') : t('organizers.create')}
              </button>
            </div>
          </form>
        </div>
      </div>

      {/* Delete Confirmation Modal */}
      <div className="modal fade" id="deleteOrgModal" tabIndex="-1" aria-hidden="true">
        <div className="modal-dialog modal-dialog-centered modal-sm">
          <div className="modal-content border-0 rounded-4 shadow text-center p-4">
            <div className="mb-3">
              <i className="bi bi-exclamation-triangle-fill text-danger" style={{ fontSize: '3rem' }}></i>
            </div>
            <h6 className="fw-bold mb-2">{t('organizers.deleteOrganizer')}</h6>
            <p className="text-secondary small mb-3">
              {t('organizers.deleteConfirm', { name: deleteTarget?.nombre })}
            </p>
            <div className="d-flex justify-content-center gap-2">
              <button className="btn btn-link text-secondary text-decoration-none" data-bs-dismiss="modal" ref={deleteOrgCloseBtnRef}>{t('common.cancel')}</button>
              <button className="btn btn-danger rounded-pill px-4" onClick={handleDelete} disabled={deleteLoading}>
                {deleteLoading ? t('organizers.deleting') : t('organizers.delete')}
              </button>
            </div>
          </div>
        </div>
      </div>
      {/* Detail View Modal */}
      {detailTarget && (
        <div className="position-fixed top-0 start-0 w-100 h-100 d-flex align-items-center justify-content-center" style={{ backgroundColor: 'rgba(0,0,0,0.5)', zIndex: 1055 }}>
          <div className="bg-white border-0 rounded-4 shadow" style={{ maxWidth: '500px', width: '90%' }}>
              <div className="modal-header border-0 px-4 pt-4 pb-0">
                <h5 className="fw-bold">{t('organizers.detail')}</h5>
                <button type="button" className="btn-close" onClick={() => setDetailTarget(null)}></button>
              </div>
              <div className="modal-body px-4 py-3">
                <div className="mb-3">
                  <span className="text-secondary small fw-bold text-uppercase">{t('organizers.name')}</span>
                  <div className="fw-semibold">{detailTarget.nombre}</div>
                </div>
                {detailTarget.correo && (
                  <div className="mb-3">
                    <span className="text-secondary small fw-bold text-uppercase">{t('organizers.email')}</span>
                    <div>{detailTarget.correo}</div>
                  </div>
                )}
                <div>
                  <span className="text-secondary small fw-bold text-uppercase">{t('organizers.description')}</span>
                  <div className="mt-1" style={{ whiteSpace: 'pre-wrap' }}>{detailTarget.descripcion}</div>
                </div>
              </div>
              <div className="modal-footer border-top px-4 py-3">
                <button className="btn btn-primary rounded-pill px-4" onClick={() => setDetailTarget(null)}>{t('organizers.close')}</button>
              </div>
          </div>
        </div>
      )}
    </div>
  )
}

export default AdminOrganizadores
