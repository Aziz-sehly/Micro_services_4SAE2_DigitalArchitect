@echo off
setlocal EnableExtensions
REM Docker Hub user : azizsehli
REM Lancer depuis la RACINE du projet (dossier qui contient docker-compose.yml)

cd /d "%~dp0"

set "HUB_USER=azizsehli"

echo ============================================
echo 1/5  Pull images deja sur Docker Hub (%HUB_USER% + MySQL^)
echo ============================================
docker pull mysql:5.7
docker pull mysql:8.0
docker pull %HUB_USER%/eurikaregister:latest
docker pull %HUB_USER%/project:latest
docker pull %HUB_USER%/proposal:latest
echo (Les erreurs "not found" sur payment/forum/candidature sont normales si pas encore pousses.)

echo.
echo ============================================
echo 2/5  Build local : apigateway, payment-service, forum-service, candidature
echo ============================================
docker compose build
if errorlevel 1 (
  echo ERREUR: docker compose build
  exit /b 1
)

echo.
echo ============================================
echo 3/5  Connexion Docker Hub — utilisateur : %HUB_USER%
echo ============================================
docker login -u %HUB_USER%
if errorlevel 1 (
  echo ERREUR: docker login
  exit /b 1
)

echo.
echo ============================================
echo 4/5  Push vers docker.io/%HUB_USER%/...
echo ============================================
echo Depots a creer une fois sur https://hub.docker.com si besoin :
echo   %HUB_USER%/apigateway   %HUB_USER%/payment-service   %HUB_USER%/forum-service   %HUB_USER%/candidature
echo.

docker push %HUB_USER%/apigateway:latest
if errorlevel 1 exit /b 1
docker push %HUB_USER%/payment-service:latest
if errorlevel 1 exit /b 1
docker push %HUB_USER%/forum-service:latest
if errorlevel 1 exit /b 1
docker push %HUB_USER%/candidature:latest
if errorlevel 1 exit /b 1

echo.
echo ============================================
echo 5/5  Demarrage de tous les conteneurs
echo ============================================
docker compose up -d
if errorlevel 1 exit /b 1

echo.
echo Termine. Controle : docker compose ps
echo Eureka  : http://localhost:8762
echo Gateway : http://localhost:8775
endlocal
exit /b 0
