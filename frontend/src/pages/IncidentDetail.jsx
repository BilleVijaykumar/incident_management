import { useState, useEffect } from 'react'
import { useParams, Link, useNavigate } from 'react-router-dom'
import { getIncident, getIncidentSignals, updateIncidentStatus } from '../services/api'
import { format } from 'date-fns'

function SeverityBadge({ severity }) {
  const colors = {
    P0: 'severity-p0',
    P1: 'severity-p1',
    P2: 'severity-p2',
    P3: 'severity-p3',
    P4: 'severity-p4',
  }
  return <span className={`px-2 py-1 rounded-full text-xs font-medium border ${colors[severity] || 'bg-gray-100'}`}>{severity}</span>
}

function StatusBadge({ status }) {
  const statusClass = `status-${status.toLowerCase()}`
  return <span className={`px-2 py-1 rounded-full text-xs font-medium ${statusClass}`}>{status}</span>
}

export default function IncidentDetail() {
  const { id } = useParams()
  const navigate = useNavigate()
  const [incident, setIncident] = useState(null)
  const [signals, setSignals] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [updating, setUpdating] = useState(false)
  
  useEffect(() => {
    fetchData()
  }, [id])
  
  const fetchData = async () => {
    try {
      setLoading(true)
      const [incidentRes, signalsRes] = await Promise.all([
        getIncident(id),
        getIncidentSignals(id)
      ])
      setIncident(incidentRes.data.data)
      setSignals(signalsRes.data.data || [])
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }
  
  const handleStatusChange = async (newStatus) => {
    try {
      setUpdating(true)
      await updateIncidentStatus(id, newStatus)
      await fetchData()
    } catch (err) {
      setError(err.message)
    } finally {
      setUpdating(false)
    }
  }
  
  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>
    )
  }
  
  if (error) {
    return (
      <div className="bg-red-50 border border-red-200 rounded-lg p-4">
        <p className="text-red-800">Error: {error}</p>
      </div>
    )
  }
  
  if (!incident) {
    return <div>Incident not found</div>
  }
  
  const allowedTransitions = {
    OPEN: ['INVESTIGATING', 'RESOLVED', 'CLOSED'],
    INVESTIGATING: ['OPEN', 'RESOLVED', 'CLOSED'],
    RESOLVED: ['INVESTIGATING', 'CLOSED'],
    CLOSED: []
  }
  
  return (
    <div>
      <div className="mb-6 flex items-center justify-between">
        <div>
          <Link to="/" className="text-blue-600 hover:text-blue-900 text-sm">← Back to Dashboard</Link>
          <h2 className="text-2xl font-bold text-gray-900 mt-2">{incident.incidentId}</h2>
        </div>
        <div className="flex items-center space-x-3">
          <SeverityBadge severity={incident.severity} />
          <StatusBadge status={incident.status} />
        </div>
      </div>
      
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Incident Details */}
        <div className="lg:col-span-2 space-y-6">
          <div className="bg-white shadow rounded-lg p-6">
            <h3 className="text-lg font-semibold text-gray-900 mb-4">Details</h3>
            <dl className="grid grid-cols-2 gap-4">
              <div>
                <dt className="text-sm font-medium text-gray-500">Component</dt>
                <dd className="mt-1 text-sm text-gray-900">{incident.componentId}</dd>
              </div>
              <div>
                <dt className="text-sm font-medium text-gray-500">Type</dt>
                <dd className="mt-1 text-sm text-gray-900">{incident.componentType}</dd>
              </div>
              <div>
                <dt className="text-sm font-medium text-gray-500">First Signal</dt>
                <dd className="mt-1 text-sm text-gray-900">
                  {incident.firstSignalTime ? format(new Date(incident.firstSignalTime), 'PPpp') : '-'}
                </dd>
              </div>
              <div>
                <dt className="text-sm font-medium text-gray-500">Last Signal</dt>
                <dd className="mt-1 text-sm text-gray-900">
                  {incident.lastSignalTime ? format(new Date(incident.lastSignalTime), 'PPpp') : '-'}
                </dd>
              </div>
              <div>
                <dt className="text-sm font-medium text-gray-500">Signal Count</dt>
                <dd className="mt-1 text-sm text-gray-900">{incident.signalCount}</dd>
              </div>
              <div>
                <dt className="text-sm font-medium text-gray-500">MTTR</dt>
                <dd className="mt-1 text-sm text-gray-900">
                  {incident.mttr ? `${Math.floor(incident.mttr / 60)}m ${incident.mttr % 60}s` : '-'}
                </dd>
              </div>
            </dl>
            <div className="mt-4">
              <dt className="text-sm font-medium text-gray-500">Description</dt>
              <dd className="mt-1 text-sm text-gray-900">{incident.description}</dd>
            </div>
          </div>
          
          {/* Signals */}
          <div className="bg-white shadow rounded-lg p-6">
            <h3 className="text-lg font-semibold text-gray-900 mb-4">
              Raw Signals ({signals.length})
            </h3>
            <div className="space-y-3 max-h-96 overflow-y-auto">
              {signals.length === 0 ? (
                <p className="text-gray-500">No signals</p>
              ) : (
                signals.map((signal) => (
                  <div key={signal.signalId} className="border rounded p-3">
                    <div className="flex justify-between items-start">
                      <div>
                        <span className="font-medium text-sm">{signal.signalId}</span>
                        <span className="ml-2 text-xs text-gray-500">
                          {signal.timestamp ? format(new Date(signal.timestamp), 'PPpp') : ''}
                        </span>
                      </div>
                      <SeverityBadge severity={signal.severity} />
                    </div>
                    <p className="text-sm text-gray-600 mt-1">{signal.message}</p>
                  </div>
                ))
              )}
            </div>
          </div>
        </div>
        
        {/* Sidebar */}
        <div className="space-y-6">
          {/* Status Actions */}
          <div className="bg-white shadow rounded-lg p-6">
            <h3 className="text-lg font-semibold text-gray-900 mb-4">Actions</h3>
            <div className="space-y-2">
              {allowedTransitions[incident.status]?.map((status) => (
                <button
                  key={status}
                  onClick={() => handleStatusChange(status)}
                  disabled={updating}
                  className="w-full px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 disabled:opacity-50"
                >
                  Move to {status}
                </button>
              ))}
              {incident.status !== 'CLOSED' && !incident.rca && (
                <Link
                  to={`/incidents/${id}/rca`}
                  className="block w-full px-4 py-2 bg-green-600 text-white rounded hover:bg-green-700 text-center"
                >
                  Submit RCA
                </Link>
              )}
            </div>
          </div>
          
          {/* RCA */}
          {incident.rca && (
            <div className="bg-white shadow rounded-lg p-6">
              <h3 className="text-lg font-semibold text-gray-900 mb-4">Root Cause Analysis</h3>
              <dl className="space-y-3">
                <div>
                  <dt className="text-xs text-gray-500">Category</dt>
                  <dd className="text-sm font-medium">{incident.rca.rootCauseCategory}</dd>
                </div>
                <div>
                  <dt className="text-xs text-gray-500">Fix Applied</dt>
                  <dd className="text-sm">{incident.rca.fixApplied}</dd>
                </div>
                <div>
                  <dt className="text-xs text-gray-500">Prevention</dt>
                  <dd className="text-sm">{incident.rca.preventionSteps}</dd>
                </div>
              </dl>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}