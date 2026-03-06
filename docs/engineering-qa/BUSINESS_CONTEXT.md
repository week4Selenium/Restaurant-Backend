# CONTEXTO DE NEGOCIO - SISTEMA DE PEDIDOS DE RESTAURANTE

## PROPOSITO DEL SISTEMA

El Sistema de Pedidos de Restaurante es una aplicación full-stack diseñada para gestionar el flujo completo de pedidos en un establecimiento gastronómico. Su objetivo principal es digitalizar y automatizar el proceso desde la selección del menú por parte del cliente hasta la preparación en cocina, eliminando el uso de papel y mejorando la comunicación entre el salón y la cocina.

El sistema separa claramente dos roles: los clientes que realizan pedidos desde sus mesas y el personal de cocina que recibe y procesa dichos pedidos en tiempo real.

## DOMINIO DE NEGOCIO

El dominio se centra en la gestión de pedidos de un restaurante físico con las siguientes características de negocio:

El restaurante cuenta con un número limitado de mesas identificadas por números del 1 al 12. Los clientes en cada mesa pueden navegar por un menú digital organizado por categorías y agregar productos a un carrito de compras. Una vez confirmado el pedido, este se transmite inmediatamente a la cocina para su preparación.

El personal de cocina visualiza los pedidos entrantes en una pantalla dedicada donde pueden consultar los detalles de cada orden, incluyendo la mesa de origen, los productos solicitados con sus cantidades y el estado del pedido. La cocina es responsable de actualizar el estado de los pedidos a medida que avanzan en el proceso de preparación.

El sistema mantiene un registro histórico de todos los pedidos realizados para permitir consultas y análisis de reportes.

## ARQUITECTURA GENERAL DEL SISTEMA

El sistema está construido con una arquitectura de microservicios distribuidos que se comunican mediante eventos asincrónicos. La arquitectura separa físicamente tres servicios backend independientes y un frontend web único que sirve tanto para clientes como para cocina.

### Componentes principales del sistema:

Frontend Web: Es una aplicación de página única construida con React que expone dos interfaces diferentes. La interfaz de cliente permite navegar el menú, construir un carrito de compras y realizar pedidos. La interfaz de cocina muestra todos los pedidos activos y permite actualizar su estado. Ambas interfaces se comunican con el backend exclusivamente mediante API REST.

Order Service: Es el microservicio principal del sistema. Expone una API REST en el puerto 8080 que gestiona todas las operaciones CRUD de pedidos, consulta del menú de productos y gestión del estado de las órdenes. Este servicio mantiene la base de datos principal del restaurante llamada restaurant_db donde persiste las órdenes, mesas, productos del menú y los items de cada pedido. Cuando se crea una nueva orden, este servicio publica un evento asíncrono a RabbitMQ con los detalles del pedido.

Kitchen Worker: Es un worker asíncrono que no expone endpoints HTTP. Su única función es escuchar eventos de nuevos pedidos desde RabbitMQ. Cuando recibe un evento de nuevo pedido, lo procesa y persiste una copia en su propia base de datos independiente llamada kitchen_db. Esta separación permite que la cocina tenga su propia proyección de datos optimizada para consultas sin afectar la base de datos principal.

Report Service: Es un microservicio adicional que corre en el puerto 8082 y también consume eventos de pedidos desde RabbitMQ. Mantiene su propia base de datos desnormalizada llamada report_db diseñada específicamente para consultas de reportería. Expone endpoints REST para generar reportes de ventas por fecha y estadísticas.

RabbitMQ: Actúa como broker de mensajería entre los servicios. Utiliza un exchange de tipo topic para distribuir eventos order.placed a múltiples consumidores. Implementa colas de reintentos y Dead Letter Queues para manejar fallos en el procesamiento de mensajes.

### Bases de datos del sistema:

El sistema utiliza PostgreSQL como motor de base de datos con tres instancias completamente segregadas siguiendo el patrón database-per-service:

restaurant_db: Base de datos del Order Service que corre en el puerto 5432. Contiene las tablas orders, menu_items, tables y order_items. Es la fuente de verdad del sistema para pedidos y menú.

kitchen_db: Base de datos del Kitchen Worker que corre en el puerto 5433. Contiene las tablas kitchen_orders, order_items y processing_logs. Es una proyección optimizada para la vista de cocina.

report_db: Base de datos del Report Service que corre en el puerto 5434. Contiene tablas desnormalizadas report_orders y report_order_items para consultas analíticas rápidas.

Todas las bases de datos utilizan Flyway para gestionar migraciones versionadas de esquema.

## TECNOLOGIAS UTILIZADAS

### Backend:

El backend está construido con Java 17 y Spring Boot 3.2.0 como framework principal. Utiliza Spring Data JPA con PostgreSQL 15 para persistencia de datos y Spring AMQP para integración con RabbitMQ. Las migraciones de base de datos se gestionan con Flyway. La documentación de API se genera automáticamente con SpringDoc OpenAPI. Maven gestiona las dependencias en estructura multi-módulo. Las pruebas utilizan JUnit 5 y Mockito. Lombok reduce código boilerplate.

### Frontend:

El frontend es una SPA construida con React 18.3.1 y TypeScript 5.5.4. Vite 5.4.2 actúa como bundler y servidor de desarrollo. Utiliza React Router DOM para navegación, TanStack React Query para gestión de estado del servidor, y Tailwind CSS para estilos. Las pruebas se ejecutan con Vitest y el código se analiza con ESLint.

### Infraestructura:

Todos los servicios se contienerizan con Docker y orquestan con Docker Compose. RabbitMQ 3 maneja la mensajería asíncrona entre microservicios con interfaz de gestión incluida.

## ESTADO ACTUAL DE IMPLEMENTACION

El sistema se encuentra en estado de MVP funcional con todas las características core implementadas:

Gestión de Menú y Pedidos: El menú contiene 12 productos organizados en tres categorías (entradas, principales, postres) con precios, descripciones e imágenes. El sistema soporta 12 mesas numeradas. Los clientes pueden navegar el menú, agregar productos al carrito con gestión de cantidades y confirmar pedidos. La creación de pedidos es end-to-end con validación de datos, persistencia en estado PENDING, generación de UUID y publicación de eventos a RabbitMQ.

Procesamiento Asíncrono: El Kitchen Worker consume eventos de RabbitMQ, procesa pedidos automáticamente, los persiste en kitchen_db y actualiza estados a IN_PREPARATION. Implementa reintentos exponenciales y Dead Letter Queue para manejo de errores.

Gestión de Estados: Los pedidos transitan por estados PENDING, IN_PREPARATION y READY. La actualización de estados funciona mediante endpoint PATCH protegido con token de cocina (X-Kitchen-Token con valor hardcodeado kitchen-secret-2024). La seguridad implementa patrón Chain of Responsibility con validadores de scope, presencia y valor del token.

Consultas y Reportes: Los clientes consultan estado de pedidos mediante GET /orders/{orderId}. La cocina consulta todos los pedidos activos con GET /orders. El Report Service mantiene proyección desnormalizada en report_db y expone endpoint GET /reports/orders para consultas por rango de fechas con agregaciones.

Interfaces de Usuario: El frontend incluye dos interfaces completas. La interfaz de cliente tiene selector de mesa, navegación de menú por categorías, carrito de compras y consulta de estado. La interfaz de cocina requiere autenticación con token y muestra dashboard con polling cada 3 segundos, tarjetas de pedidos con codificación por colores y botones para actualizar estados.

Otras Funcionalidades: Soft delete de pedidos mediante DELETE /orders/{orderId}. Consulta de menú con GET /menu. Navegación con React Router y rutas protegidas. Imágenes servidas desde Unsplash. Cobertura de pruebas con 61 tests en Order Service y 13 en Kitchen Worker.

## FLUJOS DE NEGOCIO PRINCIPALES

### Flujo de creación de pedido:

El cliente ingresa al sistema desde la pantalla de bienvenida y selecciona el número de su mesa. El sistema lo redirige a la vista de menú donde visualiza todos los productos activos organizados por categorías. El cliente navega las categorías y hace clic en "Agregar al carrito" en los productos deseados. Cada producto agregado se añade al carrito con cantidad inicial de 1. El cliente puede incrementar o decrementar cantidades desde el carrito. El carrito muestra el subtotal de cada item y el total general en tiempo real.

Cuando el cliente confirma el pedido, el frontend valida que el carrito no esté vacío y envía una petición POST /orders al Order Service con el payload JSON que contiene tableId y un array de items con productId y quantity de cada uno. El Order Service valida que tableId esté entre 1 y 12, que todos los productId existan en el menú y estén activos, y que las cantidades sean mayores a cero.

Si la validación pasa, el Order Service calcula el total del pedido multiplicando cada cantidad por el precio actual del producto y sumando todos los subtotales. Crea una entidad Order en estado PENDING con fecha actual y persiste en restaurant_db incluyendo todos los order items asociados. El servicio genera un UUID único para el pedido.

Inmediatamente después de persistir, el Order Service construye un OrderPlacedEvent con todos los detalles del pedido y lo publica al exchange de RabbitMQ usando routing key order.placed. El servicio retorna al frontend un response HTTP 201 Created con el orderId en el body.

El frontend muestra al cliente una pantalla de confirmación con el orderId y un mensaje indicando que su pedido fue recibido y está siendo procesado por la cocina. El cliente puede copiar el orderId para consultar el estado después.

### Flujo de procesamiento en cocina:

El Kitchen Worker mantiene un listener activo en la cola de RabbitMQ bindeada al routing key order.placed. Cuando llega un nuevo evento OrderPlacedEvent, el worker lo deserializa a un objeto Java y valida que contenga todos los campos requeridos como orderId, tableId e items.

Si el evento es válido, el worker crea una entidad KitchenOrder con los datos del evento y la persiste en kitchen_db. Registra el timestamp de procesamiento en la tabla processing_logs para auditoría. Actualiza el estado del pedido a IN_PREPARATION.

Si el procesamiento es exitoso, el worker envía un ACK acknowledgement a RabbitMQ para remover el mensaje de la cola. Si el procesamiento falla por error transitorio como timeout de base de datos, el worker envía un NACK y RabbitMQ reintentará el mensaje después de un delay exponencial. Si el mensaje falla 3 veces consecutivas, se mueve automáticamente a la Dead Letter Queue para análisis manual.

Paralelamente, el personal de cocina tiene abierta la interfaz web de cocina que muestra un dashboard en tiempo real. Para acceder a esta interfaz, primero ingresan el kitchen token en una pantalla de autenticación. Una vez autenticados, la interfaz realiza polling periódico cada 3 segundos mediante peticiones GET /orders para obtener todos los pedidos activos.

El dashboard muestra una tarjeta card por cada pedido con información de número de mesa, lista de productos con nombres y cantidades, total del pedido, hora de creación, y estado actual con codificación visual por colores. Los pedidos PENDING aparecen en amarillo, IN_PREPARATION en azul y READY en verde.

Cada tarjeta de pedido incluye botones para cambiar el estado. El personal de cocina hace clic en "Comenzar" para cambiar de PENDING a IN_PREPARATION, o en "Marcar listo" para cambiar de IN_PREPARATION a READY. Al hacer clic, el frontend envía una petición PATCH /orders/{orderId}/status con el nuevo estado en el body y el header X-Kitchen-Token.

El Order Service valida el token, verifica que la transición de estado sea válida según las reglas del enum OrderStatus, actualiza el estado en restaurant_db y retorna HTTP 200. El frontend muestra retroalimentación visual inmediata actualizando el color de la tarjeta y el texto del estado.

### Flujo de consulta de estado:

El cliente desde su mesa puede consultar el estado actual de su pedido ingresando a la pantalla de consulta en la interfaz web. Ingresa el orderId UUID que recibió al crear el pedido. El frontend envía petición GET /orders/{orderId} al Order Service.

Si el orderId existe y no está marcado como deleted, el servicio retorna HTTP 200 con el objeto completo del pedido incluyendo todos sus items, estado actual, total y timestamps. El frontend muestra una pantalla con resumen del pedido incluyendo mesa, lista de productos, total y estado prominente con codificación de color.

Si el orderId no existe o fue eliminado, el servicio retorna HTTP 404 Not Found y el frontend muestra mensaje de pedido no encontrado.

### Flujo de generación de reportes:

El Report Service escucha eventos OrderPlacedEvent igual que el Kitchen Worker pero los procesa con propósito diferente. Cuando recibe un evento, persiste los datos en su base de datos report_db utilizando tablas desnormalizadas optimizadas para queries analíticas.

El gerente o administrador puede consultar reportes accediendo directamente a los endpoints del Report Service en puerto 8082. Para obtener reporte de pedidos en rango de fechas, envía GET /reports/orders con parámetros startDate y endDate en formato YYYY-MM-DD.

El Report Service ejecuta query en report_db filtrando por rango de fechas, calcula agregaciones como total de ventas, cantidad de pedidos, ticket promedio, y retorna array de pedidos con toda la información. La respuesta incluye desglose por día y totales generales.

### Flujo de eliminación de pedido:

Un administrador puede eliminar un pedido enviando petición DELETE /orders/{orderId} al Order Service. El servicio valida que el orderId exista, actualiza los campos deleted a true y deleted_at con timestamp actual pero no elimina físicamente el registro de la base de datos.

Los pedidos eliminados se excluyen automáticamente de todas las consultas mediante filtros JPA con anotación @Where en la entidad. El servicio retorna HTTP 204 No Content. Los eventos de pedidos eliminados no se propagan a otros servicios, por lo que las proyecciones en kitchen_db y report_db mantienen el pedido visible aunque esté eliminado en restaurant_db. Esto es intencional para mantener historiales completos en reportería.



