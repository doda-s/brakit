import React from 'react'
import { useParams } from 'react-router-dom'
import { matchService } from '@/lib/services'
import { getChampion } from '@/lib/bracket'
import { usePolling } from '@/hooks/usePolling'
import { useTournamentName } from '@/hooks/useTournamentName'
import { BracketView } from '@/components/custom/BracketView'
import { TvLayout } from '@/components/custom/TvLayout'

export const BracketPreviewPage: React.FC = () => {
  const { id } = useParams<{ id: string }>()
  const { name, error } = useTournamentName(id)
  const { data: matches } = usePolling(
    async () => (await matchService.getByTournament(id!)).data,
    3000,
    !!id && !error,
  )
  const champion = matches ? getChampion(matches) : null

  return (
    <TvLayout title="Chave do torneio" subtitle={name} error={error}>
      {champion && (
        <p className="mb-6 text-3xl font-bold text-yellow-300">Campeão: {champion.name}</p>
      )}
      {!matches ? (
        <p className="text-2xl text-gray-300">Carregando...</p>
      ) : matches.length === 0 ? (
        <p className="text-2xl text-gray-300">O chaveamento ainda não foi gerado.</p>
      ) : (
        <BracketView matches={matches} tv />
      )}
    </TvLayout>
  )
}
