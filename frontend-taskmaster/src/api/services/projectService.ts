import { apiClient } from '../axiosConfig'

export const projectService = {
  list: () => apiClient.get('/api/projects'),
  getById: (projectId: number) => apiClient.get(`/api/projects/${projectId}`),
  create: (payload: { name: string; description?: string }) => apiClient.post('/api/projects', payload),
  update: (projectId: number, payload: { name: string; description?: string }) => apiClient.put(`/api/projects/${projectId}`, payload),
  remove: (projectId: number) => apiClient.delete(`/api/projects/${projectId}`),
}