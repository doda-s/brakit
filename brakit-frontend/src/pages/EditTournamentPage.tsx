import React from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { ArrowLeft } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Textarea } from '@/components/ui/textarea'
import { tournamentService } from '@/lib/services'
import { Tournament, TournamentVisibility } from '@/types'

export const EditTournamentPage: React.FC = () => {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const [isLoading, setIsLoading] = React.useState(true)
  const [isSaving, setIsSaving] = React.useState(false)
  const [error, setError] = React.useState('')
  const [form, setForm] = React.useState({
    name: '',
    description: '',
    visibility: 'PUBLIC' as TournamentVisibility,
    teamCountLimit: '8',
  })

  React.useEffect(() => {
    if (!id) {
      setError('Torneio inválido.')
      setIsLoading(false)
      return
    }

    let isMounted = true

    const loadTournament = async () => {
      setIsLoading(true)
      setError('')

      try {
        const { data } = await tournamentService.getById(id)
        if (!isMounted) {
          return
        }

        setForm({
          name: data.name,
          description: data.description,
          visibility: data.visibility,
          teamCountLimit: String(data.teamCountLimit),
        })
      } catch {
        if (isMounted) {
          setError('Não foi possível carregar o torneio para edição.')
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

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault()
    if (!id) {
      return
    }

    setIsSaving(true)
    setError('')

    try {
      await tournamentService.update(id, {
        name: form.name,
        description: form.description,
        visibility: form.visibility,
        teamCountLimit: Number(form.teamCountLimit),
      })
      navigate(`/tournaments/${id}`)
    } catch {
      setError('Não foi possível salvar as alterações.')
    } finally {
      setIsSaving(false)
    }
  }

  if (isLoading) {
    return (
      <div className="min-h-screen bg-gray-50">
        <div className="flex items-center justify-center py-20">
          <p>Carregando edição...</p>
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
            Editar Torneio
          </h1>
        </div>
      </header>

      <main className="mx-auto max-w-2xl px-4 py-8 sm:px-6 lg:px-8">
        <Card>
          <CardHeader>
            <CardTitle>Atualizar dados do torneio</CardTitle>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleSubmit} className="space-y-6">
              {error && (
                <div className="rounded-md bg-red-50 px-4 py-3 text-sm text-red-700">
                  {error}
                </div>
              )}

              <div className="space-y-2">
                <Label htmlFor="name">Nome do torneio *</Label>
                <Input
                  id="name"
                  value={form.name}
                  onChange={(event) =>
                    setForm((current) => ({ ...current, name: event.target.value }))
                  }
                  required
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="description">Descrição</Label>
                <Textarea
                  id="description"
                  rows={4}
                  value={form.description}
                  onChange={(event) =>
                    setForm((current) => ({
                      ...current,
                      description: event.target.value,
                    }))
                  }
                />
              </div>

              <div className="grid grid-cols-1 gap-6 sm:grid-cols-2">
                <div className="space-y-2">
                  <Label htmlFor="teamCountLimit">Máximo de times</Label>
                  <Input
                    id="teamCountLimit"
                    type="number"
                    min="2"
                    value={form.teamCountLimit}
                    onChange={(event) =>
                      setForm((current) => ({
                        ...current,
                        teamCountLimit: event.target.value,
                      }))
                    }
                    required
                  />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="visibility">Visibilidade</Label>
                  <select
                    id="visibility"
                    value={form.visibility}
                    onChange={(event) =>
                      setForm((current) => ({
                        ...current,
                        visibility: event.target.value as TournamentVisibility,
                      }))
                    }
                    className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
                  >
                    <option value="PUBLIC">Público</option>
                    <option value="PRIVATE">Privado</option>
                  </select>
                </div>
              </div>

              <div className="flex gap-3 pt-2">
                <Button type="submit" className="flex-1" disabled={isSaving}>
                  {isSaving ? 'Salvando...' : 'Salvar alterações'}
                </Button>
                <Button
                  type="button"
                  variant="outline"
                  onClick={() => navigate(`/tournaments/${id}`)}
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