#!/bin/bash

# Script para desplegar la aplicación Serverless POS
# Uso: ./deploy.sh

set -e

echo "========================================"
echo "  POS Serverless - Deploy Script"
echo "========================================"

# Verificar si SAM está instalado
if ! command -v sam &> /dev/null; then
    echo "❌ SAM CLI no está instalado. Instálalo desde: https://docs.aws.amazon.com/serverless-application-model/latest/developerguide/install-sam-cli.html"
    exit 1
fi

# Verificar si AWS CLI está instalado
if ! command -v aws &> /dev/null; then
    echo "❌ AWS CLI no está instalado. Instálalo desde: https://docs.aws.amazon.com/cli/latest/userguide/install-cliv2.html"
    exit 1
fi

# Verificar credenciales AWS
if ! aws sts get-caller-identity &> /dev/null; then
    echo "❌ Credenciales AWS no configuradas. Ejecuta: aws configure"
    exit 1
fi

echo "✅ Credenciales AWS verificadas"
echo ""

# Obtener región actual
AWS_REGION=$(aws configure get region 2>/dev/null || echo "us-east-1")
echo "Region: $AWS_REGION"
echo ""

# Construir la aplicación
echo "🔨 Ejecutando sam build..."
sam build

# Desplegar
echo ""
echo "🚀 Ejecutando sam deploy..."
sam deploy \
    --stack-name pos-serverless-stack \
    --capabilities CAPABILITY_IAM \
    --region "$AWS_REGION" \
    --no-confirm-changeset

echo ""
echo "========================================"
echo "  Despliegue completado!"
echo "========================================"
echo ""
echo "URL del API Gateway:"
aws cloudformation describe-stacks \
    --stack-name pos-serverless-stack \
    --query "Stacks[0].Outputs[?OutputKey=='ApiUrl'].OutputValue" \
    --output text
echo ""
echo "Para probar los endpoints, consulta el README.md"
