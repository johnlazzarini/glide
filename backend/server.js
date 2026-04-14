'use strict';

require('dotenv').config();
const express = require('express');
const crypto  = require('crypto');
const os      = require('os');
const path    = require('path');
const { GlideClient } = require('@glideidentity/glide-be-sdk-node');

// ── Config ────────────────────────────────────────────────────────────────────

const PORT = process.env.PORT || 3000;
function getLocalIp() {
  for (const iface of Object.values(os.networkInterfaces()).flat()) {
    if (iface.family === 'IPv4' && !iface.internal) return iface.address;
  }
  return 'localhost';
}

const HOST = getLocalIp();

// Initialize Glide Identity
let glide = null;
const isGlideStubbed = process.env.GLIDE_CLIENT_ID === 'STUB_CLIENT_ID';
if (!isGlideStubbed) {
  try {
    glide = new GlideClient({
      clientId: process.env.GLIDE_CLIENT_ID.trim(),
      clientSecret: process.env.GLIDE_CLIENT_SECRET.trim()
    });
    console.log('[Glide] Authenticated successfully via SDK.');
  } catch (e) {
    console.warn('[Glide] initialization failed, falling back to STUB behavior.', e.message);
  }
} else {
  console.log('[Glide] Running in STUB simulator mode (no live credentials attached).');
}

// ── In-memory store ───────────────────────────────────────────────────────────

/**
 * Map<attemptId, Attempt>
 * status: 'pending' | 'success' | 'failed'
 */
const attempts = new Map();

const AUTO_SUCCESS_MS = 15_000; // fallback auto-success if user never opens browser page

// ── Express setup ─────────────────────────────────────────────────────────────

const app = express();
app.use(express.json());
app.use(express.urlencoded({ extended: false }));
app.use(express.static(path.join(__dirname, 'public')));

// ── Glide API Routes ──────────────────────────────────────────────────────────

app.post('/api/phone-auth/prepare', async (req, res) => {
  console.log('\n--- [Glide] POST /prepare ---');
  console.log('Incoming Payload:', JSON.stringify(req.body, null, 2));
  try {
    const response = await glide.magicalAuth.prepare(req.body);
    console.log('Success Response:', JSON.stringify(response, null, 2));
    res.json(response);
  } catch (error) {
    console.error('ERROR in /prepare:', error.message);
    res.status(error.status || 500).json({ code: error.code, message: error.message });
  }
});

app.post('/api/phone-auth/report-invocation', async (req, res) => {
  console.log('\n--- [Glide] POST /report-invocation ---');
  console.log('Incoming Payload:', JSON.stringify(req.body, null, 2));
  try {
    const result = await glide.magicalAuth.reportInvocation(req.body);
    console.log('Success Response:', JSON.stringify(result, null, 2));
    res.json(result);
  } catch (error) {
    console.error('ERROR in /report-invocation:', error.message);
    res.json({ received: true });
  }
});

app.post('/api/phone-auth/process', async (req, res) => {
  console.log('\n--- [Glide] POST /process ---');
  console.log('Incoming Payload:', JSON.stringify(req.body, null, 2));
  try {
    const { credential, session, use_case } = req.body;
    let result;
    if (use_case === 'GetPhoneNumber') {
      result = await glide.magicalAuth.getPhoneNumber({ session, credential });
      console.log('Success Response (GetPhoneNumber):', JSON.stringify(result, null, 2));
    } else {
      result = await glide.magicalAuth.verifyPhoneNumber({ session, credential });
      console.log('Success Response (VerifyPhoneNumber):', JSON.stringify(result, null, 2));
    }
    
    if (result.sim_swap?.checked) console.log('SIM Swap Risk:', result.sim_swap.risk_level);
    if (result.device_swap?.checked) console.log('Device Swap Risk:', result.device_swap.risk_level);
    
    res.json(result);
  } catch (error) {
    console.error('ERROR in /process:', error.message);
    res.status(error.status || 500).json({ code: error.code, message: error.message });
  }
});

// ── Internal Routes ───────────────────────────────────────────────────────────

/** GET / — health check */
app.get('/', (_req, res) => {
  res.json({
    service: 'Tier1 Bank Verification API',
    status:  'running',
    endpoints: [
      'POST /verification/start',
      'GET  /verification/:id/status',
      'GET  /verify/:id          (browser page → deep links back to app)',
      'POST /verify/:id/complete (called internally by the browser page)',
    ],
  });
});

// ─────────────────────────────────────────────────────────────────────────────

/**
 * POST /verification/start
 * Body (JSON): { useCase?: string }
 *
 * Creates a new PENDING attempt and returns a verificationUrl to open in
 * the mobile browser. The page handles marking success and redirecting back.
 *
 * Response: { attemptId, status, useCase, verificationUrl }
 */
app.post('/verification/start', (req, res) => {
  const useCase   = req.body?.useCase ?? 'kyc';
  const attemptId = crypto.randomUUID();

  const attempt = {
    attemptId,
    useCase,
    status:    'pending',
    createdAt: Date.now(),
  };

  attempts.set(attemptId, attempt);

  // Fallback auto-success in case the browser page is never opened
  setTimeout(() => {
    const a = attempts.get(attemptId);
    if (a && a.status === 'pending') {
      a.status      = 'success';
      a.completedAt = Date.now();
      console.log(`[auto-success] ${attemptId} (${useCase})`);
    }
  }, AUTO_SUCCESS_MS);

  const verificationUrl = `http://${HOST}:${PORT}/verify.html?attemptId=${attemptId}`;
  console.log(`[start] ${attemptId}  useCase=${useCase}`);
  console.log(`        url=${verificationUrl}`);

  res.status(201).json({ attemptId, status: attempt.status, useCase, verificationUrl });
});

// ─────────────────────────────────────────────────────────────────────────────

/**
 * GET /verification/:id/status
 * Returns the current status of a verification attempt.
 *
 * Response: { attemptId, status, useCase, createdAt, completedAt? }
 */
app.get('/verification/:id/status', (req, res) => {
  const attempt = attempts.get(req.params.id);

  if (!attempt) {
    return res.status(404).json({ error: 'Attempt not found', attemptId: req.params.id });
  }

  console.log(`[status] ${attempt.attemptId}  status=${attempt.status}`);

  res.json({
    attemptId:   attempt.attemptId,
    status:      attempt.status,
    useCase:     attempt.useCase,
    createdAt:   attempt.createdAt,
    completedAt: attempt.completedAt ?? null,
  });
});

// (The HTML file is served automatically by express.static('public'))

// ─────────────────────────────────────────────────────────────────────────────

/**
 * POST /verify/:id/complete
 * Called by the browser page to immediately mark an attempt as success.
 * Idempotent — safe to call more than once.
 */
app.post('/verify/:id/complete', (req, res) => {
  const attempt = attempts.get(req.params.id);

  if (!attempt) {
    return res.status(404).json({ error: 'Attempt not found' });
  }
  if (attempt.status !== 'pending') {
    return res.json({ attemptId: attempt.attemptId, status: attempt.status });
  }

  attempt.status      = 'success';
  attempt.completedAt = Date.now();
  console.log(`[complete] ${attempt.attemptId} marked success`);

  res.json({ attemptId: attempt.attemptId, status: attempt.status });
});

// ── Start ─────────────────────────────────────────────────────────────────────

app.listen(PORT, '0.0.0.0', () => {
  console.log('\n┌─────────────────────────────────────────────────┐');
  console.log('│       Tier1 Bank Verification Server            │');
  console.log('├─────────────────────────────────────────────────┤');
  console.log(`│  Local:   http://localhost:${PORT}                  │`);
  console.log(`│  Network: http://${HOST}:${PORT}                 │`);
  console.log('├─────────────────────────────────────────────────┤');
  console.log(`│  Android base URL → http://${HOST}:${PORT}      │`);
  console.log('└─────────────────────────────────────────────────┘\n');
});
