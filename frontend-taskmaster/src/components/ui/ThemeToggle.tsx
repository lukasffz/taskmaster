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

  const toggleTheme = () => setTheme((currentTheme) => (currentTheme === 'dark' ? 'light' : 'dark'))
  const isDark = theme === 'dark'
  const currentLabel = isDark ? 'Escuro' : 'Claro'
  const nextLabel = isDark ? 'Claro' : 'Escuro'

  return (
    <button
      className="theme-toggle"
      type="button"
      onClick={toggleTheme}
      aria-label={`Tema atual: ${currentLabel}. Clique para mudar para ${nextLabel}`}
      title={`Mudar para tema ${nextLabel}`}
    >
      <span aria-hidden="true">{isDark ? '◐' : '☼'}</span>
      <strong>{currentLabel}</strong>
    </button>
  )
}
