# Diseño - API REST Punto de Venta Supermercado

## Arquitectura General

```
pos-api/
├── src/main/java/com/pos/api/
│   ├── PosApiApplication.java
│   ├── config/
│   │   ├── SecurityConfig.java
│   │   ├── SwaggerConfig.java
│   │   └── JpaConfig.java
│   ├── controller/
│   │   ├── ProductController.java
│   │   ├── CartController.java
│   │   ├── SaleController.java
│   │   ├── ReturnController.java
│   │   └── ReceiptController.java
│   ├── service/
│   │   ├── ProductService.java
│   │   ├── CartService.java
│   │   ├── SaleService.java
│   │   ├── ReturnService.java
│   │   └── ReceiptService.java
│   ├── repository/
│   │   ├── ProductRepository.java
│   │   ├── CartRepository.java
│   │   ├── SaleRepository.java
│   │   ├── ReturnRepository.java
│   │   └── ReceiptRepository.java
│   ├── model/
│   │   ├── Product.java
│   │   ├── Cart.java
│   │   ├── CartItem.java
│   │   ├── Sale.java
│   │   ├── SaleItem.java
│   │   ├── Return.java
│   │   ├── ReturnItem.java
│   │   ├── Receipt.java
│   │   └── Payment.java
│   ├── dto/
│   │   ├── product/
│   │   │   ├── ProductRequest.java
│   │   │   ├── ProductResponse.java
│   │   │   └── ProductSearchResponse.java
│   │   ├── cart/
│   │   │   ├── CartRequest.java
│   │   │   ├── CartItemRequest.java
│   │   │   ├── CartResponse.java
│   │   │   └── CartItemResponse.java
│   │   ├── sale/
│   │   │   ├── SaleRequest.java
│   │   │   ├── SaleResponse.java
│   │   │   ├── CheckoutRequest.java
│   │   │   └── CheckoutResponse.java
│   │   ├── payment/
│   │   │   ├── PaymentRequest.java
│   │   │   └── PaymentResponse.java
│   │   ├── return/
│   │   │   ├── ReturnRequest.java
│   │   │   └── ReturnResponse.java
│   │   └── receipt/
│   │       └── ReceiptResponse.java
│   ├── exception/
│   │   ├── GlobalExceptionHandler.java
│   │   ├── BusinessException.java
│   │   ├── ResourceNotFoundException.java
│   │   └── ValidationException.java
│   └── util/
│       ├── MoneyUtil.java
│       └── ReceiptGenerator.java
├── src/test/java/com/pos/api/
│   ├── controller/
│   │   ├── ProductControllerTest.java
│   │   ├── CartControllerTest.java
│   │   ├── SaleControllerTest.java
│   │   └── ReturnControllerTest.java
│   ├── service/
│   │   ├── ProductServiceTest.java
│   │   ├── CartServiceTest.java
│   │   ├── SaleServiceTest.java
│   │   └── ReturnServiceTest.java
│   ├── repository/
│   │   ├── ProductRepositoryTest.java
│   │   └── SaleRepositoryTest.java
│   └── integration/
│       ├── ProductIntegrationTest.java
│       ├── SaleIntegrationTest.java
│       └── ReturnIntegrationTest.java
└── src/main/resources/
    ├── application.yml
    ├── data-h2.sql
    └── schema-h2.sql
```

## Modelos de Datos

### Product
```java
- id: Long (PK)
- barcode: String (unique, not null)
- name: String (not null)
- description: String
- price: BigDecimal (not null)
- cost: BigDecimal
- stock: Integer (not null)
- category: String
- active: Boolean (default: true)
- createdAt: LocalDateTime
- updatedAt: LocalDateTime
```

### Cart
```java
- id: Long (PK)
- status: CartStatus (ACTIVE, FROZEN, CANCELLED)
- totalAmount: BigDecimal
- discountAmount: BigDecimal
- finalAmount: BigDecimal
- createdAt: LocalDateTime
- updatedAt: LocalDateTime
```

### CartItem
```java
- id: Long (PK)
- cart: Cart (FK)
- product: Product (FK)
- quantity: Integer (not null)
- unitPrice: BigDecimal (not null)
- discountAmount: BigDecimal
- finalPrice: BigDecimal
```

### Sale
```java
- id: Long (PK)
- cart: Cart (FK)
- receipt: Receipt (FK)
- status: SaleStatus (COMPLETED, CANCELLED, RETURNED)
- totalAmount: BigDecimal
- discountAmount: BigDecimal
- paidAmount: BigDecimal
- changeAmount: BigDecimal
- paymentMethod: PaymentMethod (CASH, CREDIT, CARD)
- createdAt: LocalDateTime
- cancelledAt: LocalDateTime
```

### SaleItem
```java
- id: Long (PK)
- sale: Sale (FK)
- product: Product (FK)
- quantity: Integer (not null)
- unitPrice: BigDecimal (not null)
- discountAmount: BigDecimal
- finalPrice: BigDecimal
```

### Return
```java
- id: Long (PK)
- sale: Sale (FK)
- type: ReturnType (FULL, PARTIAL)
- status: ReturnStatus (PENDING, APPROVED, REJECTED)
- totalAmount: BigDecimal
- reason: String
- createdAt: LocalDateTime
- approvedAt: LocalDateTime
```

### ReturnItem
```java
- id: Long (PK)
- return: Return (FK)
- saleItem: SaleItem (FK)
- quantity: Integer (not null)
- refundAmount: BigDecimal
```

### Receipt
```java
- id: Long (PK)
- sale: Sale (FK)
- return: Return (FK)
- pdfUrl: String
- xmlUrl: String
- totalAmount: BigDecimal
- paidAmount: BigDecimal
- changeAmount: BigDecimal
- paymentMethod: PaymentMethod
- items: List<ReceiptItem>
- createdAt: LocalDateTime
```

### Payment
```java
- id: Long (PK)
- sale: Sale (FK)
- amount: BigDecimal (not null)
- method: PaymentMethod (not null)
- reference: String
- approved: Boolean
- createdAt: LocalDateTime
```

## Endpoints de la API

### Productos
```
GET    /api/products              - Listar productos (paginado, filtros)
GET    /api/products/{id}         - Obtener producto por ID
GET    /api/products/barcode/{barcode} - Buscar por código de barras
POST   /api/products              - Crear producto
PUT    /api/products/{id}         - Actualizar producto (total)
PATCH  /api/products/{id}         - Actualizar producto (parcial)
DELETE /api/products/{id}         - Eliminar producto (soft delete)
```

### Carrito
```
GET    /api/carts                 - Listar carritos
GET    /api/carts/{id}            - Obtener carrito por ID
POST   /api/carts                 - Crear carrito
PATCH  /api/carts/{id}/freeze     - Congelar carrito
PATCH  /api/carts/{id}/resume     - Reanudar carrito
PATCH  /api/carts/{id}/cancel     - Cancelar carrito
DELETE /api/carts/{id}            - Eliminar carrito
```

### Items del Carrito
```
POST   /api/carts/{cartId}/items  - Agregar item al carrito
PUT    /api/carts/{cartId}/items/{itemId} - Actualizar item
DELETE /api/carts/{cartId}/items/{itemId} - Eliminar item
DELETE /api/carts/{cartId}/items  - Limpiar carrito
```

### Ventas y Checkout
```
POST   /api/sales/checkout        - Realizar checkout
GET    /api/sales                 - Listar ventas
GET    /api/sales/{id}            - Obtener venta por ID
PATCH  /api/sales/{id}/cancel     - Cancelar venta
GET    /api/sales/{id}/receipt    - Obtener recibo de venta
```

### Devoluciones
```
POST   /api/returns               - Crear devolución
GET    /api/returns               - Listar devoluciones
GET    /api/returns/{id}          - Obtener devolución por ID
PATCH  /api/returns/{id}/approve  - Aprobar devolución
PATCH  /api/returns/{id}/reject   - Rechazar devolución
GET    /api/returns/{id}/receipt  - Obtener recibo de devolución
```

### Recibos
```
GET    /api/receipts/{id}         - Obtener recibo por ID
GET    /api/receipts/pdf/{id}     - Descargar PDF del recibo
GET    /api/receipts/date-range   - Recibos por rango de fecha
```

### Reportes
```
GET    /api/reports/sales/daily   - Ventas diarias
GET    /api/reports/sales/weekly  - Ventas semanales
GET    /api/reports/sales/monthly - Ventas mensuales
GET    /api/reports/top-products  - Productos más vendidos
GET    /api/reports/payments      - Ingresos por método de pago
GET    /api/reports/returns       - Devoluciones por período
```

## Validaciones

### Product
- barcode: required, unique, min 1, max 50
- name: required, min 1, max 200
- price: required, positive, max 2 decimales
- stock: required, non-negative
- category: max 100

### Cart
- status: ACTIVE, FROZEN, CANCELLED
- totalAmount: calculated
- discountAmount: >= 0
- finalAmount: >= 0

### CartItem
- quantity: required, positive, max 999
- unitPrice: required, positive
- discountAmount: >= 0

### Sale
- status: COMPLETED, CANCELLED, RETURNED
- paymentMethod: CASH, CREDIT, CARD
- paidAmount: >= 0
- changeAmount: >= 0 (solo CASH)

### Return
- type: FULL, PARTIAL
- status: PENDING, APPROVED, REJECTED
- totalAmount: >= 0

## Excepciones Personalizadas

- **BusinessException**: Errores de negocio (stock insuficiente, venta ya devuelta)
- **ResourceNotFoundException**: Recurso no encontrado
- **ValidationException**: Errores de validación
- **InsufficientStockException**: Stock insuficiente
- **InvalidCartStateException**: Estado inválido del carrito
- **PaymentException**: Errores de pago

## Configuración de Base de Datos (H2)

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:posdb
    driverClassName: org.h2.Driver
    username: sa
    password:
  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
    hibernate:
      ddl-auto: create-drop
    show-sql: true
  h2:
    console:
      enabled: true
```

## Cobertura de Pruebas

- **Pruebas Unitarias**: ≥ 80% cobertura
  - Services: ≥ 85%
  - Repositories: ≥ 75%
  - Utilidades: 100%

- **Pruebas de Integración**: ≥ 80% cobertura
  - Controllers: ≥ 80%
  - Endpoints críticos: 100%

- **Casos de Prueba**:
  - Escenarios felices (happy paths)
  - Escenarios de error
  - Escenarios edge case
  - Validaciones de negocio

## Estructura de Paquetes

```
com.pos.api
├── config          # Configuración Spring (Security, Swagger, JPA)
├── controller      # REST Controllers
├── service         # Lógica de negocio
├── repository      # Data Access Layer (JPA)
├── model           # Entidades JPA
├── dto             # Data Transfer Objects
├── exception       # Excepciones personalizadas
├── util            # Utilidades (Money, ReceiptGenerator)
└── test            # Pruebas (unit, integration, controller)
```

## Patrones de Diseño

- **Repository Pattern**: Abstracción de acceso a datos
- **Service Layer Pattern**: Lógica de negocio separada
- **DTO Pattern**: Separación de modelos de dominio y API
- **Builder Pattern**: Para objetos complejos (Receipt)
- **Strategy Pattern**: Para métodos de pago
- **Factory Pattern**: Para creación de objetos

## Seguridad

- JWT Authentication
- Role-based authorization
- Input validation con Jakarta Validation
- SQL injection prevention (JPA/Hibernate)
- XSS prevention (Jackson JSON)
