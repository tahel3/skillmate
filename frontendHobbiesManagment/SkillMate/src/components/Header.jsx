import React, { useState, useEffect, useRef } from 'react';
import { Link } from 'react-router-dom';
import './Header.css';
import logoImage from '../assets/logo.png';
import { useGetUnreadNotificationsQuery, useMarkNotificationReadMutation } from '../api/Api';
import { useSelector, useDispatch } from 'react-redux';
import { logOut } from '../features/AuthSlice'; 

const NotificationBell = ({ userId }) => {
  const [open, setOpen] = useState(false);
  const dropdownRef = useRef(null);

  const { data: notificationsData } = useGetUnreadNotificationsQuery(userId, {
    skip: !userId,
  });

  const notifications = notificationsData ?? [];
  const [markRead] = useMarkNotificationReadMutation();

  useEffect(() => {
    const handleClickOutside = (e) => {
      if (dropdownRef.current && !dropdownRef.current.contains(e.target)) {
        setOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const unreadCount = notifications?.length ?? 0;

  const handleMarkRead = async (notifId) => {
    try {
      await markRead(notifId).unwrap();
    } catch (e) {
      console.error('Failed to mark notification as read', e);
    }
  };

  return (
    <div className="notif-bell-wrapper" ref={dropdownRef}>
      <button
        className="notif-bell-btn"
        onClick={() => setOpen((prev) => !prev)}
        aria-label={`${unreadCount} unread notifications`}
      >
        🔔
        {unreadCount > 0 && (
          <span className="notif-badge">{unreadCount > 9 ? '9+' : unreadCount}</span>
        )}
      </button>

      {open && (
        <div className="notif-dropdown">
          <div className="notif-dropdown-header">
            <span>Notifications</span>
            {unreadCount > 0 && (
              <span className="notif-count-label">{unreadCount} new</span>
            )}
          </div>
          {(notifications?.length ?? 0) === 0 ? (
            <p className="notif-empty">You're all caught up! 🎉</p>
          ) : (
            <ul className="notif-list">
              {notifications.map((notif) => (
                <li key={notif.id} className="notif-item">
                  <p className="notif-message">{notif.message}</p>
                  <div className="notif-item-footer">
                    <span className="notif-time">
                      {notif.createdAt
                        ? new Date(notif.createdAt).toLocaleString('en-GB', {
                            day: '2-digit',
                            month: '2-digit',
                            hour: '2-digit',
                            minute: '2-digit',
                          })
                        : ''}
                    </span>
                    <button
                      className="notif-mark-read-btn"
                      onClick={() => handleMarkRead(notif.id)}
                    >
                      Mark as read
                    </button>
                  </div>
                </li>
              ))}
            </ul>
          )}
        </div>
      )}
    </div>
  );
};

const Header = () => {
  const dispatch = useDispatch();

  // ← זה כל השינוי: במקום useState + sessionStorage, קוראים מ-Redux
  const token = useSelector((state) => state.auth.token);
  const userRole = useSelector((state) => state.auth.role);
  const userId = useSelector((state) => state.auth.userId);
  const isLoggedIn = !!token;

  const handleLogout = () => {
    dispatch(logOut());
    window.location.href = '/';
  };

  const normalizedRole = userRole?.toLowerCase();
  const isLearner = normalizedRole === 'learner' || normalizedRole === 'mentor_and_learner';
  const isMentor = normalizedRole === 'mentor' || normalizedRole === 'mentor_and_learner';

  return (
    <header className="header">
      <div className="logo">
        <Link to="/" className="logo-container">
          <img src={logoImage} alt="SkillMate Logo" />
        </Link>
      </div>
      <nav className="nav-links">
        {isLoggedIn && isLearner && (
          <Link to="/waitingList">Waiting-List</Link>
        )}

        {isLoggedIn ? (
          <>
            <Link to="/dashboard">Dashboard</Link>
            <Link to="/chat">Chat</Link>
            <Link to="/profile">Profile</Link>
            <Link to="/skill">Search page</Link>
            {isMentor && <Link to="/my-skills">My Skills</Link>}
            <NotificationBell userId={userId} />
            <span className="logout-link" onClick={handleLogout} style={{ cursor: 'pointer' }}>
              LogOut
            </span>
          </>
        ) : (
          <>
            <Link to="/auth/login">LogIn</Link>
            <Link to="/auth/register">LogUp</Link>
          </>
        )}

        <Link to="/">Home</Link>
      </nav>
    </header>
  );
};

export default Header;