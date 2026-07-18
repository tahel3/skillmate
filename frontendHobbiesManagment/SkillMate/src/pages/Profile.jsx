import React, { useState, useEffect } from 'react';
import './Profile.css';

const Profile = () => {
    const [user, setUser] = useState(null);
    const [formData, setFormData] = useState({});
    const [fetchError, setFetchError] = useState(null);

    const userId = sessionStorage.getItem('userId');

    useEffect(() => {
        if (!userId) {
            setFetchError("User ID not found. Please log in again.");
            return;
        }

        fetch(`http://localhost:8080/api/users/${userId}`, {
            headers: {
                'Authorization': `Bearer ${sessionStorage.getItem('token')}`
            }
        })
            .then(res => {
                if (!res.ok) throw new Error("Failed to load profile. User might not exist or session expired.");
                return res.json();
            })
            .then(data => {
                // Get role from sessionStorage (same source as useAuth hook)
                const userString = sessionStorage.getItem("user");
                const userObj = userString ? JSON.parse(userString) : {};
                const role = userObj.role || sessionStorage.getItem("role") || 'LEARNER';
                const selectedRole = role; // already in correct format: LEARNER / MENTOR / MENTOR_AND_LEARNER

                setUser(data);
                setFormData({ ...data, selectedRole, password: '' });
            })
            .catch(err => {
                console.error("Error in profile fetch:", err);
                setFetchError(err.message);
            });
    }, [userId]);

    const handleUpdate = (e) => {
        e.preventDefault();

        const isConfirmed = window.confirm("The details will be updated and you will need to reconnect. Continue?");
        if (!isConfirmed) return;

        // Don't send empty password to the server
        const payload = { ...formData };
        if (!payload.password) {
            delete payload.password;
        }

        console.log("Sending update payload:", payload);

        fetch(`http://localhost:8080/api/users/${user.idNumber}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${sessionStorage.getItem('token')}`
            },
            body: JSON.stringify(payload)
        })
            .then(res => {
                if (!res.ok) throw new Error("Error updating");
                return res.json();
            })
            .then(() => {
                sessionStorage.clear();
                alert("Update completed successfully! Please reconnect.");
                window.location.href = '/auth/login';
            })
            .catch(err => {
                console.error(err);
                alert("There was a problem updating the details");
            });
    };

    if (fetchError) return <div className="error-message-container"><p>{fetchError}</p></div>;
    if (!user) return <p>Loading...</p>;

    return (
        <div className="profile-container">
            <h2>Update details:</h2>
            <form onSubmit={handleUpdate} className="profile-form">
                <label>ID:</label>
                <input type="text" value={user.idNumber || ''} disabled className="disabled-field" />

                <label>Role:</label>
                <select
                    value={formData.selectedRole || 'LEARNER'}
                    onChange={e => setFormData({ ...formData, selectedRole: e.target.value })}
                    className="form-select"
                >
                    <option value="LEARNER">Learner</option>
                    <option value="MENTOR">Mentor</option>
                    <option value="MENTOR_AND_LEARNER">Mentor &amp; Learner</option>
                </select>

                <label>Date of birth:</label>
                <input type="date" value={user.birthday || ''} disabled className="disabled-field" />

                <label>Name:</label>
                <input type="text" value={formData.name || ''} onChange={e => setFormData({ ...formData, name: e.target.value })} />

                <label>Email:</label>
                <input type="email" value={formData.email || ''} onChange={e => setFormData({ ...formData, email: e.target.value })} />

                <label>Gender:</label>
                <select
                    value={formData.gender || 'MALE'}
                    onChange={e => setFormData({ ...formData, gender: e.target.value })}>
                    <option value="MALE">Male</option>
                    <option value="FEMALE">Female</option>
                </select>

                <label>New password (leave blank if you don't want to change):</label>
                <input type="password" onChange={e => setFormData({ ...formData, password: e.target.value })} />

                <label>City:</label>
                <input type="text" value={formData.city || ''} onChange={e => setFormData({ ...formData, city: e.target.value })} />

                <button type="submit" className="save-button">Save changes</button>
            </form>
        </div>
    );
};

export default Profile;