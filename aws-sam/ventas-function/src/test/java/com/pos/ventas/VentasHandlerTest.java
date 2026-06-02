package com.pos.ventas;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.model.PutItemEnhancedRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VentasHandlerTest {

    @Mock
    private DynamoDbTable<Sale> saleTable;

    @Mock
    private Context context;

    private VentasHandler handler;

    @BeforeEach
    void setUp() {
        handler = new VentasHandler(saleTable);
    }

    // ── Test 1: venta válida devuelve 201 con saleId y status REGISTERED ──
    @Test
    void testPostVenta_validBody_returns201WithSaleId() {
        String body = """
                {
                  "items": [{"productId": "P001", "quantity": 2, "price": 1.50}],
                  "total": 3.00,
                  "paymentMethod": "CASH"
                }
                """;

        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent();
        request.setBody(body);

        doNothing().when(saleTable).putItem(any(PutItemEnhancedRequest.class));

        APIGatewayProxyResponseEvent response = handler.handleRequest(request, context);

        assertEquals(201, response.getStatusCode());
        assertTrue(response.getBody().contains("saleId"));
        assertTrue(response.getBody().contains("REGISTERED"));

        // Verificar que se guardó exactamente un item en DynamoDB
        @SuppressWarnings("unchecked")
        ArgumentCaptor<PutItemEnhancedRequest<Sale>> captor =
            ArgumentCaptor.forClass(PutItemEnhancedRequest.class);
        verify(saleTable, times(1)).putItem(captor.capture());

        Sale savedSale = captor.getValue().item();
        assertNotNull(savedSale.getSaleId());
        assertFalse(savedSale.getSaleId().isBlank());
        assertEquals("REGISTERED", savedSale.getStatus());
        assertNotNull(savedSale.getCreatedAt());
        assertFalse(savedSale.getCreatedAt().isBlank());
    }

    // ── Test 2: body sin campo "total" devuelve 400 ───────────────────────
    @Test
    void testPostVenta_missingTotal_returns400() {
        String body = """
                {
                  "items": [{"productId": "P001", "quantity": 1}]
                }
                """;

        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent();
        request.setBody(body);

        APIGatewayProxyResponseEvent response = handler.handleRequest(request, context);

        assertEquals(400, response.getStatusCode());
        assertTrue(response.getBody().contains("Total is required"));
        verify(saleTable, never()).putItem(any(PutItemEnhancedRequest.class));
    }

    // ── Test 3: body con items vacío devuelve 400 ────────────────────────
    @Test
    void testPostVenta_emptyItems_returns400() {
        String body = """
                {
                  "items": [],
                  "total": 5.00
                }
                """;

        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent();
        request.setBody(body);

        APIGatewayProxyResponseEvent response = handler.handleRequest(request, context);

        assertEquals(400, response.getStatusCode());
        assertTrue(response.getBody().contains("Items is required"));
        verify(saleTable, never()).putItem(any(PutItemEnhancedRequest.class));
    }

    // ── Test 4: body nulo/vacío devuelve 400 ─────────────────────────────
    @Test
    void testPostVenta_nullBody_returns400() {
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent();
        request.setBody(null);

        APIGatewayProxyResponseEvent response = handler.handleRequest(request, context);

        assertEquals(400, response.getStatusCode());
        assertTrue(response.getBody().contains("Request body is required"));
        verify(saleTable, never()).putItem(any(PutItemEnhancedRequest.class));
    }
}
