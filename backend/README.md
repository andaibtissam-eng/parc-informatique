# Backend Spring Boot

Ce dossier contient le **seul backend officiel** du projet ParcFlow.

## Technologies

- Spring Boot 3
- Spring Security
- JWT Authentication
- Spring Data JPA / Hibernate
- PostgreSQL
- WebSocket
- Swagger OpenAPI

## Rôle du backend

- exposer les API REST metier
- sécuriser l’authentification et le RBAC
- servir le frontend React compilé depuis `src/main/resources/static`
- gérer les uploads, notifications, affectations et maintenances

## Lancement

```powershell
cd C:\Users\andai\Desktop\parc_informatique\backend
.\mvnw.cmd spring-boot:run
```

## Build

```powershell
cd C:\Users\andai\Desktop\parc_informatique\backend
.\mvnw.cmd test
```

## Configuration

Exemple d'environnement : [backend/.env.example](C:/Users/andai/Desktop/parc_informatique/backend/.env.example)

## URL utiles

- application : [http://localhost:8080](http://localhost:8080)
- login : [http://localhost:8080/login](http://localhost:8080/login)
- swagger : [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
