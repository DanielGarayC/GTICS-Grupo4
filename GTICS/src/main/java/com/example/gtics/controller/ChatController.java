package com.example.gtics.controller;

import com.example.gtics.entity.Message;
import com.example.gtics.entity.MessageSock;
import com.example.gtics.entity.Usuario;
import com.example.gtics.repository.MessageRepository;
import com.example.gtics.repository.UsuarioRepository;
import com.example.gtics.service.ChatRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;


@Controller
public class ChatController {

    @Autowired
    private ChatRoomService chatRoomService;
    @Autowired
    private MessageRepository messageRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;

    @MessageMapping("/chat/{room}")
    @SendTo("/topic/room/{room}")
    public MessageSock sendMessage(@DestinationVariable String room, MessageSock message, SimpMessageHeaderAccessor headerAccessor) {
        chatRoomService.markRoomAsActive(room);
        Message messagedb  = new Message();
        Usuario sender = usuarioRepository.getReferenceById(message.getSenderId());
        messagedb.setIdUsuario(sender);
        messagedb.setContenido(message.getContent());
        messagedb.setSala(message.getRoom());
        messagedb.setFechaEnvio(LocalDateTime.now());
        messageRepository.save(messagedb);
        // Devolver el mensaje si todo está bien
        return message;
    }
}