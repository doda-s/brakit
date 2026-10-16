import React from 'react'
import { useParams } from 'react-router-dom'
import { rankingService } from '@/lib/services'
import { usePolling } from '@/hooks/usePolling'
import { useTournamentName } from '@/hooks/useTournamentName'
import { RankingTable } from '@/components/custom/RankingTable'
import { TvLayout } from '@/components/custom/TvLayout'

export const RankingPreviewPage: React.FC = () => {
  const { id } = useParams<{ id: string }>()
  const { name, error } = useTournamentName(id)
  const { data: ranking } = usePolling(
    async () => (await rankingService.getByTournament(id!)).data,
    3000,
    !!id && !error,
  )

  return (
    <TvLayout title="Ranking dos times" subtitle={name} error={error}>
      {!ranking ? (
        <p className="text-2xl text-gray-300">Carregando...</p>
      ) : ranking.length === 0 ? (
        <p className="text-2xl text-gray-300">Nenhum time cadastrado.</p>
      ) : (
        <RankingTable rows={ranking} tv />
      )}
    </TvLayout>
  )
}
