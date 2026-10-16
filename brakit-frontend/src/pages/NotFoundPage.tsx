import React from 'react'
import { useNavigate } from 'react-router-dom'
import { Button } from '@/components/ui/button'

export const NotFoundPage: React.FC = () => {
  const navigate = useNavigate()
  return (
    <div className="flex min-h-screen flex-col items-center justify-center gap-4 bg-gray-50">
      <h1 className="text-3xl font-bold text-gray-900">Página não encontrada</h1>
      <Button onClick={() => navigate('/dashboard')}>Voltar ao início</Button>
    </div>
  )
}
