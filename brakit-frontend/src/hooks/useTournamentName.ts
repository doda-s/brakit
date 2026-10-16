import { useEffect, useState } from 'react'
import { tournamentService } from '@/lib/services'
import { getErrorMessage } from '@/lib/errors'

// Nome do torneio para os telões (leitura pública)
export function useTournamentName(id: string | undefined) {
  const [name, setName] = useState('')
  const [error, setError] = useState('')

  useEffect(() => {
    if (!id) return
    let active = true
    tournamentService
      .getById(id)
      .then(({ data }) => active && setName(data.name))
      .catch((err) => active && setError(getErrorMessage(err, 'Torneio não encontrado.')))
    return () => {
      active = false
    }
  }, [id])

  return { name, error }
}
