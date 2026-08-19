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
    if (!axios.isAxiosError(error) || !error.config || error.response?.status === 401) {
      return Promise.reject(error)
    }

    const shouldRetry = !error.response && coldStartAttempts < coldStartLimit
    if (!shouldRetry) return Promise.reject(error)

    coldStartAttempts += 1
    await new Promise((resolve) => window.setTimeout(resolve, 1200 * coldStartAttempts))
    return apiClient.request(error.config)
  },
)