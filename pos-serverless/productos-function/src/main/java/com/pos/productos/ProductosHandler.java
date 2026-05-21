package com.pos.productos;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProductosHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private DynamoDbEnhancedClient dynamoDbEnhancedClient;
    private DynamoDbTable<Product> productTable;

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        initializeDynamoDB();

        String queryString = input.getQueryStringParameters() != null ? 
            input.getQueryStringParameters().get("q") : null;

        if (queryString == null || queryString.trim().isEmpty()) {
            return buildResponse(400, "Query parameter 'q' is required");
        }

        String trimmedQuery = queryString.trim();

        List<Product> products;
        if (trimmedQuery.matches("\\d+")) {
            // Búsqueda por código de barras
            products = searchByBarcode(trimmedQuery);
        } else {
            // Búsqueda por nombre/categoría
            products = searchByNameOrCategory(trimmedQuery);
        }

        ProductosResponse response = new ProductosResponse();
        response.setProducts(products);
        response.setCount(products.size());

        return buildResponse(200, response);
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
            productTable = dynamoDbEnhancedClient.table(
                System.getenv("PRODUCT_TABLE_NAME"),
                TableSchema.fromBean(Product.class)
            );
        }
    }

    private List<Product> searchByBarcode(String barcode) {
        try {
            Product product = productTable.getItem(Key.builder().partitionValue(barcode).build());
            if (product != null) {
                List<Product> result = new ArrayList<>();
                result.add(product);
                return result;
            }
            return new ArrayList<>();
        } catch (Exception e) {
            throw new RuntimeException("Error searching by barcode: " + e.getMessage(), e);
        }
    }

    private List<Product> searchByNameOrCategory(String query) {
        try {
            // Scan table and filter in memory (simple approach for MVP)
            // For production, use GSI with name/category attributes
            List<Product> results = new ArrayList<>();
            
            productTable.scan().items().forEach(product -> {
                if (product.getName() != null && 
                    product.getName().toLowerCase().contains(query.toLowerCase())) {
                    results.add(product);
                } else if (product.getCategory() != null && 
                           product.getCategory().toLowerCase().contains(query.toLowerCase())) {
                    results.add(product);
                }
            });

            return results;
        } catch (Exception e) {
            throw new RuntimeException("Error searching by name/category: " + e.getMessage(), e);
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
