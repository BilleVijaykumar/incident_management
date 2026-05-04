import { BrowserRouter, Routes, Route } from 'react-router-dom'
import Dashboard from './pages/Dashboard'
import IncidentDetail from './pages/IncidentDetail'
import RcaForm from './pages/RcaForm'

function App() {
  return (
    <BrowserRouter>
      <div className="min-h-screen bg-gray-50">
        <nav className="bg-white shadow-sm border-b">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <div className="flex justify-between h-16">
              <div className="flex items-center">
                <h1 className="text-xl font-bold text-gray-900">Incident Management System</h1>
              </div>
              <div className="flex items-center space-x-4">
                <a href="/" className="text-gray-600 hover:text-gray-900">Dashboard</a>
              </div>
            </div>
          </div>
        </nav>
        
        <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          <Routes>
            <Route path="/" element={<Dashboard />} />
            <Route path="/incidents/:id" element={<IncidentDetail />} />
            <Route path="/incidents/:id/rca" element={<RcaForm />} />
          </Routes>
        </main>
      </div>
    </BrowserRouter>
  )
}

export default App