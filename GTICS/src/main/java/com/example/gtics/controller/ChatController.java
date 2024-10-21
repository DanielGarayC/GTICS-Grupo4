package com.example.gtics.controller;

import com.example.gtics.entity.Message;
import com.example.gtics.service.ChatRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.sql.SQLOutput;


@Controller
public class ChatController {

    @Autowired
    private ChatRoomService chatRoomService;

    @MessageMapping("/chat/{room}")
    @SendTo("/topic/room/{room}")
    public Message sendMessage(@DestinationVariable String room, Message message, SimpMessageHeaderAccessor headerAccessor) {
        chatRoomService.markRoomAsActive(room);

        System.out.println("Contenido del mensaje");
        System.out.println("Envia: "+message.getSenderId());
        System.out.println("Sala: " + message.getRoom());
        System.out.println("Contenido: " + message.getContent());
        // Devolver el mensaje si todo está bien
        return message;
    }
}