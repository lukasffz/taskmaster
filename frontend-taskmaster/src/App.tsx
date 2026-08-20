import { Navigate, Route, Routes } from 'react-router-dom'
import './App.css'
import { DashboardPage } from './pages/DashboardPage'
import { LoginPage } from './pages/LoginPage'
import { RegisterPage } from './pages/RegisterPage'
import { PrivateRoute } from './routes/PrivateRoute'
import { PublicRoute } from './routes/PublicRoute'

function App() {
  return <Routes><Route element={<PublicRoute />}><Route path="/login" element={<LoginPage />} /><Route path="/register" element={<RegisterPage />} /></Route><Route element={<PrivateRoute />}><Route path="/dashboard" element={<DashboardPage />} /></Route><Route path="*" element={<Navigate to="/dashboard" replace />} /></Routes>
}

export default App
