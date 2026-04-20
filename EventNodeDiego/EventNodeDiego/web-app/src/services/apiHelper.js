import { authService } from './authService'

const API_URL = '/api'

/**
 * Wrapper para fetch con manejo de errores centralizado.
 * Elimina la repetición de try/parse-error/throw en cada servicio.
 */
export async function apiFetch(endpoint, options = {}) {
  const config = { ...options }

  // Agregar auth header si se requiere
  if (options.auth !== false) {
    config.headers = {
      ...config.headers,
      ...authService.getAuthHeader(),
    }
  }

  // Agregar Content-Type JSON si hay body
  if (config.body && typeof config.body === 'string') {
    config.headers = {
      'Content-Type': 'application/json',
      ...config.headers,
    }
  }

  // Limpiar props custom que no son de fetch
  delete config.auth
  delete config.errorMsg

  const response = await fetch(`${API_URL}${endpoint}`, config)

  if (!response.ok) {
    const errorData = await response.json().catch(() => ({}))
    throw new Error(errorData.mensaje || options.errorMsg || 'Error en la solicitud')
  }

  return response
}

/** fetch + parse JSON */
export async function apiGet(endpoint, options = {}) {
  const res = await apiFetch(endpoint, options)
  return res.json()
}

/** POST con body JSON + parse respuesta */
export async function apiPost(endpoint, body, options = {}) {
  const res = await apiFetch(endpoint, {
    method: 'POST',
    body: JSON.stringify(body),
    ...options,
  })
  return res.json()
}

/** PUT con body JSON + parse respuesta */
export async function apiPut(endpoint, body, options = {}) {
  const res = await apiFetch(endpoint, {
    method: 'PUT',
    body: JSON.stringify(body),
    ...options,
  })
  return res.json()
}

/** PATCH con body JSON + parse respuesta */
export async function apiPatch(endpoint, body, options = {}) {
  const res = await apiFetch(endpoint, {
    method: 'PATCH',
    body: JSON.stringify(body),
    ...options,
  })
  return res.json()
}

/** DELETE + parse respuesta */
export async function apiDelete(endpoint, options = {}) {
  const res = await apiFetch(endpoint, { method: 'DELETE', ...options })
  return res.json()
}

/**
 * Cierra un modal de Bootstrap por su ID.
 * Centraliza el patrón repetido en 6+ páginas admin.
 */
export function closeModal(modalId) {
  const el = document.getElementById(modalId)
  if (el && window.bootstrap) {
    window.bootstrap.Modal.getOrCreateInstance(el).hide()
  }
}
