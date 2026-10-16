import React from 'react'
import { useAuth } from '@/hooks/useAuth'
import { useNavigate } from 'react-router-dom'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Plus, Settings, LogOut } from 'lucide-react'
import { Tournament } from '@/types'
import { tournamentService } from '@/lib/services'
import { getErrorMessage } from '@/lib/errors'

const PAGE_SIZE = 12

export const DashboardPage: React.FC = () => {
  const { user, isAuthenticated, logout } = useAuth()
  const navigate = useNavigate()
  const [tournaments, setTournaments] = React.useState<Tournament[]>([])
  const [page, setPage] = React.useState(0)
  const [totalPages, setTotalPages] = React.useState(0)
  const [isLoading, setIsLoading] = React.useState(true)
  const [error, setError] = React.useState('')

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  React.useEffect(() => {
    let isMounted = true

    const loadTournaments = async () => {
      setIsLoading(true)
      setError('')

      try {
        const { data } = await tournamentService.getAll(page, PAGE_SIZE)
        if (isMounted) {
          setTournaments(data.content)
          setTotalPages(data.totalPages)
        }
      } catch (err) {
        if (isMounted) {
          setError(getErrorMessage(err, 'Não foi possível carregar os torneios.'))
        }
      } finally {
        if (isMounted) {
          setIsLoading(false)
        }
      }
    }

    loadTournaments()

    return () => {
      isMounted = false
    }
  }, [page])

  const isOwner = (tournament: Tournament) =>
    !!user && tournament.ownerId === user.id

  return (
    <div className="min-h-screen bg-gray-50">
      <header className="border-b border-gray-200 bg-white">
        <div className="mx-auto max-w-7xl px-4 py-4 sm:px-6 lg:px-8">
          <div className="flex items-center justify-between">
            <div>
              <h1 className="text-3xl font-bold tracking-tight text-gray-900">
                BrakIt
              </h1>
              <p className="mt-1 text-sm text-gray-500">
                Gerenciador de Torneios
              </p>
            </div>
            <div className="flex items-center gap-4">
              {isAuthenticated ? (
                <>
                  <span className="text-sm text-gray-600">{user?.name}</span>
                  <Button
                    variant="ghost"
                    size="icon"
                    onClick={handleLogout}
                    title="Sair"
                  >
                    <LogOut className="h-4 w-4" />
                  </Button>
                </>
              ) : (
                <>
                  <Button variant="ghost" onClick={() => navigate('/login')}>
                    Entrar
                  </Button>
                  <Button onClick={() => navigate('/signup')}>Cadastrar</Button>
                </>
              )}
            </div>
          </div>
        </div>
      </header>

      <main className="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
        <div className="mb-8 flex items-center justify-between">
          <div>
            <h2 className="text-2xl font-bold text-gray-900">Torneios</h2>
            <p className="mt-1 text-gray-600">
              {isAuthenticated
                ? 'Seus torneios e os torneios públicos'
                : 'Torneios públicos'}
            </p>
          </div>
          {isAuthenticated && (
            <Button
              onClick={() => navigate('/tournaments/create')}
              className="gap-2"
            >
              <Plus className="h-4 w-4" />
              Novo Torneio
            </Button>
          )}
        </div>

        {isLoading ? (
          <Card>
            <CardContent className="pt-6 text-center text-gray-500">
              Carregando torneios...
            </CardContent>
          </Card>
        ) : error ? (
          <Card>
            <CardContent className="pt-6 text-center text-red-600">
              {error}
            </CardContent>
          </Card>
        ) : tournaments.length === 0 ? (
          <Card>
            <CardContent className="pt-6 text-center">
              <p className="text-gray-500">Nenhum torneio encontrado</p>
              {isAuthenticated && (
                <Button
                  onClick={() => navigate('/tournaments/create')}
                  variant="outline"
                  className="mt-4"
                >
                  Criar primeiro torneio
                </Button>
              )}
            </CardContent>
          </Card>
        ) : (
          <>
            <div className="grid grid-cols-1 gap-6 md:grid-cols-2 lg:grid-cols-3">
              {tournaments.map((tournament) => (
                <Card key={tournament.id}>
                  <CardHeader>
                    <CardTitle>{tournament.name}</CardTitle>
                  </CardHeader>
                  <CardContent>
                    <p className="text-sm text-gray-600">
                      {tournament.description}
                    </p>
                    <p className="mt-2 text-xs uppercase tracking-wide text-gray-400">
                      {tournament.visibility === 'PUBLIC' ? 'Público' : 'Privado'}{' '}
                      - limite de {tournament.teamCountLimit} times
                      {isOwner(tournament) ? ' - seu torneio' : ''}
                    </p>
                    <div className="mt-4 flex gap-2">
                      <Button
                        variant="outline"
                        size="sm"
                        onClick={() => navigate(`/tournaments/${tournament.id}`)}
                      >
                        Abrir
                      </Button>
                      {isOwner(tournament) && (
                        <Button
                          variant="ghost"
                          size="sm"
                          title="Editar torneio"
                          onClick={() =>
                            navigate(`/tournaments/${tournament.id}/edit`)
                          }
                        >
                          <Settings className="h-4 w-4" />
                        </Button>
                      )}
                    </div>
                  </CardContent>
                </Card>
              ))}
            </div>

            {totalPages > 1 && (
              <div className="mt-8 flex items-center justify-center gap-4">
                <Button
                  variant="outline"
                  size="sm"
                  disabled={page === 0}
                  onClick={() => setPage((p) => p - 1)}
                >
                  Anterior
                </Button>
                <span className="text-sm text-gray-600">
                  Página {page + 1} de {totalPages}
                </span>
                <Button
                  variant="outline"
                  size="sm"
                  disabled={page + 1 >= totalPages}
                  onClick={() => setPage((p) => p + 1)}
                >
                  Próxima
                </Button>
              </div>
            )}
          </>
        )}
      </main>
    </div>
  )
}
