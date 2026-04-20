import { apiGet, apiPost, apiPut, apiDelete, apiFetch } from './apiHelper'

export const diplomaService = {
  crearDiploma: (datos) =>
    apiPost('/diplomas/crear', datos),

  listarDiplomas: () =>
    apiGet('/diplomas/'),

  obtenerDiploma: (idDiploma) =>
    apiGet(`/diplomas/${idDiploma}`),

  emitirDiplomas: async (idDiploma) => {
    const res = await apiFetch(`/diplomas/${idDiploma}/emitir`, { method: 'POST' })
    return res.json()
  },

  descargarDiploma: async (idDiploma, idUsuario) => {
    const res = await apiFetch(`/diplomas/${idDiploma}/descargar/${idUsuario}`, {
      errorMsg: 'Error al descargar diploma',
    })
    return res.blob()
  },

  actualizarDiploma: (idDiploma, datos) =>
    apiPut(`/diplomas/${idDiploma}`, datos),

  eliminarDiploma: (idDiploma) =>
    apiDelete(`/diplomas/${idDiploma}`),

  listarDiplomasEstudiante: (idUsuario) =>
    apiGet(`/diplomas/estudiante/${idUsuario}`),

  previewTemplate: async (datos) => {
    const res = await apiFetch('/diplomas/preview-template', {
      method: 'POST',
      body: JSON.stringify(datos),
    })
    return res.blob()
  },

  previewDiploma: async (idDiploma) => {
    const res = await apiFetch(`/diplomas/${idDiploma}/preview`)
    return res.blob()
  },
}
