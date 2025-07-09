package com.ndungutse.order_service.service;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.filter.OrderedRequestContextFilter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.ndungutse.order_service.configuration.RabbitMQConfig;
import com.ndungutse.order_service.dto.OrderRequest;
import com.ndungutse.order_service.model.Order;
import com.ndungutse.order_service.model.OrderStatus;
import com.ndungutse.order_service.repository.OrderRepository;
import com.ndungutse.order_service.security.UserPrincipal;

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
    public Order createOrder(OrderRequest orderRequest) {

        Order order = new Order();
        order.setRestaurantId(orderRequest.getRestaurantId());
        order.setDescription(orderRequest.getDescription());
        order.setTotal_amount(orderRequest.getTotalAmount());
        order.setStatus(OrderStatus.PENDING);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Set customer ID from authenticated user
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        Long customerId = Long.parseLong(principal.getUserId());
        order.setCustomerId(customerId);

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
    private void validateOrder(OrderRequest order) {

        if (order.getRestaurantId() == null) {
            throw new IllegalArgumentException("Restaurant ID is required");
        }
        if (order.getDescription() == null || order.getDescription().trim().isEmpty()) {
            throw new IllegalArgumentException("Order description is required");
        }
        if (order.getTotalAmount() != null && order.getTotalAmount() < 0) {
            throw new IllegalArgumentException("Total amount cannot be negative");
        }
    }
}
