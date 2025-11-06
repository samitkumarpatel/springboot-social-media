// Determine API base URL
// Priority: VITE_API_BASE > default('http://localhost:8080')
const explicit = (import.meta as any).env?.VITE_API_BASE as string | undefined
const defaultBase = 'http://localhost:8080'

export const API_BASE = (explicit && explicit.trim()) ? explicit : defaultBase

export function apiUrl(path: string): string {
  if (!path.startsWith('/')) path = '/' + path
  // If API_BASE already ends with '/', avoid double slash
  const base = API_BASE.endsWith('/') ? API_BASE.slice(0, -1) : API_BASE
  return base + path
}


