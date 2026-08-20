import axios from 'axios'
import { apiClient } from './axiosConfig'

const coldStartLimit = 2
let coldStartAttempts = 0

apiClient.interceptors.response.use(
  (response) => {
    coldStartAttempts = 0
    return response
  },
  async (error: unknown) => {
    if (!axios.isAxiosError(error) || !error.config) {
      return Promise.reject(error)
    }

    if (error.response?.status === 401) {
      window.dispatchEvent(new Event('taskmaster:session-expired'))
      return Promise.reject(error)
    }

    const shouldRetry = !error.response && coldStartAttempts < coldStartLimit
    if (!shouldRetry) return Promise.reject(error)

    coldStartAttempts += 1
    await new Promise((resolve) => window.setTimeout(resolve, 1200 * coldStartAttempts))
    return apiClient.request(error.config)
  },
)