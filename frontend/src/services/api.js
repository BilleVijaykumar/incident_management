import axios from 'axios'

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8081'

const api = axios.create({
  baseURL: `${API_URL}/api`,
  headers: {
    'Content-Type': 'application/json',
  },
})

// Incidents
export const getActiveIncidents = () => api.get('/incidents/active')
export const getIncident = (id) => api.get(`/incidents/${id}`)
export const getIncidentByIncidentId = (incidentId) => api.get(`/incidents/by-incident-id/${incidentId}`)
export const updateIncidentStatus = (id, status) => api.patch(`/incidents/${id}/status`, { newStatus: status })
export const getIncidentSignals = (id) => api.get(`/incidents/${id}/signals`)

// RCA
export const getRca = (id) => api.get(`/incidents/${id}/rca`)
export const submitRca = (id, rcaData) => api.post(`/incidents/${id}/rca`, rcaData)

// Health
export const getHealth = () => api.get('/health')

export default api