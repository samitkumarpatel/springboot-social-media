import { FormEvent, useEffect, useState } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'

export default function Login() {
  const [id, setId] = useState('')
  const [error, setError] = useState<string | null>(null)
  const navigate = useNavigate()
  const { login, userId, ready } = useAuth()
  const location = useLocation()

  useEffect(() => {
    if (!ready) return
    if (userId != null) {
      const from = (location.state as any)?.from?.pathname || '/'
      navigate(from, { replace: true })
    }
  }, [ready, userId, navigate, location])

  function submit(e: FormEvent) {
    e.preventDefault()
    const num = Number(id)
    if (!id.trim() || Number.isNaN(num) || num <= 0) {
      setError('Enter a valid numeric userId (> 0)')
      return
    }
    login(num)
    navigate('/')
  }

  if (!ready) return null
  if (userId != null) return null
  return (
    <div className="min-h-screen flex items-center justify-center px-4">
      <div className="w-full max-w-sm card">
        <h2 className="text-2xl font-bold text-center">Login</h2>
        <p className="muted mt-1 text-center">Enter your userId to continue</p>
        <form onSubmit={submit} className="mt-5 flex flex-col gap-3">
          <input className="input" placeholder="User ID" inputMode="numeric" value={id} onChange={e => setId(e.target.value)} />
          <button type="submit" className="btn btn-primary w-full">Continue</button>
        </form>
        {error && <p className="mt-3 text-sm text-red-600 text-center">{error}</p>}
      </div>
    </div>
  )
}


