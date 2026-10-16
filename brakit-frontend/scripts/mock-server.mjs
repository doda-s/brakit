// Mock API server for BrakIt frontend.
// Pure Node.js (no external dependencies): roda sem Java, Maven, Docker ou Postgres.
// Reproduz os endpoints, validacoes e regras de negocio do backend (brakit-api).

import { createServer } from 'node:http'

const PORT = Number(process.env.MOCK_PORT || 8080)

// ----- Armazenamento em memoria -----
let nextId = 1
const genId = () => nextId++

const users = [
  { id: genId(), name: 'Demo User', email: 'demo@brakit.com', password: '12345678' },
]

const tournaments = [
  {
    id: genId(),
    name: 'Campeonato de Exemplo',
    description: 'Torneio de demonstração criado pelo mock',
    visibility: 'PUBLIC',
    teamCountLimit: 8,
    ownerId: users[0].id,
  },
]

const teams = [] // { id, tournamentId, name }
const members = [] // { id, teamId, name, role }
// { id, tournamentId, round, position, team1Id, team2Id, team1Score, team2Score,
//   status: SCHEDULED|IN_PROGRESS|FINISHED, winnerId, resultType: SCORE|MANUAL|DISQUALIFICATION|BYE }
const matches = []
const disqualified = new Set() // ids de times desqualificados

const POINTS_PER_WIN = 3

// ----- Erros no formato ErrorResponse { status, message } -----
class ApiError extends Error {
  constructor(status, message) {
    super(message)
    this.status = status
  }
}
const notFound = (m) => new ApiError(404, m)
const conflict = (m) => new ApiError(409, m)
const badRequest = (m) => new ApiError(400, m)

// ----- Helpers HTTP -----
const TOKEN_PREFIX = 'mock-token-'

function send(res, status, body) {
  res.writeHead(status, {
    'Content-Type': 'application/json',
    'Access-Control-Allow-Origin': '*',
    'Access-Control-Allow-Methods': 'GET,POST,PUT,PATCH,DELETE,OPTIONS',
    'Access-Control-Allow-Headers': 'Content-Type,Authorization',
  })
  res.end(body === undefined ? '' : JSON.stringify(body))
}

function readBody(req) {
  return new Promise((resolve) => {
    let data = ''
    req.on('data', (chunk) => (data += chunk))
    req.on('end', () => {
      if (!data) return resolve(null)
      try {
        resolve(JSON.parse(data))
      } catch {
        resolve(null)
      }
    })
  })
}

// Usuario do token, ou null se ausente. Token invalido -> 401 sem corpo (como o framework).
function viewer(req) {
  const auth = req.headers['authorization']
  if (!auth) return null
  const prefix = 'Bearer ' + TOKEN_PREFIX
  const id = auth.startsWith(prefix) ? Number(auth.slice(prefix.length)) : NaN
  const user = users.find((u) => u.id === id)
  if (!user) throw new ApiError(401, undefined)
  return user
}

function requireUser(req) {
  const user = viewer(req)
  if (!user) throw new ApiError(401, undefined)
  return user
}

const publicUser = (u) => ({ id: u.id, name: u.name, email: u.email })
const normalizeEmail = (e) => String(e).trim().toLowerCase()

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

// ----- Validacao (mensagens "campo: msg; campo2: msg", ordenadas) -----
const blank = (v) => typeof v !== 'string' || v.trim() === ''

function validate(checks) {
  const errors = checks.filter(Boolean).sort()
  if (errors.length) throw badRequest(errors.join('; '))
}

function memberChecks(m, prefix = '') {
  return [
    blank(m?.name) && `${prefix}name: must not be blank`,
    typeof m?.name === 'string' && m.name.length > 255 && `${prefix}name: size must be between 0 and 255`,
    m?.role != null && typeof m.role === 'string' && m.role.trim() === '' && `${prefix}role: must not be blank`,
    typeof m?.role === 'string' && m.role.length > 255 && `${prefix}role: size must be between 0 and 255`,
  ]
}

const validVisibility = (v) => v === 'PUBLIC' || v === 'PRIVATE'
const positiveInt = (v) => Number.isInteger(v) && v > 0

// ----- Regras de negocio -----
const isVisibleTo = (t, user) => t.visibility === 'PUBLIC' || (user && t.ownerId === user.id)

function getVisible(id, user) {
  const t = tournaments.find((x) => String(x.id) === String(id))
  if (!t || !isVisibleTo(t, user)) throw notFound(`Tournament ${id} not found.`)
  return t
}

function getOwned(id, user) {
  const t = getVisible(id, user)
  if (t.ownerId !== user.id) throw new ApiError(403, `User is not the owner of tournament ${id}.`)
  return t
}

function getTeam(tid, teamId) {
  const team = teams.find((x) => String(x.id) === String(teamId) && String(x.tournamentId) === String(tid))
  if (!team) throw notFound(`Team ${teamId} not found.`)
  return team
}

function getMember(team, memberId) {
  const m = members.find((x) => String(x.id) === String(memberId) && x.teamId === team.id)
  if (!m) throw notFound(`Member ${memberId} not found.`)
  return m
}

const nameTaken = (tid, name, exceptId) =>
  teams.some(
    (t) => t.tournamentId === tid && t.id !== exceptId && t.name.toLowerCase() === name.trim().toLowerCase(),
  )

// ----- Chaveamento (mata-mata simples) -----
const tournamentMatches = (tid) =>
  matches.filter((x) => x.tournamentId === tid).sort((a, b) => a.round - b.round || a.position - b.position)

const hasBracket = (tid) => matches.some((x) => x.tournamentId === tid)

const teamName = (id) => teams.find((t) => t.id === id)?.name ?? null

const matchResponse = (x) => ({ ...x, team1Name: teamName(x.team1Id), team2Name: teamName(x.team2Id) })

function getMatch(tid, matchId) {
  const x = matches.find((y) => String(y.id) === String(matchId) && y.tournamentId === tid)
  if (!x) throw notFound(`Match ${matchId} not found.`)
  return x
}

// Ordem de seeds do bracket (1 vs ultimo, etc.) para distribuir os byes entre os confrontos
function seedOrder(size) {
  let order = [1]
  while (order.length < size) {
    const total = order.length * 2
    order = order.flatMap((s) => [s, total + 1 - s])
  }
  return order
}

function shuffle(list) {
  const a = [...list]
  for (let i = a.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1))
    ;[a[i], a[j]] = [a[j], a[i]]
  }
  return a
}

// Coloca o vencedor no proximo confronto e resolve desqualificacoes pendentes em cascata
function finishMatch(match, winnerId, resultType) {
  match.winnerId = winnerId
  match.resultType = resultType
  match.status = 'FINISHED'
  const next = matches.find(
    (x) => x.tournamentId === match.tournamentId && x.round === match.round + 1 && x.position === Math.floor(match.position / 2),
  )
  if (!next) return
  next[match.position % 2 === 0 ? 'team1Id' : 'team2Id'] = winnerId
  resolveDisqualified(next)
}

// Time desqualificado perde por W.O. assim que o adversario estiver definido
function resolveDisqualified(match) {
  if (match.status === 'FINISHED' || !match.team1Id || !match.team2Id) return
  const dq1 = disqualified.has(match.team1Id)
  const dq2 = disqualified.has(match.team2Id)
  if (dq1 && !dq2) finishMatch(match, match.team2Id, 'DISQUALIFICATION')
  else if (dq2 && !dq1) finishMatch(match, match.team1Id, 'DISQUALIFICATION')
}

function generateBracket(tid) {
  const list = teams.filter((t) => t.tournamentId === tid)
  if (list.length < 2) throw conflict('At least 2 teams are required to generate the bracket.')
  const size = 2 ** Math.ceil(Math.log2(list.length))
  const rounds = Math.log2(size)
  const seeded = shuffle(list)
  const order = seedOrder(size)
  const slot = (seed) => seeded[seed - 1]?.id ?? null // seed > n => bye

  for (let round = 1; round <= rounds; round++) {
    for (let position = 0; position < size / 2 ** round; position++) {
      matches.push({
        id: genId(),
        tournamentId: tid,
        round,
        position,
        team1Id: round === 1 ? slot(order[position * 2]) : null,
        team2Id: round === 1 ? slot(order[position * 2 + 1]) : null,
        team1Score: 0,
        team2Score: 0,
        status: 'SCHEDULED',
        winnerId: null,
        resultType: null,
      })
    }
  }
  // Byes: quem nao tem adversario na 1a rodada avanca direto
  for (const x of tournamentMatches(tid).filter((y) => y.round === 1)) {
    if (!x.team1Id !== !x.team2Id) finishMatch(x, x.team1Id ?? x.team2Id, 'BYE')
  }
}

function rankingFor(tid) {
  const all = tournamentMatches(tid)
  const finalMatch = all.length ? all[all.length - 1] : null
  const rows = teams
    .filter((t) => t.tournamentId === tid)
    .map((t) => {
      const played = all.filter(
        (x) => x.status === 'FINISHED' && x.resultType !== 'BYE' && (x.team1Id === t.id || x.team2Id === t.id),
      )
      const wins = played.filter((x) => x.winnerId === t.id).length
      let pointsFor = 0
      let pointsAgainst = 0
      for (const x of played) {
        const mine = x.team1Id === t.id ? x.team1Score : x.team2Score
        const theirs = x.team1Id === t.id ? x.team2Score : x.team1Score
        pointsFor += mine
        pointsAgainst += theirs
      }
      const lost = all.some(
        (x) => x.status === 'FINISHED' && x.winnerId !== t.id && (x.team1Id === t.id || x.team2Id === t.id),
      )
      let status = 'ACTIVE'
      if (finalMatch?.status === 'FINISHED' && finalMatch.winnerId === t.id) status = 'CHAMPION'
      else if (disqualified.has(t.id)) status = 'DISQUALIFIED'
      else if (lost) status = 'ELIMINATED'
      return {
        teamId: t.id,
        teamName: t.name,
        played: played.length,
        wins,
        losses: played.length - wins,
        pointsFor,
        pointsAgainst,
        scoreDiff: pointsFor - pointsAgainst,
        points: wins * POINTS_PER_WIN,
        status,
      }
    })
  // Campeao primeiro; depois pontos, saldo, pontos pro e nome
  rows.sort(
    (a, b) =>
      (b.status === 'CHAMPION') - (a.status === 'CHAMPION') ||
      b.points - a.points ||
      b.scoreDiff - a.scoreDiff ||
      b.pointsFor - a.pointsFor ||
      a.teamName.localeCompare(b.teamName),
  )
  return rows.map((r, i) => ({ position: i + 1, ...r }))
}

const bracketLocked = () =>
  conflict('Bracket already generated; teams can no longer be added or removed.')

const memberResponse = (m) => ({ id: m.id, teamId: m.teamId, name: m.name, role: m.role ?? null })

// ----- Rotas -----
async function handle(req, path, url) {
  const method = req.method
  const body = method === 'GET' || method === 'DELETE' ? null : await readBody(req)
  let m

  // ---- Auth ----
  if (method === 'POST' && path === '/auth/register') {
    validate([
      blank(body?.name) && 'name: must not be blank',
      blank(body?.email) && 'email: must not be blank',
      !blank(body?.email) && !/^[^@\s]+@[^@\s]+$/.test(body.email) && 'email: must be a well-formed email address',
      (typeof body?.password !== 'string' || body.password.length < 8 || body.password.length > 72) &&
        'password: size must be between 8 and 72',
    ])
    const email = normalizeEmail(body.email)
    if (users.some((u) => u.email === email)) throw conflict('Email already registered.')
    const u = { id: genId(), name: body.name, email, password: body.password }
    users.push(u)
    return [201, publicUser(u)]
  }

  if (method === 'POST' && path === '/auth/login') {
    validate([blank(body?.email) && 'email: must not be blank', blank(body?.password) && 'password: must not be blank'])
    const u = users.find((x) => x.email === normalizeEmail(body.email) && x.password === body.password)
    if (!u) throw new ApiError(401, 'Invalid credentials.')
    return [200, { accessToken: TOKEN_PREFIX + u.id, tokenType: 'Bearer', expiresIn: 43200 }]
  }

  if (method === 'GET' && path === '/auth/me') {
    return [200, publicUser(requireUser(req))]
  }

  // ---- Torneios ----
  if (path === '/tournaments') {
    if (method === 'GET') {
      const user = viewer(req)
      const page = Number(url.searchParams.get('page') ?? 0)
      const size = Number(url.searchParams.get('size') ?? 20)
      validate([
        !(page >= 0) && 'page: must be greater than or equal to 0',
        !(size >= 1) && 'size: must be greater than or equal to 1',
        size > 100 && 'size: must be less than or equal to 100',
      ])
      const visible = tournaments.filter((t) => isVisibleTo(t, user))
      return [
        200,
        {
          content: visible.slice(page * size, page * size + size),
          page,
          size,
          totalElements: visible.length,
          totalPages: Math.ceil(visible.length / size),
        },
      ]
    }
    if (method === 'POST') {
      const user = requireUser(req)
      validate([
        blank(body?.name) && 'name: must not be blank',
        blank(body?.description) && 'description: must not be blank',
        !validVisibility(body?.visibility) && 'visibility: must not be null',
        !positiveInt(body?.teamCountLimit) && 'teamCountLimit: must be greater than 0',
      ])
      const t = {
        id: genId(),
        name: body.name,
        description: body.description,
        visibility: body.visibility,
        teamCountLimit: body.teamCountLimit,
        ownerId: user.id,
      }
      tournaments.push(t)
      return [201, t]
    }
  }

  if ((m = match('/tournaments/:id', path))) {
    if (method === 'GET') return [200, getVisible(m.id, viewer(req))]

    if (method === 'PUT') {
      const user = requireUser(req)
      const t = getOwned(m.id, user)
      validate([
        body?.name != null && blank(body.name) && 'name: must not be blank',
        body?.description != null && blank(body.description) && 'description: must not be blank',
        body?.visibility != null && !validVisibility(body.visibility) && 'visibility: must be PUBLIC or PRIVATE',
        body?.teamCountLimit != null && !positiveInt(body.teamCountLimit) && 'teamCountLimit: must be greater than 0',
      ])
      const limit = body.teamCountLimit ?? t.teamCountLimit
      const count = teams.filter((x) => x.tournamentId === t.id).length
      if (count > limit) {
        throw conflict(`Team count limit ${limit} is below the current team count ${count}.`)
      }
      Object.assign(t, {
        name: body.name ?? t.name,
        description: body.description ?? t.description,
        visibility: body.visibility ?? t.visibility,
        teamCountLimit: limit,
      })
      return [200, t]
    }

    if (method === 'DELETE') {
      const t = getOwned(m.id, requireUser(req))
      for (let i = matches.length - 1; i >= 0; i--) if (matches[i].tournamentId === t.id) matches.splice(i, 1)
      for (let i = teams.length - 1; i >= 0; i--) {
        if (teams[i].tournamentId === t.id) {
          const teamId = teams[i].id
          for (let j = members.length - 1; j >= 0; j--) if (members[j].teamId === teamId) members.splice(j, 1)
          teams.splice(i, 1)
        }
      }
      tournaments.splice(tournaments.indexOf(t), 1)
      return [204]
    }
  }

  // ---- Times ----
  if ((m = match('/tournaments/:id/teams', path))) {
    if (method === 'GET') {
      const t = getVisible(m.id, viewer(req))
      return [200, teams.filter((x) => x.tournamentId === t.id)]
    }
    if (method === 'POST') {
      const user = requireUser(req)
      const t = getOwned(m.id, user)
      if (hasBracket(t.id)) throw bracketLocked()
      const list = Array.isArray(body?.members) ? body.members : []
      validate([
        blank(body?.name) && 'name: must not be blank',
        typeof body?.name === 'string' && body.name.length > 255 && 'name: size must be between 0 and 255',
        list.length > 20 && 'members: size must be between 0 and 20',
        ...list.flatMap((mem, i) => memberChecks(mem, `members[${i}].`)),
      ])
      const count = teams.filter((x) => x.tournamentId === t.id).length
      if (count >= t.teamCountLimit) throw conflict(`Tournament ${t.id} is full.`)
      if (nameTaken(t.id, body.name)) throw conflict(`Team name "${body.name}" is already taken in this tournament.`)
      const team = { id: genId(), tournamentId: t.id, name: body.name }
      teams.push(team)
      const created = list.map((mem) => {
        const row = { id: genId(), teamId: team.id, name: mem.name, role: mem.role ?? null }
        members.push(row)
        return memberResponse(row)
      })
      return [201, { ...team, members: created }]
    }
  }

  if ((m = match('/tournaments/:id/teams/:teamId', path))) {
    if (method === 'GET') {
      const t = getVisible(m.id, viewer(req))
      return [200, getTeam(t.id, m.teamId)]
    }
    if (method === 'PUT') {
      const t = getOwned(m.id, requireUser(req))
      const team = getTeam(t.id, m.teamId)
      validate([
        blank(body?.name) && 'name: must not be blank',
        typeof body?.name === 'string' && body.name.length > 255 && 'name: size must be between 0 and 255',
      ])
      if (nameTaken(t.id, body.name, team.id)) {
        throw conflict(`Team name "${body.name}" is already taken in this tournament.`)
      }
      team.name = body.name
      return [200, team]
    }
    if (method === 'DELETE') {
      const t = getOwned(m.id, requireUser(req))
      const team = getTeam(t.id, m.teamId)
      if (hasBracket(t.id)) throw bracketLocked()
      for (let j = members.length - 1; j >= 0; j--) if (members[j].teamId === team.id) members.splice(j, 1)
      teams.splice(teams.indexOf(team), 1)
      return [204]
    }
  }

  // ---- Integrantes ----
  if ((m = match('/tournaments/:id/teams/:teamId/members', path))) {
    if (method === 'GET') {
      const t = getVisible(m.id, viewer(req))
      const team = getTeam(t.id, m.teamId)
      return [200, members.filter((x) => x.teamId === team.id).map(memberResponse)]
    }
    if (method === 'POST') {
      const t = getOwned(m.id, requireUser(req))
      const team = getTeam(t.id, m.teamId)
      validate(memberChecks(body))
      const row = { id: genId(), teamId: team.id, name: body.name, role: body.role ?? null }
      members.push(row)
      return [201, memberResponse(row)]
    }
  }

  if ((m = match('/tournaments/:id/teams/:teamId/members/:memberId', path))) {
    if (method === 'GET') {
      const t = getVisible(m.id, viewer(req))
      return [200, memberResponse(getMember(getTeam(t.id, m.teamId), m.memberId))]
    }
    if (method === 'PUT') {
      const t = getOwned(m.id, requireUser(req))
      const row = getMember(getTeam(t.id, m.teamId), m.memberId)
      validate(memberChecks(body))
      row.name = body.name
      row.role = body.role ?? null // substituicao total
      return [200, memberResponse(row)]
    }
    if (method === 'DELETE') {
      const t = getOwned(m.id, requireUser(req))
      const row = getMember(getTeam(t.id, m.teamId), m.memberId)
      members.splice(members.indexOf(row), 1)
      return [204]
    }
  }

  // ---- Chaveamento ----
  if ((m = match('/tournaments/:id/bracket', path))) {
    if (method === 'POST') {
      const t = getOwned(m.id, requireUser(req))
      if (hasBracket(t.id)) throw conflict('Bracket already generated.')
      generateBracket(t.id)
      return [201, tournamentMatches(t.id).map(matchResponse)]
    }
    if (method === 'DELETE') {
      const t = getOwned(m.id, requireUser(req))
      if (!hasBracket(t.id)) throw conflict('Bracket has not been generated.')
      for (let i = matches.length - 1; i >= 0; i--) if (matches[i].tournamentId === t.id) matches.splice(i, 1)
      for (const team of teams) if (team.tournamentId === t.id) disqualified.delete(team.id)
      return [204]
    }
  }

  // ---- Partidas ----
  if ((m = match('/tournaments/:id/matches', path)) && method === 'GET') {
    const t = getVisible(m.id, viewer(req))
    return [200, tournamentMatches(t.id).map(matchResponse)]
  }

  if ((m = match('/tournaments/:id/matches/:matchId', path)) && method === 'GET') {
    const t = getVisible(m.id, viewer(req))
    return [200, matchResponse(getMatch(t.id, m.matchId))]
  }

  if ((m = match('/tournaments/:id/matches/:matchId/start', path)) && method === 'POST') {
    const t = getOwned(m.id, requireUser(req))
    const x = getMatch(t.id, m.matchId)
    if (x.status !== 'SCHEDULED') throw conflict('Match is not scheduled.')
    if (!x.team1Id || !x.team2Id) throw conflict('Match teams are not defined yet.')
    x.status = 'IN_PROGRESS'
    return [200, matchResponse(x)]
  }

  if ((m = match('/tournaments/:id/matches/:matchId/score', path)) && method === 'PATCH') {
    const t = getOwned(m.id, requireUser(req))
    const x = getMatch(t.id, m.matchId)
    const ok = (v) => Number.isInteger(v) && v >= 0
    validate([
      !ok(body?.team1Score) && 'team1Score: must be greater than or equal to 0',
      !ok(body?.team2Score) && 'team2Score: must be greater than or equal to 0',
    ])
    if (x.status !== 'IN_PROGRESS') throw conflict('Match is not in progress.')
    x.team1Score = body.team1Score
    x.team2Score = body.team2Score
    return [200, matchResponse(x)]
  }

  if ((m = match('/tournaments/:id/matches/:matchId/finish', path)) && method === 'POST') {
    const t = getOwned(m.id, requireUser(req))
    const x = getMatch(t.id, m.matchId)
    if (x.status !== 'IN_PROGRESS') throw conflict('Match is not in progress.')
    if (x.team1Score === x.team2Score) {
      throw conflict('Match is tied; set a winner manually or change the score.')
    }
    finishMatch(x, x.team1Score > x.team2Score ? x.team1Id : x.team2Id, 'SCORE')
    return [200, matchResponse(x)]
  }

  if ((m = match('/tournaments/:id/matches/:matchId/winner', path)) && method === 'POST') {
    const t = getOwned(m.id, requireUser(req))
    const x = getMatch(t.id, m.matchId)
    validate([!Number.isInteger(body?.teamId) && 'teamId: must not be null'])
    if (x.status === 'FINISHED') throw conflict('Match is already finished.')
    if (!x.team1Id || !x.team2Id) throw conflict('Match teams are not defined yet.')
    if (body.teamId !== x.team1Id && body.teamId !== x.team2Id) {
      throw badRequest('teamId: team does not belong to this match');
    }
    finishMatch(x, body.teamId, 'MANUAL')
    return [200, matchResponse(x)]
  }

  if ((m = match('/tournaments/:id/teams/:teamId/disqualify', path)) && method === 'POST') {
    const t = getOwned(m.id, requireUser(req))
    const team = getTeam(t.id, m.teamId)
    if (!hasBracket(t.id)) throw conflict('Bracket has not been generated.')
    if (disqualified.has(team.id)) throw conflict('Team is already disqualified.')
    const pending = tournamentMatches(t.id).find(
      (x) => x.status !== 'FINISHED' && (x.team1Id === team.id || x.team2Id === team.id),
    )
    if (!pending) throw conflict('Team is already out of the tournament.')
    disqualified.add(team.id)
    resolveDisqualified(pending)
    return [200, tournamentMatches(t.id).map(matchResponse)]
  }

  // ---- Ranking ----
  if ((m = match('/tournaments/:id/ranking', path)) && method === 'GET') {
    const t = getVisible(m.id, viewer(req))
    return [200, rankingFor(t.id)]
  }

  return [404, { status: 404, message: `Route not found: ${method} ${path}` }]
}

const server = createServer(async (req, res) => {
  if (req.method === 'OPTIONS') return send(res, 204)

  const url = new URL(req.url, `http://localhost:${PORT}`)
  let path = url.pathname
  if (path.startsWith('/api')) path = path.slice(4) || '/'

  try {
    const [status, body] = await handle(req, path, url)
    send(res, status, body)
  } catch (e) {
    if (e instanceof ApiError) {
      // 401/403 do framework de seguranca nao tem corpo
      return send(res, e.status, e.message ? { status: e.status, message: e.message } : undefined)
    }
    console.error(e)
    send(res, 500, { status: 500, message: 'Internal server error.' })
  }
})

server.listen(PORT, () => {
  console.log(`\n  Mock API BrakIt rodando em http://localhost:${PORT}/api`)
  console.log('  Login de teste: demo@brakit.com / 12345678\n')
})
