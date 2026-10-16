import {
  BrowserRouter as Router,
  Routes,
  Route,
  Navigate,
} from 'react-router-dom'
import { AuthProvider } from '@/contexts/AuthContext'
import { LoginPage } from '@/pages/LoginPage'
import { SignupPage } from '@/pages/SignupPage'
import { DashboardPage } from '@/pages/DashboardPage'
import { CreateTournamentPage } from '@/pages/CreateTournamentPage'
import { EditTournamentPage } from '@/pages/EditTournamentPage'
import { TournamentDetailPage } from '@/pages/TournamentDetailPage'
import { CreateTeamPage } from '@/pages/CreateTeamPage'
import { TeamDetailPage } from '@/pages/TeamDetailPage'
import { ArbiterPanelPage } from '@/pages/ArbiterPanelPage'
import { BracketPreviewPage } from '@/pages/BracketPreviewPage'
import { RankingPreviewPage } from '@/pages/RankingPreviewPage'
import { RoundPreviewPage } from '@/pages/RoundPreviewPage'
import { NotFoundPage } from '@/pages/NotFoundPage'
import { ProtectedRoute } from '@/pages/ProtectedRoute'

function App() {
  return (
    <AuthProvider>
      <Router>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/signup" element={<SignupPage />} />

          {/* Leitura pública: o backend libera torneios PUBLIC para anônimos */}
          <Route path="/dashboard" element={<DashboardPage />} />
          <Route path="/preview/bracket/:id" element={<BracketPreviewPage />} />
          <Route path="/preview/ranking/:id" element={<RankingPreviewPage />} />
          <Route path="/preview/round/:id" element={<RoundPreviewPage />} />
          <Route path="/tournaments/:id" element={<TournamentDetailPage />} />
          <Route
            path="/tournaments/:id/teams/:teamId"
            element={<TeamDetailPage />}
          />

          {/* Escrita: exige login */}
          <Route element={<ProtectedRoute />}>
            <Route
              path="/tournaments/create"
              element={<CreateTournamentPage />}
            />
            <Route path="/tournaments/:id/edit" element={<EditTournamentPage />} />
            <Route
              path="/tournaments/:id/arbiter"
              element={<ArbiterPanelPage />}
            />
            <Route
              path="/tournaments/:id/teams/create"
              element={<CreateTeamPage />}
            />
          </Route>

          <Route path="/" element={<Navigate to="/dashboard" replace />} />
          <Route path="*" element={<NotFoundPage />} />
        </Routes>
      </Router>
    </AuthProvider>
  )
}

export default App
