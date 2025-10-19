@echo off
REM Script para iniciar el backend dockerizado en Windows (configuración para Render)

echo 🐳 Iniciando backend dockerizado (configuración Render)...

REM Verificar que existe el archivo .env en el directorio backArquitectura
if not exist backArquitectura\.env (
    echo ⚠️  Archivo .env no encontrado en backArquitectura/
    if exist backArquitectura\.env.example (
        echo 📝 Copiando desde .env.example...
        copy backArquitectura\.env.example backArquitectura\.env
    ) else (
        echo ❌ No se encontró .env.example. Creando archivo básico...
        echo FRONT_URI=http://localhost:5173 > backArquitectura\.env
        echo PORT=8080 >> backArquitectura\.env
        echo DATABASE_URL=postgresql://testuser:testpass@localhost:5432/testdb >> backArquitectura\.env
    )
    echo 📝 Por favor, edita backArquitectura\.env con tus credenciales reales antes de continuar.
    echo Especialmente para producción en Render:
    echo   - DATABASE_URL (URL completa de PostgreSQL de Render)
    echo   - MERCADOPAGO_ACCESS_TOKEN (solo backend)
    echo   - FRONTEND_URL (URL de tu frontend en Render)
    echo   - Nota: PUBLIC_KEY va en el frontend, no en el backend
    pause
)

echo.
echo 🔧 Opciones de inicio:
echo [1] Solo backend (para usar con BD externa como Render)
echo [2] Backend + PostgreSQL local (para desarrollo)
echo.
set /p choice="Elige una opción (1 o 2): "

if "%choice%"=="1" (
    echo 🔨 Construyendo y levantando solo el backend...
    docker-compose up --build backend
) else if "%choice%"=="2" (
    echo 🔨 Construyendo y levantando backend + PostgreSQL local...
    docker-compose --profile dev up --build
) else (
    echo ❌ Opción inválida. Iniciando solo el backend por defecto...
    docker-compose up --build backend
)

echo.
echo 📝 Información útil:
echo   - Backend: http://localhost:8080
echo   - Para ver logs: docker-compose logs -f backend
echo   - Para detener: docker-compose down
echo.
pause