import { useState, useEffect } from "react";
import {
  useGetMentorProfileByIdQuery,
  useUpdateMentorAvailabilityMutation,
  useAddToWaitlistMutation,
  useCreateSessionMutation,
  useGetSkillsByMentorQuery,
} from "../api/Api";
import { useParams, useNavigate, useLocation } from "react-router-dom";
import "./MentorProfile.css";
import { Box, Paper, Typography, IconButton } from "@mui/material";
import ChatBubbleOutlineIcon from "@mui/icons-material/ChatBubbleOutlined";
import { LocalizationProvider } from "@mui/x-date-pickers/LocalizationProvider";
import { AdapterDayjs } from "@mui/x-date-pickers/AdapterDayjs";
import { StaticDatePicker } from "@mui/x-date-pickers/StaticDatePicker";
import AddCircleOutlineIcon from "@mui/icons-material/AddCircleOutlined";
import DeleteOutlineIcon from "@mui/icons-material/DeleteOutlined";
import SaveIcon from "@mui/icons-material/Save";
import dayjs from "dayjs";
import "dayjs/locale/en";

const DAYS = ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"];
const emptySlot = () => ({ dayOfWeek: "SUNDAY", startTime: "09:00", endTime: "17:00" });

export const MentorProfile = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const location = useLocation();
  // אם הגענו מ-SearchPage עם כישור מסוים מראש
  const preselectedSkillId = location.state?.preselectedSkillId || null;

  // ---- auth state ----
  const loggedInUserId = sessionStorage.getItem("userId");
  const loggedInRole = (() => {
    try { return JSON.parse(sessionStorage.getItem("user") || "{}").role; } catch { return null; }
  })() || sessionStorage.getItem("role");

  // isOwner: המשתמש המחובר הוא בעל הפרופיל הזה
  const isOwner = loggedInUserId && String(loggedInUserId) === String(id);
  const isLearner = loggedInRole === "LEARNER" || loggedInRole === "MENTOR_AND_LEARNER";
  const canBook = isLearner && !isOwner;

  // ---- data ----
  const { data: serverProfile, isLoading } = useGetMentorProfileByIdQuery(id, { skip: !id });
  const { data: mentorSkills = [] } = useGetSkillsByMentorQuery(id, { skip: !id });
  const [updateAvailability, { isLoading: isSaving }] = useUpdateMentorAvailabilityMutation();
  const [addToWaitlist] = useAddToWaitlistMutation();
  const [createSession] = useCreateSessionMutation();

  // ---- local state ----
  const [selectedDate, setSelectedDate] = useState(dayjs());
  const [selectedSkillId, setSelectedSkillId] = useState(preselectedSkillId);
  const [selectedSlotId, setSelectedSlotId] = useState(null);
  const [bookingMessage, setBookingMessage] = useState(null);
  const [showAvailabilityEditor, setShowAvailabilityEditor] = useState(false);
  const [availabilitySlots, setAvailabilitySlots] = useState(null);
  const [saveMessage, setSaveMessage] = useState(null);
  const [bookedSlotIds, setBookedSlotIds] = useState([]);

  // ---- helpers ----
  const profile = serverProfile || {};
  const currentAvailability = profile.availabilities || [];

  const isDateAvailable = (date) => {
    if (!currentAvailability.length) return false;
    // לא לאפשר תאריכים בעבר
    if (date.isBefore(dayjs().startOf("day"))) return false;
    const dayName = date.format("dddd").toUpperCase();
    return currentAvailability.some((slot) => slot.dayOfWeek === dayName);
  };

  // מציאת slot הזמינות לתאריך שנבחר
  const availableSlotsForDate = currentAvailability.filter(
    (slot) =>
      slot.dayOfWeek === selectedDate.format("dddd").toUpperCase() &&
      !bookedSlotIds.includes(slot.id)
  );

  // ---- booking ----
  const handleBookLesson = async () => {
    if (!loggedInUserId) { navigate("/auth/login"); return; }
    if (!selectedSkillId) {
      setBookingMessage({ ok: false, text: "Please select a skill first." });
      return;
    }
    if (!isDateAvailable(selectedDate)) {
      setBookingMessage({ ok: false, text: "This date is not available. Please select a highlighted day." });
      return;
    }
    if (!selectedSlotId) {
      setBookingMessage({ ok: false, text: "Please select a time slot." });
      return;
    }

    const selectedSlot = availableSlotsForDate.find((slot) => slot.id === selectedSlotId);
    if (!selectedSlot) {
      setBookingMessage({ ok: false, text: "Selected time slot could not be found." });
      return;
    }

    const startDateTime = dayjs(`${selectedDate.format("YYYY-MM-DD")}T${selectedSlot.startTime}`);
    const endDateTime = dayjs(`${selectedDate.format("YYYY-MM-DD")}T${selectedSlot.endTime}`);

    if (!startDateTime.isValid() || !endDateTime.isValid() || !endDateTime.isAfter(startDateTime)) {
      setBookingMessage({ ok: false, text: "Invalid lesson time range." });
      return;
    }

    try {
      if (!isFull) {
        await createSession({
          learnerId: Number(loggedInUserId),
          mentorId: Number(id),
          skillId: Number(selectedSkillId),
          startTime: startDateTime.toISOString(),
          endTime: endDateTime.toISOString(),
        }).unwrap();
        setBookingMessage({ ok: true, text: "Lesson booked successfully!" });
      } else {
        await addToWaitlist({
          learnerId: Number(loggedInUserId),
          mentorId: Number(id),
          skillId: Number(selectedSkillId),
        }).unwrap();
        setBookingMessage({ ok: true, text: "You've been added to the waiting list!" });
      }
    } catch (err) {
      const message = err?.data || err?.message || "Something went wrong. Please try again.";
      if (err?.status === 409) {
        setBookingMessage({ ok: false, text: message });
      } else {
        setBookingMessage({ ok: false, text: message });
      }
    }
  };

  // ---- availability editor ----
  const openEditor = () => {
    const slots = currentAvailability.length > 0
      ? currentAvailability.map((s) => ({
          dayOfWeek: s.dayOfWeek || "SUNDAY",
          startTime: s.startTime || "09:00",
          endTime: s.endTime || "17:00",
        }))
      : [emptySlot()];
    setAvailabilitySlots(slots);
    setShowAvailabilityEditor(true);
    setSaveMessage(null);
  };

  const handleSlotChange = (index, field, value) => {
    const updated = [...availabilitySlots];
    updated[index] = { ...updated[index], [field]: value };
    setAvailabilitySlots(updated);
  };

  const handleAddSlot = () => setAvailabilitySlots([...availabilitySlots, emptySlot()]);
  const handleRemoveSlot = (index) => setAvailabilitySlots(availabilitySlots.filter((_, i) => i !== index));

  const handleSave = async () => {
    try {
      await updateAvailability({ id, availabilityList: availabilitySlots }).unwrap();
      setSaveMessage({ ok: true, text: "Availability saved successfully!" });
      setShowAvailabilityEditor(false);
    } catch {
      setSaveMessage({ ok: false, text: "Failed to save. Please try again." });
    }
  };

  // ---- render ----
  if (isLoading) {
    return <div className="mentor-profile-container"><p>Loading mentor profile data...</p></div>;
  }

  const isFull = profile.currentStudents >= profile.maxStudents && profile.maxStudents > 0;

  return (
    <LocalizationProvider dateAdapter={AdapterDayjs} adapterLocale="en">
      <div className="mentor-profile-container">

        {/* Main Mentor Card */}
        <div className="mentor-main-card">
          <div className="mentor-large-avatar">👨‍🏫</div>
          <div className="mentor-main-info">
            <h1 className="mentor-name-title">{profile.fullName || profile.name || "Mentor"}</h1>
            <p className="mentor-location-sub">📍 Location: {profile.city || "—"}</p>
            {isOwner && (
              <div className="profile-actions-row">
                <button className="profile-action-btn" onClick={() => navigate("/profile")}>Edit Profile</button>
                <button className="profile-action-btn" onClick={() => navigate("/dashboard")}>Dashboard</button>
              </div>
            )}
          </div>
        </div>

        {/* Skills */}
        <div className="mentor-details-grid">
          <div className="detail-block">
            <h2 className="detail-block-title">Professional Experience &amp; Background</h2>
            <p className="detail-block-text">{profile.experience || "No professional background specified yet."}</p>
          </div>
          <div className="detail-block">
            <h2 className="detail-block-title">Areas of Expertise &amp; Technologies</h2>
            <div className="skills-tags-container">
              {(profile.skillNames || []).length > 0
                ? profile.skillNames.map((skill, i) => <span key={i} className="skill-tag">{skill}</span>)
                : <p className="detail-block-text">No skills specified.</p>}
            </div>
          </div>
        </div>

        {/* Calendar */}
        <h2 className="section-title">Availability Calendar &amp; Dates</h2>
        <Paper className="mentor-calendar-paper" elevation={0}>
          <StaticDatePicker
            displayStaticWrapperAs="desktop"
            value={selectedDate}
            onChange={(newDate) => { setSelectedDate(newDate); setSelectedSlotId(null); }}
            slotProps={{ actionBar: { actions: [] } }}
            shouldDisableDate={(date) => date.isBefore(dayjs().startOf("day")) || !isDateAvailable(date)}
            sx={{
              backgroundColor: "transparent",
              "& .MuiPickersDay-root.Mui-selected": { backgroundColor: "#B85C38 !important" },
              "& .MuiPickersDay-root:hover:not(.Mui-disabled)": { backgroundColor: "#FDF4F2" },
              "& .MuiPickersDay-root.Mui-disabled": { opacity: 0.3 },
            }}
          />
        </Paper>
        <Box sx={{ mb: 2, pr: 1 }}>
          <Typography variant="body2" color="textSecondary">
            Selected date: <strong>{selectedDate.format("DD/MM/YYYY")}</strong>
            {isDateAvailable(selectedDate)
              ? <span style={{ color: "#2e7d32", marginLeft: 8 }}>✓ Available</span>
              : <span style={{ color: "#c62828", marginLeft: 8 }}>✗ Not available</span>}
          </Typography>
        </Box>

        {/* Booking box — only for learners who are NOT the owner */}
        {canBook && (
          <div style={{ marginBottom: "24px", padding: "16px", background: "#FFF9F1", borderRadius: "12px", border: "1px solid #e0d6cc" }}>
            <h3 style={{ margin: "0 0 12px", color: "#2d1b10" }}>Book a lesson</h3>

            {mentorSkills.length > 0 ? (
              <div style={{ marginBottom: "12px" }}>
                <label style={{ display: "block", marginBottom: "6px", fontWeight: 500 }}>Select a skill:</label>
                {preselectedSkillId ? (
                  (() => {
                    const skill = mentorSkills.find(s => s.id === preselectedSkillId);
                    return skill ? (
                      <div style={{ padding: "10px", background: "#fff", borderRadius: "8px", border: "1px solid #ccc", fontWeight: 500 }}>
                        {skill.name} (${skill.cost}/hr)
                      </div>
                    ) : null;
                  })()
                ) : (
                  <select
                    value={selectedSkillId || ""}
                    onChange={(e) => setSelectedSkillId(Number(e.target.value))}
                    style={{ width: "100%", padding: "8px", borderRadius: "8px", border: "1px solid #ccc" }}
                  >
                    <option value="">-- Choose a skill --</option>
                    {mentorSkills.map((skill) => (
                      <option key={skill.id} value={skill.id}>{skill.name} (${skill.cost}/hr)</option>
                    ))}
                  </select>
                )}
              </div>
            ) : (
              <p style={{ color: "#8e7e74" }}>No skills listed yet.</p>
            )}

            {/* בחירת שעה — מוצגת רק כשיש תאריך זמין שנבחר */}
            {isDateAvailable(selectedDate) && availableSlotsForDate.length > 0 && (
              <div style={{ marginBottom: "12px" }}>
                <label style={{ display: "block", marginBottom: "6px", fontWeight: 500 }}>
                  Select a time slot for {selectedDate.format("DD/MM/YYYY")}:
                </label>
                <div style={{ display: "flex", flexWrap: "wrap", gap: "8px" }}>
                  {availableSlotsForDate.map((slot) => (
                    <button
                      key={slot.id}
                      onClick={() => setSelectedSlotId(slot.id)}
                      style={{
                        padding: "8px 16px",
                        borderRadius: "20px",
                        border: `2px solid ${selectedSlotId === slot.id ? "#bc4a23" : "#ccc"}`,
                        backgroundColor: selectedSlotId === slot.id ? "#bc4a23" : "white",
                        color: selectedSlotId === slot.id ? "white" : "#333",
                        cursor: "pointer",
                        fontWeight: 500,
                        fontSize: "0.9rem",
                      }}
                    >
                      {slot.startTime?.substring(0, 5)} – {slot.endTime?.substring(0, 5)}
                    </button>
                  ))}
                </div>
              </div>
            )}

            <button
              className="waitlist-submit-btn"
              onClick={handleBookLesson}
              disabled={!selectedSkillId || !isDateAvailable(selectedDate) || !selectedSlotId}
              style={{ marginTop: "8px" }}
            >
              {isFull ? "Join Waiting List" : "Book this lesson"}
            </button>

            {bookingMessage && (
              <p style={{ marginTop: "10px", color: bookingMessage.ok ? "#2e7d32" : "#c62828", fontWeight: 500 }}>
                {bookingMessage.text}
              </p>
            )}

            {/* Message button — inside the booking box, below the booking action */}
            <button
              onClick={() =>
                navigate("/chat", {
                  state: {
                    recipientId: Number(id),
                    recipientName: profile.fullName || profile.name || "Mentor",
                  },
                })
              }
              style={{
                marginTop: "12px",
                width: "100%",
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
                gap: "8px",
                padding: "10px 16px",
                background: "#fff",
                border: "1.5px solid #e0d6cc",
                borderRadius: "24px",
                color: "#2d1b10",
                fontSize: "0.95rem",
                fontWeight: 500,
                cursor: "pointer",
                transition: "border-color 0.2s, background 0.2s",
              }}
              onMouseEnter={(e) => {
                e.currentTarget.style.borderColor = "#B85C38";
                e.currentTarget.style.background = "#FDF4F2";
              }}
              onMouseLeave={(e) => {
                e.currentTarget.style.borderColor = "#e0d6cc";
                e.currentTarget.style.background = "#fff";
              }}
            >
              <ChatBubbleOutlineIcon style={{ fontSize: "1.1rem" }} />
              Message {profile.fullName?.split(" ")[0] || profile.name?.split(" ")[0] || "Mentor"}
            </button>
          </div>
        )}

        {/* מנטור שזה הפרופיל שלו — לא רואה כפתור הזמנה */}
        {isOwner && (
          <div style={{ marginBottom: "16px", padding: "12px", background: "#f5f5f5", borderRadius: "8px", color: "#666" }}>
            This is your profile. You cannot book your own lessons.
          </div>
        )}

        {/* משתמש לא מחובר או מנטור בלבד ללא תפקיד לומד */}
        {!isOwner && !canBook && loggedInUserId && (
          <div style={{ marginBottom: "16px" }}>
            <p style={{ color: "#8e7e74" }}>Only learners can book lessons. Update your role in your profile.</p>
          </div>
        )}

        {!loggedInUserId && (
          <div style={{ marginBottom: "16px" }}>
            <button className="waitlist-submit-btn" onClick={() => navigate("/auth/login")}>
              Log in to book a lesson
            </button>
          </div>
        )}

        {/* Availability Section */}
        <div className="availability-section-header" style={{ display: "flex", alignItems: "center", justifyContent: "space-between", flexWrap: "wrap", gap: "12px" }}>
          <h2 className="section-title" style={{ margin: 0 }}>Scheduled Availability Slots</h2>
          {isOwner && !showAvailabilityEditor && (
            <button className="profile-action-btn" onClick={openEditor} style={{ display: "flex", alignItems: "center", gap: "6px" }}>
              <AddCircleOutlineIcon style={{ fontSize: "1rem" }} />
              Manage Availability
            </button>
          )}
        </div>

        {saveMessage && (
          <p style={{ color: saveMessage.ok ? "#2e7d32" : "#c62828", marginBottom: "12px", fontWeight: 500 }}>
            {saveMessage.text}
          </p>
        )}

        {/* Availability Editor */}
        {showAvailabilityEditor && availabilitySlots && (
          <div className="availability-editor" style={{ background: "#FFF9F1", border: "1px solid #e0d6cc", borderRadius: "12px", padding: "20px", marginBottom: "24px" }}>
            <h3 style={{ marginTop: 0, color: "#2d1b10" }}>Edit your available hours</h3>
            {availabilitySlots.map((slot, i) => (
              <div key={i} className="availability-row" style={{ display: "flex", alignItems: "center", gap: "10px", marginBottom: "12px", flexWrap: "wrap" }}>
                <select className="avail-select" value={slot.dayOfWeek} onChange={(e) => handleSlotChange(i, "dayOfWeek", e.target.value)}>
                  {DAYS.map((day) => <option key={day} value={day.toUpperCase()}>{day}</option>)}
                </select>
                <input type="time" className="avail-time" value={slot.startTime} onChange={(e) => handleSlotChange(i, "startTime", e.target.value)} />
                <span className="avail-sep">–</span>
                <input type="time" className="avail-time" value={slot.endTime} onChange={(e) => handleSlotChange(i, "endTime", e.target.value)} />
                <IconButton size="small" onClick={() => handleRemoveSlot(i)} color="error" disabled={availabilitySlots.length === 1}>
                  <DeleteOutlineIcon fontSize="small" />
                </IconButton>
              </div>
            ))}
            <div style={{ display: "flex", gap: "12px", marginTop: "16px", flexWrap: "wrap" }}>
              <button className="profile-action-btn" onClick={handleAddSlot} style={{ display: "flex", alignItems: "center", gap: "6px" }}>
                <AddCircleOutlineIcon style={{ fontSize: "1rem" }} /> Add time slot
              </button>
              <button className="waitlist-submit-btn" onClick={handleSave} disabled={isSaving} style={{ display: "flex", alignItems: "center", gap: "6px" }}>
                <SaveIcon style={{ fontSize: "1rem" }} /> {isSaving ? "Saving..." : "Save availability"}
              </button>
              <button className="profile-action-btn" onClick={() => { setShowAvailabilityEditor(false); setSaveMessage(null); }}>Cancel</button>
            </div>
          </div>
        )}

        {/* Current Availability Display */}
        <div className="availability-list">
          {currentAvailability.length > 0
            ? currentAvailability.map((slot, i) => (
                <div key={slot.id ?? i} className="availability-item">
                  <span>{slot.dayOfWeek} from {slot.startTime} to {slot.endTime}</span>
                </div>
              ))
            : <div className="no-requests-box"><p>No availability hours configured yet.</p></div>}
        </div>

      </div>
    </LocalizationProvider>
  );
};

export default MentorProfile;
