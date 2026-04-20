import { useState, useEffect, useRef, useCallback } from 'react'
import { toast } from 'react-toastify'
import { useTranslation } from '../../i18n/I18nContext'
import { diplomaService } from '../../services/diplomaService'

function EditarDiplomaModal({ diploma, onSubmit, isLoading }) {
  const { t } = useTranslation()
  const [formData, setFormData] = useState({ firma: '', diseno: 'Personalizado', plantillaPdf: '', firmaImagen: '' })
  const [pdfFileName, setPdfFileName] = useState('')
  const [firmaFileName, setFirmaFileName] = useState('')
  const [firmaPreviewActual, setFirmaPreviewActual] = useState(null)
  const [newFirmaPreview, setNewFirmaPreview] = useState(null)
  const [plantillaQuitada, setPlantillaQuitada] = useState(false)
  const [firmaQuitada, setFirmaQuitada] = useState(false)
  const [loadingDetail, setLoadingDetail] = useState(false)
  const [previewPdfUrl, setPreviewPdfUrl] = useState(null)
  const [loadingPreview, setLoadingPreview] = useState(false)
  const [errors, setErrors] = useState({})
  const pdfInputRef = useRef(null)
  const firmaInputRef = useRef(null)
  const closeBtnRef = useRef(null)
  const previewBlobRef = useRef(null)

  const firmaPreviewMostrada = newFirmaPreview || (firmaQuitada ? null : firmaPreviewActual)

  const generarPreviewTemplate = useCallback(async (plantillaPdf, firmaImg) => {
    if (!plantillaPdf) return
    setLoadingPreview(true)
    try {
      const blob = await diplomaService.previewTemplate({
        plantillaPdf,
        eventName: diploma?.nombreEvento || '',
        signerName: formData.firma || '',
        firmaImagen: firmaImg || '',
      })
      if (previewBlobRef.current) URL.revokeObjectURL(previewBlobRef.current)
      const url = URL.createObjectURL(blob)
      previewBlobRef.current = url
      setPreviewPdfUrl(url)
    } catch (err) {
      console.error('Error generando previsualización:', err?.message || err)
      toast.warning('No se pudo generar la previsualización: ' + (err?.message || 'Error desconocido'))
    } finally {
      setLoadingPreview(false)
    }
  }, [diploma?.nombreEvento, formData.firma])

  const cargarPreviewDiploma = useCallback(async (idDiploma) => {
    setLoadingPreview(true)
    try {
      const blob = await diplomaService.previewDiploma(idDiploma)
      if (previewBlobRef.current) URL.revokeObjectURL(previewBlobRef.current)
      const url = URL.createObjectURL(blob)
      previewBlobRef.current = url
      setPreviewPdfUrl(url)
    } catch {
      setPreviewPdfUrl(null)
    } finally {
      setLoadingPreview(false)
    }
  }, [])

  useEffect(() => {
    if (!diploma) return
    setFormData({ firma: diploma.firma || '', diseno: diploma.diseno || 'Personalizado', plantillaPdf: '', firmaImagen: '' })
    setPdfFileName('')
    setFirmaFileName('')
    setNewFirmaPreview(null)
    setPlantillaQuitada(false)
    setFirmaQuitada(false)
    setErrors({})
    setFirmaPreviewActual(null)
    setPreviewPdfUrl(null)
    if (previewBlobRef.current) { URL.revokeObjectURL(previewBlobRef.current); previewBlobRef.current = null }

    setLoadingDetail(true)
    diplomaService.obtenerDiploma(diploma.idDiploma)
      .then(detail => {
        if (detail?.firmaImagen) {
          const src = detail.firmaImagen.startsWith('data:')
            ? detail.firmaImagen
            : `data:image/png;base64,${detail.firmaImagen}`
          setFirmaPreviewActual(src)
        }
      })
      .catch(() => {})
      .finally(() => setLoadingDetail(false))

    // Cargar previsualización real del diploma actual
    if (diploma.tienePlantilla) {
      cargarPreviewDiploma(diploma.idDiploma)
    }
  }, [diploma, cargarPreviewDiploma])

  useEffect(() => {
    const modalEl = document.getElementById('editarDiplomaModal')
    if (!modalEl) return
    const handleHide = () => {
      if (document.activeElement && modalEl.contains(document.activeElement)) {
        document.activeElement.blur()
      }
    }
    modalEl.addEventListener('hide.bs.modal', handleHide)
    return () => modalEl.removeEventListener('hide.bs.modal', handleHide)
  }, [])

  useEffect(() => {
    return () => { if (previewBlobRef.current) URL.revokeObjectURL(previewBlobRef.current) }
  }, [])

  const handleFileChange = (e, field) => {
    const file = e.target.files[0]
    if (!file) return

    if (field === 'plantillaPdf' && !file.name.endsWith('.jrxml')) {
      toast.warning('Solo se permiten plantillas Jasper (.jrxml)')
      return
    }
    if (field === 'firmaImagen' && !file.type.startsWith('image/')) {
      toast.warning('Solo se permiten imágenes')
      return
    }
    if (file.size > 15 * 1024 * 1024) {
      toast.warning('El archivo no debe exceder 15 MB')
      return
    }

    const reader = new FileReader()
    reader.onload = () => {
      setFormData(prev => ({ ...prev, [field]: reader.result }))
      if (field === 'plantillaPdf') {
        setPdfFileName(file.name)
        setPlantillaQuitada(false)
        generarPreviewTemplate(reader.result, firmaPreviewMostrada)
      }
      if (field === 'firmaImagen') {
        setFirmaFileName(file.name)
        setFirmaQuitada(false)
        setNewFirmaPreview(reader.result)
      }
    }
    reader.readAsDataURL(file)
  }

  const quitarPlantilla = () => {
    setPlantillaQuitada(true)
    setPdfFileName('')
    setFormData(prev => ({ ...prev, plantillaPdf: '' }))
    if (pdfInputRef.current) pdfInputRef.current.value = ''
  }

  const quitarFirma = () => {
    setFirmaQuitada(true)
    setFirmaFileName('')
    setNewFirmaPreview(null)
    setFormData(prev => ({ ...prev, firmaImagen: '' }))
    if (firmaInputRef.current) firmaInputRef.current.value = ''
  }

  const handleSubmit = async () => {
    const datos = {}
    if (formData.firma !== (diploma?.firma || '')) datos.firma = formData.firma
    if (formData.diseno && formData.diseno !== (diploma?.diseno || 'Personalizado')) datos.diseno = formData.diseno
    if (formData.plantillaPdf) datos.plantillaPdf = formData.plantillaPdf
    if (formData.firmaImagen) datos.firmaImagen = formData.firmaImagen

    if (Object.keys(datos).length === 0) {
      setErrors({ general: 'No hay cambios para guardar' })
      return
    }
    setErrors({})
    try {
      await onSubmit(diploma.idDiploma, datos)
      closeBtnRef.current?.click()
    } catch {
      // El error ya fue mostrado por el padre
    }
  }

  // Sección de plantilla: muestra el estado actual o el área de subida
  const renderPlantillaSection = () => {
    const tieneActual = diploma?.tienePlantilla && !plantillaQuitada && !pdfFileName
    const tieneNueva = !!pdfFileName

    if (tieneActual) {
      return (
        <div className="d-flex align-items-center gap-3 p-3 bg-light rounded-3 border">
          <div className="bg-success bg-opacity-10 text-success rounded d-flex align-items-center justify-content-center" style={{ width: '40px', height: '40px', minWidth: '40px' }}>
            <i className="bi bi-file-earmark-check fs-5"></i>
          </div>
          <div className="flex-grow-1">
            <p className="mb-0 fw-semibold small text-dark">Plantilla JRXML activa</p>
            <p className="mb-0 text-secondary" style={{ fontSize: '11px' }}>Diseño guardado en el diploma</p>
          </div>
          <div className="d-flex gap-1">
            <button
              type="button"
              className="btn btn-sm btn-outline-primary"
              style={{ fontSize: '11px' }}
              onClick={() => pdfInputRef.current?.click()}
            >
              Cambiar
            </button>
            <button
              type="button"
              className="btn btn-sm btn-outline-danger"
              style={{ fontSize: '11px' }}
              onClick={quitarPlantilla}
            >
              Quitar
            </button>
          </div>
          <input ref={pdfInputRef} type="file" className="d-none" accept=".jrxml" onChange={(e) => handleFileChange(e, 'plantillaPdf')} />
        </div>
      )
    }

    if (tieneNueva) {
      return (
        <div className="d-flex align-items-center gap-3 p-3 bg-light rounded-3 border">
          <div className="bg-primary bg-opacity-10 text-primary rounded d-flex align-items-center justify-content-center" style={{ width: '40px', height: '40px', minWidth: '40px' }}>
            <i className="bi bi-file-earmark-code fs-5"></i>
          </div>
          <div className="flex-grow-1 overflow-hidden">
            <p className="mb-0 fw-semibold small text-truncate">{pdfFileName}</p>
            <p className="mb-0 text-secondary" style={{ fontSize: '11px' }}>Nueva plantilla seleccionada</p>
          </div>
          <button
            type="button"
            className="btn btn-sm btn-outline-danger rounded-circle"
            style={{ width: '30px', height: '30px', padding: 0 }}
            onClick={() => { setPdfFileName(''); setFormData(prev => ({ ...prev, plantillaPdf: '' })); if (pdfInputRef.current) pdfInputRef.current.value = '' }}
          >
            <i className="bi bi-x"></i>
          </button>
          <input ref={pdfInputRef} type="file" className="d-none" accept=".jrxml" onChange={(e) => handleFileChange(e, 'plantillaPdf')} />
        </div>
      )
    }

    // Sin plantilla — mostrar área de subida
    return (
      <div>
        <div
          className="border border-primary border-opacity-25 rounded-3 d-flex flex-column py-3 px-3 text-center"
          style={{ borderStyle: 'dashed', backgroundColor: '#f8faff', cursor: 'pointer' }}
          onClick={() => pdfInputRef.current?.click()}
        >
          <i className="bi bi-cloud-arrow-up text-primary fs-3 mb-2"></i>
          <div className="text-secondary" style={{ fontSize: '11px', lineHeight: '1.5' }}>
            Subir plantilla JRXML
            <br />{t('editDiploma.pdfLimit')}
          </div>
          <input ref={pdfInputRef} type="file" className="d-none" accept=".jrxml" onChange={(e) => handleFileChange(e, 'plantillaPdf')} />
        </div>
      </div>
    )
  }

  // Sección de firma: muestra imagen actual o área de subida
  const renderFirmaSection = () => {
    const firmaActualVisible = firmaPreviewActual && !firmaQuitada && !newFirmaPreview

    if (firmaActualVisible) {
      return (
        <div className="d-flex align-items-center gap-3 p-3 bg-light rounded-3 border">
          <img
            src={firmaPreviewActual}
            alt="Firma actual"
            style={{ height: '48px', maxWidth: '120px', objectFit: 'contain', background: '#fff', borderRadius: '4px', border: '1px solid #dee2e6', padding: '2px' }}
          />
          <div className="flex-grow-1">
            <p className="mb-0 fw-semibold small text-dark">Imagen de firma actual</p>
            <p className="mb-0 text-secondary" style={{ fontSize: '11px' }}>Guardada en el diploma</p>
          </div>
          <div className="d-flex gap-1">
            <button
              type="button"
              className="btn btn-sm btn-outline-primary"
              style={{ fontSize: '11px' }}
              onClick={() => firmaInputRef.current?.click()}
            >
              Cambiar
            </button>
            <button
              type="button"
              className="btn btn-sm btn-outline-danger"
              style={{ fontSize: '11px' }}
              onClick={quitarFirma}
            >
              Quitar
            </button>
          </div>
          <input ref={firmaInputRef} type="file" className="d-none" accept="image/*" onChange={(e) => handleFileChange(e, 'firmaImagen')} />
        </div>
      )
    }

    if (newFirmaPreview) {
      return (
        <div className="text-center">
          <div className="d-inline-block position-relative mb-2">
            <img
              src={newFirmaPreview}
              alt="Nueva firma"
              className="border rounded-3"
              style={{ maxHeight: '100px', maxWidth: '100%', objectFit: 'contain', backgroundColor: '#f8f9fa' }}
            />
            <button
              type="button"
              className="btn btn-sm btn-danger rounded-circle position-absolute"
              style={{ top: '-8px', right: '-8px', width: '24px', height: '24px', padding: 0, fontSize: '12px' }}
              onClick={() => { setNewFirmaPreview(null); setFirmaFileName(''); setFormData(prev => ({ ...prev, firmaImagen: '' })); if (firmaInputRef.current) firmaInputRef.current.value = '' }}
            >
              <i className="bi bi-x"></i>
            </button>
          </div>
          <p className="text-secondary mb-0" style={{ fontSize: '11px' }}>Nueva firma seleccionada</p>
          <input ref={firmaInputRef} type="file" className="d-none" accept="image/*" onChange={(e) => handleFileChange(e, 'firmaImagen')} />
        </div>
      )
    }

    // Sin firma — mostrar área de subida
    return (
      <div
        className="border border-primary border-opacity-25 rounded-3 d-flex flex-column py-3 px-3 text-center"
        style={{ borderStyle: 'dashed', backgroundColor: '#f8faff', cursor: 'pointer' }}
        onClick={() => firmaInputRef.current?.click()}
      >
        <i className="bi bi-cloud-arrow-up text-primary fs-3 mb-2"></i>
        <div className="text-secondary" style={{ fontSize: '11px', lineHeight: '1.5' }}>
          {diploma?.tieneFirma && !firmaQuitada ? t('editDiploma.replaceSignature') : t('editDiploma.uploadSignature')}
          <br />{t('editDiploma.signatureLimit')}
        </div>
        <input ref={firmaInputRef} type="file" className="d-none" accept="image/*" onChange={(e) => handleFileChange(e, 'firmaImagen')} />
      </div>
    )
  }

  return (
    <div className="modal fade" id="editarDiplomaModal" tabIndex="-1" aria-labelledby="editarDiplomaModalLabel" aria-hidden="true">
      <div className="modal-dialog modal-dialog-centered modal-xl">
        <div className="modal-content border-0 rounded-4 shadow bg-light">
          <div className="modal-header border-0 bg-white px-4 pt-4 pb-3 rounded-top-4">
            <div>
              <h5 className="fw-bold mb-1 text-dark" id="editarDiplomaModalLabel">{t('editDiploma.title')}</h5>
              <p className="text-secondary small mb-0" style={{ fontSize: '13px' }}>{t('editDiploma.subtitle')}</p>
            </div>
            <button type="button" className="btn-close align-self-start mt-1" data-bs-dismiss="modal" aria-label={t('common.close')} ref={closeBtnRef}></button>
          </div>

          <div className="modal-body p-0 bg-light">
            <div className="row g-0" style={{ minHeight: '480px' }}>
              {/* ── Formulario ── */}
              <div className="col-12 col-lg-7 p-4" style={{ maxHeight: '65vh', overflowY: 'auto' }}>
                {/* Info evento */}
                <div className="bg-white rounded-3 p-4 mb-3 shadow-sm">
                  <div className="d-flex align-items-center gap-2 mb-3">
                    <i className="bi bi-info-circle text-primary"></i>
                    <h6 className="fw-bold mb-0 text-dark fs-6">{t('editDiploma.eventInfo')}</h6>
                  </div>
                  <input
                    type="text"
                    className="form-control bg-light text-dark small"
                    value={diploma?.nombreEvento || ''}
                    disabled
                    style={{ fontSize: '13px' }}
                  />
                </div>

                {/* Nombre firmante */}
                <div className="bg-white rounded-3 p-4 mb-3 shadow-sm">
                  <div className="d-flex align-items-center gap-2 mb-3">
                    <i className="bi bi-pen text-primary"></i>
                    <h6 className="fw-bold mb-0 text-dark fs-6">{t('editDiploma.signerName')}</h6>
                  </div>
                  <input
                    type="text"
                    className="form-control text-dark small"
                    placeholder={t('editDiploma.signerPlaceholder')}
                    value={formData.firma}
                    onChange={(e) => setFormData(prev => ({ ...prev, firma: e.target.value }))}
                    style={{ fontSize: '13px' }}
                  />
                </div>

                {/* Plantilla JRXML */}
                <div className="bg-white rounded-3 p-4 mb-3 shadow-sm">
                  <div className="d-flex align-items-center gap-2 mb-3">
                    <i className="bi bi-file-earmark-code text-primary"></i>
                    <h6 className="fw-bold mb-0 text-dark fs-6">{t('editDiploma.pdfTemplate')}</h6>
                  </div>
                  {renderPlantillaSection()}
                </div>

                {/* Firma imagen */}
                <div className="bg-white rounded-3 p-4 shadow-sm">
                  <div className="d-flex align-items-center gap-2 mb-3">
                    <i className="bi bi-image text-primary"></i>
                    <h6 className="fw-bold mb-0 text-dark fs-6">{t('editDiploma.signatureImage')}</h6>
                  </div>
                  {loadingDetail ? (
                    <div className="d-flex align-items-center gap-2 text-secondary small">
                      <div className="spinner-border spinner-border-sm" role="status"></div>
                      Cargando firma actual...
                    </div>
                  ) : renderFirmaSection()}
                </div>

                {diploma && (diploma.totalEmitidos > 0) && (
                  <div className="alert alert-warning mt-3 mb-0 d-flex align-items-center gap-2" style={{ fontSize: '13px' }}>
                    <i className="bi bi-exclamation-triangle"></i>
                    <span>{t('editDiploma.emissionWarning', { count: diploma.totalEmitidos })}</span>
                  </div>
                )}

                {errors.general && (
                  <div className="alert alert-danger mt-3 mb-0 d-flex align-items-center gap-2" style={{ fontSize: '13px' }}>
                    <i className="bi bi-x-circle"></i>
                    <span>{errors.general}</span>
                  </div>
                )}
              </div>

              {/* ── Previsualización ── */}
              <div
                className="col-12 col-lg-5 p-4 d-flex flex-column"
                style={{ borderLeft: '1px solid #f0f0f0', background: '#f8f9fb' }}
              >
                <div className="d-flex align-items-center gap-2 mb-3">
                  <i className="bi bi-eye text-primary"></i>
                  <h6 className="fw-bold mb-0 text-dark fs-6">Previsualización</h6>
                </div>

                {previewPdfUrl ? (
                  <div className="flex-grow-1 position-relative" style={{ minHeight: '300px' }}>
                    {loadingPreview && (
                      <div className="position-absolute top-0 start-0 w-100 h-100 d-flex align-items-center justify-content-center" style={{ background: 'rgba(248,249,251,0.8)', zIndex: 2, borderRadius: '8px' }}>
                        <div className="spinner-border spinner-border-sm text-primary me-2" role="status"></div>
                        <span className="text-secondary small">Actualizando...</span>
                      </div>
                    )}
                    <iframe
                      src={previewPdfUrl}
                      title="Previsualización del diploma"
                      style={{ width: '100%', height: '100%', minHeight: '340px', border: 'none', borderRadius: '8px' }}
                    />
                  </div>
                ) : loadingPreview ? (
                  <div className="flex-grow-1 d-flex flex-column align-items-center justify-content-center gap-2">
                    <div className="spinner-border spinner-border-sm text-primary" role="status"></div>
                    <span className="text-secondary small">Generando previsualización...</span>
                  </div>
                ) : (
                  <div className="flex-grow-1 d-flex flex-column align-items-center justify-content-center gap-2 border border-dashed rounded-3" style={{ borderColor: '#dee2e6', minHeight: '300px' }}>
                    <i className="bi bi-file-earmark-pdf text-secondary fs-1 opacity-25"></i>
                    <p className="text-secondary mb-0 text-center" style={{ fontSize: '12px' }}>
                      Sin plantilla activa.<br />Sube una para ver la previsualización.
                    </p>
                  </div>
                )}

                <p className="text-secondary text-center mt-3 mb-0" style={{ fontSize: '11px' }}>
                  {previewPdfUrl
                    ? 'Vista previa del diseño real del diploma.'
                    : 'La previsualización aparece al cargar una plantilla JRXML.'}
                </p>
              </div>
            </div>
          </div>

          <div className="modal-footer border-0 px-4 py-3 bg-white rounded-bottom-4 d-flex justify-content-end gap-2">
            <button
              type="button"
              className="btn btn-outline-secondary px-4 fw-semibold border-light-subtle text-dark"
              data-bs-dismiss="modal"
              style={{ fontSize: '13px' }}
            >
              {t('common.cancel')}
            </button>
            <button
              type="button"
              className="btn btn-primary px-4 fw-semibold d-flex align-items-center gap-2"
              style={{ fontSize: '13px' }}
              onClick={handleSubmit}
              disabled={isLoading}
            >
              {isLoading ? (
                <><span className="spinner-border spinner-border-sm me-1" role="status"></span>{t('editDiploma.saving')}</>
              ) : (
                <><i className="bi bi-check2 border border-white rounded-circle px-1" style={{ fontSize: '10px' }}></i>{t('common.save')}</>
              )}
            </button>
          </div>
        </div>
      </div>
    </div>
  )
}

export default EditarDiplomaModal
