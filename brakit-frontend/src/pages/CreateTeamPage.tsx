import React from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { ArrowLeft, Plus, Trash2 } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { teamService } from '@/lib/services'
import { getErrorMessage } from '@/lib/errors'

const MAX_MEMBERS = 20

type MemberDraft = { name: string; role: string }

export const CreateTeamPage: React.FC = () => {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const [name, setName] = React.useState('')
  const [members, setMembers] = React.useState<MemberDraft[]>([])
  const [isSaving, setIsSaving] = React.useState(false)
  const [error, setError] = React.useState('')

  const updateMember = (index: number, patch: Partial<MemberDraft>) =>
    setMembers((current) =>
      current.map((m, i) => (i === index ? { ...m, ...patch } : m)),
    )

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault()
    if (!id) {
      return
    }
    setIsSaving(true)
    setError('')

    try {
      await teamService.create(id, {
        name: name.trim(),
        members: members.map((m) => ({
          name: m.name.trim(),
          // O backend rejeita role em branco; omitimos quando vazio
          ...(m.role.trim() ? { role: m.role.trim() } : {}),
        })),
      })
      navigate(`/tournaments/${id}`)
    } catch (err) {
      setError(getErrorMessage(err, 'Não foi possível criar o time.'))
    } finally {
      setIsSaving(false)
    }
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
            Novo Time
          </h1>
        </div>
      </header>

      <main className="mx-auto max-w-2xl px-4 py-8 sm:px-6 lg:px-8">
        <Card>
          <CardHeader>
            <CardTitle>Dados do time</CardTitle>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleSubmit} className="space-y-6">
              {error && (
                <div className="rounded-md bg-red-50 px-4 py-3 text-sm text-red-700">
                  {error}
                </div>
              )}

              <div className="space-y-2">
                <Label htmlFor="name">Nome do time *</Label>
                <Input
                  id="name"
                  value={name}
                  maxLength={255}
                  onChange={(e) => setName(e.target.value)}
                  required
                />
              </div>

              <div className="space-y-3">
                <div className="flex items-center justify-between">
                  <Label>
                    Integrantes ({members.length}/{MAX_MEMBERS})
                  </Label>
                  <Button
                    type="button"
                    variant="outline"
                    size="sm"
                    disabled={members.length >= MAX_MEMBERS}
                    onClick={() =>
                      setMembers((current) => [...current, { name: '', role: '' }])
                    }
                  >
                    <Plus className="mr-1 h-4 w-4" />
                    Adicionar
                  </Button>
                </div>

                {members.map((member, index) => (
                  <div key={index} className="flex gap-2">
                    <Input
                      aria-label={`Nome do integrante ${index + 1}`}
                      placeholder="Nome"
                      value={member.name}
                      maxLength={255}
                      onChange={(e) => updateMember(index, { name: e.target.value })}
                      required
                    />
                    <Input
                      aria-label={`Função do integrante ${index + 1}`}
                      placeholder="Função (opcional)"
                      value={member.role}
                      maxLength={255}
                      onChange={(e) => updateMember(index, { role: e.target.value })}
                    />
                    <Button
                      type="button"
                      variant="ghost"
                      size="icon"
                      className="text-red-600"
                      title="Remover integrante"
                      onClick={() =>
                        setMembers((current) => current.filter((_, i) => i !== index))
                      }
                    >
                      <Trash2 className="h-4 w-4" />
                    </Button>
                  </div>
                ))}
              </div>

              <div className="flex gap-3 pt-2">
                <Button type="submit" className="flex-1" disabled={isSaving}>
                  {isSaving ? 'Criando...' : 'Criar time'}
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
