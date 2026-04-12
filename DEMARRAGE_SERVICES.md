# Ordre de démarrage des microservices

Pour que Milestones et Payment fonctionnent, démarrez les services **dans cet ordre** :

## 1. Eureka (port 8761)
```bash
cd EurikaRegister
mvn spring-boot:run
```
Attendre que Eureka soit prêt (log: "Started EurekaServer").

## 2. ApiGateway (port 8765)
```bash
cd ApiGateway
mvn spring-boot:run
```

## 3. Milestone (port 8094) — **OBLIGATOIRE pour la page Milestones**
```bash
cd milestone/milestone
mvn spring-boot:run
```
Attendre le log: "Started MilestoneApplication".

## 4. Payment (port 8082) — pour les paiements
```bash
cd payment/payment
mvn spring-boot:run
```

## 5. Frontend Angular
```bash
cd Pi_4Sae-template
ng serve
```

---

## Vérification rapide

- **Milestone direct** : http://localhost:8094/milestone/api?contractId=1  
  → Doit retourner `[]` (liste vide) et non 404.

- **Via Gateway** : http://localhost:8765/milestone/api?contractId=1  
  → Même résultat si la gateway route correctement.

Si vous voyez 404, le microservice Milestone n'est pas démarré.
