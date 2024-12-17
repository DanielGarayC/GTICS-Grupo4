package com.example.gtics.service;
import com.example.gtics.entity.Orden;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public void sendNotificationToUser(String userEmail, String message) {
        messagingTemplate.convertAndSendToUser(userEmail, "/queue/notifications", message);
    }

    public void sendOrderStatusChangeNotification(Orden orden) {
        String message = "El estado de tu orden #" + orden.getId() + " ha cambiado a: " + orden.getEstadoorden().getNombreEstado();
        sendNotificationToUser(orden.getIdCarritoCompra().getIdUsuario().getEmail(), message);
    }
}
