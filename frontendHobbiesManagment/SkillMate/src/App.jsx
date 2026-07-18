import { BrowserRouter, Routes, Route, useLocation, useNavigate } from "react-router-dom";
import { Dashboard } from "./pages/Dashboard";
import Footer from "./components/Footer";
import Login from "./pages/auth/Login";
import Register from "./pages/auth/Register";
import MentorProfile from "./pages/MentorProfile";
import WaitingList from "./pages/WaitingList";
import Profile from "./pages/Profile";
import Home from "./pages/Home";
import Header from "./components/Header";
import AddSkill from "./pages/AddSkill";
import SkillsSearchPage from './pages/SearchPage';
import MySkillsPage from './pages/MySkillsPage';
import Chat from './pages/Chat';
import React, { useEffect, useState } from 'react';

const NotFound = () => (
  <div className="p-8 text-red-500" style={{ direction: 'rtl' }}>
    Error 404 - Page Not Found
  </div>
);

function ProtectedRoute({ children }) {
  const navigate = useNavigate();
  const isLoggedIn = !!sessionStorage.getItem('token');

  if (!isLoggedIn) {
    return (
      <div style={{ minHeight: '60vh', display: 'flex', alignItems: 'center', justifyContent: 'center', textAlign: 'center', padding: '24px', direction: 'rtl' }}>
        <div style={{ maxWidth: '420px' }}>
          <h2 style={{ color: '#c62828', marginBottom: '12px' }}>no access for this page</h2>
          <p style={{ color: '#555', marginBottom: '16px' }}>since you are not registered to the system</p>
          <button
            onClick={() => navigate('/auth/login')}
            style={{ padding: '10px 16px', background: '#bc4a23', color: 'white', border: 'none', borderRadius: '8px', cursor: 'pointer' }}
          >
            to login page
            
          </button>
        </div>
      </div>
    );
  }

  return children;
}

function AppLayout() {
  const location = useLocation();
  const hideHeader = ['/auth/login', '/auth/register'].includes(location.pathname);
  

  const [isInitialized, setIsInitialized] = useState(false);
  useEffect(() => {
    setIsInitialized(true);
  }, []);

  // אם האפליקציה עדיין בשלב האתחול והניקוי, לא נרנדר את ה-Routes כדי למנוע קריאות שגויות לשרת
  if (!isInitialized) {
    return <div style={{ padding: '20px', textAlign: 'center' }}>Loading Application...</div>;
  }

  return (
    <div style={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
      {!hideHeader && <Header />}
      <main style={{ flex: 1 }}>
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/auth/login" element={<Login />} />
          <Route path="/auth/register" element={<Register />} />
          <Route path="/dashboard" element={<ProtectedRoute><Dashboard /></ProtectedRoute>} />
          <Route path="/profile" element={<ProtectedRoute><Profile /></ProtectedRoute>} />
          <Route path="/mentorProfile/:id" element={<ProtectedRoute><MentorProfile /></ProtectedRoute>} />
          <Route path="/add-skill/:id" element={<ProtectedRoute><AddSkill /></ProtectedRoute>}/>
          <Route path="/my-skills" element={<ProtectedRoute><MySkillsPage /></ProtectedRoute>} />
          <Route path="/skill" element={<ProtectedRoute><SkillsSearchPage /></ProtectedRoute>}/>
          <Route path="/waitingList" element={<ProtectedRoute><WaitingList /></ProtectedRoute>} />
          <Route path="/chat" element={<ProtectedRoute><Chat /></ProtectedRoute>} />
          <Route path="*" element={<NotFound />} />
        </Routes>
      </main>
      <Footer />
    </div>
  );
}

function App() {
  return (
    <BrowserRouter>
      <AppLayout />
    </BrowserRouter>
  );
}

export default App;