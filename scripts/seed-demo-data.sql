-- Demo seed data for pks-java e2e. Idempotent: safe to run repeatedly
-- (ON CONFLICT DO NOTHING on natural keys).
-- Run:  docker exec -i postgres psql -U user -d db < scripts/seed-demo-data.sql
-- BCrypt hashes generated with: htpasswd -bnBC 10 "" '<password>'
-- (Spring's BCryptPasswordEncoder accepts the $2y$ variant.)

-- ============ Users (5: 1 ADMIN + 2 MODER + 2 USER) ============
-- admin@example.com / admin123 (ADMIN)  — also seeded at bootstrap
-- moder@example.com / moder123 (MODER)
-- moder2@example.com / moder234 (MODER)
-- user@example.com  / user123  (USER)
-- user2@example.com / user234  (USER)
INSERT INTO users (uuid, email, password, role, enabled, first_name, last_name, created_at, updated_at) VALUES
  ('11111111-1111-1111-1111-111111111111', 'admin@example.com',  '$2y$10$ZGuXkxQHS4Ba93wUHNJlRuM1MAyEQioDh./lwGvgh2ogrZCfs3B1m', 'ADMIN', true, 'Admin', 'Adminov',   '2024-01-10 09:00:00', '2024-01-10 09:00:00'),
  ('22222222-2222-2222-2222-222222222222', 'moder@example.com',  '$2y$10$mUZB/qZ6BsJvc6DeXsoJxek9Wzi6s8KrN.SmMnfpBaJizoHVKToKy', 'MODER', true, 'Modest', 'Moderov',   '2024-02-15 10:30:00', '2024-02-15 10:30:00'),
  ('33333333-3333-3333-3333-333333333333', 'moder2@example.com', '$2y$10$wdeI4iqwyE3uuGD80/TU4.ijFf8bWwOE3Mfry8vm2VNJYuXO3pc/C', 'MODER', true, 'Mira',   'Moderova',  '2024-03-20 14:00:00', '2024-03-20 14:00:00'),
  ('44444444-4444-4444-4444-444444444444', 'user@example.com',   '$2y$10$g/EmHZ5gxBNwiBLlQ1fhpOGNLTW48yPJ1Y5iUgRwXNMyR9XHAWpZa', 'USER',  true, 'Uri',    'Userov',    '2024-04-25 08:15:00', '2024-04-25 08:15:00'),
  ('55555555-5555-5555-5555-555555555555', 'user2@example.com',  '$2y$10$hyWvKv5kCLbGooC3rYLg0ugjiJ9xeZ9mg/R8/e6EjN3fh36rsstB6', 'USER',  true, 'Ulyana', 'Userova',   '2024-05-30 16:45:00', '2024-05-30 16:45:00')
ON CONFLICT (email) DO NOTHING;

-- ============ Resources (12, varied titles + spread-out timestamps) ============
INSERT INTO resources (uuid, title, description, created_at, updated_at) VALUES
  ('a0000001-0000-0000-0000-000000000001', 'Отчёт за 2023 год',        'Годовой финансовый отчёт',        '2024-01-05 09:00:00', '2024-06-01 12:00:00'),
  ('a0000002-0000-0000-0000-000000000002', 'Отчёт за 2024 год',        'Годовой финансовый отчёт',        '2024-02-10 10:00:00', '2024-06-02 12:00:00'),
  ('a0000003-0000-0000-0000-000000000003', 'Презентация продукта',     'Слайды для демо клиентам',        '2024-03-15 11:00:00', '2024-06-03 12:00:00'),
  ('a0000004-0000-0000-0000-000000000004', 'Annual report draft',      'English draft of yearly report',  '2024-04-20 12:00:00', '2024-06-04 12:00:00'),
  ('a0000005-0000-0000-0000-000000000005', 'Руководство пользователя', 'Инструкция для консольного клиента','2024-05-25 13:00:00', '2024-06-05 12:00:00'),
  ('a0000006-0000-0000-0000-000000000006', 'Маркетинговый план',       'План кампании на Q3',             '2024-06-30 14:00:00', '2024-07-06 12:00:00'),
  ('a0000007-0000-0000-0000-000000000007', 'Техническая документация', 'API reference и схемы БД',        '2024-07-05 15:00:00', '2024-07-07 12:00:00'),
  ('a0000008-0000-0000-0000-000000000008', 'Архив сканов',             'Отсканированные договоры 2022',   '2024-08-10 16:00:00', '2024-08-08 12:00:00'),
  ('a0000009-0000-0000-0000-000000000009', 'Дизайн-макеты',            'Макеты лендинга, Figma export',   '2024-09-15 17:00:00', '2024-09-09 12:00:00'),
  ('a0000010-0000-0000-0000-000000000010', 'Звуковые дорожки',         'Аудио для обучающих роликов',     '2024-10-20 18:00:00', '2024-10-10 12:00:00'),
  ('a0000011-0000-0000-0000-000000000011', 'Видеоархив',               'Записи вебинаров 2024',           '2024-11-25 19:00:00', '2024-11-11 12:00:00'),
  ('a0000012-0000-0000-0000-000000000012', 'Прочее',                   NULL,                              '2024-12-30 20:00:00', '2024-12-12 12:00:00')
ON CONFLICT (uuid) DO NOTHING;
