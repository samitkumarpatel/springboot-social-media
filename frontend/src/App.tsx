import { Link, Navigate, Route, Routes, useLocation } from 'react-router-dom'
import PostList from './pages/PostList'
import PostDetail from './pages/PostDetail'
import Login from './pages/Login'
import { AuthProvider, useAuth } from './auth/AuthContext'

export default function App() {
  return (
    <AuthProvider>
      <div className="mx-auto max-w-3xl px-4 py-6">
        <SiteHeader />
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/" element={<RequireAuth><PostList /></RequireAuth>} />
          <Route path="/posts/:id" element={<RequireAuth><PostDetail /></RequireAuth>} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </div>
    </AuthProvider>
  )
}

function SiteHeader() {
  const { userId, logout } = useAuth()
  return (
    <header className="mb-6">
      <div className="flex items-center justify-between">
        <Link to="/" className="text-2xl font-bold">Social Media</Link>
        <nav className="flex items-center gap-3">
          <Link to="/" className="btn btn-ghost">Home</Link>
          {userId != null ? (
            <>
              <span className="muted">User: {userId}</span>
              <button onClick={logout} className="btn btn-ghost">Logout</button>
            </>
          ) : (
            <Link to="/login" className="btn btn-primary">Login</Link>
          )}
        </nav>
      </div>
    </header>
  )
}

function RequireAuth({ children }: { children: React.ReactNode }) {
  const { userId, ready } = useAuth()
  const location = useLocation()
  if (!ready) return null
  if (userId == null) return <Navigate to="/login" state={{ from: location }} replace />
  return <>{children}</>
}


