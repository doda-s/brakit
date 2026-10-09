import React from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { ArrowLeft, Plus, Trash2 } from 'lucide-react'
import { Tournament } from '@/types'
import { tournamentService } from '@/lib/services'

type TeamSummary = {
  id: string
  name: string
  members?: Array<{ id: string }>
}

type RankingRow = {
  teamId: string
  teamName: string
  wins: number
  losses: number
  points: number
  position: number
}

export const TournamentDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const [tournament, setTournament] = React.useState<Tournament | null>(null)
  const [isLoading, setIsLoading] = React.useState(true)
  const [error, setError] = React.useState('')
  const teams: TeamSummary[] = []
  const bracket = null
  const ranking: RankingRow[] = []

  React.useEffect(() => {
    if (!id) {
      return
    }

    let isMounted = true

    const loadTournament = async () => {
      setIsLoading(true)
      setError('')

      try {
        const { data } = await tournamentService.getById(id)
        if (isMounted) {
          setTournament(data)
        }
      } catch {
        if (isMounted) {
          setError('Não foi possível carregar o torneio.')
        }
      } finally {
        if (isMounted) {
          setIsLoading(false)
        }
      }
    }

    loadTournament()

    return () => {
      isMounted = false
    }
  }, [id])

  if (isLoading) {
    return (
      <div className="min-h-screen bg-gray-50">
        <div className="flex items-center justify-center">
          <p>Carregando...</p>
        </div>
      </div>
    )
  }

  if (error || !tournament) {
    return (
      <div className="min-h-screen bg-gray-50">
        <div className="flex items-center justify-center">
          <p>{error || 'Torneio não encontrado.'}</p>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
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
          <Button
            variant="outline"
            className="mt-4"
            onClick={() => navigate(`/tournaments/${id}/edit`)}
          >
            Editar torneio
          </Button>
        </div>
      </header>

      {/* Tabs */}
      <main className="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
        <Tabs defaultValue="teams" className="w-full">
          <TabsList>
            <TabsTrigger value="teams">Times</TabsTrigger>
            <TabsTrigger value="bracket">Chaveamento</TabsTrigger>
            <TabsTrigger value="ranking">Ranking</TabsTrigger>
            <TabsTrigger value="settings">Configurações</TabsTrigger>
          </TabsList>

          {/* Times */}
          <TabsContent value="teams" className="space-y-4">
            <div className="flex items-center justify-between">
              <h2 className="text-xl font-bold">Times</h2>
              <Button
                onClick={() => navigate(`/tournaments/${id}/teams/create`)}
              >
                <Plus className="mr-2 h-4 w-4" />
                Novo Time
              </Button>
            </div>

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
                        <Button
                          variant="ghost"
                          size="icon"
                          className="text-red-600"
                        >
                          <Trash2 className="h-4 w-4" />
                        </Button>
                      </CardTitle>
                    </CardHeader>
                    <CardContent>
                      <p className="text-sm text-gray-600">
                        {team.members?.length || 0} jogadores
                      </p>
                      <Button
                        variant="outline"
                        className="mt-4 w-full"
                        onClick={() =>
                          navigate(`/tournaments/${id}/teams/${team.id}`)
                        }
                      >
                        Gerenciar
                      </Button>
                    </CardContent>
                  </Card>
                ))}
              </div>
            )}
          </TabsContent>

          {/* Chaveamento */}
          <TabsContent value="bracket" className="space-y-4">
            <div className="flex items-center justify-between">
              <h2 className="text-xl font-bold">Chaveamento</h2>
              <Button variant={bracket ? 'outline' : 'default'}>
                {bracket ? 'Regenerar' : 'Gerar Chaveamento'}
              </Button>
            </div>

            {bracket ? (
              <Card>
                <CardContent className="pt-6">
                  <p className="text-gray-600">Bracket será exibido aqui</p>
                </CardContent>
              </Card>
            ) : (
              <Card>
                <CardContent className="pt-6 text-center">
                  <p className="text-gray-500">
                    Cadastre times para gerar o chaveamento
                  </p>
                </CardContent>
              </Card>
            )}
          </TabsContent>

          {/* Ranking */}
          <TabsContent value="ranking" className="space-y-4">
            <h2 className="text-xl font-bold">Ranking</h2>

            {ranking.length === 0 ? (
              <Card>
                <CardContent className="pt-6 text-center">
                  <p className="text-gray-500">Sem dados ainda</p>
                </CardContent>
              </Card>
            ) : (
              <Card>
                <CardContent className="pt-6">
                  <table className="w-full text-sm">
                    <thead>
                      <tr className="border-b">
                        <th className="py-2 text-left">Posição</th>
                        <th className="py-2 text-left">Time</th>
                        <th className="py-2 text-center">Vitórias</th>
                        <th className="py-2 text-center">Derrotas</th>
                        <th className="py-2 text-right">Pontos</th>
                      </tr>
                    </thead>
                    <tbody>
                      {ranking.map((row) => (
                        <tr key={row.teamId} className="border-b">
                          <td className="py-2">{row.position}</td>
                          <td className="py-2">{row.teamName}</td>
                          <td className="py-2 text-center">{row.wins}</td>
                          <td className="py-2 text-center">{row.losses}</td>
                          <td className="py-2 text-right font-bold">
                            {row.points}
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </CardContent>
              </Card>
            )}
          </TabsContent>

          {/* Settings */}
          <TabsContent value="settings" className="space-y-4">
            <h2 className="text-xl font-bold">Configurações</h2>
            <Card>
              <CardContent className="pt-6">
                <div className="space-y-2 text-sm text-gray-600">
                  <p>
                    Visibilidade:{' '}
                    {tournament.visibility === 'PUBLIC' ? 'Público' : 'Privado'}
                  </p>
                  <p>Limite de times: {tournament.teamCountLimit}</p>
                </div>
              </CardContent>
            </Card>
          </TabsContent>
        </Tabs>
      </main>
    </div>
  )
}
