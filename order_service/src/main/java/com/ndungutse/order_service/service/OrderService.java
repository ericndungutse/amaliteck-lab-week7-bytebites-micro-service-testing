package com.ndungutse.order_service.service;

import java.util.List;
import java.util.Optional;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ndungutse.order_service.configuration.RabbitMQConfig;
import com.ndungutse.order_service.model.Order;
import com.ndungutse.order_service.model.OrderStatus;
import com.ndungutse.order_service.repository.OrderRepository;

@Service
public class OrderService {
    @Autowired
    private RabbitTemplate rabbitTemplate;
    private static final String QUEUE_NAME = RabbitMQConfig.QUEUE_NAME;

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    // Create a new order
    public Order createOrder(Order order) {
        // Validate order before creation
        validateOrder(order);

        // Set default status if not provided
        if (order.getStatus() == null) {
            order.setStatus(OrderStatus.PENDING);
        }

        // Send order to RabbitMQ queue
        rabbitTemplate.convertAndSend(QUEUE_NAME, order);
        return orderRepository.save(order);
    }

    // Get order by ID
    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }

    // Get orders by customer ID
    public List<Order> getOrdersByCustomerId(Long customerId) {
        return orderRepository.findByCustomerId(customerId);
    }

    // Get orders by restaurant ID
    public List<Order> getOrdersByRestaurantId(Long restaurantId) {
        return orderRepository.findByRestaurantId(restaurantId);
    }

    // Validate order before creation/update
    private void validateOrder(Order order) {
        if (order.getCustomerId() == null) {
            throw new IllegalArgumentException("Customer ID is required");
        }
        if (order.getRestaurantId() == null) {
            throw new IllegalArgumentException("Restaurant ID is required");
        }
        if (order.getDescription() == null || order.getDescription().trim().isEmpty()) {
            throw new IllegalArgumentException("Order description is required");
        }
        if (order.getTotal_amount() != null && order.getTotal_amount() < 0) {
            throw new IllegalArgumentException("Total amount cannot be negative");
        }
    }
}
