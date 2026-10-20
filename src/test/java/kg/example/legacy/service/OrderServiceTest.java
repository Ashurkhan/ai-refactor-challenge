package kg.example.legacy.service;

import kg.example.legacy.dto.OrderRequest;
import kg.example.legacy.dto.OrderTotalResponse;
import kg.example.legacy.entity.Order;
import kg.example.legacy.exception.OrderNotFoundException;
import kg.example.legacy.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    private OrderRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new OrderRequest();
        validRequest.setCustomerName("Alice");
        validRequest.setProductName("Laptop");
        validRequest.setPrice(new BigDecimal("99.50"));
        validRequest.setQuantity(2);
    }

    @Test
    void calculateLineTotal_multipliesPriceByQuantity() {
        BigDecimal total = orderService.calculateLineTotal(new BigDecimal("10.25"), 3);

        assertThat(total).isEqualByComparingTo(new BigDecimal("30.75"));
    }

    @Test
    void createOrder_persistsOrderWithComputedTotal() {
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order created = orderService.createOrder(validRequest);

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(captor.capture());
        Order persisted = captor.getValue();

        assertThat(persisted.getCustomerName()).isEqualTo("Alice");
        assertThat(persisted.getTotal()).isEqualByComparingTo(new BigDecimal("199.00"));
        assertThat(created.getTotal()).isEqualByComparingTo(new BigDecimal("199.00"));
    }

    @Test
    void getOrderTotal_returnsComputedTotalForExistingOrder() {
        Order order = new Order("Alice", "Laptop", new BigDecimal("50.00"), 4, new BigDecimal("200.00"));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        OrderTotalResponse response = orderService.getOrderTotal(1L);

        assertThat(response.total()).isEqualByComparingTo(new BigDecimal("200.00"));
    }

    @Test
    void getOrderTotal_throwsWhenOrderMissing() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrderTotal(99L))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void updateOrder_appliesChangesAndRecalculatesTotal() {
        Order existing = new Order("Bob", "Phone", new BigDecimal("100.00"), 1, new BigDecimal("100.00"));
        when(orderRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(orderRepository.save(existing)).thenReturn(existing);

        validRequest.setQuantity(3);
        validRequest.setPrice(new BigDecimal("10.00"));

        Order updated = orderService.updateOrder(5L, validRequest);

        assertThat(updated.getCustomerName()).isEqualTo("Alice");
        assertThat(updated.getQuantity()).isEqualTo(3);
        assertThat(updated.getTotal()).isEqualByComparingTo(new BigDecimal("30.00"));
    }

    @Test
    void searchOrders_delegatesToRepositoryQuery() {
        Page<Order> page = new PageImpl<>(List.of());
        PageRequest pageable = PageRequest.of(0, 10);
        when(orderRepository.searchByCustomerOrProduct(eq("lap"), eq(pageable))).thenReturn(page);

        Page<Order> result = orderService.searchOrders("lap", pageable);

        assertThat(result).isSameAs(page);
    }

    @Test
    void deleteOrder_throwsWhenOrderMissing() {
        when(orderRepository.existsById(7L)).thenReturn(false);

        assertThatThrownBy(() -> orderService.deleteOrder(7L))
                .isInstanceOf(OrderNotFoundException.class);
    }
}
