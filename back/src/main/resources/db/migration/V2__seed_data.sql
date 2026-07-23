-- V2__seed_data.sql
-- Minimal sample data for local development/testing

-- Password for both seeded users is: Admin123!
INSERT INTO users (first_name, last_name, email, password_hash, role, active) VALUES
('Ana', 'Petrovic', 'ana.petrovic@example.com', '$2a$10$hVIb2xWWA1poIlzqgKDod.S0Au0HSQqb.OPgAfPbQcUjtwVR/txia', 'admin', TRUE),
('Marko', 'Jovanovic', 'marko.jovanovic@example.com', '$2a$10$hVIb2xWWA1poIlzqgKDod.S0Au0HSQqb.OPgAfPbQcUjtwVR/txia', 'member', TRUE);

INSERT INTO projects (name, description, active) VALUES
('Website Redesign', 'Revamp the marketing site with a new design system.', TRUE),
('Internal Tools', 'Build internal dashboards for the ops team.', TRUE);

INSERT INTO project_members (project_id, user_id) VALUES
(1, 1),
(1, 2),
(2, 1);

INSERT INTO tasks (project_id, title, description, status, priority, assigned_user_id, created_by_user_id, due_date) VALUES
(1, 'Design new homepage', 'Create Figma mockups for the new homepage layout.', 'in_progress', 'high', 2, 1, '2026-08-01'),
(1, 'Set up CI pipeline', 'Add GitHub Actions workflow for lint and tests.', 'todo', 'medium', 1, 1, '2026-07-25');

INSERT INTO task_comments (task_id, user_id, comment) VALUES
(1, 1, 'Looks great so far, can we try a darker header color?'),
(1, 2, 'Sure, I will update it and push a new version tomorrow.');

INSERT INTO task_history (task_id, user_id, field_name, old_value, new_value) VALUES
(1, 2, 'status', 'todo', 'in_progress'),
(2, 1, 'priority', 'low', 'medium');
