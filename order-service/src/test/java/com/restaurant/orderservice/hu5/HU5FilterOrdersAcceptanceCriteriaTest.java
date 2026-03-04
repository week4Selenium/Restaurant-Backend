package com.restaurant.orderservice.hu5;

import com.restaurant.orderservice.dto.ErrorResponse;
import com.restaurant.orderservice.dto.OrderResponse;
import com.restaurant.orderservice.entity.Order;
import com.restaurant.orderservice.enums.OrderStatus;
import com.restaurant.orderservice.exception.GlobalExceptionHandler;
import com.restaurant.orderservice.repository.OrderRepository;
import com.restaurant.orderservice.service.OrderMapper;
import com.restaurant.orderservice.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests that validate the acceptance criteria for HU5: Filtrado de Pedidos.
 *
 * Criterios de Aceptación (HU5 refinada):
 * 1. Endpoint GET /orders con parámetro status opcional
 * 2. Filtrado por estado y devolución de todos los pedidos sin filtro
 * 3. Valores de estado case-sensitive: PENDING, IN_PREPARATION, READY
 * 4. Valores de estado inválidos → HTTP 400 con mensaje descriptivo
 * 5. Orden descendente por createdAt (más recientes primero)
 * 6. Exclusión de pedidos eliminados
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("HU5 - Filtrado de Pedidos: Criterios de Aceptación")
class HU5FilterOrdersAcceptanceCriteriaTest {

    @Nested
    @DisplayName("Criterio 3: Valores de Estado Case-Sensitive (PENDING, IN_PREPARATION, READY)")
    class CaseSensitiveStatusTests {

        private GlobalExceptionHandler exceptionHandler;

        @BeforeEach
        void setUp() {
            exceptionHandler = new GlobalExceptionHandler();
        }

        @Test
        @DisplayName("status=pending (minúsculas) → 400 Bad Request con mensaje descriptivo sobre case-sensitivity")
        void lowercaseStatus_Returns400WithDescriptiveCaseSensitiveMessage() {
            // Simulate what Spring does when "pending" can't be converted to OrderStatus
            MethodArgumentTypeMismatchException ex = new MethodArgumentTypeMismatchException(
                    "pending", OrderStatus.class, "status", null, new IllegalArgumentException("No enum constant"));

            // Act
            ResponseEntity<ErrorResponse> response = exceptionHandler.handleTypeMismatch(ex);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getStatus()).isEqualTo(400);
            assertThat(response.getBody().getError()).isEqualTo("Bad Request");
            // Must contain descriptive message about case-sensitivity
            assertThat(response.getBody().getMessage()).contains("case-sensitive");
            assertThat(response.getBody().getMessage()).contains("pending");
            assertThat(response.getBody().getMessage()).contains("PENDING");
            assertThat(response.getBody().getMessage()).contains("IN_PREPARATION");
            assertThat(response.getBody().getMessage()).contains("READY");
        }

        @Test
        @DisplayName("status=in_preparation (minúsculas) → 400 Bad Request con mensaje descriptivo")
        void lowercaseInPreparation_Returns400WithDescriptiveMessage() {
            MethodArgumentTypeMismatchException ex = new MethodArgumentTypeMismatchException(
                    "in_preparation", OrderStatus.class, "status", null, new IllegalArgumentException("No enum constant"));

            ResponseEntity<ErrorResponse> response = exceptionHandler.handleTypeMismatch(ex);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getMessage()).contains("case-sensitive");
            assertThat(response.getBody().getMessage()).contains("in_preparation");
            assertThat(response.getBody().getMessage()).contains("PENDING, IN_PREPARATION, READY");
        }

        @Test
        @DisplayName("status=Ready (mixed case) → 400 Bad Request con mensaje descriptivo")
        void mixedCaseStatus_Returns400WithDescriptiveMessage() {
            MethodArgumentTypeMismatchException ex = new MethodArgumentTypeMismatchException(
                    "Ready", OrderStatus.class, "status", null, new IllegalArgumentException("No enum constant"));

            ResponseEntity<ErrorResponse> response = exceptionHandler.handleTypeMismatch(ex);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getMessage()).contains("case-sensitive");
            assertThat(response.getBody().getMessage()).contains("Ready");
        }

        @Test
        @DisplayName("status=INVALID → 400 Bad Request con mensaje que incluye valores válidos")
        void invalidStatus_Returns400WithValidValuesList() {
            MethodArgumentTypeMismatchException ex = new MethodArgumentTypeMismatchException(
                    "INVALID", OrderStatus.class, "status", null, new IllegalArgumentException("No enum constant"));

            ResponseEntity<ErrorResponse> response = exceptionHandler.handleTypeMismatch(ex);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getMessage()).contains("INVALID");
            assertThat(response.getBody().getMessage()).contains("PENDING");
            assertThat(response.getBody().getMessage()).contains("IN_PREPARATION");
            assertThat(response.getBody().getMessage()).contains("READY");
        }

        @Test
        @DisplayName("Non-enum type mismatch retains generic message")
        void nonEnumTypeMismatch_ReturnsGenericMessage() {
            MethodArgumentTypeMismatchException ex = new MethodArgumentTypeMismatchException(
                    "not-a-uuid", UUID.class, "id", null, new IllegalArgumentException("Invalid UUID"));

            ResponseEntity<ErrorResponse> response = exceptionHandler.handleTypeMismatch(ex);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getMessage()).isEqualTo("Invalid parameter: id");
        }

        @Test
        @DisplayName("status en JSON body con valor minúscula → 400 Bad Request con mensaje descriptivo")
        void jsonBodyWithLowercaseStatus_Returns400WithDescriptiveMessage() {
            // Use mocking to simulate Jackson exception structure
            com.fasterxml.jackson.databind.exc.InvalidFormatException invalidFormatEx = 
                    org.mockito.Mockito.mock(com.fasterxml.jackson.databind.exc.InvalidFormatException.class);
            
            org.mockito.Mockito.doReturn(com.restaurant.orderservice.enums.OrderStatus.class)
                    .when(invalidFormatEx).getTargetType();
            org.mockito.Mockito.when(invalidFormatEx.getValue()).thenReturn("in_preparation");
            
            com.fasterxml.jackson.databind.JsonMappingException.Reference ref = 
                    new com.fasterxml.jackson.databind.JsonMappingException.Reference(null, "status");
            org.mockito.Mockito.when(invalidFormatEx.getPath())
                    .thenReturn(java.util.Collections.singletonList(ref));
            
            org.springframework.http.converter.HttpMessageNotReadableException ex =
                    new org.springframework.http.converter.HttpMessageNotReadableException(
                            "JSON parse error", invalidFormatEx, (org.springframework.http.HttpInputMessage) null);

            ResponseEntity<ErrorResponse> response = exceptionHandler.handleMalformedJson(ex);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getMessage()).contains("case-sensitive");
            assertThat(response.getBody().getMessage()).contains("in_preparation");
            assertThat(response.getBody().getMessage()).contains("PENDING");
            assertThat(response.getBody().getMessage()).contains("IN_PREPARATION");
            assertThat(response.getBody().getMessage()).contains("READY");
        }

        @Test
        @DisplayName("status en query param con valor minúscula → 400 Bad Request con mensaje descriptivo (ConversionFailedException)")
        void queryParamWithLowercaseStatus_Returns400WithDescriptiveMessage() {
            // Simulate Spring ConversionService failure for query parameters
            org.springframework.core.convert.TypeDescriptor sourceType = 
                    org.springframework.core.convert.TypeDescriptor.valueOf(String.class);
            org.springframework.core.convert.TypeDescriptor targetType = 
                    org.springframework.core.convert.TypeDescriptor.valueOf(OrderStatus.class);
            
            org.springframework.core.convert.ConversionFailedException ex =
                    new org.springframework.core.convert.ConversionFailedException(
                            sourceType, targetType, "pending", 
                            new IllegalArgumentException("No enum constant"));

            ResponseEntity<ErrorResponse> response = exceptionHandler.handleConversionFailed(ex);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getMessage()).contains("case-sensitive");
            assertThat(response.getBody().getMessage()).contains("pending");
            assertThat(response.getBody().getMessage()).contains("PENDING, IN_PREPARATION, READY");
        }
    }

    @Nested
    @DisplayName("Criterio 5: Orden Descendente por createdAt")
    class DescendingOrderTests {

        @Mock
        private OrderRepository orderRepository;

        @Mock
        private OrderMapper orderMapper;

        @Mock
        private com.restaurant.orderservice.service.OrderValidator orderValidator;

        @Mock
        private com.restaurant.orderservice.application.port.out.OrderPlacedEventPublisherPort orderPlacedEventPublisherPort;

        @Mock
        private com.restaurant.orderservice.application.port.out.OrderReadyEventPublisherPort orderReadyEventPublisherPort;

        @Mock
        private com.restaurant.orderservice.service.command.OrderCommandExecutor orderCommandExecutor;

        @InjectMocks
        private OrderService orderService;

        @Test
        @DisplayName("getOrders sin filtro → los pedidos se devuelven en orden descendente por createdAt")
        void getOrders_WithoutFilter_ReturnsOrdersInDescendingCreatedAtOrder() {
            // Arrange: Create orders with different creation times
            Order olderOrder = buildOrder(UUID.randomUUID(), OrderStatus.PENDING,
                    LocalDateTime.of(2026, 3, 1, 10, 0));
            Order newerOrder = buildOrder(UUID.randomUUID(), OrderStatus.IN_PREPARATION,
                    LocalDateTime.of(2026, 3, 3, 10, 0));

            // Repository returns them in descending order (as per the updated query)
            List<Order> ordersFromDb = List.of(newerOrder, olderOrder);

            OrderResponse newerResponse = buildOrderResponse(newerOrder);
            OrderResponse olderResponse = buildOrderResponse(olderOrder);
            List<OrderResponse> mappedResponses = List.of(newerResponse, olderResponse);

            when(orderRepository.findAllActive()).thenReturn(ordersFromDb);
            when(orderMapper.mapToOrderResponseList(ordersFromDb)).thenReturn(mappedResponses);

            // Act
            List<OrderResponse> result = orderService.getOrders(null);

            // Assert: newest order should be first
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getCreatedAt()).isAfter(result.get(1).getCreatedAt());
            verify(orderRepository).findAllActive();
        }

        @Test
        @DisplayName("getOrders con filtro → los pedidos filtrados se devuelven en orden descendente por createdAt")
        void getOrders_WithFilter_ReturnsFilteredOrdersInDescendingOrder() {
            List<OrderStatus> filter = List.of(OrderStatus.PENDING);

            Order olderPending = buildOrder(UUID.randomUUID(), OrderStatus.PENDING,
                    LocalDateTime.of(2026, 3, 1, 8, 0));
            Order newerPending = buildOrder(UUID.randomUUID(), OrderStatus.PENDING,
                    LocalDateTime.of(2026, 3, 3, 14, 0));

            List<Order> ordersFromDb = List.of(newerPending, olderPending);
            List<OrderResponse> mappedResponses = List.of(
                    buildOrderResponse(newerPending), buildOrderResponse(olderPending));

            when(orderRepository.findByStatusInActive(filter)).thenReturn(ordersFromDb);
            when(orderMapper.mapToOrderResponseList(ordersFromDb)).thenReturn(mappedResponses);

            List<OrderResponse> result = orderService.getOrders(filter);

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getCreatedAt()).isAfter(result.get(1).getCreatedAt());
            verify(orderRepository).findByStatusInActive(filter);
        }
    }

    @Nested
    @DisplayName("Criterio 6: Exclusión de Pedidos Eliminados")
    class DeletedOrdersExclusionTests {

        @Mock
        private OrderRepository orderRepository;

        @Mock
        private OrderMapper orderMapper;

        @Mock
        private com.restaurant.orderservice.service.OrderValidator orderValidator;

        @Mock
        private com.restaurant.orderservice.application.port.out.OrderPlacedEventPublisherPort orderPlacedEventPublisherPort;

        @Mock
        private com.restaurant.orderservice.application.port.out.OrderReadyEventPublisherPort orderReadyEventPublisherPort;

        @Mock
        private com.restaurant.orderservice.service.command.OrderCommandExecutor orderCommandExecutor;

        @InjectMocks
        private OrderService orderService;

        @Test
        @DisplayName("Después de eliminar un pedido, getOrders no debe incluirlo")
        void afterDeletingOrder_GetOrdersShouldNotIncludeDeletedOrder() {
            // Arrange: 2 orders, one will be deleted
            UUID deletedOrderId = UUID.randomUUID();
            UUID activeOrderId = UUID.randomUUID();

            Order activeOrder = buildOrder(activeOrderId, OrderStatus.PENDING,
                    LocalDateTime.of(2026, 3, 3, 10, 0));
            
            // After deletion, the repository only returns active orders
            List<Order> activeOnlyOrders = List.of(activeOrder);
            List<OrderResponse> mappedResponses = List.of(buildOrderResponse(activeOrder));

            when(orderRepository.findAllActive()).thenReturn(activeOnlyOrders);
            when(orderMapper.mapToOrderResponseList(activeOnlyOrders)).thenReturn(mappedResponses);

            // Act
            List<OrderResponse> result = orderService.getOrders(null);

            // Assert: deleted order should NOT appear
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getId()).isEqualTo(activeOrderId);
            assertThat(result).noneMatch(o -> o.getId().equals(deletedOrderId));
            verify(orderRepository).findAllActive();
        }

        @Test
        @DisplayName("Pedidos eliminados no aparecen al filtrar por estado")
        void deletedOrders_ShouldNotAppearWhenFilteringByStatus() {
            List<OrderStatus> filter = List.of(OrderStatus.PENDING);

            Order activeOrder = buildOrder(UUID.randomUUID(), OrderStatus.PENDING,
                    LocalDateTime.of(2026, 3, 3, 10, 0));
            List<Order> activeOnlyOrders = List.of(activeOrder);
            List<OrderResponse> mappedResponses = List.of(buildOrderResponse(activeOrder));

            when(orderRepository.findByStatusInActive(filter)).thenReturn(activeOnlyOrders);
            when(orderMapper.mapToOrderResponseList(activeOnlyOrders)).thenReturn(mappedResponses);

            List<OrderResponse> result = orderService.getOrders(filter);

            assertThat(result).hasSize(1);
            verify(orderRepository).findByStatusInActive(filter);
            verify(orderRepository, never()).findAllActive();
        }

        @Test
        @DisplayName("markAsDeleted establece deleted=true y deletedAt")
        void markAsDeleted_SetsDeletedFlagAndTimestamp() {
            Order order = new Order();
            order.setId(UUID.randomUUID());
            order.setTableId(5);
            order.setStatus(OrderStatus.PENDING);
            order.setCreatedAt(LocalDateTime.now());
            order.setUpdatedAt(LocalDateTime.now());
            order.setDeleted(false);

            // Act
            order.markAsDeleted();

            // Assert
            assertThat(order.isDeleted()).isTrue();
            assertThat(order.getDeletedAt()).isNotNull();
        }

        @Test
        @DisplayName("Cuando no hay pedidos activos, getOrders devuelve lista vacía")
        void allOrdersDeleted_GetOrdersReturnsEmptyList() {
            when(orderRepository.findAllActive()).thenReturn(List.of());
            when(orderMapper.mapToOrderResponseList(List.of())).thenReturn(List.of());

            List<OrderResponse> result = orderService.getOrders(null);

            assertThat(result).isEmpty();
            verify(orderRepository).findAllActive();
        }
    }

    // ============================================
    // Helper Methods
    // ============================================

    private static Order buildOrder(UUID id, OrderStatus status, LocalDateTime createdAt) {
        Order order = new Order();
        order.setId(id);
        order.setTableId(5);
        order.setStatus(status);
        order.setCreatedAt(createdAt);
        order.setUpdatedAt(createdAt);
        order.setItems(new ArrayList<>());
        order.setDeleted(false);
        return order;
    }

    private static OrderResponse buildOrderResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .tableId(order.getTableId())
                .status(order.getStatus())
                .items(new ArrayList<>())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
