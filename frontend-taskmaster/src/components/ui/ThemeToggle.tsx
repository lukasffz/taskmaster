import { useEffect, useState } from 'react'

const themeStorageKey = 'taskmaster-theme'

type Theme = 'light' | 'dark'

function getInitialTheme(): Theme {
  const savedTheme = window.localStorage.getItem(themeStorageKey)
  if (savedTheme === 'light' || savedTheme === 'dark') return savedTheme
  return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light'
}

export function ThemeToggle() {
  const [theme, setTheme] = useState<Theme>(() => getInitialTheme())

  useEffect(() => {
    document.documentElement.dataset.theme = theme
    window.localStorage.setItem(themeStorageKey, theme)
  }, [theme])

  const toggleTheme = () => setTheme((currentTheme) => currentTheme === 'dark' ? 'light' : 'dark')
  const nextTheme = theme === 'dark' ? 'claro' : 'escuro'

  return <button className="theme-toggle" type="button" onClick={toggleTheme} aria-label={`Ativar tema ${nextTheme}`} title={`Tema ${nextTheme}`}><span aria-hidden="true">{theme === 'dark' ? '☼' : '◐'}</span><strong>{theme === 'dark' ? 'Claro' : 'Escuro'}</strong></button>
}
