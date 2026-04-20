import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { useState, useEffect } from 'react'
import { ToastContainer } from 'react-toastify'
import { authService } from './services/authService'

// Vistas de Autenticación
import Login from './pages/Login'
import Register from './pages/Register'
import ForgotPassword from './pages/ForgotPassword'

// Dashboards Modulares
import AdminDashboard from './pages/admin/AdminDashboard'
import StudentDashboard from './pages/student/StudentDashboard'

function App() {
  // Estado del usuario logueado, sincronizado con authService
  const [loggedUser, setLoggedUser] = useState(() => authService.getCurrentUser())

  // Sincronizar estado cuando cambie el storage
  useEffect(() => {
    const handleStorageChange = () => {
      setLoggedUser(authService.getCurrentUser())
    }
    window.addEventListener('storage', handleStorageChange)
    return () => window.removeEventListener('storage', handleStorageChange)
  }, [])

  const handleLogin = (userData) => {
    setLoggedUser(userData)
  }

  const handleLogout = () => {
    authService.logout()
    setLoggedUser(null)
  }

  return (
    <BrowserRouter>
      <Routes>
        {/* Rutas Públicas (Autenticación) */}
        <Route
          path="/"
          element={loggedUser ? <Navigate to={loggedUser.role === 'ADMIN' ? '/admin' : '/estudiante'} /> : <Navigate to="/login" />}
        />
        <Route
          path="/login"
          element={!loggedUser ? <Login onLogin={handleLogin} /> : <Navigate to={loggedUser.role === 'ADMIN' ? '/admin' : '/estudiante'} />}
        />
        <Route
          path="/register"
          element={!loggedUser ? <Register /> : <Navigate to={loggedUser.role === 'ADMIN' ? '/admin' : '/estudiante'} />}
        />
        <Route
          path="/forgot-password"
          element={!loggedUser ? <ForgotPassword /> : <Navigate to={loggedUser.role === 'ADMIN' ? '/admin' : '/estudiante'} />}
        />

        {/* Rutas Privadas (Admin) */}
        <Route
          path="/admin/*"
          element={
            loggedUser && loggedUser.role === 'ADMIN'
              ? <AdminDashboard loggedUser={loggedUser} onLogout={handleLogout} />
              : <Navigate to="/login" />
          }
        />

        {/* Rutas Privadas (Estudiante) */}
        <Route
          path="/estudiante/*"
          element={
            loggedUser && loggedUser.role === 'STUDENT'
              ? <StudentDashboard loggedUser={loggedUser} onLogout={handleLogout} />
              : <Navigate to="/login" />
          }
        />
      </Routes>

      <ToastContainer
        position="top-right"
        autoClose={3000}
        hideProgressBar={false}
        newestOnTop
        closeOnClick
        pauseOnFocusLoss
        draggable
        pauseOnHover
        theme="colored"
      />
    </BrowserRouter>
  )
}

export default App
