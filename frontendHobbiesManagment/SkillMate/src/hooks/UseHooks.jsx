// useAuth.js
import { useState, useEffect } from "react";

const AUTH_CHANGE_EVENT = "authStateChanged";
export const notifyAuthChanged = () => {
  window.dispatchEvent(new Event(AUTH_CHANGE_EVENT));
};

const readAuthFromStorage = () => {
  const token = sessionStorage.getItem("token");
  const userString = sessionStorage.getItem("user");
  const userObj = userString ? JSON.parse(userString) : {};

  const role = userObj.role || sessionStorage.getItem("role") || null;
  const email = userObj.email || sessionStorage.getItem("email") || null;
  const name = userObj.name || sessionStorage.getItem("name") || null;
  const userId = userObj.userId || sessionStorage.getItem("userId") || null;

  const isMentor = role === "MENTOR" || role === "MENTOR_AND_LEARNER";
  const isLearner = role === "LEARNER" || role === "MENTOR_AND_LEARNER";

  return { token, role, email, name, userId, isAuthenticated: !!token, isMentor, isLearner };
};

export const useAuth = () => {
  const [authState, setAuthState] = useState(readAuthFromStorage);

  useEffect(() => {
    const handleChange = () => setAuthState(readAuthFromStorage());

    window.addEventListener("storage", handleChange);       
    window.addEventListener(AUTH_CHANGE_EVENT, handleChange); 

    return () => {
      window.removeEventListener("storage", handleChange);
      window.removeEventListener(AUTH_CHANGE_EVENT, handleChange);
    };
  }, []);

  return authState;
};