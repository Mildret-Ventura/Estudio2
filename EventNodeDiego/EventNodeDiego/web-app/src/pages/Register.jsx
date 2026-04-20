import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { toast } from 'react-toastify'
import { authService } from '../services/authService'
import RegisterHeader from '../components/RegisterHeader'
import RegisterForm from '../components/RegisterForm'

function Register() {
  const [age, setAge] = useState('')
  const [error, setError] = useState(null)
  const [success, setSuccess] = useState(false)
  const [isLoading, setIsLoading] = useState(false)
  const navigate = useNavigate()

  const [confirmPassword, setConfirmPassword] = useState('')

  const [formData, setFormData] = useState({
    nombre: '',
    apellidoPaterno: '',
    apellidoMaterno: '',
    matricula: '',
    correo: '',
    password: '',
    fechaNacimiento: '2008-01-01',
    sexo: '',
    cuatrimestre: ''
  })

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value
    })
  }

  const handleBirthDateChange = (e) => {
    handleChange(e)
    const birthDate = new Date(e.target.value)
    const today = new Date()
    let calculatedAge = today.getFullYear() - birthDate.getFullYear()
    const monthDiff = today.getMonth() - birthDate.getMonth()
    if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birthDate.getDate())) {
      calculatedAge--
    }
    setAge(calculatedAge >= 0 ? calculatedAge : '')
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError(null)
    setSuccess(false)

    if (formData.password !== confirmPassword) {
      setError('Las contraseñas no coinciden')
      return
    }

    setIsLoading(true)

    const submitData = {
      ...formData,
      cuatrimestre: formData.cuatrimestre ? parseInt(formData.cuatrimestre, 10) : null
    }

    try {
      await authService.register(submitData)
      setSuccess(true)
      toast.success('¡Cuenta creada con éxito! Redirigiendo al login...')
      setTimeout(() => navigate('/login'), 2500)
    } catch (err) {
      setError(err.message)
      toast.error(err.message)
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <div className="bg-light min-vh-100 d-flex justify-content-center align-items-start align-items-md-center p-3">
      <div className="col-11 col-sm-10 col-md-7 col-lg-6 col-xl-5">
        <div className="card shadow border-0 rounded-4">
          <div className="card-body p-4 p-md-5">
            <RegisterHeader />
            <RegisterForm
              formData={formData}
              confirmPassword={confirmPassword}
              onConfirmPasswordChange={(e) => setConfirmPassword(e.target.value)}
              age={age}
              error={error}
              success={success}
              isLoading={isLoading}
              onChange={handleChange}
              onBirthDateChange={handleBirthDateChange}
              onSubmit={handleSubmit}
            />
          </div>
        </div>
      </div>
    </div>
  )
}

export default Register
