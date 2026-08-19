import { apiClient } from '../axiosConfig'

export const authService = {
  getCurrentUser: () => apiClient.get('/api/auth/me'),
  login: (payload: { email: string; password: string }) => apiClient.post('/api/auth/login', payload),
  register: (payload: { name: string; email: string; password: string }) => apiClient.post('/api/auth/register', payload),
  logout: () => apiClient.post('/api/auth/logout'),
}