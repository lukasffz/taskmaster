import { useEffect, useState } from 'react'
import './App.css'
import { apiClient } from './api/axiosConfig'

function App() {
  const [apiStatus, setApiStatus] = useState<'checking' | 'ready' | 'offline'>('checking')

  useEffect(() => {
    apiClient.get('/api/auth/me')
      .then(() => setApiStatus('ready'))
      .catch(() => setApiStatus('offline'))
  }, [])

  return (
    <main className="app-shell">
      <nav className="topbar">
        <span className="brand-mark">TM</span>
        <span className="brand-name">TaskMaster</span>
        <span className={`status-pill ${apiStatus}`}>
          <span className="status-dot" />
          {apiStatus === 'checking' && 'Acordando servidor...'}
          {apiStatus === 'ready' && 'API online'}
          {apiStatus === 'offline' && 'API indisponível'}
        </span>
      </nav>

      <section className="welcome-panel">
        <p className="eyebrow">Workspace de projetos</p>
        <h1>Ideias em movimento.</h1>
        <p className="lead">Um espaço claro para transformar trabalho em progresso visível.</p>
        {apiStatus === 'checking' && (
          <div className="cold-start-note" role="status">
            <span className="loader" />
            <span>Isso pode levar alguns segundos.</span>
          </div>
        )}
        {apiStatus === 'offline' && (
          <p className="offline-note">Não foi possível conectar à API agora. Tente novamente em instantes.</p>
        )}
      </section>

      <section className="feature-grid" aria-label="Status da aplicação">
        <article className="feature-item">
          <span className="feature-index">01</span>
          <h2>Projetos</h2>
          <p>Organize objetivos, contexto e entregas em um único lugar.</p>
        </article>
        <article className="feature-item accent-item">
          <span className="feature-index">02</span>
          <h2>Tarefas</h2>
          <p>Veja o próximo passo e acompanhe cada mudança de status.</p>
        </article>
        <article className="feature-item">
          <span className="feature-index">03</span>
          <h2>Seguro por padrão</h2>
          <p>Sessões protegidas por cookies HttpOnly e autorização no backend.</p>
        </article>
      </section>
    </main>
  )
}

export default App
