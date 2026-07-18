import { useState, useMemo } from 'react';
import { Box, CircularProgress, Typography, IconButton } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import ClearIcon from '@mui/icons-material/Clear';
import SearchIcon from '@mui/icons-material/Search';
import FavoriteBorderIcon from '@mui/icons-material/FavoriteBorder';
import FavoriteIcon from '@mui/icons-material/Favorite';
import StarIcon from '@mui/icons-material/Star';
import LocationOnIcon from '@mui/icons-material/LocationOn';
import RefreshIcon from '@mui/icons-material/Refresh';
import { useGetMentorsQuery, useGetFavoriteSkillsQuery, useAddFavoriteSkillMutation, useRemoveFavoriteSkillMutation } from '../api/Api';
import AiAdvisor from '../components/AiAdvisor';
import './Home.css';

const STATIC_CATEGORIES = [
  { name: "Singing", icon: "🎤" },
  { name: "Programming", icon: "💻" },
  { name: "Fitness", icon: "🏃🏼‍♀️" },
  { name: "Ball games", icon: "⛹🏼‍♀️" },
  { name: "Painting", icon: "🎨" },
  { name: "Cooking", icon: "👩🏼‍🍳" },
  { name: "Baking", icon: "🧁" },
  { name: "Planning", icon: "🎼" }
];

export default function Home() {
  const navigate = useNavigate();
  const { data: mentors, isLoading, isError } = useGetMentorsQuery();
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('');

  // מועדפים
  const userId = sessionStorage.getItem('userId');
  const isLearner = (() => {
    try { const r = JSON.parse(sessionStorage.getItem('user') || '{}').role || sessionStorage.getItem('role'); return r === 'LEARNER' || r === 'MENTOR_AND_LEARNER'; } catch { return false; }
  })();
  const { data: favoritesRaw } = useGetFavoriteSkillsQuery(userId, { skip: !userId || !isLearner });
  const favorites = Array.isArray(favoritesRaw) ? favoritesRaw : [];
  const [addFavorite] = useAddFavoriteSkillMutation();
  const [removeFavorite] = useRemoveFavoriteSkillMutation();

  const handleToggleFavorite = async (e, skillId) => {
    e.stopPropagation();
    if (!skillId) return; 
    if (!userId) { navigate('/auth/login'); return; }
    if (!isLearner) return;
    const isFav = favorites.some(f => f.id === skillId);
    try {
      if (isFav) {
        await removeFavorite({ learnerId: Number(userId), skillId }).unwrap();
      } else {
        await addFavorite({ learnerId: Number(userId), skillId }).unwrap();
      }
    } catch (err) {
      console.error('Failed to toggle favorite:', err);
    }
  };

  const featuredSkills = useMemo(() => {
    if (!mentors || !Array.isArray(mentors)) return [];
    return mentors.flatMap((mentor) => {
      const skillNames = mentor.skillNames || [];
      const imageSkills = mentor.imageSkills || [];
      const skillCategories = mentor.skillCategories || [];
      const skillIds = mentor.skillIds || [];
      const skillCosts = mentor.skillCosts || [];

      const skillLevels = mentor.skillLevels || [];
      return skillNames
        .filter(name => name && name !== "No skills")
        .map((skillName, index) => ({
          skillId: skillIds[index] || null,
          mentorId: mentor.mentorId || mentor.id,
          fullName: mentor.fullName || mentor.name || "Unknown Mentor",
          averageRating: mentor.averageRating ? Number(mentor.averageRating).toFixed(1) : "0.0",
          reviewsCount: mentor.reviewsCount || 0,
          skillName,
          category: (skillCategories[index] || "").toLowerCase(),
          price: skillCosts[index] != null ? skillCosts[index] : "—",
          location: mentor.city || mentor.location || "Israel",
          imageSkill: imageSkills[index] || `https://picsum.photos/seed/${encodeURIComponent(skillName)}/300/200`,
          level: skillLevels[index] || null,
        }));
    });
  }, [mentors]);
  const categoriesWithCounts = useMemo(() => {
    return STATIC_CATEGORIES.map(cat => {
      const count = featuredSkills.filter(item =>
        item.category === cat.name.toLowerCase()
      ).length;
      return { ...cat, count: `${count} ${count === 1 ? 'skill' : 'skills'}` };
    });
  }, [featuredSkills]);

 
  const filteredSkills = useMemo(() => {
    return featuredSkills.filter((item) => {
      const matchesSearch =
        item.skillName.toLowerCase().includes(searchQuery.toLowerCase()) ||
        item.fullName.toLowerCase().includes(searchQuery.toLowerCase());
      const matchesCategory = selectedCategory
        ? item.category === selectedCategory.toLowerCase()
        : true;
      return matchesSearch && matchesCategory;
    });
  }, [featuredSkills, searchQuery, selectedCategory]);

  
  const handleResetFilters = () => {
    // אם המשתמש חיפש משהו עליון וגם בחר קטגוריה, ננקה קודם את הקטגוריה כדי להחזיר את תוצאות החיפוש
    if (selectedCategory && searchQuery) {
      setSelectedCategory('');
    } else {
      // אם הוא רק חיפש או רק בחר קטגוריה - ננקה את הכל
      setSearchQuery('');
      setSelectedCategory('');
    }
  };

  if (isLoading) {
    return (
      <div className="home-loading-box">
        <CircularProgress sx={{ color: '#bc4a23' }} />
        <div className="home-loading-text">Loading SkillMate...</div>
      </div>
    );
  }

  if (isError) {
    return (
      <div className="home-error-box">
        <Typography color="error" variant="h6">An error occurred while loading data.</Typography>
      </div>
    );
  }

  return (
    <div className="home-main-wrapper">

      {/* אזור ה-Hero */}
      <div className="home-hero-container">
        <h1 className="home-hero-title" style={{ fontSize: '3.5rem', color: '#2d1b10' }}>
          Learn anything,<br />from someone who lives it.
        </h1>
        <p className="home-hero-subtitle" style={{ fontSize: '1.2rem', color: '#6e5d53' }}>
          Book one-on-one lessons with verified mentors in music, programming, cooking and dozens of other crafts.
        </p>

        {/* תיבת החיפוש */}
        <div className="home-search-bar-box">
          <SearchIcon sx={{ color: '#8e7e74', ml: 1 }} />
          <input
            type="text"
            className="home-search-input"
            placeholder="Try 'guitar', 'React', or 'sourdough'..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
          />
          {searchQuery && (
            <IconButton onClick={() => setSearchQuery('')} size="small">
              <ClearIcon />
            </IconButton>
          )}
          <button
            className="home-search-btn"
            style={{ backgroundColor: '#bc4a23', color: 'white', border: 'none', padding: '10px 24px', borderRadius: '25px', cursor: 'pointer', fontWeight: 'bold' }}
          >
            Search
          </button>
        </div>
      </div>

      <div style={{ maxWidth: '1200px', margin: '0 auto', padding: '0 24px' }}>

        {/* אזור קטגוריות */}
        <div className="home-categories-section">
          <div className="home-categories-header">
            <div>
              <h2 className="home-categories-title" style={{ fontSize: '2rem' }}>Browse by category</h2>
              <p style={{ color: '#6e5d53' }}>Pick a craft to get started.</p>
            </div>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(130px, 1fr))', gap: '16px' }}>
            {categoriesWithCounts.map((cat) => {
              const isSelected = selectedCategory.toLowerCase() === cat.name.toLowerCase();
              return (
                <div
                  key={cat.name}
                  className={`home-category-card ${isSelected ? 'selected' : ''}`}
                  onClick={() => setSelectedCategory(isSelected ? '' : cat.name)} // 👈 כאן מתבצע השחרור בלחיצה חוזרת!
                >
                  <span className="home-category-icon" style={{ fontSize: '2rem' }}>{cat.icon}</span>
                  <div className="home-category-name" style={{ fontSize: '1rem', marginTop: '8px' }}>{cat.name}</div>
                  <div style={{ fontSize: '0.75rem', color: '#8e7e74', marginTop: '4px' }}>{cat.count}</div>
                </div>
              );
            })}
          </div>
        </div>

        {/* אזור השיעורים  */}
        <div className="home-featured-section">
          {filteredSkills.length > 0 ? (
            <>
              <h2 className="home-featured-title" style={{ fontSize: '2rem' }}>
                {selectedCategory ? `${selectedCategory} Mentors` : 'Featured mentors'}
              </h2>
              <p className="home-featured-subtitle" style={{ color: '#6e5d53', marginBottom: '32px' }}>
                {filteredSkills.length} mentors available
              </p>

              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(320px, 1fr))', gap: '24px' }}>
                {filteredSkills.map((item, idx) => (
                  <div
                    className="home-mentor-card"
                    key={`${item.mentorId}-${idx}`}
                    style={{ backgroundColor: 'white', overflow: 'hidden', cursor: 'pointer' }}
                 
                  >

                    <div className="home-mentor-card-image-wrapper">
                      <img src={item.imageSkill} alt={item.skillName} />
                      <IconButton
                        style={{ position: 'absolute', top: '12px', right: '12px', backgroundColor: 'white', zIndex: 1 }}
                        size="small"
                        onClick={(e) => handleToggleFavorite(e, item.skillId)}
                        title={item.skillId && favorites?.some(f => f.id === item.skillId) ? "Remove from favorites" : "Add to favorites"}
                      >
                        {item.skillId && favorites?.some(f => f.id === item.skillId)
                          ? <FavoriteIcon size="small" style={{ color: '#bc4a23' }} />
                          : <FavoriteBorderIcon size="small" style={{ color: '#bc4a23' }} />}
                      </IconButton>
                    </div>

                    <div className="home-card-content">
                      <div className="home-card-header">
                        <div>
                          <h3 className="home-skill-name" style={{ fontSize: '1.25rem', margin: 0 }}>{item.skillName}</h3>
                          <p style={{ margin: '4px 0', color: '#6e5d53', fontSize: '0.95rem' }}>by {item.fullName}</p>
                          {item.level && (
                            <span className={`home-level-badge home-level-badge--${item.level.toLowerCase()}`}>
                              {item.level.charAt(0) + item.level.slice(1).toLowerCase()}
                            </span>
                          )}
                        </div>
                        <div className="home-rating-box">
                          <StarIcon className="home-star-icon" style={{ fontSize: '1.1rem' }} />
                          <span style={{ fontWeight: 'bold', fontSize: '0.95rem' }}>{item.averageRating}</span>
                          <span style={{ color: '#8e7e74', fontSize: '0.85rem', marginLeft: '2px' }}>({item.reviewsCount})</span>
                        </div>
                      </div>

                      <div style={{ display: 'flex', alignItems: 'center', color: '#8e7e74', fontSize: '0.85rem', marginBottom: '16px' }}>
                        <LocationOnIcon style={{ fontSize: '1rem', marginRight: '4px' }} />
                        {item.location}
                      </div>

                      <div className="home-card-footer">
                        <div className="home-price-text" style={{ fontSize: '1.1rem' }}>
                          ${item.price}<span style={{ fontWeight: 'normal', fontSize: '0.85rem', color: '#6e5d53' }}>/hr</span>
                        </div>
                        <button
                            onClick={() => navigate(`/mentorProfile/${item.mentorId}`, { state: { preselectedSkillId: item.skillId } })}
                          style={{ border: '1px solid #e0e0e0', backgroundColor: 'orange', padding: '6px 16px', borderRadius: '8px', cursor: 'pointer', fontWeight: '500' }}
                        >
                          Book lesson
                        </button>
                      </div>
                    </div>

                  </div>
                ))}
              </div>
            </>
          ) : (
            /*  כרטיס איפוס סינונים מעוצב */
            <div style={{
              textAlign: 'center',
              padding: '64px 24px',
              backgroundColor: 'white',
              borderRadius: '20px',
              border: '1px solid #e0e0e0',
              maxWidth: '500px',
              margin: '40px auto'
            }}>
              <Typography variant="h5" style={{ fontFamily: 'serif', fontWeight: 'bold', color: '#2d1b10', marginBottom: '8px' }}>
                No mentors found
              </Typography>
              <Typography style={{ color: '#6e5d53', marginBottom: '24px' }}>
                We couldn't find any results matching your search or selected category. Try clearing the filters to start over.
              </Typography>
              <button
                onClick={handleResetFilters}
                style={{
                  backgroundColor: '#bc4a23',
                  color: 'white',
                  border: 'none',
                  padding: '12px 28px',
                  borderRadius: '30px',
                  cursor: 'pointer',
                  fontWeight: 'bold',
                  display: 'inline-flex',
                  alignItems: 'center',
                  gap: '8px',
                  boxShadow: '0px 2px 4px rgba(188, 74, 35, 0.2)'
                }}
              >
                <RefreshIcon style={{ fontSize: '1.2rem' }} />
                {selectedCategory && searchQuery ? 'Clear Category Filter' : 'Clear All Filters'}
              </button>
            </div>
          )}
        </div>
    </div>
    <AiAdvisor />
  </div>
  );
}