package com.example.gtics.controller;

import com.example.gtics.entity.Message;
import com.example.gtics.service.ChatRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;


@Controller
public class ChatController {

    @Autowired
    private ChatRoomService chatRoomService;

    @MessageMapping("/chat/{room}")
    @SendTo("/topic/room/{room}")
    public Message sendMessage(@DestinationVariable String room, Message message, SimpMessageHeaderAccessor headerAccessor) {
        chatRoomService.markRoomAsActive(room);

        // Devolver el mensaje si todo está bien
        return message;
    }
}