import { Routes, Route } from 'react-router-dom'
import { toast } from 'react-toastify'
import DashboardLayout from '../../components/DashboardLayout'
import Sidebar from '../../components/Sidebar'

// Vistas Internas de Estudiante
import StudentHome from './StudentHome'
import StudentEvents from './StudentEvents'
import StudentMyEvents from './StudentMyEvents'
import StudentEventDetail from './StudentEventDetail'
import StudentEventEnrolled from './StudentEventEnrolled'
import StudentDiplomas from './StudentDiplomas'
import StudentDiplomaDetail from './StudentDiplomaDetail'
import StudentProfile from './StudentProfile'
import CerrarSesionModal from '../../components/modals/CerrarSesionModal'

function StudentDashboard({ loggedUser, onLogout }) {
  const handleLogout = () => {
    toast.info('Sesión cerrada correctamente')
    onLogout()
  }

  const menuItems = [
    { path: '/estudiante', exact: true, label: 'Inicio', icon: 'bi bi-house' },
    { path: '/estudiante/eventos', exact: false, label: 'Eventos', icon: 'bi bi-calendar-event' },
    { path: '/estudiante/diplomas', exact: false, label: 'Diplomas', icon: 'bi bi-award' },
    { path: '/estudiante/perfil', exact: false, label: 'Perfil', icon: 'bi bi-person' }
  ]

  const sidebar = <Sidebar menuItems={menuItems} user={loggedUser} />

  return (
    <DashboardLayout sidebar={sidebar} user={loggedUser}>
      <Routes>
        <Route path="/" element={<StudentHome />} />
        <Route path="/eventos" element={<StudentEvents />} />
        <Route path="/mis-eventos" element={<StudentMyEvents user={loggedUser} />} />
        <Route path="/evento/:id" element={<StudentEventDetail user={loggedUser} />} />
        <Route path="/evento/:id/inscrito" element={<StudentEventEnrolled />} />
        <Route path="/diplomas" element={<StudentDiplomas user={loggedUser} />} />
        <Route path="/diplomas/:id" element={<StudentDiplomaDetail user={loggedUser} />} />
        <Route path="/perfil" element={<StudentProfile user={loggedUser} />} />
      </Routes>
      <CerrarSesionModal onLogout={handleLogout} />
    </DashboardLayout>
  )
}

export default StudentDashboard
