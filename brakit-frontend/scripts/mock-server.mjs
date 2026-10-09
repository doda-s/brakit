// Mock API server for BrakIt frontend.
// Pure Node.js (no external dependencies) so it runs without installing anything.
// Implements the endpoints consumed by src/lib/services.ts.

import { createServer } from 'node:http'

const PORT = Number(process.env.MOCK_PORT || 8080)

// ----- In-memory data store -----
let nextId = 1
const genId = () => nextId++

const users = [
  { id: genId(), name: 'Demo User', email: 'demo@brakit.com', password: '123456' },
]

const tournaments = [
  {
    id: genId(),
    name: 'Campeonato de Exemplo',
    description: 'Torneio de demonstração criado pelo mock',
    visibility: 'PUBLIC',
    teamCountLimit: 8,
  },
]

// tournamentId -> teams[]; team has members[]
const teamsByTournament = new Map()
// tournamentId -> matches[]
const matchesByTournament = new Map()

// ----- Helpers -----
const TOKEN_PREFIX = 'mock-token-'

function send(res, status, body) {
  const payload = body === undefined ? '' : JSON.stringify(body)
  res.writeHead(status, {
    'Content-Type': 'application/json',
    'Access-Control-Allow-Origin': '*',
    'Access-Control-Allow-Methods': 'GET,POST,PUT,PATCH,DELETE,OPTIONS',
    'Access-Control-Allow-Headers': 'Content-Type,Authorization',
  })
  res.end(payload)
}

function readBody(req) {
  return new Promise((resolve) => {
    let data = ''
    req.on('data', (chunk) => (data += chunk))
    req.on('end', () => {
      if (!data) return resolve({})
      try {
        resolve(JSON.parse(data))
      } catch {
        resolve({})
      }
    })
  })
}

function userFromAuth(req) {
  const auth = req.headers['authorization'] || ''
  if (!auth.startsWith('Bearer ' + TOKEN_PREFIX)) return null
  const id = Number(auth.slice(('Bearer ' + TOKEN_PREFIX).length))
  return users.find((u) => u.id === id) || null
}

function publicUser(u) {
  return { id: u.id, name: u.name, email: u.email }
}

function getTeams(tid) {
  if (!teamsByTournament.has(tid)) teamsByTournament.set(tid, [])
  return teamsByTournament.get(tid)
}

function getMatches(tid) {
  if (!matchesByTournament.has(tid)) matchesByTournament.set(tid, [])
  return matchesByTournament.get(tid)
}

// Match a path against a pattern like /tournaments/:id/teams/:teamId
function match(pattern, path) {
  const p = pattern.split('/').filter(Boolean)
  const s = path.split('/').filter(Boolean)
  if (p.length !== s.length) return null
  const params = {}
  for (let i = 0; i < p.length; i++) {
    if (p[i].startsWith(':')) params[p[i].slice(1)] = decodeURIComponent(s[i])
    else if (p[i] !== s[i]) return null
  }
  return params
}

// ----- Request handler -----
const server = createServer(async (req, res) => {
  if (req.method === 'OPTIONS') return send(res, 204)

  const url = new URL(req.url, `http://localhost:${PORT}`)
  let path = url.pathname
  // Frontend uses baseURL http://localhost:8080/api
  if (path.startsWith('/api')) path = path.slice(4) || '/'

  const method = req.method
  const body = method === 'GET' || method === 'DELETE' ? {} : await readBody(req)

  // ---- Auth ----
  if (method === 'POST' && path === '/auth/login') {
    const u = users.find((x) => x.email === body.email && x.password === body.password)
    if (!u) return send(res, 401, { message: 'Credenciais inválidas' })
    return send(res, 200, {
      accessToken: TOKEN_PREFIX + u.id,
      tokenType: 'Bearer',
      expiresIn: 3600,
    })
  }

  if (method === 'POST' && path === '/auth/register') {
    if (users.some((x) => x.email === body.email)) {
      return send(res, 409, { message: 'E-mail já cadastrado' })
    }
    const u = { id: genId(), name: body.name, email: body.email, password: body.password }
    users.push(u)
    return send(res, 201, publicUser(u))
  }

  if (method === 'GET' && path === '/auth/me') {
    const u = userFromAuth(req)
    if (!u) return send(res, 401, { message: 'Não autenticado' })
    return send(res, 200, publicUser(u))
  }

  // ---- Tournaments ----
  if (method === 'GET' && path === '/tournaments') {
    const page = Number(url.searchParams.get('page') || 0)
    const size = Number(url.searchParams.get('size') || 20)
    const start = page * size
    const content = tournaments.slice(start, start + size)
    return send(res, 200, {
      content,
      page,
      size,
      totalElements: tournaments.length,
      totalPages: Math.max(1, Math.ceil(tournaments.length / size)),
    })
  }

  if (method === 'POST' && path === '/tournaments') {
    const t = {
      id: genId(),
      name: body.name,
      description: body.description || '',
      visibility: body.visibility || 'PUBLIC',
      teamCountLimit: body.teamCountLimit || 8,
    }
    tournaments.push(t)
    return send(res, 201, t)
  }

  let m
  if ((m = match('/tournaments/:id', path))) {
    const t = tournaments.find((x) => String(x.id) === m.id)
    if (method === 'GET') {
      if (!t) return send(res, 404, { message: 'Torneio não encontrado' })
      return send(res, 200, t)
    }
    if (method === 'PUT') {
      if (!t) return send(res, 404, { message: 'Torneio não encontrado' })
      Object.assign(t, {
        name: body.name ?? t.name,
        description: body.description ?? t.description,
        visibility: body.visibility ?? t.visibility,
        teamCountLimit: body.teamCountLimit ?? t.teamCountLimit,
      })
      return send(res, 200, t)
    }
    if (method === 'DELETE') {
      const idx = tournaments.findIndex((x) => String(x.id) === m.id)
      if (idx >= 0) tournaments.splice(idx, 1)
      return send(res, 204)
    }
  }

  // ---- Bracket ----
  if ((m = match('/tournaments/:id/bracket', path))) {
    const matches = getMatches(m.id)
    if (method === 'POST') {
      const teams = getTeams(m.id)
      matches.length = 0
      for (let i = 0; i + 1 < teams.length; i += 2) {
        matches.push({
          id: String(genId()),
          roundNumber: 1,
          team1Id: teams[i].id,
          team2Id: teams[i + 1].id,
          team1Score: 0,
          team2Score: 0,
          status: 'pending',
        })
      }
      return send(res, 200, matches)
    }
    if (method === 'GET') return send(res, 200, matches)
  }

  // ---- Teams ----
  if ((m = match('/tournaments/:id/teams', path))) {
    const teams = getTeams(m.id)
    if (method === 'GET') return send(res, 200, teams)
    if (method === 'POST') {
      const team = {
        id: String(genId()),
        tournamentId: m.id,
        name: body.name,
        members: [],
        createdAt: new Date().toISOString(),
      }
      teams.push(team)
      return send(res, 201, team)
    }
  }

  if ((m = match('/tournaments/:id/teams/:teamId', path))) {
    const teams = getTeams(m.id)
    const team = teams.find((t) => t.id === m.teamId)
    if (method === 'GET') {
      if (!team) return send(res, 404, { message: 'Time não encontrado' })
      return send(res, 200, team)
    }
    if (method === 'DELETE') {
      const idx = teams.findIndex((t) => t.id === m.teamId)
      if (idx >= 0) teams.splice(idx, 1)
      return send(res, 204)
    }
  }

  // ---- Team members ----
  if ((m = match('/tournaments/:id/teams/:teamId/members', path))) {
    const team = getTeams(m.id).find((t) => t.id === m.teamId)
    if (!team) return send(res, 404, { message: 'Time não encontrado' })
    if (method === 'GET') return send(res, 200, team.members)
    if (method === 'POST') {
      const member = {
        id: String(genId()),
        teamId: team.id,
        name: body.name,
        position: body.position || '',
        joinedAt: new Date().toISOString(),
      }
      team.members.push(member)
      return send(res, 201, member)
    }
  }

  if ((m = match('/tournaments/:id/teams/:teamId/members/:memberId', path))) {
    const team = getTeams(m.id).find((t) => t.id === m.teamId)
    if (team && method === 'DELETE') {
      const idx = team.members.findIndex((x) => x.id === m.memberId)
      if (idx >= 0) team.members.splice(idx, 1)
      return send(res, 204)
    }
  }

  // ---- Matches ----
  if ((m = match('/tournaments/:id/matches', path))) {
    if (method === 'GET') return send(res, 200, getMatches(m.id))
  }

  if ((m = match('/tournaments/:id/matches/:matchId', path))) {
    const match_ = getMatches(m.id).find((x) => x.id === m.matchId)
    if (method === 'GET') {
      if (!match_) return send(res, 404, { message: 'Partida não encontrada' })
      return send(res, 200, match_)
    }
  }

  if ((m = match('/tournaments/:id/matches/:matchId/score', path))) {
    const match_ = getMatches(m.id).find((x) => x.id === m.matchId)
    if (match_ && method === 'PATCH') {
      match_.team1Score = body.team1Score ?? match_.team1Score
      match_.team2Score = body.team2Score ?? match_.team2Score
      match_.status = 'in_progress'
      return send(res, 200, match_)
    }
  }

  if ((m = match('/tournaments/:id/matches/:matchId/winner', path))) {
    const match_ = getMatches(m.id).find((x) => x.id === m.matchId)
    if (match_ && method === 'POST') {
      match_.winnerId = body.winnerId
      match_.status = 'finished'
      match_.finishedAt = new Date().toISOString()
      return send(res, 200, match_)
    }
  }

  if ((m = match('/tournaments/:id/matches/:matchId/disqualify', path))) {
    const match_ = getMatches(m.id).find((x) => x.id === m.matchId)
    if (match_ && method === 'POST') {
      const loser = body.teamId
      match_.winnerId = loser === match_.team1Id ? match_.team2Id : match_.team1Id
      match_.status = 'finished'
      return send(res, 200, match_)
    }
  }

  // ---- Ranking ----
  if ((m = match('/tournaments/:id/ranking', path))) {
    if (method === 'GET') {
      const teams = getTeams(m.id)
      const ranking = teams.map((t, i) => ({
        teamId: t.id,
        teamName: t.name,
        wins: 0,
        losses: 0,
        points: 0,
        position: i + 1,
      }))
      return send(res, 200, ranking)
    }
  }

  return send(res, 404, { message: `Rota não encontrada: ${method} ${path}` })
})

server.listen(PORT, () => {
  console.log(`\n  Mock API BrakIt rodando em http://localhost:${PORT}/api`)
  console.log('  Login de teste: demo@brakit.com / 123456\n')
})
