// URL base del API Gateway desplegado en AWS.
// Para sobreescribir en desarrollo local, crea .env con:
// VITE_API_BASE_URL=http://localhost:3000
export const API_BASE_URL: string =
  (import.meta.env.VITE_API_BASE_URL as string | undefined) ??
  'https://uyk0n85xdh.execute-api.us-east-2.amazonaws.com/prod';
