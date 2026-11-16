# Backend Upgrade & Schema Extension Guide
**For Teammate Working on `keyboard-learning-system` Backend**

This document outlines the database schema additions and Express/TypeScript API routes needed to support the newly added features in the Private Closet Android frontend:
1. **Sound & Beat Marketplace** (Buy & sell melodies, voices, beats, chord MIDI packs with multiple payment options)
2. **Community Chat System** (Channels, student-tutor live discussions, pinned guidelines)
3. **Tutor / Instructor Studio & Master Account Verification** (Restricted tutor registration via master invitation codes, course & notes uploading)
4. **Multiple Payment Gateways** (Stripe/Card, PayPal, Google Pay, M-Pesa / Mobile Money, Crypto)

---

## 1. Database Schema Migrations (MySQL)

Run the following SQL migrations against your `keyboard_learning` database:

```sql
-- ==========================================================
-- 1. MARKETPLACE ITEMS & PURCHASES
-- ==========================================================
CREATE TABLE IF NOT EXISTS marketplace_items (
    id VARCHAR(36) PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    category ENUM('MELODIES', 'VOICES', 'BEATS', 'CHORD_PACKS', 'SAMPLE_PACKS') NOT NULL DEFAULT 'MELODIES',
    price_usd DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    seller_id VARCHAR(36) NOT NULL,
    bpm INT NULL,
    key_note VARCHAR(16) NULL,
    format_tags JSON NULL, -- e.g. ["WAV", "MIDI", "Serum"]
    file_download_url VARCHAR(512) NULL,
    audio_preview_url VARCHAR(512) NULL,
    sales_count INT DEFAULT 0,
    rating DECIMAL(3, 2) DEFAULT 5.00,
    status ENUM('DRAFT', 'PUBLISHED', 'ARCHIVED') DEFAULT 'PUBLISHED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (seller_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS marketplace_purchases (
    id VARCHAR(36) PRIMARY KEY,
    item_id VARCHAR(36) NOT NULL,
    buyer_id VARCHAR(36) NOT NULL,
    amount_usd DECIMAL(10, 2) NOT NULL,
    payment_method ENUM('CARD', 'GOOGLE_PAY', 'PAYPAL', 'MPESA', 'CRYPTO') NOT NULL,
    gateway_transaction_id VARCHAR(255) NOT NULL,
    status ENUM('PENDING', 'COMPLETED', 'FAILED', 'REFUNDED') DEFAULT 'COMPLETED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (item_id) REFERENCES marketplace_items(id) ON DELETE RESTRICT,
    FOREIGN KEY (buyer_id) REFERENCES users(id) ON DELETE CASCADE
);

-- ==========================================================
-- 2. COMMUNITY CHANNELS & REAL-TIME MESSAGES
-- ==========================================================
CREATE TABLE IF NOT EXISTS community_channels (
    id VARCHAR(64) PRIMARY KEY,
    name VARCHAR(64) NOT NULL UNIQUE,
    topic VARCHAR(255) NOT NULL,
    icon VARCHAR(16) DEFAULT '💬',
    is_private BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Seed standard starter channels
INSERT IGNORE INTO community_channels (id, name, topic, icon) VALUES
('c_general', 'general-lounge', 'General discussions, music talk & introductions', '🎹'),
('c_beginners', 'beginner-help', 'Ask questions about hand posture, scales and basic chords', '🌱'),
('c_chords', 'chords-and-progressions', 'Share 2-5-1 voicings, Gospel passing chords & reharmonizations', '🎼'),
('c_sound', 'synth-and-sound-design', 'Preset creation, patch sharing, and keyboard audio beats', '🎛️'),
('c_tutors', 'ask-the-tutors', 'Direct answers and feedback from verified instructors', '🎓');

CREATE TABLE IF NOT EXISTS community_messages (
    id VARCHAR(36) PRIMARY KEY,
    channel_id VARCHAR(64) NOT NULL,
    sender_id VARCHAR(36) NOT NULL,
    message_text TEXT NOT NULL,
    is_pinned BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (channel_id) REFERENCES community_channels(id) ON DELETE CASCADE,
    FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE
);

-- ==========================================================
-- 3. MASTER TUTOR INVITATION CODES
-- ==========================================================
CREATE TABLE IF NOT EXISTS tutor_invitations (
    id VARCHAR(36) PRIMARY KEY,
    invite_code VARCHAR(64) NOT NULL UNIQUE,
    issued_by_admin_id VARCHAR(36) NOT NULL,
    assigned_email VARCHAR(255) NULL,
    max_uses INT DEFAULT 1,
    times_used INT DEFAULT 0,
    expires_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (issued_by_admin_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Seed default master invite codes
INSERT IGNORE INTO tutor_invitations (id, invite_code, issued_by_admin_id, max_uses) VALUES
('inv_master_1', 'MASTER-TUTOR-2026', 'admin_1', 999),
('inv_master_2', 'KEYBOARD-MASTER', 'admin_1', 999);
```

---

## 2. Recommended Express/TypeScript Routes

Add these controller routes to your Express backend:

### A. Marketplace Endpoints
- `GET /api/marketplace`
  - Query params: `category` (`ALL`, `MELODIES`, `VOICES`, `BEATS`, `CHORD_PACKS`, `SAMPLE_PACKS`), `search`
  - Returns: Array of `MarketplaceItem` objects.
- `POST /api/marketplace` (Auth required, tutor or student creator)
  - Body: `{ title, description, category, price_usd, bpm, key_note, format_tags }`
  - Creates a new listing in `marketplace_items`.
- `POST /api/marketplace/:id/purchase` (Auth required)
  - Body: `{ payment_method, token_or_nonce }`
  - Interacts with payment gateway (Stripe, PayPal, M-Pesa Daraja API) and stores purchase record in `marketplace_purchases`.

### B. Community Chat Endpoints
- `GET /api/community/channels`
  - Returns list of channels with `unread_count`.
- `GET /api/community/channels/:id/messages`
  - Query params: `limit` (default 50), `before`
  - Returns recent messages with joined `sender_name`, `sender_role` (`STUDENT`, `INSTRUCTOR`, `ADMIN`), and timestamp.
- `POST /api/community/channels/:id/messages` (Auth required)
  - Body: `{ message_text }`
  - Saves message and broadcasts via WebSocket/Socket.IO if enabled.

### C. Tutor Authorization Endpoints
- `POST /api/tutors/verify-invite`
  - Body: `{ invite_code }`
  - Validates code in `tutor_invitations` table.
- `POST /api/tutors/courses` (Auth required, role = INSTRUCTOR or ADMIN)
  - Body: `{ title, description, level, price_usd, video_url, notes_filename, materials_note }`
  - Creates course and first lesson with materials attachments.

### D. Multiple Payment Webhooks
- `POST /api/payments/stripe-webhook`
- `POST /api/payments/paypal-ipn`
- `POST /api/payments/mpesa-callback`

---

## 3. How the Frontend Currently Handles These Routes
- The Android frontend includes full model representations (`MarketplaceItem`, `CommunityMessage`, `TutorStats`, `PaymentMethod`).
- If your backend does not yet have these routes deployed, the frontend **seamlessly falls back to local cached repository data** with realistic initial content. Once you add these routes to Express, the app connects automatically!
