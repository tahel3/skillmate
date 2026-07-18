import { createApi, fetchBaseQuery } from "@reduxjs/toolkit/query/react";
import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";

export const api = createApi({
  reducerPath: "api",
  refetchOnFocus: true,     
  refetchOnReconnect: true, 
  baseQuery: fetchBaseQuery({ 
    baseUrl: "http://localhost:8080/api",
    prepareHeaders: (headers) => {
      const token = sessionStorage.getItem("token"); 
      if (token) {
        headers.set("Authorization", `Bearer ${token}`);
      }
      return headers;
    },
  }), 
  
  tagTypes: ["MentorProfile", "SkillsPage", "Sessions", "MentorsList", "WaitingList", "Review", "Skills", "Notifications", "Messages", "MentorDashboard"],

  endpoints: (builder) => ({
    
    // --- אימות משתמשים (Auth) ---
    login: builder.mutation({
      query: (credentials) => ({ url: "/auth/login", method: "POST", body: credentials }),
    }),
    register: builder.mutation({
      query: (userData) => ({ url: "/auth/register", method: "POST", body: userData }),
    }),

    // --- יצירת פרופילים ראשונית (Setup) ---
    createLearnerProfile: builder.mutation({
      query: (profileData) => ({ url: "/learners", method: "POST", body: profileData }),
    }),
    createMentorProfile: builder.mutation({
      query: (profileData) => ({ url: "/mentors", method: "POST", body: profileData }),
    }),

    // --- מנטורים וחיפוש ---
    getMentors: builder.query({
      query: () => "/mentors",
      providesTags: ["MentorsList"],
    }),    
    searchSkills: builder.query({
      query: ({ name = "", category = "", page = 0, size = 10 }) => {
   
        const params = new URLSearchParams();
        
        params.append('page', page.toString());
        params.append('size', size.toString());
  
        if (name && name.trim() !== "") {
          params.append('name', name);
        }
        if (category && category.trim() !== "") {
          params.append('category', category);
        }
        return `skill/search?${params.toString()}`;
      },
      providesTags: ["SkillsPage"],
    }),
    // --- פרופיל מנטור וניהול זמינות ---
    getMyMentorProfile: builder.query({
      query: () => `/mentors/me`,
      providesTags: ["MentorProfile", "MentorDashboard"],
    }),
    getMentorProfileById: builder.query({
      query: (id) => `/mentors/${id}`,
      providesTags: ["MentorProfile"],
    }),
    getMentorCalendar: builder.query({
      query: (mentorId) => `/mentors/${mentorId}/available-dates`, 
      providesTags: ["Sessions", "MentorProfile"],
    }),
    createSession: builder.mutation({
      query: (sessionData) => ({ url: "/sessions", method: "POST", body: sessionData }),
      invalidatesTags: ["Sessions", "MentorDashboard", "WaitingList"],
    }),
    deleteSession: builder.mutation({
      query: (sessionId) => ({ url: `/sessions/${sessionId}`, method: "DELETE" }),
      invalidatesTags: ["Sessions", "MentorDashboard", "WaitingList"],
    }),
    updateMentorAvailability: builder.mutation({
      query: ({ id, availabilityList }) => ({ url: `/mentors/${id}/availability`, method: "PUT", body: availabilityList }),
      invalidatesTags: ["MentorProfile", "MentorDashboard"],
    }),
    updateMentorCapacity: builder.mutation({
      query: ({ id, maxStudents }) => ({ url: `/mentors/${id}/capacity`, method: "PUT", body: { maxStudents } }),
      invalidatesTags: ["MentorProfile", "MentorDashboard"],
    }),

    // --- שיעורים ומועדפים (Sessions & Favorites) ---
    getFavoriteSkills: builder.query({
      query: (learnerId) => `/learners/${learnerId}/favorites`,
      providesTags: ["Skills"],
    }),
    addFavoriteSkill: builder.mutation({
      query: ({ learnerId, skillId }) => ({ url: `/learners/${learnerId}/favorites/${skillId}`, method: "POST" }),
      invalidatesTags: ["Skills"],
    }),
    removeFavoriteSkill: builder.mutation({
      query: ({ learnerId, skillId }) => ({ url: `/learners/${learnerId}/favorites/${skillId}`, method: "DELETE" }),
      invalidatesTags: ["Skills"],
    }),
      getSkillsByMentor: builder.query({
      query: (learnerId) => `/skill/by-mentor/${learnerId}`,
      providesTags: ["Skills"], 
    }),
 getLearnerSessions: builder.query({
  query: (learnerId) => `/sessions/by-learner/${learnerId}`,
  providesTags: ["Sessions"],
}),
getMentorSessions: builder.query({
  query: (mentorId) => `/sessions/by-mentor/${mentorId}`, 
  providesTags: ["Sessions"],
}),
    // --- רשימות המתנה (Waiting List) ---
    getIncomingRequests: builder.query({
      query: (mentorId) => `/waitlist/incoming/${mentorId}`, 
      providesTags: ["WaitingList"],
    }),
    getLearnerWaitlist: builder.query({
      query: (learnerId) => `/waitlist/by-learner/${learnerId}`,
      providesTags: ["WaitingList"],
    }),
    deleteWaitinglistEntry: builder.mutation({
      query: (entryId) => ({ url: `/waitlist/${entryId}`, method: "DELETE" }),
      invalidatesTags: ["WaitingList", "MentorDashboard"],
    }),
    addToWaitlist: builder.mutation({
      query: (waitlistData) => ({ url: "/waitlist/add", method: "POST", body: waitlistData }),
      invalidatesTags: ["WaitingList", "MentorDashboard"],
    }),
    enrollFromWaitlist: builder.mutation({
      query: (entryId) => ({ url: `/waitlist/${entryId}/enroll`, method: "PUT" }),
      invalidatesTags: ["WaitingList", "Sessions", "MentorDashboard"],
    }),

    // --- ביקורות (Reviews) ---
    getReviews: builder.query({
      query: (mentorId) => `/review/mentor/${mentorId}`, 
      providesTags: ["Review"],
    }),
    addReview: builder.mutation({
      query: (reviewData) => ({ url: "/review", method: "POST", body: reviewData }),
      invalidatesTags: ["Review"],
    }),
    linkExistingSkill: builder.mutation({
      query: (skillId) => ({ url: `/mentors/my-skills/link/${skillId}`, method: "POST" }),
      invalidatesTags: ["MentorProfile", "MentorsList"],
    }),

    // ==========================================
    //  רכיבי זמן אמת חדשים (WebSockets)
    // ==========================================

    //  שליפת התראות לא נקראות והאזנה לסוקט
    getUnreadNotifications: builder.query({
      query: (userId) => `/notifications/unread/${userId}`,
      providesTags: ["Notifications"],
      async onCacheEntryAdded(userId, { updateCachedData, cacheDataLoaded, cacheEntryRemoved }) {
        const stompClient = new Client({
          webSocketFactory: () => new SockJS("http://localhost:8080/ws-chat"),
          reconnectDelay: 5000,
        });
        try {
          await cacheDataLoaded;
          stompClient.onConnect = () => {
            stompClient.subscribe(`/user/${userId}/queue/notifications`, (message) => {
              const newNotif = JSON.parse(message.body);
              updateCachedData((draft) => {
                draft.unshift(newNotif);
              });
            });
          };
          stompClient.activate();
        } catch (err) {
          console.error("Notification socket error:", err);
        }
        await cacheEntryRemoved;
        stompClient.deactivate();
      },
    }),

    // --- זמינות מנטור: עדכון רשימה מלאה בלבד (אין sub-endpoints לסלוט בודד בשרת) ---

    // --- סימון התראה כנקראה ---
    markNotificationRead: builder.mutation({
      query: (notifId) => ({ url: `/notifications/mark-as-read/${notifId}`, method: "PUT" }),
      invalidatesTags: ["Notifications"],
    }),
    getChatMessages: builder.query({
  query: ({ userId1, userId2 }) => `/chat/messages?userId1=${userId1}&userId2=${userId2}`,
  providesTags: ["Messages"],
  async onCacheEntryAdded({ userId1 }, { updateCachedData, cacheDataLoaded, cacheEntryRemoved }) {
    const stompClient = new Client({
      webSocketFactory: () => new SockJS("http://localhost:8080/ws-chat"),
      reconnectDelay: 5000,
    });
    try {
      await cacheDataLoaded;
      stompClient.onConnect = () => {
        stompClient.subscribe(`/user/${userId1}/queue/messages`, (message) => {
          const newMsg = JSON.parse(message.body);
          updateCachedData((draft) => {
            draft.push(newMsg);
          });
        });
      };
      stompClient.activate();
    } catch (err) {
      console.error("Chat socket error:", err);
    }
    await cacheEntryRemoved;
    stompClient.deactivate();
  },
}),

    askAi: builder.mutation({
      query: (message) => ({
        url: "/ai/ask",
        method: "POST",
        body: { message },
      }),
    }),

    getConversations: builder.query({
      query: (userId) => `/chat/conversations/${userId}`,
      providesTags: ["Messages"],
    }),

  }), 
});

export const {
  useLoginMutation,
  useRegisterMutation,
  useCreateLearnerProfileMutation,
  useCreateMentorProfileMutation,
  useGetMentorsQuery,
  useDeleteWaitinglistEntryMutation,
  useGetFavoriteSkillsQuery,
  useAddFavoriteSkillMutation,
  useRemoveFavoriteSkillMutation,
  useGetSkillsByMentorQuery,
useGetLearnerSessionsQuery,
useGetMentorSessionsQuery,
  useGetMyMentorProfileQuery,
  useGetMentorProfileByIdQuery, 
  useGetMentorCalendarQuery,
  useCreateSessionMutation,
  useDeleteSessionMutation,
  useSearchSkillsQuery,
  useUpdateMentorAvailabilityMutation,
  useUpdateMentorCapacityMutation,
  useAddToWaitlistMutation,
  useEnrollFromWaitlistMutation,
  useGetReviewsQuery,
  useAddReviewMutation,
  useLinkExistingSkillMutation,
  useGetIncomingRequestsQuery,
  useGetLearnerWaitlistQuery,
  useGetUnreadNotificationsQuery,
  useMarkNotificationReadMutation,
  useGetChatMessagesQuery,
  useAskAiMutation,
  useGetConversationsQuery,
} = api;