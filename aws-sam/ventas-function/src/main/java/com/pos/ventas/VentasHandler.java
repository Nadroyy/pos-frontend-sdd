package com.pos.ventas;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.PutItemEnhancedRequest;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public class VentasHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private DynamoDbEnhancedClient dynamoDbEnhancedClient;
    private DynamoDbTable<Sale> saleTable;

    /** Constructor por defecto — usado por Lambda en producción. */
    public VentasHandler() {}

    /** Constructor para inyección en pruebas unitarias. */
    VentasHandler(DynamoDbTable<Sale> saleTable) {
        this.saleTable = saleTable;
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        initializeDynamoDB();

        try {
            String body = input.getBody();
            if (body == null || body.trim().isEmpty()) {
                return buildResponse(400, "Request body is required");
            }

            SaleRequest saleRequest = new com.fasterxml.jackson.databind.ObjectMapper()
                .readValue(body, SaleRequest.class);

            // Validar que total exista
            if (saleRequest.getTotal() == null) {
                return buildResponse(400, "Total is required");
            }

            // Validar que items exista y no esté vacío
            if (saleRequest.getItems() == null || saleRequest.getItems().isEmpty()) {
                return buildResponse(400, "Items is required and cannot be empty");
            }

            // Crear registro de venta
            Sale sale = new Sale();
            String saleId = UUID.randomUUID().toString();
            sale.setSaleId(saleId);
            sale.setCreatedAt(Instant.now().toString());
            sale.setPayload(body);
            sale.setTotal(saleRequest.getTotal());
            sale.setStatus("REGISTERED");

            // Guardar en DynamoDB
            PutItemEnhancedRequest<Sale> putRequest = PutItemEnhancedRequest.builder(Sale.class)
                .item(sale)
                .build();
            saleTable.putItem(putRequest);

            // Construir respuesta
            SaleResponse response = new SaleResponse();
            response.setSaleId(saleId);
            response.setStatus("REGISTERED");
            response.setTimestamp(Instant.now().toString());

            return buildResponse(201, response);

        } catch (Exception e) {
            return buildResponse(500, "Error processing sale: " + e.getMessage());
        }
    }

    private void initializeDynamoDB() {
        if (saleTable == null) {
            DynamoDbClient dynamoDbClient = DynamoDbClient.builder()
                .region(Region.of(System.getenv("AWS_REGION")))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
            dynamoDbEnhancedClient = DynamoDbEnhancedClient.builder()
                .dynamoDbClient(dynamoDbClient)
                .build();
            saleTable = dynamoDbEnhancedClient.table(
                System.getenv("SALES_TABLE_NAME"),
                TableSchema.fromBean(Sale.class)
            );
        }
    }

    private APIGatewayProxyResponseEvent buildResponse(int statusCode, Object body) {
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        response.setStatusCode(statusCode);
        
        try {
            String jsonBody = body instanceof String ? 
                (String) body : 
                new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(body);
            response.setBody(jsonBody);
        } catch (Exception e) {
            response.setBody("{\"error\": \"Failed to serialize response\"}");
        }
        
        response.setHeaders(Map.of(
            "Content-Type", "application/json",
            "Access-Control-Allow-Origin", "*"
        ));
        
        return response;
    }
}
