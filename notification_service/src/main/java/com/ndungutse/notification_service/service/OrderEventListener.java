package com.ndungutse.notification_service.service;

import com.ndungutse.notification_service.dto.OrderEvent;
import com.ndungutse.notification_service.model.Notification;
import com.ndungutse.notification_service.repository.NotificationRepository;
import com.ndungutse.notification_service.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderEventListener {
    @Autowired
    private NotificationRepository notificationRepository;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void handleOrderPlaced(OrderEvent orderEvent) {
        // Create a notification for the customer
        Notification notification = new Notification();
        notification.setRecipientId(orderEvent.getCustomerId());
        notification.setMessage("Your order has been placed: " + orderEvent.getDescription());
        notification.setRead(false);
        notificationRepository.save(notification);
    }
}