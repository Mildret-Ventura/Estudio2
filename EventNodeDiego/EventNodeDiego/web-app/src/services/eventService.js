import { apiGet, apiPost, apiPut, apiDelete, apiFetch } from './apiHelper'

export const eventService = {
  getEventos: async (nombre, mes, categoriaId, estado) => {
    const params = new URLSearchParams()
    if (nombre) params.append('nombre', nombre)
    if (mes) params.append('mes', mes)
    if (categoriaId) params.append('categoriaId', categoriaId)
    if (estado) params.append('estado', estado)

    const qs = params.toString()
    const data = await apiGet(`/eventos${qs ? `?${qs}` : ''}`, { auth: false })
    return data.mensaje ? [] : data
  },

  getCategorias: () =>
    apiGet('/eventos/categorias', { auth: false }),

  buscarOrganizadores: (nombre = '') => {
    const qs = nombre ? `?nombre=${encodeURIComponent(nombre)}` : ''
    return apiGet(`/eventos/organizadores${qs}`, { auth: false })
  },

  crearOrganizador: (datos) =>
    apiPost('/eventos/organizadores', datos),

  actualizarOrganizador: (id, datos) =>
    apiPut(`/eventos/organizadores/${id}`, datos),

  eliminarOrganizador: (id) =>
    apiDelete(`/eventos/organizadores/${id}`),

  crearEvento: (eventoData) =>
    apiPost('/eventos/crear', eventoData),

  actualizarEvento: (idEvento, eventoData) =>
    apiPut(`/eventos/${idEvento}`, eventoData),

  cancelarEvento: async (idEvento) => {
    const res = await apiFetch(`/eventos/${idEvento}/cancelar`, { method: 'POST' })
    return res.json()
  },

  reactivarEvento: async (idEvento) => {
    const res = await apiFetch(`/eventos/${idEvento}/reactivar`, { method: 'POST' })
    return res.json()
  },

  eliminarEvento: (idEvento) =>
    apiDelete(`/eventos/${idEvento}`),

  getEvento: (idEvento) =>
    apiGet(`/eventos/${idEvento}`, { auth: false }),
}
