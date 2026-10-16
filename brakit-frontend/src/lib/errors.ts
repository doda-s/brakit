import axios from 'axios'
import { ErrorResponse } from '@/types'

// Extrai a mensagem do ErrorResponse da API ({ status, message }) ou devolve um fallback.
// 401/403 gerados pelo framework de segurança chegam sem corpo.
export function getErrorMessage(error: unknown, fallback: string): string {
  if (axios.isAxiosError(error)) {
    const data = error.response?.data as Partial<ErrorResponse> | undefined
    if (data?.message) {
      return data.message
    }
    if (error.response?.status === 403) {
      return 'Você não tem permissão para realizar esta ação.'
    }
    if (error.response?.status === 401) {
      return 'Sessão expirada ou credenciais inválidas.'
    }
    if (!error.response) {
      return 'Não foi possível conectar ao servidor.'
    }
  }
  return fallback
}
