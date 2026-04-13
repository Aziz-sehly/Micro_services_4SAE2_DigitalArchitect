/** Aligné sur la gateway locale (docker-compose, port hôte 8775) */
export const environment = {
  production: true,
  apiGateway: 'http://localhost:8775',
  keycloak: {
    /** Avec `ng serve` (même proxy). Build statique seul : mettre l’URL Keycloak + Web Origins sur le client, ou proxifier /keycloak-proxy. */
    tokenUrl: '/keycloak-proxy/realms/Job_Board_Realm/protocol/openid-connect/token',
    clientId: 'freelance_client',
    clientSecret: 'AldzasvC35OchXCB7IVOMvnTFNvuQryi',
  },
};
