# Architecture

## Vue d'ensemble

Le projet suit desormais une architecture **frontend React + backend Spring Boot uniquement**.

```text
frontend (React/Vite) <-> backend (Spring Boot API + JWT + WebSocket) <-> PostgreSQL
```

Le backend sert aussi les assets statiques compiles du frontend pour l'execution unifiee sur `localhost:8080`.

## Frontend

- `frontend/src/app` : bootstrap applicatif et routing React Router
- `frontend/src/components` : composants reutilisables UI, layout, charts
- `frontend/src/features` : stores et logique metier cote interface
- `frontend/src/pages` : ecrans metier React
- `frontend/src/api` : client HTTP Axios
- `frontend/src/styles` : styles Tailwind et design system

## Backend

- `backend/src/main/java/com/parcinformatique/app/controller` : controleurs REST et web
- `backend/src/main/java/com/parcinformatique/app/service` : logique metier
- `backend/src/main/java/com/parcinformatique/app/repository` : acces JPA
- `backend/src/main/java/com/parcinformatique/app/entity` : entites Hibernate
- `backend/src/main/java/com/parcinformatique/app/security` : JWT, filtres, user details
- `backend/src/main/resources` : configuration Spring, messages, static React compile

## Base de donnees

- `database/01-init.sql` : index, extensions et ajustements PostgreSQL pour Spring
- `database/02-seed-demo-data.sql` : complements SQL de demonstration
- les migrations metier principales sont pilotees par JPA/Hibernate et les seeders Spring

## Temps reel

- Spring WebSocket diffuse les evenements metier
- le frontend consomme les notifications et les met a jour dans l'interface

## Decision d'architecture

L'ancien backend Node.js / Prisma a ete supprime pour eviter :

- les API dupliquées
- l'authentification concurrente
- les modeles de donnees divergents
- les scripts Docker incoherents
- la confusion entre plusieurs stacks backend
