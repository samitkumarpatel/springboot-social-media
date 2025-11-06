import React, { createContext, useContext, useEffect, useMemo, useState } from 'react'

type AuthContextValue = {
  userId: number | null
  ready: boolean
  login: (userId: number) => void
  logout: () => void
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined)

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [userId, setUserId] = useState<number | null>(null)
  const [ready, setReady] = useState(false)

  useEffect(() => {
    const raw = localStorage.getItem('sm.userId')
    if (raw) {
      const parsed = Number(raw)
      if (!Number.isNaN(parsed)) setUserId(parsed)
    }
    setReady(true)
  }, [])

  const value = useMemo<AuthContextValue>(() => ({
    userId,
    ready,
    login: (id: number) => {
      setUserId(id)
      localStorage.setItem('sm.userId', String(id))
    },
    logout: () => {
      setUserId(null)
      localStorage.removeItem('sm.userId')
    }
  }), [userId, ready])

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within AuthProvider')
  return ctx
}


