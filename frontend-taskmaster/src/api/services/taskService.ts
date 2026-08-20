import { apiClient } from '../axiosConfig'

export const taskService = {
  list: (projectId: number, status?: string) => apiClient.get(`/api/projects/${projectId}/tasks`, { params: { status } }),
  getById: (projectId: number, taskId: number) => apiClient.get(`/api/projects/${projectId}/tasks/${taskId}`),
  create: (projectId: number, payload: { title: string; description?: string; status?: string }) => apiClient.post(`/api/projects/${projectId}/tasks`, payload),
  update: (projectId: number, taskId: number, payload: { title: string; description?: string; status: string }) => apiClient.put(`/api/projects/${projectId}/tasks/${taskId}`, payload),
  remove: (projectId: number, taskId: number) => apiClient.delete(`/api/projects/${projectId}/tasks/${taskId}`),
}