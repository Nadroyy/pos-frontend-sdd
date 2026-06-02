package com.pos.productos;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Spliterator;
import java.util.Spliterators;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductosHandlerTest {

    @Mock
    private DynamoDbTable<Product> productTable;

    @Mock
    private Context context;

    private ProductosHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ProductosHandler(productTable);
    }

    // ── Test 1: scan exitoso devuelve 200 con lista de productos ──────────
    @Test
    void testGetAllProducts_returnsOk() {
        Product p1 = new Product();
        p1.setPk("P001");
        p1.setName("Leche Entera");
        p1.setCategory("Lácteos");
        p1.setPrice(1.50);
        p1.setStock(100);

        Product p2 = new Product();
        p2.setPk("P002");
        p2.setName("Pan Integral");
        p2.setCategory("Panadería");
        p2.setPrice(0.80);
        p2.setStock(50);

        List<Product> products = Arrays.asList(p1, p2);
        stubScan(products);

        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent();
        APIGatewayProxyResponseEvent response = handler.handleRequest(request, context);

        assertEquals(200, response.getStatusCode());
        assertTrue(response.getBody().contains("Leche Entera"));
        assertTrue(response.getBody().contains("Pan Integral"));
        assertTrue(response.getBody().contains("\"count\":2"));
    }

    // ── Test 2: tabla vacía devuelve 200 con lista vacía ─────────────────
    @Test
    void testGetAllProducts_emptyTable_returns200WithEmptyList() {
        stubScan(Collections.emptyList());

        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent();
        APIGatewayProxyResponseEvent response = handler.handleRequest(request, context);

        assertEquals(200, response.getStatusCode());
        assertTrue(response.getBody().contains("\"count\":0"));
        assertTrue(response.getBody().contains("\"products\":[]"));
    }

    // ── Test 3: excepción en DynamoDB se propaga como RuntimeException ────
    //    scanAllProducts() relanza como RuntimeException; el handler no la
    //    captura, por lo que assertThrows es el comportamiento correcto.
    @Test
    void testGetProducts_dynamoConnectionError_throwsRuntimeException() {
        when(productTable.scan()).thenThrow(
            new RuntimeException("Unable to connect to DynamoDB endpoint")
        );

        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent();

        RuntimeException ex = assertThrows(
            RuntimeException.class,
            () -> handler.handleRequest(request, context)
        );
        assertTrue(ex.getMessage().contains("Unable to connect to DynamoDB endpoint")
            || ex.getMessage().contains("Error scanning products"));
    }

    // ── Test 4: búsqueda por ?q=leche filtra correctamente por nombre ─────
    @Test
    void testSearchByQuery_filtersProductsByName() {
        Product leche = new Product();
        leche.setPk("P001");
        leche.setName("Leche Entera");
        leche.setCategory("Lácteos");

        Product pan = new Product();
        pan.setPk("P002");
        pan.setName("Pan Integral");
        pan.setCategory("Panadería");

        List<Product> products = Arrays.asList(leche, pan);
        stubScan(products);

        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent();
        request.setQueryStringParameters(Map.of("q", "leche"));

        APIGatewayProxyResponseEvent response = handler.handleRequest(request, context);

        assertEquals(200, response.getStatusCode());
        assertTrue(response.getBody().contains("Leche Entera"));
        assertFalse(response.getBody().contains("Pan Integral"));
        assertTrue(response.getBody().contains("\"count\":1"));
    }

    // ── stubScan: configura productTable.scan().items() con una implementación
    //    real de SdkIterable que envuelve la lista — evita mockear spliterator()
    //    e iterator() por separado, que es frágil según la JVM.
    @SuppressWarnings("unchecked")
    private void stubScan(List<Product> products) {
        // SdkIterable concreto: delega forEach/spliterator/iterator a la lista
        SdkIterable<Product> sdkIterable = new SdkIterable<Product>() {
            @Override
            public Iterator<Product> iterator() {
                return products.iterator();
            }
            @Override
            public Spliterator<Product> spliterator() {
                return Spliterators.spliterator(products, 0);
            }
        };

        PageIterable<Product> pageIterable = mock(PageIterable.class);
        lenient().when(pageIterable.items()).thenReturn(sdkIterable);
        when(productTable.scan()).thenReturn(pageIterable);
    }
}
