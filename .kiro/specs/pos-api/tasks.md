# Tareas - API REST Punto de Venta Supermercado

## Fase 1: Configuración del Proyecto

- [ ] **T01**: Crear proyecto Maven con Spring Boot 3.x
  - [ ] Inicializar proyecto con Spring Initializr
  - [ ] Configurar Java 17
  - [ ] Agregar dependencias: Spring Web, Spring Data JPA, H2, Validation, Test
  - [ ] Configurar JaCoCo para cobertura

- [ ] **T02**: Configurar estructura de carpetas
  - [ ] Crear paquetes: controller, service, repository, model, dto, exception, util, config
  - [ ] Crear estructura de pruebas: test/java, test/resources

- [ ] **T03**: Configurar archivos de configuración
  - [ ] application.yml con configuración H2
  - [ ] application-test.yml para pruebas
  - [ ] pom.xml con plugins de test y cobertura

## Fase 2: Modelos y Repositorios

- [ ] **T04**: Crear modelo Product
  - [ ] Entidad con JPA annotations
  - [ ] Validaciones con Jakarta Validation
  - [ ] Constructor, getters, setters
  - [ ] @CreatedDate, @LastModifiedDate

- [ ] **T05**: Crear modelo Cart
  - [ ] Entidad con JPA annotations
  - [ ] Enum CartStatus (ACTIVE, FROZEN, CANCELLED)
  - [ ] Relación OneToMany con CartItem

- [ ] **T06**: Crear modelo CartItem
  - [ ] Entidad con JPA annotations
  - [ ] Relaciones ManyToOne con Cart y Product
  - [ ] Validaciones de cantidad y precios

- [ ] **T07**: Crear modelo Sale
  - [ ] Entidad con JPA annotations
  - [ ] Enum SaleStatus (COMPLETED, CANCELLED, RETURNED)
  - [ ] Enum PaymentMethod (CASH, CREDIT, CARD)
  - [ ] Relaciones con Cart y Receipt

- [ ] **T08**: Crear modelo SaleItem
  - [ ] Entidad con JPA annotations
  - [ ] Relaciones ManyToOne con Sale y Product

- [ ] **T09**: Crear modelo Return
  - [ ] Entidad con JPA annotations
  - [ ] Enum ReturnType (FULL, PARTIAL)
  - [ ] Enum ReturnStatus (PENDING, APPROVED, REJECTED)
  - [ ] Relación ManyToOne con Sale

- [ ] **T10**: Crear modelo ReturnItem
  - [ ] Entidad con JPA annotations
  - [ ] Relaciones ManyToOne con Return y SaleItem

- [ ] **T11**: Crear modelo Receipt
  - [ ] Entidad con JPA annotations
  - [ ] Relaciones con Sale y Return
  - [ ] Lista de ReceiptItem

- [ ] **T12**: Crear modelo Payment
  - [ ] Entidad con JPA annotations
  - [ ] Enum PaymentMethod
  - [ ] Relación ManyToOne con Sale

- [ ] **T13**: Crear Repositories
  - [ ] ProductRepository (con métodos custom)
  - [ ] CartRepository
  - [ ] CartItemRepository
  - [ ] SaleRepository
  - [ ] SaleItemRepository
  - [ ] ReturnRepository
  - [ ] ReturnItemRepository
  - [ ] ReceiptRepository
  - [ ] PaymentRepository

## Fase 3: DTOs

- [ ] **T14**: Crear DTOs de Product
  - [ ] ProductRequest
  - [ ] ProductResponse
  - [ ] ProductSearchResponse

- [ ] **T15**: Crear DTOs de Cart
  - [ ] CartRequest
  - [ ] CartItemRequest
  - [ ] CartResponse
  - [ ] CartItemResponse

- [ ] **T16**: Crear DTOs de Sale
  - [ ] SaleRequest
  - [ ] SaleResponse
  - [ ] CheckoutRequest
  - [ ] CheckoutResponse

- [ ] **T17**: Crear DTOs de Payment
  - [ ] PaymentRequest
  - [ ] PaymentResponse

- [ ] **T18**: Crear DTOs de Return
  - [ ] ReturnRequest
  - [ ] ReturnResponse

- [ ] **T19**: Crear DTOs de Receipt
  - [ ] ReceiptResponse

## Fase 4: Utilidades

- [ ] **T20**: Crear MoneyUtil
  - [ ] Formatear moneda
  - [ ] Redondear decimales
  - [ ] Comparar montos

- [ ] **T21**: Crear ReceiptGenerator
  - [ ] Generar contenido de recibo
  - [ ] Generar PDF (opcional)

## Fase 5: Services

- [ ] **T22**: Implementar ProductService
  - [ ] CRUD de productos
  - [ ] Búsqueda por código de barras
  - [ ] Búsqueda por nombre/descripción
  - [ ] Validación de stock

- [ ] **T23**: Implementar CartService
  - [ ] Crear carrito
  - [ ] Agregar item
  - [ ] Actualizar item
  - [ ] Eliminar item
  - [ ] Limpiar carrito
  - [ ] Congelar/reanudar carrito
  - [ ] Cancelar carrito
  - [ ] Calcular totales

- [ ] **T24**: Implementar SaleService
  - [ ] Realizar checkout
  - [ ] Validar stock antes de venta
  - [ ] Actualizar stock después de venta
  - [ ] Generar recibo
  - [ ] Cancelar venta
  - [ ] Consultar venta

- [ ] **T25**: Implementar ReturnService
  - [ ] Crear devolución completa
  - [ ] Crear devolución parcial
  - [ ] Validar devolución
  - [ ] Actualizar stock
  - [ ] Generar recibo de devolución
  - [ ] Aprobar/rechazar devolución

- [ ] **T26**: Implementar ReceiptService
  - [ ] Generar recibo
  - [ ] Consultar recibo
  - [ ] Exportar reportes

## Fase 6: Controllers

- [ ] **T27**: Implementar ProductController
  - [ ] GET /api/products
  - [ ] GET /api/products/{id}
  - [ ] GET /api/products/barcode/{barcode}
  - [ ] POST /api/products
  - [ ] PUT /api/products/{id}
  - [ ] PATCH /api/products/{id}
  - [ ] DELETE /api/products/{id}

- [ ] **T28**: Implementar CartController
  - [ ] GET /api/carts
  - [ ] GET /api/carts/{id}
  - [ ] POST /api/carts
  - [ ] PATCH /api/carts/{id}/freeze
  - [ ] PATCH /api/carts/{id}/resume
  - [ ] PATCH /api/carts/{id}/cancel
  - [ ] DELETE /api/carts/{id}

- [ ] **T29**: Implementar CartItemController
  - [ ] POST /api/carts/{cartId}/items
  - [ ] PUT /api/carts/{cartId}/items/{itemId}
  - [ ] DELETE /api/carts/{cartId}/items/{itemId}
  - [ ] DELETE /api/carts/{cartId}/items

- [ ] **T30**: Implementar SaleController
  - [ ] POST /api/sales/checkout
  - [ ] GET /api/sales
  - [ ] GET /api/sales/{id}
  - [ ] PATCH /api/sales/{id}/cancel
  - [ ] GET /api/sales/{id}/receipt

- [ ] **T31**: Implementar ReturnController
  - [ ] POST /api/returns
  - [ ] GET /api/returns
  - [ ] GET /api/returns/{id}
  - [ ] PATCH /api/returns/{id}/approve
  - [ ] PATCH /api/returns/{id}/reject
  - [ ] GET /api/returns/{id}/receipt

- [ ] **T32**: Implementar ReceiptController
  - [ ] GET /api/receipts/{id}
  - [ ] GET /api/receipts/pdf/{id}
  - [ ] GET /api/receipts/date-range

- [ ] **T33**: Implementar ReportController
  - [ ] GET /api/reports/sales/daily
  - [ ] GET /api/reports/sales/weekly
  - [ ] GET /api/reports/sales/monthly
  - [ ] GET /api/reports/top-products
  - [ ] GET /api/reports/payments
  - [ ] GET /api/reports/returns

## Fase 7: Configuración y Excepciones

- [ ] **T34**: Implementar GlobalExceptionHandler
  - [ ] BusinessException
  - [ ] ResourceNotFoundException
  - [ ] ValidationException
  - [ ] InsufficientStockException
  - [ ] InvalidCartStateException
  - [ ] PaymentException

- [ ] **T35**: Configurar Swagger/OpenAPI
  - [ ] SwaggerConfig
  - [ ] Anotaciones @Api, @Operation, @ApiResponse

- [ ] **T36**: Configurar Security (opcional para MVP)
  - [ ] SecurityConfig
  - [ ] JWT Authentication (opcional)

## Fase 8: Pruebas Unitarias

- [ ] **T37**: Pruebas de ProductRepository
  - [ ] findAll con paginación
  - [ ] findByBarcode
  - [ ] findByNameContaining
  - [ ] countByActiveTrue

- [ ] **T38**: Pruebas de CartRepository
  - [ ] findAllByStatus
  - [ ] findByStatus

- [ ] **T39**: Pruebas de SaleRepository
  - [ ] findAllByStatus
  - [ ] findByCreatedAtBetween

- [ ] **T40**: Pruebas de ProductService
  - [ ] CRUD operations
  - [ ] Search by barcode
  - [ ] Search by name
  - [ ] Stock validation

- [ ] **T41**: Pruebas de CartService
  - [ ] Create cart
  - [ ] Add item
  - [ ] Update item
  - [ ] Remove item
  - [ ] Clear cart
  - [ ] Freeze/resume cart
  - [ ] Cancel cart
  - [ ] Calculate totals

- [ ] **T42**: Pruebas de SaleService
  - [ ] Checkout with CASH
  - [ ] Checkout with CREDIT
  - [ ] Checkout with CARD
  - [ ] Stock validation
  - [ ] Update stock
  - [ ] Cancel sale

- [ ] **T43**: Pruebas de ReturnService
  - [ ] Full return
  - [ ] Partial return
  - [ ] Stock update
  - [ ] Approval workflow

- [ ] **T44**: Pruebas de ReceiptService
  - [ ] Generate receipt
  - [ ] Calculate totals

- [ ] **T45**: Pruebas de Utilidades
  - [ ] MoneyUtil tests
  - [ ] ReceiptGenerator tests

## Fase 9: Pruebas de Integración

- [ ] **T46**: Pruebas de ProductController
  - [ ] GET /api/products
  - [ ] GET /api/products/{id}
  - [ ] POST /api/products
  - [ ] PUT /api/products/{id}
  - [ ] DELETE /api/products/{id}

- [ ] **T47**: Pruebas de CartController
  - [ ] GET /api/carts
  - [ ] POST /api/carts
  - [ ] PATCH /api/carts/{id}/freeze

- [ ] **T48**: Pruebas de CartItemController
  - [ ] POST /api/carts/{cartId}/items
  - [ ] PUT /api/carts/{cartId}/items/{itemId}
  - [ ] DELETE /api/carts/{cartId}/items/{itemId}

- [ ] **T49**: Pruebas de SaleController
  - [ ] POST /api/sales/checkout - CASH
  - [ ] POST /api/sales/checkout - CREDIT
  - [ ] POST /api/sales/checkout - CARD
  - [ ] GET /api/sales/{id}
  - [ ] PATCH /api/sales/{id}/cancel

- [ ] **T50**: Pruebas de ReturnController
  - [ ] POST /api/returns - Full return
  - [ ] POST /api/returns - Partial return
  - [ ] PATCH /api/returns/{id}/approve

- [ ] **T51**: Pruebas de ReceiptController
  - [ ] GET /api/receipts/{id}
  - [ ] GET /api/receipts/pdf/{id}

- [ ] **T52**: Pruebas de ReportController
  - [ ] GET /api/reports/sales/daily
  - [ ] GET /api/reports/top-products

## Fase 10: Validación y Cobertura

- [ ] **T53**: Verificar cobertura de pruebas
  - [ ] Ejecutar mvn test con JaCoCo
  - [ ] Verificar ≥ 80% cobertura unitaria
  - [ ] Verificar ≥ 80% cobertura integración

- [ ] **T54**: Revisar y ajustar pruebas
  - [ ] Agregar pruebas faltantes
  - [ ] Ajustar mocks si es necesario

- [ ] **T55**: Validar validaciones
  - [ ] Validar input con Jakarta Validation
  - [ ] Validar negocio (stock, estado, etc.)

## Fase 11: Documentación

- [ ] **T56**: Documentar API con Swagger
  - [ ] Anotaciones en controllers
  - [ ] Ejemplos de request/response

- [ ] **T57**: Crear README.md
  - [ ] Instrucciones de instalación
  - [ ] Instrucciones de uso
  - [ ] Endpoints disponibles
  - [ ] Ejemplos de request

## Fase 12: Optimización (Opcional)

- [ ] **T58**: Optimizar queries
  - [ ] Eager/lazy loading
  - [ ] Queries optimizadas

- [ ] **T59**: Caching (opcional)
  - [ ] Cache de productos

- [ ] **T60**: Logging
  - [ ] Logging de operaciones críticas
