/**
 * Dev (`ng serve`) : chaîne vide = URLs relatives (`/user`, `/project`, …).
 * Le navigateur appelle donc le même hôte que l’app (4200) et `proxy.conf.json`
 * transmet à la gateway (8775) → **pas de CORS**, plus d’erreur HTTP 0.
 *
 * Si tu dois appeler la gateway sans proxy (ex. tests Postman depuis le navigateur),
 * mets temporairement : `'http://localhost:8775'`.
 */
export const environment = {
  production: false,
  apiGateway: '',
  /**
   * Keycloak — grant « password » (Direct Access Grants).
   * - clientId : doit exister dans le realm (Clients), exactement le même libellé.
   * - clientSecret : laisser vide si le client est PUBLIC. Si le client est CONFIDENTIAL,
   *   copier le secret (onglet Credentials) et le coller ici (dev uniquement).
   */
  keycloak: {
    /** Même origine que ng serve (4200) → évite CORS « Failed to fetch » ; proxy dans proxy.conf.json */
    tokenUrl: '/keycloak-proxy/realms/Job_Board_Realm/protocol/openid-connect/token',
    clientId: 'freelance_client',
    clientSecret: 'AldzasvC35OchXCB7IVOMvnTFNvuQryi',
  },
};
