package com.ndungutse.order_service.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ndungutse.order_service.model.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // Find orders by customer ID
    List<Order> findByCustomerId(Long customerId);

    // Find orders by restaurant ID
    List<Order> findByRestaurantId(Long restaurantId);

}
