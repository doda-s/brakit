import React from 'react'
import { useParams } from 'react-router-dom'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'

type MatchInfo = {
  id: string
  team1Name: string
  team2Name: string
}

export const ArbiterPanelPage: React.FC = () => {
  const { id } = useParams<{ id: string }>()
  const [matches] = React.useState<MatchInfo[]>([])
  const [selectedMatch, setSelectedMatch] = React.useState<MatchInfo | null>(
    null,
  )
  const [team1Score, setTeam1Score] = React.useState('0')
  const [team2Score, setTeam2Score] = React.useState('0')

  React.useEffect(() => {
    // TODO: Carregar partidas do torneio
    console.log('Carregando partidas:', id)
  }, [id])

  const handleScoreChange = async () => {
    // TODO: Atualizar placar via API
    console.log({
      matchId: selectedMatch?.id,
      team1Score: parseInt(team1Score),
      team2Score: parseInt(team2Score),
    })
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <header className="border-b border-gray-200 bg-white">
        <div className="mx-auto max-w-7xl px-4 py-4 sm:px-6 lg:px-8">
          <h1 className="text-3xl font-bold tracking-tight text-gray-900">
            Painel de Arbitragem
          </h1>
        </div>
      </header>

      <main className="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
        <div className="grid gap-8 lg:grid-cols-3">
          {/* Lista de Partidas */}
          <div className="lg:col-span-1">
            <Card>
              <CardHeader>
                <CardTitle>Partidas</CardTitle>
              </CardHeader>
              <CardContent className="space-y-2">
                {matches.length === 0 ? (
                  <p className="text-sm text-gray-500">
                    Nenhuma partida disponível
                  </p>
                ) : (
                  matches.map((match) => (
                    <button
                      key={match.id}
                      onClick={() => setSelectedMatch(match)}
                      className={`w-full rounded-lg px-3 py-2 text-left text-sm transition ${
                        selectedMatch?.id === match.id
                          ? 'bg-blue-600 text-white'
                          : 'bg-gray-100 text-gray-900 hover:bg-gray-200'
                      }`}
                    >
                      <div className="font-semibold">{match.team1Name}</div>
                      <div className="text-xs">vs</div>
                      <div className="font-semibold">{match.team2Name}</div>
                    </button>
                  ))
                )}
              </CardContent>
            </Card>
          </div>

          {/* Editor de Placar */}
          <div className="lg:col-span-2">
            {selectedMatch ? (
              <Card>
                <CardHeader>
                  <CardTitle>Gerenciar Placar</CardTitle>
                </CardHeader>
                <CardContent className="space-y-6">
                  <div className="grid gap-6 sm:grid-cols-2">
                    {/* Time 1 */}
                    <div className="space-y-4">
                      <h3 className="text-lg font-semibold">
                        {selectedMatch.team1Name}
                      </h3>
                      <div className="flex items-end gap-2">
                        <Input
                          type="number"
                          min="0"
                          value={team1Score}
                          onChange={(e) => setTeam1Score(e.target.value)}
                          className="flex-1 text-center text-4xl"
                        />
                      </div>
                      <div className="flex gap-2">
                        <Button
                          variant="outline"
                          onClick={() =>
                            setTeam1Score((prev) =>
                              String(Math.max(0, parseInt(prev) - 1)),
                            )
                          }
                        >
                          -
                        </Button>
                        <Button
                          onClick={() =>
                            setTeam1Score((prev) => String(parseInt(prev) + 1))
                          }
                        >
                          +
                        </Button>
                      </div>
                    </div>

                    {/* Time 2 */}
                    <div className="space-y-4">
                      <h3 className="text-lg font-semibold">
                        {selectedMatch.team2Name}
                      </h3>
                      <div className="flex items-end gap-2">
                        <Input
                          type="number"
                          min="0"
                          value={team2Score}
                          onChange={(e) => setTeam2Score(e.target.value)}
                          className="flex-1 text-center text-4xl"
                        />
                      </div>
                      <div className="flex gap-2">
                        <Button
                          variant="outline"
                          onClick={() =>
                            setTeam2Score((prev) =>
                              String(Math.max(0, parseInt(prev) - 1)),
                            )
                          }
                        >
                          -
                        </Button>
                        <Button
                          onClick={() =>
                            setTeam2Score((prev) => String(parseInt(prev) + 1))
                          }
                        >
                          +
                        </Button>
                      </div>
                    </div>
                  </div>

                  <div className="space-y-2 border-t pt-6">
                    <Button onClick={handleScoreChange} className="w-full">
                      Atualizar Placar
                    </Button>
                    <Button variant="destructive" className="w-full">
                      Desqualificar Time
                    </Button>
                  </div>
                </CardContent>
              </Card>
            ) : (
              <Card>
                <CardContent className="flex items-center justify-center py-12">
                  <p className="text-gray-500">
                    Selecione uma partida para gerenciar
                  </p>
                </CardContent>
              </Card>
            )}
          </div>
        </div>
      </main>
    </div>
  )
}
