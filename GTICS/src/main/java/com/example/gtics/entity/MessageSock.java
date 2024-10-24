
package com.example.gtics.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MessageSock {

    // Contenido del mensaje
    private String content;

    // Nombre del usuario que envía el mensaje
    private int senderId;

    // Sala de chat a la que pertenece el mensaje
    private String room;
}