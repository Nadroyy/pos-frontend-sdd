# POS Serverless — AWS SAM

Estructura base serverless para el sistema POS Supermercado usando AWS SAM.

---

## ⚠️ Advertencia de seguridad

**Nunca subas credenciales al repositorio.**
No incluyas `access key`, `secret key`, tokens ni archivos `.env` con secretos en Git.
Usa siempre `aws configure` o variables de entorno del sistema.

---

## Requisitos previos

- [AWS CLI](https://docs.aws.amazon.com/cli/latest/userguide/install-cliv2.html) instalado
- [AWS SAM CLI](https://docs.aws.amazon.com/serverless-application-model/latest/developerguide/install-sam-cli.html) instalado
- [Node.js 20+](https://nodejs.org/) instalado
- Una cuenta AWS con permisos para Lambda, API Gateway, DynamoDB, IAM y S3

---

## 1. Configurar credenciales AWS

Ejecuta el siguiente comando y sigue las instrucciones:

```bash
aws configure
```

Se te pedirá:
- **AWS Access Key ID** — tu clave de acceso (no la subas al repo)
- **AWS Secret Access Key** — tu clave secreta (no la subas al repo)
- **Default region name** — usa `us-east-1`
- **Default output format** — usa `json`

---

## 2. Construir el proyecto

Desde la carpeta `aws-sam/`:

```bash
cd aws-sam
sam build
```

Esto compila las funciones Lambda y prepara el artefacto de despliegue en `.aws-sam/build/`.

---

## 3. Primer despliegue (guiado)

```bash
sam deploy --guided
```

SAM te preguntará:
- Nombre del stack → `pos-serverless-sdd`
- Región → `us-east-1`
- Confirmar cambios antes de desplegar → `Y`
- Permitir creación de roles IAM → `Y`
- Guardar configuración en `samconfig.toml` → `Y`

Al finalizar verás los **Outputs** con la URL de la API.

---

## 4. Despliegues posteriores

Una vez configurado el primer despliegue, usa simplemente:

```bash
sam build && sam deploy
```

---

## 5. Probar el endpoint /health

Después del despliegue, copia la `ApiUrl` de los Outputs y ejecuta:

```bash
curl https://<tu-api-id>.execute-api.us-east-1.amazonaws.com/prod/health
```

Respuesta esperada:

```json
{
  "status": "ok",
  "service": "pos-serverless",
  "timestamp": "2026-05-13T...",
  "region": "us-east-1"
}
```

---

## Estructura del proyecto

```
aws-sam/
├── template.yaml                  # Plantilla SAM (infraestructura)
├── samconfig.toml                 # Configuración del CLI
├── README.md                      # Este archivo
└── src/
    └── handlers/
        └── health/
            ├── app.js             # Handler Lambda del endpoint /health
            └── package.json       # Dependencias del handler
```

## Recursos creados

| Recurso        | Tipo                  | Descripción                        |
|----------------|-----------------------|------------------------------------|
| PosApi         | API Gateway           | API REST con endpoint /health      |
| PosApiFunction | Lambda (Node.js 20.x) | Handler del health check           |
| PosTable       | DynamoDB              | Tabla principal con pk/sk (string) |

---

## Eliminar el stack

Para eliminar todos los recursos creados:

```bash
sam delete --stack-name pos-serverless-sdd
```
