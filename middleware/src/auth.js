const jwt = require('jsonwebtoken');

const JWT_SECRET = process.env.JWT_SECRET || 'inventory-secret-key-change-in-prod';
const JWT_EXPIRY = '24h';

// Hardcoded users for demo — in production this would query a database
const USERS = [
    { id: 1, username: 'admin', password: 'admin123', role: 'ADMIN' },
    { id: 2, username: 'manager', password: 'manager123', role: 'MANAGER' },
    { id: 3, username: 'viewer', password: 'viewer123', role: 'VIEWER' },
];

// Generate JWT token for a user
const generateToken = (user) => {
    const payload = {
        id: user.id,
        username: user.username,
        role: user.role,
    };
    return jwt.sign(payload, JWT_SECRET, { expiresIn: JWT_EXPIRY });
};

// Verify JWT token from request header
const verifyToken = (req, res, next) => {
    const authHeader = req.headers['authorization'];
    const token = authHeader && authHeader.split(' ')[1]; // Bearer <token>

    if (!token) {
        return res.status(401).json({ error: 'No token provided' });
    }

    try {
        const decoded = jwt.verify(token, JWT_SECRET);
        req.user = decoded;
        next();
    } catch (err) {
        return res.status(401).json({ error: 'Invalid or expired token' });
    }
};

// Login — find user and generate token
const login = (username, password) => {
    const user = USERS.find(
        u => u.username === username && u.password === password
    );
    if (!user) return null;
    return {
        token: generateToken(user),
        user: { id: user.id, username: user.username, role: user.role },
    };
};

module.exports = { generateToken, verifyToken, login };