package com.example.demo.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.example.demo.DTO.LocationMessage;
import java.util.Map;
import java.util.HashMap;

@Controller
public class LocationTrackingController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/location.sendLocation")
    public void sendLocation(@Payload LocationMessage locationMessage) {
        // Handle incoming location updates and route them appropriately
        
        if ("USER_LOCATION".equals(locationMessage.getMessageType())) {
            // User location update - send to the driver assigned to this slot
            // The driver will subscribe to /topic/driver/{driverId}/slots/{slotId}
            messagingTemplate.convertAndSend(
                "/topic/driver/" + locationMessage.getDriverId() + "/slots/" + locationMessage.getSlotId(), 
                locationMessage
            );
            
            // Also send to all users in this slot (for awareness of other users if needed)
            messagingTemplate.convertAndSend(
                "/topic/slots/" + locationMessage.getSlotId() + "/users", 
                locationMessage
            );
        } 
        else if ("DRIVER_LOCATION".equals(locationMessage.getMessageType())) {
            // Driver location update - send to all users in this slot
            // Users will subscribe to /topic/slots/{slotId}/driver
            messagingTemplate.convertAndSend(
                "/topic/slots/" + locationMessage.getSlotId() + "/driver", 
                locationMessage
            );
        }
    }
    
    @MessageMapping("/location.connect")
    public void handleConnection(@Payload LocationMessage connectionMessage) {
        // Handle new connections
        String responseDestination;
        
        if (connectionMessage.getDriverId() != null && !connectionMessage.getDriverId().isEmpty()) {
            // This is a driver connecting
            responseDestination = "/topic/driver/" + connectionMessage.getDriverId() + "/slots/" + connectionMessage.getSlotId();
            
            // Notify all users in this slot that a driver has connected
            messagingTemplate.convertAndSend(
                "/topic/slots/" + connectionMessage.getSlotId() + "/driver/status", 
                "Driver " + connectionMessage.getDriverId() + " connected to slot " + connectionMessage.getSlotId()
            );
        } else {
            // This is a user connecting
            responseDestination = "/topic/slots/" + connectionMessage.getSlotId() + "/users/" + connectionMessage.getUserId();
            
            // Notify the driver of this slot about the new user
            messagingTemplate.convertAndSend(
                "/topic/driver/slots/" + connectionMessage.getSlotId() + "/users/new", 
                connectionMessage
            );
        }
        
        // Send connection confirmation
        messagingTemplate.convertAndSend(responseDestination, "Successfully connected to slot " + connectionMessage.getSlotId());
    }
}
