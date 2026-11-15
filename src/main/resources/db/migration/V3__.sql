-- Dev seed: one moderator + one basic user (additive, do NOT edit V1__).
-- DEV-ONLY data. Plaintext passwords are NOT stored here — see team vault.
-- Hashes are BCrypt (compatible with Spring BCryptPasswordEncoder).
-- Admin row is re-asserted here (V1 seeds it, but ON CONFLICT keeps this
-- idempotent on DBs where it was deleted).
INSERT INTO users (
    uuid,
    email,
    password,
    role,
    enabled,
    first_name,
    last_name,
    created_at,
    updated_at
) VALUES (
    gen_random_uuid(),
    'admin@example.com',
    '$2a$10$GoZp8NSEz8WTVlfElueuLO9USrkf5Gq3a5Qz61rJEVXDXOeQ0NbuG',
    'ADMIN',
    true,
    'Admin',
    'User',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
), (
    gen_random_uuid(),
    'moder@example.com',
    '$2b$10$stP0WWKSVgKtDK/cfI489.ljsCSdvJkYzV4uWPS7I.HUtnDQRPUq6',
    'MODER',
    true,
    'Moderator',
    'User',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
), (
    gen_random_uuid(),
    'user@example.com',
    '$2b$10$qa9O7i8oi7AuhurEmX0.jOMMghBLwORQS.U0BVyCQ5IzccO2Qp.Wm',
    'USER',
    true,
    'Basic',
    'User',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
ON CONFLICT (email) DO NOTHING;
