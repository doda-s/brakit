import React from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { ArrowLeft, ExternalLink, Minus, Plus } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Match, Tournament } from '@/types'
import {
  disqualifyService,
  matchService,
  rankingService,
  tournamentService,
} from '@/lib/services'
import { getErrorMessage } from '@/lib/errors'
import {
  currentRound,
  getChampion,
  groupByRound,
  isReady,
  resultLabel,
  roundName,
  statusLabel,
} from '@/lib/bracket'
import { useAuth } from '@/hooks/useAuth'
import { usePolling } from '@/hooks/usePolling'

export const ArbiterPanelPage: React.FC = () => {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const { user } = useAuth()
  const [tournament, setTournament] = React.useState<Tournament | null>(null)
  const [loadError, setLoadError] = React.useState('')
  const [actionError, setActionError] = React.useState('')
  const [busyId, setBusyId] = React.useState<number | null>(null)

  React.useEffect(() => {
    if (!id) return
    let active = true
    tournamentService
      .getById(id)
      .then(({ data }) => {
        if (!active) return
        if (user && data.ownerId !== user.id) {
          navigate(`/tournaments/${id}`, { replace: true })
          return
        }
        setTournament(data)
      })
      .catch((err) => {
        if (active) setLoadError(getErrorMessage(err, 'Não foi possível carregar o torneio.'))
      })
    return () => {
      active = false
    }
  }, [id, user, navigate])

  const ready = !!id && !!tournament
  const { data: matches, reload: reloadMatches } = usePolling(
    async () => (await matchService.getByTournament(id!)).data,
    3000,
    ready,
  )
  const { data: ranking, reload: reloadRanking } = usePolling(
    async () => (await rankingService.getByTournament(id!)).data,
    3000,
    ready,
  )

  const disqualifiedIds = new Set(
    (ranking ?? []).filter((r) => r.status === 'DISQUALIFIED').map((r) => r.teamId),
  )

  // Executa uma ação do árbitro e recarrega a chave
  const run = async (matchId: number, action: () => Promise<unknown>, fallback: string) => {
    setBusyId(matchId)
    setActionError('')
    try {
      await action()
      await Promise.all([reloadMatches(), reloadRanking()])
    } catch (err) {
      setActionError(getErrorMessage(err, fallback))
      await Promise.all([reloadMatches(), reloadRanking()])
    } finally {
      setBusyId(null)
    }
  }

  const changeScore = (match: Match, team: 1 | 2, delta: number) => {
    const next = {
      team1Score: match.team1Score + (team === 1 ? delta : 0),
      team2Score: match.team2Score + (team === 2 ? delta : 0),
    }
    if (next.team1Score < 0 || next.team2Score < 0) return
    return run(match.id, () => matchService.updateScore(id!, match.id, next), 'Não foi possível atualizar o placar.')
  }

  const forceWinner = (match: Match, teamId: number, name: string | null) => {
    if (!window.confirm(`Declarar "${name}" vencedor desta partida, independentemente do placar?`)) return
    return run(match.id, () => matchService.setWinner(id!, match.id, teamId), 'Não foi possível declarar o vencedor.')
  }

  const disqualify = (match: Match, teamId: number, name: string | null) => {
    if (!window.confirm(`Desqualificar "${name}"? O time será eliminado do torneio e o adversário avança por W.O.`)) return
    return run(match.id, () => disqualifyService.disqualify(id!, teamId), 'Não foi possível desqualificar o time.')
  }

  if (loadError) {
    return (
      <div className="flex min-h-screen flex-col items-center justify-center gap-4 bg-gray-50">
        <p>{loadError}</p>
        <Button variant="outline" onClick={() => navigate('/dashboard')}>Voltar</Button>
      </div>
    )
  }

  if (!tournament || !matches) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-gray-50">
        <p>Carregando...</p>
      </div>
    )
  }

  const rounds = groupByRound(matches)
  const total = rounds.length
  const active = currentRound(matches)
  const champion = getChampion(matches)

  return (
    <div className="min-h-screen bg-gray-50">
      <header className="border-b border-gray-200 bg-white">
        <div className="mx-auto max-w-5xl px-4 py-4 sm:px-6 lg:px-8">
          <Button variant="ghost" onClick={() => navigate(`/tournaments/${id}`)} className="mb-4 gap-2">
            <ArrowLeft className="h-4 w-4" />
            Voltar
          </Button>
          <div className="flex flex-wrap items-center justify-between gap-2">
            <div>
              <h1 className="text-3xl font-bold tracking-tight text-gray-900">Painel do árbitro</h1>
              <p className="mt-1 text-gray-600">{tournament.name}</p>
            </div>
            <Button
              variant="outline"
              size="sm"
              onClick={() => window.open(`/preview/round/${id}`, '_blank', 'noopener')}
            >
              <ExternalLink className="mr-1 h-4 w-4" />
              Telão: round atual
            </Button>
          </div>
          {champion && (
            <p className="mt-3 rounded-md bg-yellow-50 px-3 py-2 text-sm font-semibold text-yellow-800">
              Torneio encerrado. Campeão: {champion.name}
            </p>
          )}
        </div>
      </header>

      <main className="mx-auto max-w-5xl space-y-8 px-4 py-8 sm:px-6 lg:px-8">
        {actionError && (
          <div role="alert" className="rounded-md bg-red-50 px-4 py-3 text-sm text-red-700">
            {actionError}
          </div>
        )}

        {matches.length === 0 && (
          <Card>
            <CardContent className="pt-6 text-center text-gray-500">
              O chaveamento ainda não foi gerado.
            </CardContent>
          </Card>
        )}

        {rounds.map((roundMatches, index) => (
          <section key={index} className="space-y-3">
            <h2 className="flex items-center gap-2 text-xl font-bold">
              {roundName(index + 1, total)}
              {active === index + 1 && <Badge>Round atual</Badge>}
            </h2>

            <div className="grid gap-4 md:grid-cols-2">
              {roundMatches.map((match) => {
                const busy = busyId === match.id
                const finished = match.status === 'FINISHED'
                const sides = [
                  { n: 1 as const, teamId: match.team1Id, name: match.team1Name, score: match.team1Score },
                  { n: 2 as const, teamId: match.team2Id, name: match.team2Name, score: match.team2Score },
                ]
                return (
                  <Card key={match.id}>
                    <CardHeader className="pb-2">
                      <CardTitle className="flex items-center justify-between text-base">
                        <span>Partida {match.position + 1}</span>
                        <Badge variant={match.status === 'IN_PROGRESS' ? 'destructive' : 'secondary'}>
                          {statusLabel[match.status]}
                        </Badge>
                      </CardTitle>
                    </CardHeader>
                    <CardContent className="space-y-3">
                      {sides.map((side) => (
                        <div
                          key={side.n}
                          className={`flex items-center justify-between gap-2 rounded-md p-2 ${
                            finished && match.winnerId === side.teamId
                              ? 'bg-green-50 font-semibold text-green-800'
                              : 'bg-gray-50'
                          }`}
                        >
                          <span className="truncate">{side.name ?? 'A definir'}</span>
                          <div className="flex items-center gap-1">
                            {match.status === 'IN_PROGRESS' && (
                              <Button
                                size="icon"
                                variant="outline"
                                className="h-8 w-8"
                                aria-label={`Diminuir placar de ${side.name}`}
                                disabled={busy || side.score === 0}
                                onClick={() => changeScore(match, side.n, -1)}
                              >
                                <Minus className="h-4 w-4" />
                              </Button>
                            )}
                            <span className="w-8 text-center text-lg font-bold tabular-nums">
                              {match.resultType === 'BYE' ? '-' : side.score}
                            </span>
                            {match.status === 'IN_PROGRESS' && (
                              <Button
                                size="icon"
                                variant="outline"
                                className="h-8 w-8"
                                aria-label={`Aumentar placar de ${side.name}`}
                                disabled={busy}
                                onClick={() => changeScore(match, side.n, 1)}
                              >
                                <Plus className="h-4 w-4" />
                              </Button>
                            )}
                          </div>
                        </div>
                      ))}

                      {finished && match.resultType && (
                        <p className="text-xs text-gray-500">Resultado: {resultLabel[match.resultType]}</p>
                      )}

                      {!finished && (
                        <div className="flex flex-wrap gap-2">
                          {match.status === 'SCHEDULED' && (
                            <Button
                              size="sm"
                              disabled={busy || !isReady(match)}
                              title={!isReady(match) ? 'Aguardando os times desta partida' : undefined}
                              onClick={() =>
                                run(match.id, () => matchService.start(id!, match.id), 'Não foi possível iniciar a partida.')
                              }
                            >
                              Iniciar partida
                            </Button>
                          )}
                          {match.status === 'IN_PROGRESS' && (
                            <Button
                              size="sm"
                              disabled={busy}
                              onClick={() =>
                                run(match.id, () => matchService.finish(id!, match.id), 'Não foi possível encerrar a partida.')
                              }
                            >
                              Encerrar pelo placar
                            </Button>
                          )}
                        </div>
                      )}

                      {!finished && isReady(match) && (
                        <div className="space-y-2 border-t pt-3">
                          <p className="text-xs font-semibold uppercase text-gray-500">
                            Decisões do árbitro
                          </p>
                          <div className="flex flex-wrap gap-2">
                            {sides.map((side) => (
                              <Button
                                key={`w${side.n}`}
                                size="sm"
                                variant="outline"
                                disabled={busy}
                                onClick={() => forceWinner(match, side.teamId!, side.name)}
                              >
                                Vitória: {side.name}
                              </Button>
                            ))}
                          </div>
                          <div className="flex flex-wrap gap-2">
                            {sides.map((side) => (
                              <Button
                                key={`d${side.n}`}
                                size="sm"
                                variant="outline"
                                className="text-red-600"
                                disabled={busy || disqualifiedIds.has(side.teamId!)}
                                onClick={() => disqualify(match, side.teamId!, side.name)}
                              >
                                Desqualificar {side.name}
                              </Button>
                            ))}
                          </div>
                        </div>
                      )}
                    </CardContent>
                  </Card>
                )
              })}
            </div>
          </section>
        ))}
      </main>
    </div>
  )
}
