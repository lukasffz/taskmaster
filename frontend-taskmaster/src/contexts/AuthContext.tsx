import { useEffect, useState, type ReactNode } from 'react'
import { authService } from '../api/services/authService'
import type { User } from '../types/auth'
import { AuthContext } from './AuthContextValue'

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null)
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    const loadSession = async () => {
      try {
        const response = await authService.getCurrentUser()
        setUser(response.data)
      } catch {
        setUser(null)
      } finally {
        setIsLoading(false)
      }
    }

    const handleSessionExpired = () => setUser(null)
    window.addEventListener('taskmaster:session-expired', handleSessionExpired)
    void loadSession()
    return () => window.removeEventListener('taskmaster:session-expired', handleSessionExpired)
  }, [])

  const login = async (email: string, password: string) => {
    const response = await authService.login({ email, password })
    setUser(response.data)
  }

  const register = async (name: string, email: string, password: string) => {
    const response = await authService.register({ name, email, password })
    setUser(response.data)
  }

  const logout = async () => {
    try {
      await authService.logout()
    } finally {
      setUser(null)
    }
  }

  return <AuthContext.Provider value={{ user, isLoading, isAuthenticated: user !== null, login, register, logout }}>{children}</AuthContext.Provider>
}