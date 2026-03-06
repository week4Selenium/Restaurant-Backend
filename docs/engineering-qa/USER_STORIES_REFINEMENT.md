# Informe de Refinamiento de Historias de Usuario

Este documento presenta un análisis comparativo entre las Historias de Usuario originales y sus versiones refinadas, identificando las mejoras aplicadas para cumplir con los principios INVEST.

---

## Historia de Usuario #2: Creación de Pedidos

### Cuadro Comparativo

| HU original | HU refinada por la Gema | Diferencias detectadas |
|-------------|-------------------------|------------------------|
| **Requisito 2: Creación de Pedidos**<br><br>**Historia de Usuario:** Como cliente del restaurante, quiero crear un pedido seleccionando los platos del menú que deseo consumir, para que mi solicitud sea enviada a la cocina y puedan preparar mi comida.<br><br>**Criterios de Aceptación:**<br>1. El Order_Service deberá exponer un endpoint POST /orders que acepte tableId y una lista de elementos del pedido<br>2. Cuando se cree un pedido, el Order_Service deberá validar que todos los productIds referenciados existan y estén activos<br>3. Cuando se cree un pedido con datos válidos, el Order_Service deberá persistir el pedido con estado PENDING en la base de datos PostgreSQL<br>4. Cuando se cree un pedido, el Order_Service deberá generar un UUID único como identificador del pedido<br>5. Cuando se cree un pedido, el Order_Service deberá establecer automáticamente las marcas de tiempo createdAt y updatedAt<br>6. Si un productId referenciado no existe o está inactivo, entonces el Order_Service deberá rechazar el pedido y devolver una respuesta de error<br>7. Si el tableId falta o es inválido, entonces el Order_Service deberá rechazar el pedido y devolver una respuesta de error<br>8. Si la lista de elementos del pedido está vacía, entonces el Order_Service deberá rechazar el pedido y devolver una respuesta de error | **Título:** Creación de Pedidos<br><br>**Descripción:** Como cliente del restaurante, quiero crear un pedido seleccionando los platos del menú que deseo consumir, **especificando las cantidades**, para que mi solicitud sea enviada a la cocina y puedan preparar mi comida. **El pedido se enviará inmediatamente después de la confirmación, y la cocina recibirá una notificación en tiempo real.**<br><br>**Criterios de Aceptación:**<br>1. **Endpoint POST /orders:** El Order Service deberá exponer un endpoint POST /orders que acepte tableId y una lista de elementos del pedido con **productId y quantity**<br>2. **Validación de Productos y Cantidades:** Cuando se cree un pedido, el Order Service deberá validar que todos los productIds referenciados existan y estén activos. **Las cantidades de cada producto deberán ser mayores que cero**<br>3. **Persistencia y Estado del Pedido:** Cuando se cree un pedido con datos válidos, el Order Service deberá persistir el pedido con estado PENDING en la base de datos PostgreSQL<br>4. **Generación de UUID:** Cuando se cree un pedido, el Order Service deberá generar un UUID único como identificador del pedido<br>5. **Marcas de Tiempo:** Cuando se cree un pedido, el Order Service deberá establecer automáticamente las marcas de tiempo createdAt y updatedAt<br>6. **Respuesta de Éxito:** Si el pedido es creado con éxito, el Order Service deberá devolver una respuesta **HTTP 201 Created con el orderId en el body**<br>7. **Respuesta de Error:** Si un productId referenciado no existe o está inactivo, el Order Service deberá rechazar el pedido y devolver una respuesta de error **HTTP 404 Not Found con un mensaje descriptivo**. <br>Si tableId falta o es inválido, el Order Service deberá rechazar el pedido y devolver una respuesta de error **HTTP 400 Bad Request con un mensaje descriptivo**. <br> Si la lista de elementos del pedido está vacía, el Order Service deberá rechazar el pedido y devolver una respuesta de error **HTTP 400 Bad Request con un mensaje descriptivo**<br>8. **Publicación de Eventos:** Cuando se cree un pedido con éxito, el Order Service deberá publicar un evento **OrderPlacedEvent a RabbitMQ** con los detalles del pedido | **La Gema añadió:**<br><br>• **Especificación de cantidades:** Se aclara que el cliente puede especificar cantidades por producto<br><br>• **Clarificación de envío inmediato:** Se especifica que el envío es inmediato tras la confirmación y que la cocina recibe notificación en tiempo real<br><br>• **Validación de cantidades:** Se añade la validación explícita de que las cantidades deben ser mayores que cero<br><br>• **Códigos HTTP específicos:** Se detallan los códigos de respuesta HTTP exactos (201 Created, 400 Bad Request)<br><br>• **Mensajes descriptivos:** Se especifica que los errores deben incluir mensajes descriptivos<br><br>• **Respuesta de éxito detallada:** Se define que la respuesta exitosa debe incluir el orderId<br><br>• **Publicación de eventos explícita:** Se añade como criterio de aceptación la publicación del evento OrderPlacedEvent a RabbitMQ<br><br>• **Organización y títulos:** Se organizan los criterios con títulos descriptivos para mejor legibilidad |

---

### Análisis según Principios INVEST (HU #2: Creación de Pedidos)

### **I - Independiente**

✅ **Evaluación:** La historia es independiente.

La creación del pedido no depende de otras historias de usuario (como actualización o consulta de pedidos), aunque sí requiere que el menú (productos) y las mesas existan como prerequisitos del sistema.

**Conclusión:** Cumple con el principio de independencia.

---

### **N - Negociable**

⚠️ **Evaluación:** Es negociable, aunque los criterios están muy definidos.

**Observaciones:**
- La historia actual tiene criterios técnicos muy específicos
- Existe flexibilidad potencial en áreas como:
  - Validación de productos (¿permitir productos inactivos en casos especiales?)
  - Manejo de mesas (ya refinado: tableId opcional para take away)
  - Reglas de negocio adicionales

**Recomendación:** Señalar explícitamente las áreas donde el negocio podría ajustar reglas o lógicas. La versión refinada mejora esto al hacer el tableId opcional, permitiendo casos de take away.

**Conclusión:** Cumple parcialmente. La refinación mejora la negociabilidad.

---

### **V - Valiosa**

✅ **Evaluación:** Aporta valor directo al negocio.

Esta historia representa el punto de entrada fundamental para el sistema de pedidos del restaurante. Sin la capacidad de crear pedidos, el sistema no cumple su propósito principal.

**Valor de negocio:**
- Permite a los clientes realizar pedidos
- Facilita la comunicación automática con la cocina
- Genera el registro de reportes

**Conclusión:** Cumple plenamente con el principio de valor.

---

### **E - Estimable**

✅ **Evaluación:** Es estimable.

Los criterios técnicos y funcionales están claramente definidos:
- Endpoint REST específico (POST /orders)
- Validaciones concretas
- Integración con base de datos y RabbitMQ
- Respuestas HTTP específicas

El equipo de desarrollo puede estimar el esfuerzo necesario con precisión.

**Conclusión:** Cumple con el principio de estimabilidad.

---

### **S - Small (Pequeña)**

✅⚠️ **Evaluación:** Es suficientemente pequeña para una iteración.

**Análisis:**
- La historia cubre múltiples aspectos: validación, persistencia, eventos
- Puede completarse en un sprint típico (1-2 semanas)
- Sin embargo, podría dividirse si se agregan más reglas de negocio complejas

**Consideraciones para división potencial:**
1. Historia 1: Creación básica de pedido (endpoint + persistencia)
2. Historia 2: Validaciones avanzadas de productos y cantidades
3. Historia 3: Publicación de eventos a RabbitMQ

**Recomendación:** Si la validación de productos resulta compleja o si se agregan validaciones de negocio adicionales (por ejemplo, validaciones de horarios, disponibilidad limitada, etc.), considerar dividir la historia.

**Conclusión:** Cumple adecuadamente. Es pequeña pero no trivial.

---

### **T - Testeable**

✅ **Evaluación:** Es testeable.

Los criterios de aceptación permiten la construcción de pruebas automáticas claras:

**Casos de prueba identificables:**

**Éxito:**
- Crear pedido con datos válidos
- Verificar respuesta 201 con orderId
- Confirmar persistencia en BD con estado PENDING
- Validar publicación de evento a RabbitMQ
- Verificar generación de UUID
- Confirmar timestamps createdAt y updatedAt

**Errores:**
- ProductId inexistente → 400 Bad Request
- ProductId inactivo → 400 Bad Request
- Cantidad cero o negativa → 400 Bad Request
- Lista de items vacía → 400 Bad Request

**Conclusión:** Cumple plenamente con el principio de testeabilidad.

---

## Historia de Usuario #5: Filtrado de Pedidos

### Cuadro Comparativo

| HU original | HU refinada por la Gema | Diferencias detectadas |
|-------------|-------------------------|------------------------|
| **Requisito 5: Filtrado de Pedidos**<br><br>**Historia de Usuario:** Como miembro del personal del restaurante, quiero filtrar pedidos por estado, para poder ver pedidos en etapas específicas de preparación.<br><br>**Criterios de Aceptación:**<br>1. El Order_Service deberá exponer un endpoint GET /orders con un parámetro de consulta status opcional<br>2. Cuando se proporcione el parámetro status, el Order_Service deberá devolver solo pedidos que coincidan con ese estado<br>3. Cuando se omita el parámetro status, el Order_Service deberá devolver todos los pedidos<br>4. El Order_Service deberá soportar filtrado por valores de estado PENDING, IN_PREPARATION y READY<br>5. Si se proporciona un valor de estado inválido, entonces el Order_Service deberá devolver una respuesta 400 Bad Request | **Título:** Filtrado de Pedidos<br><br>**Descripción:** Como miembro del personal del restaurante, quiero filtrar pedidos por estado, para poder ver pedidos en etapas específicas de preparación, **como pendientes, en preparación o listos para servir.**<br><br>**Criterios de Aceptación:**<br>1. **Endpoint GET /orders:** El Order Service deberá exponer un endpoint GET /orders con un parámetro de consulta status opcional<br>2. **Filtrado por Estado:** Cuando se proporcione el parámetro status, el Order Service deberá devolver solo los pedidos que coincidan con ese estado. Cuando se omita el parámetro status, el Order Service deberá devolver todos los pedidos<br>3. **Valores de Estado Soportados:** El Order Service deberá soportar filtrado por los valores de estado **PENDING, IN_PREPARATION y READY (case-sensitive)**<br>4. **Valores de Estado Inválidos:** Si se proporciona un valor de estado inválido, el Order Service deberá devolver una respuesta **HTTP 400 Bad Request con un mensaje descriptivo**<br>5. **Orden de Resultados:** Los pedidos deberán devolverse ordenados por **fecha de creación (createdAt) descendente (más recientes primero)**<br>6. **Exclusión de Pedidos Eliminados:** Los pedidos eliminados **no deberán incluirse** en los resultados | **La Gema añadió:**<br><br>• **Ejemplos específicos:** Se mencionan ejemplos concretos de los estados en la descripción (pendientes, en preparación, listos para servir)<br><br>• **Case-sensitivity:** Se especifica que los valores de estado son case-sensitive (PENDING, IN_PREPARATION, READY)<br><br>• **Código HTTP específico:** Se detalla el código de respuesta HTTP exacto (400 Bad Request)<br><br>• **Mensajes descriptivos:** Se especifica que los errores deben incluir mensajes descriptivos<br><br>• **Orden de resultados:** Se añade criterio explícito sobre el ordenamiento de los resultados (por fecha de creación descendente)<br><br>• **Exclusión de eliminados:** Se aclara que los pedidos eliminados no deben incluirse en los resultados<br><br>• **Organización y títulos:** Se organizan los criterios con títulos descriptivos para mejor legibilidad |

---

### Análisis según Principios INVEST (HU #5: Filtrado de Pedidos)

#### **I - Independiente**

✅ **Evaluación:** La historia es independiente.

El filtrado de pedidos puede implementarse sin depender de otras funcionalidades, salvo la existencia de pedidos en el sistema. No requiere cambios en otras historias de usuario.

**Conclusión:** Cumple con el principio de independencia.

---

#### **N - Negociable**

✅ **Evaluación:** Es altamente negociable.

**Observaciones:**
- El alcance del filtrado puede ampliarse a otros campos en el futuro:
  - Filtrado por mesa (tableId)
  - Filtrado por rango de fechas
  - Filtrado por usuario que creó el pedido
  - Combinación de múltiples filtros


**Recomendación:** Dejar abierta la posibilidad de extender el filtrado a otros campos. La versión refinada establece una base sólida que permite extensiones futuras sin reescribir la funcionalidad existente.

**Conclusión:** Cumple plenamente con el principio de negociabilidad.

---

#### **V - Valiosa**

✅ **Evaluación:** Es valiosa para el negocio.

Esta historia permite al personal del restaurante gestionar el flujo de trabajo de manera eficiente y focalizarse en los pedidos relevantes según su estado.

**Valor de negocio:**
- Mejora la productividad del personal al permitir visualización filtrada
- Facilita la gestión de pedidos en cocina y servicio
- Reduce errores al enfocarse en estados específicos
- Mejora la experiencia operativa del restaurante

**Conclusión:** Cumple plenamente con el principio de valor.

---

#### **E - Estimable**

✅ **Evaluación:** Es estimable.

La funcionalidad y los criterios están claramente definidos:
- Endpoint REST específico (GET /orders con parámetro query)
- Validaciones concretas de valores de estado
- Comportamiento claro para casos válidos e inválidos
- Ordenamiento definido

El equipo de desarrollo puede estimar el esfuerzo necesario con precisión.

**Conclusión:** Cumple con el principio de estimabilidad.

---

#### **S - Small (Pequeña)**

✅ **Evaluación:** Es pequeña y adecuada para una iteración ágil.

**Análisis:**
- La historia está claramente delimitada: agregar lógica de filtrado a un endpoint existente o crear uno nuevo
- Puede completarse en pocos días dentro de un sprint
- No requiere cambios arquitectónicos significativos
- Es una funcionalidad sencilla de implementar y probar

**Conclusión:** Cumple plenamente. Es pequeña y bien delimitada.

---

#### **T - Testeable**

✅ **Evaluación:** Es testeable.

Los criterios de aceptación permiten la construcción de pruebas automáticas claras:

**Casos de prueba identificables:**

**Éxito:**
- Filtrar por STATUS=PENDING → Devuelve solo pedidos con estado PENDING
- Filtrar por STATUS=IN_PREPARATION → Devuelve solo pedidos en preparación
- Filtrar por STATUS=READY → Devuelve solo pedidos listos
- Sin parámetro status → Devuelve todos los pedidos
- Verificar orden descendente por createdAt
- Verificar que pedidos eliminados no aparecen 

**Errores:**
- STATUS=INVALID → 400 Bad Request con mensaje descriptivo
- STATUS=pending (minúsculas) → 400 Bad Request (case-sensitive)
- STATUS=COMPLETED (estado no existente) → 400 Bad Request

**Conclusión:** Cumple plenamente con el principio de testeabilidad.

---

## Historia de Usuario #6: Actualizaciones de Estado de Pedidos

### Cuadro Comparativo

| HU original | HU refinada por la Gema | Diferencias detectadas |
|-------------|-------------------------|------------------------|
| **Requisito 6: Actualizaciones de Estado de Pedidos**<br><br>**Historia de Usuario:** Como miembro del personal del restaurante, quiero actualizar manualmente el estado del pedido, para poder corregir o avanzar estados de pedidos cuando sea necesario.<br><br>**Criterios de Aceptación:**<br>1. El Order_Service deberá exponer un endpoint PATCH /orders/{id}/status que acepte un valor de estado<br>2. Cuando se solicite una actualización de estado válida, el Order_Service deberá actualizar el estado del pedido y la marca de tiempo updatedAt<br>3. El Order_Service deberá aceptar PENDING, IN_PREPARATION y READY como valores de estado válidos<br>4. Si el ID del pedido no existe, entonces el Order_Service deberá devolver una respuesta 404 Not Found<br>5. Si el valor del estado es inválido, entonces el Order_Service deberá devolver una respuesta 400 Bad Request | **Título:** Actualizaciones de Estado de Pedidos<br><br>**Descripción:** Como miembro del personal del restaurante, quiero actualizar manualmente el estado del pedido, para poder corregir o avanzar estados de pedidos cuando sea necesario, **asegurando una gestión eficiente del flujo de pedidos.**<br><br>**Criterios de Aceptación:**<br>1. **Endpoint PATCH /orders/{id}/status:** El Order Service deberá exponer un endpoint PATCH /orders/{id}/status que acepte un valor de estado **en el cuerpo de la solicitud en formato JSON**<br>2. **Actualización de Estado:** Cuando se solicite una actualización de estado válida, el Order Service deberá actualizar el estado del pedido y la marca de tiempo updatedAt<br>3. **Valores de Estado Válidos:** El Order Service deberá aceptar PENDING, IN_PREPARATION y READY como valores de estado válidos<br>4. **ID de Pedido Inexistente:** Si el ID del pedido no existe, el Order Service deberá devolver una respuesta **HTTP 404 Not Found con un mensaje descriptivo**<br>5. **Valor de Estado Inválido:** Si el valor del estado es inválido, el Order Service deberá devolver una respuesta **HTTP 400 Bad Request con un mensaje descriptivo**<br><br>6. **Autenticación y Autorización:** El endpoint deberá requerir **autenticación mediante un token JWT**. Solo usuarios con **roles de cocina o administrador** podrán actualizar el estado de los pedidos<br>7. **Transiciones de Estado:** Se permite **avanzar el estado del pedido desde PENDING a IN_PREPARATION y desde IN_PREPARATION a READY**. **No se permite retroceder o cambiar a estados no consecutivos**<br>8. **Respuesta de Éxito:** Si el estado es actualizado con éxito, el Order Service deberá devolver una respuesta **HTTP 200 OK con el pedido actualizado** | **La Gema añadió:**<br><br>• **Formato del body:** Se especifica que el valor de estado debe enviarse en el cuerpo de la solicitud en formato JSON<br><br>• **Mensajes descriptivos:** Se añade que las respuestas de error deben incluir mensajes descriptivos<br><br>• **Autenticación y autorización:** Se añaden requisitos de seguridad con token JWT y validación de roles (cocina o administrador)<br><br>• **Reglas de transición:** Se especifican explícitamente las transiciones permitidas (solo avance secuencial, no retroceso)<br><br>• **Restricción de estados no consecutivos:** Se prohíbe saltar estados (ej: de PENDING a READY directamente)<br><br>• **Respuesta de éxito detallada:** Se define que la respuesta exitosa debe ser HTTP 200 OK con el pedido actualizado<br><br>• **Propósito ampliado:** Se enfatiza la "gestión eficiente del flujo de pedidos" en la descripción |

---

### Análisis según Principios INVEST (HU #6: Actualizaciones de Estado de Pedidos)

#### **I - Independiente**

✅ **Evaluación:** La historia es independiente.

La actualización manual de estado puede desarrollarse sin depender de otras historias de usuario. Aunque interactúa con el sistema de pedidos existente, no requiere cambios en otras historias para funcionar.

**Conclusión:** Cumple con el principio de independencia.

---

#### **N - Negociable**

✅ **Evaluación:** Es altamente negociable.

**Observaciones:**
- El control de transiciones puede ajustarse según necesidades del negocio
- La posibilidad de revertir estados podría ser negociable (actualmente no permitido)
- Los roles autorizados pueden ampliarse o modificarse
- La publicación de eventos podría ser opcional o incluir más detalles

**Conclusión:** Cumple plenamente con el principio de negociabilidad.

---

#### **V - Valiosa**

✅ **Evaluación:** Es fundamental para la operación.

Esta historia aporta valor directo al personal del restaurante, permitiendo gestionar eficientemente el flujo de pedidos.

**Valor de negocio:**
- Permite correcciones manuales en caso de errores del sistema
- Facilita el avance del flujo de trabajo cuando el proceso automático no aplica
- Mejora la seguridad con control de acceso basado en roles
- Mantiene la consistencia del sistema mediante eventos

**Conclusión:** Cumple plenamente con el principio de valor.

---

#### **E - Estimable**

✅⚠️ **Evaluación:** Es estimable, aunque la refinación añade complejidad.

**Observaciones:**
- Los criterios funcionales son claros
- La versión refinada añade varios componentes:
  - Sistema de auditoría (nueva tabla/entidad)
  - Autenticación y autorización JWT
  - Validación de transiciones de estado
  - Publicación de eventos
  
**Consideración:** Precisar si la infraestructura de autenticación JWT ya existe o debe implementarse desde cero afectará significativamente la estimación.

**Conclusión:** Es estimable, pero la estimación será mayor que la versión original debido a los requisitos añadidos.

---

#### **S - Small (Pequeña)**

⚠️ **Evaluación:** La versión refinada es más grande, pero aún abordable para una iteración.

**Análisis:**
- La versión original era pequeña (simple endpoint PATCH)
- La versión refinada añade:
  2. Autenticación y autorización (si no existe)
  3. Validación de transiciones (lógica de negocio)
  4. Publicación de eventos (integración)

**Conclusión:** Cumple, pero está en el límite. Considerar división si no existe infraestructura de autenticación.

---

#### **T - Testeable**

✅ **Evaluación:** Es altamente testeable.

Los criterios de aceptación permiten la construcción de pruebas automáticas exhaustivas:

**Casos de prueba identificables:**

**Éxito:**
- Actualizar PENDING → IN_PREPARATION → 200 OK
- Actualizar IN_PREPARATION → READY → 200 OK
- Verificar actualización de updatedAt
- Verificar publicación de evento OrderStatusUpdatedEvent
- Verificar respuesta incluye pedido actualizado

**Errores de validación:**
- ID de pedido inexistente → 404 Not Found
- Formato de UUID inválido → 400 Bad Request
- Estado inválido (ej: "INVALID") → 400 Bad Request
- Transición no permitida (READY → PENDING) → 400 Bad Request
- Salto de estado (PENDING → READY) → 400 Bad Request

**Errores de seguridad:**
- Sin token JWT → 401 Unauthorized
- Token JWT expirado → 401 Unauthorized
- Usuario sin rol adecuado → 403 Forbidden
- Token inválido → 401 Unauthorized

**Conclusión:** Cumple plenamente con el principio de testeabilidad.

---

## Conclusión General

Las Historias de Usuario refinadas cumplen con **todos los principios INVEST** de manera satisfactoria. Las mejoras aplicadas aumentan significativamente la claridad, testeabilidad y valor de las historias, facilitando tanto su implementación como su validación.

### Resumen de Historias Analizadas

1. **HU #2 - Creación de Pedidos:** Historia fundamental del sistema que establece el punto de entrada para pedidos. Cumple todos los principios INVEST con mejoras en especificación de cantidades, validaciones explícitas y publicación de eventos.

2. **HU #5 - Filtrado de Pedidos:** Historia valiosa para la gestión operativa que permite al personal filtrar pedidos por estado. Cumple todos los principios INVEST con mejoras en case-sensitivity, ordenamiento y exclusión de eliminados.

3. **HU #6 - Actualizaciones de Estado de Pedidos:** Historia crítica para la gestión del flujo de trabajo que permite actualizaciones manuales controladas. Cumple todos los principios INVEST con mejoras significativas en seguridad (JWT, roles), reglas de transición explícitas y publicación de eventos.

### Comparativa de Complejidad

| Historia | Complejidad Original | Complejidad Refinada | Componentes Añadidos |
|----------|---------------------|---------------------|----------------------|
| HU #2 | Media | Media-Alta | Validación de cantidades, tableId opcional, eventos |
| HU #5 | Baja | Baja-Media | Ordenamiento, case-sensitivity, exclusión de eliminados |
| HU #6 | Baja | Alta |  JWT, autorización, transiciones, eventos |


---

**Documento generado el:** 2 de marzo de 2026  
**Versión:** 3.0  
**Historias Analizadas:** 3 (HU #2, HU #5, HU #6)
