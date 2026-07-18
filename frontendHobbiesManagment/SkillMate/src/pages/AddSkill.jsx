import React, { useState } from 'react';
import { Box, TextField, MenuItem, Button, CircularProgress, Typography, Paper, InputAdornment } from '@mui/material';
import CloudUploadIcon from '@mui/icons-material/CloudUpload';
import './AddSkill.css';

const CATEGORIES = ["Singing", "Programming", "Fitness", "Ball games", "Painting", "Cooking", "Baking", "Planning"];

const LEVELS = ["BEGINNER", "ADVANCED", "EXPERT"];

export default function AddSkill() {
  const [formData, setFormData] = useState({
    name: '',
    cost: '',
    description: '',
    level: 'BEGINNER',
    category: 'Programming',
    image: ''
  });

  const [isLoading, setIsLoading] = useState(false);
  const [message, setMessage] = useState({ text: '', isError: false });

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };
const handleSubmit = async (e) => {
    e.preventDefault();
    setIsLoading(true);
    setMessage({ text: '', isError: false });

    const token = sessionStorage.getItem('token');
    const payload = { ...formData, cost: parseFloat(formData.cost) || 0.0 };

    try {
      const response = await fetch('http://localhost:8080/api/mentors/my-skills', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify(payload),
      });

      if (response.ok) {
        const data = await response.json();
        setMessage({ text: `Skill "${payload.name}" added to your profile successfully!`, isError: false });
        setFormData({ name: '', cost: '', description: '', level: 'BEGINNER', category: 'Programming', image: '' });
      } else {
        const errorText = await response.text();
        setMessage({ text: errorText || 'Failed to add skill. Please try again.', isError: true });
      }
    } catch (error) {
      setMessage({ text: 'Network error. Cannot connect to server.', isError: true });
    } finally {
      setIsLoading(false);
    }
  };
  return (
    <div className="add-skill-main-wrapper">
      <Paper elevation={0} className="add-skill-card-container">
        
        {/* כותרת הטופס */}
        <div className="add-skill-header-box">
          <h2 className="add-skill-title">Share a New Craft</h2>
          <p className="add-skill-subtitle">Fill in the details below to add a new skill to your mentor profile.</p>
        </div>

        {/* הודעות הצלחה או שגיאה */}
        {message.text && (
          <Box className={`add-skill-alert ${message.isError ? 'error' : 'success'}`}>
            <Typography variant="body2">{message.text}</Typography>
          </Box>
        )}

        {/* גוף הטופס */}
        <form onSubmit={handleSubmit} className="add-skill-form">
          
          <TextField
            label="Skill Name"
            name="name"
            value={formData.name}
            onChange={handleChange}
            fullWidth
            required
            placeholder="e.g., Advanced React Development, Sourdough Baking"
            variant="outlined"
            className="add-skill-input"
          />

          <div className="add-skill-row">
            <TextField
              select
              label="Category"
              name="category"
              value={formData.category}
              onChange={handleChange}
              fullWidth
              required
              variant="outlined"
            >
              {CATEGORIES.map((cat) => (
                <MenuItem key={cat} value={cat}>{cat}</MenuItem>
              ))}
            </TextField>

            <TextField
              select
              label="Skill Level"
              name="level"
              value={formData.level}
              onChange={handleChange}
              fullWidth
              required
              variant="outlined"
            >
              {LEVELS.map((lvl) => (
                <MenuItem key={lvl} value={lvl}>{lvl}</MenuItem>
              ))}
            </TextField>
          </div>

          <TextField
            label="Hourly Rate"
            name="cost"
            type="number"
            value={formData.cost}
            onChange={handleChange}
            fullWidth
            required
            placeholder="0.00"
            variant="outlined"
           slotProps={{
    input: {
      startAdornment: <InputAdornment position="start">$</InputAdornment>,
      min: 0, 
      step: "0.01"
    }
  }}
          />

          <TextField
            label="Image URL"
            name="image"
            value={formData.image}
            onChange={handleChange}
            fullWidth
            placeholder="https://example.com/image.jpg"
            variant="outlined"
            helperText="Provide a direct link to an image showcasing this craft."
          />

          <TextField
            label="Description"
            name="description"
            value={formData.description}
            onChange={handleChange}
            fullWidth
            required
            multiline
            rows={4}
            placeholder="Tell future learners what they will achieve in your sessions, your teaching methodology, or any required materials..."
            variant="outlined"
          />

          <Button
            type="submit"
            disabled={isLoading}
            variant="contained"
            className="add-skill-submit-btn"
            disableElevation
          >
            {isLoading ? <CircularProgress size={24} sx={{ color: 'white' }} /> : 'Publish Skill'}
          </Button>

        </form>
      </Paper>
    </div>
  );
}