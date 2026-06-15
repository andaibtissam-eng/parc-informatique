-- Les données de démonstration principales sont générées automatiquement par DataSeeder au démarrage.
-- Ce script fournit des exemples SQL complémentaires pour les environnements PostgreSQL purs.

INSERT INTO departments (id, created_at, updated_at, name, code, description)
VALUES (gen_random_uuid(), now(), now(), 'Direction des Systèmes d''Information', 'DSI', 'Direction centrale IT')
ON CONFLICT (code) DO NOTHING;

INSERT INTO categories (id, created_at, updated_at, name, code, description)
VALUES (gen_random_uuid(), now(), now(), 'Ordinateurs portables', 'LAPTOP', 'Parc mobile')
ON CONFLICT (code) DO NOTHING;
