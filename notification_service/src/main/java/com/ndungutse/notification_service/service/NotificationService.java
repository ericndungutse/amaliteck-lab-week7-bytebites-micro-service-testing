package com.ndungutse.notification_service.service;

import com.ndungutse.notification_service.model.Notification;
import com.ndungutse.notification_service.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class NotificationService {
    @Autowired
    private NotificationRepository notificationRepository;

    public List<Notification> getNotificationsByRecipientId(Long recipientId) {
        return notificationRepository.findByRecipientId(recipientId);
    }

    public List<Notification> getAllNotifications() {
        return notificationRepository.findAll();
    }
}