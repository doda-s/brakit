import React from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { ArrowLeft, ExternalLink, Plus, Trash2, Trophy } from 'lucide-react'
import { Team, Tournament } from '@/types'
import {
  bracketService,
  matchService,
  rankingService,
  teamService,
  tournamentService,
} from '@/lib/services'
import { getErrorMessage } from '@/lib/errors'
import { getChampion } from '@/lib/bracket'
import { useAuth } from '@/hooks/useAuth'
import { usePolling } from '@/hooks/usePolling'
import { BracketView } from '@/components/custom/BracketView'
import { RankingTable } from '@/components/custom/RankingTable'

export const TournamentDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const { user } = useAuth()
  const [tournament, setTournament] = React.useState<Tournament | null>(null)
  const [teams, setTeams] = React.useState<Team[]>([])
  const [isLoading, setIsLoading] = React.useState(true)
  const [error, setError] = React.useState('')
  const [actionError, setActionError] = React.useState('')
  const [isGenerating, setIsGenerating] = React.useState(false)

  const isOwner = !!user && !!tournament && tournament.ownerId === user.id

  React.useEffect(() => {
    if (!id) {
      return
    }

    let isMounted = true

    const load = async () => {
      setIsLoading(true)
      setError('')

      try {
        const [tournamentRes, teamsRes] = await Promise.all([
          tournamentService.getById(id),
          teamService.getByTournament(id),
        ])
        if (isMounted) {
          setTournament(tournamentRes.data)
          setTeams(teamsRes.data)
        }
      } catch (err) {
        if (isMounted) {
          setError(getErrorMessage(err, 'Não foi possível carregar o torneio.'))
        }
      } finally {
        if (isMounted) {
          setIsLoading(false)
        }
      }
    }

    load()

    return () => {
      isMounted = false
    }
  }, [id])

  // Chave e ranking atualizam sozinhos (RNF03)
  const ready = !!id && !!tournament
  const { data: matches, reload: reloadMatches } = usePolling(
    async () => (await matchService.getByTournament(id!)).data,
    5000,
    ready,
  )
  const { data: ranking, reload: reloadRanking } = usePolling(
    async () => (await rankingService.getByTournament(id!)).data,
    5000,
    ready,
  )

  const refresh = async () => {
    await Promise.all([reloadMatches(), reloadRanking()])
  }

  const hasBracket = (matches?.length ?? 0) > 0
  const champion = matches ? getChampion(matches) : null

  const handleDeleteTeam = async (team: Team) => {
    if (!id || !window.confirm(`Excluir o time "${team.name}"?`)) {
      return
    }
    setActionError('')
    try {
      await teamService.delete(id, team.id)
      setTeams((current) => current.filter((t) => t.id !== team.id))
      await reloadRanking()
    } catch (err) {
      setActionError(getErrorMessage(err, 'Não foi possível excluir o time.'))
    }
  }

  const handleDeleteTournament = async () => {
    if (
      !id ||
      !tournament ||
      !window.confirm(
        `Excluir o torneio "${tournament.name}"? Todos os times, integrantes e partidas serão removidos.`,
      )
    ) {
      return
    }
    setActionError('')
    try {
      await tournamentService.delete(id)
      navigate('/dashboard')
    } catch (err) {
      setActionError(getErrorMessage(err, 'Não foi possível excluir o torneio.'))
    }
  }

  const handleGenerate = async () => {
    if (!id) return
    if (
      !window.confirm(
        'Gerar o chaveamento? Depois disso não será possível adicionar ou excluir times.',
      )
    ) {
      return
    }
    setIsGenerating(true)
    setActionError('')
    try {
      await bracketService.generate(id)
      await refresh()
    } catch (err) {
      setActionError(getErrorMessage(err, 'Não foi possível gerar o chaveamento.'))
    } finally {
      setIsGenerating(false)
    }
  }

  const handleReset = async () => {
    if (
      !id ||
      !window.confirm(
        'Reiniciar o chaveamento? Todas as partidas e resultados serão apagados.',
      )
    ) {
      return
    }
    setActionError('')
    try {
      await bracketService.reset(id)
      await refresh()
    } catch (err) {
      setActionError(getErrorMessage(err, 'Não foi possível reiniciar o chaveamento.'))
    }
  }

  if (isLoading) {
    return (
      <div className="min-h-screen bg-gray-50">
        <div className="flex items-center justify-center py-20">
          <p>Carregando...</p>
        </div>
      </div>
    )
  }

  if (error || !tournament) {
    return (
      <div className="min-h-screen bg-gray-50">
        <div className="flex flex-col items-center justify-center gap-4 py-20">
          <p>{error || 'Torneio não encontrado.'}</p>
          <Button variant="outline" onClick={() => navigate('/dashboard')}>
            Voltar
          </Button>
        </div>
      </div>
    )
  }

  const isFull = teams.length >= tournament.teamCountLimit
  const canAddTeams = isOwner && !hasBracket && !isFull
  const openPreview = (path: string) => window.open(path, '_blank', 'noopener')

  return (
    <div className="min-h-screen bg-gray-50">
      <header className="border-b border-gray-200 bg-white">
        <div className="mx-auto max-w-7xl px-4 py-4 sm:px-6 lg:px-8">
          <Button
            variant="ghost"
            onClick={() => navigate('/dashboard')}
            className="mb-4 gap-2"
          >
            <ArrowLeft className="h-4 w-4" />
            Voltar
          </Button>
          <h1 className="text-3xl font-bold tracking-tight text-gray-900">
            {tournament.name}
          </h1>
          <p className="mt-1 text-gray-600">{tournament.description}</p>
          <p className="mt-2 text-xs uppercase tracking-wide text-gray-400">
            {tournament.visibility === 'PUBLIC' ? 'Público' : 'Privado'} -{' '}
            {teams.length}/{tournament.teamCountLimit} times - mata-mata
          </p>
          {champion && (
            <p className="mt-3 inline-flex items-center gap-2 rounded-md bg-yellow-50 px-3 py-2 text-sm font-semibold text-yellow-800">
              <Trophy className="h-4 w-4" />
              Campeão: {champion.name}
            </p>
          )}
          {isOwner && (
            <div className="mt-4 flex flex-wrap gap-2">
              <Button
                variant="outline"
                onClick={() => navigate(`/tournaments/${id}/edit`)}
              >
                Editar torneio
              </Button>
              <Button
                variant="outline"
                className="text-red-600"
                onClick={handleDeleteTournament}
              >
                Excluir torneio
              </Button>
            </div>
          )}
        </div>
      </header>

      <main className="mx-auto max-w-7xl space-y-4 px-4 py-8 sm:px-6 lg:px-8">
        {actionError && (
          <div
            role="alert"
            className="rounded-md bg-red-50 px-4 py-3 text-sm text-red-700"
          >
            {actionError}
          </div>
        )}

        <Tabs defaultValue="teams" className="w-full">
          <TabsList>
            <TabsTrigger value="teams">Times</TabsTrigger>
            <TabsTrigger value="bracket">Chaveamento</TabsTrigger>
            <TabsTrigger value="ranking">Ranking</TabsTrigger>
          </TabsList>

          {/* Times */}
          <TabsContent value="teams" className="space-y-4">
            <div className="flex items-center justify-between">
              <h2 className="text-xl font-bold">Times</h2>
              {isOwner && (
                <Button
                  disabled={!canAddTeams}
                  title={
                    hasBracket
                      ? 'Chaveamento gerado: não é possível adicionar times'
                      : isFull
                        ? 'Limite de times atingido'
                        : undefined
                  }
                  onClick={() => navigate(`/tournaments/${id}/teams/create`)}
                >
                  <Plus className="mr-2 h-4 w-4" />
                  Novo Time
                </Button>
              )}
            </div>

            {hasBracket && isOwner && (
              <p className="text-sm text-gray-500">
                O chaveamento já foi gerado. Times não podem ser adicionados nem
                excluídos (você ainda pode renomeá-los e editar integrantes).
              </p>
            )}

            {teams.length === 0 ? (
              <Card>
                <CardContent className="pt-6 text-center">
                  <p className="text-gray-500">Nenhum time cadastrado</p>
                </CardContent>
              </Card>
            ) : (
              <div className="grid grid-cols-1 gap-4 md:grid-cols-2 lg:grid-cols-3">
                {teams.map((team) => (
                  <Card key={team.id}>
                    <CardHeader>
                      <CardTitle className="flex items-center justify-between">
                        <span>{team.name}</span>
                        {isOwner && !hasBracket && (
                          <Button
                            variant="ghost"
                            size="icon"
                            className="text-red-600"
                            title="Excluir time"
                            onClick={() => handleDeleteTeam(team)}
                          >
                            <Trash2 className="h-4 w-4" />
                          </Button>
                        )}
                      </CardTitle>
                    </CardHeader>
                    <CardContent>
                      <Button
                        variant="outline"
                        className="w-full"
                        onClick={() =>
                          navigate(`/tournaments/${id}/teams/${team.id}`)
                        }
                      >
                        {isOwner ? 'Gerenciar' : 'Ver integrantes'}
                      </Button>
                    </CardContent>
                  </Card>
                ))}
              </div>
            )}
          </TabsContent>

          {/* Chaveamento */}
          <TabsContent value="bracket" className="space-y-4">
            <div className="flex flex-wrap items-center justify-between gap-2">
              <h2 className="text-xl font-bold">Chaveamento</h2>
              <div className="flex flex-wrap gap-2">
                {hasBracket && (
                  <>
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => openPreview(`/preview/bracket/${id}`)}
                    >
                      <ExternalLink className="mr-1 h-4 w-4" />
                      Telão: chave
                    </Button>
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => openPreview(`/preview/round/${id}`)}
                    >
                      <ExternalLink className="mr-1 h-4 w-4" />
                      Telão: round atual
                    </Button>
                  </>
                )}
                {isOwner && hasBracket && (
                  <>
                    <Button size="sm" onClick={() => navigate(`/tournaments/${id}/arbiter`)}>
                      Painel do árbitro
                    </Button>
                    <Button
                      variant="outline"
                      size="sm"
                      className="text-red-600"
                      onClick={handleReset}
                    >
                      Reiniciar chave
                    </Button>
                  </>
                )}
                {isOwner && !hasBracket && (
                  <Button
                    onClick={handleGenerate}
                    disabled={isGenerating || teams.length < 2}
                    title={
                      teams.length < 2
                        ? 'Cadastre ao menos 2 times'
                        : undefined
                    }
                  >
                    {isGenerating ? 'Gerando...' : 'Gerar Chaveamento'}
                  </Button>
                )}
              </div>
            </div>

            {hasBracket && matches ? (
              <Card>
                <CardContent className="pt-6">
                  <BracketView matches={matches} />
                </CardContent>
              </Card>
            ) : (
              <Card>
                <CardContent className="pt-6 text-center">
                  <p className="text-gray-500">
                    {isOwner
                      ? 'Cadastre ao menos 2 times e gere o chaveamento (mata-mata).'
                      : 'O chaveamento ainda não foi gerado.'}
                  </p>
                </CardContent>
              </Card>
            )}
          </TabsContent>

          {/* Ranking */}
          <TabsContent value="ranking" className="space-y-4">
            <div className="flex items-center justify-between">
              <h2 className="text-xl font-bold">Ranking</h2>
              <Button
                variant="outline"
                size="sm"
                onClick={() => openPreview(`/preview/ranking/${id}`)}
              >
                <ExternalLink className="mr-1 h-4 w-4" />
                Telão: ranking
              </Button>
            </div>

            {!ranking || ranking.length === 0 ? (
              <Card>
                <CardContent className="pt-6 text-center">
                  <p className="text-gray-500">Sem dados ainda</p>
                </CardContent>
              </Card>
            ) : (
              <Card>
                <CardContent className="overflow-x-auto pt-6">
                  <RankingTable rows={ranking} />
                  <p className="mt-4 text-xs text-gray-500">
                    Vitória vale 3 pontos. Desempate: saldo de placar, depois
                    pontos marcados. O campeão aparece sempre em primeiro. J =
                    jogos, V = vitórias, D = derrotas.
                  </p>
                </CardContent>
              </Card>
            )}
          </TabsContent>
        </Tabs>
      </main>
    </div>
  )
}
