import { Routes, Route } from 'react-router-dom'
import { toast } from 'react-toastify'
import DashboardLayout from '../../components/DashboardLayout'
import Sidebar from '../../components/Sidebar'

// Vistas Internas de Admin
import AdminHome from './AdminHome'
import GestionEventos from './GestionEventos'
import AdminEventDetail from './AdminEventDetail'
import AdminPreCheckIn from './AdminPreCheckIn'
import AdminCheckIn from './AdminCheckIn'
import AdminDiplomas from './AdminDiplomas'
import AdminDiplomaDetail from './AdminDiplomaDetail'
import AdminEstudiantes from './AdminEstudiantes'
import AdminPerfil from './AdminPerfil'
import AdminOrganizadores from './AdminOrganizadores'
import AdminAnalytics from './AdminAnalytics'
import AdminCategorias from './AdminCategorias'
import CerrarSesionModal from '../../components/modals/CerrarSesionModal'

function AdminDashboard({ loggedUser, onLogout }) {
  const handleLogout = () => {
    toast.info('Sesión cerrada correctamente')
    onLogout()
  }
  const menuItems = [
    { path: '/admin', exact: true, label: 'Inicio', icon: 'bi bi-house' },
    { path: '/admin/eventos', exact: false, label: 'Eventos', icon: 'bi bi-calendar-event' },
    { path: '/admin/diplomas', exact: false, label: 'Diplomas', icon: 'bi bi-award' },
    { path: '/admin/categorias', exact: false, label: 'Categorías', icon: 'bi bi-tags' },
    { path: '/admin/organizadores', exact: false, label: 'Organizadores', icon: 'bi bi-building' },
    { path: '/admin/usuarios', exact: false, label: 'Usuarios', icon: 'bi bi-people' },
    { path: '/admin/analiticas', exact: false, label: 'Analíticas', icon: 'bi bi-graph-up' },
    { path: '/admin/perfil', exact: false, label: 'Perfil', icon: 'bi bi-person' }
  ]

  const sidebar = <Sidebar menuItems={menuItems} user={loggedUser} />

  return (
    <DashboardLayout sidebar={sidebar} user={loggedUser}>
      <Routes>
        <Route path="/" element={<AdminHome />} />
        <Route path="/eventos" element={<GestionEventos user={loggedUser} />} />
        <Route path="/evento/:id" element={<AdminEventDetail />} />
        <Route path="/evento/:id/pre-check-in" element={<AdminPreCheckIn />} />
        <Route path="/evento/:id/check-in" element={<AdminCheckIn />} />
        <Route path="/diplomas" element={<AdminDiplomas />} />
        <Route path="/diploma/:id" element={<AdminDiplomaDetail />} />
        <Route path="/categorias" element={<AdminCategorias />} />
        <Route path="/organizadores" element={<AdminOrganizadores />} />
        <Route path="/usuarios" element={<AdminEstudiantes user={loggedUser} />} />
        <Route path="/analiticas" element={<AdminAnalytics />} />
        <Route path="/perfil" element={<AdminPerfil user={loggedUser} />} />
      </Routes>
      <CerrarSesionModal onLogout={handleLogout} />
    </DashboardLayout>
  )
}

export default AdminDashboard
