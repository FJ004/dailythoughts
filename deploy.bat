@echo off
echo ========================================
echo   DAILY THOUGHTS APP DEPLOYMENT SCRIPT
echo ========================================
echo.

echo Step 1: Cleaning previous build...
call mvn clean

echo.
echo Step 2: Building with Java 25 compatibility...
call mvn package -DskipTests

echo.
echo Step 3: Checking build result...
if exist "target\dailythoughts-*.jar" (
    echo ✅ Build successful!
    echo Jar file created: target\dailythoughts-*.jar
) else (
    echo ❌ Build failed!
    pause
    exit /b 1
)

echo.
echo ========================================
echo   DEPLOYMENT INSTRUCTIONS
echo ========================================
echo.
echo OPTION 1: RENDER.COM (Free, Recommended)
echo -----------------------------------------
echo 1. Go to: https://render.com
echo 2. Click "New +" → "Web Service"
echo 3. Connect your GitHub repository OR
echo 4. Upload: target\dailythoughts-*.jar
echo 5. Settings:
echo    - Name: daily-thoughts
echo    - Environment: Docker
echo    - Build Command: mvn clean package
echo    - Start Command: java -jar target/dailythoughts-*.jar
echo    - Plan: Free
echo.
echo OPTION 2: RAILWAY.APP (Free)
echo ---------------------------------
echo 1. Go to: https://railway.app
echo 2. New Project → Deploy from GitHub
echo 3. Select your repo
echo 4. It auto-detects Spring Boot
echo.
echo OPTION 3: HEROKU (Free with credit card)
echo -----------------------------------------
echo 1. Install Heroku CLI
echo 2. heroku create daily-thoughts
echo 3. git push heroku main
echo.
echo OPTION 4: RUN LOCALLY FOREVER
echo ------------------------------
echo Run this command in PowerShell (as Admin):
echo   java -jar target\dailythoughts-*.jar
echo.
echo ========================================
echo   ENVIRONMENT VARIABLES NEEDED:
echo ========================================
echo.
echo For cloud deployment, set these variables:
echo 1. JAVA_OPTS="-Dspring.classformat.ignore=true"
echo 2. SPRING_PROFILES_ACTIVE=prod (optional)
echo.
echo ========================================
echo   FIREBASE SETUP (IMPORTANT!)
echo ========================================
echo.
echo 1. Keep your firebase-service-account.json
echo 2. It's already in src/main/resources/
echo 3. Firebase works in cloud automatically
echo.
pause

REM Optional: Open deployment sites
echo.
set /p choice="Open Render.com now? (y/n): "
if /i "%choice%"=="y" start https://render.com

set /p choice="Open Firebase Console? (y/n): "
if /i "%choice%"=="y" start https://console.firebase.google.com/

echo.
echo Deployment script complete!
echo Your app is ready for the cloud! 🚀
pause