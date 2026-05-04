import { useState, useEffect } from 'react'
import { useParams, Link, useNavigate } from 'react-router-dom'
import { getIncident, submitRca } from '../services/api'

export default function RcaForm() {
  const { id } = useParams()
  const navigate = useNavigate()
  const [incident, setIncident] = useState(null)
  const [loading, setLoading] = useState(true)
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState(null)
  
  const [formData, setFormData] = useState({
    rootCauseCategory: '',
    incidentStartTime: '',
    incidentEndTime: '',
    fixApplied: '',
    preventionSteps: '',
    additionalNotes: ''
  })
  
  useEffect(() => {
    fetchIncident()
  }, [id])
  
  const fetchIncident = async () => {
    try {
      const response = await getIncident(id)
      setIncident(response.data.data)
      
      // Pre-fill times from incident
      if (response.data.data.firstSignalTime) {
        setFormData(prev => ({
          ...prev,
          incidentStartTime: response.data.data.firstSignalTime.slice(0, 16)
        }))
      }
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }
  
  const handleChange = (e) => {
    const { name, value } = e.target
    setFormData(prev => ({ ...prev, [name]: value }))
  }
  
  const handleSubmit = async (e) => {
    e.preventDefault()
    setSubmitting(true)
    setError(null)
    
    try {
      // Convert datetime-local to ISO string
      const rcaData = {
        ...formData,
        incidentStartTime: new Date(formData.incidentStartTime).toISOString(),
        incidentEndTime: new Date(formData.incidentEndTime).toISOString()
      }
      
      await submitRca(id, rcaData)
      navigate(`/incidents/${id}`)
    } catch (err) {
      setError(err.response?.data?.message || err.message)
    } finally {
      setSubmitting(false)
    }
  }
  
  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>
    )
  }
  
  return (
    <div>
      <div className="mb-6">
        <Link to={`/incidents/${id}`} className="text-blue-600 hover:text-blue-900 text-sm">
          ← Back to Incident
        </Link>
        <h2 className="text-2xl font-bold text-gray-900 mt-2">Submit Root Cause Analysis</h2>
        <p className="text-gray-600 mt-1">For incident: {incident?.incidentId}</p>
      </div>
      
      {error && (
        <div className="bg-red-50 border border-red-200 rounded-lg p-4 mb-6">
          <p className="text-red-800">{error}</p>
        </div>
      )}
      
      <form onSubmit={handleSubmit} className="bg-white shadow rounded-lg p-6 space-y-6">
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            Root Cause Category *
          </label>
          <select
            name="rootCauseCategory"
            value={formData.rootCauseCategory}
            onChange={handleChange}
            required
            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          >
            <option value="">Select category</option>
            <option value="HARDWARE">Hardware Failure</option>
            <option value="SOFTWARE">Software Bug</option>
            <option value="CONFIGURATION">Configuration Error</option>
            <option value="NETWORK">Network Issue</option>
            <option value="DATABASE">Database Issue</option>
            <option value="CACHE">Cache Issue</option>
            <option value="THIRD_PARTY">Third Party</option>
            <option value="CAPACITY">Capacity/Load</option>
            <option value="OTHER">Other</option>
          </select>
        </div>
        
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Incident Start Time *
            </label>
            <input
              type="datetime-local"
              name="incidentStartTime"
              value={formData.incidentStartTime}
              onChange={handleChange}
              required
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Incident End Time *
            </label>
            <input
              type="datetime-local"
              name="incidentEndTime"
              value={formData.incidentEndTime}
              onChange={handleChange}
              required
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>
        </div>
        
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            Fix Applied *
          </label>
          <textarea
            name="fixApplied"
            value={formData.fixApplied}
            onChange={handleChange}
            required
            rows={3}
            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            placeholder="Describe the steps taken to resolve the incident..."
          />
        </div>
        
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            Prevention Steps *
          </label>
          <textarea
            name="preventionSteps"
            value={formData.preventionSteps}
            onChange={handleChange}
            required
            rows={3}
            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            placeholder="Describe steps to prevent recurrence..."
          />
        </div>
        
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            Additional Notes
          </label>
          <textarea
            name="additionalNotes"
            value={formData.additionalNotes}
            onChange={handleChange}
            rows={2}
            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            placeholder="Any additional information..."
          />
        </div>
        
        <div className="flex justify-end space-x-3">
          <Link
            to={`/incidents/${id}`}
            className="px-4 py-2 border border-gray-300 rounded-md text-gray-700 hover:bg-gray-50"
          >
            Cancel
          </Link>
          <button
            type="submit"
            disabled={submitting}
            className="px-4 py-2 bg-green-600 text-white rounded-md hover:bg-green-700 disabled:opacity-50"
          >
            {submitting ? 'Submitting...' : 'Submit RCA'}
          </button>
        </div>
      </form>
    </div>
  )
}