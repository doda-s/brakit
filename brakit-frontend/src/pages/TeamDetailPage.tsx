import React from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { ArrowLeft, Trash2 } from 'lucide-react'

type TeamInfo = {
  id: string
  name: string
}

type TeamMemberInfo = {
  id: string
  name: string
  position: string
}

export const TeamDetailPage: React.FC = () => {
  const { id, teamId } = useParams<{ id: string; teamId: string }>()
  const navigate = useNavigate()
  const [team] = React.useState<TeamInfo | null>(null)
  const [members] = React.useState<TeamMemberInfo[]>([])
  const [newMember, setNewMember] = React.useState({ name: '', position: '' })
  const [isAdding, setIsAdding] = React.useState(false)

  React.useEffect(() => {
    // TODO: Carregar dados do time
    console.log('Carregando time:', { tournamentId: id, teamId })
  }, [id, teamId])

  const handleAddMember = async (e: React.FormEvent) => {
    e.preventDefault()
    setIsAdding(true)
    try {
      // TODO: Chamar API para adicionar membro
      console.log('Adicionando membro:', newMember)
      setNewMember({ name: '', position: '' })
    } finally {
      setIsAdding(false)
    }
  }

  if (!team) {
    return (
      <div className="min-h-screen bg-gray-50">
        <div className="flex items-center justify-center">
          <p>Carregando...</p>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <header className="border-b border-gray-200 bg-white">
        <div className="mx-auto max-w-7xl px-4 py-4 sm:px-6 lg:px-8">
          <Button
            variant="ghost"
            onClick={() => navigate(`/tournaments/${id}`)}
            className="mb-4 gap-2"
          >
            <ArrowLeft className="h-4 w-4" />
            Voltar
          </Button>
          <h1 className="text-3xl font-bold tracking-tight text-gray-900">
            {team?.name}
          </h1>
        </div>
      </header>

      <main className="mx-auto max-w-4xl px-4 py-8 sm:px-6 lg:px-8">
        <div className="grid gap-8 lg:grid-cols-3">
          {/* Adicionar Membro */}
          <Card className="lg:col-span-1">
            <CardHeader>
              <CardTitle className="text-lg">Adicionar Jogador</CardTitle>
            </CardHeader>
            <CardContent>
              <form onSubmit={handleAddMember} className="space-y-4">
                <div className="space-y-2">
                  <Label htmlFor="name">Nome</Label>
                  <Input
                    id="name"
                    type="text"
                    placeholder="Nome do jogador"
                    value={newMember.name}
                    onChange={(e) =>
                      setNewMember({ ...newMember, name: e.target.value })
                    }
                    required
                  />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="position">Posição/Cargo</Label>
                  <Input
                    id="position"
                    type="text"
                    placeholder="Ex: Zagueiro"
                    value={newMember.position}
                    onChange={(e) =>
                      setNewMember({ ...newMember, position: e.target.value })
                    }
                    required
                  />
                </div>

                <Button type="submit" className="w-full" disabled={isAdding}>
                  {isAdding ? 'Adicionando...' : 'Adicionar Jogador'}
                </Button>
              </form>
            </CardContent>
          </Card>

          {/* Lista de Membros */}
          <div className="lg:col-span-2">
            <Card>
              <CardHeader>
                <CardTitle>Jogadores ({members.length})</CardTitle>
              </CardHeader>
              <CardContent>
                {members.length === 0 ? (
                  <p className="text-center text-gray-500">
                    Nenhum jogador cadastrado
                  </p>
                ) : (
                  <div className="space-y-2">
                    {members.map((member) => (
                      <div
                        key={member.id}
                        className="flex items-center justify-between rounded-lg bg-gray-50 p-3"
                      >
                        <div>
                          <h4 className="font-semibold">{member.name}</h4>
                          <p className="text-sm text-gray-600">
                            {member.position}
                          </p>
                        </div>
                        <Button
                          variant="ghost"
                          size="icon"
                          className="text-red-600"
                          onClick={() => {
                            // TODO: Remover membro
                            console.log('Removendo membro:', member.id)
                          }}
                        >
                          <Trash2 className="h-4 w-4" />
                        </Button>
                      </div>
                    ))}
                  </div>
                )}
              </CardContent>
            </Card>
          </div>
        </div>
      </main>
    </div>
  )
}
