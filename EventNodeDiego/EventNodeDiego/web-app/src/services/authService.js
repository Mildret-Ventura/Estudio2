const API_URL = '/api';

const rolMap = {
  'ALUMNO': 'STUDENT',
  'ADMINISTRADOR': 'ADMIN',
  'SUPERADMIN': 'ADMIN',
};

export const authService = {
  login: async (correo, password, rememberMe = false) => {
    const response = await fetch(`${API_URL}/auth/login`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ correo, password }),
    });

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}));
      throw new Error(errorData.mensaje || 'Error al iniciar sesión');
    }

    const data = await response.json();

    // Backend devuelve: { mensaje, rol, idUsuario, nombre, ..., token }
    const userData = {
      id: data.idUsuario,
      name: data.nombre || 'Usuario',
      role: rolMap[data.rol] || 'STUDENT',
      originalRole: data.rol,
      email: data.correo || correo,
      apellidoPaterno: data.apellidoPaterno,
      apellidoMaterno: data.apellidoMaterno,
      matricula: data.matricula,
      sexo: data.sexo,
      cuatrimestre: data.cuatrimestre,
    };

    // Almacenar según preferencia
    const storage = rememberMe ? localStorage : sessionStorage;
    storage.setItem('auth_user', JSON.stringify(userData));
    storage.setItem('auth_token', data.token);

    // Limpiar el otro storage por si acaso quedaron residuos
    const otherStorage = rememberMe ? sessionStorage : localStorage;
    otherStorage.removeItem('auth_user');
    otherStorage.removeItem('auth_token');

    return userData;
  },

  register: async (alumnoData) => {
    const response = await fetch(`${API_URL}/alumnos/registro`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(alumnoData),
    });

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}));
      throw new Error(errorData.mensaje || 'Error al registrar la cuenta');
    }

    return response.json();
  },

  logout: () => {
    localStorage.removeItem('auth_user');
    localStorage.removeItem('auth_token');
    sessionStorage.removeItem('auth_user');
    sessionStorage.removeItem('auth_token');
  },

  getAuthHeader: () => {
    const token = localStorage.getItem('auth_token') || sessionStorage.getItem('auth_token');
    return token ? { 'Authorization': `Bearer ${token}` } : {};
  },

  getCurrentUser: () => {
    const userStr = localStorage.getItem('auth_user') || sessionStorage.getItem('auth_user');
    return userStr ? JSON.parse(userStr) : null;
  },

  enviarCodigoRecuperacion: async (correo) => {
    const response = await fetch(`${API_URL}/auth/recuperar/enviar-codigo`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ correo }),
    });
    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}));
      throw new Error(errorData.mensaje || 'Error al enviar el código');
    }
    return response.json();
  },

  verificarCodigo: async (correo, codigo) => {
    const response = await fetch(`${API_URL}/auth/recuperar/verificar-codigo`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ correo, codigo }),
    });
    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}));
      throw new Error(errorData.mensaje || 'Error al verificar el código');
    }
    return response.json();
  },

  restablecerPassword: async (correo, codigo, nuevaPassword) => {
    const response = await fetch(`${API_URL}/auth/recuperar/restablecer`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ correo, codigo, nuevaPassword }),
    });
    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}));
      throw new Error(errorData.mensaje || 'Error al restablecer la contraseña');
    }
    return response.json();
  },
};
