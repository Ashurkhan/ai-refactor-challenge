package kg.example.legacy.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import kg.example.legacy.dto.OrderRequest;
import kg.example.legacy.dto.OrderTotalResponse;
import kg.example.legacy.entity.Order;
import kg.example.legacy.service.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@Validated
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Order add(@Valid @RequestBody OrderRequest request) {
        return orderService.createOrder(request);
    }

    @GetMapping
    public Page<Order> all(
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        return orderService.listOrders(pageable);
    }

    @GetMapping("/search")
    public Page<Order> search(
            @RequestParam @NotBlank String q,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        return orderService.searchOrders(q, pageable);
    }

    @GetMapping("/{id}/total")
    public OrderTotalResponse total(@PathVariable Long id) {
        return orderService.getOrderTotal(id);
    }

    @GetMapping("/expensive")
    public Page<Order> expensive(
            @RequestParam(defaultValue = "100") BigDecimal price,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        return orderService.findExpensiveOrders(price, pageable);
    }

    @PutMapping("/{id}")
    public Order update(@PathVariable Long id, @Valid @RequestBody OrderRequest request) {
        return orderService.updateOrder(id, request);
    }

    @DeleteMapping("/{id}")
    public Map<String, String> delete(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return Map.of("message", "deleted");
    }
}
