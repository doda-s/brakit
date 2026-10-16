import React from 'react'
import { useParams } from 'react-router-dom'
import { matchService } from '@/lib/services'
import { currentRound, getChampion, groupByRound, roundName } from '@/lib/bracket'
import { usePolling } from '@/hooks/usePolling'
import { useTournamentName } from '@/hooks/useTournamentName'
import { TvLayout } from '@/components/custom/TvLayout'
import { Match } from '@/types'
import { cn } from '@/lib/utils'

const Team: React.FC<{ name: string | null; score: number; show: boolean; win: boolean }> = ({
  name,
  score,
  show,
  win,
}) => (
  <div className={cn('flex items-center justify-between gap-6 text-4xl font-bold', win && 'text-green-300')}>
    <span className="truncate">{name ?? 'A definir'}</span>
    {show && <span className="tabular-nums text-5xl">{score}</span>}
  </div>
)

export const RoundPreviewPage: React.FC = () => {
  const { id } = useParams<{ id: string }>()
  const { name, error } = useTournamentName(id)
  const { data: matches } = usePolling(
    async () => (await matchService.getByTournament(id!)).data,
    3000,
    !!id && !error,
  )

  const body = () => {
    if (!matches) return <p className="text-2xl text-gray-300">Carregando...</p>
    if (matches.length === 0) {
      return <p className="text-2xl text-gray-300">O chaveamento ainda não foi gerado.</p>
    }

    const champion = getChampion(matches)
    if (champion) {
      return (
        <p className="text-center text-6xl font-extrabold text-yellow-300">
          Campeão: {champion.name}
        </p>
      )
    }

    const round = currentRound(matches)
    const rounds = groupByRound(matches)
    const total = rounds.length
    const current = round ? rounds[round - 1].filter((m) => m.resultType !== 'BYE') : []

    // Próximos: demais partidas ainda não encerradas, das rodadas seguintes
    const upcoming: Match[] = round
      ? rounds.slice(round).flat().filter((m) => m.status !== 'FINISHED')
      : []

    return (
      <div className="space-y-10">
        <section>
          <h2 className="mb-4 text-3xl font-bold text-yellow-300">
            {round ? roundName(round, total) : 'Aguardando próxima rodada'}
          </h2>
          <div className="grid gap-6 lg:grid-cols-2">
            {current.map((m) => (
              <div
                key={m.id}
                className={cn(
                  'space-y-3 rounded-xl border-2 bg-gray-800 p-6',
                  m.status === 'IN_PROGRESS' ? 'border-red-400' : 'border-gray-600',
                  m.status === 'FINISHED' && 'opacity-60',
                )}
              >
                <Team
                  name={m.team1Name}
                  score={m.team1Score}
                  show={m.status !== 'SCHEDULED'}
                  win={m.status === 'FINISHED' && m.winnerId === m.team1Id}
                />
                <p className="text-center text-xl text-gray-400">vs</p>
                <Team
                  name={m.team2Name}
                  score={m.team2Score}
                  show={m.status !== 'SCHEDULED'}
                  win={m.status === 'FINISHED' && m.winnerId === m.team2Id}
                />
                {m.status === 'IN_PROGRESS' && (
                  <p className="text-center text-xl font-semibold text-red-300">Em andamento</p>
                )}
              </div>
            ))}
          </div>
        </section>

        {upcoming.length > 0 && (
          <section>
            <h2 className="mb-4 text-3xl font-bold text-yellow-300">Próximas partidas</h2>
            <ul className="space-y-2 text-2xl">
              {upcoming.map((m) => (
                <li key={m.id} className="flex justify-between gap-4 border-b border-gray-700 py-2">
                  <span className="text-gray-400">{roundName(m.round, total)}</span>
                  <span>
                    {m.team1Name ?? 'A definir'} x {m.team2Name ?? 'A definir'}
                  </span>
                </li>
              ))}
            </ul>
          </section>
        )}
      </div>
    )
  }

  return (
    <TvLayout title="Round atual" subtitle={name} error={error}>
      {body()}
    </TvLayout>
  )
}
