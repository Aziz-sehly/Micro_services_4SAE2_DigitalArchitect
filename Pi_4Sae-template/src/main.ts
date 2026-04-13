import { bootstrapApplication } from '@angular/platform-browser';
import { appConfig } from './app/app.config';
import { AppComponent } from './app/app.component';

bootstrapApplication(AppComponent, appConfig).catch((err: unknown) => {
    console.error(err);
    const msg = err instanceof Error ? err.stack ?? err.message : String(err);
    document.body.innerHTML =
        '<div style="padding:24px;font-family:system-ui,sans-serif;max-width:720px">' +
        '<h1 style="margin:0 0 12px">Erreur au démarrage de l’application</h1>' +
        '<p style="color:#444">Ouvre aussi la console du navigateur (F12) pour le détail.</p>' +
        '<pre style="overflow:auto;background:#f5f5f5;padding:12px;border-radius:8px;font-size:12px">' +
        msg.replace(/</g, '&lt;') +
        '</pre></div>';
});