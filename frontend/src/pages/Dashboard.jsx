import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { getActiveIncidents } from '../services/api'
import { format } from 'date-fns'

function SeverityBadge({ severity }) {
  const colors = {
    P0: 'severity-p0',
    P1: 'severity-p1',
    P2: 'severity-p2',
    P3: 'severity-p3',
    P4: 'severity-p4',
  }
  
  return (
    <span className={`px-2 py-1 rounded-full text-xs font-medium border ${colors[severity] || 'bg-gray-100'}`}>
      {severity}
    </span>
  )
}

function StatusBadge({ status }) {
  const statusClass = `status-${status.toLowerCase()}`
  
  return (
    <span className={`px-2 py-1 rounded-full text-xs font-medium ${statusClass}`}>
      {status}
    </span>
  )
}

export default function Dashboard() {
  const [incidents, setIncidents] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  
  useEffect(() => {
    fetchIncidents()
  }, [])
  
  const fetchIncidents = async () => {
    try {
      setLoading(true)
      const response = await getActiveIncidents()
      setIncidents(response.data.data || [])
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
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
  
  return (
    <div>
      <div className="mb-6">
        <h2 className="text-2xl font-bold text-gray-900">Active Incidents</h2>
        <p className="text-gray-600 mt-1">Current incidents requiring attention</p>
      </div>
      
      {incidents.length === 0 ? (
        <div className="bg-white rounded-lg shadow p-8 text-center">
          <p className="text-gray-500">No active incidents</p>
        </div>
      ) : (
        <div className="bg-white shadow rounded-lg overflow-hidden">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">ID</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Title</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Component</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Severity</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Status</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Signals</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Created</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Actions</th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {incidents.map((incident) => (
                <tr key={incident.id} className="hover:bg-gray-50">
                  <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                    {incident.incidentId}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    {incident.title}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    {incident.componentId}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <SeverityBadge severity={incident.severity} />
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <StatusBadge status={incident.status} />
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    {incident.signalCount}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    {incident.createdAt ? format(new Date(incident.createdAt), 'MMM d, HH:mm') : '-'}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm">
                    <Link 
                      to={`/incidents/${incident.id}`}
                      className="text-blue-600 hover:text-blue-900"
                    >
                      View
                    </Link>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  )
}