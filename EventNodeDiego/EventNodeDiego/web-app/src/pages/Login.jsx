import LoginHeader from '../components/LoginHeader'
import LoginForm from '../components/LoginForm'

function Login({ onLogin }) {
  return (
    <div className="bg-light min-vh-100 d-flex justify-content-center align-items-center p-3">
      <div className="col-11 col-sm-8 col-md-6 col-lg-5 col-xl-4">
        <div className="card shadow border-0 rounded-4">
          <div className="card-body p-4 p-md-5">
            <LoginHeader />
            <LoginForm onLogin={onLogin} />
          </div>
        </div>
      </div>
    </div>
  )
}

export default Login
