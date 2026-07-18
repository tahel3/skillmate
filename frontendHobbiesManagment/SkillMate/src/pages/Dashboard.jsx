import React, { useEffect, useState } from "react";
import { useAuth } from "../hooks/useHooks";
import { useNavigate } from "react-router-dom";
import {
  useGetMentorSessionsQuery,
  useGetFavoriteSkillsQuery,
  useGetMyMentorProfileQuery,
  useGetIncomingRequestsQuery,
  useGetLearnerSessionsQuery,
  useGetReviewsQuery,
  useAddReviewMutation,
  useDeleteSessionMutation,
  useUpdateMentorAvailabilityMutation,
  useUpdateMentorCapacityMutation,
} from "../api/Api";
import { Button } from "@mui/material";
import "./Dashboard.css";

const DAYS = ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"];

const CATEGORIES = ["Singing", "Programming", "Fitness", "Ball games", "Painting", "Cooking", "Baking", "Planning"];

const getLessonIcon = (skillName) => {
  if (!skillName) return "📚";

  const iconMap = {
    "singing": "🎤",
    "programming": "💻",
    "fitness": "💪",
    "ball games": "⚽",
    "painting": "🎨",
    "cooking": "🍳",
    "baking": "🧁",
    "planning": "📅"
  };
  const name = skillName.toLowerCase();
  const category = CATEGORIES.find(cat => name.includes(cat.toLowerCase()));
  return category ? iconMap[category.toLowerCase()] : "📚";
};

export const Dashboard = () => {
  const { userId, isMentor, isLearner } = useAuth();
  const navigate = useNavigate();

  const [activeTab, setActiveTab] = useState(isMentor ? "mentor" : "learner");
  const [showAvailabilityModal, setShowAvailabilityModal] = useState(false);
  const [availabilitySlots, setAvailabilitySlots] = useState([{ dayOfWeek: "SUNDAY", startTime: "", endTime: "" }]);

  const [showReviewForm, setShowReviewForm] = useState(false);
  const [selectedSession, setSelectedSession] = useState(null);
  const [rating, setRating] = useState(5);
  const [comment, setComment] = useState("");
  const [maxStudentsInput, setMaxStudentsInput] = useState("");

  const { data: mentorProfileRaw, isLoading: isLoadingProfile } = useGetMyMentorProfileQuery(userId, { skip: !userId || !isMentor });
  const { data: incomingRequestsRaw = [], isLoading: isLoadingRequests } = useGetIncomingRequestsQuery(userId, { skip: !userId || !isMentor });
  const { data: mySessionsRaw, isLoading: isLoadingSessions } = useGetLearnerSessionsQuery(userId, { skip: !userId || !isLearner });
  const { data: mentorSessionsRaw, isLoading: isLoadingMentorSessions } = useGetMentorSessionsQuery(userId, { skip: !userId || !isMentor });
  const { data: favoriteSkillsRaw, isLoading: isLoadingFavorites } = useGetFavoriteSkillsQuery(userId, { skip: !userId || !isLearner });
  const { data: reviewsRaw, isLoading: isLoadingReviews } = useGetReviewsQuery(userId, { skip: !isMentor });

  const mentorProfile = mentorProfileRaw ?? null;
  const incomingRequests = incomingRequestsRaw ?? [];
  const mySessions = mySessionsRaw ?? [];
  const mentorSessions = mentorSessionsRaw ?? [];
  const favoriteSkills = favoriteSkillsRaw ?? [];
  const reviews = reviewsRaw ?? [];
  const mentorId = mentorProfile?.mentorId || userId;

  const [addReview] = useAddReviewMutation();
  const [deleteSession] = useDeleteSessionMutation();
  const [updateAvailability] = useUpdateMentorAvailabilityMutation();
  const [updateMentorCapacity] = useUpdateMentorCapacityMutation();

  useEffect(() => {
    if (mentorProfile?.maxStudents != null) {
      setMaxStudentsInput(String(mentorProfile.maxStudents));
    }
  }, [mentorProfile?.maxStudents]);

  if (isLoadingSessions || isLoadingFavorites || isLoadingProfile || isLoadingRequests || isLoadingReviews || isLoadingMentorSessions) {
    console.log("rendering — isMentor:", isMentor, "isLearner:", isLearner);
    return <div className="dashboard-container-wrapper"><p>טוען נתונים מהשרת...</p></div>;
  }
console.log("userId:", userId, "isLearner:", isLearner)

console.log("mySessions:", mySessions);
  const handleSubmitReview = async (e) => {
    e.preventDefault();
    if (!selectedSession) return;
    try {
     await addReview({ mentorId: selectedSession.mentorId, learnerId: userId, rating, comment }).unwrap();
      setComment("");
      setSelectedSession(null);
      setShowReviewForm(false);
      alert("המשוב נשמר בהצלחה!");
    } catch (err) {
      alert("שגיאה בשמירת המשוב: " + err.message);
    }
  };

  const handleAddSlot = () => {
    setAvailabilitySlots([...availabilitySlots, { dayOfWeek: "SUNDAY", startTime: "", endTime: "" }]);
  };

  const handleRemoveSlot = (index) => {
    setAvailabilitySlots(availabilitySlots.filter((_, i) => i !== index));
  };

  const handleSlotChange = (index, field, value) => {
    const updated = [...availabilitySlots];
    updated[index][field] = value;
    setAvailabilitySlots(updated);
  };

  const handleSaveAvailability = async () => {
    try {
      await updateAvailability({ id: userId, availabilityList: availabilitySlots }).unwrap();
      setShowAvailabilityModal(false);
      alert("הזמינות עודכנה בהצלחה!");
    } catch (err) {
      alert("שגיאה בעדכון הזמינות: " + err.message);
    }
  };

  const handleSaveCapacity = async () => {
    if (!mentorId) {
      alert("Mentor profile is not loaded yet.");
      return;
    }

    const value = Number(maxStudentsInput);
    if (!Number.isInteger(value) || value < 0) {
      alert("Please enter a valid non-negative number.");
      return;
    }

    try {
      await updateMentorCapacity({ id: mentorId, maxStudents: value }).unwrap();
      alert("Maximum active students updated successfully!");
    } catch (err) {
      alert("Failed to update max students: " + (err?.message || "Unknown error"));
    }
  };

  const handleDeleteSession = async (sessionId) => {
    if (!window.confirm("Are you sure you want to cancel this lesson?")) {
      return;
    }

    try {
      await deleteSession(sessionId).unwrap();
      alert("Lesson canceled successfully.");
    } catch (err) {
      alert("Failed to cancel lesson: " + (err?.message || "Unknown error"));
    }
  };

  return (
    <div className="dashboard-container-wrapper">
      <div className="dashboard-container">

        {isMentor && isLearner && (
          <div className="tabs-container">
            <button className={`tab-btn ${activeTab === "learner" ? "active" : ""}`} onClick={() => setActiveTab("learner")}>
              Learner Dashboard
            </button>
            <button className={`tab-btn ${activeTab === "mentor" ? "active" : ""}`} onClick={() => setActiveTab("mentor")}>
              Mentor Dashboard
            </button>
          </div>
        )}

        {/* ================= אזור לומד ================= */}
        {isLearner && (!isMentor || activeTab === "learner") && (
          <div className="dashboard-section">
            <h2 className="section-title">Upcoming lessons</h2>
            <div className="cards-grid-row">
              {mySessions.length > 0 ? (
                mySessions.map((session) => (
                  <div key={session.id} className="dashboard-card-horizontal">
                    <div className="card-avatar-img">{getLessonIcon(session.skillName)}</div>
                    <div className="card-info-content">
                      <div className="card-subject-title">{session.skillName}</div>
                      <p className="card-detail-text">with {session.mentorName}</p>
                    </div>
                    <div style={{ display: "flex", gap: "8px" }}>
                      <button className="card-view-btn review-btn" onClick={() => { setSelectedSession(session); setShowReviewForm(true); }}>
                        Write Review
                      </button>
                      <button className="card-view-btn" style={{ background: "#f3e0db", color: "#a23a1d" }} onClick={() => handleDeleteSession(session.id)}>
                        Cancel lesson
                      </button>
                    </div>
                  </div>
                ))
              ) : (
                <p className="dashboard-sub">No upcoming lessons scheduled.</p>
              )}
            </div>

            {showReviewForm && selectedSession && (
              <div className="review-form-overlay">
                <form onSubmit={handleSubmitReview} className="review-form-box">
                  <h3>Leave a review for {selectedSession?.skillName}</h3>
                  <p>Teacher: {selectedSession?.mentorName}</p>
                  <div className="form-group">
                    <label>Rating: </label>
                    <select value={rating} onChange={(e) => setRating(Number(e.target.value))}>
                      <option value="5">⭐⭐⭐⭐⭐ (5)</option>
                      <option value="4">⭐⭐⭐⭐ (4)</option>
                      <option value="3">⭐⭐⭐ (3)</option>
                      <option value="2">⭐⭐ (2)</option>
                      <option value="1">⭐ (1)</option>
                    </select>
                  </div>
                  <div className="form-group">
                    <label>Your comment:</label>
                    <textarea value={comment} onChange={(e) => setComment(e.target.value)} required placeholder="How was your lesson?..." />
                  </div>
                  <div className="form-actions">
                    <button type="submit" className="save-btn">Submit</button>
                    <button type="button" className="cancel-btn" onClick={() => { setShowReviewForm(false); setSelectedSession(null); }}>Cancel</button>
                  </div>
                </form>
              </div>
            )}

            <h2 className="section-title">❤️ Favorite lessons</h2>
            <div className="cards-grid-row">
              {favoriteSkills.length > 0 ? (
                favoriteSkills.map((favSkill) => (
                  <div key={favSkill.id} className="dashboard-card-horizontal">
                    <div className="card-avatar-img">{getLessonIcon(favSkill.category)}</div>
                    <div className="card-info-content">
                      <div className="card-subject-title">{favSkill.name}</div>
                      <p className="card-detail-text">Category: {favSkill.category || "General"}</p>
                    </div>
                  </div>
                ))
              ) : (
                <p className="dashboard-sub">No favorite lessons saved yet.</p>
              )}
            </div>
          </div>
        )}

        {/* ================= אזור מנטור ================= */}
        {isMentor && (!isLearner || activeTab === "mentor") && (
          <div className="dashboard-section">

            <div className="stats-grid">
              <div className="stat-card">
                <div className="stat-icon">👥</div>
                <div className="stat-info">
                  <span className="stat-label">ACTIVE STUDENTS</span>
                  <span className="stat-value">{mentorProfile?.activeStudentsCount || 0}</span>
                </div>
              </div>
              <div className="stat-card">
                <div className="stat-icon">✉️</div>
                <div className="stat-info">
                  <span className="stat-label">PENDING REQUESTS</span>
                  <span className="stat-value">{mentorProfile?.pendingRequestsCount || 0}</span>
                </div>
              </div>
              <div className="stat-card">
                <div className="stat-icon">📅</div>
                <div className="stat-info">
                  <span className="stat-label">HOURS THIS MONTH</span>
                  <span className="stat-value">{mentorProfile?.hoursThisMonth || 0}</span>
                </div>
              </div>
            </div>
            <div>
 <Button
                variant="contained"
                startIcon={<span>➕</span>}
                onClick={() => navigate(`/add-skill/${userId}`)}
                className="add-skill-dashboard-btn"
                sx={{
                  backgroundColor: "#bc4a23",
                  color: "#ffffff",
                  fontFamily: "inherit",
                  fontWeight: 600,
                  borderRadius: "30px",
                  padding: "8px 20px",
                  textTransform: "none",
                  boxShadow: "none",
                  "&:hover": { backgroundColor: "#a33e1c", boxShadow: "0px 4px 8px rgba(188, 74, 35, 0.2)" },
                }}
              >
                Add New Skill
              </Button></div>
            {/* שיעורים קרובים של המנטור */}
            <h2 className="section-title">📅 Upcoming lessons</h2>
            <div className="cards-grid-row">
              {mentorSessions.length > 0 ? (
                mentorSessions.slice(0, 3).map((session) => (
                  <div key={session.id} className="dashboard-card-horizontal">
                    <div className="card-avatar-img">{getLessonIcon(session.skillName)}</div>
                    <div className="card-info-content">
                      <div className="card-subject-title">{session.skillName}</div>
                      <p className="card-detail-text">Student: {session.learnerName}</p>
                      {session.startTime && (
                        <p className="card-detail-text card-time">
                          🕐 {new Date(session.startTime).toLocaleString("en-GB", { weekday: "short", hour: "2-digit", minute: "2-digit" })}
                        </p>
                      )}
                    </div>
                  </div>
                ))
              ) : (
                <p className="dashboard-sub">No upcoming lessons scheduled.</p>
              )}
            </div>
  <div className="mentor-section-header" style={{ marginTop: "16px" }}>
              <h2 className="section-title">👥 Max active students</h2>
              <div style={{ display: "flex", gap: "8px", alignItems: "center" }}>
                <input
                  type="number"
                  min="0"
                  value={maxStudentsInput}
                  onChange={(e) => setMaxStudentsInput(e.target.value)}
                  placeholder={mentorProfile?.maxStudents ?? 0}
                  style={{ width: "120px", padding: "8px 10px", borderRadius: "8px", border: "1px solid #ccc" }}
                />
                <Button
                  variant="contained"
                  onClick={handleSaveCapacity}
                  sx={{
                    backgroundColor: "#bc4a23",
                    color: "#fff",
                    fontFamily: "inherit",
                    fontWeight: 600,
                    borderRadius: "30px",
                    padding: "8px 16px",
                    textTransform: "none",
                    "&:hover": { backgroundColor: "#a33e1c" },
                  }}
                >
                  Save limit
                </Button>
              </div>
            </div>

            {/* ניהול זמינות */}
            <div className="mentor-section-header">
              <h2 className="section-title">🕐 My availability</h2>
              <Button
                variant="outlined"
                onClick={() => setShowAvailabilityModal(true)}
                sx={{
                  borderColor: "#bc4a23",
                  color: "#bc4a23",
                  fontFamily: "inherit",
                  fontWeight: 600,
                  borderRadius: "30px",
                  padding: "8px 20px",
                  textTransform: "none",
                  "&:hover": { borderColor: "#a33e1c", color: "#a33e1c", backgroundColor: "#fdf2ee" },
                }}
              >
                Manage hours
              </Button>
            </div>

          
           {(mentorProfile?.availabilities ?? []).length > 0 ? (
              <div className="availability-chips">
                {mentorProfile.availabilities.map((slot, i) => (
                  <div key={i} className="availability-chip">
                    <span className="chip-day">{slot.dayOfWeek}</span>
                    <span className="chip-time">{slot.startTime} – {slot.endTime}</span>
                  </div>
                ))}
              </div>
            ) : (
              <div className="no-requests-box"><p>No availability set yet. Click "Manage hours" to add your schedule.</p></div>
            )}

            {/* בקשות נכנסות */}
            <div className="mentor-section-header">
              <h2 className="section-title">Incoming requests</h2>
            </div>

            {incomingRequests.length > 0 ? (
              <div className="cards-grid-row">
                {incomingRequests.map((request) => (
                  <div key={request.id} className="dashboard-card-horizontal">
                    <div className="card-avatar-img">💻</div>
                    <div className="card-info-content">
                      <div className="card-subject-title">{request.skillName || "Private Lesson"}</div>
                      <p className="card-detail-text">Student: {request.learnerName}</p>
                    </div>
                  </div>
                ))}
              </div>
            ) : (
              <div className="no-requests-box"><p>No pending requests right now.</p></div>
            )}

            {/* ביקורות */}
            <h2 className="section-title" style={{ marginTop: "40px" }}>What students say (Reviews)</h2>
            {reviews.length > 0 ? (
              <div className="reviews-list-grid">
                {reviews.map((rev) => (
                  <div key={rev.id} className="review-item-card">
                    <span className="review-stars">{"⭐".repeat(rev.rating ?? 0)} ({rev.rating ?? 0}/5)</span>
                    <p className="review-comment-text">"{rev.comment}"</p>
                  </div>
                ))}
              </div>
            ) : (
              <div className="no-requests-box"><p>No reviews received yet.</p></div>
            )}
          </div>
        )}

        {/* ================= מודל ניהול זמינות ================= */}
        {showAvailabilityModal && (
          <div className="modal-overlay">
            <div className="modal-box">
              <div className="modal-header">
                <h3>Manage availability</h3>
                <button className="modal-close-btn" onClick={() => setShowAvailabilityModal(false)}>✕</button>
              </div>

              <div className="modal-body">
            {availabilitySlots.map((slot, i) => (
                  <div key={i} className="availability-row">
                    <select
                      className="avail-select"
                      value={slot.dayOfWeek}
                      onChange={(e) => handleSlotChange(i, "dayOfWeek", e.target.value)}
                    >
                      {DAYS.map((day) => (
                        <option key={day} value={day.toUpperCase()}>{day}</option>
                      ))}
                    </select>
                    <input
                      type="time"
                      className="avail-time"
                      value={slot.startTime}
                      onChange={(e) => handleSlotChange(i, "startTime", e.target.value)}
                    />
                    <span className="avail-sep">–</span>
                    <input
                      type="time"
                      className="avail-time"
                      value={slot.endTime}
                      onChange={(e) => handleSlotChange(i, "endTime", e.target.value)}
                    />
                    {availabilitySlots.length > 1 && (
                      <button className="remove-slot-btn" onClick={() => handleRemoveSlot(i)}>✕</button>
                    )}
                  </div>
                ))}

                <button className="add-slot-btn" onClick={handleAddSlot}>+ Add another time slot</button>
              </div>

              <div className="modal-footer">
                <button className="cancel-btn" onClick={() => setShowAvailabilityModal(false)}>Cancel</button>
                <button className="save-btn" onClick={handleSaveAvailability}>Save changes</button>
              </div>
            </div>
          </div>
        )}

      </div>
    </div>
  );
};

export default Dashboard;