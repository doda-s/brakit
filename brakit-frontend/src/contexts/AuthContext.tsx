import React, { createContext, useState, useEffect } from 'react'
import { User } from '@/types'
import { authService } from '@/lib/services'

interface AuthContextType {
  user: User | null
  isLoading: boolean
  isAuthenticated: boolean
  login: (email: string, password: string) => Promise<void>
  signup: (email: string, password: string, name: string) => Promise<void>
  logout: () => void
}

export const AuthContext = createContext<AuthContextType | undefined>(undefined)

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({
  children,
}) => {
  const [user, setUser] = useState<User | null>(null)
  const [isLoading, setIsLoading] = useState(true)

  const syncSession = async () => {
    const { data } = await authService.me()
    localStorage.setItem('auth_user', JSON.stringify(data))
    setUser(data)
  }

  useEffect(() => {
    const token = localStorage.getItem('auth_token')

    if (!token) {
      setIsLoading(false)
      return
    }

    syncSession()
      .catch(() => {
        localStorage.removeItem('auth_token')
        localStorage.removeItem('auth_user')
        setUser(null)
      })
      .finally(() => setIsLoading(false))
  }, [])

  const login = async (email: string, password: string) => {
    setIsLoading(true)
    try {
      const { data } = await authService.login({ email, password })
      localStorage.setItem('auth_token', data.accessToken)
      await syncSession()
    } finally {
      setIsLoading(false)
    }
  }

  const signup = async (email: string, password: string, name: string) => {
    setIsLoading(true)
    try {
      await authService.register({ email, password, name })
      const { data } = await authService.login({ email, password })
      localStorage.setItem('auth_token', data.accessToken)
      await syncSession()
    } finally {
      setIsLoading(false)
    }
  }

  const logout = () => {
    localStorage.removeItem('auth_token')
    localStorage.removeItem('auth_user')
    setUser(null)
  }

  return (
    <AuthContext.Provider
      value={{
        user,
        isLoading,
        isAuthenticated: !!user,
        login,
        signup,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  )
}
