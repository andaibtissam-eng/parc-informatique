# ParcFlow

Plateforme web intelligente de gestion du parc informatique et des affectations materielles.

L'architecture officielle du projet est desormais **unique et coherente** :

- `frontend/` : React + Vite + TailwindCSS
- `backend/` : Spring Boot + Spring Security + JWT + WebSocket + Swagger
- `database/` : scripts PostgreSQL
- `docker/` : configuration Docker officielle
- `docs/` : documentation technique et rapport

L'ancien backend Node.js / Express / Prisma a ete retire du projet pour supprimer les doublons de logique, les routes conflictuelles et les modeles de donnees incoherents.

## Stack finale

### Frontend

- React.js
- Vite
- TailwindCSS
- React Router
- Zustand
- Axios

### Backend

- Java 17
- Spring Boot 3
- Spring Security
- JWT Authentication
- Spring Data JPA / Hibernate
- PostgreSQL
- WebSocket
- Swagger OpenAPI

## Structure

```text
frontend/
backend/
database/
docker/
docs/
logs/
tools/
```

## Lancement local

### 1. Frontend React vers Spring Boot

Compiler le frontend dans les assets statiques du backend :

```powershell
cd C:\Users\andai\Desktop\parc_informatique\frontend
"C:\Program Files\nodejs\npm.cmd" run build
```

### 2. Lancer le backend Spring Boot

Avec le wrapper Maven du backend :

```powershell
cd C:\Users\andai\Desktop\parc_informatique\backend
.\mvnw.cmd spring-boot:run
```

L'application est alors disponible sur [http://localhost:8080](http://localhost:8080).

### 3. Developpement frontend separe

Pour travailler en mode Vite avec proxy API :

```powershell
cd C:\Users\andai\Desktop\parc_informatique\frontend
"C:\Program Files\nodejs\npm.cmd" run dev
```

Le proxy Vite redirige `/api`, `/uploads` et `/ws` vers `http://localhost:8080`.

## Lancement Docker

```bash
docker compose -f docker/docker-compose.yml up --build
```

## Comptes de demonstration

- `admin@emsi.ma` / `Admin123*`
- `gestionnaire@parc.local` / `Manager@12345`
- `technicien@parc.local` / `Tech@12345`
- `beneficiaire@parc.local` / `Benef@12345`

## Endpoints utiles

- UI : `/login`
- Swagger : `/swagger-ui.html`
- OpenAPI : `/api/docs`
- Dashboard API : `/api/dashboard/summary`

## Notes importantes

- Le frontend React premium est la seule UI officielle du projet.
- Le backend Spring Boot est la seule API officielle du projet.
- Les scripts SQL racine correspondent desormais au backend Spring uniquement.
- Les anciens fichiers Node/Prisma ont ete retires pour eviter toute ambiguite.
