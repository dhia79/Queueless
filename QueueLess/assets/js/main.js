// Helper Functions and Global Methods
const API_BASE = 'api/';

// Auth checking
function requireAuth(allowedRole = null) {
    const userJson = localStorage.getItem('user');
    if (!userJson) {
        window.location.href = 'login.html';
        return;
    }

    try {
        const user = JSON.parse(userJson);
        if (allowedRole && user.role !== allowedRole && user.role !== 'admin') {
            // Automatically correct their path based on their actual role
            if (user.role === 'business') window.location.href = 'business_dashboard.html';
            else if (user.role === 'customer') window.location.href = 'user_dashboard.html';
            else if (user.role === 'admin') window.location.href = 'admin_dashboard.html';
            else window.location.href = 'index.html';
            return;
        }
    } catch (e) {
        window.location.href = 'login.html';
    }
}

function getAuthUser() {
    const json = localStorage.getItem('user');
    return json ? JSON.parse(json) : null;
}

async function logout() {
    try {
        await fetch(API_BASE + 'auth.php?action=logout');
    } catch (e) {}
    localStorage.removeItem('user');
    localStorage.removeItem('active_queue');
    window.location.href = 'login.html';
}

// Global API Fetch helper
async function apiCall(action, method = 'GET', body = null, endpoint = 'auth.php') {
    const options = {
        method,
        headers: {
            'Content-Type': 'application/json'
        }
    };

    if (body && (method === 'POST' || method === 'PUT')) {
        options.body = JSON.stringify(body);
    }

    const url = `${API_BASE}${endpoint}?action=${action}`;
    
    try {
        const response = await fetch(url, options);
        if (!response.ok) {
            if (window.QueueLessDebug) window.QueueLessDebug.addLog(action, 'error', url);
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        const data = await response.json();
        if (window.QueueLessDebug) window.QueueLessDebug.addLog(action, 'success', url);
        return data;
    } catch (error) {
        if (window.QueueLessDebug) window.QueueLessDebug.addLog(action, 'error', url);
        console.error('API call failed:', error);
        throw error;
    }
}

// Auto-inject Debug Console
const debugScript = document.createElement('script');
debugScript.src = 'assets/js/debug.js';
document.head.appendChild(debugScript);
