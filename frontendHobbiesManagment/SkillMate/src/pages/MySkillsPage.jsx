import { useEffect, useMemo, useState } from "react";
import {
  Alert,
  Box,
  Button,
  CircularProgress,
  Container,
  MenuItem,
  Paper,
  TextField,
  Typography,
} from "@mui/material";
import { useGetMyMentorProfileQuery, useGetSkillsByMentorQuery } from "../api/Api";

const LEVELS = ["BEGINNER", "ADVANCED", "EXPERT"];
const CATEGORIES = ["Singing", "Programming", "Fitness", "Ball games", "Painting", "Cooking", "Baking", "Planning"];

const MySkillsPage = () => {
  const [editingId, setEditingId] = useState(null);
  const [formData, setFormData] = useState({});
  const [message, setMessage] = useState(null);
  const [savingId, setSavingId] = useState(null);

  const {
    data: mentorProfile,
    isLoading: isLoadingMentor,
    isError: isMentorError,
  } = useGetMyMentorProfileQuery();
  const mentorId = mentorProfile?.mentorId;

 const {
    data: skillsRaw,
    isLoading: isLoadingSkills,
    refetch,
  } = useGetSkillsByMentorQuery(mentorId, { skip: !mentorId });

  const skills = Array.isArray(skillsRaw) ? skillsRaw : [];

  useEffect(() => {
    if (skills.length > 0) {
      const first = skills[0];
      setFormData((prev) => ({
        ...prev,
        ...Object.fromEntries(skills.map((skill) => [skill.id, skill])),
      }));
    }
  }, [skills]);

  const sortedSkills = useMemo(() => {
    return [...skills].sort((a, b) => (a.name || "").localeCompare(b.name || ""));
  }, [skills]);

  const handleFieldChange = (skillId, field, value) => {
    setFormData((prev) => ({
      ...prev,
      [skillId]: {
        ...(prev[skillId] || {}),
        [field]: value,
      },
    }));
  };

  const handleSave = async (skill) => {
    const payload = formData[skill.id];
    if (!payload) return;

    try {
      setSavingId(skill.id);
      setMessage(null);
      const token = sessionStorage.getItem("token");
      const response = await fetch(`http://localhost:8080/api/skill/${skill.id}`, {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({
          ...payload,
          cost: Number(payload.cost || 0),
        }),
      });

      if (!response.ok) {
        const text = await response.text();
        throw new Error(text || "Failed to update skill");
      }

      setEditingId(null);
      setMessage({ ok: true, text: `Updated "${payload.name}" successfully.` });
      await refetch();
    } catch (error) {
      setMessage({ ok: false, text: error.message || "Could not update this skill." });
    } finally {
      setSavingId(null);
    }
  };

  if (isLoadingMentor || isLoadingSkills) {
    return (
      <Container sx={{ py: 6, textAlign: "center" }}>
        <CircularProgress sx={{ color: "#B85C38" }} />
        <Typography sx={{ mt: 2 }}>Loading your skills...</Typography>
      </Container>
    );
  }

  if (isMentorError || !mentorId) {
    return (
      <Container sx={{ py: 6 }}>
        <Alert severity="warning">
          No mentor profile found. Please complete your mentor profile setup before managing skills.
        </Alert>
      </Container>
    );
  }

  return (
    <Container maxWidth="lg" sx={{ py: 6 }}>
      <Typography variant="h3" sx={{ mb: 1, color: "#2d1b10" }}>
        My Skills
      </Typography>
      <Typography variant="body1" color="text.secondary" sx={{ mb: 3 }}>
        Review and update your teaching skills.
      </Typography>

      {message && (
        <Alert severity={message.ok ? "success" : "error"} sx={{ mb: 3 }}>
          {message.text}
        </Alert>
      )}

      {sortedSkills.length === 0 ? (
        <Paper sx={{ p: 4, textAlign: "center", borderRadius: 3 }}>
          <Typography>No skills added yet.</Typography>
        </Paper>
      ) : (
        sortedSkills.map((skill) => {
          const current = formData[skill.id] || skill;
          const isEditing = editingId === skill.id;
          return (
            <Paper key={skill.id} sx={{ p: 3, mb: 3, borderRadius: 3 }}>
              <Box sx={{ display: "flex", justifyContent: "space-between", gap: 2, flexWrap: "wrap" }}>
                <Typography variant="h5" sx={{ color: "#2d1b10" }}>
                  {skill.name}
                </Typography>
                {!isEditing && (
                  <Button
                    variant="contained"
                    onClick={() => setEditingId(skill.id)}
                    sx={{ backgroundColor: "#bc4a23", '&:hover': { backgroundColor: '#a43f1c' } }}
                  >
                    Update
                  </Button>
                )}
              </Box>

              {isEditing ? (
                <Box component="form" sx={{ mt: 2, display: "grid", gap: 2 }}>
                  <TextField
                    label="Name"
                    value={current.name || ""}
                    onChange={(e) => handleFieldChange(skill.id, "name", e.target.value)}
                  />
                  <TextField
                    label="Category"
                    select
                    value={current.category || ""}
                    onChange={(e) => handleFieldChange(skill.id, "category", e.target.value)}
                  >
                    {CATEGORIES.map((cat) => (
                      <MenuItem key={cat} value={cat}>{cat}</MenuItem>
                    ))}
                  </TextField>
                  <TextField
                    label="Level"
                    select
                    value={current.level || "BEGINNER"}
                    onChange={(e) => handleFieldChange(skill.id, "level", e.target.value)}
                  >
                    {LEVELS.map((lvl) => (
                      <MenuItem key={lvl} value={lvl}>{lvl}</MenuItem>
                    ))}
                  </TextField>
                  <TextField
                    label="Cost"
                    type="number"
                    value={current.cost || 0}
                    onChange={(e) => handleFieldChange(skill.id, "cost", e.target.value)}
                  />
                  <TextField
                    label="Image URL"
                    value={current.image || ""}
                    onChange={(e) => handleFieldChange(skill.id, "image", e.target.value)}
                  />
                  <TextField
                    label="Description"
                    multiline
                    rows={4}
                    value={current.description || ""}
                    onChange={(e) => handleFieldChange(skill.id, "description", e.target.value)}
                  />
                  <Box sx={{ display: "flex", gap: 1, flexWrap: "wrap" }}>
                    <Button
                      variant="contained"
                      onClick={() => handleSave(skill)}
                      disabled={savingId === skill.id}
                      sx={{ backgroundColor: "#bc4a23", '&:hover': { backgroundColor: '#a43f1c' } }}
                    >
                      {savingId === skill.id ? "Saving..." : "Save"}
                    </Button>
                    <Button variant="outlined" onClick={() => setEditingId(null)}>
                      Cancel
                    </Button>
                  </Box>
                </Box>
              ) : (
                <Box sx={{ mt: 2, display: "grid", gap: 1 }}>
                  <Typography><strong>Category:</strong> {skill.category || "—"}</Typography>
                  <Typography><strong>Level:</strong> {skill.level || "—"}</Typography>
                  <Typography><strong>Cost:</strong> ${skill.cost || 0}</Typography>
                  <Typography><strong>Description:</strong> {skill.description || "—"}</Typography>
                </Box>
              )}
            </Paper>
          );
        })
      )}
    </Container>
  );
};

export default MySkillsPage;
