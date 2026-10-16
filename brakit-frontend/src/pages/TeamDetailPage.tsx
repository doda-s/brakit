import React from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { ArrowLeft, Check, Pencil, Trash2, X } from 'lucide-react'
import { Team, TeamMember, Tournament } from '@/types'
import {
  teamMemberService,
  teamService,
  tournamentService,
} from '@/lib/services'
import { getErrorMessage } from '@/lib/errors'
import { useAuth } from '@/hooks/useAuth'

export const TeamDetailPage: React.FC = () => {
  const { id, teamId } = useParams<{ id: string; teamId: string }>()
  const navigate = useNavigate()
  const { user } = useAuth()
  const [tournament, setTournament] = React.useState<Tournament | null>(null)
  const [team, setTeam] = React.useState<Team | null>(null)
  const [members, setMembers] = React.useState<TeamMember[]>([])
  const [isLoading, setIsLoading] = React.useState(true)
  const [error, setError] = React.useState('')
  const [actionError, setActionError] = React.useState('')

  const [teamName, setTeamName] = React.useState('')
  const [isRenaming, setIsRenaming] = React.useState(false)

  const [newMember, setNewMember] = React.useState({ name: '', role: '' })
  const [isAdding, setIsAdding] = React.useState(false)

  const [editingId, setEditingId] = React.useState<number | null>(null)
  const [editDraft, setEditDraft] = React.useState({ name: '', role: '' })

  const isOwner = !!user && !!tournament && tournament.ownerId === user.id

  React.useEffect(() => {
    if (!id || !teamId) {
      return
    }

    let isMounted = true

    const load = async () => {
      setIsLoading(true)
      setError('')

      try {
        const [tournamentRes, teamRes, membersRes] = await Promise.all([
          tournamentService.getById(id),
          teamService.getById(id, teamId),
          teamMemberService.getByTeam(id, teamId),
        ])
        if (isMounted) {
          setTournament(tournamentRes.data)
          setTeam(teamRes.data)
          setTeamName(teamRes.data.name)
          setMembers(membersRes.data)
        }
      } catch (err) {
        if (isMounted) {
          setError(getErrorMessage(err, 'Não foi possível carregar o time.'))
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
  }, [id, teamId])

  const handleRename = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!id || !teamId) return
    setIsRenaming(true)
    setActionError('')
    try {
      const { data } = await teamService.update(id, teamId, {
        name: teamName.trim(),
      })
      setTeam(data)
      setTeamName(data.name)
    } catch (err) {
      setActionError(getErrorMessage(err, 'Não foi possível renomear o time.'))
    } finally {
      setIsRenaming(false)
    }
  }

  const handleAddMember = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!id || !teamId) return
    setIsAdding(true)
    setActionError('')
    try {
      const role = newMember.role.trim()
      const { data } = await teamMemberService.add(id, teamId, {
        name: newMember.name.trim(),
        ...(role ? { role } : {}),
      })
      setMembers((current) => [...current, data])
      setNewMember({ name: '', role: '' })
    } catch (err) {
      setActionError(getErrorMessage(err, 'Não foi possível adicionar o integrante.'))
    } finally {
      setIsAdding(false)
    }
  }

  const startEdit = (member: TeamMember) => {
    setEditingId(member.id)
    setEditDraft({ name: member.name, role: member.role ?? '' })
  }

  const handleSaveEdit = async (member: TeamMember) => {
    if (!id || !teamId) return
    setActionError('')
    try {
      const role = editDraft.role.trim()
      const { data } = await teamMemberService.update(id, teamId, member.id, {
        name: editDraft.name.trim(),
        ...(role ? { role } : {}),
      })
      setMembers((current) => current.map((m) => (m.id === data.id ? data : m)))
      setEditingId(null)
    } catch (err) {
      setActionError(getErrorMessage(err, 'Não foi possível atualizar o integrante.'))
    }
  }

  const handleRemoveMember = async (member: TeamMember) => {
    if (!id || !teamId || !window.confirm(`Remover "${member.name}"?`)) return
    setActionError('')
    try {
      await teamMemberService.remove(id, teamId, member.id)
      setMembers((current) => current.filter((m) => m.id !== member.id))
    } catch (err) {
      setActionError(getErrorMessage(err, 'Não foi possível remover o integrante.'))
    }
  }

  const handleDeleteTeam = async () => {
    if (!id || !teamId || !team) return
    if (!window.confirm(`Excluir o time "${team.name}"?`)) return
    setActionError('')
    try {
      await teamService.delete(id, teamId)
      navigate(`/tournaments/${id}`)
    } catch (err) {
      setActionError(getErrorMessage(err, 'Não foi possível excluir o time.'))
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

  if (error || !team) {
    return (
      <div className="min-h-screen bg-gray-50">
        <div className="flex flex-col items-center justify-center gap-4 py-20">
          <p>{error || 'Time não encontrado.'}</p>
          <Button variant="outline" onClick={() => navigate(`/tournaments/${id}`)}>
            Voltar
          </Button>
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
            {team.name}
          </h1>
          {tournament && (
            <p className="mt-1 text-sm text-gray-500">{tournament.name}</p>
          )}
        </div>
      </header>

      <main className="mx-auto max-w-4xl space-y-4 px-4 py-8 sm:px-6 lg:px-8">
        {actionError && (
          <div className="rounded-md bg-red-50 px-4 py-3 text-sm text-red-700">
            {actionError}
          </div>
        )}

        <div className="grid gap-8 lg:grid-cols-3">
          {isOwner && (
            <div className="space-y-6 lg:col-span-1">
              <Card>
                <CardHeader>
                  <CardTitle className="text-lg">Nome do time</CardTitle>
                </CardHeader>
                <CardContent>
                  <form onSubmit={handleRename} className="space-y-4">
                    <Input
                      aria-label="Nome do time"
                      value={teamName}
                      maxLength={255}
                      onChange={(e) => setTeamName(e.target.value)}
                      required
                    />
                    <Button
                      type="submit"
                      className="w-full"
                      disabled={isRenaming || teamName.trim() === team.name}
                    >
                      {isRenaming ? 'Salvando...' : 'Renomear'}
                    </Button>
                  </form>
                </CardContent>
              </Card>

              <Card>
                <CardHeader>
                  <CardTitle className="text-lg">Adicionar Integrante</CardTitle>
                </CardHeader>
                <CardContent>
                  <form onSubmit={handleAddMember} className="space-y-4">
                    <div className="space-y-2">
                      <Label htmlFor="name">Nome</Label>
                      <Input
                        id="name"
                        placeholder="Nome do integrante"
                        value={newMember.name}
                        maxLength={255}
                        onChange={(e) =>
                          setNewMember({ ...newMember, name: e.target.value })
                        }
                        required
                      />
                    </div>

                    <div className="space-y-2">
                      <Label htmlFor="role">Função (opcional)</Label>
                      <Input
                        id="role"
                        placeholder="Ex: Zagueiro"
                        value={newMember.role}
                        maxLength={255}
                        onChange={(e) =>
                          setNewMember({ ...newMember, role: e.target.value })
                        }
                      />
                    </div>

                    <Button type="submit" className="w-full" disabled={isAdding}>
                      {isAdding ? 'Adicionando...' : 'Adicionar'}
                    </Button>
                  </form>
                </CardContent>
              </Card>

              <Button
                variant="outline"
                className="w-full text-red-600"
                onClick={handleDeleteTeam}
              >
                Excluir time
              </Button>
            </div>
          )}

          <div className={isOwner ? 'lg:col-span-2' : 'lg:col-span-3'}>
            <Card>
              <CardHeader>
                <CardTitle>Integrantes ({members.length})</CardTitle>
              </CardHeader>
              <CardContent>
                {members.length === 0 ? (
                  <p className="text-center text-gray-500">
                    Nenhum integrante cadastrado
                  </p>
                ) : (
                  <div className="space-y-2">
                    {members.map((member) => (
                      <div
                        key={member.id}
                        className="flex items-center justify-between gap-2 rounded-lg bg-gray-50 p-3"
                      >
                        {editingId === member.id ? (
                          <div className="flex flex-1 gap-2">
                            <Input
                              aria-label="Nome do integrante"
                              value={editDraft.name}
                              maxLength={255}
                              onChange={(e) =>
                                setEditDraft({ ...editDraft, name: e.target.value })
                              }
                            />
                            <Input
                              aria-label="Função do integrante"
                              placeholder="Função"
                              value={editDraft.role}
                              maxLength={255}
                              onChange={(e) =>
                                setEditDraft({ ...editDraft, role: e.target.value })
                              }
                            />
                          </div>
                        ) : (
                          <div>
                            <h4 className="font-semibold">{member.name}</h4>
                            {member.role && (
                              <p className="text-sm text-gray-600">{member.role}</p>
                            )}
                          </div>
                        )}

                        {isOwner && (
                          <div className="flex gap-1">
                            {editingId === member.id ? (
                              <>
                                <Button
                                  variant="ghost"
                                  size="icon"
                                  title="Salvar"
                                  disabled={!editDraft.name.trim()}
                                  onClick={() => handleSaveEdit(member)}
                                >
                                  <Check className="h-4 w-4" />
                                </Button>
                                <Button
                                  variant="ghost"
                                  size="icon"
                                  title="Cancelar"
                                  onClick={() => setEditingId(null)}
                                >
                                  <X className="h-4 w-4" />
                                </Button>
                              </>
                            ) : (
                              <>
                                <Button
                                  variant="ghost"
                                  size="icon"
                                  title="Editar"
                                  onClick={() => startEdit(member)}
                                >
                                  <Pencil className="h-4 w-4" />
                                </Button>
                                <Button
                                  variant="ghost"
                                  size="icon"
                                  className="text-red-600"
                                  title="Remover"
                                  onClick={() => handleRemoveMember(member)}
                                >
                                  <Trash2 className="h-4 w-4" />
                                </Button>
                              </>
                            )}
                          </div>
                        )}
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
