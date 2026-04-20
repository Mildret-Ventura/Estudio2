import { apiGet, apiPost } from './apiHelper'

export const precheckinService = {
  inscribirse: (idUsuario, idEvento) =>
    apiPost('/precheckin/inscribirse', { idUsuario, idEvento }),

  cancelarInscripcion: (idUsuario, idEvento) =>
    apiPost('/precheckin/cancelar', { idUsuario, idEvento }),

  listarInscritos: (idEvento) =>
    apiGet(`/precheckin/evento/${idEvento}`),

  listarMisEventos: (idUsuario) =>
    apiGet(`/precheckin/usuario/${idUsuario}`),

  contarInscritos: async (idEvento) => {
    try {
      const data = await apiGet(`/precheckin/evento/${idEvento}/count`)
      return data.totalInscritos || 0
    } catch {
      return 0
    }
  },
}
