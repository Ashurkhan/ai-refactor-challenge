package kg.example.legacy.service;

import kg.example.legacy.dto.OrderRequest;
import kg.example.legacy.dto.OrderTotalResponse;
import kg.example.legacy.entity.Order;
import kg.example.legacy.exception.OrderNotFoundException;
import kg.example.legacy.repository.OrderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Transactional
    public Order createOrder(OrderRequest request) {
        BigDecimal total = calculateLineTotal(request.getPrice(), request.getQuantity());
        Order order = new Order(
                request.getCustomerName(),
                request.getProductName(),
                request.getPrice(),
                request.getQuantity(),
                total
        );
        return orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public Page<Order> listOrders(Pageable pageable) {
        return orderRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Order> searchOrders(String query, Pageable pageable) {
        return orderRepository.searchByCustomerOrProduct(query, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Order> findExpensiveOrders(BigDecimal minPrice, Pageable pageable) {
        return orderRepository.findByPriceGreaterThan(minPrice, pageable);
    }

    @Transactional(readOnly = true)
    public OrderTotalResponse getOrderTotal(Long orderId) {
        Order order = getOrderOrThrow(orderId);
        return new OrderTotalResponse(order.getId(), calculateLineTotal(order.getPrice(), order.getQuantity()));
    }

    @Transactional
    public Order updateOrder(Long orderId, OrderRequest request) {
        Order order = getOrderOrThrow(orderId);
        order.setCustomerName(request.getCustomerName());
        order.setProductName(request.getProductName());
        order.setPrice(request.getPrice());
        order.setQuantity(request.getQuantity());
        order.setTotal(calculateLineTotal(request.getPrice(), request.getQuantity()));
        return orderRepository.save(order);
    }

    @Transactional
    public void deleteOrder(Long orderId) {
        if (!orderRepository.existsById(orderId)) {
            throw new OrderNotFoundException(orderId);
        }
        orderRepository.deleteById(orderId);
    }

    public BigDecimal calculateLineTotal(BigDecimal price, int quantity) {
        return price.multiply(BigDecimal.valueOf(quantity));
    }

    private Order getOrderOrThrow(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }
}
