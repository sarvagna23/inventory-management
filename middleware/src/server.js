require('dotenv').config();
const express = require('express');
const cors = require('cors');
const { createProxyMiddleware } = require('http-proxy-middleware');
const { verifyToken, login } = require('./auth');

const app = express();
const PORT = process.env.PORT || 4000;
const SPRING_BOOT_URL = process.env.SPRING_BOOT_URL || 'http://localhost:8080';

app.use(cors());
app.use(express.json());

// Health check — no auth needed
app.get('/health', (req, res) => {
    res.json({ status: 'ok', service: 'inventory-middleware' });
});

// Login endpoint — generates JWT token
app.post('/auth/login', (req, res) => {
    const { username, password } = req.body;

    if (!username || !password) {
        return res.status(400).json({ error: 'Username and password required' });
    }

    const result = login(username, password);
    if (!result) {
        return res.status(401).json({ error: 'Invalid credentials' });
    }

    console.log(`User logged in: ${username} (${result.user.role})`);
    res.json(result);
});

// All /api/* and /graphql routes require JWT auth then proxy to Spring Boot
app.use('/api', verifyToken, createProxyMiddleware({
    target: SPRING_BOOT_URL,
    changeOrigin: true,
    pathRewrite: (path) => '/api' + path,
    on: {
        proxyReq: (proxyReq, req) => {
            // Pass user info to Spring Boot via header
            proxyReq.setHeader('X-User-Id', req.user.id);
            proxyReq.setHeader('X-User-Role', req.user.role);
        },
        error: (err, req, res) => {
            console.error('Proxy error:', err.message);
            res.status(502).json({ error: 'Backend service unavailable' });
        },
    },
}));

app.use('/graphql', verifyToken, createProxyMiddleware({
    target: SPRING_BOOT_URL,
    changeOrigin: true,
    on: {
        error: (err, req, res) => {
            console.error('GraphQL proxy error:', err.message);
            res.status(502).json({ error: 'Backend service unavailable' });
        },
    },
}));

// 404 handler
app.use((req, res) => {
    res.status(404).json({ error: `Route not found: ${req.method} ${req.path}` });
});

app.listen(PORT, () => {
    console.log(`Middleware running on port ${PORT}`);
    console.log(`Proxying to Spring Boot at ${SPRING_BOOT_URL}`);
});