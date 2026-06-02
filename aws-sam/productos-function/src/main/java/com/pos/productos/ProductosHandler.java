package com.pos.productos;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProductosHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private DynamoDbEnhancedClient dynamoDbEnhancedClient;
    private DynamoDbTable<Product> productTable;

    /** Constructor por defecto — usado por Lambda en producción. */
    public ProductosHandler() {}

    /** Constructor para inyección en pruebas unitarias. */
    ProductosHandler(DynamoDbTable<Product> productTable) {
        this.productTable = productTable;
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        initializeDynamoDB();

        String queryString = input.getQueryStringParameters() != null ? 
            input.getQueryStringParameters().get("q") : null;

        List<Product> products;
        if (queryString == null || queryString.trim().isEmpty()) {
            // Si no llega q, devolver todos los productos (limitado a 100)
            products = scanAllProducts();
        } else {
            String trimmedQuery = queryString.trim();
            if (trimmedQuery.matches("\\d+")) {
                // Búsqueda por código de barras
                products = searchByBarcode(trimmedQuery);
            } else {
                // Búsqueda por nombre o categoría
                products = searchByNameOrCategory(trimmedQuery);
            }
        }

        ProductosResponse response = new ProductosResponse();
        response.setProducts(products);
        response.setCount(products.size());

        return buildResponse(200, response);
    }

    private void initializeDynamoDB() {
        if (productTable == null) {
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

    private List<Product> scanAllProducts() {
        try {
            List<Product> results = new ArrayList<>();
            productTable.scan().items().forEach(results::add);
            return results;
        } catch (Exception e) {
            throw new RuntimeException("Error scanning products: " + e.getMessage(), e);
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
