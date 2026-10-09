import React from 'react'
import { useNavigate } from 'react-router-dom'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Textarea } from '@/components/ui/textarea'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { ArrowLeft } from 'lucide-react'
import { tournamentService } from '@/lib/services'

export const CreateTournamentPage: React.FC = () => {
  const [name, setName] = React.useState('')
  const [description, setDescription] = React.useState('')
  const [visibility, setVisibility] = React.useState<'PUBLIC' | 'PRIVATE'>(
    'PUBLIC',
  )
  const [maxTeams, setMaxTeams] = React.useState('8')
  const [isLoading, setIsLoading] = React.useState(false)
  const [error, setError] = React.useState('')
  const navigate = useNavigate()

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setIsLoading(true)
    setError('')

    try {
      const { data } = await tournamentService.create({
        name,
        description,
        visibility,
        teamCountLimit: parseInt(maxTeams),
      })
      navigate(`/tournaments/${data.id}`)
    } catch {
      setError('Não foi possível criar o torneio.')
    } finally {
      setIsLoading(false)
    }
  }

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
            Novo Torneio
          </h1>
        </div>
      </header>

      <main className="mx-auto max-w-2xl px-4 py-8 sm:px-6 lg:px-8">
        <Card>
          <CardHeader>
            <CardTitle>Configurar Torneio</CardTitle>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleSubmit} className="space-y-6">
              {error && (
                <div className="rounded-md bg-red-50 px-4 py-3 text-sm text-red-700">
                  {error}
                </div>
              )}

              <div className="space-y-2">
                <Label htmlFor="name">Nome do Torneio *</Label>
                <Input
                  id="name"
                  type="text"
                  placeholder="Ex: Campeonato de Futsal 2024"
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  required
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="description">Descrição</Label>
                <Textarea
                  id="description"
                  placeholder="Descreva o torneio..."
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                  rows={4}
                />
              </div>

              <div className="grid grid-cols-1 gap-6 sm:grid-cols-2">
                <div className="space-y-2">
                  <Label htmlFor="maxTeams">Máximo de Times *</Label>
                  <Input
                    id="maxTeams"
                    type="number"
                    min="2"
                    value={maxTeams}
                    onChange={(e) => setMaxTeams(e.target.value)}
                    required
                  />
                </div>
              </div>

              <div className="space-y-2">
                <Label htmlFor="visibility">Visibilidade</Label>
                <select
                  id="visibility"
                  value={visibility}
                  onChange={(e) =>
                    setVisibility(e.target.value as 'PUBLIC' | 'PRIVATE')
                  }
                  className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
                >
                  <option value="PUBLIC">Público</option>
                  <option value="PRIVATE">Privado</option>
                </select>
              </div>

              <div className="flex gap-3 pt-4">
                <Button type="submit" disabled={isLoading} className="flex-1">
                  {isLoading ? 'Criando...' : 'Criar Torneio'}
                </Button>
                <Button
                  type="button"
                  variant="outline"
                  onClick={() => navigate('/dashboard')}
                >
                  Cancelar
                </Button>
              </div>
            </form>
          </CardContent>
        </Card>
      </main>
    </div>
  )
}
