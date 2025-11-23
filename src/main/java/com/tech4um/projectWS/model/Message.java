package com.tech4um.projectWS.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.awt.*;

@Data
@Document(collection = "messages")
public class Message {

    @Id
    private String id;

    private String forumId;

    private String senderId;

    private String content;

    private MessageType type = MessageType.PUBLIC;

    private String recipientId; //Usado apenas se TYPE for PRIVATE!

    private Long timestamp = System.currentTimeMillis();

    public enum MessageType {
        PUBLIC,
        PRIVATE
    }
}
