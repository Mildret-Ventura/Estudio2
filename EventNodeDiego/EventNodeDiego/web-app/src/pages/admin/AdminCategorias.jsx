import { useState, useEffect, useRef } from 'react'
import { toast } from 'react-toastify'
import { categoriaService } from '../../services/categoriaService'

function AdminCategorias() {
  const [categorias, setCategorias] = useState([])
  const [loading, setLoading] = useState(true)
  const [searchTerm, setSearchTerm] = useState('')

  // Crear
  const [nombreNueva, setNombreNueva] = useState('')
  const [creando, setCreando] = useState(false)
  const [errorCrear, setErrorCrear] = useState('')
  const crearCloseRef = useRef(null)

  // Editar
  const [categoriaEdit, setCategoriaEdit] = useState(null)
  const [nombreEdit, setNombreEdit] = useState('')
  const [editando, setEditando] = useState(false)
  const [errorEditar, setErrorEditar] = useState('')
  const editCloseRef = useRef(null)

  // Eliminar
  const [categoriaDelete, setCategoriaDelete] = useState(null)
  const [eliminando, setEliminando] = useState(false)
  const deleteCloseRef = useRef(null)

  const fetchCategorias = async () => {
    try {
      const data = await categoriaService.listar()
      setCategorias(data)
    } catch {
      setCategorias([])
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { fetchCategorias() }, [])

  const categoriasFiltradas = categorias.filter(c =>
    c.nombre.toLowerCase().includes(searchTerm.toLowerCase())
  )

  const handleCrear = async () => {
    if (!nombreNueva.trim()) { setErrorCrear('El nombre es requerido'); return }
    setCreando(true)
    setErrorCrear('')
    try {
      await categoriaService.crear(nombreNueva.trim())
      toast.success('Categoría creada exitosamente')
      setNombreNueva('')
      crearCloseRef.current?.click()
      fetchCategorias()
    } catch (err) {
      setErrorCrear(err.message)
    } finally {
      setCreando(false)
    }
  }

  const abrirEditar = (cat) => {
    setCategoriaEdit(cat)
    setNombreEdit(cat.nombre)
    setErrorEditar('')
  }

  const handleEditar = async () => {
    if (!nombreEdit.trim()) { setErrorEditar('El nombre es requerido'); return }
    setEditando(true)
    setErrorEditar('')
    try {
      await categoriaService.actualizar(categoriaEdit.idCategoria, nombreEdit.trim())
      toast.success('Categoría actualizada exitosamente')
      editCloseRef.current?.click()
      setCategoriaEdit(null)
      fetchCategorias()
    } catch (err) {
      setErrorEditar(err.message)
    } finally {
      setEditando(false)
    }
  }

  const handleEliminar = async () => {
    setEliminando(true)
    try {
      await categoriaService.eliminar(categoriaDelete.idCategoria)
      toast.success('Categoría eliminada exitosamente')
      deleteCloseRef.current?.click()
      setCategoriaDelete(null)
      fetchCategorias()
    } catch (err) {
      toast.error(err.message)
    } finally {
      setEliminando(false)
    }
  }

  return (
    <div>
      {/* Header */}
      <div className="d-flex flex-column flex-md-row justify-content-between align-items-start align-items-md-center mb-4 gap-3">
        <div>
          <h2 className="fw-bold mb-1">Categorías</h2>
          <p className="text-secondary small mb-0">Gestiona las categorías de eventos</p>
        </div>
        <button
          className="btn btn-primary rounded-pill d-flex align-items-center gap-2 flex-shrink-0 px-4"
          data-bs-toggle="modal"
          data-bs-target="#crearCategoriaModal"
          onClick={() => { setNombreNueva(''); setErrorCrear('') }}
        >
          <i className="bi bi-plus-lg"></i>
          Nueva Categoría
        </button>
      </div>

      {/* Stats */}
      <div className="row g-3 mb-4">
        <div className="col-12 col-md-4">
          <div className="card border-0 shadow-sm rounded-4 p-3">
            <div className="card-body">
              <div className="bg-primary bg-opacity-10 text-primary rounded px-2 py-1 d-inline-flex mb-3">
                <i className="bi bi-tags"></i>
              </div>
              <div className="text-secondary small mb-1">Total categorías</div>
              <h3 className="fw-bold mb-0">{categorias.length}</h3>
            </div>
          </div>
        </div>
        <div className="col-12 col-md-4">
          <div className="card border-0 shadow-sm rounded-4 p-3">
            <div className="card-body">
              <div className="bg-success bg-opacity-10 text-success rounded px-2 py-1 d-inline-flex mb-3">
                <i className="bi bi-calendar-check"></i>
              </div>
              <div className="text-secondary small mb-1">Con eventos</div>
              <h3 className="fw-bold mb-0">{categorias.filter(c => c.totalEventos > 0).length}</h3>
            </div>
          </div>
        </div>
        <div className="col-12 col-md-4">
          <div className="card border-0 shadow-sm rounded-4 p-3">
            <div className="card-body">
              <div className="bg-warning bg-opacity-10 text-warning rounded px-2 py-1 d-inline-flex mb-3">
                <i className="bi bi-calendar-x"></i>
              </div>
              <div className="text-secondary small mb-1">Sin eventos</div>
              <h3 className="fw-bold mb-0">{categorias.filter(c => c.totalEventos === 0).length}</h3>
            </div>
          </div>
        </div>
      </div>

      {/* Buscador */}
      <div className="card border-0 shadow-sm rounded-4 mb-4">
        <div className="card-body p-3">
          <div className="input-group">
            <span className="input-group-text bg-transparent border-end-0">
              <i className="bi bi-search text-secondary"></i>
            </span>
            <input
              type="text"
              className="form-control border-start-0 ps-0"
              placeholder="Buscar categoría..."
              value={searchTerm}
              onChange={e => setSearchTerm(e.target.value)}
              style={{ fontSize: '13px' }}
            />
            {searchTerm && (
              <button className="btn btn-outline-secondary border-start-0" onClick={() => setSearchTerm('')}>
                <i className="bi bi-x"></i>
              </button>
            )}
          </div>
        </div>
      </div>

      {/* Lista */}
      {loading ? (
        <div className="text-center py-5">
          <div className="spinner-border text-primary" role="status">
            <span className="visually-hidden">Cargando...</span>
          </div>
        </div>
      ) : categoriasFiltradas.length === 0 ? (
        <div className="card border-0 shadow-sm rounded-4">
          <div className="card-body text-center py-5">
            <div className="rounded-circle bg-primary bg-opacity-10 d-inline-flex align-items-center justify-content-center mb-3" style={{ width: '64px', height: '64px' }}>
              <i className="bi bi-tags text-primary fs-3"></i>
            </div>
            <h6 className="fw-bold mb-1">{searchTerm ? 'Sin resultados' : 'No hay categorías'}</h6>
            <p className="text-secondary small mb-0">
              {searchTerm ? 'Intenta con otro término de búsqueda.' : 'Crea la primera categoría con el botón de arriba.'}
            </p>
          </div>
        </div>
      ) : (
        <div className="row g-3">
          {categoriasFiltradas.map(cat => (
            <div className="col-12 col-md-6 col-lg-4" key={cat.idCategoria}>
              <div className="card border-0 shadow-sm rounded-4 h-100 card-hover">
                <div className="card-body p-4">
                  <div className="d-flex align-items-start justify-content-between mb-3">
                    <div className="d-flex align-items-center gap-3">
                      <div className="bg-primary bg-opacity-10 text-primary rounded-circle d-flex align-items-center justify-content-center flex-shrink-0"
                        style={{ width: '44px', height: '44px' }}>
                        <i className="bi bi-tag fs-5"></i>
                      </div>
                      <div>
                        <h6 className="fw-bold mb-0 text-dark">{cat.nombre}</h6>
                        <span className="text-secondary small">ID #{cat.idCategoria}</span>
                      </div>
                    </div>
                  </div>

                  <div className="d-flex justify-content-between align-items-center mb-3">
                    <span className="text-secondary small">Eventos asociados</span>
                    <span className={`badge rounded-pill ${cat.totalEventos > 0 ? 'bg-primary bg-opacity-10 text-primary' : 'bg-secondary bg-opacity-10 text-secondary'}`}
                      style={{ fontSize: '11px' }}>
                      {cat.totalEventos}
                    </span>
                  </div>

                  <div className="d-flex gap-2 mt-auto">
                    <button
                      className="btn btn-outline-primary btn-sm flex-grow-1 rounded-pill fw-semibold"
                      data-bs-toggle="modal"
                      data-bs-target="#editarCategoriaModal"
                      onClick={() => abrirEditar(cat)}
                    >
                      <i className="bi bi-pencil me-1"></i>Editar
                    </button>
                    <button
                      className="btn btn-outline-danger btn-sm flex-grow-1 rounded-pill fw-semibold"
                      data-bs-toggle="modal"
                      data-bs-target="#eliminarCategoriaModal"
                      onClick={() => setCategoriaDelete(cat)}
                      disabled={cat.totalEventos > 0}
                      title={cat.totalEventos > 0 ? 'No se puede eliminar: tiene eventos asociados' : ''}
                    >
                      <i className="bi bi-trash me-1"></i>Eliminar
                    </button>
                  </div>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* ── Modal Crear ── */}
      <div className="modal fade" id="crearCategoriaModal" tabIndex="-1" aria-hidden="true">
        <div className="modal-dialog modal-dialog-centered">
          <div className="modal-content border-0 rounded-4 shadow">
            <div className="modal-header border-0 px-4 pt-4 pb-2">
              <div>
                <h5 className="fw-bold mb-0 text-dark">Nueva Categoría</h5>
                <p className="text-secondary small mb-0">Agrega una nueva categoría de eventos</p>
              </div>
              <button type="button" className="btn-close" data-bs-dismiss="modal" ref={crearCloseRef}></button>
            </div>
            <div className="modal-body px-4 py-3">
              <label className="form-label fw-semibold small text-dark">Nombre de la categoría</label>
              <input
                type="text"
                className={`form-control ${errorCrear ? 'is-invalid' : ''}`}
                placeholder="Ej. TECNOLOGÍA, CULTURA..."
                value={nombreNueva}
                onChange={e => { setNombreNueva(e.target.value); setErrorCrear('') }}
                onKeyDown={e => e.key === 'Enter' && handleCrear()}
                maxLength={100}
                style={{ fontSize: '13px' }}
              />
              {errorCrear && <div className="invalid-feedback">{errorCrear}</div>}
              <p className="text-secondary mt-2 mb-0" style={{ fontSize: '11px' }}>
                El nombre se guardará en mayúsculas automáticamente.
              </p>
            </div>
            <div className="modal-footer border-0 px-4 pb-4 pt-0 gap-2">
              <button type="button" className="btn btn-outline-secondary px-4 fw-semibold" data-bs-dismiss="modal" style={{ fontSize: '13px' }}>
                Cancelar
              </button>
              <button
                type="button"
                className="btn btn-primary px-4 fw-semibold d-flex align-items-center gap-2"
                onClick={handleCrear}
                disabled={creando}
                style={{ fontSize: '13px' }}
              >
                {creando
                  ? <><span className="spinner-border spinner-border-sm me-1" role="status"></span>Creando...</>
                  : <><i className="bi bi-check2"></i>Crear categoría</>}
              </button>
            </div>
          </div>
        </div>
      </div>

      {/* ── Modal Editar ── */}
      <div className="modal fade" id="editarCategoriaModal" tabIndex="-1" aria-hidden="true">
        <div className="modal-dialog modal-dialog-centered">
          <div className="modal-content border-0 rounded-4 shadow">
            <div className="modal-header border-0 px-4 pt-4 pb-2">
              <div>
                <h5 className="fw-bold mb-0 text-dark">Editar Categoría</h5>
                <p className="text-secondary small mb-0">Modifica el nombre de la categoría</p>
              </div>
              <button type="button" className="btn-close" data-bs-dismiss="modal" ref={editCloseRef}></button>
            </div>
            <div className="modal-body px-4 py-3">
              <label className="form-label fw-semibold small text-dark">Nombre de la categoría</label>
              <input
                type="text"
                className={`form-control ${errorEditar ? 'is-invalid' : ''}`}
                value={nombreEdit}
                onChange={e => { setNombreEdit(e.target.value); setErrorEditar('') }}
                onKeyDown={e => e.key === 'Enter' && handleEditar()}
                maxLength={100}
                style={{ fontSize: '13px' }}
              />
              {errorEditar && <div className="invalid-feedback">{errorEditar}</div>}
            </div>
            <div className="modal-footer border-0 px-4 pb-4 pt-0 gap-2">
              <button type="button" className="btn btn-outline-secondary px-4 fw-semibold" data-bs-dismiss="modal" style={{ fontSize: '13px' }}>
                Cancelar
              </button>
              <button
                type="button"
                className="btn btn-primary px-4 fw-semibold d-flex align-items-center gap-2"
                onClick={handleEditar}
                disabled={editando}
                style={{ fontSize: '13px' }}
              >
                {editando
                  ? <><span className="spinner-border spinner-border-sm me-1" role="status"></span>Guardando...</>
                  : <><i className="bi bi-check2"></i>Guardar cambios</>}
              </button>
            </div>
          </div>
        </div>
      </div>

      {/* ── Modal Eliminar ── */}
      <div className="modal fade" id="eliminarCategoriaModal" tabIndex="-1" aria-hidden="true">
        <div className="modal-dialog modal-dialog-centered">
          <div className="modal-content border-0 rounded-4 shadow">
            <div className="modal-header border-0 px-4 pt-4 pb-2">
              <h5 className="fw-bold mb-0 text-dark">Eliminar Categoría</h5>
              <button type="button" className="btn-close" data-bs-dismiss="modal" ref={deleteCloseRef}></button>
            </div>
            <div className="modal-body px-4 py-3">
              <div className="d-flex align-items-start gap-3">
                <div className="bg-danger bg-opacity-10 rounded-circle d-flex align-items-center justify-content-center flex-shrink-0" style={{ width: '44px', height: '44px' }}>
                  <i className="bi bi-exclamation-triangle text-danger fs-5"></i>
                </div>
                <div>
                  <p className="mb-1 fw-semibold text-dark">¿Eliminar "{categoriaDelete?.nombre}"?</p>
                  <p className="text-secondary small mb-0">
                    Esta acción no se puede deshacer. Solo se pueden eliminar categorías sin eventos asociados.
                  </p>
                </div>
              </div>
            </div>
            <div className="modal-footer border-0 px-4 pb-4 pt-0 gap-2">
              <button type="button" className="btn btn-outline-secondary px-4 fw-semibold" data-bs-dismiss="modal" style={{ fontSize: '13px' }}>
                Cancelar
              </button>
              <button
                type="button"
                className="btn btn-danger px-4 fw-semibold d-flex align-items-center gap-2"
                onClick={handleEliminar}
                disabled={eliminando}
                style={{ fontSize: '13px' }}
              >
                {eliminando
                  ? <><span className="spinner-border spinner-border-sm me-1" role="status"></span>Eliminando...</>
                  : <><i className="bi bi-trash me-1"></i>Eliminar</>}
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}

export default AdminCategorias
