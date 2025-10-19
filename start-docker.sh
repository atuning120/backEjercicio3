#!/bin/bash

# Script para iniciar el backend dockerizado

echo "🐳 Iniciando backend dockerizado..."

# Verificar que existe el archivo .env
if [ ! -f .env ]; then
    echo "⚠️  Archivo .env no encontrado. Copiando desde .env.example..."
    cp .env.example .env
    echo "📝 Por favor, edita el archivo .env con tus credenciales reales antes de continuar."
    echo "Especialmente las credenciales de MercadoPago:"
    echo "  - MERCADOPAGO_ACCESS_TOKEN"
    echo "  - MERCADOPAGO_PUBLIC_KEY"
    read -p "Presiona Enter cuando hayas configurado el archivo .env..."
fi

# Construir y levantar los servicios
echo "🔨 Construyendo y levantando servicios..."
docker-compose up --build -d

# Esperar a que los servicios estén listos
echo "⏳ Esperando a que los servicios estén listos..."
sleep 10

# Mostrar el estado de los servicios
echo "📊 Estado de los servicios:"
docker-compose ps

# Mostrar logs en tiempo real
echo ""
echo "📝 Mostrando logs del backend (Ctrl+C para salir):"
echo "Para ver logs de la base de datos: docker-compose logs -f postgres"
echo "Para detener todos los servicios: docker-compose down"
echo ""
docker-compose logs -f backend