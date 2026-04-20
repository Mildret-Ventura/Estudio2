import { useState, useEffect } from 'react'
import { toast } from 'react-toastify'
import { useTranslation } from '../../i18n/I18nContext'
import { userService } from '../../services/userService'
import CambiarContrasenaModal from '../../components/modals/CambiarContrasenaModal'

function AdminPerfil({ user }) {
  const { t, language, setLanguage } = useTranslation()
  const [perfil, setPerfil] = useState(null)
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)

  // Editable fields
  const [nombre, setNombre] = useState('')
  const [apellidoPaterno, setApellidoPaterno] = useState('')
  const [apellidoMaterno, setApellidoMaterno] = useState('')

  useEffect(() => {
    if (!user?.id) return
    const fetchPerfil = async () => {
      try {
        const data = await userService.getPerfil(user.id)
        setPerfil(data)
        setNombre(data.nombre || '')
        setApellidoPaterno(data.apellidoPaterno || '')
        setApellidoMaterno(data.apellidoMaterno || '')
      } catch (err) {
        toast.error(t('common.error'))
      } finally {
        setLoading(false)
      }
    }
    fetchPerfil()
  }, [user?.id])

  const handleSave = async () => {
    if (!nombre.trim() || !apellidoPaterno.trim()) {
      toast.error(t('profile.requiredFields'))
      return
    }
    setSaving(true)
    try {
      await userService.actualizarPerfil(user.id, {
        nombre: nombre.trim(),
        apellidoPaterno: apellidoPaterno.trim(),
        apellidoMaterno: apellidoMaterno.trim(),
      })
      toast.success(t('profile.profileUpdated'))
      const updated = await userService.getPerfil(user.id)
      setPerfil(updated)
    } catch (err) {
      toast.error(t('common.error'))
    } finally {
      setSaving(false)
    }
  }

  const hasChanges = perfil && (
    nombre.trim() !== (perfil.nombre || '') ||
    apellidoPaterno.trim() !== (perfil.apellidoPaterno || '') ||
    apellidoMaterno.trim() !== (perfil.apellidoMaterno || '')
  )

  const handleCancel = () => {
    if (!perfil) return
    setNombre(perfil.nombre || '')
    setApellidoPaterno(perfil.apellidoPaterno || '')
    setApellidoMaterno(perfil.apellidoMaterno || '')
  }

  const userName = perfil
    ? `${perfil.nombre} ${perfil.apellidoPaterno}`
    : user?.name || 'Administrador'

  const userInitials = userName.split(' ').filter(Boolean).map(n => n[0]).join('').toUpperCase().slice(0, 2)

  const rolLabel = perfil?.rol === 'SUPERADMIN'
    ? t('profile.superAdmin')
    : t('profile.adminRole')

  if (loading) {
    return (
      <div className="d-flex justify-content-center align-items-center py-5">
        <div className="spinner-border text-primary" role="status">
          <span className="visually-hidden">{t('common.loading')}</span>
        </div>
      </div>
    )
  }

  return (
    <div>
      <h2 className="fw-bold mb-4">{t('profile.title')}</h2>

      <div className="card border-0 shadow-sm rounded-4 mb-4">
        <div className="card-body p-4">
          <div className="d-flex flex-column flex-md-row align-items-center gap-3 mb-4">
            <div className="rounded-circle border border-2 border-primary bg-primary bg-opacity-10 d-flex align-items-center justify-content-center flex-shrink-0 fw-bold text-primary"
              style={{ width: '90px', height: '90px', fontSize: '32px' }}>
              {userInitials}
            </div>
            <div className="text-center text-md-start">
              <h5 className="fw-bold mb-0">{userName}</h5>
              <span className={`badge ${perfil?.estado === 'ACTIVO' ? 'bg-success bg-opacity-10 text-success' : 'bg-danger bg-opacity-10 text-danger'} small`}>
                {perfil?.estado || 'Activo'}
              </span>
            </div>
          </div>

          <h6 className="fw-bold mb-3">
            <i className="bi bi-person-badge text-primary me-2"></i>
            {t('profile.personalInfo')}
          </h6>

          <div className="row g-3 mb-4">
            <div className="col-12 col-md-4">
              <label className="form-label text-secondary small">{t('profile.firstName')}</label>
              <input type="text" className="form-control" value={nombre} onChange={e => setNombre(e.target.value)} />
            </div>
            <div className="col-12 col-md-4">
              <label className="form-label text-secondary small">{t('profile.lastNameP')}</label>
              <input type="text" className="form-control" value={apellidoPaterno} onChange={e => setApellidoPaterno(e.target.value)} />
            </div>
            <div className="col-12 col-md-4">
              <label className="form-label text-secondary small">{t('profile.lastNameM')}</label>
              <input type="text" className="form-control" value={apellidoMaterno} onChange={e => setApellidoMaterno(e.target.value)} />
            </div>
            <div className="col-12 col-md-6">
              <label className="form-label text-secondary small">{t('profile.email')}</label>
              <input type="email" className="form-control bg-light" value={perfil?.correo || ''} readOnly />
            </div>
            <div className="col-12 col-md-6">
              <label className="form-label text-secondary small">{t('profile.role')}</label>
              <input type="text" className="form-control bg-light" value={rolLabel} readOnly />
            </div>
          </div>

          <hr className="my-4" />

          <h6 className="fw-bold mb-3">
            <i className="bi bi-shield-lock text-primary me-2"></i>
            {t('profile.security')}
          </h6>

          <div className="mb-4">
            <label className="form-label text-secondary small">{t('profile.password')}</label>
            <div className="d-flex align-items-center gap-3">
              <div className="input-group" style={{ maxWidth: '250px' }}>
                <span className="input-group-text bg-light border-end-0">
                  <i className="bi bi-lock text-secondary"></i>
                </span>
                <input type="password" className="form-control bg-light border-start-0" defaultValue="••••••••" readOnly />
              </div>
              <button
                className="btn btn-link text-primary text-decoration-none small fw-semibold p-0"
                data-bs-toggle="modal"
                data-bs-target="#cambiarContrasenaModal"
              >
                <i className="bi bi-pencil me-1"></i>
                {t('profile.changePassword')}
              </button>
            </div>
          </div>

          <hr className="my-4" />

          <h6 className="fw-bold mb-3">
            <i className="bi bi-translate text-primary me-2"></i>
            {t('profile.language')}
          </h6>
          <div className="d-flex gap-2 mb-4">
            <button className={`btn ${language === 'es' ? 'btn-primary' : 'btn-outline-primary'} rounded-pill px-3`} onClick={() => setLanguage('es')}>
              {t('profile.spanish')}
            </button>
            <button className={`btn ${language === 'en' ? 'btn-primary' : 'btn-outline-primary'} rounded-pill px-3`} onClick={() => setLanguage('en')}>
              {t('profile.english')}
            </button>
          </div>

          <div className="d-flex justify-content-end gap-2">
            <button className="btn btn-link text-secondary text-decoration-none" onClick={handleCancel} disabled={saving}>
              {t('profile.cancel')}
            </button>
            <button className="btn btn-primary rounded-pill px-4 d-flex align-items-center gap-2" onClick={handleSave} disabled={saving || !hasChanges}>
              {saving ? (
                <span className="spinner-border spinner-border-sm"></span>
              ) : (
                <i className="bi bi-save"></i>
              )}
              {saving ? t('profile.saving') : t('profile.saveChanges')}
            </button>
          </div>
        </div>
      </div>

      <CambiarContrasenaModal correo={perfil?.correo || user?.email || ''} />
    </div>
  )
}

export default AdminPerfil
