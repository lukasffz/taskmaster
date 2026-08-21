import axios from 'axios'

const localApiUrl = typeof window === 'undefined'
  ? 'http://localhost:8080'
  : `http://${window.location.hostname}:8080`

export const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_URL || localApiUrl,
  withCredentials: true,
  timeout: 30000,
  headers: { 'Content-Type': 'application/json' },
})