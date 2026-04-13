# Job Board — Architecture microservices (4SAE2 Digital Architect)

Plateforme type **job board / freelance** : gestion de **projets**, **propositions**, **paiements**, **forum**, **candidatures / préférences freelancer**, **utilisateurs (.NET 9 / PostgreSQL)**, avec **Spring Cloud Gateway**, **Eureka**, **Keycloak (OAuth2 / JWT)** et front **Angular** (`Pi_4Sae-template`).

### Texte pour la description du dépôt Git (champ *Description* sur GitHub / GitLab)

**FR (court)**  
`Microservices Spring Boot — Eureka, API Gateway, JWT Keycloak, MySQL — projets, propositions, paiements, forum, candidatures ; front Angular.`

**EN (court)**  
`Spring Boot + .NET 9 microservices: Eureka, API Gateway, Keycloak JWT, MySQL, PostgreSQL — projects, proposals, payments, forum, applications, users; Angular SPA.`

---

## Description courte (README / aperçu)

> Stack microservices Java Spring Boot, découverte Eureka, routage et sécurité centralisés via API Gateway (JWT Keycloak), bases MySQL par service, forum dédié, service paiement, intégration Angular pour le front office et le back office admin.

---

## Composants

| Composant | Rôle |
|-----------|------|
| **Eureka** | Service registry |
| **API Gateway** | Routage, CORS, OAuth2 Resource Server (JWT) |
| **Microservice_project** | Projets (CRUD, recherche, stats, notifications) |
| **Microservice_proposal** | Propositions freelances |
| **payment-service** | Paiements |
| **forum-service** | Posts / réactions / réponses |
| **Microservice_candidature** | Préférences freelancer (`/candidature`, `/application`) |
| **user-service** (.NET 9) | Utilisateurs — CRUD, rôles, sync Keycloak (PostgreSQL) |
| **milestone** *(optionnel, hors compose par défaut)* | Jalons contrat |
| **Keycloak** | Realm `Job_Board_Realm`, rôles realm `client`, `freelancer`, etc. |
| **Pi_4Sae-template** | SPA Angular (gateway `8775`, proxy Keycloak en dev) |

---

## Démarrage rapide

À la **racine** du dépôt (là où se trouve `docker-compose.yml`) :

```bash
docker compose build
docker compose up -d
```

Démarrer **Keycloak** (realm, clients, utilisateurs) selon ta configuration locale (souvent `http://localhost:8180`).

Front Angular :

```bash
cd Pi_4Sae-template
npm install
npm start
```

---

## Ports (hôte — `docker-compose` actuel)

| Service | URL hôte | Notes |
|---------|----------|--------|
| **API Gateway** | `http://localhost:8775` | Point d’entrée **recommandé** pour Postman |
| **Eureka Dashboard** | `http://localhost:8762` | UI (conteneur `8761` mappé sur `8762`) |
| **Project** (direct) | `http://localhost:18091` | Context path `/freelance` |
| **Proposal** (direct) | `http://localhost:8094` | Context path `/freelance1` |
| **Payment** (direct) | `http://localhost:8099` | Context path `/payment` |
| **Forum** (direct) | `http://localhost:9082` | Pas de context path dédié dans les props listées |
| **Candidature** (direct) | `http://localhost:8097` | Context path `/h2` |
| **Keycloak** | `http://localhost:8180` | Token realm ci-dessous |
| **User** (direct) | `http://localhost:5088` | API REST .NET 9 (`/api/Users/...`) |
| **PostgreSQL (user)** | `localhost:5433` | Base `userdb` |
| **Reviews** (gateway) | via Gateway → `http://localhost:8081` | Service externe référencé par la gateway |
| **Milestone** (gateway) | via Gateway → `http://localhost:8094` | Même port hôte que proposal si les deux tournent sur l’hôte : à adapter si conflit |

---

## Postman — obtenir un JWT (Keycloak)

**POST** `http://localhost:8180/realms/Job_Board_Realm/protocol/openid-connect/token`  

**Headers** : `Content-Type: application/x-www-form-urlencoded`  

**Body (x-www-form-urlencoded)** :

| Clé | Valeur (exemple) |
|-----|-------------------|
| `grant_type` | `password` |
| `client_id` | ton client (ex. `freelance_client`) |
| `client_secret` | si client *confidential* |
| `username` | utilisateur realm |
| `password` | mot de passe |

Réponse : copier `access_token`.

**Requêtes vers la gateway** : onglet **Authorization** → Type **Bearer Token** → coller `access_token`.

> Les rôles Keycloak doivent inclure `client` et/ou `freelancer` (realm roles) pour passer les règles des microservices et de la gateway.

---

## Postman — base URL gateway

```
{{gateway}} = http://localhost:8775
```

Tous les chemins ci-dessous sont préfixés par `{{gateway}}` **sauf** le token Keycloak et Eureka.

---

## Endpoints via API Gateway (`http://localhost:8775`)

### Projet (`Microservice_project` — réécriture vers `/freelance/project/...`)

| Méthode | Chemin gateway | Rôles (indicatif) |
|---------|----------------|-------------------|
| GET | `/project/all` | `client` **ou** `freelancer` |
| GET | `/project/{id}` | `client` **ou** `freelancer` |
| GET | `/project/my-projects` | `client` |
| GET | `/project/search` | Query params optionnels — `client` **ou** `freelancer` |
| GET | `/project/filter` | `client` **ou** `freelancer` |
| GET | `/project/stats` | `client` |
| GET | `/project/stats/freelancer` | `freelancer` |
| POST | `/project/add` | `client` |
| POST | `/project/add/ai` | `client` |
| POST | `/project/ai-suggest` | `client` |
| PUT | `/project/{id}` | `client` (propriétaire) |
| DELETE | `/project/{id}` | `client` (propriétaire) |
| POST | `/project/notify/new-proposal` | `client` **ou** `freelancer` |

### Propositions (`Microservice_proposal` — réécriture vers `/freelance1/proposal/...`)

| Méthode | Chemin gateway | Rôles (indicatif) |
|---------|----------------|-------------------|
| POST | `/proposal/AddProposal` | `freelancer` |
| GET | `/proposal/GetAllProposals` | `client` **ou** `freelancer` |
| GET | `/proposal/GetProposal/{id}` | `client` **ou** `freelancer` |
| PUT | `/proposal/UpdateProposal/{id}` | `freelancer` |
| DELETE | `/proposal/DeleteProposal/{id}` | `freelancer` |
| GET | `/proposal/GetProposalsByProject/{projectId}` | `client` |
| GET | `/proposal/GetProposalsByFreelancer/{freelancerId}` | `freelancer` |
| GET | `/proposal/GetProject/{id}` | `client` **ou** `freelancer` |
| GET | `/proposal/GetFreelancer/{id}` | `client` **ou** `freelancer` |
| GET | `/proposal/GetAllProjects` | `client` **ou** `freelancer` |
| PUT | `/proposal/AcceptProposal/{id}` | `client` |
| PUT | `/proposal/WithdrawProposal/{id}` | `freelancer` |

### Paiement (`payment-service` — réécriture vers `/payment/api/...`)

| Méthode | Chemin gateway | Rôles (gateway) |
|---------|----------------|-----------------|
| POST | `/payment/api/payments` | `client` |
| GET | `/payment/api/payments` | `client` **ou** `freelancer` |
| GET | `/payment/api/payments/{id}` | `client` **ou** `freelancer` |
| DELETE | `/payment/api/payments/{id}` | `client` |

### Forum (`forum-service`)

| Méthode | Chemin gateway | Accès |
|---------|----------------|--------|
| GET | `/api/posts` | **Public** (GET liste) |
| GET | `/api/posts/{id}` | **Public** |
| POST | `/api/posts` | JWT requis (`authenticated`) |
| PUT | `/api/posts/{id}` | JWT |
| DELETE | `/api/posts/{id}` | JWT |
| POST | `/api/posts/{id}/replies` | JWT |
| GET | `/api/posts/{id}/replies` | JWT |
| DELETE | `/api/posts/replies/{replyId}` | JWT |
| POST | `/api/posts/{id}/react` | JWT |

### Candidature / application (`Microservice_candidature` — réécriture `/h2/candidature/...` ou `/h2/application/...`)

Préfixes gateway équivalents : **`/candidature/...`** ou **`/application/...`**.

| Méthode | Chemin gateway (exemple) | Rôles |
|---------|--------------------------|--------|
| GET | `/candidature/me` | `freelancer` |
| POST | `/candidature/me` | `freelancer` |
| PUT | `/candidature/me` | `freelancer` |
| DELETE | `/candidature/me` | `freelancer` |
| GET | `/candidature/all` | `freelancer` |
| GET | `/candidature/freelancer/{subject}` | `admin` |

Même jeu de chemins avec **`/application/`** à la place de **`/candidature/`**.

### Utilisateurs (`user-service` .NET 9 — réécriture vers `/api/Users/...`)

| Méthode | Chemin gateway | Accès |
|---------|----------------|--------|
| POST | `/user` | **Public** (inscription) |
| GET | `/user/me` | JWT (authentifié) |
| GET | `/user/{id}` | JWT |
| GET | `/user/email/{email}` | JWT |
| GET | `/user/by-role/{role}` | JWT |
| PUT | `/user/{id}` | JWT |
| DELETE | `/user/{id}` | `ADMIN` |

### Milestone (routage gateway vers hôte `8094` — conflit possible avec proposal)

Context applicatif milestone : `/milestone` + contrôleur `/api`.

| Méthode | Chemin gateway | Notes |
|---------|----------------|--------|
| POST | `/milestone/api` | Création |
| GET | `/milestone/api/{id}` | Détail |
| GET | `/milestone/api?contractId={id}` | Liste par contrat |
| PUT | `/milestone/api/{id}` | Mise à jour |
| DELETE | `/milestone/api/{id}` | Suppression |
| PATCH | `/milestone/api/{id}/approve` | Approbation |
| PATCH | `/milestone/api/{id}/submit` | Soumission |
| POST | `/milestone/api/{id}/mark-paid` | Marquer payé |

> Vérifier que le **service milestone** tourne bien sur le port attendu par la gateway ; sinon ajuster `ApiGatewayApplication` ou `docker-compose`.

### Reviews (routage gateway → service externe)

| Méthode | Chemin gateway | Rôles (gateway) |
|---------|----------------|-----------------|
| * | `/api/reviews/**` | `client` **ou** `freelancer` |

Détails des chemins : suivre l’API du service sur **`http://localhost:8081`** si exposé localement.

---

## Accès direct aux microservices (sans gateway)

Utile pour debug ; la sécurité JWT peut rester active selon `application.properties` de chaque service.

### Project — `http://localhost:18091/freelance`

Même schéma de chemins que ci-dessus mais préfixe : **`/freelance/project/...`** (ex. `GET /freelance/project/all`).

### Proposal — `http://localhost:8094/freelance1`

Préfixe : **`/freelance1/proposal/...`**.

### Payment — `http://localhost:8099/payment`

Préfixe : **`/payment/api/...`**.

### Forum — `http://localhost:9082`

Préfixe : **`/api/posts/...`**.

### Candidature — `http://localhost:8097/h2`

Préfixe : **`/h2/candidature/...`** ou **`/h2/application/...`**.

### User — `http://localhost:5088`

Préfixe : **`/api/Users/...`** (ex. `GET /api/Users/me`).

### Milestone — `http://localhost:8094/milestone` *(si instance dédiée)*

Préfixe : **`/milestone/api/...`**.

---

## Eureka

- Dashboard : `http://localhost:8762`
- Les instances enregistrées dépendent des services effectivement démarrés.

---

## Front Angular

- Dossier : **`Pi_4Sae-template`**
- Gateway configurée (ex. `http://localhost:8775`)
- Login Keycloak + proxy dev : voir `Pi_4Sae-template/proxy.conf.json` et `src/environments/environment.ts`

---

## Licence / auteurs

À compléter selon ton encadrement pédagogique (ESPRIT / 4SAE2, etc.).
