'use strict';

/**
 * Health Check Handler
 * GET /health
 *
 * Devuelve el estado del servicio sin lógica de negocio.
 * Útil para verificar que el despliegue fue exitoso.
 */
exports.handler = async (event) => {
  console.log('Health check solicitado:', JSON.stringify(event, null, 2));

  return {
    statusCode: 200,
    headers: {
      'Content-Type': 'application/json',
      'Access-Control-Allow-Origin': '*',
    },
    body: JSON.stringify({
      status: 'ok',
      service: 'pos-serverless',
      timestamp: new Date().toISOString(),
      region: process.env.AWS_REGION || 'us-east-1',
    }),
  };
};
