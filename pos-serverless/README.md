# POS Serverless API - Supermercado

API REST Serverless en AWS Lambda y API Gateway para gestión de Punto de Venta en supermercado.

## Arquitectura

```
┌─────────────────────────────────────────────────────────────┐
│                    API Gateway (HTTP API)                    │
└─────────────────────────────────────────────────────────────┘
                              │
        ┌─────────────────────┼─────────────────────┐
        │                     │                     │
        ▼                     ▼                     ▼
┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐
│  Productos      │  │   Ventas        │  │   Health Check    │
│  Function       │  │   Function      │  │   (Optional)      │
└─────────────────┘  └─────────────────┘  └─────────────────┘
        │                     │
        ▼                     ▼
┌─────────────────┐  ┌─────────────────┐
│ ProductosTable  │  │  VentasTable    │
│  (DynamoDB)     │  │  (DynamoDB)     │
└─────────────────┘  └─────────────────┘
```

## Endpoints

### GET /productos
Buscar productos por código de barras o nombre/categoría.

**Query Parameters:**
- `q` (required): Código de barras (solo números) o término de búsqueda (texto)

**Ejemplos:**
```bash
# Búsqueda por código de barras
curl "https://api.execute-api.region.amazonaws.com/prod/productos?q=7701234567890"

# Búsqueda por nombre
curl "https://api.execute-api.region.amazonaws.com/prod/productos?q=leche"

# Búsqueda por categoría
curl "https://api.execute-api.region.amazonaws.com/prod/productos?q=abarrotes"
```

**Response:**
```json
{
  "products": [
    {
      "pk": "7701234567890",
      "barcode": "7701234567890",
      "name": "Leche Entera 1L",
      "category": "Lácteos",
      "price": 2.50,
      "stock": 100
    }
  ],
  "count": 1
}
```

### POST /ventas
Crear una nueva venta.

**Request Body:**
```json
{
  "items": [
    {
      "barcode": "7701234567890",
      "name": "Leche Entera 1L",
      "quantity": 2,
      "price": 2.50
    }
  ],
  "total": 5.00,
  "paymentMethod": "CASH",
  "customer": "John Doe"
}
```

**Response:**
```json
{
  "saleId": "SALE#abc123-def456...",
  "status": "COMPLETED",
  "timestamp": "2026-05-20T10:30:00"
}
```

## Requisitos Previos

1. **AWS CLI** - [Instalar AWS CLI](https://docs.aws.amazon.com/cli/latest/userguide/install-cliv2.html)
2. **SAM CLI** - [Instalar SAM CLI](https://docs.aws.amazon.com/serverless-application-model/latest/developerguide/install-sam-cli.html)
3. **Java 21** - Para compilar las Lambdas
4. **Maven** - Para gestionar dependencias

## Configuración

### 1. Configurar credenciales AWS

```bash
aws configure
```

Proporciona:
- AWS Access Key ID
- AWS Secret Access Key
- Default region name (ej: us-east-1)
- Default output format (json)

### 2. Verificar instalación

```bash
aws --version
sam --version
java --version
mvn --version
```

## Despliegue

### Opción 1: Despliegue interactivo (recomendado)

```bash
cd pos-serverless
sam build
sam deploy --guided
```

Durante el despliegue interactivo:
- Presiona Enter para usar valores por defecto
- Confirma los cambios
- Espera a que se complete el despliegue

### Opción 2: Despliegue automático con script

```bash
cd pos-serverless
chmod +x deploy.sh
./deploy.sh
```

### Opción 3: Despliegue directo

```bash
cd pos-serverless
sam build
sam deploy \
  --stack-name pos-serverless-stack \
  --capabilities CAPABILITY_IAM \
  --region us-east-1
```

## Verificar Despliegue

Después del despliegue, verás la URL del API Gateway en los outputs:

```bash
sam deploy --guided | grep PosApiUrl
```

O consulta los outputs del stack:

```bash
aws cloudformation describe-stacks --stack-name pos-serverless-stack --query "Stacks[0].Outputs"
```

## Probar los Endpoints

### 1. Probar GET /productos

```bash
# Configurar variable de entorno
export API_URL="https://xxxxx.execute-api.us-east-1.amazonaws.com/prod"

# Búsqueda por código de barras
curl "$API_URL/productos?q=7701234567890"

# Búsqueda por nombre
curl "$API_URL/productos?q=leche"
```

### 2. Probar POST /ventas

```bash
# Crear venta
curl -X POST "$API_URL/ventas" \
  -H "Content-Type: application/json" \
  -d '{
    "items": [
      {
        "barcode": "7701234567890",
        "name": "Leche Entera 1L",
        "quantity": 2,
        "price": 2.50
      }
    ],
    "total": 5.00,
    "paymentMethod": "CASH",
    "customer": "John Doe"
  }'
```

## Limpiar Recursos

Para eliminar todos los recursos creados:

```bash
sam delete \
  --stack-name pos-serverless-stack \
  --region us-east-1
```

O desde AWS Console:
1. Ir a CloudFormation
2. Seleccionar el stack `pos-serverless-stack`
3. Click en "Delete"

## Estructura del Proyecto

```
pos-serverless/
├── template.yaml              # AWS SAM template
├── deploy.sh                  # Script de despliegue
├── README.md                  # Este archivo
├── productos-function/
│   ├── pom.xml
│   └── src/main/java/com/pos/productos/
│       ├── Product.java
│       ├── ProductosRequest.java
│       ├── ProductosResponse.java
│       └── ProductosHandler.java
└── ventas-function/
    ├── pom.xml
    └── src/main/java/com/pos/ventas/
        ├── Sale.java
        ├── SaleRequest.java
        ├── SaleResponse.java
        └── VentasHandler.java
```

## Costos Estimados

- **DynamoDB**: ~$2.50/mes (tablas en modo on-demand con bajo tráfico)
- **API Gateway**: ~$3.50/mes (HTTP API con bajo tráfico)
- **Lambda**: ~$0.20/mes (ejecuciones bajo el free tier)
- **Total estimado**: ~$6/mes (bajo tráfico)

## Solución de Problemas

### Error: "No credentials found"
```bash
aws configure
```

### Error: "Stack already exists"
```bash
sam delete --stack-name pos-serverless-stack
```

### Error: "Insufficient capacity"
Usa regiones diferentes o cambia a modo provisioned en DynamoDB.

### Lambda no se ejecuta
Verifica los logs en CloudWatch:
```bash
aws cloudwatch describe-alarms-for-metric \
  --metric-name Errors \
  --namespace AWS/Lambda
```

## Mejoras Futuras

- [ ] Agregar autenticación con Cognito
- [ ] Implementar rate limiting
- [ ] Agregar validaciones más estrictas
- [ ] Implementar GSI para búsquedas eficientes
- [ ] Agregar logging con CloudWatch
- [ ] Implementar dead letter queue para errores
- [ ] Agregar métricas y alertas
