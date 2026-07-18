// src/api/userApi.js
const API_URL = 'http://localhost:8080/api/users';

export const fetchUserByEmail = async (email) => {
    const response = await fetch(`${API_URL}/by-email/${encodeURIComponent(email)}`);
    return response.json();
};

export const updateProfile = async (id, userData) => {
    const response = await fetch(`${API_URL}/${id}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(userData)
    });
    return response.json();
};