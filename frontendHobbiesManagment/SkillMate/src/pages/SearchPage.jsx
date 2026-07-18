import { useState, useEffect } from "react";
import { useSearchSkillsQuery } from "../api/Api";
import { useNavigate } from "react-router-dom";
import {
  Box,
  TextField,
  Grid,
  Card,
  CardContent,
  Typography,
  Pagination,
  CircularProgress,
  Container,
  InputAdornment,
  Alert,
  MenuItem,
} from "@mui/material";
import SearchIcon from "@mui/icons-material/Search";
import dayjs from "dayjs";
import AiAdvisor from "../components/AiAdvisor";
import "./SearchPage.css";

const CATEGORIES = ["Singing", "Programming", "Fitness", "Ball games", "Painting", "Cooking", "Baking", "Planning"];

export const SearchPage = () => {
  const navigate = useNavigate();
  const [searchTerm, setSearchTerm] = useState("");
  const [selectedCategory, setSelectedCategory] = useState("");
  const [selectedDate, setSelectedDate] = useState("");
  const [debouncedName, setDebouncedName] = useState("");
  const [page, setPage] = useState(0);
  const [size] = useState(6);
  
  // State לשמירת יומני הזמינות של המנטורים שנטען מהשרת
  const [mentorAvailabilityMap, setMentorAvailabilityMap] = useState({});

  useEffect(() => {
    const timer = setTimeout(() => setDebouncedName(searchTerm), 400);
    return () => clearTimeout(timer);
  }, [searchTerm]);

  const { data, isLoading, isError } = useSearchSkillsQuery({
    name: debouncedName,
    category: selectedCategory,
    page,
    size,
  });

  const handlePageChange = (_event, value) => setPage(value - 1);

  // פונקציית בדיקה האם מנטור פנוי בתאריך מסוים
  const isMentorAvailableOnDate = (mentorId, dateValue) => {
    if (!mentorId || !dateValue) return true;
    const calendar = mentorAvailabilityMap[mentorId];
    if (!calendar) return true; // אם הנתונים עדיין בטעינה, מציגים כדי לא להסתיר בטעות

    const selected = dayjs(dateValue);
    const dayName = selected.format("dddd").toUpperCase();
    
    // מוודאים שיום בשבוע פנוי ושאינו כלול במועדים התפוסים
   const isDayAvailable = (calendar.availableDays || []).includes(dayName);
    const bookedDates = calendar.bookedDates || [];
    const isBooked = bookedDates.some((d) => d.startsWith(selected.format("YYYY-MM-DD")));

    return isDayAvailable && !isBooked;
  };

  // טעינת זמינות המנטורים עבור המיומנויות שמוצגות בעמוד הנוכחי
  useEffect(() => {
    const skillsList = data?.content || [];
    if (!skillsList.length) return;

    const uniqueMentorIds = [...new Set(skillsList.map((skill) => skill.mentorId).filter(Boolean))];

    const loadAvailability = async () => {
      const results = await Promise.all(
        uniqueMentorIds.map(async (mentorId) => {
          try {
            const response = await fetch(`http://localhost:8080/api/mentors/${mentorId}/available-dates`);
            if (!response.ok) return [mentorId, null];
            const payload = await response.json();
            return [mentorId, payload];
          } catch {
            return [mentorId, null];
          }
        })
      );

      const nextMap = {};
      results.forEach(([mentorId, payload]) => {
        if (payload) nextMap[mentorId] = payload;
      });

      setMentorAvailabilityMap((prev) => ({ ...prev, ...nextMap }));
    };

    loadAvailability();
  }, [data]);

  if (isLoading) {
    return (
      <div className="skills-center-flex-box">
        <CircularProgress size={50} sx={{ color: "#B85C38" }} />
        <Typography variant="h6" sx={{ mt: 2, color: "text.secondary" }}>
          Searching for relevant skills...
        </Typography>
      </div>
    );
  }

  if (isError) {
    return (
      <div className="skills-center-flex-box" style={{ padding: "24px" }}>
        <Alert severity="error" variant="filled" style={{ borderRadius: "12px", maxWidth: "450px" }}>
          Error communicating with the server. Ensure the backend server is running correctly.
        </Alert>
      </div>
    );
  }

  const skillsList = data?.content || [];
  
  // מסנן את המיומנויות בעמוד לפי התאריך הנבחר (במידה והוזן)
  const filteredSkills = selectedDate
    ? skillsList.filter((skill) => isMentorAvailableOnDate(skill.mentorId, selectedDate))
    : skillsList;

  const totalPages = data?.totalPages || 0;
  const totalElements = filteredSkills.length;

  return (
    <div className="skills-main-container">
      <Container maxWidth="xl">

        <Box sx={{ mb: 4 }}>
          <Typography variant="h3" component="h1" className="skills-page-title">
            Explore Available Skills
          </Typography>
          <Typography variant="body1" color="textSecondary" sx={{ mt: 1 }}>
            Find the exact study materials and skills you are looking for from our mentors.
          </Typography>
        </Box>

        {/* Search and Category Filter — MUI v6+ API */}
        <div className="skills-search-section">
          <Grid container spacing={2}>
            <Grid item xs={12} sm={4}>
              <TextField
                fullWidth
                label="Search by skill name..."
                variant="outlined"
                value={searchTerm}
                onChange={(e) => { setSearchTerm(e.target.value); setPage(0); }}
                InputProps={{
                  startAdornment: (
                    <InputAdornment position="start">
                      <SearchIcon sx={{ color: "#B85C38" }} />
                    </InputAdornment>
                  ),
                }}
                className="skills-text-field-custom"
              />
            </Grid>
            <Grid item xs={12} sm={4}>
              <TextField
                select
                fullWidth
                label="Filter by category"
                variant="outlined"
                value={selectedCategory}
                onChange={(e) => { setSelectedCategory(e.target.value); setPage(0); }}
                className="skills-text-field-custom"
              >
                <MenuItem value="">All categories</MenuItem>
                {CATEGORIES.map((cat) => (
                  <MenuItem key={cat} value={cat}>{cat}</MenuItem>
                ))}
              </TextField>
            </Grid>
            <Grid item xs={12} sm={4}>
              <TextField
                fullWidth
                label="Filter by date"
                type="date"
                variant="outlined"
                value={selectedDate}
                onChange={(e) => { setSelectedDate(e.target.value); setPage(0); }}
                InputLabelProps={{ shrink: true }}
                className="skills-text-field-custom"
              />
            </Grid>
          </Grid>
        </div>

        <Box sx={{ mb: 3, display: "flex", justifyContent: "space-between", alignItems: "center" }}>
          <Typography variant="body2" color="textSecondary">
            Found <strong>{totalElements}</strong> relevant skills in this page
          </Typography>
        </Box>

        {filteredSkills.length === 0 ? (
          <Alert severity="info" style={{ borderRadius: "12px", marginTop: "16px" }}>
            No skills found matching your current search criteria.
          </Alert>
        ) : (
          <Grid container spacing={3}>
            {filteredSkills.map((skill) => (
              <Grid item xs={12} sm={6} md={4} key={skill.id}>
                <Card
                  className="skills-skill-card"
                  onClick={() => skill.mentorId && navigate(`/mentorProfile/${skill.mentorId}`, { state: { preselectedSkillId: skill.id } })}
                  style={{ cursor: skill.mentorId ? "pointer" : "default" }}
                >
                  <img
                    src={skill.image || `https://picsum.photos/seed/${encodeURIComponent(skill.name || "skill")}/300/200`}
                    alt={skill.name}
                    className="skills-card-image"
                  />
                  <CardContent className="skills-card-content-custom">
                    <Box>
                      <div className="skills-card-header-row">
                        <Typography variant="h6" className="skills-name-text">{skill.name}</Typography>
                        <div className="skills-category-badge">{skill.category || "General"}</div>
                      </div>
                      {skill.level && (
                        <span className={`skills-level-badge skills-level-badge--${skill.level.toLowerCase()}`}>
                          {skill.level.charAt(0) + skill.level.slice(1).toLowerCase()}
                        </span>
                      )}
                      <Typography variant="body2" className="skills-description-text">
                        {skill.description}
                      </Typography>
                      {skill.mentorId && (
                        <button
                          onClick={(e) => {
                            e.stopPropagation();
                            navigate(`/mentorProfile/${skill.mentorId}`, { state: { preselectedSkillId: skill.id } });
                          }}
                          style={{ marginTop: "12px", width: "100%", padding: "8px", backgroundColor: "#bc4a23", color: "white", border: "none", borderRadius: "8px", cursor: "pointer", fontWeight: "600" }}
                        >
                          Book this lesson
                        </button>
                      )}
                    </Box>
                  </CardContent>
                </Card>
              </Grid>
            ))}
          </Grid>
        )}

        {totalPages > 1 && (
          <div className="skills-pagination-wrapper">
            <Pagination
              count={totalPages}
              page={page + 1}
              onChange={handlePageChange}
              size="large"
              className="skills-pagination-custom"
            />
          </div>
        )}
      </Container>
      <AiAdvisor />
    </div>
  );
};

export default SearchPage;