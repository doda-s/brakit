// Launcher for "npm run dev:mock".
// Starts the pure-Node mock API (scripts/mock-server.mjs) and the Vite dev server
// together, so the frontend can be tested without Java, Maven, Docker or Postgres.

import { spawn } from 'node:child_process'
import { fileURLToPath } from 'node:url'
import { dirname, join } from 'node:path'

const __dirname = dirname(fileURLToPath(import.meta.url))
const root = join(__dirname, '..')
const isWindows = process.platform === 'win32'

const children = []

function run(label, command, args, { env, shell } = {}) {
  const child = spawn(command, args, {
    cwd: root,
    stdio: 'inherit',
    shell: Boolean(shell),
    env: { ...process.env, ...env },
  })
  child.on('exit', (code) => {
    console.log(`[${label}] encerrado (code ${code ?? 0})`)
    shutdown()
  })
  children.push(child)
  return child
}

function shutdown() {
  for (const c of children) {
    if (!c.killed) c.kill()
  }
  process.exit(0)
}

process.on('SIGINT', shutdown)
process.on('SIGTERM', shutdown)

// 1) Mock API on port 8080 (matches VITE_API_URL=http://localhost:8080/api).
// Launch node directly (no shell) so a node path containing spaces works on Windows.
run('mock-api', process.execPath, [join(__dirname, 'mock-server.mjs')])

// 2) Vite dev server. Needs a shell on Windows so "npm" resolves to npm.cmd.
run('vite', isWindows ? 'npm.cmd' : 'npm', ['run', 'dev'], { shell: isWindows })
