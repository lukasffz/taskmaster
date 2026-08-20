import { useNavigate } from 'react-router-dom'
import { FeatureCarousel } from '../components/ui/FeatureCarousel'
import { useAuth } from '../hooks/useAuth'

export function DashboardPage() {
  const { user, logout } = useAuth(); const navigate = useNavigate()
  const handleLogout = async () => { await logout(); navigate('/login', { replace: true }) }
  return <main className="dashboard-layout"><nav className="topbar"><span className="brand-mark">TM</span><span className="brand-name">TaskMaster</span><button className="ghost-button" onClick={handleLogout}>Sair</button></nav><section className="dashboard-hero"><p className="eyebrow">Seu workspace</p><h1>Olá, {user?.name.split(' ')[0]}.</h1><p className="lead">Sua sessão está ativa. O espaço de projetos começa aqui.</p></section><FeatureCarousel /></main>
}