import { apiGet, apiPost, apiPut, apiDelete } from './apiHelper'

export const categoriaService = {
  listar: () => apiGet('/categorias'),
  crear: (nombre) => apiPost('/categorias', { nombre }),
  actualizar: (id, nombre) => apiPut(`/categorias/${id}`, { nombre }),
  eliminar: (id) => apiDelete(`/categorias/${id}`),
}
