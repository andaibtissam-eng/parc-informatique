# Docker

Les configurations Docker officielles du projet sont centralisées ici.

## Services

- `postgres` : base de données PostgreSQL 16
- `backend` : application Spring Boot qui sert aussi le frontend React compilé

## Lancement

```bash
docker compose -f docker/docker-compose.yml up --build
```
