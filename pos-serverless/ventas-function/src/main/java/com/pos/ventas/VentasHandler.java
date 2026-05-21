package com.pos.ventas;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.PutItemEnhancedRequest;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;

public class VentasHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private DynamoDbEnhancedClient dynamoDbEnhancedClient;
    private DynamoDbTable<Sale> saleTable;

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        initializeDynamoDB();

        try {
            // Parse request body
            String body = input.getBody();
            if (body == null || body.trim().isEmpty()) {
                return buildResponse(400, "Request body is required");
            }

            SaleRequest saleRequest = com.fasterxml.jackson.databind.ObjectMapper.class
                .getDeclaredConstructor()
                .newInstance()
                .readValue(body, SaleRequest.class);

            // Validate minimum structure
            if (saleRequest.getTotal() == null) {
                return buildResponse(400, "Total is required");
            }

            // Create sale record
            Sale sale = new Sale();
            String saleId = "SALE#" + UUID.randomUUID().toString();
            sale.setPk(saleId);
            sale.setCreatedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            sale.setPayload(body);
            sale.setTotal(saleRequest.getTotal());
            sale.setStatus("COMPLETED");

            // Save to DynamoDB
            PutItemEnhancedRequest<Sale> putRequest = PutItemEnhancedRequest.builder(Sale.class)
                .item(sale)
                .build();
            saleTable.putItem(putRequest);

            // Build response
            SaleResponse response = new SaleResponse();
            response.setSaleId(saleId);
            response.setStatus("COMPLETED");
            response.setTimestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

            return buildResponse(201, response);

        } catch (Exception e) {
            return buildResponse(500, "Error processing sale: " + e.getMessage());
        }
    }

    private void initializeDynamoDB() {
        if (dynamoDbEnhancedClient == null) {
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
                com.fasterxml.jackson.databind.ObjectMapper.class
                    .getDeclaredConstructor()
                    .newInstance()
                    .writeValueAsString(body);
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
