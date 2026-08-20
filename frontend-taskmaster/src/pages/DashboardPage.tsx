import { useEffect, useMemo, useState, type FormEvent, type ReactNode } from 'react'
import { useNavigate } from 'react-router-dom'
import { projectService } from '../api/services/projectService'
import { taskService } from '../api/services/taskService'
import { FeatureCarousel } from '../components/ui/FeatureCarousel'
import { useAuth } from '../hooks/useAuth'

type Project = { id: number; name: string; description?: string }
type TaskStatus = 'TODO' | 'IN_PROGRESS' | 'DONE'
type Task = { id: number; title: string; description?: string; status: TaskStatus }

const statusLabels: Record<TaskStatus, string> = { TODO: 'A fazer', IN_PROGRESS: 'Em andamento', DONE: 'Concluída' }

export function DashboardPage() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const [projects, setProjects] = useState<Project[]>([])
  const [selectedProjectId, setSelectedProjectId] = useState<number | null>(null)
  const [tasks, setTasks] = useState<Task[]>([])
  const [taskFilter, setTaskFilter] = useState<'ALL' | TaskStatus>('ALL')
  const [isLoading, setIsLoading] = useState(true)
  const [isTasksLoading, setIsTasksLoading] = useState(false)
  const [feedback, setFeedback] = useState('')
  const [error, setError] = useState('')
  const [showProjectForm, setShowProjectForm] = useState(false)
  const [editingProject, setEditingProject] = useState<Project | null>(null)
  const [showTaskForm, setShowTaskForm] = useState(false)
  const [editingTask, setEditingTask] = useState<Task | null>(null)

  const selectedProject = projects.find((project) => project.id === selectedProjectId) ?? null
  const visibleTasks = useMemo(() => taskFilter === 'ALL' ? tasks : tasks.filter((task) => task.status === taskFilter), [taskFilter, tasks])

  useEffect(() => {
    const loadProjects = async () => {
      try {
        const response = await projectService.list()
        const loadedProjects = response.data as Project[]
        setProjects(loadedProjects)
        setSelectedProjectId(loadedProjects[0]?.id ?? null)
      } catch {
        setError('Não foi possível carregar seus projetos.')
      } finally {
        setIsLoading(false)
      }
    }
    void loadProjects()
  }, [])

  useEffect(() => {
    if (selectedProjectId === null) return
    const loadTasks = async () => {
      setIsTasksLoading(true)
      try {
        const response = await taskService.list(selectedProjectId)
        setTasks(response.data as Task[])
      } catch {
        setError('Não foi possível carregar as tarefas.')
      } finally {
        setIsTasksLoading(false)
      }
    }
    void loadTasks()
  }, [selectedProjectId])

  const notify = (message: string) => {
    setFeedback(message)
    window.setTimeout(() => setFeedback(''), 2800)
  }

  const saveProject = async (name: string, description: string) => {
    try {
      if (editingProject) {
        const response = await projectService.update(editingProject.id, { name, description })
        setProjects((current) => current.map((project) => project.id === editingProject.id ? response.data : project))
        notify('Projeto atualizado.')
      } else {
        const response = await projectService.create({ name, description })
        setProjects((current) => [...current, response.data])
        setSelectedProjectId(response.data.id)
        notify('Projeto criado.')
      }
      setShowProjectForm(false)
      setEditingProject(null)
      setError('')
    } catch {
      setError('Não foi possível salvar o projeto.')
    }
  }

  const removeProject = async (project: Project) => {
    if (!window.confirm(`Excluir o projeto "${project.name}"?`)) return
    try {
      await projectService.remove(project.id)
      const remaining = projects.filter((item) => item.id !== project.id)
      setProjects(remaining)
      setSelectedProjectId(remaining[0]?.id ?? null)
      notify('Projeto excluído.')
    } catch {
      setError('Não foi possível excluir o projeto.')
    }
  }

  const saveTask = async (title: string, description: string, status: TaskStatus) => {
    if (selectedProjectId === null) return
    try {
      if (editingTask) {
        const response = await taskService.update(selectedProjectId, editingTask.id, { title, description, status })
        setTasks((current) => current.map((task) => task.id === editingTask.id ? response.data : task))
        notify('Tarefa atualizada.')
      } else {
        const response = await taskService.create(selectedProjectId, { title, description, status })
        setTasks((current) => [...current, response.data])
        notify('Tarefa criada.')
      }
      setShowTaskForm(false)
      setEditingTask(null)
      setError('')
    } catch {
      setError('Não foi possível salvar a tarefa.')
    }
  }

  const removeTask = async (task: Task) => {
    if (selectedProjectId === null || !window.confirm(`Excluir a tarefa "${task.title}"?`)) return
    try {
      await taskService.remove(selectedProjectId, task.id)
      setTasks((current) => current.filter((item) => item.id !== task.id))
      notify('Tarefa excluída.')
    } catch {
      setError('Não foi possível excluir a tarefa.')
    }
  }

  const handleLogout = async () => {
    await logout()
    navigate('/login', { replace: true })
  }

  return <main className="dashboard-layout">
    <nav className="topbar"><span className="brand-mark">TM</span><span className="brand-name">TaskMaster</span><span className="user-label">{user?.name}</span><button className="ghost-button" onClick={handleLogout}>Sair</button></nav>
    <section className="dashboard-hero"><p className="eyebrow">Seu workspace</p><h1>Olá, {user?.name.split(' ')[0]}.</h1><p className="lead">Transforme intenção em progresso visível.</p></section>
    <FeatureCarousel />
    {feedback && <div className="toast" role="status">{feedback}</div>}
    {error && <div className="workspace-error" role="alert">{error}<button onClick={() => setError('')} aria-label="Fechar erro">×</button></div>}
    <section className="workspace-grid">
      <aside className="project-panel"><div className="section-heading"><div><p className="eyebrow">Navegação</p><h2>Projetos</h2></div><button className="icon-button" onClick={() => { setEditingProject(null); setShowProjectForm(true) }} aria-label="Criar projeto">+</button></div>{isLoading ? <p className="muted">Carregando projetos...</p> : projects.length === 0 ? <div className="empty-state"><p>Nenhum projeto ainda.</p><button className="text-button" onClick={() => setShowProjectForm(true)}>Criar o primeiro</button></div> : <div className="project-list">{projects.map((project) => <button key={project.id} className={`project-row ${selectedProjectId === project.id ? 'selected' : ''}`} onClick={() => setSelectedProjectId(project.id)}><span>{project.name}</span><small>{project.description || 'Sem descrição'}</small></button>)}</div>}</aside>
      <section className="task-panel">{selectedProject ? <><div className="section-heading"><div><p className="eyebrow">Projeto selecionado</p><h2>{selectedProject.name}</h2></div><div className="heading-actions"><button className="ghost-button" onClick={() => { setEditingProject(selectedProject); setShowProjectForm(true) }}>Editar</button><button className="danger-button" onClick={() => void removeProject(selectedProject)}>Excluir</button><button className="primary-button compact" onClick={() => { setEditingTask(null); setShowTaskForm(true) }}>Nova tarefa</button></div></div><div className="task-toolbar"><div className="filter-group">{(['ALL', 'TODO', 'IN_PROGRESS', 'DONE'] as const).map((status) => <button key={status} className={taskFilter === status ? 'active' : ''} onClick={() => setTaskFilter(status)}>{status === 'ALL' ? 'Todas' : statusLabels[status]}</button>)}</div><span className="task-count">{visibleTasks.length} tarefa{visibleTasks.length === 1 ? '' : 's'}</span></div>{isTasksLoading ? <p className="muted">Carregando tarefas...</p> : visibleTasks.length === 0 ? <div className="empty-state large"><p>Nenhuma tarefa neste filtro.</p><button className="text-button" onClick={() => setShowTaskForm(true)}>Adicionar tarefa</button></div> : <div className="task-list">{visibleTasks.map((task) => <article className="task-card" key={task.id}><div className="task-status"><span className={`status-badge ${task.status.toLowerCase()}`}>{statusLabels[task.status]}</span><div className="card-actions"><button onClick={() => { setEditingTask(task); setShowTaskForm(true) }}>Editar</button><button onClick={() => void removeTask(task)}>Excluir</button></div></div><h3>{task.title}</h3>{task.description && <p>{task.description}</p>}</article>)}</div>}</> : <div className="empty-workspace"><p className="eyebrow">Comece por aqui</p><h2>Escolha um projeto.</h2><p>Crie um projeto para organizar suas primeiras tarefas.</p><button className="primary-button" onClick={() => setShowProjectForm(true)}>Criar projeto</button></div>}</section>
    </section>
    {showProjectForm && <ProjectForm project={editingProject} onClose={() => { setShowProjectForm(false); setEditingProject(null) }} onSave={saveProject} />}
    {showTaskForm && <TaskForm task={editingTask} onClose={() => { setShowTaskForm(false); setEditingTask(null) }} onSave={saveTask} />}
  </main>
}

function ProjectForm({ project, onClose, onSave }: { project: Project | null; onClose: () => void; onSave: (name: string, description: string) => Promise<void> }) {
  const [name, setName] = useState(project?.name ?? '')
  const [description, setDescription] = useState(project?.description ?? '')
  const [isSaving, setIsSaving] = useState(false)
  const submit = async (event: FormEvent) => { event.preventDefault(); setIsSaving(true); await onSave(name, description); setIsSaving(false) }
  return <Modal title={project ? 'Editar projeto' : 'Novo projeto'} onClose={onClose}><form className="modal-form" onSubmit={submit}><label>Nome<input value={name} onChange={(event) => setName(event.target.value)} required maxLength={200} autoFocus /></label><label>Descrição<textarea value={description} onChange={(event) => setDescription(event.target.value)} maxLength={1000} rows={4} /></label><button className="primary-button" disabled={isSaving}>{isSaving ? 'Salvando...' : 'Salvar projeto'}</button></form></Modal>
}

function TaskForm({ task, onClose, onSave }: { task: Task | null; onClose: () => void; onSave: (title: string, description: string, status: TaskStatus) => Promise<void> }) {
  const [title, setTitle] = useState(task?.title ?? '')
  const [description, setDescription] = useState(task?.description ?? '')
  const [status, setStatus] = useState<TaskStatus>(task?.status ?? 'TODO')
  const [isSaving, setIsSaving] = useState(false)
  const submit = async (event: FormEvent) => { event.preventDefault(); setIsSaving(true); await onSave(title, description, status); setIsSaving(false) }
  return <Modal title={task ? 'Editar tarefa' : 'Nova tarefa'} onClose={onClose}><form className="modal-form" onSubmit={submit}><label>Título<input value={title} onChange={(event) => setTitle(event.target.value)} required maxLength={300} autoFocus /></label><label>Descrição<textarea value={description} onChange={(event) => setDescription(event.target.value)} maxLength={2000} rows={4} /></label><label>Status<select value={status} onChange={(event) => setStatus(event.target.value as TaskStatus)}>{Object.entries(statusLabels).map(([value, label]) => <option key={value} value={value}>{label}</option>)}</select></label><button className="primary-button" disabled={isSaving}>{isSaving ? 'Salvando...' : 'Salvar tarefa'}</button></form></Modal>
}

function Modal({ title, onClose, children }: { title: string; onClose: () => void; children: ReactNode }) { return <div className="modal-backdrop" role="presentation" onMouseDown={(event) => { if (event.currentTarget === event.target) onClose() }}><section className="modal" role="dialog" aria-modal="true" aria-labelledby="modal-title"><div className="modal-heading"><h2 id="modal-title">{title}</h2><button className="icon-button" onClick={onClose} aria-label="Fechar">×</button></div>{children}</section></div> }
