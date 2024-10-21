// Ubicación: /src/main/java/com/example/chatapp/entity/Message.java

package com.example.gtics.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Message {

    // Contenido del mensaje
    private String content;

    // Nombre del usuario que envía el mensaje
    private int senderId;

    // Sala de chat a la que pertenece el mensaje
    private String room;
}