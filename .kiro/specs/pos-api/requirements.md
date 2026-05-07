# Requisitos - API REST Punto de Venta Supermercado

## Visión General
Sistema RESTful para gestión de punto de venta en supermercado con soporte para productos, ventas, carritos, descuentos, devoluciones y recepción.

## Requisitos Funcionales

### 1. Gestión de Productos (CRUD)
- **RF01**: Crear producto con código de barras único, nombre, descripción, precio, stock, categoría
- **RF02**: Leer lista de productos con paginación y filtros
- **RF03**: Leer producto por ID o código de barras
- **RF04**: Actualizar producto (parcial o total)
- **RF05**: Eliminar producto (soft delete)
- **RF06**: Búsqueda de productos por código de barras
- **RF07**: Búsqueda de productos por nombre/descripción (parcial)
- **RF08**: Validación de stock disponible antes de operaciones de venta

### 2. Gestión de Carrito/Ventas
- **RF09**: Crear nuevo carrito/venta
- **RF10**: Agregar producto al carrito (con validación de stock)
- **RF11**: Actualizar cantidad de producto en carrito
- **RF12**: Eliminar producto del carrito
- **RF13**: Limpiar carrito completo
- **RF14**: Consultar estado de carrito
- **RF15**: Aplicar descuento al carrito (porcentaje o fijo)
- **RF16**: Aplicar descuento por producto específico
- **RF17**: Cancelar venta antes del checkout
- **RF18**: Congelar venta (pausar)
- **RF19**: Reanudar venta congelada
- **RF20**: Listar ventas activas/congeladas

### 3. Checkout y Pago
- **RF21**: Realizar checkout con tres métodos: CASH, CREDIT, CARD
- **RF22**: Calcular total (subtotal, descuentos, total final)
- **RF23**: Generar recibo de compra (formato JSON)
- **RF24**: Validar pago recibido (suficiente para el total)
- **RF25**: Calcular vuelto para pagos en efectivo
- **RF26**: Guardar transacción de pago
- **RF27**: Actualizar stock después del checkout exitoso

### 4. Devoluciones
- **RF28**: Crear devolución por venta existente
- **RF29**: Devolución completa (todos los items)
- **RF30**: Devolución parcial (items específicos)
- **RF31**: Validar que la venta exista y no haya sido devuelta completamente
- **RF32**: Actualizar stock al procesar devolución
- **RF33**: Generar recibo de devolución (formato JSON)
- **RF34**: Reversar descuentos aplicados en devolución

### 5. Gestión de Recibos
- **RF35**: Generar recibo en formato JSON
- **RF36**: Consultar recibo por ID
- **RF37**: Listar recibos por rango de fecha (pospuesto a futuro)
- **RF38**: Exportar reporte de ventas (pospuesto a futuro)

### 6. Reportes y Estadísticas (Fuera de alcance - MVP)
- **RF39**: Ventas diarias/semanales/mensuales (pospuesto)
- **RF40**: Productos más vendidos (pospuesto)
- **RF41**: Ingresos por método de pago (pospuesto)
- **RF42**: Devoluciones por período (pospuesto)

## Requisitos No Funcionales

### Rendimiento
- **RNF01**: Tiempo de respuesta < 200ms para operaciones CRUD de productos
- **RNF02**: Soportar hasta 50 transacciones por segundo (ajustado para MVP)
- **RNF03**: Tiempo de respuesta < 500ms para checkout completo

### Seguridad
- **RNF04**: Validación de input con Jakarta Validation
- **RNF05**: Prevención de inyección SQL
- **RNF06**: Logging de operaciones críticas

### Confianza y Resiliencia
- **RNF08**: Transacciones atómicas (ACID)
- **RNF09**: Manejo de errores consistente
- **RNF10**: Retries para operaciones idempotentes

### Calidad
- **RNF11**: Cobertura de pruebas unitarias ≥ 80%
- **RNF12**: Cobertura de pruebas de integración ≥ 80%
- **RNF13**: CI/CD con JaCoCo

## Restricciones Técnicas
- Java 17
- Spring Boot 3.x
- Maven
- H2 Database (desarrollo)
- Spring Data JPA
- Jakarta Validation
- JUnit 5
- Mockito
- JaCoCo
- REST API con JSON
- Swagger/OpenAPI 3.0 (documentación básica)
