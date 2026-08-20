import { useState, type FormEvent, type ReactNode } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { ThemeToggle } from '../components/ui/ThemeToggle'
import { useAuth } from '../hooks/useAuth'

export function LoginPage() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)

  const submit = async (event: FormEvent) => {
    event.preventDefault(); setError(''); setIsSubmitting(true)
    try {
      await login(email, password)
      navigate((location.state as { from?: string } | null)?.from || '/dashboard', { replace: true })
    } catch { setError('Email ou senha inválidos.') } finally { setIsSubmitting(false) }
  }

  return <AuthForm title="Bem-vindo de volta." subtitle="Entre para continuar seu trabalho." onSubmit={submit} error={error}>{<><label>Email<input type="email" value={email} onChange={(event) => setEmail(event.target.value)} required autoComplete="email" /></label><label>Senha<input type="password" value={password} onChange={(event) => setPassword(event.target.value)} required autoComplete="current-password" /></label><button className="primary-button" disabled={isSubmitting}>{isSubmitting ? 'Entrando...' : 'Entrar'}</button><p className="form-footer">Ainda não tem conta? <Link to="/register">Criar conta</Link></p></>}</AuthForm>
}

function AuthForm({ title, subtitle, onSubmit, error, children }: { title: string; subtitle: string; onSubmit: (event: FormEvent) => void; error: string; children: ReactNode }) {
  return <main className="auth-layout"><div className="auth-theme"><ThemeToggle /></div><div className="auth-copy"><span className="brand-mark">TM</span><p className="eyebrow">TaskMaster / access</p><h1>{title}</h1><p className="lead">{subtitle}</p></div><form className="auth-form" onSubmit={onSubmit}>{error && <p className="form-error" role="alert">{error}</p>}{children}</form></main>
}