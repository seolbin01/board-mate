import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { useAuthStore } from './stores/authStore';
import LoginPage from './pages/LoginPage';
import SignupPage from './pages/SignupPage';
import RoomListPage from './pages/RoomListPage';
import RoomDetailPage from './pages/RoomDetailPage';
import Layout from './components/Layout';
import CreateRoomPage from './pages/CreateRoomPage';
import ProfilePage from './pages/ProfilePage';
import MyRoomsPage from './pages/MyRoomsPage';
import SommelierPage from './pages/SommelierPage';

function PrivateRoute({ children }: { children: React.ReactNode }) {
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
  return isAuthenticated ? <>{children}</> : <Navigate to="/login" />;
}

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/signup" element={<SignupPage />} />
        <Route path="/" element={
          <PrivateRoute>
            <Layout />
          </PrivateRoute>
        }>
          <Route index element={<RoomListPage />} />
          <Route path="rooms/:id" element={<RoomDetailPage />} />
          <Route path="rooms/new" element={<CreateRoomPage />} />
          <Route path="my-rooms" element={<MyRoomsPage />} />
          <Route path="profile" element={<ProfilePage />} />
          <Route path="sommelier" element={<SommelierPage />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}

export default App;
