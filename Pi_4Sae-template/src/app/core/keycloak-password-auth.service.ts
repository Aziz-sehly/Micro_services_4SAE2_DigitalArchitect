import { Injectable, inject } from '@angular/core';
import { environment } from '../../environments/environment';
import { AuthTokenService } from './auth-token.service';

/**
 * Obtient un access_token Keycloak (grant password).
 * Client public : Direct Access Grants + pas de secret.
 * Client confidential : renseigner environment.keycloak.clientSecret.
 */
@Injectable({ providedIn: 'root' })
export class KeycloakPasswordAuthService {
  private readonly tokens = inject(AuthTokenService);

  async login(username: string, password: string, rememberMe: boolean): Promise<void> {
    const kc = environment.keycloak;
    if (!kc?.tokenUrl || !kc?.clientId) {
      throw new Error('Keycloak non configuré dans environment.ts');
    }
    const body = new URLSearchParams({
      grant_type: 'password',
      client_id: kc.clientId,
      username: username.trim(),
      password,
    });
    const secret = typeof kc.clientSecret === 'string' ? kc.clientSecret.trim() : '';
    if (secret) {
      body.set('client_secret', secret);
    }
    let res: Response;
    try {
      res = await fetch(kc.tokenUrl, {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: body.toString(),
      });
    } catch (e) {
      const hint =
        kc.tokenUrl.startsWith('/')
          ? ' Vérifie que `ng serve` tourne avec `proxy.conf.json` et que Keycloak est sur le port 8180.'
          : '';
      throw new Error(
        `Impossible de joindre Keycloak (réseau / CORS / serveur arrêté).${hint} Détail : ${e instanceof Error ? e.message : String(e)}`
      );
    }
    const text = await res.text();
    if (!res.ok) {
      throw new Error(this.formatKeycloakError(text, res.status));
    }
    const data = JSON.parse(text) as { access_token: string; refresh_token?: string };
    if (!data.access_token) {
      throw new Error('Réponse token invalide');
    }
    this.tokens.setTokens(data.access_token, data.refresh_token, rememberMe);
  }

  private formatKeycloakError(body: string, status: number): string {
    try {
      const j = JSON.parse(body) as { error?: string; error_description?: string };
      const code = j.error ?? '';
      const desc = (j.error_description ?? '').replace(/\+/g, ' ');
      if (code === 'invalid_grant') {
        return (
          'Identifiant ou mot de passe incorrect, ou « Direct Access Grants » désactivé pour ce client Keycloak. ' +
          (desc ? `(${desc})` : '')
        );
      }
      if (code === 'invalid_client') {
        return `Client Keycloak invalide : vérifie clientId / clientSecret dans environment.ts. (${desc || body})`;
      }
      if (code === 'unauthorized_client') {
        return `Ce client n’est pas autorisé pour le grant « password ». Active « Direct Access Grants » sur le client Keycloak. (${desc || body})`;
      }
      if (desc) return `${code}: ${desc}`;
    } catch {
      /* pas du JSON */
    }
    return body || `HTTP ${status}`;
  }
}
