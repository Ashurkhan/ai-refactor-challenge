package kg.example.legacy.repository;

import kg.example.legacy.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("""
            SELECT o FROM Order o
            WHERE LOWER(o.customerName) LIKE LOWER(CONCAT('%', :query, '%'))
               OR LOWER(o.productName) LIKE LOWER(CONCAT('%', :query, '%'))
            """)
    Page<Order> searchByCustomerOrProduct(@Param("query") String query, Pageable pageable);

    Page<Order> findByPriceGreaterThan(BigDecimal price, Pageable pageable);
}
