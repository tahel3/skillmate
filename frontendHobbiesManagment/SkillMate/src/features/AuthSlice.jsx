import { createSlice } from "@reduxjs/toolkit";

// 1. קביעת ערכים ראשוניים (ברירת מחדל מה-localStorage כשהאתר נטען מחדש)
const initialState = {
  token: sessionStorage.getItem("token"), 
  role: sessionStorage.getItem("role"),
  email: sessionStorage.getItem("email"),
  name: sessionStorage.getItem("name"),
   userId: sessionStorage.getItem("userId")
};

//  יצירת ה-Slice עם פונקציות העדכון (Reducers)
const authSlice = createSlice({
  name: "auth",
  initialState,
  reducers: {
    setCredentials: (state, action) => {
      console.log("נתוני ה-Action שמגיעים ל-Slice:", action.payload);
      const { token, role, email, name,userId } = action.payload;

      state.token = token;
      state.role = role;
      state.email = email;
      state.name = name || (email ? email.split('@')[0] : "אורח");
      state.userId = userId;

      // שמירה ב-LocalStorage כדי שהמידע יישמר גם אם מרעננים את הדף
      if (token) sessionStorage.setItem("token", token);
      if (role) sessionStorage.setItem("role", role);
      if (email) sessionStorage.setItem("email", email);
      if (name) sessionStorage.setItem("name", state.name);
      if (userId) sessionStorage.setItem("userId", userId);
    },
    
    // פונקציה לביצוע התנתקות (Logout) מהמערכת והסרת המידע
    logOut: (state) => {
      state.token = null;
      state.role = null;
      state.email = null;
      state.name = null;
    state.userId = null;
      sessionStorage.removeItem("token");
      sessionStorage.removeItem("role");
      sessionStorage.removeItem("email");
      sessionStorage.removeItem("name");
      sessionStorage.removeItem("userId");
    },
  },
});

export const { setCredentials, logOut } = authSlice.actions;

export default authSlice.reducer;